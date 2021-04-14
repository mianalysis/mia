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
import wbif.sjx.MIA.Module.Core.InputControl;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.FilePathP;
import wbif.sjx.MIA.Object.Parameters.GenericButtonP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.Parameters.Text.TextAreaP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class RunMacroOnObjects extends AbstractMacroRunner {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String UPDATE_INPUT_IMAGE = "Update image after each run";
    public static final String VARIABLE_SEPARATOR = "Variables input";
    public static final String MACRO_SEPARATOR = "Macro definition";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh parameters";
    public static final String OUTPUT_SEPARATOR = "Measurement output";
    public static final String ADD_INTERCEPTED_VARIABLE = "Intercept variable as measurement";
    public static final String VARIABLE = "Variable";
    

    public interface MacroModes {
        String MACRO_FILE = "Macro file";
        String MACRO_TEXT = "Macro text";

        String[] ALL = new String[]{MACRO_FILE,MACRO_TEXT};

    }


    public RunMacroOnObjects(ModuleCollection modules) {
        super("Run macro on objects",modules);
    }

    static String addObjectToMacroText(String macroString, String objectsName, int objectID, int count) {
        StringBuilder sb = new StringBuilder();

        // Adding the object name
        sb.append("objectName=\"");
        sb.append(objectsName);
        sb.append("\";\n");

        // Adding the object ID
        sb.append("ID=");
        sb.append(objectID);
        sb.append(";\n");

        // Adding the macro iteration count
        sb.append("count=");
        sb.append(count);
        sb.append(";\n");

        // Adding the main macro text
        sb.append(macroString);

        return sb.toString();

    }


    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS_MACROS;
    }

    @Override
    public String getDescription() {
        return "Run a specific ImageJ macro once per object from a specified input object collection (as opposed to the \"" + new RunMacro(null).getName()
        + "\" module, which runs once per analysis run).  This module can optionally open an image into ImageJ for the macro to run on.  Variables assigned during the macro can be extracted and stored as measurements associated with the current object.<br><br>"

        + "Note: ImageJ can only run one macro at a time, so by using this module the \""
        + InputControl.SIMULTANEOUS_JOBS + "\" parameter of the \"" + new InputControl(null).getName()
        + "\" module must be set to 1.<br><br>"

                + "Note: When this module runs, all windows currently open in ImageJ will be automatically hidden, then re-opened upon macro completion.  This is to prevent accidental interference while the macro is running.  It also allows the macro to run much faster (batch mode).  To keep images open while the macro is running (for example, during debugging) start the macro with the command \"setBatchMode(false)\".";
        
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        boolean provideInputImage = parameters.getValue(PROVIDE_INPUT_IMAGE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean updateInputImage = parameters.getValue(UPDATE_INPUT_IMAGE);
        String macroMode = parameters.getValue(MACRO_MODE);
        String macroText = parameters.getValue(MACRO_TEXT);
        String macroFile = parameters.getValue(MACRO_FILE);

        // Getting the input objects
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting a list of measurement headings
        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group, VARIABLE);

        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(RunMacro.MacroModes.MACRO_FILE)) macroText = IJ.openAsString(macroFile);

        // Appending variables to the front of the macro
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        macroText = addVariables(macroText, variableGroup);
        
        // If providing the input image direct from the workspace, hide all open windows while the macro runs
        ArrayList<ImagePlus> openImages = new ArrayList<>();
        if (provideInputImage) {
            String[] imageTitles = WindowManager.getImageTitles();
            for (String imageTitle:imageTitles) {
                ImagePlus openImage = WindowManager.getImage(imageTitle);
                openImages.add(openImage);
                openImage.hide();
            }
        }

        // Show current image
        int count = 1;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            writeStatus("Running macro on object "+(count++)+" of "+nTotal);

            // Appending object name and ID number onto macro
            String finalMacroText = addObjectToMacroText(macroText,inputObjectsName,inputObject.getID(),count-1);

            // Get current image
            Image inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
            ImagePlus inputImagePlus = (inputImage != null) ? inputImage.getImagePlus().duplicate() : null;

            // Running the macro
            CustomInterpreter interpreter = new CustomInterpreter();
            try {
                inputImagePlus = interpreter.runBatchMacro(finalMacroText, inputImagePlus);
                if (interpreter.wasError()) throw new RuntimeException();
            } catch (RuntimeException e) {
                String errorMessage = interpreter.getErrorMessage();
                if (errorMessage == null || errorMessage.equals("") || errorMessage.equals(" ")) continue; // Don't display blank errors
                MIA.log.writeWarning("Macro failed with error \""+errorMessage+"\" for object ID = "+inputObject.getID()+".  Skipping object.");
                continue;
            }

            // Intercepting measurements
            for (String expectedMeasurement : expectedMeasurements) {
                double value = interpreter.getVariable(expectedMeasurement);
                Measurement measurement = new Measurement(getFullName(expectedMeasurement), value);
                inputObject.addMeasurement(measurement);
            }
            
            // If necessary, updating the input image
            if (updateInputImage)
                inputImage.setImagePlus(inputImagePlus);

        }

        // If providing the input image direct from the workspace, re-opening all open windows
        if (provideInputImage) for (ImagePlus openImage:openImages) openImage.show();

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(UPDATE_INPUT_IMAGE, this, false));
        
        parameters.add(new SeparatorP(VARIABLE_SEPARATOR, this));

        parameters.add(new SeparatorP(MACRO_SEPARATOR,this));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,"// Variables have been pre-defined for the input object name " +
                "(\"objectName\"), its ID number (\"ID\") and the macro iteration count (\"count\")." +
                "\n\nrun(\"Enable MIA Extensions\");\n\n",true));
        parameters.add(new FilePathP(MACRO_FILE,this));
        parameters.add(new GenericButtonP(REFRESH_BUTTON,this,"Refresh",GenericButtonP.DefaultModes.REFRESH));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR,this));
        ParameterCollection collection = new ParameterCollection();
        collection.add(new StringP(VARIABLE,this));
        parameters.add(new ParameterGroup(ADD_INTERCEPTED_VARIABLE, this, collection));
        
        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(PROVIDE_INPUT_IMAGE));
        if ((boolean) parameters.getValue(PROVIDE_INPUT_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
            returnedParameters.add(parameters.getParameter(UPDATE_INPUT_IMAGE));
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

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_INTERCEPTED_VARIABLE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_VARIABLE);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group, VARIABLE);

        for (String expectedMeasurement:expectedMeasurements) {
            String fullName = getFullName(expectedMeasurement);
            ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(fullName);
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);
        }

        return returnedRefs;

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

        parameters.get(INPUT_OBJECTS).setDescription(
                "The specified macro will be run once on each of the objects from this object collection.  No information (e.g. assigned variables) is transferred between macro runs.");

        parameters.get(PROVIDE_INPUT_IMAGE).setDescription(
                "When selected, a specified image from the workspace will be opened prior to running the macro.  This image will be the \"active\" image the macro runs on.");

        parameters.get(INPUT_IMAGE).setDescription("If \"" + PROVIDE_INPUT_IMAGE
                + "\" is selected, this is the image that will be loaded into the macro.  A duplicate of this image is made, so the image stored in the workspace will not be affected by any processing in the macro.");

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

        parameters.get(ADD_INTERCEPTED_VARIABLE).setDescription(
                "This allows variables assigned in the macro to be stored as measurements associated with the current object.");

        ParameterGroup group = (ParameterGroup) parameters.get(ADD_INTERCEPTED_VARIABLE);
        ParameterCollection collection = group.getTemplateParameters();
        collection.get(VARIABLE).setDescription(
                "Variable assigned in the macro to be stored as a measurement associated with the current object.  This name must exactly match (including case) the name as written in the macro.");
    }
}
