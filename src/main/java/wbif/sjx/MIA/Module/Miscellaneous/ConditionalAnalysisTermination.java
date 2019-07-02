package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class ConditionalAnalysisTermination extends Module {
    public static final String TEST_MODE = "Test mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_IMAGE_MEASUREMENT_MODE = "Reference image measurement mode";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_OBJECT_COUNT_MODE = "Reference object count mode";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";
    public static final String REMOVE_IMAGES = "Remove images from workspace";

    public ConditionalAnalysisTermination(ModuleCollection modules) {
        super("Conditional analysis termination",modules);
    }


    public interface TestModes {
        String IMAGE_MEASUREMENT = "Image measurement";
        String OBJECT_COUNT = "Object count";

        String[] ALL = new String[]{IMAGE_MEASUREMENT,OBJECT_COUNT};

    }

    public interface ReferenceImageMeasurementModes {
        String MEASUREMENT_LESS_THAN = "Terminate if measurement less than";
        String MEASUREMENT_GREATER_THAN = "Terminate if measurement greater than";

        String[] ALL = new String[]{MEASUREMENT_LESS_THAN,MEASUREMENT_GREATER_THAN};

    }

    public interface ReferenceObjectCountModes {
        String FEWER_OBJECTS_THAN = "Fewer objects than";
        String MORE_OBJECTS_THAN = "More objects than";
        String NO_OBJECTS = "No objects";

        String[] ALL = new String[]{FEWER_OBJECTS_THAN,MORE_OBJECTS_THAN,NO_OBJECTS};

    }


    public static boolean testImageMeasurement(Image inputImage, String measurementName, String referenceMode, double referenceValue) {
        // Getting the measurement
        Measurement measurement = inputImage.getMeasurement(measurementName);

        // If this measurement doesn't exist, fail the test
        if (measurement == null) return false;

        // Testing the value
        double measurementValue = measurement.getValue();
        switch (referenceMode) {
            case ReferenceImageMeasurementModes.MEASUREMENT_LESS_THAN:
                return measurementValue < referenceValue;

            case ReferenceImageMeasurementModes.MEASUREMENT_GREATER_THAN:
                return measurementValue > referenceValue;

        }

        return true;

    }

    public static boolean testObjectCount(ObjCollection inputObjects, String referenceMode, double referenceValue) {
        switch (referenceMode) {
            case ReferenceObjectCountModes.FEWER_OBJECTS_THAN:
                return inputObjects == null || inputObjects.size() < (int) Math.round(referenceValue);
            case ReferenceObjectCountModes.MORE_OBJECTS_THAN:
                return inputObjects != null && inputObjects.size() > (int) Math.round(referenceValue);
            case ReferenceObjectCountModes.NO_OBJECTS:
                return inputObjects.size() == 0;
        }

        return false;

    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        String testMode = parameters.getValue(TEST_MODE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String imageMeasurementReferenceMode = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT_MODE);
        String referenceImageMeasurement = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String objectCountReferenceMode = parameters.getValue(REFERENCE_OBJECT_COUNT_MODE);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS);

        // Running relevant tests
        boolean terminate = false;
        switch (testMode) {
            case TestModes.IMAGE_MEASUREMENT:
                Image inputImage = workspace.getImage(inputImageName);
                terminate = testImageMeasurement(inputImage,referenceImageMeasurement,imageMeasurementReferenceMode,referenceValue);
                break;
            case TestModes.OBJECT_COUNT:
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                terminate = testObjectCount(inputObjects,objectCountReferenceMode,referenceValue);
                break;
        }

        // If terminate, remove necessary images and objects
        if (terminate) {
            if (removeImages) workspace.clearAllImages(false);
            if (removeObjects) workspace.clearAllObjects(false);
        }

        // Modules return true if they are to continue
        return !terminate;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(TEST_MODE,this,TestModes.IMAGE_MEASUREMENT,TestModes.ALL));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ChoiceP(REFERENCE_IMAGE_MEASUREMENT_MODE,this, ReferenceImageMeasurementModes.MEASUREMENT_LESS_THAN, ReferenceImageMeasurementModes.ALL));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT,this));
        parameters.add(new ChoiceP(REFERENCE_OBJECT_COUNT_MODE,this,ReferenceObjectCountModes.FEWER_OBJECTS_THAN,ReferenceObjectCountModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE,this,0d));
        parameters.add(new BooleanP(REMOVE_IMAGES,this,false));
        parameters.add(new BooleanP(REMOVE_OBJECTS,this,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(TEST_MODE));

        switch ((String) parameters.getValue(TEST_MODE)) {
            case TestModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

                String inputImageName = parameters.getValue(INPUT_IMAGE);
                ImageMeasurementP parameter = parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT);
                parameter.setImageName(inputImageName);

                break;

            case TestModes.OBJECT_COUNT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_COUNT_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

                break;
        }

        returnedParameters.add(parameters.getParameter(REMOVE_IMAGES));
        returnedParameters.add(parameters.getParameter(REMOVE_OBJECTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
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
}
