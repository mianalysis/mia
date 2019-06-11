package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Process.SkeletonTools.BreakFixer;

public class FixSkeletonBreaks extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String PROCESSING_SEPARATOR = "Processing options";
    public static final String N_PX_FOR_FITTING = "Number of end pixels to fit";
    public static final String MAX_LINKING_DISTANCE = "Maximum linking distance";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MAX_LINKING_ANGLE = "Maximum linking angle (degrees)";
    public static final String ONLY_LINK_ENDS = "Only link ends";


    public FixSkeletonBreaks(ModuleCollection modules) {
        super("Fix skeleton breaks", modules);
    }

    public static void fixBreaks(Image inputImage, int nPx, int maxDist, double maxAngle, boolean onlyLinkEnds) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);

                    BreakFixer.process(inputImagePlus.getProcessor(),nPx,maxDist,maxAngle,onlyLinkEnds);

                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int nPx = parameters.getValue(N_PX_FOR_FITTING);
        double maxDist = parameters.getValue(MAX_LINKING_DISTANCE);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        double maxAngle = parameters.getValue(MAX_LINKING_ANGLE);
        boolean onlyLinkEnds = parameters.getValue(ONLY_LINK_ENDS);

        // Applying calibration
        if (calibratedUnits) maxDist = inputImagePlus.getCalibration().getRawX(maxDist);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = inputImagePlus.duplicate();}

        // Running skeleton break fixing
        int maxDistPx = (int) Math.round(maxDist);
        fixBreaks(inputImage,nPx,maxDistPx,maxAngle,onlyLinkEnds);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();
        } else {
            if (showOutput) inputImage.showImage();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply break correction to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Select if the correction should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output image created during processing.  This image will be added to the workspace."));
        parameters.add(new ParamSeparatorP(PROCESSING_SEPARATOR,this));
        parameters.add(new IntegerP(N_PX_FOR_FITTING,this,5,"Number of pixels at the end of a branch to be used for determination of branch orientation."));
        parameters.add(new DoubleP(MAX_LINKING_DISTANCE,this,10, "Maximum break distance that can be bridged.  Specified in pixels unless \"Calibrated units\" is enabled."));
        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false, "Select whether \"Maximum linking distance\" should be specified in pixel (false) or calibrated (true) units."));
        parameters.add(new DoubleP(MAX_LINKING_ANGLE,this,45, "Maximum angular deviation of linking region relative to orientation of existing branch end.  Specified in degrees."));
        parameters.add(new BooleanP(ONLY_LINK_ENDS,this,false,"Only remove breaks between pixels at branch ends.  When disabled, an end can link into the middle of another branch."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(N_PX_FOR_FITTING));
        returnedParameters.add(parameters.getParameter(MAX_LINKING_DISTANCE));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(MAX_LINKING_ANGLE));
        returnedParameters.add(parameters.getParameter(ONLY_LINK_ENDS));

        return returnedParameters;

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
