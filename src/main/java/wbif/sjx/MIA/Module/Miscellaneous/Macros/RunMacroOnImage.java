package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import com.sun.corba.se.spi.orbutil.threadpool.Work;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Macro.MacroHandler;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import javax.annotation.Nullable;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class RunMacroOnImage extends Module {
    public static final String PROVIDE_INPUT_IMAGE = "Provide input image";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MACRO_MODE = "Macro mode";
    public static final String MACRO_TEXT = "Macro text";
    public static final String MACRO_FILE = "Macro file";
    public static final String REFRESH_BUTTON = "Refresh parameters";
    public static final String INTERCEPT_OUTPUT_IMAGE = "Intercept output image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";


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
    public String getHelp() {
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

        // Releasing the macro lock
        MIA.setMacroLock(false);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new BooleanP(PROVIDE_INPUT_IMAGE,this,true));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new ChoiceP(MACRO_MODE,this,MacroModes.MACRO_TEXT,MacroModes.ALL));
        parameters.add(new TextAreaP(MACRO_TEXT,this,true));
        parameters.add(new FilePathP(MACRO_FILE,this));
        parameters.add(new RefreshButtonP(REFRESH_BUTTON,this));
        parameters.add(new BooleanP(INTERCEPT_OUTPUT_IMAGE,this,true));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(PROVIDE_INPUT_IMAGE));
        if (parameters.getValue(PROVIDE_INPUT_IMAGE)) {
            returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        }

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

}
