package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 08/05/2017.
 */
public class ChannelExtractor extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CHANNEL_TO_EXTRACT = "Channel to extract (>= 1)";
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
    public void run(Workspace workspace, boolean verbose) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+inputImageName+") into workspace");
        ImagePlus ipl = workspace.getImages().get(inputImageName).getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int channel = parameters.getValue(CHANNEL_TO_EXTRACT);

        // Getting selected channel
        if (verbose) System.out.println("["+moduleName+"] Extracting channel "+channel);
//        ImagePlus outputChannelImagePlus = SubHyperstackMaker.makeSubhyperstack(ipl,String.valueOf(channel),"1-"+ipl.getNSlices(),"1-"+ipl.getNFrames());
        ipl = new Duplicator().run(ipl);
        ImagePlus outputChannelImagePlus = ChannelSplitter.split(ipl)[channel-1];

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        workspace.addImage(new Image(outputImageName,outputChannelImagePlus));

        // (If selected) displaying the loaded image
        if (parameters.getValue(SHOW_IMAGE)) {
            new Duplicator().run(outputChannelImagePlus).show();

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(CHANNEL_TO_EXTRACT, Parameter.INTEGER,1));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
