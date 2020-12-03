package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class RunSingleMacroCommand extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String MACRO_SEPARATOR = "Macro controls";
    public static final String MACRO_TITLE = "Macro title";
    public static final String ARGUMENTS = "Parameters";
    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public RunSingleMacroCommand(ModuleCollection modules) {
        super("Run single macro command", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS_MACROS;
    }

    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS_MACROS;
    }

    @Override
    public String getDescription() {
        return "Run a single macro command on an image from the workspace.   This module only runs commands of the format \"run([MACRO TITLE], [ARGUMENTS])\".  For example, the command \"run(\"Subtract Background...\", \"rolling=50 stack\");\" would be specified with the \""+MACRO_TITLE+"\" parameter set to \"Subtract Background...\" and the \""+ARGUMENTS+"\" parameter set to \"rolling=50 stack\".  For more advanced macro processing please use the \""+new RunMacroOnImage(null).getName()+"\" module.";
    }

    public void runMacroMultithreaded(ImagePlus inputImagePlus, String macroTitle, String arguments) {
        // Setting up multithreading
        int nThreads = Prefs.getThreads();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Applying the macro
        ImageStack ist = inputImagePlus.getStack();
        for (int i = 0; i < ist.size(); i++) {
            ImageProcessor ipr = ist.getProcessor(i + 1);
            ImagePlus ipl = new ImagePlus("Temp", ipr);
            Runnable task = () -> {
                IJ.run(ipl, macroTitle, arguments);
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        inputImagePlus.updateChannelAndDraw();
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String macroTitle = parameters.getValue(MACRO_TITLE);
        String arguments = parameters.getValue(ARGUMENTS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Only multithread the operation if it's being conducted on a single slice at a time.
        if (!arguments.contains("stack"))
            multithread = false;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImagePlus = new Duplicator().run(inputImagePlus);
        }

        if (multithread) {
            // If multithreading, remove the "stack" argument
            arguments = arguments.replace("stack","");
            runMacroMultithreaded(inputImagePlus, macroTitle, arguments);
        } else {
            IJ.run(inputImagePlus, macroTitle, arguments);
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();
        } else {
            if (showOutput)
                inputImage.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(MACRO_SEPARATOR, this));
        parameters.add(new StringP(MACRO_TITLE, this));
        parameters.add(new StringP(ARGUMENTS, this));
        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(MACRO_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MACRO_TITLE));
        returnedParameters.add(parameters.getParameter(ARGUMENTS));

        String arguments = parameters.getValue(ARGUMENTS);
        if (arguments.contains("stack")) {
            returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
            returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));
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

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply macro to.  This image is duplicated prior to application of the macro, so won't be updated by default.  To store any changes back onto this image, select the \""+APPLY_TO_INPUT+"\" parameter.");

      parameters.get(APPLY_TO_INPUT).setDescription("When selected, the image returned by the macro will be stored back into the MIA workspace at the same name as the input image.  This will update the input image.");

      parameters.get(OUTPUT_IMAGE).setDescription("When \""+APPLY_TO_INPUT+"\" is not selected this will store the macro output image into the MIA workspace with the name specified by this parameter.");

      parameters.get(MACRO_TITLE).setDescription("The macro command to run.  This must be the exact name as given by the ImageJ macro recorder.  Note: Only commands of the format \"run([MACRO TITLE], [ARGUMENTS])\" can be run by this module.  For more advanced macro processing please use the \""+new RunMacroOnImage(null).getName()+"\" module.");

      parameters.get(ARGUMENTS).setDescription("The options to pass to the macro.");

      parameters.get(ENABLE_MULTITHREADING).setDescription("When running a macro which operates on a single slice at a time, multithreading will create a new thread for each slice.  This can provide a speed improvement when working on a computer with a multi-core CPU.  Note: Multithreading is only available for macros containing the \"stack\" argument.");

    }
}
