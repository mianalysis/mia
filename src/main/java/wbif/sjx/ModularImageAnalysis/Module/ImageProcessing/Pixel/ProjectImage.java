package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.ZProjector;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class ProjectImage extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String PROJECTION_MODE = "Projection mode";
    public static final String SHOW_IMAGE = "Show image";

    public interface ProjectionModes {
        String AVERAGE = "Average";
        String MIN = "Minimum";
        String MEDIAN = "Median";
        String MAX = "Maximum";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[]{AVERAGE, MIN, MEDIAN, MAX, STDEV, SUM};

    }

    public Image projectImageInZ(Image inputImage, String outputImageName, String projectionMode) {
        ZProjector zProjector = new ZProjector(inputImage.getImagePlus());

        switch (projectionMode) {
            case ProjectionModes.AVERAGE:
                zProjector.setMethod(ZProjector.AVG_METHOD);
                break;

            case ProjectionModes.MIN:
                zProjector.setMethod(ZProjector.MIN_METHOD);
                break;

            case ProjectionModes.MEDIAN:
                zProjector.setMethod(ZProjector.MEDIAN_METHOD);
                break;

            case ProjectionModes.MAX:
                zProjector.setMethod(ZProjector.MAX_METHOD);
                break;

            case ProjectionModes.STDEV:
                zProjector.setMethod(ZProjector.SD_METHOD);
                break;

            case ProjectionModes.SUM:
                zProjector.setMethod(ZProjector.SUM_METHOD);
                break;
        }

        zProjector.doProjection();
        ImagePlus iplOut = zProjector.getProjection();

        // Setting spatial calibration
        Calibration calibrationIn = inputImage.getImagePlus().getCalibration();
        Calibration calibrationOut = new Calibration();

        calibrationOut.pixelHeight = calibrationIn.pixelHeight;
        calibrationOut.pixelWidth= calibrationIn.pixelWidth;
        calibrationOut.pixelDepth = calibrationIn.pixelDepth;
        calibrationOut.setUnit(calibrationIn.getUnit());

        iplOut.setCalibration(calibrationOut);

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
    public void run(Workspace workspace, boolean verbose) {
        // Loading image into workspace
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String projectionMode = parameters.getValue(PROJECTION_MODE);

        // Create max projection image
        Image outputImage = projectImageInZ(inputImage,outputImageName,projectionMode);

        // Adding projected image to workspace
        workspace.addImage(outputImage);

        // If selected, displaying the image
        if (parameters.getValue(SHOW_IMAGE)) {
            new Duplicator().run(outputImage.getImagePlus()).show();
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(PROJECTION_MODE,Parameter.CHOICE_ARRAY,ProjectionModes.AVERAGE,ProjectionModes.ALL));
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
