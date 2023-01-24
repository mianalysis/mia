package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import deepimagej.DeepImageJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.deepimagej.PrepareDeepImageJ;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RunDeepImageJModel extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String AXES_ORDER = "Axes order";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String MODEL_SEPARATOR = "Model controls";
    public static final String MODEL = "Model";
    public static final String FORMAT = "Format";
    // public static final String PREPROCESSING = "Preprocessing";
    public static final String USE_POSTPROCESSING = "Use postprocessing";
    // public static final String POSTPROCESSING = "Postprocessing";
    // public static final String PATCH_SIZE = "Patch size";

    public interface Models {
        String[] ALL = PrepareDeepImageJ.getAvailableModels();

    }

    public interface FormatsBoth {
        String PYTORCH = "Pytorch";
        String TENSORFLOW = "Tensorflow";

        String[] ALL = new String[] { PYTORCH, TENSORFLOW };

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
        String format = parameters.getValue(FORMAT, workspace);
        // String preprocessing = parameters.getValue(PREPROCESSING, workspace);
        boolean usePostprocessing = parameters.getValue(USE_POSTPROCESSING, workspace);
        // String postprocessing = parameters.getValue(POSTPROCESSING, workspace);
        // String patchSize = parameters.getValue(PATCH_SIZE, workspace);

        // Get input image
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputIpl = inputImage.getImagePlus();

        // Running deep learning model
        DeepImageJ model = PrepareDeepImageJ.getModel(modelName);

        // Updating pre and post processing options
        boolean usePreprocessing = true;        
        if (PrepareDeepImageJ.getPreprocessings(modelName).length == 0)
            usePostprocessing = false;
        if (PrepareDeepImageJ.getPostprocessings(modelName).length == 0)
            usePostprocessing = false;

        PrepareDeepImageJ pDIJ = new PrepareDeepImageJ();
        String patchSize = PrepareDeepImageJ.getOptimalPatch(modelName);
        ImagePlus outputIpl = pDIJ.runModel(inputIpl, model, format, usePreprocessing, usePostprocessing, patchSize);

        // Storing output image
        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        Preferences preferences = MIA.getPreferences();
        boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new MessageP(AXES_ORDER, this, Colours.getBlue(darkMode), 20));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(MODEL_SEPARATOR, this));
        parameters.add(new ChoiceP(MODEL, this, "", Models.ALL));
        parameters.add(new ChoiceP(FORMAT, this, "", new String[0]));
        // parameters.add(new ChoiceP(PREPROCESSING, this, "", new String[0]));
        parameters.add(new BooleanP(USE_POSTPROCESSING, this, false));
        // parameters.add(new ChoiceP(POSTPROCESSING, this, "", new String[0]));
        // parameters.add(new StringP(PATCH_SIZE, this, ""));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        String modelName = parameters.getValue(MODEL, workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(AXES_ORDER));
        ((MessageP) parameters.get(AXES_ORDER))
                .setValue("Selected model requires input image axes to be in the order: "
                        + PrepareDeepImageJ.getAxes(modelName));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MODEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MODEL));

        ((ChoiceP) parameters.get(FORMAT)).setChoices(PrepareDeepImageJ.getFormats(modelName));
        returnedParameters.add(parameters.getParameter(FORMAT));

        // String[] preprocessingChoices = PrepareDeepImageJ.getPreprocessings(modelName);
        // if (preprocessingChoices.length > 0) {
        //     ((ChoiceP) parameters.get(PREPROCESSING)).setChoices(preprocessingChoices);
        //     returnedParameters.add(parameters.getParameter(PREPROCESSING));
        // }

        String[] postprocessingChoices = PrepareDeepImageJ.getPostprocessings(modelName);
        if (postprocessingChoices.length > 0) {
            returnedParameters.add(parameters.getParameter(USE_POSTPROCESSING));
            // if ((boolean) parameters.getValue(USE_POSTPROCESSING, workspace)) {
            //     ((ChoiceP) parameters.get(POSTPROCESSING)).setChoices(postprocessingChoices);
            //     returnedParameters.add(parameters.getParameter(POSTPROCESSING));
            // }
        }

        // returnedParameters.add(parameters.getParameter(PATCH_SIZE));
        // ((StringP)
        // parameters.get(PATCH_SIZE)).setValue(PrepareDeepImageJ.getOptimalPatch(modelName));

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
