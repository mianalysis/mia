package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.filters.CombingCorrector;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class CombingCorrection extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CORRECTION_SEPARATOR = "Combing correction";
    public static final String OFFSET = "Offset (px)";

    public CombingCorrection(Modules modules) {
        super("Combing correction",modules);
    }



    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Applies an integer pixel row shift to every other row (starting with top-most row).";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        int offset = parameters.getValue(OFFSET,workspace);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = inputImagePlus.duplicate();}

        // Running the correction
        CombingCorrector.run(inputImagePlus,offset,true);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = ImageFactory.createImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            if (showOutput) inputImage.showImage();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to which the correction will be applied."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Set to \"true\" to apply correction to the input image or \"false\" to store the corrected image separately."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "","Output image with correction applied."));

        parameters.add(new SeparatorP(CORRECTION_SEPARATOR, this));
        parameters.add(new IntegerP(OFFSET, this, 0, "Pixel offset to be applied to every other row."));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
            returnedParameters.add(parameters.getParameter(CORRECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OFFSET));

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
        parameters.get(INPUT_IMAGE).setDescription("Image to which the correction will be applied.");

        parameters.get(APPLY_TO_INPUT).setDescription("When selected, the input image will be updated to contain the corrected image.  Otherwise, the corrected image will be stored separately in the workspace (name controlled by \""+OUTPUT_IMAGE+"\" parameter).");

        parameters.get(OUTPUT_IMAGE).setDescription("Output image with correction applied.");

        parameters.get(OFFSET).setDescription("Pixel offset to be applied to every other row.");

    }
}
