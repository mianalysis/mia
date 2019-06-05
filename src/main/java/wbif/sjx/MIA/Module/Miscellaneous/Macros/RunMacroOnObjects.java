package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;

import java.awt.*;
import java.util.LinkedHashSet;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class RunMacroOnObjects extends CoreMacroRunner {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
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

        // Getting a list of measurement headings
        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group,MEASUREMENT_HEADING);

        // Setting the MacroHandler to the current workspace
        MacroHandler.setWorkspace(workspace);

        // Applying the macro. Only one macro can be run at a time, so this checks if a macro is already running
        while (MIA.isMacroLocked()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        MIA.setMacroLock(true);

        // Show current image
        for (Obj inputObject:inputObjects.values()) {
            Image inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
            ImagePlus inputImagePlus;
            if (inputImage != null) {
                inputImagePlus = inputImage.getImagePlus().duplicate();
                inputImagePlus.show();
            }

            // Creating argument string (objectsName, ID)
            String arg = inputObjectsName + "," +inputObject.getID();

            // Now run the macro
            switch (macroMode) {
                case MacroModes.MACRO_FILE:
                    IJ.runMacroFile(macroFile,arg);
                    break;
                case MacroModes.MACRO_TEXT:
                    IJ.runMacro(macroText,arg);
                    break;
            }

            // Intercepting measurements
            ResultsTable table = ResultsTable.getResultsTable();
            for (String expectedMeasurement:expectedMeasurements) {
                Measurement measurement = interceptMeasurement(table,expectedMeasurement);
                inputObject.addMeasurement(measurement);
            }

            // Closing the results table
            table.reset();
            Frame resultsFrame = WindowManager.getFrame("Results");
            if (resultsFrame != null) resultsFrame.dispose();

        }

        // Releasing the macro lock
        MIA.setMacroLock(false);

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(MACRO_SEPARATOR,this));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,"// This block of code will provide the input object name, " +
                "along with its ID number.\n\nrun(\"Enable MIA Extensions\");\nargs = split(getArgument(),\",\");\n" +
                "inputObjectsName = args[0];\nobjectID = args[1];\n\n",true));
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
