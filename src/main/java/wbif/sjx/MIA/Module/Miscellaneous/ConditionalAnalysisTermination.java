package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects.CoreFilter;
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
    public static final String FILTER_MODE = "Reference image measurement mode";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_OBJECT_COUNT_MODE = "Reference object count mode";
    public static final String REFERENCE_METADATA_VALUE = "Reference metadata value";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";
    public static final String REMOVE_IMAGES = "Remove images from workspace";

    public ConditionalAnalysisTermination(ModuleCollection modules) {
        super("Conditional analysis termination",modules);
    }


    public interface TestModes {
        String IMAGE_MEASUREMENT = "Image measurement";
        String METADATA_VALUE = "Metadata value";
        String OBJECT_COUNT = "Object count";

        String[] ALL = new String[]{IMAGE_MEASUREMENT,METADATA_VALUE,OBJECT_COUNT};

    }

    public interface FilterModes extends CoreFilter.FilterMethods {}


    public static boolean testImageMeasurement(Image inputImage, String measurementName, String referenceMode, double referenceValue) {
        // Getting the measurement
        Measurement measurement = inputImage.getMeasurement(measurementName);

        // If this measurement doesn't exist, fail the test
        if (measurement == null) return false;

        // Testing the value
        double measurementValue = measurement.getValue();
        return CoreFilter.testFilter(measurementValue,referenceValue,referenceMode);

    }

    public static boolean testMetadata(String metadataValue, String referenceMode, double referenceValue) {
        try {
            double testValue = Double.parseDouble(metadataValue);
            return CoreFilter.testFilter(testValue,referenceValue,referenceMode);

        } catch (NumberFormatException e) {
            // This will be thrown if the value specified wasn't a number
            MIA.log.writeWarning("Specified metadata value for filtering ("+metadataValue+") wasn't a number.  Terminating this run.");
            return false;

        }
    }

    public static boolean testObjectCount(ObjCollection inputObjects, String referenceMode, double referenceValue) {
        int testValue = 0;
        if (inputObjects != null) testValue = inputObjects.size();

        return CoreFilter.testFilter(testValue,referenceValue,referenceMode);

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
        String filterMode = parameters.getValue(FILTER_MODE);
        String referenceImageMeasurement = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String referenceMetadataValue = parameters.getValue(REFERENCE_METADATA_VALUE);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS);

        // Running relevant tests
        boolean terminate = false;
        switch (testMode) {
            case TestModes.IMAGE_MEASUREMENT:
                Image inputImage = workspace.getImage(inputImageName);
                terminate = testImageMeasurement(inputImage,referenceImageMeasurement,filterMode,referenceValue);
                break;
            case TestModes.METADATA_VALUE:
                String metadataValue = workspace.getMetadata().get(referenceMetadataValue).toString();
                terminate = testMetadata(metadataValue,filterMode,referenceValue);
                break;
            case TestModes.OBJECT_COUNT:
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                terminate = testObjectCount(inputObjects,filterMode,referenceValue);
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
        parameters.add(new ChoiceP(FILTER_MODE,this,FilterModes.LESS_THAN,FilterModes.ALL));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT,this));
        parameters.add(new MetadataItemP(REFERENCE_METADATA_VALUE,this));
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
                returnedParameters.add(parameters.getParameter(FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));

                String inputImageName = parameters.getValue(INPUT_IMAGE);
                ImageMeasurementP parameter = parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT);
                parameter.setImageName(inputImageName);
                break;

            case TestModes.METADATA_VALUE:
                returnedParameters.add(parameters.getParameter(FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_METADATA_VALUE));
                break;

            case TestModes.OBJECT_COUNT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(FILTER_MODE));

                break;
        }

        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

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

    @Override
    public boolean verify() {
        return true;
    }
}
