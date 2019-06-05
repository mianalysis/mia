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
public class RunMacroOnImage extends CoreMacroRunner {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MACRO_SEPARATOR = "Macro definition";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh parameters";
    public static final String IMAGE_OUTPUT_SEPARATOR = "Image output";
    public static final String INTERCEPT_OUTPUT_IMAGE = "Intercept output image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_SEPARATOR = "Measurement output";
    public static final String ADD_INTERCEPTED_MEASUREMENT = "Add intercepted measurement";
    public static final String MEASUREMENT_HEADING = "Measurement heading";

    public interface MacroModes {
        String MACRO_FILE = "Macro file";
        String MACRO_TEXT = "Macro text";

        String[] ALL = new String[]{MACRO_FILE,MACRO_TEXT};

    }

    public RunMacroOnImage(ModuleCollection modules) {
        super("Run macro on image",modules);
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
        boolean provideInputImage = parameters.getValue(PROVIDE_INPUT_IMAGE);
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String macroMode = parameters.getValue(MACRO_MODE);
        String macroText = parameters.getValue(MACRO_TEXT);
        String macroFile = parameters.getValue(MACRO_FILE);
        boolean interceptOutputImage = parameters.getValue(INTERCEPT_OUTPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

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
        Image inputImage = provideInputImage ? workspace.getImage(inputImageName) : null;
        ImagePlus inputImagePlus;
        if (inputImage != null) {
            inputImagePlus = inputImage.getImagePlus().duplicate();
            inputImagePlus.show();
        }

        // Now run the macro
        switch (macroMode) {
            case MacroModes.MACRO_FILE:
                IJ.runMacroFile(macroFile);
                break;
            case MacroModes.MACRO_TEXT:
                IJ.runMacro(macroText);
                break;
        }

        if (interceptOutputImage) {
            inputImagePlus = IJ.getImage();
            if (inputImage != null && inputImagePlus != null) {
                if (applyToInput) {
                    inputImage.setImagePlus(inputImagePlus);
                    if (showOutput) inputImage.showImage();
                } else {
                    Image outputImage = new Image(outputImageName, inputImagePlus);
                    workspace.addImage(outputImage);
                    if (showOutput) outputImage.showImage();
                }

                // If it hasn't already been hidden or removed, hide it now
                inputImagePlus.hide();
            }
        }

        // Intercepting measurements
        ResultsTable table = ResultsTable.getResultsTable();
        for (String expectedMeasurement:expectedMeasurements) {
            Measurement measurement = interceptMeasurement(table,expectedMeasurement);
            inputImage.addMeasurement(measurement);
        }

        // Closing the results table
        table.reset();
        Frame resultsFrame = WindowManager.getFrame("Results");
        if (resultsFrame != null) resultsFrame.dispose();

        // Releasing the macro lock
        MIA.setMacroLock(false);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(MACRO_SEPARATOR,this));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,true));
        parameters.add(new FilePathP(MACRO_FILE,this));
        parameters.add(new RefreshButtonP(REFRESH_BUTTON,this));

        parameters.add(new ParamSeparatorP(IMAGE_OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(INTERCEPT_OUTPUT_IMAGE,this,true));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        ParameterCollection collection = new ParameterCollection();
        collection.add(new StringP(MEASUREMENT_HEADING,this));
        parameters.add(new ParameterGroup(ADD_INTERCEPTED_MEASUREMENT,this,collection));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
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

        returnedParameters.add(parameters.getParameter(IMAGE_OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INTERCEPT_OUTPUT_IMAGE));
        if (parameters.getValue(INTERCEPT_OUTPUT_IMAGE)) {
            if (parameters.getValue(PROVIDE_INPUT_IMAGE)) {
                returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
                if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
                    returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
                }
            } else {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImage = parameters.getValue(INPUT_IMAGE);

        ParameterGroup group = parameters.getParameter(ADD_INTERCEPTED_MEASUREMENT);
        LinkedHashSet<String> expectedMeasurements = expectedMeasurements(group,MEASUREMENT_HEADING);

        for (String expectedMeasurement:expectedMeasurements) {
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
