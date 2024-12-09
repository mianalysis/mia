package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen on 08/05/2017.
 */

/**
* DEPRECATED: Please use ExtractSubstack module.<br><br> Extracts a single channel from a stack and stores it as a new image in the workspace.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ChannelExtractor extends Module {

	/**
	* Multi-channel image to extract a channel from.  This image is unaffected by the extraction process (i.e. its channel count doesn't decrease by 1).
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* The extracted channel will be stored in the workspace under this name.
	*/
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CHANNEL_TO_EXTRACT = "Channel to extract (>= 1)";

    public ChannelExtractor(Modules modules) {
        super("Channel extractor",modules);
        deprecated = true;
    }


    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: Please use ExtractSubstack module.<br><br> "
        + "Extracts a single channel from a stack and stores it as a new image in the workspace.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        writeStatus("Loading image ("+inputImageName+") into workspace");
        ImagePlus ipl = workspace.getImages().get(inputImageName).getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        int channel = parameters.getValue(CHANNEL_TO_EXTRACT,workspace);

        // Getting selected channel
        writeStatus("Extracting channel "+channel);
        ipl = new Duplicator().run(ipl);
        ImagePlus outputChannelImagePlus = ChannelSplitter.split(ipl)[channel-1];

        // Adding image to workspace
        writeStatus("Adding image ("+outputImageName+") to workspace");
        Image outputImage = ImageFactory.createImage(outputImageName,outputChannelImagePlus);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new IntegerP(CHANNEL_TO_EXTRACT,this,1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
