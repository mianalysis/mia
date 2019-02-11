package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Resizer;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class InterpolateZAxis extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static ImagePlus matchZToXY(ImagePlus inputImagePlus) {
        // Calculating scaling
        double distPerPxXY = inputImagePlus.getCalibration().pixelWidth;
        double distPerPxZ = inputImagePlus.getCalibration().pixelDepth;
        int finalNSlices = (int) Math.round(inputImagePlus.getNSlices()*distPerPxZ/distPerPxXY);

        // Checking if interpolation is necessary
        if (finalNSlices == inputImagePlus.getNSlices()) return inputImagePlus;

        Resizer resizer = new Resizer();
        resizer.setAverageWhenDownsizing(true);
        return resizer.zScale(inputImagePlus,finalNSlices,Resizer.IN_PLACE);

    }

    @Override
    public String getTitle() {
        return "Interpolate Z axis";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Interpolates Z-axis of image to match XY spatial calibration";
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        ImagePlus outputImagePlus = matchZToXY(inputImagePlus);

        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
