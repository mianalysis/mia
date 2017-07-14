package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.ZProjector;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ProjectImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    public Image projectImageInZ(Image inputImage, String outputImageName) {
        ZProjector z_projector = new ZProjector(inputImage.getImagePlus());
        z_projector.setMethod(ZProjector.MAX_METHOD);
        z_projector.doProjection();
        ImagePlus iplOut = z_projector.getProjection();

        return new Image(outputImageName,iplOut);

    }

    @Override
    public String getTitle() {
        return "Project image";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading image into workspace
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting output image name
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Create max projection image
        Image outputImage = projectImageInZ(inputImage,outputImageName);

        // Adding projected image to workspace
        workspace.addImage(outputImage);

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            new Duplicator().run(outputImage.getImagePlus()).show();
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

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
