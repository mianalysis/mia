package wbif.sjx.ModularImageAnalysis.Module.InputOutput;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
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
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting image
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        ImagePlus imagePlus = IJ.getImage();

        // Adding image to workspace
        if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
        Image outputImage = new Image(outputImageName, imagePlus);
        workspace.addImage(outputImage);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
