package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Resizer;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

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
    protected void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        ImagePlus outputImagePlus = matchZToXY(inputImagePlus);

        Image outputImage = new Image(outputImageName,outputImagePlus);
        workspace.addImage(outputImage);

        if (showOutput) showImage(outputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
