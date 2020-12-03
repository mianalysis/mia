package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import ij.plugin.Resizer;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 23/03/2018.
 */
public class InterpolateZAxis extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public InterpolateZAxis(ModuleCollection modules) {
        super("Interpolate Z axis",modules);
    }

    public static ImagePlus matchZToXY(ImagePlus inputImagePlus) {
        // Calculating scaling
        int nSlices = inputImagePlus.getNSlices();
        double distPerPxXY = inputImagePlus.getCalibration().pixelWidth;
        double distPerPxZ = inputImagePlus.getCalibration().pixelDepth;
        int finalNSlices = (int) Math.round(nSlices*distPerPxZ/distPerPxXY);

        // Checking if interpolation is necessary
        if (finalNSlices == nSlices) return inputImagePlus;

        Resizer resizer = new Resizer();
        resizer.setAverageWhenDownsizing(true);

        ImagePlus resized = resizer.zScale(inputImagePlus,finalNSlices,Resizer.IN_PLACE);
        resized.setDimensions(inputImagePlus.getNChannels(),finalNSlices,inputImagePlus.getNFrames());

        return resized;

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Interpolates Z-axis of image to match XY spatial calibration";
    }

    @Override
    public Status process(Workspace workspace) {
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

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        
        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Input image to which the Z-axis interpolation will be applied.");

        parameters.get(OUTPUT_IMAGE).setDescription("Output image with Z-axis interpolation applied.  This image will be stored in the workspace and be accessible using this name.");

    }
}
