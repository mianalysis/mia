package io.github.mianalysis.mia.module.script;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 31/01/2018.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class RunSingleCommand extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String COMMAND_SEPARATOR = "Command controls";
    public static final String COMMAND = "Command";
    public static final String ARGUMENTS = "Parameters";
    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public RunSingleCommand(Modules modules) {
        super("Run single command", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.SCRIPT;
    }

    @Override
    public String getDescription() {
        return "Run a single command on an image from the workspace.   This module only runs commands of the format \"run([COMMAND], [ARGUMENTS])\".  For example, the command \"run(\"Subtract Background...\", \"rolling=50 stack\");\" would be specified with the \""+COMMAND+"\" parameter set to \"Subtract Background...\" and the \""+ARGUMENTS+"\" parameter set to \"rolling=50 stack\".  For more advanced macro processing please use the \""+new RunMacro(null).getName()+"\" module.";
    }

    public void runCommandMultithreaded(ImagePlus inputImagePlus, String commandTitle, String arguments) {
        // Setting up multithreading
        int nThreads = Prefs.getThreads();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Applying the command
        ImageStack ist = inputImagePlus.getStack();
        for (int i = 0; i < ist.size(); i++) {
            ImageProcessor ipr = ist.getProcessor(i + 1);
            ImagePlus ipl = new ImagePlus("Temp", ipr);
            Runnable task = () -> {
                IJ.run(ipl, commandTitle, arguments);
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        inputImagePlus.updateChannelAndDraw();
        
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String commandTitle = parameters.getValue(COMMAND,workspace);
        String arguments = parameters.getValue(ARGUMENTS,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

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
            runCommandMultithreaded(inputImagePlus, commandTitle, arguments);
        } else {
            IJ.run(inputImagePlus, commandTitle, arguments);
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
        parameters.add(new SeparatorP(COMMAND_SEPARATOR, this));
        parameters.add(new StringP(COMMAND, this));
        parameters.add(new StringP(ARGUMENTS, this));
        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(COMMAND_SEPARATOR));
        returnedParameters.add(parameters.getParameter(COMMAND));
        returnedParameters.add(parameters.getParameter(ARGUMENTS));

            returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
            returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));
        

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply command to.  This image is duplicated prior to application of the command, so won't be updated by default.  To store any changes back onto this image, select the \""+APPLY_TO_INPUT+"\" parameter.");

      parameters.get(APPLY_TO_INPUT).setDescription("When selected, the image returned by the command will be stored back into the MIA workspace at the same name as the input image.  This will update the input image.");

      parameters.get(OUTPUT_IMAGE).setDescription("When \""+APPLY_TO_INPUT+"\" is not selected this will store the command output image into the MIA workspace with the name specified by this parameter.");

      parameters.get(COMMAND).setDescription("The command command to run.  This must be the exact name as given by the ImageJ command recorder.  Note: Only commands of the format \"run([MACRO TITLE], [ARGUMENTS])\" can be run by this module.  For more advanced command processing please use the \""+new RunMacro(null).getName()+"\" module.");

      parameters.get(ARGUMENTS).setDescription("The options to pass to the command.");

      parameters.get(ENABLE_MULTITHREADING).setDescription("When running a command which operates on a single slice at a time, multithreading will create a new thread for each slice.  This can provide a speed improvement when working on a computer with a multi-core CPU.  Note: Multithreading is only available for commands containing the \"stack\" argument.");

    }
}
