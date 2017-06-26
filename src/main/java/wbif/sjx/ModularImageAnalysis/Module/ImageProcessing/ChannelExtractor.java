package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 08/05/2017.
 */
public class ChannelExtractor extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image1";
    public static final String CHANNEL_TO_EXTRACT = "Channel to extract";
    public static final String SHOW_IMAGE = "Show output image";

    @Override
    public String getTitle() {
        return "Channel extractor";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+inputImageName.getName()+") into workspace");
        ImagePlus ipl = workspace.getImages().get(inputImageName).getImagePlus();

        // Getting parameters
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int channel = parameters.getValue(CHANNEL_TO_EXTRACT);

        // Getting selected channel
        if (verbose) System.out.println("["+moduleName+"] Extracting channel "+channel);
//        ImagePlus outputChannelImagePlus = SubHyperstackMaker.makeSubhyperstack(ipl,String.valueOf(channel),"1-"+ipl.getNSlices(),"1-"+ipl.getNFrames());
        ipl = new Duplicator().run(ipl);
        ImagePlus outputChannelImagePlus = ChannelSplitter.split(ipl)[channel-1];

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName.getName()+") to workspace");
        workspace.addImage(new HCImage(outputImageName,outputChannelImagePlus));

        // (If selected) displaying the loaded image
        if (parameters.getValue(SHOW_IMAGE)) {
            new Duplicator().run(outputChannelImagePlus).show();

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE, HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE, HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(CHANNEL_TO_EXTRACT, HCParameter.INTEGER,1));
        parameters.addParameter(new HCParameter(SHOW_IMAGE, HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
