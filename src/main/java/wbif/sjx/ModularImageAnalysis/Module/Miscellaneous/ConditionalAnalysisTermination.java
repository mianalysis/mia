package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class ConditionalAnalysisTermination extends Module {
    public static final String TEST_MODE = "Test mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_VALUE = "Reference value";


    public interface TestModes {
        String IMAGE_MEASUREMENT = "Image measurement";

        String[] ALL = new String[]{IMAGE_MEASUREMENT};

    }

    public interface ReferenceModes {
        String MEASUREMENT_LESS_THAN = "Terminate if measurement less than";
        String MEASUREMENT_GREATER_THAN = "Terminate if measurement greater than";

        String[] ALL = new String[]{MEASUREMENT_LESS_THAN,MEASUREMENT_GREATER_THAN};

    }


    public static boolean testImageMeasurement(Image inputImage, String measurementName, String referenceMode, double referenceValue) {
        // Getting the measurement
        Measurement measurement = inputImage.getMeasurement(measurementName);

        // If this measurement doesn't exist, fail the test
        if (measurement == null) return false;

        // Testing the value
        double measurementValue = measurement.getValue();
        switch (referenceMode) {
            case ReferenceModes.MEASUREMENT_LESS_THAN:
                return !(measurementValue < referenceValue);

            case ReferenceModes.MEASUREMENT_GREATER_THAN:
                return !(measurementValue > referenceValue);

        }

        return false;

    }

    @Override
    public String getTitle() {
        return "Conditional analysis termination";
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting parameters
        String testMode = parameters.getValue(TEST_MODE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String referenceImageMeasurement = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);

        // Running relevant tests
        switch (testMode) {
            case TestModes.IMAGE_MEASUREMENT:
                Image inputImage = workspace.getImage(inputImageName);
                return testImageMeasurement(inputImage,referenceImageMeasurement,referenceMode,referenceValue);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(TEST_MODE,Parameter.CHOICE_ARRAY,TestModes.IMAGE_MEASUREMENT,TestModes.ALL));
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.MEASUREMENT_LESS_THAN,ReferenceModes.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE_MEASUREMENT,Parameter.IMAGE_MEASUREMENT,null));
        parameters.add(new Parameter(REFERENCE_VALUE,Parameter.DOUBLE,0d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();
        returnedParamters.add(parameters.getParameter(TEST_MODE));

        switch ((String) parameters.getValue(TEST_MODE)) {
            case TestModes.IMAGE_MEASUREMENT:
                returnedParamters.add(parameters.getParameter(INPUT_IMAGE));
                returnedParamters.add(parameters.getParameter(REFERENCE_MODE));
                returnedParamters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                returnedParamters.add(parameters.getParameter(REFERENCE_VALUE));

                String inputImageName = parameters.getValue(INPUT_IMAGE);
                parameters.updateValueSource(REFERENCE_IMAGE_MEASUREMENT,inputImageName);

                break;
        }

        return returnedParamters;

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
}
