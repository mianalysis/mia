package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.LUTs;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 23/05/2017.
 */
public class FilterObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MOVE_FILTERED = "Move filtered objects to new class";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";
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

    public interface FilterMethods {
        String REMOVE_ON_IMAGE_EDGE_2D = "Exclude objects on image edge (2D)";
        String MISSING_MEASUREMENTS = "Remove objects with missing measurements";
        String NO_PARENT = "Remove objects without parent";
        String WITH_PARENT = "Remove objects with a parent";
        String MIN_NUMBER_OF_CHILDREN = "Remove objects with fewer children than:";
        String MAX_NUMBER_OF_CHILDREN = "Remove objects with more children than:";
        String MEASUREMENTS_SMALLER_THAN = "Remove objects with measurements < than:";
        String MEASUREMENTS_LARGER_THAN = "Remove objects with measurements > than:";

        String[] ALL = new String[]{REMOVE_ON_IMAGE_EDGE_2D, MISSING_MEASUREMENTS, NO_PARENT, WITH_PARENT,
                MIN_NUMBER_OF_CHILDREN, MAX_NUMBER_OF_CHILDREN, MEASUREMENTS_SMALLER_THAN, MEASUREMENTS_LARGER_THAN};

    }

    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[]{FIXED_VALUE,IMAGE_MEASUREMENT,PARENT_OBJECT_MEASUREMENT};

    }


    public void filterObjectsOnImageEdge(ObjCollection inputObjects, Image inputImage, @Nullable ObjCollection outputObjects, boolean includeZ) {
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
                    processRemoval(inputObject,outputObjects,iterator);
                    break;
                }

                // Only consider Z if the user requested this
                if (includeZ && (z.get(i) == minZ | z.get(i) == maxZ)) {
                    processRemoval(inputObject,outputObjects,iterator);
                    break;
                }
            }
        }
    }

    public void filterObjectsWithMissingMeasurement(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String measurement) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            if (Double.isNaN(inputObject.getMeasurement(measurement).getValue())) {
                processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithoutAParent(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) == null) {
                processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithAParent(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String parentObjectName) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
            if (parents.get(parentObjectName) != null) {
                processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMinNumOfChildren(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String childObjectsName, double minChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection childObjects = inputObject.getChildren(childObjectsName);

            // Removing the object if it has no children
            if (childObjects == null) {
                processRemoval(inputObject,outputObjects,iterator);
                continue;
            }

            // Removing the object if it has too few children
            if (childObjects.size() < minChildN) {
                processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMaxNumOfChildren(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String childObjectsName, double maxChildN) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection childObjects = inputObject.getChildren(childObjectsName);

            if (childObjects == null) continue;

            // Removing the object if it has too few children
            if (childObjects.size() > maxChildN) {
                processRemoval(inputObject,outputObjects,iterator);
            }
        }
    }

    public void filterObjectsWithMeasSmallerThan(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String measurement, MeasurementReference measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            double value = inputObject.getMeasurement(measurement).getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) processRemoval(inputObject,outputObjects,iterator);
            if (value < referenceValue || Double.isNaN(value)) processRemoval(inputObject,outputObjects,iterator);

        }
    }

    public void filterObjectsWithMeasLargerThan(ObjCollection inputObjects, @Nullable ObjCollection outputObjects, String measurement, MeasurementReference measurementReference) {
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            double value = inputObject.getMeasurement(measurement).getValue();
            double referenceValue = measurementReference.getValue(inputObject);

            if (Double.isNaN(referenceValue)) processRemoval(inputObject,outputObjects,iterator);
            if (value > referenceValue || Double.isNaN(value)) processRemoval(inputObject,outputObjects,iterator);

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
    public String getTitle() {
        return "Filter objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean moveFiltered = parameters.getValue(MOVE_FILTERED);
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

        ObjCollection outputObjects = moveFiltered ? new ObjCollection(outputObjectsName) : null;

        // Removing objects with a missing measurement (i.e. value set to null)
        switch (method) {
            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                filterObjectsOnImageEdge(inputObjects,inputImage,outputObjects,includeZ);
                break;

            case FilterMethods.MISSING_MEASUREMENTS:
                filterObjectsWithMissingMeasurement(inputObjects,outputObjects,measurement);
                break;

            case FilterMethods.NO_PARENT:
                filterObjectsWithoutAParent(inputObjects,outputObjects,parentObjectName);
                break;

            case FilterMethods.WITH_PARENT:
                filterObjectsWithAParent(inputObjects,outputObjects,parentObjectName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
                filterObjectsWithMinNumOfChildren(inputObjects,outputObjects,childObjectsName,referenceValue);
                break;

            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                filterObjectsWithMaxNumOfChildren(inputObjects,outputObjects,childObjectsName,referenceValue);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
                MeasurementReference measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasurementReference(ReferenceModes.FIXED_VALUE,referenceValue,1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement).getValue();
                        measurementReference = new MeasurementReference(ReferenceModes.IMAGE_MEASUREMENT,value,referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasurementReference(ReferenceModes.PARENT_OBJECT_MEASUREMENT,referenceValueParent,referenceObjectMeasurement,referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasSmallerThan(inputObjects,outputObjects,measurement,measurementReference);
                break;

            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                measurementReference = null;
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                        measurementReference = new MeasurementReference(ReferenceModes.FIXED_VALUE,referenceValue,1);
                        break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                        double value = workspace.getImage(referenceValueImage).getMeasurement(referenceImageMeasurement).getValue();
                        measurementReference = new MeasurementReference(ReferenceModes.IMAGE_MEASUREMENT,value,referenceMultiplier);
                        break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        measurementReference = new MeasurementReference(ReferenceModes.PARENT_OBJECT_MEASUREMENT,referenceValueParent,referenceObjectMeasurement,referenceMultiplier);
                        break;
                }

                filterObjectsWithMeasLargerThan(inputObjects,outputObjects,measurement,measurementReference);
                break;
        }

        if (moveFiltered) workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) {
            HashMap<Integer,Float> hues = inputObjects.getHues(ObjCollection.ColourModes.RANDOM_COLOUR,"",false);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            ImagePlus dispIpl = inputObjects.convertObjectsToImage("Objects", inputImage, mode, hues).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(MOVE_FILTERED,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(OUTPUT_FILTERED_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(FILTER_METHOD, Parameter.CHOICE_ARRAY,FilterMethods.REMOVE_ON_IMAGE_EDGE_2D,FilterMethods.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INCLUDE_Z_POSITION,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(CHILD_OBJECTS, Parameter.CHILD_OBJECTS,null,null));
        parameters.add(new Parameter(REFERENCE_MODE, Parameter.CHOICE_ARRAY, ReferenceModes.FIXED_VALUE,ReferenceModes.ALL));
        parameters.add(new Parameter(REFERENCE_VALUE, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(REFERENCE_VAL_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(REFERENCE_IMAGE_MEASUREMENT, Parameter.IMAGE_MEASUREMENT,"",""));
        parameters.add(new Parameter(REFERENCE_VAL_PARENT_OBJECT, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(REFERENCE_OBJECT_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,"",""));
        parameters.add(new Parameter(REFERENCE_MULTIPLIER, Parameter.DOUBLE, 1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(MOVE_FILTERED));
        if (parameters.getValue(MOVE_FILTERED)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));
        }

        returnedParameters.add(parameters.getParameter(FILTER_METHOD));

        switch ((String) parameters.getValue(FILTER_METHOD)) {
            case FilterMethods.MISSING_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

                }
                break;

            case FilterMethods.REMOVE_ON_IMAGE_EDGE_2D:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                returnedParameters.add(parameters.getParameter(INCLUDE_Z_POSITION));
                break;

            case FilterMethods.NO_PARENT:
            case FilterMethods.WITH_PARENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT,inputObjectsName);
                break;

            case FilterMethods.MIN_NUMBER_OF_CHILDREN:
            case FilterMethods.MAX_NUMBER_OF_CHILDREN:
                returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

                inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(CHILD_OBJECTS,inputObjectsName);
                break;

            case FilterMethods.MEASUREMENTS_SMALLER_THAN:
            case FilterMethods.MEASUREMENTS_LARGER_THAN:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                parameters.updateValueSource(MEASUREMENT, parameters.getValue(INPUT_OBJECTS));

                returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
                switch ((String) parameters.getValue(REFERENCE_MODE)) {
                    case ReferenceModes.FIXED_VALUE:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                        break;

                    case ReferenceModes.IMAGE_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VAL_IMAGE));
                        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                        parameters.updateValueSource(REFERENCE_IMAGE_MEASUREMENT,parameters.getValue(REFERENCE_VAL_IMAGE));
                        break;

                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                        parameters.updateValueSource(REFERENCE_VAL_PARENT_OBJECT,parameters.getValue(INPUT_OBJECTS));
                        parameters.updateValueSource(REFERENCE_OBJECT_MEASUREMENT,parameters.getValue(REFERENCE_VAL_PARENT_OBJECT));
                        break;
                }

                break;

        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }


    public class MeasurementReference {
        final String referenceType;
        final double value;
        final String referenceName;
        final String referenceMeasurementName;
        final double referenceMultiplier;

        /**
         * Constructing a fixed- or image-value type
         * @param value
         */
        public MeasurementReference(String referenceType, double value, double referenceMultiplier) {
            this.referenceType = referenceType;
            this.value = value;
            this.referenceName = null;
            this.referenceMeasurementName = null;
            this.referenceMultiplier = referenceMultiplier;

        }

        /**
         * Constructing a parent-reference type
         * @param referenceType
         * @param referenceName
         * @param referenceMeasurementName
         * @param referenceMultiplier
         */
        public MeasurementReference(String referenceType, String referenceName, String referenceMeasurementName, double referenceMultiplier) {
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
}
