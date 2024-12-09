package io.github.mianalysis.mia.module.workflow;

import java.io.File;
import java.io.IOException;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.filter.AbstractNumericObjectFilter;
import io.github.mianalysis.mia.module.objects.filter.AbstractTextObjectFilter;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.MetadataItemP;
import io.github.mianalysis.mia.object.parameters.ModuleP;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.logging.LogRenderer.Level;
import io.github.mianalysis.mia.process.system.FileTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;

/**
 * Created by Stephen Cross on 23/11/2018.
 */

/**
 * Implement workflow handling outcome based on a variety of metrics (e.g.
 * object counts, image measurements, metadata values). Outcomes can include
 * termination of the analysis and redirection of the active module to another
 * part of the workflow. Redirection allows parts of the analysis to skipped.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class WorkflowHandling extends Module {

    /**
    * 
    */
    public static final String CONDITION_SEPARATOR = "Condition";

    /**
     * Controls what condition is being tested:<br>
     * <ul>
     * <li>"Image measurement" Numeric filter against a measurement (specified by
     * "Image measurement") associated with an image from the workspace (specified
     * by "Input image").</li>
     * <li>"Metadata numeric value" Numeric filter against a metadata value
     * (specified by "Metadata value") associated with the workspace. Metadata
     * values are stored as text, but this filter will attempt to parse any numeric
     * values as numbers. Text comparison can be done using "Metadata text value"
     * mode.</li>
     * <li>"Metadata text value" Text filter against a metadata value (specified by
     * "Metadata value") associated with the workspace. This filter compares for
     * exact text matches to a reference, specified by "Reference text value"</li>
     * <li>"File exists" Checks if a specified file exists on the accessible
     * computer filesystem.</li>
     * <li>"File doesn't exist" Checks if a specified file doesn't exist on the
     * accessible computer filesystem.</li>
     * <li>"Fixed value" Numeric filter against a fixed value.</li>
     * <li>"Object count" Numeric filter against the number of objects contained in
     * an object collection stored in the workspace (specified by "Input
     * objects").</li>
     * </ul>
     */
    public static final String TEST_MODE = "Test mode";

    /**
     * If testing against an image measurement ("Test mode" set to "Image
     * measurement"), this is the image from which that measurement will be taken.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * If testing against an object count ("Test mode" set to "Object count"), this
     * is the object collection which will be counted.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * If testing against an image measurement ("Test mode" set to "Image
     * measurement"), this is the measurement from the image (specified by "Input
     * image") that will be tested.
     */
    public static final String IMAGE_MEASUREMENT = "Image measurement";

    /**
     * If testing against a metadata value (either text or numeric) associated with
     * the active workspace ("Test mode" set to "Metadata text value"), this is the
     * value that will be tested.
     */
    public static final String METADATA_VALUE = "Metadata value";

    /**
     * If testing against a fixed numeric value ("Test mode" set to "Fixed value"),
     * this is the value that will be tested.
     */
    public static final String FIXED_VALUE = "Fixed value";

    /**
     * Numeric comparison used to determine whether the test passes or fails.
     * Choices are: Less than, Less than or equal to, Equal to, Greater than or
     * equal to, Greater than, Not equal to.
     */
    public static final String NUMERIC_FILTER_MODE = "Numeric filter mode";

    /**
     * Text comparison used to determine whether the test passes or fails. Choices
     * are: Contains, Does not contain, Equal to, Not equal to.
     */
    public static final String TEXT_FILTER_MODE = "Text filter mode";

    /**
     * If testing against a numeric value, this is the reference value against which
     * it will be tested. What classes as a pass or fail is determined by the
     * parameter "Numeric filter mode".
     */
    public static final String REFERENCE_NUMERIC_VALUE = "Reference numeric value";

    /**
     * If testing against a text value, this is the reference value against which it
     * will be tested. What classes as a pass or fail is determined by the parameter
     * "Text filter mode".
     */
    public static final String REFERENCE_TEXT_VALUE = "Reference text value";

    /**
     * Format for a generic filename. Plain text can be mixed with global variables
     * or metadata values currently stored in the workspace. Global variables are
     * specified using the "V{name}" notation, where "name" is the name of the
     * variable to insert. Similarly, metadata values are specified with the
     * "M{name}" notation.
     */
    public static final String GENERIC_FORMAT = "Generic format";

    /**
     * List of the currently-available metadata values for this workspace. These can
     * be used when compiling a generic filename.
     */
    public static final String AVAILABLE_METADATA_FIELDS = "Available metadata fields";

    /**
    * 
    */
    public static final String RESULT_SEPARATOR = "Result";

    /**
     * Controls what happens if the termination/redirection condition is met:<br>
     * <ul>
     * <li>"Redirect to module" The analysis workflow will skip to the module
     * specified by the "Redirect module" parameter. Any modules between the present
     * module and the target module will not be evaluated.</li>
     * <li>"Terminate" The analysis will stop evaluating any further modules.</li>
     * </ul>
     */
    public static final String CONTINUATION_MODE = "Continuation mode";

    /**
     * If the condition is met, the workflow will redirect to this module. In doing
     * so, it will skip evaluation of any modules between the present module and
     * this module.
     */
    public static final String REDIRECT_MODULE = "Redirect module";

    /**
     * Controls if a message should be displayed in the log if redirection occurs.
     */
    public static final String SHOW_REDIRECT_MESSAGE = "Show redirect message";

    /**
     * Message to display if redirection occurs.
     */
    public static final String REDIRECT_MESSAGE = "Redirect message";

    /**
     * Controls the logging level in which the message will be displayed. Warnings
     * are enabled for users by default, but debug and message aren't.
     */
    public static final String MESSAGE_LEVEL = "Message level";

    /**
    * 
    */
    public static final String SHOW_TERMINATION_WARNING = "Show termination warning";

    /**
     * Controls if the workspace should still be exported to the output Excel
     * spreadsheet if termination occurs.
     */
    public static final String EXPORT_WORKSPACE = "Export terminated workspaces";

    /**
     * Controls if objects should be completely removed from the workspace along
     * with any associated measurements if termination occurs.
     */
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";

    /**
     * Controls if images should be completely removed from the workspace along with
     * any associated measurements if termination occurs.
     */
    public static final String REMOVE_IMAGES = "Remove images from workspace";

    public WorkflowHandling(Modules modules) {
        super("Workflow handling", modules);
    }

    public interface TestModes {
        String IMAGE_MEASUREMENT = "Image measurement";
        String METADATA_NUMERIC_VALUE = "Metadata numeric value";
        String METADATA_TEXT_VALUE = "Metadata text value";
        String FILE_EXISTS = "File exists";
        String FILE_DOES_NOT_EXIST = "File doesn't exist";
        String FIXED_VALUE = "Fixed value";
        String OBJECT_COUNT = "Object count";

        String[] ALL = new String[] { FILE_DOES_NOT_EXIST, FILE_EXISTS, FIXED_VALUE, IMAGE_MEASUREMENT,
                METADATA_NUMERIC_VALUE, METADATA_TEXT_VALUE, OBJECT_COUNT };

    }

    public interface MessageLevels {
        String DEBUG = "Debug";
        String MESSAGE = "Message";
        String WARNING = "Warning";

        String[] ALL = new String[] { DEBUG, MESSAGE, WARNING };

    }

    public interface NumericFilterModes extends AbstractNumericObjectFilter.FilterMethods {
    }

    public interface TextFilterModes extends AbstractTextObjectFilter.FilterMethods {
        
    }

    public interface ContinuationModes {
        String REDIRECT_TO_MODULE = "Redirect to module";
        String TERMINATE = "Terminate";

        String[] ALL = new String[] { REDIRECT_TO_MODULE, TERMINATE };

    }

    Status processTermination(Parameters params) {
        return processTermination(params, null, false);

    }

    Status processTermination(Parameters parameters, Workspace workspace, boolean showRedirectMessage) {
        String continuationMode = parameters.getValue(CONTINUATION_MODE, workspace);
        String redirectMessage = parameters.getValue(REDIRECT_MESSAGE, workspace);
        String messageLevel = parameters.getValue(MESSAGE_LEVEL, workspace);
        boolean showTerminationWarning = parameters.getValue(SHOW_TERMINATION_WARNING, workspace);
        boolean exportWorkspace = parameters.getValue(EXPORT_WORKSPACE, workspace);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES, workspace);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS, workspace);

        // If terminate, remove necessary images and objects
        switch (continuationMode) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                redirectModuleID = parameters.getValue(REDIRECT_MODULE, workspace);
                if (showRedirectMessage) {
                    Level level = getLevel(messageLevel);
                    MIA.log.write(workspace.getMetadata().insertMetadataValues(redirectMessage), level);
                }

                return Status.REDIRECT;

            case ContinuationModes.TERMINATE:
                if (showTerminationWarning)
                    MIA.log.writeWarning("Analysis terminated early");

                workspace.setExportWorkspace(exportWorkspace);
                if (removeImages)
                    workspace.clearAllImages(false);
                if (removeObjects)
                    workspace.clearAllObjects(false);

                return Status.TERMINATE_SILENT;

        }

        return Status.PASS;

    }

    static Level getLevel(String messageLevel) {
        switch (messageLevel) {
            case MessageLevels.DEBUG:
                return Level.DEBUG;
            case MessageLevels.MESSAGE:
            default:
                return Level.MESSAGE;
            case MessageLevels.WARNING:
                return Level.WARNING;
        }
    }

    public static boolean testFileExists(Metadata metadata, String genericFormat) {
        String name = "";
        try {
            name = FileTools.getGenericName(metadata, genericFormat);
        } catch (ServiceException | DependencyException | FormatException | IOException e) {
            return false;
        }

        // If no name matching the format was found
        if (name == null)
            return false;

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
        return AbstractNumericObjectFilter.testFilter(measurementValue, referenceValue, referenceMode);

    }

    public static boolean testNumericMetadata(String metadataValue, String referenceMode, double referenceValue) {
        try {
            double testValue = Double.parseDouble(metadataValue);
            return AbstractNumericObjectFilter.testFilter(testValue, referenceValue, referenceMode);

        } catch (NumberFormatException e) {
            // This will be thrown if the value specified wasn't a number
            MIA.log.writeWarning("Specified metadata value for filtering (" + metadataValue
                    + ") wasn't a number.  Terminating this run.");
            return false;

        }
    }

    public static boolean testTextMetadata(String metadataValue, String referenceMode, String referenceValue) {
            return AbstractTextObjectFilter.testFilter(metadataValue, referenceValue, referenceMode);
    }

    public static boolean testObjectCount(Objs inputObjects, String referenceMode, double referenceValue) {
        int testValue = 0;
        if (inputObjects != null)
            testValue = inputObjects.size();

        return AbstractNumericObjectFilter.testFilter(testValue, referenceValue, referenceMode);

    }

    @Override
    public Category getCategory() {
        return Categories.WORKFLOW;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.1";
    }

    @Override
    public String getDescription() {
        return "Implement workflow handling outcome based on a variety of metrics (e.g. object counts, image measurements, metadata values).  Outcomes can include termination of the analysis and redirection of the active module to another part of the workflow.  Redirection allows parts of the analysis to skipped.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String testMode = parameters.getValue(TEST_MODE, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String numericFilterMode = parameters.getValue(NUMERIC_FILTER_MODE, workspace);
        String textFilterMode = parameters.getValue(TEXT_FILTER_MODE, workspace);
        String referenceImageMeasurement = parameters.getValue(IMAGE_MEASUREMENT, workspace);
        String referenceMetadataValue = parameters.getValue(METADATA_VALUE, workspace);
        double referenceValueNumber = parameters.getValue(REFERENCE_NUMERIC_VALUE, workspace);
        String referenceValueText = parameters.getValue(REFERENCE_TEXT_VALUE, workspace);
        double fixedValueNumber = parameters.getValue(FIXED_VALUE, workspace);
        String genericFormat = parameters.getValue(GENERIC_FORMAT, workspace);
        boolean showRedirectMessage = parameters.getValue(SHOW_REDIRECT_MESSAGE, workspace);

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
                terminate = fixedValueNumber == referenceValueNumber;
                break;
            case TestModes.IMAGE_MEASUREMENT:
                Image inputImage = workspace.getImage(inputImageName);
                terminate = testImageMeasurement(inputImage, referenceImageMeasurement, numericFilterMode,
                        referenceValueNumber);
                break;
            case TestModes.METADATA_NUMERIC_VALUE:
                Object value = workspace.getMetadata().get(referenceMetadataValue);
                if (value == null)
                    terminate = true;
                else
                    terminate = testNumericMetadata(value.toString(), numericFilterMode, referenceValueNumber);
                break;
            case TestModes.METADATA_TEXT_VALUE:
                value = workspace.getMetadata().get(referenceMetadataValue);
                if (value == null)
                    terminate = true;
                else
                    terminate = testTextMetadata(value.toString(), textFilterMode, referenceValueText);
                break;
            case TestModes.OBJECT_COUNT:
                Objs inputObjects = workspace.getObjects(inputObjectsName);
                terminate = testObjectCount(inputObjects, numericFilterMode, referenceValueNumber);
                break;
        }

        if (terminate)
            return processTermination(parameters, workspace, showRedirectMessage);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(CONDITION_SEPARATOR, this));
        parameters.add(new ChoiceP(TEST_MODE, this, TestModes.IMAGE_MEASUREMENT, TestModes.ALL));

        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(NUMERIC_FILTER_MODE, this, NumericFilterModes.LESS_THAN, NumericFilterModes.ALL));
        parameters.add(new ChoiceP(TEXT_FILTER_MODE, this, TextFilterModes.CONTAINS, TextFilterModes.ALL));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT, this));
        parameters.add(new MetadataItemP(METADATA_VALUE, this));
        parameters.add(new DoubleP(REFERENCE_NUMERIC_VALUE, this, 0d));
        parameters.add(new StringP(REFERENCE_TEXT_VALUE, this));
        parameters.add(new DoubleP(FIXED_VALUE, this, 0d));
        parameters.add(new StringP(GENERIC_FORMAT, this));
        parameters.add(new MessageP(AVAILABLE_METADATA_FIELDS, this, ParameterState.MESSAGE, 170));

        parameters.add(new SeparatorP(RESULT_SEPARATOR, this));
        parameters.add(new ChoiceP(CONTINUATION_MODE, this, ContinuationModes.TERMINATE, ContinuationModes.ALL));
        parameters.add(new ModuleP(REDIRECT_MODULE, this, true));
        parameters.add(new BooleanP(SHOW_REDIRECT_MESSAGE, this, false));
        parameters.add(new StringP(REDIRECT_MESSAGE, this, ""));
        parameters.add(new ChoiceP(MESSAGE_LEVEL, this, MessageLevels.MESSAGE, MessageLevels.ALL));
        parameters.add(new BooleanP(SHOW_TERMINATION_WARNING, this, true));
        parameters.add(new BooleanP(EXPORT_WORKSPACE, this, true));
        parameters.add(new BooleanP(REMOVE_IMAGES, this, false));
        parameters.add(new BooleanP(REMOVE_OBJECTS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(CONDITION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(TEST_MODE));
        switch ((String) parameters.getValue(TEST_MODE, workspace)) {
            case TestModes.FILE_EXISTS:
            case TestModes.FILE_DOES_NOT_EXIST:
                returnedParameters.add(parameters.getParameter(GENERIC_FORMAT));
                returnedParameters.add(parameters.getParameter(AVAILABLE_METADATA_FIELDS));
                MetadataRefs metadataRefs = modules.getMetadataRefs(this);
                parameters.getParameter(AVAILABLE_METADATA_FIELDS).setValue(metadataRefs.getMetadataValues());
                break;

            case TestModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE));
                returnedParameters.add(parameters.getParameter(REFERENCE_NUMERIC_VALUE));
                break;

            case TestModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(IMAGE_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(NUMERIC_FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_NUMERIC_VALUE));

                String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
                ImageMeasurementP parameter = parameters.getParameter(IMAGE_MEASUREMENT);
                parameter.setImageName(inputImageName);
                break;

            case TestModes.METADATA_NUMERIC_VALUE:
                returnedParameters.add(parameters.getParameter(METADATA_VALUE));
                returnedParameters.add(parameters.getParameter(NUMERIC_FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_NUMERIC_VALUE));
                break;

            case TestModes.METADATA_TEXT_VALUE:
                returnedParameters.add(parameters.getParameter(METADATA_VALUE));
                returnedParameters.add(parameters.getParameter(TEXT_FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_TEXT_VALUE));
                break;

            case TestModes.OBJECT_COUNT:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(NUMERIC_FILTER_MODE));
                returnedParameters.add(parameters.getParameter(REFERENCE_NUMERIC_VALUE));
                break;
        }

        returnedParameters.add(parameters.getParameter(RESULT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONTINUATION_MODE));
        switch ((String) parameters.getValue(CONTINUATION_MODE, workspace)) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                returnedParameters.add(parameters.getParameter(REDIRECT_MODULE));
                redirectModuleID = parameters.getValue(REDIRECT_MODULE, workspace);
                returnedParameters.add(parameters.getParameter(SHOW_REDIRECT_MESSAGE));
                if ((boolean) parameters.getValue(SHOW_REDIRECT_MESSAGE, workspace)) {
                    returnedParameters.add(parameters.getParameter(REDIRECT_MESSAGE));
                    returnedParameters.add(parameters.getParameter(MESSAGE_LEVEL));
                }
                break;
            case ContinuationModes.TERMINATE:
                returnedParameters.add(parameters.getParameter(SHOW_TERMINATION_WARNING));
                returnedParameters.add(parameters.getParameter(EXPORT_WORKSPACE));
                returnedParameters.add(parameters.getParameter(REMOVE_IMAGES));
                returnedParameters.add(parameters.getParameter(REMOVE_OBJECTS));
                redirectModuleID = null;
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
        return null;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
        return true;
    }

    void addParameterDescriptions() {

        parameters.get(TEST_MODE).setDescription("Controls what condition is being tested:<br><ul>"

                + "<li>\"" + TestModes.IMAGE_MEASUREMENT + "\" Numeric filter against a measurement (specified by \""
                + IMAGE_MEASUREMENT + "\") associated with an image from the workspace (specified by \"" + INPUT_IMAGE
                + "\").</li>"

                + "<li>\"" + TestModes.METADATA_NUMERIC_VALUE
                + "\" Numeric filter against a metadata value (specified by \"" + METADATA_VALUE
                + "\") associated with the workspace.  Metadata values are stored as text, but this filter will attempt to parse any numeric values as numbers.  Text comparison can be done using \""
                + TestModes.METADATA_TEXT_VALUE + "\" mode.</li>"

                + "<li>\"" + TestModes.METADATA_TEXT_VALUE + "\" Text filter against a metadata value (specified by \""
                + METADATA_VALUE
                + "\") associated with the workspace.  This filter compares for exact text matches to a reference, specified by \""
                + REFERENCE_TEXT_VALUE + "\"</li>"

                + "<li>\"" + TestModes.FILE_EXISTS
                + "\" Checks if a specified file exists on the accessible computer filesystem.</li>"

                + "<li>\"" + TestModes.FILE_DOES_NOT_EXIST
                + "\" Checks if a specified file doesn't exist on the accessible computer filesystem.</li>"

                + "<li>\"" + TestModes.FIXED_VALUE + "\" Numeric filter against a fixed value.</li>"

                + "<li>\"" + TestModes.OBJECT_COUNT
                + "\" Numeric filter against the number of objects contained in an object collection stored in the workspace (specified by \""
                + INPUT_OBJECTS + "\").</li></ul>");

        parameters.get(INPUT_IMAGE)
                .setDescription("If testing against an image measurement (\"" + TEST_MODE + "\" set to \""
                        + TestModes.IMAGE_MEASUREMENT
                        + "\"), this is the image from which that measurement will be taken.");

        parameters.get(INPUT_OBJECTS)
                .setDescription("If testing against an object count (\"" + TEST_MODE + "\" set to \""
                        + TestModes.OBJECT_COUNT + "\"), this is the object collection which will be counted.");

        parameters.get(NUMERIC_FILTER_MODE)
                .setDescription("Numeric comparison used to determine whether the test passes or fails.  Choices are: "
                        + String.join(", ", NumericFilterModes.ALL) + ".");

        parameters.get(TEXT_FILTER_MODE)
                .setDescription("Text comparison used to determine whether the test passes or fails.  Choices are: "
                        + String.join(", ", TextFilterModes.ALL) + ".");

        parameters.get(IMAGE_MEASUREMENT)
                .setDescription("If testing against an image measurement (\"" + TEST_MODE + "\" set to \""
                        + TestModes.IMAGE_MEASUREMENT + "\"), this is the measurement from the image (specified by \""
                        + INPUT_IMAGE + "\") that will be tested.");

        parameters.get(METADATA_VALUE).setDescription(
                "If testing against a metadata value (either text or numeric) associated with the active workspace (\""
                        + TEST_MODE + "\" set to \"" + TestModes.METADATA_TEXT_VALUE
                        + "\"), this is the value that will be tested.");

        parameters.get(REFERENCE_NUMERIC_VALUE).setDescription(
                "If testing against a numeric value, this is the reference value against which it will be tested.  What classes as a pass or fail is determined by the parameter \""
                        + NUMERIC_FILTER_MODE + "\".");

        parameters.get(REFERENCE_TEXT_VALUE).setDescription(
                "If testing against a text value, this is the reference value against which it will be tested.  What classes as a pass or fail is determined by the parameter \""
                        + TEXT_FILTER_MODE + "\".");

        parameters.get(FIXED_VALUE).setDescription("If testing against a fixed numeric value (\"" + TEST_MODE
                + "\" set to \"" + TestModes.FIXED_VALUE + "\"), this is the value that will be tested.");

        parameters.get(GENERIC_FORMAT).setDescription(
                "Format for a generic filename.  Plain text can be mixed with global variables or metadata values currently stored in the workspace.  Global variables are specified using the \"V{name}\" notation, where \"name\" is the name of the variable to insert.  Similarly, metadata values are specified with the \"M{name}\" notation.");

        parameters.get(AVAILABLE_METADATA_FIELDS).setDescription(
                "List of the currently-available metadata values for this workspace.  These can be used when compiling a generic filename.");

        parameters.get(CONTINUATION_MODE)
                .setDescription("Controls what happens if the termination/redirection condition is met:<br><ul>"

                        + "<li>\"" + ContinuationModes.REDIRECT_TO_MODULE
                        + "\" The analysis workflow will skip to the module specified by the \"" + REDIRECT_MODULE
                        + "\" parameter.  Any modules between the present module and the target module will not be evaluated.</li>"

                        + "<li>\"" + ContinuationModes.TERMINATE
                        + "\" The analysis will stop evaluating any further modules.</li></ul>");

        parameters.get(REDIRECT_MODULE).setDescription(
                "If the condition is met, the workflow will redirect to this module.  In doing so, it will skip evaluation of any modules between the present module and this module.");

        parameters.get(SHOW_REDIRECT_MESSAGE)
                .setDescription("Controls if a message should be displayed in the log if redirection occurs.");

        parameters.get(REDIRECT_MESSAGE).setDescription("Message to display if redirection occurs.");

        parameters.get(MESSAGE_LEVEL).setDescription(
                "Controls the logging level in which the message will be displayed.  Warnings are enabled for users by default, but debug and message aren't.");

        parameters.get(EXPORT_WORKSPACE).setDescription(
                "Controls if the workspace should still be exported to the output Excel spreadsheet if termination occurs.");

        parameters.get(REMOVE_IMAGES).setDescription(
                "Controls if images should be completely removed from the workspace along with any associated measurements if termination occurs.");

        parameters.get(REMOVE_OBJECTS).setDescription(
                "Controls if objects should be completely removed from the workspace along with any associated measurements if termination occurs.");

    }
}
