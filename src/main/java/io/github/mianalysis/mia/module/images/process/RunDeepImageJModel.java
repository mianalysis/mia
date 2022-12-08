package io.github.mianalysis.mia.module.images.process;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import deepimagej.DeepImageJ;
import deepimagej.DeepLearningModel;
import deepimagej.RunnerProgress;
import deepimagej.tools.ArrayOperations;
import deepimagej.tools.DijRunnerPreprocessing;
import deepimagej.tools.DijTensor;
import deepimagej.tools.ModelLoader;
import deepimagej.tools.StartTensorflowService;
import deepimagej.tools.SystemUsage;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.ShowImage;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.deepimagej.PrepareDeepImageJ;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunDeepImageJModel extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String MODEL_SEPARATOR = "Model controls";
    public static final String MODEL = "Model";
    public static final String FORMAT = "Format";

    public interface Models {
        String[] ALL = PrepareDeepImageJ.getAvailableModels();

    }

    public interface FormatsBoth {
        String PYTORCH = "Pytorch";
        String TENSORFLOW = "Tensorflow";

        String[] ALL = new String[]{PYTORCH, TENSORFLOW};

    }

    public RunDeepImageJModel(Modules modules) {
        super("Run DeepImageJ model", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String modelName = parameters.getValue(MODEL, workspace);

        // Get input image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Running deep learning model
        DeepImageJ model = PrepareDeepImageJ.getModel(modelName);
        PrepareDeepImageJ pDIJ = new PrepareDeepImageJ();
        ImagePlus outputIpl = pDIJ.runModel(inputIpl, model, "Tensorflow", "no preprocessing", "no postprocessing", "X,Y,C", "400,400,1");

        // Storing output image
        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        System.err.println("NEED TO CHANGE PATH TO MODEL FOLDER");
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new ChoiceP(MODEL, this, "", Models.ALL));
        parameters.add(new ChoiceP(FORMAT,this, "", FormatsBoth.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MODEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MODEL));

        String modelName = parameters.getValue(MODEL, workspace);
        ((ChoiceP) parameters.get(FORMAT)).setChoices(PrepareDeepImageJ.getFormats(modelName));
        returnedParameters.add(parameters.getParameter(FORMAT));

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

    @Override
    public String getDescription() {
        return "";
    }
}
