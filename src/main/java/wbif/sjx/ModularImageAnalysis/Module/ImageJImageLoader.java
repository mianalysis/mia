package wbif.sjx.ModularImageAnalysis.Module;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by Stephen on 10/05/2017.
 */
public class ImageJImageLoader extends HCModule {
    public static final String OUTPUT_IMAGE = "Output image";

    @Override
    public String getTitle() {
        return "Load image from ImageJ";

    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting image
        HCName outputImageName = parameters.getValue(OUTPUT_IMAGE);
        ImagePlus imagePlus = IJ.getImage();

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        HCImage outputImage = new HCImage(outputImageName, imagePlus);
        workspace.addImage(outputImage);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE, HCParameter.OUTPUT_IMAGE,null));

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
