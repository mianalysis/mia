package io.github.mianalysis.mia.module.objects.filter;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import net.imagej.ImageJ;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)

public class LiveFilterByMeasurement extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";

    /**
     * Objects will be filtered against their value of this measurement. Objects
     * missing this measurement are not removed; however, they can be removed by
     * using the module "With / without measurement".
     */
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String LIVE_SELECTION = "Live selection";

    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";

    public interface FilterMethods extends AbstractNumericObjectFilter.FilterMethods {
    }

    public LiveFilterByMeasurement(Modules modules) {
        // The first argument is the name by which the module will be seen in the GUI.
        super("Live measurement filter", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static String getIndividualFixedValueFullName(String filterMethod, String targetName,
            String referenceValue) {
        String filterMethodSymbol = AbstractNumericObjectFilter.getFilterMethodSymbol(filterMethod);
        return "FILTER // " + targetName + " " + filterMethodSymbol + " " + referenceValue;
    }

    public static String getSummaryFixedValueFullName(String inputObjectsName, String filterMethod, String targetName,
            String referenceValue) {
        String filterMethodSymbol = AbstractNumericObjectFilter.getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + targetName + " " + filterMethodSymbol + " "
                + referenceValue;
    }

    public double getThreshold(Objs inputObjects, String measName, double refValue, String filterMethod) {
        ImagePlus showIpl = inputObjects.convertToImageIDColours().getImagePlus();
        showIpl.getProcessor().resetMinAndMax();
        ImagePlus testIpl = showIpl.duplicate();

        JFrame frame = new JFrame();
        frame.setTitle("Select threshold");
        frame.setLayout(new FlowLayout(FlowLayout.CENTER));

        JTextField thresholdTextField = new JTextField();
        thresholdTextField.setText(String.valueOf(refValue));
        thresholdTextField.selectAll();
        thresholdTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                testThreshold(inputObjects, measName, thresholdTextField, filterMethod, testIpl);
            }
        });
        frame.add(thresholdTextField);

        JButton acceptButton = new JButton();
        acceptButton.setText("Accept");
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
                showIpl.close();
                testIpl.close();
            }
        });
        frame.add(acceptButton);

        testThreshold(inputObjects, measName, thresholdTextField, filterMethod, testIpl);

        showIpl.show();
        testIpl.show();

        frame.pack();
        frame.setLocation(100, 100);
        frame.setVisible(true);
        frame.setResizable(false);

        // All the while the control is open, do nothing
        while (frame.isVisible() && showIpl != null && testIpl != null)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }

        return Double.parseDouble(thresholdTextField.getText());

    }

    static void testThreshold(Objs inputObjects, String measName, JTextField thresholdTextField, String filterMethod,
            ImagePlus testIpl) {
        double refValue = Double.parseDouble(thresholdTextField.getText());

        process(inputObjects, measName, refValue, filterMethod, true, false, null);

        String measurementName = getIndividualFixedValueFullName(filterMethod, measName, String.valueOf(refValue));
        HashMap<Integer, Float> hues = ColourFactory.getMeasurementValueHues(inputObjects, measurementName, false,
                new double[] { -1, 1 });
        Image dispImage = inputObjects.convertToImage("Thresholded", hues, 32, true);
        SetLookupTable.setLUT(dispImage, LUTs.BlackSpectrum(), SetLookupTable.ChannelModes.ALL_CHANNELS, 0);

        // Replacing
        testIpl.setStack(dispImage.getImagePlus().getStack());
        testIpl.setTitle("Threshold @ " + refValue);
        testIpl.getProcessor().setMinAndMax(-1, 1);

        thresholdTextField.grabFocus();
        thresholdTextField.selectAll();

        // Removing temporary measurements
        for (Obj inputObj : inputObjects.values())
            inputObj.removeMeasurement(measurementName);

    }

    public static int process(Objs inputObjects, String measName, double refValue, String filterMethod,
            boolean storeIndividual, boolean remove, Objs outputObjects) {
        String measurementName = getIndividualFixedValueFullName(filterMethod, measName, String.valueOf(refValue));

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Skipping this object if it doesn't have the measurement
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null)
                continue;

            double value = measurement.getValue();

            // Checking for blank measurements
            if (Double.isNaN(refValue) || Double.isNaN(value))
                continue;

            // Checking the main filter
            boolean conditionMet = AbstractNumericObjectFilter.testFilter(value, refValue, filterMethod);

            // Adding measurements
            if (storeIndividual)
                inputObject.addMeasurement(new Measurement(measurementName, conditionMet ? 1 : 0));

            if (conditionMet) {
                count++;
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }

        return count;

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        String measName = parameters.getValue(MEASUREMENT, workspace);
        double refValue = parameters.getValue(REFERENCE_VALUE, workspace);
        boolean liveSelection = parameters.getValue(LIVE_SELECTION, workspace);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS, workspace);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace);
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        // Getting input objects
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        if (liveSelection)
            refValue = getThreshold(inputObjects, measName, refValue, filterMethod);

        // Updating threshold value parameter and updating GUI
        parameters.getParameter(REFERENCE_VALUE).setValue(refValue);
        GUI.updateParameters(false, null);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        int count = process(inputObjects, measName, refValue, filterMethod, storeIndividual, remove, outputObjects);

        // If moving objects, add them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String metadataName = getSummaryFixedValueFullName(inputObjectsName, filterMethod, measName,
                    String.valueOf(refValue));
            workspace.getMetadata().put(metadataName, count);
        }

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 1d));
        parameters.add(new BooleanP(LIVE_SELECTION, this, true));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
        returnedParameters.add(parameters.getParameter(LIVE_SELECTION));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = super.updateAndGetObjectMeasurementRefs();

        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS, workspace)) {
            String measName = parameters.getValue(MEASUREMENT, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            double refValue = parameters.getValue(REFERENCE_VALUE, workspace);
            String measurementName = getIndividualFixedValueFullName(filterMethod, measName, String.valueOf(refValue));
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

            returnedRefs.add(new ObjMeasurementRef(measurementName, inputObjectsName));
            if (parameters.getValue(FILTER_MODE, workspace).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
                returnedRefs.add(new ObjMeasurementRef(measurementName, outputObjectsName));
            }
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS, workspace)) {
            String measName = parameters.getValue(MEASUREMENT, workspace);
            String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
            double refValue = parameters.getValue(REFERENCE_VALUE, workspace);
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String metadataName = getSummaryFixedValueFullName(inputObjectsName, filterMethod, measName,
                    String.valueOf(refValue));

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}