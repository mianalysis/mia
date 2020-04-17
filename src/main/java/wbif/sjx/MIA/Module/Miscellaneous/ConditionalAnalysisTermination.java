package wbif.sjx.MIA.Module.Miscellaneous;

import java.io.File;
import java.io.IOException;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects.CoreFilter;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.MetadataItemP;
import wbif.sjx.MIA.Object.Parameters.ModuleP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Object.Metadata;

/**
 * Created by Stephen Cross on 23/11/2018.
 */
public class ConditionalAnalysisTermination extends Module {
    public static final String CONDITION_SEPARATOR = "Condition";
    public static final String TEST_MODE = "Test mode";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Reference image measurement mode";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_OBJECT_COUNT_MODE = "Reference object count mode";
    public static final String REFERENCE_METADATA_VALUE = "Reference metadata value";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String FIXED_VALUE = "Fixed value";
    public static final String GENERIC_FORMAT = "Generic format";
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    public static final String RESULT_SEPARATOR = "Result";
    public static final String CONTINUATION_MODE = "Continuation mode";
    public static final String REDIRECT_MODULE = "Redirect module";
    public static final String SHOW_REDIRECT_MESSAGE = "Show redirect message";
    public static final String REDIRECT_MESSAGE = "Redirect message";
    public static final String EXPORT_WORKSPACE = "Export terminated workspaces";
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";
    public static final String REMOVE_IMAGES = "Remove images from workspace";

    public ConditionalAnalysisTermination(ModuleCollection modules) {
        super("Conditional analysis handling", modules);
    }

    public interface TestModes {
        String IMAGE_MEASUREMENT = "Image measurement";
        String METADATA_VALUE = "Metadata value";
        String FILE_EXISTS = "File exists";
        String FILE_DOES_NOT_EXIST = "File doesn't exist";
        String FIXED_VALUE = "Fixed value";
        String OBJECT_COUNT = "Object count";

        String[] ALL = new String[] { FILE_DOES_NOT_EXIST, FILE_EXISTS, FIXED_VALUE, IMAGE_MEASUREMENT, METADATA_VALUE,
                OBJECT_COUNT };

    }

    public interface FilterModes extends CoreFilter.FilterMethods {
    }

    public interface ContinuationModes {
        String REDIRECT_TO_MODULE = "Redirect to module";
        String TERMINATE = "Terminate";

        String[] ALL = new String[] { REDIRECT_TO_MODULE, TERMINATE };

    }

    public static boolean testFileExists(Metadata metadata, String genericFormat) {
        String name = "";
        try {
            name = ImageLoader.getGenericName(metadata, genericFormat);
        } catch (ServiceException | DependencyException | FormatException | IOException e) {
            return false;
        }

        return (new File(name)).exists();

    }

    public static boolean testImageMeasurement(Image inputImage, String measurementName, String referenceMode,
            double referenceValue) {
        // Getting the measurement
        Measurement measurement = inputImage.getMeasurement(measurementName);

        // If this measurement doesn't exist, fail the test
        if (measurement == null)
            return false;

        // Testing the value
        double measurementValue = measurement.getValue();
        return CoreFilter.testFilter(measurementValue, referenceValue, referenceMode);

    }

    public static boolean testMetadata(String metadataValue, String referenceMode, double referenceValue) {
        try {
            double testValue = Double.parseDouble(metadataValue);
            return CoreFilter.testFilter(testValue, referenceValue, referenceMode);

        } catch (NumberFormatException e) {
            // This will be thrown if the value specified wasn't a number
            MIA.log.writeWarning("Specified metadata value for filtering (" + metadataValue
                    + ") wasn't a number.  Terminating this run.");
            return false;

        }
    }

    public static boolean testObjectCount(ObjCollection inputObjects, String referenceMode, double referenceValue) {
        int testValue = 0;
        if (inputObjects != null)
            testValue = inputObjects.size();

        return CoreFilter.testFilter(testValue, referenceValue, referenceMode);

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
    protected Status process(Workspace workspace) {
        // Getting parameters
        String testMode = parameters.getValue(TEST_MODE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String filterMode = parameters.getValue(FILTER_MODE);
        String referenceImageMeasurement = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String referenceMetadataValue = parameters.getValue(REFERENCE_METADATA_VALUE);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        double fixedValue = parameters.getValue(FIXED_VALUE);
        String genericFormat = parameters.getValue(GENERIC_FORMAT);
        String continuationMode = parameters.getValue(CONTINUATION_MODE);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE);
        String redirectMessage = parameters.getValue(REDIRECT_MESSAGE);
        boolean exportWorkspace = parameters.getValue(EXPORT_WORKSPACE);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS);

        // Running relevant tests
        boolean terminate = false;
        switch (testMode) {
            case TestModes.FILE_DOES_NOT_EXIST:
                terminate = !testFileExists(workspace.getMetadata(), genericFormat);
                break;
            case TestModes.FILE_EXISTS:
                terminate = testFileExists(workspace.getMetadata(), genericFormat);
                break;
            case TestModes.FIXED_VALUE:
                terminate = fixedValue == referenceValue;
                break;
            case TestModes.IMAGE_MEASUREMENT:
                Image inputImage = workspace.getImage(inputImageName);
                terminate = testImageMeasurement(inputImage, referenceImageMeasurement, filterMode, referenceValue);
                break;
            case TestModes.METADATA_VALUE:
                String metadataValue = workspace.getMetadata().get(referenceMetadataValue).toString();
                terminate = testMetadata(metadataValue, filterMode, referenceValue);
                break;
            case TestModes.OBJECT_COUNT:
                ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
                terminate = testObjectCount(inputObjects, filterMode, referenceValue);
                break;
        }

        // If terminate, remove necessary images and objects
        if (terminate) {
            switch (continuationMode) {
                case ContinuationModes.REDIRECT_TO_MODULE:
                    if (showRedirectMessage)
                        MIA.log.writeMessage(workspace.getMetadata().insertMetadataValues(redirectMessage));
                    return Status.REDIRECT;
                case ContinuationModes.TERMINATE:
                    workspace.setExportWorkspace(exportWorkspace);
                    if (removeImages)
                        workspace.clearAllImages(false);
                    if (removeObjects)
                        workspace.clearAllObjects(false);
                    return Status.TERMINATE;
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ChoiceP(TEST_MODE, this, TestModes.IMAGE_MEASUREMENT, TestModes.ALL));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.LESS_THAN, FilterModes.ALL));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new MetadataItemP(REFERENCE_METADATA_VALUE, this));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 0d));
        parameters.add(new DoubleP(FIXED_VALUE, this, 0d));
        parameters.add(new StringP(GENERIC_FORMAT, this, "",
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation."));
        parameters.add(new TextAreaP(AVAILABLE_METADATA_FIELDS, this, false,
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename."));

        parameters.add(new ParamSeparatorP(RESULT_SEPARATOR, this));
        parameters.add(new ChoiceP(CONTINUATION_MODE, this, ContinuationModes.TERMINATE, ContinuationModes.ALL));
        parameters.add(new ModuleP(REDIRECT_MODULE, this));
        parameters.add(new BooleanP(SHOW_REDIRECT_MESSAGE, this, false));
        parameters.add(new StringP(REDIRECT_MESSAGE, this, ""));
        parameters.add(new BooleanP(EXPORT_WORKSPACE, this, true));
        parameters.add(new BooleanP(REMOVE_IMAGES, this, false));
        parameters.add(new BooleanP(REMOVE_OBJECTS, this, false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));

        returnedParameters.add(parameters.getParameter(TEST_MODE));
        switch ((String) parameters.getValue(TEST_MODE)) {
            case TestModes.FILE_EXISTS:
            case TestModes.FILE_DOES_NOT_EXIST:
                returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                MetadataRefCollection metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS)
                        .setValue(ImageLoader.getMetadataValues(metadataRefs));
                break;

            case TestModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                break;

            case TestModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));

                String inputImageName = parameters.getValue(INPUT_IMAGE);
                ImageMeasurementP parameter = parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT);
                parameter.setImageName(inputImageName);
                break;

            case TestModes.METADATA_VALUE:
                returnedParameters.add(parameters.getParameter(FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_METADATA_VALUE));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                break;

            case TestModes.OBJECT_COUNT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                break;
        }

        returnedParameters.add(parameters.getParameter(RESULT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONTINUATION_MODE));
        switch ((String) parameters.getValue(CONTINUATION_MODE)) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                returnedParameters.add(parameters.getParameter(REDIRECT_MODULE));
                redirectModule = parameters.getValue(REDIRECT_MODULE);
                returnedParameters.add(parameters.getParameter(SHOW_REDIRECT_MESSAGE));
                if ((boolean) parameters.getValue(SHOW_REDIRECT_MESSAGE)) {
                    returnedParameters.add(parameters.getParameter(REDIRECT_MESSAGE));
                }
                break;
            case ContinuationModes.TERMINATE:
                returnedParameters.add(parameters.getParameter(EXPORT_WORKSPACE));
                returnedParameters.add(parameters.getParameter(REMOVE_IMAGES));
                returnedParameters.add(parameters.getParameter(REMOVE_OBJECTS));
                redirectModule = null;
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
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
