// TODO: Could add option to store variables as metadata as well as image measurements (this would allow variables to be stored even if no image is provided)

package io.github.mianalysis.mia.module.script;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.Interpreter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen on 31/01/2018.
 */

/**
* Run a specific ImageJ macro once (as opposed to the "Run macro on objects" module, which runs once per object).  This module can optionally open an image into ImageJ for the macro to run on.  It can also intercept the output image and store it in the MIA workspace.  Variables assigned during the macro can be extracted and stored as measurements associated with the input image.<br><br>Note: ImageJ can only run one macro at a time, so by using this module the "Simultaneous jobs" parameter of the "Input control" module must be set to 1.<br><br>Note: When this module runs, all windows currently open in ImageJ will be automatically hidden, then re-opened upon macro completion.  This is to prevent accidental interference while the macro is running.  It also allows the macro to run much faster (batch mode).  To keep images open while the macro is running (for example, during debugging) start the macro with the command "setBatchMode(false)".
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunMacro extends AbstractMacroRunner {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* When selected, a specified image from the workspace will be opened prior to running the macro.  This image will be the "active" image the macro runs on.
	*/
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";

	/**
	* If "Provide input image" is selected, this is the image that will be loaded into the macro.  A duplicate of this image is made, so the image stored in the workspace will not be affected by any processing in the macro.  The final active image once the macro has completed can be stored in the workspace using the "Intercept output image" parameter.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* 
	*/
    public static final String VARIABLE_SEPARATOR = "Variables input";

	/**
	* 
	*/
    public static final String MACRO_SEPARATOR = "Macro definition";

	/**
	* Select the source for the macro code:<br><ul><li>"Macro file" Load the macro from the file specified by the "Macro file" parameter.</li><li>"Macro text" Macro code is written directly into the "Macro text" box.</li></ul>
	*/
    public static final String MACRO_MODE = "Macro mode";

	/**
	* Macro code to be executed.  MIA macro commands are enabled using the "run("Enable MIA Extensions");" command which is included by default.  This should always be the first line of a macro if these commands are needed.
	*/
    public static final String MACRO_TEXT = "Macro text";

	/**
	* Select a macro file (.ijm) to run once, after all analysis runs have completed.
	*/
    public static final String MACRO_FILE = "Macro file";

	/**
	* 
	*/
    public static final String IMAGE_OUTPUT_SEPARATOR = "ImageI output";

	/**
	* When selected, the image currently active in ImageJ at completion of the macro can be stored into the workspace.  This can either overwrite the input image in the workspace or be stored as a new image (controlled by "Apply to input image").
	*/
    public static final String INTERCEPT_OUTPUT_IMAGE = "Intercept output image";

	/**
	* When this and "Intercept output image" are selected, the image active in ImageJ at completion of the macro will update the input image in the MIA workspace.  Otherwise, the actie image will be stored as a new image in the workspace with the name specified by "Output image".
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* When "Intercept output image" is selected, but not updating the input image, the image active in ImageJ at completion of the macro will be stored in the MIA workspace with this name.  This image will be accessible to other modules using this name.
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Measurement output";

	/**
	* This allows variables assigned in the macro to be stored as measurements associated with the input image.
	*/
    public static final String ADD_INTERCEPTED_VARIABLE = "Intercept variable as measurement";
    public static final String VARIABLE = "Variable";

    public interface MacroModes {
        String MACRO_FILE = "Macro file";
        String MACRO_TEXT = "Macro text";

        String[] ALL = new String[] { MACRO_FILE, MACRO_TEXT };

    }

    public RunMacro(Modules modules) {
        super("Run macro", modules);
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
        return Categories.SCRIPT;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
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
    public Status process(WorkspaceI workspace) {
        // Getting input image
        boolean provideInputImage = parameters.getValue(PROVIDE_INPUT_IMAGE, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String macroMode = parameters.getValue(MACRO_MODE, workspace);
        String macroText = parameters.getValue(MACRO_TEXT, workspace);
        String macroFile = parameters.getValue(MACRO_FILE, workspace);
        boolean interceptOutputImage = parameters.getValue(INTERCEPT_OUTPUT_IMAGE, workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting a list of measurement headings
        ParameterGroup measurementGroup = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(measurementGroup, VARIABLE, workspace);

        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);
        MacroHandler.setModules(modules);

        // Get current image
        ImageI inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
        ImagePlus inputImagePlus = (inputImage != null) ? inputImage.getImagePlus().duplicate() : null;

        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(MacroModes.MACRO_FILE)) {
            macroText = IJ.openAsString(macroFile);
            macroText = GlobalVariables.convertString(macroText, modules);
            macroText = TextType.applyCalculation(macroText);
        }

        // Appending variables to the front of the macro
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        String finalMacroText = addVariables(macroText, variableGroup, workspace);

        // If providing the input image direct from the workspace, hide all open windows
        // while the macro runs
        ArrayList<ImagePlus> openImages = null;
        if (provideInputImage)
            openImages = hideImages();

        // Running the macro
        Interpreter interpreter = new Interpreter();
        interpreter.setIgnoreErrors(true);
        try {
            inputImagePlus = interpreter.runBatchMacro(finalMacroText, inputImagePlus);
            if (interpreter.getErrorMessage() != null)
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
                    inputImage.showAsIs();
            } else {
                ImageI outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
                workspace.addImage(outputImage);
                if (showOutput)
                    outputImage.showAsIs();
            }
        }

        // Intercepting measurements
        if (provideInputImage) {
            for (String expectedMeasurement : expectedMeasurements) {
                double value = interpreter.getVariable(expectedMeasurement);
                Measurement measurement = new Measurement(getFullName(expectedMeasurement), value);
                inputImage.addMeasurement(measurement);
            }

            if (showOutput && expectedMeasurements.size() != 0)
                inputImage.showMeasurements(this);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
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

        parameters.add(new SeparatorP(IMAGE_OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(INTERCEPT_OUTPUT_IMAGE, this, true));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        Parameters measurementCollection = new Parameters();
        measurementCollection.add(new StringP(VARIABLE, this));
        parameters.add(new ParameterGroup(ADD_INTERCEPTED_VARIABLE, this, measurementCollection));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PROVIDE_INPUT_IMAGE));
        if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE, workspace)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(VARIABLE_SEPARATOR));
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(MACRO_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MACRO_MODE));
        switch ((String) parameters.getValue(MACRO_MODE, workspace)) {
            case MacroModes.MACRO_FILE:
                returnedParameters.add(parameters.getParameter(MACRO_FILE));
                break;
            case MacroModes.MACRO_TEXT:
                returnedParameters.add(parameters.getParameter(MACRO_TEXT));
                break;
        }

        returnedParameters.add(parameters.getParameter(IMAGE_OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INTERCEPT_OUTPUT_IMAGE));
        if ((boolean) parameters.getValue(INTERCEPT_OUTPUT_IMAGE, workspace)) {
            if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE, workspace)) {
                returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
                if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
                    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                }
            } else {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
            returnedParameters.add(parameters.getParameter(ADD_INTERCEPTED_VARIABLE));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        // If no input image is provided, there's nowhere to store the values
        if (!(boolean) parameters.getValue(PROVIDE_INPUT_IMAGE, workspace))
            return returnedRefs;

        String inputImage = parameters.getValue(INPUT_IMAGE, workspace);

        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group, VARIABLE, workspace);

        for (String expectedMeasurement : expectedMeasurements) {
            String fullName = getFullName(expectedMeasurement);
            ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(fullName);
            ref.setImageName(inputImage);
            returnedRefs.add(ref);
        }

        return returnedRefs;

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
        Parameters collection = group.getTemplateParameters();
        collection.get(VARIABLE).setDescription(
                "Variable assigned in the macro to be stored as a measurement associated with the input image.  This name must exactly match (including case) the name as written in the macro.");
    }
}
