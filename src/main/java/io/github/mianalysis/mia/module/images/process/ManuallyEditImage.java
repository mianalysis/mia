package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.ShowImage;
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
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ManuallyEditImage extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String DISPLAY_SEPARATOR = "Display controls";
    public static final String CHANNEL_MODE = "Channel mode";

    public interface ChannelModes extends ShowImage.ChannelModes {
    }

    public ManuallyEditImage(Modules modules) {
        super("Manually edit image", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getDescription() {
        return "Displays an image from the workspace, allowing the user to make manual edits, before saving it back into the workspace.  Edited images can either be stored as a new image in the workspace or overwrite the input image.  Once the image has been displayed, the user makes edits until clicking \"OK\" in the dialog box that appears.  As such, this module is only suitable for running in GUI mode.";
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
        String channelMode = parameters.getValue(CHANNEL_MODE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // Displaying image to edit
        switch (channelMode) {
            case ChannelModes.COLOUR:
                inputImagePlus.setDisplayMode(CompositeImage.COLOR);
                break;
            case ChannelModes.COMPOSITE:
                inputImagePlus.setDisplayMode(CompositeImage.COMPOSITE);
                break;
        }
        inputImagePlus.show();

        // When the edits have been made, the user needs to click on the following
        // button to proceed
        IJ.runMacro("waitForUser(getArgument())", "Click \"OK\" once edits complete");

        inputImagePlus.hide();

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
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DISPLAY_SEPARATOR, this));
        parameters.add(new ChoiceP(CHANNEL_MODE, this, ChannelModes.COMPOSITE, ChannelModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(DISPLAY_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace to manually edit.  When this module executes the image will be displayed along with a dialog box allowing the user to identify when editing is complete.  Depending on the \""
                        + APPLY_TO_INPUT
                        + "\" parameter, the edits will either be applied directly to this input image or stored in a separate image in the workspace.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the edited image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the edited image will be saved to the workspace with this name.");

        parameters.get(CHANNEL_MODE).setDescription(
                "Select whether multi-channel images should be displayed as composites (show all channels overlaid) or individually (the displayed channel is controlled by the \"C\" slider at the bottom of the image window).");

    }
}
