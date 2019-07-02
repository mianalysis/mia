package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.Interpreter;
import ij.measure.ResultsTable;
import ij.text.TextWindow;
import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class RunMacroOnObjects extends CoreMacroRunner {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String VARIABLE_SEPARATOR = "Variables input";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_VALUE = "Variable value";
    public static final String ADD_VARIABLE = "Add variable";
    public static final String MACRO_SEPARATOR = "Macro definition";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh parameters";
    public static final String OUTPUT_SEPARATOR = "Measurement output";
    public static final String ADD_INTERCEPTED_MEASUREMENT = "Add intercepted measurement";
    public static final String MEASUREMENT_HEADING = "Measurement heading";


    public interface MacroModes {
        String MACRO_FILE = "Macro file";
        String MACRO_TEXT = "Macro text";

        String[] ALL = new String[]{MACRO_FILE,MACRO_TEXT};

    }


    public RunMacroOnObjects(ModuleCollection modules) {
        super("Run macro on objects",modules);
    }

    static String addObjectToMacroText(String macroString, String objectsName, int objectID) {
        StringBuilder sb = new StringBuilder();

        // Adding the object name
        sb.append("objectName=\"");
        sb.append(objectsName);
        sb.append("\";\n");

        // Adding the object ID
        sb.append("ID=");
        sb.append(objectID);
        sb.append(";\n");

        sb.append(macroString);

        return sb.toString();

    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS_MACROS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        boolean provideInputImage = parameters.getValue(PROVIDE_INPUT_IMAGE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String macroMode = parameters.getValue(MACRO_MODE);
        String macroText = parameters.getValue(MACRO_TEXT);
        String macroFile = parameters.getValue(MACRO_FILE);

        // Getting the input objects
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting a Map of input variable names and their values
        ParameterGroup variableGroup = parameters.getParameter(ADD_VARIABLE);
        LinkedHashMap<String,String> inputVariables = inputVariables(variableGroup,VARIABLE_NAME,VARIABLE_VALUE);

        // Getting a list of measurement headings
        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group,MEASUREMENT_HEADING);

        // If the macro is stored as a file, load this to the macroText string
        if (macroMode.equals(RunMacroOnImage.MacroModes.MACRO_FILE)) macroText = IJ.openAsString(macroFile);

        // Appending variables to the front of the macro
        macroText = addVariables(macroText,inputVariables);

        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);

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
            writeMessage("Running macro on object "+(count++)+" of "+nTotal);

            // Appending object name and ID number onto macro
            String finalMacroText = addObjectToMacroText(macroText,inputObjectsName,inputObject.getID());

            // Get current image
            Image inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
            ImagePlus inputImagePlus = (inputImage != null) ? inputImage.getImagePlus().duplicate() : null;

            // Creating argument string (objectsName, ID)
            String arg = inputObjectsName + "," +inputObject.getID();

            // Running the macro
            inputImagePlus = new Interpreter().runBatchMacro(finalMacroText,inputImagePlus);

            // Intercepting measurements
            ResultsTable table = ResultsTable.getResultsTable();
            for (String expectedMeasurement:expectedMeasurements) {
                Measurement measurement = interceptMeasurement(table,expectedMeasurement);
                inputObject.addMeasurement(measurement);
            }

            // Closing the results table
            TextWindow window = ResultsTable.getResultsWindow();
            if (window != null) window.close(false);

        }

        // If providing the input image direct from the workspace, re-opening all open windows
        if (provideInputImage) {
            for (ImagePlus openImage:openImages) openImage.show();
        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(VARIABLE_SEPARATOR,this));
        ParameterCollection variableCollection = new ParameterCollection();
        variableCollection.add(new StringP(VARIABLE_NAME,this));
        variableCollection.add(new StringP(VARIABLE_VALUE,this));
        parameters.add(new ParameterGroup(ADD_VARIABLE,this,variableCollection));

        parameters.add(new ParamSeparatorP(MACRO_SEPARATOR,this));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,"// Variables have been pre-defined for the input object name " +
                "(\"objectName\") and its ID number (\"ID\").\n\nrun(\"Enable MIA Extensions\");\n\n",true));
        parameters.add(new FilePathP(MACRO_FILE,this));
        parameters.add(new RefreshButtonP(REFRESH_BUTTON,this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        ParameterCollection collection = new ParameterCollection();
        collection.add(new StringP(MEASUREMENT_HEADING,this));
        parameters.add(new ParameterGroup(ADD_INTERCEPTED_MEASUREMENT,this,collection));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(PROVIDE_INPUT_IMAGE));
        if (parameters.getValue(PROVIDE_INPUT_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(VARIABLE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_VARIABLE));

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
        returnedParameters.add(parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT));

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

        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group,MEASUREMENT_HEADING);

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
