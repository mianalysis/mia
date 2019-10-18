package wbif.sjx.MIA.Module.Deprecated;

import javax.annotation.Nullable;
import javax.swing.*;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.MIA.Process.CommaSeparatedStringInterpreter;
import wbif.sjx.common.Object.LUTs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends Module implements ActionListener {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_IMAGE = "Reference image";
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

    public FilterObjects(ModuleCollection modules) {
        super("Filter objects",modules);
    }


    public interface FilterModes {
        String DO_NOTHING = "Do nothing";
        String MOVE_FILTERED_OBJECTS = "Move filtered objects to new class";
        String REMOVE_FILTERED_OBJECTS = "Remove filtered objects";

        String[] ALL = new String[]{DO_NOTHING,MOVE_FILTERED_OBJECTS,REMOVE_FILTERED_OBJECTS};

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

        String[] ALL = new String[]{REMOVE_ON_IMAGE_EDGE_2D, MISSING_MEASUREMENTS, NO_PARENT, WITH_PARENT,
                MIN_NUMBER_OF_CHILDREN, MAX_NUMBER_OF_CHILDREN, MEASUREMENTS_SMALLER_THAN, MEASUREMENTS_LARGER_THAN,
                RUNTIME_OBJECT_ID};

    }

    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[]{FIXED_VALUE,IMAGE_MEASUREMENT,PARENT_OBJECT_MEASUREMENT};

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

    public String getFullName(String inputObjectsName, String filterMethod, @Nullable String referenceName, @Nullable String filterValue) {
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
                return "FILTER // NUM_" + inputObjectsName + "_WITH_" + referenceName+ "_SMALLER_THAN_" + filterValue;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                return "FILTER // NUM_" + inputObjectsName + "_WITH_" + referenceName+ "_LARGER_THAN_" + filterValue;

            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                return "FILTER // NUM_" + inputObjectsName + "_ON_" + referenceName + "_EDGE_2D";

            case FilterMethods.RUNTIME_OBJECT_ID:
                return "FILTER // NUM_" + inputObjectsName + "_MATCHING_GIVEN_ID";

        }

        return "";

    }

    public void filterObjectsOnImageEdge(ObjCollection inputObjects, Image inputImage, @Nullable ObjCollection outputObjects, boolean remove, boolean includeZ) {
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = inputImage.getImagePlus().getWidth()-1;
        int maxY = inputImage.getImagePlus().getHeight()-1;
        int maxZ = inputImage.getImagePlus().getNSlices()-1;

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            ArrayList<Integer> x = inputObject.getXCoords();
            ArrayList<Integer> y = inputObject.getYCoords();
            ArrayList<Integer> z = inputObject.getZCoords();

            for (int i=0;i<x.size();i++) {
                if (x.get(i) == minX | x.get(i) == maxX | y.get(i) == minY | y.get(i) == maxY) {
                    if (remove) processRemoval(inputObject,outputObjects,iterator);
                    break;
                }

                // Only consider Z if the user requested this
                if (includeZ && (z.get(i) == minZ | z.get(i) == maxZ)) {
                    if (remove) processRemoval(inputObject,outputObjects,iterator);
                    break;
                }
            }
        }
    }

    public void filterObjectsWithMissingMeasurement(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String measurement) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            if (Double.isNaN(inputObject.getMeasurement(measurement).getValue())) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithoutAParent(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) == null) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithAParent(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) != null) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMinNumOfChildren(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String childObjectsName, double minChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection childObjects = inputObject.getChildren(childObjectsName);

            // Removing the object if it has no children
            if (childObjects == null) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
                continue;
            }

            // Removing the object if it has too few children
            if (childObjects.size() < minChildN) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMaxNumOfChildren(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String childObjectsName, double maxChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection childObjects = inputObject.getChildren(childObjectsName);

            if (childObjects == null) continue;

            // Removing the object if it has too few children
            if (childObjects.size() > maxChildN) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMeasSmallerThan(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String measurementName, MeasRef measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
                continue;
            }

            double value = measurement.getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }

            if (value < referenceValue || Double.isNaN(value)) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMeasLargerThan(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, String measurementName, MeasRef measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
                continue;
            }

            // Removing the object if it has no children
            double value = measurement.getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }

            if (value > referenceValue || Double.isNaN(value)) {
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsByID(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, boolean remove, @Nullable Image image) {
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

        for (int id:ids) {
            Obj obj = inputObjects.get(id);
            if (remove) {
                obj.removeRelationships();
                if (outputObjects != null) {
                    obj.setName(outputObjects.getName());
                    outputObjects.add(obj);
                }
                inputObjects.remove(id);
            }
        }
    }

    void processRemoval(Obj inputObject, ObjCollection outputObjects, Iterator<Obj> iterator) {
        inputObject.removeRelationships();
        if (outputObjects != null) {
            inputObject.setName(outputObjects.getName());
            outputObjects.add(inputObject);
        }
        iterator.remove();
    }


    @Override
    public String getPackageName() {
        return PackageNames.DEPRECATED;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String method = parameters.getValue(FILTER_METHOD);
        String inputImageName = parameters.getValue(REFERENCE_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
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

        ObjCollection outputObjects = filterMode.equals(FilterModes.MOVE_FILTERED_OBJECTS)
                ? new ObjCollection(outputObjectsName) : null;

        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        // Removing objects with a missing measurement (i.e. value set to null)
        switch (method) {
            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                filterObjectsOnImageEdge(inputObjects,inputImage,outputObjects,remove,includeZ);
                break;

            case FilterMethods.MISSING_MEASUREMENTS:
                filterObjectsWithMissingMeasurement(inputObjects,outputObjects,remove,measurement);
                break;

            case FilterMethods.NO_PARENT:
                filterObjectsWithoutAParent(inputObjects,outputObjects,remove,parentObjectName);
                break;

            case FilterMethods.WITH_PARENT:
                filterObjectsWithAParent(inputObjects,outputObjects,remove,parentObjectName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                filterObjectsWithMinNumOfChildren(inputObjects,outputObjects,remove,childObjectsName,referenceValue);
                break;

            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                filterObjectsWithMaxNumOfChildren(inputObjects,outputObjects,remove,childObjectsName,referenceValue);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
                MeasRef measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasRef(ReferenceModes.FIXED_VALUE,referenceValue,1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement).getValue();
                        measurementReference = new MeasRef(ReferenceModes.IMAGE_MEASUREMENT,value,referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasRef(ReferenceModes.PARENT_OBJECT_MEASUREMENT,referenceValueParent,referenceObjectMeasurement,referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasSmallerThan(inputObjects,outputObjects,remove,measurement,measurementReference);
                break;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasRef(ReferenceModes.FIXED_VALUE,referenceValue,1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement).getValue();
                        measurementReference = new MeasRef(ReferenceModes.IMAGE_MEASUREMENT,value,referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasRef(ReferenceModes.PARENT_OBJECT_MEASUREMENT,referenceValueParent,referenceObjectMeasurement,referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasLargerThan(inputObjects,outputObjects,remove,measurement,measurementReference);
                break;

            case FilterMethods.RUNTIME_OBJECT_ID:
                Image displayImage = showImage ? workspace.getImage(displayImageName) : null;
                filterObjectsByID(inputObjects,outputObjects,remove,displayImage);
                break;
        }

        if (filterMode.equals(FilterModes.MOVE_FILTERED_OBJECTS)) workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) inputObjects.convertToImageRandomColours().showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE,this,FilterModes.REMOVE_FILTERED_OBJECTS,FilterModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_METHOD, this,FilterMethods.REMOVE_ON_IMAGE_EDGE_2D,FilterMethods.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new BooleanP(INCLUDE_Z_POSITION,this,false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIXED_VALUE,ReferenceModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this,1d));
        parameters.add(new InputImageP(REFERENCE_VAL_IMAGE, this));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(REFERENCE_VAL_PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_OBJECT_MEASUREMENT, this));
        parameters.add(new DoubleP(REFERENCE_MULTIPLIER, this, 1d));
        parameters.add(new BooleanP(SHOW_IMAGE, this, true));
        parameters.add(new InputImageP(DISPLAY_IMAGE_NAME, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
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
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
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
                        ((ImageMeasurementP) parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT)).setImageName(referenceValueImageName);
                        break;

                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                        String referenceValueParentObjectsName = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
                        ((ParentObjectsP) parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
                        ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT)).setObjectName(referenceValueParentObjectsName);
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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED_OBJECTS)) {
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
        return null;
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

    public class MeasRef {
        final String referenceType;
        final double value;
        final String referenceName;
        final String referenceMeasurementName;
        final double referenceMultiplier;

        /*
         * Constructing a fixed- or image-value type
         * @param value
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
        public MeasRef(String referenceType, String referenceName, String referenceMeasurementName, double referenceMultiplier) {
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
                    return value*referenceMultiplier;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                    if (object.getParent(referenceName) == null) return Double.NaN;
                    return object.getParent(referenceName).getMeasurement(referenceMeasurementName).getValue()*referenceMultiplier;
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

    @Override
    public boolean verify() {
        return true;
    }
}
