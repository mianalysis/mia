package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.stacks.Hyperstack_rearranger;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
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
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 19/06/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class Convert3DStack extends Module {
    public static final String IMAGE_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CONVERSION_SEPARTOR = "Stack conversion";
    public static final String MODE = "Mode";

    public interface Modes {
        String OUTPUT_TIMESERIES = "Output timeseries";
        String OUTPUT_Z_STACK = "Output Z-stack";

        String[] ALL = new String[] { OUTPUT_TIMESERIES, OUTPUT_Z_STACK };

    }

    public Convert3DStack(Modules modules) {
        super("Convert 3D stack (switch Z and T)", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }
    
    @Override
    public String getDescription() {
        return "Emsures 3D stacks (or 4D with multiple channels) are of the expected type (timeseries or Z-stack).  This module verifies the singular dimension of a 3D stack is correct for the specified output type (e.g. single slice when dealing with timeseries).  Any stacks which are not in the expected order have their T and Z axes swapped.";
    }

    public static void process(ImagePlus inputImagePlus, String mode) {
        int nChannels = inputImagePlus.getNChannels();
        int nFrames = inputImagePlus.getNFrames();
        int nSlices = inputImagePlus.getNSlices();

        if (nSlices == 1 && nFrames == 1)
            return;
            
        switch (mode) {
            case Modes.OUTPUT_TIMESERIES:
                if (inputImagePlus.getNSlices() == 1 && inputImagePlus.getNFrames() > 1)
                    return;
                break;
            case Modes.OUTPUT_Z_STACK:
                if (inputImagePlus.getNFrames() == 1 && inputImagePlus.getNSlices() > 1)
                    return;
                break;
        }

        ImagePlus processedImagePlus = HyperStackConverter.toHyperStack(inputImagePlus, nChannels, nFrames, nSlices);
        processedImagePlus = Hyperstack_rearranger.reorderHyperstack(processedImagePlus, "CTZ", true, false);
        inputImagePlus.setStack(processedImagePlus.getStack());

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
        String mode = parameters.getValue(MODE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        process(inputImagePlus, mode);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
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
        parameters.add(new SeparatorP(IMAGE_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CONVERSION_SEPARTOR, this));
        parameters.add(new ChoiceP(MODE, this, Modes.OUTPUT_TIMESERIES, Modes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(IMAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CONVERSION_SEPARTOR));
        returnedParameters.add(parameters.getParameter(MODE));

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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to test T and Z inversion of.");

        parameters.get(APPLY_TO_INPUT).setDescription("When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
        + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(MODE).setDescription(
                "Controls the expected stack output type.  Any input stacks which do not match this format are updated to give the expected order.  Choices are: "
                        + String.join(", ", Modes.ALL) + ".");

    }
}
