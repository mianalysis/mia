package io.github.mianalysis.mia.module.objects.filter;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.drew.lang.annotations.Nullable;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AddLabels;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.imagej.LUTs;
import io.github.sjcross.sjcommon.process.CommaSeparatedStringInterpreter;

/**
 * Created by sc13967 on 23/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FilterObjects extends Module implements ActionListener {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String INCLUDE_Z_POSITION = "Include Z-position";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REFERENCE_VAL_IMAGE = "Reference value image";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_VAL_PARENT_OBJECT = "Reference value parent object";
    public static final String REFERENCE_OBJECT_MEASUREMENT = "Reference object measurement";
    public static final String REFERENCE_MULTIPLIER = "Reference value multiplier";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DISPLAY_IMAGE_NAME = "Image to display";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    public FilterObjects(Modules modules) {
        super("Filter objects", modules);
        deprecated = true;
    }

    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED_OBJECTS = "Move filtered objects to new class";
        String REMOVE_FILTERED_OBJECTS = "Remove filtered objects";

        String[] ALL = new String[] { DO_NOTHING, MOVE_FILTERED_OBJECTS, REMOVE_FILTERED_OBJECTS };

    }

    public interface FilterMethods {
        String MISSING_MEASUREMENTS = "Remove objects with missing measurements"; // TRANSFERRED
        String NO_PARENT = "Remove objects without parent"; // TRANSFERRED
        String WITH_PARENT = "Remove objects with a parent"; // TRANSFERRED
        String MIN_NUMBER_OF_CHILDREN = "Remove objects with fewer children than:"; // TRANSFERRED
        String MAX_NUMBER_OF_CHILDREN = "Remove objects with more children than:"; // TRANSFERRED
        String MEASUREMENTS_SMALLER_THAN = "Remove objects with measurements < than:"; // TRANSFERRED
        String MEASUREMENTS_LARGER_THAN = "Remove objects with measurements > than:"; // TRANSFERRED
        String REMOVE_ON_IMAGE_EDGE_2D = "Exclude objects on image edge (2D)"; // TRANSFERRED
        String RUNTIME_OBJECT_ID = "Specific object IDs (runtime)"; // TRANSFERRED

        String[] ALL = new String[] { REMOVE_ON_IMAGE_EDGE_2D, MISSING_MEASUREMENTS, NO_PARENT, WITH_PARENT,
                MIN_NUMBER_OF_CHILDREN, MAX_NUMBER_OF_CHILDREN, MEASUREMENTS_SMALLER_THAN, MEASUREMENTS_LARGER_THAN,
                RUNTIME_OBJECT_ID };

    }

    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[] { FIXED_VALUE, IMAGE_MEASUREMENT, PARENT_OBJECT_MEASUREMENT };

    }

    private void showOptionsPanel() {
        active = true;
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Specify object IDs to remove (comma separated)</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        headerLabel.setPreferredSize(new Dimension(300, elementHeight));
        frame.add(headerLabel, c);

        numbersField = new JTextField();
        numbersField.setPreferredSize(new Dimension(200, elementHeight));
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        frame.add(numbersField, c);

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this);
        okButton.setActionCommand(OK);
        okButton.setPreferredSize(new Dimension(300, elementHeight));
        c.gridy++;
        frame.add(okButton, c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public String getFullName(String inputObjectsName, String filterMethod, @Nullable String referenceName,
            @Nullable String filterValue) {
        switch (filterMethod) {
            case FilterMethods.MISSING_MEASUREMENTS:
                return "FILTER // NUM_" + inputObjectsName + "_MISSING_" + referenceName + "_MEAS";

            case FilterMethods.NO_PARENT:
                return "FILTER // NUM_" + inputObjectsName + "_MISSING_" + referenceName + "_PARENT";

            case FilterMethods.WITH_PARENT:
                return "FILTER // NUM_" + inputObjectsName + "_WITH_" + referenceName + "_PARENT";

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                return "FILTER // NUM_" + inputObjectsName + "_LESS_THAN_" + filterValue + "_" + referenceName;

            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                return "FILTER // NUM_" + inputObjectsName + "_MORE_THAN_" + filterValue + "_" + referenceName;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
                return "FILTER // NUM_" + inputObjectsName + "_WITH_" + referenceName + "_SMALLER_THAN_" + filterValue;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                return "FILTER // NUM_" + inputObjectsName + "_WITH_" + referenceName + "_LARGER_THAN_" + filterValue;

            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                return "FILTER // NUM_" + inputObjectsName + "_ON_" + referenceName + "_EDGE_2D";

            case FilterMethods.RUNTIME_OBJECT_ID:
                return "FILTER // NUM_" + inputObjectsName + "_MATCHING_GIVEN_ID";

        }

        return "";

    }

    public void filterObjectsOnImageEdge(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, boolean includeZ) {
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = inputObjects.getSpatialCalibration().getWidth() - 1;
        int maxY = inputObjects.getSpatialCalibration().getHeight() - 1;
        int maxZ = inputObjects.getSpatialCalibration().getNSlices() - 1;

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            ArrayList<Integer> x = inputObject.getXCoords();
            ArrayList<Integer> y = inputObject.getYCoords();
            ArrayList<Integer> z = inputObject.getZCoords();

            for (int i = 0; i < x.size(); i++) {
                if (x.get(i) == minX | x.get(i) == maxX | y.get(i) == minY | y.get(i) == maxY) {
                    if (remove)
                        processRemoval(inputObject, outputObjects, iterator);
                    break;
                }

                // Only consider Z if the user requested this
                if (includeZ && (z.get(i) == minZ | z.get(i) == maxZ)) {
                    if (remove)
                        processRemoval(inputObject, outputObjects, iterator);
                    break;
                }
            }
        }
    }

    public void filterObjectsWithMissingMeasurement(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String measurement) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            if (Double.isNaN(inputObject.getMeasurement(measurement).getValue())) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithoutAParent(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String, Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) == null) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithAParent(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String, Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) != null) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithMinNumOfChildren(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String childObjectsName, double minChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Objs childObjects = inputObject.getChildren(childObjectsName);

            // Removing the object if it has no children
            if (childObjects == null) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
                continue;
            }

            // Removing the object if it has too few children
            if (childObjects.size() < minChildN) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithMaxNumOfChildren(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String childObjectsName, double maxChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Objs childObjects = inputObject.getChildren(childObjectsName);

            if (childObjects == null)
                continue;

            // Removing the object if it has too few children
            if (childObjects.size() > maxChildN) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithMeasSmallerThan(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String measurementName, MeasRef measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
                continue;
            }

            double value = measurement.getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }

            if (value < referenceValue || Double.isNaN(value)) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsWithMeasLargerThan(Objs inputObjects, @Nullable Objs outputObjects,
            boolean remove, String measurementName, MeasRef measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
                continue;
            }

            // Removing the object if it has no children
            double value = measurement.getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }

            if (value > referenceValue || Double.isNaN(value)) {
                if (remove)
                    processRemoval(inputObject, outputObjects, iterator);
            }
        }
    }

    public void filterObjectsByID(Objs inputObjects, @Nullable Objs outputObjects, boolean remove,
            @Nullable Image image) {
        ImagePlus ipl = null;
        if (image != null) {
            ipl = image.getImagePlus().duplicate();
            ipl.show();
        }

        showOptionsPanel();

        // All the while the control is open, do nothing
        while (active) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }
        }

        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(numbersField.getText(), true, 0);

        frame.dispose();
        frame = null;
        if (ipl != null)
            ipl.close();

        for (int id : ids) {
            Obj obj = inputObjects.get(id);
            if (remove) {
                obj.removeRelationships();
                if (outputObjects != null) {
                    obj.setObjectCollection(outputObjects);
                    outputObjects.add(obj);
                }
                inputObjects.remove(id);
            }
        }
    }

    void processRemoval(Obj inputObject, Objs outputObjects, Iterator<Obj> iterator) {
        inputObject.removeRelationships();
        if (outputObjects != null) {
            inputObject.setObjectCollection(outputObjects);
            outputObjects.add(inputObject);
        }
        iterator.remove();
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED:  Please use individual filter modules instead (e.g. \""+new FilterByMeasurement(null).getName()+"\").<br><br>"
        
        +"Filter an object collection based on a variety of properties.  Objects that satisfy the relevant condition can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String method = parameters.getValue(FILTER_METHOD);
        boolean includeZ = parameters.getValue(INCLUDE_Z_POSITION);
        String measurement = parameters.getValue(MEASUREMENT);
        String parentObjectName = parameters.getValue(PARENT_OBJECT);
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        String referenceValueImage = parameters.getValue(REFERENCE_VAL_IMAGE);
        String referenceValueParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
        String referenceImageMeasurement = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String referenceObjectMeasurement = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);
        double referenceMultiplier = parameters.getValue(REFERENCE_MULTIPLIER);
        boolean showImage = parameters.getValue(SHOW_IMAGE);
        String displayImageName = parameters.getValue(DISPLAY_IMAGE_NAME);

        Objs outputObjects = filterMode.equals(FilterModes.MOVE_FILTERED_OBJECTS)
                ? new Objs(outputObjectsName, inputObjects)
                : null;

        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        // Removing objects with a missing measurement (i.e. value set to null)
        switch (method) {
            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                filterObjectsOnImageEdge(inputObjects, outputObjects, remove, includeZ);
                break;

            case FilterMethods.MISSING_MEASUREMENTS:
                filterObjectsWithMissingMeasurement(inputObjects, outputObjects, remove, measurement);
                break;

            case FilterMethods.NO_PARENT:
                filterObjectsWithoutAParent(inputObjects, outputObjects, remove, parentObjectName);
                break;

            case FilterMethods.WITH_PARENT:
                filterObjectsWithAParent(inputObjects, outputObjects, remove, parentObjectName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                filterObjectsWithMinNumOfChildren(inputObjects, outputObjects, remove, childObjectsName,
                        referenceValue);
                break;

            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                filterObjectsWithMaxNumOfChildren(inputObjects, outputObjects, remove, childObjectsName,
                        referenceValue);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
                MeasRef measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasRef(ReferenceModes.FIXED_VALUE, referenceValue, 1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement)
                                .getValue();
                        measurementReference = new MeasRef(ReferenceModes.IMAGE_MEASUREMENT, value,
                                referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasRef(ReferenceModes.PARENT_OBJECT_MEASUREMENT,
                                referenceValueParent, referenceObjectMeasurement, referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasSmallerThan(inputObjects, outputObjects, remove, measurement,
                        measurementReference);
                break;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasRef(ReferenceModes.FIXED_VALUE, referenceValue, 1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement)
                                .getValue();
                        measurementReference = new MeasRef(ReferenceModes.IMAGE_MEASUREMENT, value,
                                referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasRef(ReferenceModes.PARENT_OBJECT_MEASUREMENT,
                                referenceValueParent, referenceObjectMeasurement, referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasLargerThan(inputObjects, outputObjects, remove, measurement, measurementReference);
                break;

            case FilterMethods.RUNTIME_OBJECT_ID:
                Image displayImage = showImage ? workspace.getImage(displayImageName) : null;
                filterObjectsByID(inputObjects, outputObjects, remove, displayImage);
                break;
        }

        if (filterMode.equals(FilterModes.MOVE_FILTERED_OBJECTS))
            workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageRandomColours().showImage(LUTs.Random(true));

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.REMOVE_FILTERED_OBJECTS, FilterModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.REMOVE_ON_IMAGE_EDGE_2D, FilterMethods.ALL));
        parameters.add(new BooleanP(INCLUDE_Z_POSITION, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIXED_VALUE, ReferenceModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 1d));
        parameters.add(new InputImageP(REFERENCE_VAL_IMAGE, this));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(REFERENCE_VAL_PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_OBJECT_MEASUREMENT, this));
        parameters.add(new DoubleP(REFERENCE_MULTIPLIER, this, 1d));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));

        switch ((String) parameters.getValue(FILTER_METHOD)) {
            case FilterMethods.MISSING_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
                }
                break;

            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                returnedParameters.add(parameters.getParameter(INCLUDE_Z_POSITION));
                break;

            case FilterMethods.NO_PARENT:
            case FilterMethods.WITH_PARENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
                switch ((String) parameters.getValue(REFERENCE_MODE)) {
                    case ReferenceModes.FIXED_VALUE:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                        break;

                    case ReferenceModes.IMAGE_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VAL_IMAGE));
                        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                        String referenceValueImageName = parameters.getValue(REFERENCE_VAL_IMAGE);
                        ((ImageMeasurementP) parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT))
                                .setImageName(referenceValueImageName);
                        break;

                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                        String referenceValueParentObjectsName = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
                        ((ParentObjectsP) parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT))
                                .setChildObjectsName(inputObjectsName);
                        ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT))
                                .setObjectName(referenceValueParentObjectsName);
                        break;
                }
                break;

            case FilterMethods.RUNTIME_OBJECT_ID:
                returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
                returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));
                break;

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        // If the filtered objects are to be moved to a new class, assign them the
        // measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED_OBJECTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefs references = modules.getObjectMeasurementRefs(inputObjectsName, this);

            for (ObjMeasurementRef reference : references.values()) {
                returnedRefs
                        .add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }

            return returnedRefs;

        }

        return null;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects to be filtered.");

        parameters.get(FILTER_MODE)
                .setDescription("Controls what happens to objects which don't pass the filter:<br><ul>" + "<li>\""
                        + FilterModes.DO_NOTHING
                        + "\" Retains all input objects, irrespective of whether they passed or failed the filter.  This is useful when also storing the filter results as metadata values (i.e. just counting the number of objects which pass the filter).</li>"

                        + "<li>\"" + FilterModes.MOVE_FILTERED_OBJECTS
                        + "\" Objects failing the filter are moved to a new object class.  The name of the class is determined by the \""
                        + OUTPUT_FILTERED_OBJECTS
                        + "\" parameter.  All existing measurements and relationships are carried forward into the new object collection.</li>"

                        + "<li>\"" + FilterModes.REMOVE_FILTERED_OBJECTS
                        + "\" (default) Removes objects failing the filter.  Once removed, these objects are unavailable for further use by modules and won't be included in exported results.</li></ul>");

        parameters.get(OUTPUT_FILTERED_OBJECTS).setDescription(
                "New object collection containing input objects which did not pass the filter.  These objects are only stored if \""
                        + FILTER_MODE + "\" is set to \"" + FilterModes.MOVE_FILTERED_OBJECTS + "\".");

        parameters.get(FILTER_METHOD).setDescription("Controls the type of filter being used:<br><ul>" + "<li>\""
                + FilterMethods.REMOVE_ON_IMAGE_EDGE_2D
                + "\" Remove any objects based on contact with the image edge.  Contact is considered as a case where an object pixel is in the outer-most row, column or slice  of an image (e.g. x = 0 or y = max_value).  Can optionally also consider top and bottom of 3D stack (Z-position).</li>"

                + "<li>\"" + FilterMethods.MISSING_MEASUREMENTS
                + "\" Remove any objects without a specific measurement value (i.e. values with NaN values).</li>"

                + "<li>\"" + FilterMethods.NO_PARENT
                + "\" Remove any objects without an assigned parent from a specific object collection.</li>"

                + "<li>\"" + FilterMethods.WITH_PARENT
                + "\" Remove any objects with an assigned parent from a specific object collection.</li>"

                + "<li>\"" + FilterMethods.MIN_NUMBER_OF_CHILDREN
                + "\" Remove any objects with fewer children from a specific object collection than a threshold value.</li>"

                + "<li>\"" + FilterMethods.MAX_NUMBER_OF_CHILDREN
                + "\" Remove any objects with more children from a specific object collection than a threshold value.</li>"

                + "<li>\"" + FilterMethods.MEASUREMENTS_SMALLER_THAN
                + "\" Remove any objects with a smaller measurement value a specific threshold.</li>"

                + "<li>\"" + FilterMethods.MEASUREMENTS_LARGER_THAN
                + "\" Remove any objects with a larger measurement value a specific threshold.</li>"

                + "<li>\"" + FilterMethods.RUNTIME_OBJECT_ID
                + "\" Remove objects by their specific ID numbers.  This is a manual option, whereby an image is displayed to the user (pre-prepared to show object ID numbers) along with a dialog box allowing the user to write a comma-separated list of objects to remove.</li></ul>");

        parameters.get(INCLUDE_Z_POSITION).setDescription(
                "When selected, object pixels which make contact with the lower (z = 0) and upper (z = max_value) slices of the image stack will lead to removal of that object.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).  If enabled for single slice stacks all objects will removed.");

        parameters.get(MEASUREMENT).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""
                        + new FilterWithWithoutMeasurement(null).getName() + "\".");

        parameters.get(PARENT_OBJECT).setDescription(
                "Parent object to filter by.  The presence or absence of this relationship will determine which of the input objects are counted, removed or moved (depending on the \""
                        + FILTER_MODE + "\" parameter).");

        parameters.get(CHILD_OBJECTS).setDescription(
                "Objects will be filtered against the number of children they have from this object collection.");

        parameters.get(REFERENCE_MODE).setDescription("Type of reference value used to compare objects to:<br><ul>"
                + "<li>\"" + ReferenceModes.FIXED_VALUE
                + "\" Objects will be compared to a single, fixed value specified using the \"" + REFERENCE_VALUE
                + "\" parameter.</li>"

                + "<li>\"" + ReferenceModes.IMAGE_MEASUREMENT
                + "\" Objects will be compared to a measurement associated with an image in the workspace.  The image and associated measurement are specified by the \""
                + REFERENCE_VAL_IMAGE + "\" and \"" + REFERENCE_IMAGE_MEASUREMENT
                + "\" parameters.  In this mode, all objects will still be compared to the same value; however, this value can vary between input files.</li>"

                + "<li>\"" + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\" Objects will be compared to a measurement associated with a parent object.  The parent object and measurement are specified by the \""
                + REFERENCE_VAL_PARENT_OBJECT + "\" and \"" + REFERENCE_OBJECT_MEASUREMENT
                + "\" parameters.  In this mode all objects can (but won't necessarily) be compared to different values.</li></ul>");

        parameters.get(REFERENCE_VALUE).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.FIXED_VALUE + "\", all objects will be compared to this fixed value.");

        parameters.get(REFERENCE_VAL_IMAGE)
                .setDescription("When \"" + REFERENCE_MODE + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", all objects will be compared to a measurement associated with this image.");

        parameters.get(REFERENCE_IMAGE_MEASUREMENT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.IMAGE_MEASUREMENT
                + "\", all objects will be compared to this measurement, which itself is associated with the image specified by \""
                + REFERENCE_VAL_IMAGE + "\".");

        parameters.get(REFERENCE_VAL_PARENT_OBJECT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\", all objects will be compared to a measurement associated with their parent object from this collection.");

        parameters.get(REFERENCE_OBJECT_MEASUREMENT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\", all objects will be compared to this measurement, which itself is associated with the parent object specified by \""
                + REFERENCE_VAL_PARENT_OBJECT + "\".");

        parameters.get(REFERENCE_MULTIPLIER).setDescription(
                "Irrespective of how the reference value was determined, it can be systematically adjusted prior to use for comparison using this multiplier.  For example, an image measurement reference value of 32 with a \""
                        + REFERENCE_MULTIPLIER
                        + "\" of \"0.5\" will see all objects compared against a value of 16.  This is useful when comparing to dynamic values coming from image and parent object measurements.");

        parameters.get(SHOW_IMAGE).setDescription(
                "When selected, a specific image will be displayed when this module executes.  This can be used to display a pre-prepared, object ID-labelled image to the user, thus acting as a reference for which object IDs to remove.  The image to be displayed is set using the \""
                        + DISPLAY_IMAGE_NAME + "\" parameter.");

        parameters.get(DISPLAY_IMAGE_NAME).setDescription(
                "Image to display when the module executes.  For example, this could be a pre-prepared image with object IDs inserted as text overlays using the \""
                        + new AddLabels(null).getName() + "\" module.");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (OK):
                active = false;
                break;
        }
    }

public class MeasRef {
        final String referenceType;
        final double value;
        final String referenceName;
        final String referenceMeasurementName;
        final double referenceMultiplier;

        /*
         * Constructing a fixed- or image-value type
         */
        public MeasRef(String referenceType, double value, double referenceMultiplier) {
            this.referenceType = referenceType;
            this.value = value;
            this.referenceName = null;
            this.referenceMeasurementName = null;
            this.referenceMultiplier = referenceMultiplier;

        }

        /*
         * Constructing a parent-reference type
         */
        public MeasRef(String referenceType, String referenceName, String referenceMeasurementName,
                double referenceMultiplier) {
            this.referenceType = referenceType;
            this.value = 0;
            this.referenceName = referenceName;
            this.referenceMeasurementName = referenceMeasurementName;
            this.referenceMultiplier = referenceMultiplier;

        }

        public String getReferenceType() {
            return referenceType;
        }

        public double getValue(@Nullable Obj object) {
            switch (referenceType) {
                case ReferenceModes.FIXED_VALUE:
                case ReferenceModes.IMAGE_MEASUREMENT:
                    return value * referenceMultiplier;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                    if (object.getParent(referenceName) == null)
                        return Double.NaN;
                    return object.getParent(referenceName).getMeasurement(referenceMeasurementName).getValue()
                            * referenceMultiplier;
            }

            return 0;

        }

        public String getReferenceName() {
            return referenceName;
        }

        public String getReferenceMeasurementName() {
            return referenceMeasurementName;
        }

        public double getReferenceMultiplier() {
            return referenceMultiplier;
        }
    }
}
