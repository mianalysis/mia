package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FilterSpecificObjectIDs extends CoreFilter implements ActionListener {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String SHOW_IMAGE = "Show image";
    public static final String DISPLAY_IMAGE_NAME = "Image to display";
    public static final String STORE_RESULTS = "Store filter results";

    private static final String OK = "OK";

    private JFrame frame;
    private JTextField numbersField;
    private int elementHeight = 30;
    private boolean active = false;

    public FilterSpecificObjectIDs(ModuleCollection modules) {
        super("Objects with specific IDs",modules);
    }

    public interface FilterMethods {
        String WITH_MEASUREMENT = "Remove objects with measurement";
        String WITHOUT_MEASUREMENT = "Remove objects without measurement";

        String[] ALL = new String[]{WITH_MEASUREMENT, WITHOUT_MEASUREMENT};

    }


    public String getFullName(String inputObjectsName, String filterMethod, String measName) {
        switch (filterMethod) {
            case FilterMethods.WITH_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITH " + measName + " MEASUREMENT";
            case FilterMethods.WITHOUT_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITHOUT " + measName + " MEASUREMENT";
            default:
                return "";
        }
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
        c.insets = new Insets(5,5,5,5);

        // Header panel
        JLabel headerLabel = new JLabel("<html>Specify object IDs to remove (comma separated)</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        headerLabel.setPreferredSize(new Dimension(300,elementHeight));
        frame.add(headerLabel,c);

        numbersField = new JTextField();
        numbersField.setPreferredSize(new Dimension(200, elementHeight));
        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        frame.add(numbersField,c);

        JButton okButton = new JButton(OK);
        okButton.addActionListener(this);
        okButton.setActionCommand(OK);
        okButton.setPreferredSize(new Dimension(300,elementHeight));
        c.gridy++;
        frame.add(okButton,c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

    }

    public int filter(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, @Nullable Image image) {
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
                e.printStackTrace();
            }
        }

        int[] ids = CommaSeparatedStringInterpreter.interpretIntegers(numbersField.getText(),true);

        frame.dispose();
        frame = null;
        if (ipl != null) ipl.close();

        int count = 0;
        for (int id:ids) {
            Obj obj = inputObjects.get(id);
            count++;
            if (remove) {
                obj.removeRelationships();
                if (outputObjects != null) {
                    obj.setName(outputObjects.getName());
                    outputObjects.add(obj);
                }
                inputObjects.remove(id);
            }
        }

        return count;

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String measName = parameters.getValue(MEASUREMENT);
        boolean showImage = parameters.getValue(SHOW_IMAGE);
        String displayImageName = parameters.getValue(DISPLAY_IMAGE_NAME);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName) : null;
        Image displayImage = showImage ? workspace.getImage(displayImageName) : null;

        int count = filter(inputObjects,outputObjects,remove,displayImage);

        // If moving objects, addRef them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getFullName(inputObjectsName,filterMethod,measName);
        workspace.getMetadata().put(metadataName,count);

        // Showing objects
        if (showOutput) showRemainingObjects(inputObjects);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE,this, FilterModes.REMOVE_FILTERED, FilterModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.WITHOUT_MEASUREMENT, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));
        if (parameters.getValue(SHOW_IMAGE)) {
            returnedParameters.add(parameters.getParameter(DISPLAY_IMAGE_NAME));
        }

        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName,this);

            for (ObjMeasurementRef reference:references.values()) {
                returnedRefs.add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }

            return returnedRefs;

        }

        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole set
        if (parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String measName = parameters.getValue(MEASUREMENT);

            String metadataName = getFullName(inputObjectsName,filterMethod,measName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case (OK):
                active = false;
                break;
        }
    }
}
