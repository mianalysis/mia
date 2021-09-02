package io.github.mianalysis.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.OutputImageP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

/**
 * Created by Stephen on 08/05/2017.
 */
public class ChannelExtractor extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CHANNEL_TO_EXTRACT = "Channel to extract (>= 1)";

    public ChannelExtractor(Modules modules) {
        super("Channel extractor",modules);
        deprecated = true;
    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: Please use ExtractSubstack module.<br><br> "
        + "Extracts a single channel from a stack and stores it as a new image in the workspace.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        writeStatus("Loading image ("+inputImageName+") into workspace");
        ImagePlus ipl = workspace.getImages().get(inputImageName).getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int channel = parameters.getValue(CHANNEL_TO_EXTRACT);

        // Getting selected channel
        writeStatus("Extracting channel "+channel);
        ipl = new Duplicator().run(ipl);
        ImagePlus outputChannelImagePlus = ChannelSplitter.split(ipl)[channel-1];

        // Adding image to workspace
        writeStatus("Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName,outputChannelImagePlus);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new IntegerP(CHANNEL_TO_EXTRACT,this,1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
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
        parameters.get(INPUT_IMAGE).setDescription("Multi-channel image to extract a channel from.  This image is unaffected by the extraction process (i.e. its channel count doesn't decrease by 1).");

        parameters.get(OUTPUT_IMAGE).setDescription("The extracted channel will be stored in the workspace under this name.");

        parameters.get(CHANNEL_TO_EXTRACT).setDescription("Channel index to extract.  Index numbering starts at 1 and must be specified as a single integer value.");

    }
}
