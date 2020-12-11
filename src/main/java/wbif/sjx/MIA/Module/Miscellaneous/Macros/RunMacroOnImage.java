// TODO: Could add option to store variables as metadata as well as image measurements (this would allow variables to be stored even if no image is provided)

package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.CustomInterpreter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.GenericButtonP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by Stephen on 31/01/2018.
 */
public class RunMacroOnImage extends AbstractMacroRunner {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String VARIABLE_SEPARATOR = "Variables input";
    public static final String MACRO_SEPARATOR = "Macro definition";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh macro";
    public static final String IMAGE_OUTPUT_SEPARATOR = "Image output";
    public static final String INTERCEPT_OUTPUT_IMAGE = "Intercept output image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_SEPARATOR = "Measurement output";
    public static final String ADD_INTERCEPTED_VARIABLE = "Intercept variable as measurement";
    public static final String VARIABLE = "Variable";

    public interface MacroModes {
        String MACRO_FILE = "Macro file";
        String MACRO_TEXT = "Macro text";

        String[] ALL = new String[] { MACRO_FILE, MACRO_TEXT };

    }

    public RunMacroOnImage(ModuleCollection modules) {
        super("Run macro on image", modules);
    }

    static ArrayList<ImagePlus> hideImages() {
        ArrayList<ImagePlus> openImages = new ArrayList<>();
        String[] imageTitles = WindowManager.getImageTitles();
        for (String imageTitle : imageTitles) {
            ImagePlus openImage = WindowManager.getImage(imageTitle);
            openImages.add(openImage);
            openImage.hide();
        }

        return openImages;

    }


    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS_MACROS;
    }

    @Override
    public String getDescription() {
        return "Run a specific ImageJ macro once (as opposed to the \"" + new RunMacroOnObjects(modules).getName()
                + "\" module, which runs once per object).  This module can optionally open an image into ImageJ for the macro to run on.  It can also intercept the output image and store it in the MIA workspace.  Variables assigned during the macro can be extracted and stored as measurements associated with the input image.<br><br>"

                + "Note: ImageJ can only run one macro at a time, so by using this module the \""
                + InputControl.SIMULTANEOUS_JOBS + "\" parameter of the \"" + new InputControl(null).getName()
                + "\" module must be set to 1.<br><br>"

                + "Note: When this module runs, all windows currently open in ImageJ will be automatically hidden, then re-opened upon macro completion.  This is to prevent accidental interference while the macro is running.  It also allows the macro to run much faster (batch mode).  To keep images open while the macro is running (for example, during debugging) start the macro with the command \"setBatchMode(false)\".";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        boolean provideInputImage = parameters.getValue(PROVIDE_INPUT_IMAGE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String macroMode = parameters.getValue(MACRO_MODE);
        String macroText = parameters.getValue(MACRO_TEXT);
        String macroFile = parameters.getValue(MACRO_FILE);
        boolean interceptOutputImage = parameters.getValue(INTERCEPT_OUTPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);


        // Getting a list of measurement headings
        ParameterGroup measurementGroup = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(measurementGroup, VARIABLE);

        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);
        MacroHandler.setModules(modules);

        // Get current image
        Image inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
        ImagePlus inputImagePlus = (inputImage != null) ? inputImage.getImagePlus().duplicate() : null;

        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(MacroModes.MACRO_FILE))
            macroText = IJ.openAsString(macroFile);

        // Appending variables to the front of the macro
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        String finalMacroText = addVariables(macroText, variableGroup);

        // If providing the input image direct from the workspace, hide all open windows
        // while the macro runs
        ArrayList<ImagePlus> openImages = null;
        if (provideInputImage)
            openImages = hideImages();

        // Running the macro
        CustomInterpreter interpreter = new CustomInterpreter();
        try {
            inputImagePlus = interpreter.runBatchMacro(finalMacroText, inputImagePlus);
            if (interpreter.wasError())
                throw new RuntimeException();
        } catch (RuntimeException e) {
            IJ.runMacro("setBatchMode(false)");
            if (provideInputImage)
                for (ImagePlus openImage : openImages)
                    openImage.show();
            MIA.log.writeError("Macro failed with error \"" + interpreter.getErrorMessage() + "\".  Skipping file.");
            return Status.FAIL;
        }

        // If providing the input image direct from the workspace, re-opening all open
        // windows
        if (provideInputImage)
            for (ImagePlus openImage : openImages)
                openImage.show();

        if (interceptOutputImage && inputImagePlus != null) {
            if (applyToInput && inputImage != null) {
                inputImage.setImagePlus(inputImagePlus);
                if (showOutput)
                    inputImage.showImage();
            } else {
                Image outputImage = new Image(outputImageName, inputImagePlus);
                workspace.addImage(outputImage);
                if (showOutput)
                    outputImage.showImage();
            }
        }

        // Intercepting measurements
        if (provideInputImage) {
            for (String expectedMeasurement : expectedMeasurements) {
                double value = interpreter.getVariable(expectedMeasurement);
                Measurement measurement = new Measurement(getFullName(expectedMeasurement), value);
                inputImage.addMeasurement(measurement);
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE, this, true));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(VARIABLE_SEPARATOR, this));

        parameters.add(new SeparatorP(MACRO_SEPARATOR, this));
        parameters.add(new ChoiceP(MACRO_MODE, this, MacroModes.MACRO_TEXT, MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT, this, "// A variable has been pre-defined for the input image name."
                + "\n\nrun(\"Enable MIA Extensions\");\n\n", true));
        parameters.add(new FilePathP(MACRO_FILE, this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON, this, "Refresh", GenericButtonP.DefaultModes.REFRESH));

        parameters.add(new SeparatorP(IMAGE_OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(INTERCEPT_OUTPUT_IMAGE, this, true));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        ParameterCollection measurementCollection = new ParameterCollection();
        measurementCollection.add(new StringP(VARIABLE, this));
        parameters.add(new ParameterGroup(ADD_INTERCEPTED_VARIABLE, this, measurementCollection));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PROVIDE_INPUT_IMAGE));
        if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(VARIABLE_SEPARATOR));
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(MACRO_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MACRO_MODE));
        switch ((String) parameters.getValue(MACRO_MODE)) {
            case MacroModes.MACRO_FILE:
                returnedParameters.add(parameters.getParameter(MACRO_FILE));
                break;
            case MacroModes.MACRO_TEXT:
                returnedParameters.add(parameters.getParameter(MACRO_TEXT));
                returnedParameters.add(parameters.getParameter(REFRESH_BUTTON));
                break;
        }

        returnedParameters.add(parameters.getParameter(IMAGE_OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INTERCEPT_OUTPUT_IMAGE));
        if ((boolean) parameters.getValue(INTERCEPT_OUTPUT_IMAGE)) {
            if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE)) {
                returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
                if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
                    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                }
            } else {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
            returnedParameters.add(parameters.getParameter(ADD_INTERCEPTED_VARIABLE));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        // If no input image is provided, there's nowhere to store the values
        if (!(boolean) parameters.getValue(PROVIDE_INPUT_IMAGE))
            return returnedRefs;

        String inputImage = parameters.getValue(INPUT_IMAGE);

        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group, VARIABLE);

        for (String expectedMeasurement : expectedMeasurements) {
            String fullName = getFullName(expectedMeasurement);
            ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(fullName);
            ref.setImageName(inputImage);
            returnedRefs.add(ref);
        }

        return returnedRefs;

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

    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(PROVIDE_INPUT_IMAGE).setDescription(
                "When selected, a specified image from the workspace will be opened prior to running the macro.  This image will be the \"active\" image the macro runs on.");

        parameters.get(INPUT_IMAGE).setDescription("If \"" + PROVIDE_INPUT_IMAGE
                + "\" is selected, this is the image that will be loaded into the macro.  A duplicate of this image is made, so the image stored in the workspace will not be affected by any processing in the macro.  The final active image once the macro has completed can be stored in the workspace using the \""
                + INTERCEPT_OUTPUT_IMAGE + "\" parameter.");

        parameters.get(MACRO_MODE)
                .setDescription("Select the source for the macro code:<br><ul>" + "<li>\"" + MacroModes.MACRO_FILE
                        + "\" Load the macro from the file specified by the \"" + MACRO_FILE + "\" parameter.</li>"

                        + "<li>\"" + MacroModes.MACRO_TEXT + "\" Macro code is written directly into the \""
                        + MACRO_TEXT + "\" box.</li></ul>");

        parameters.get(MACRO_TEXT).setDescription(
                "Macro code to be executed.  MIA macro commands are enabled using the \"run(\"Enable MIA Extensions\");\" command which is included by default.  This should always be the first line of a macro if these commands are needed.");

        parameters.get(MACRO_FILE)
                .setDescription("Select a macro file (.ijm) to run once, after all analysis runs have completed.");

        parameters.get(REFRESH_BUTTON).setDescription(
                "This button refreshes the macro code as stored within MIA.  Clicking this will create an \"undo\" checkpoint.");

        parameters.get(INTERCEPT_OUTPUT_IMAGE).setDescription(
                "When selected, the image currently active in ImageJ at completion of the macro can be stored into the workspace.  This can either overwrite the input image in the workspace or be stored as a new image (controlled by \""
                        + APPLY_TO_INPUT + "\").");

        parameters.get(APPLY_TO_INPUT).setDescription("When this and \"" + INTERCEPT_OUTPUT_IMAGE
                + "\" are selected, the image active in ImageJ at completion of the macro will update the input image in the MIA workspace.  Otherwise, the actie image will be stored as a new image in the workspace with the name specified by \""
                + OUTPUT_IMAGE + "\".");

        parameters.get(OUTPUT_IMAGE).setDescription("When \"" + INTERCEPT_OUTPUT_IMAGE
                + "\" is selected, but not updating the input image, the image active in ImageJ at completion of the macro will be stored in the MIA workspace with this name.  This image will be accessible to other modules using this name.");

        parameters.get(ADD_INTERCEPTED_VARIABLE).setDescription(
                "This allows variables assigned in the macro to be stored as measurements associated with the input image.");

        ParameterGroup group = (ParameterGroup) parameters.get(ADD_INTERCEPTED_VARIABLE);
        ParameterCollection collection = group.getTemplateParameters();
        collection.get(VARIABLE).setDescription(
                "Variable assigned in the macro to be stored as a measurement associated with the input image.  This name must exactly match (including case) the name as written in the macro.");
    }
}
