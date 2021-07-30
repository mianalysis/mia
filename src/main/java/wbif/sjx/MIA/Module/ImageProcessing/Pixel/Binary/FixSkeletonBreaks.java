package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
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
    public static final String ANGLE_WEIGHT = "Angle weight";
    public static final String DISTANCE_WEIGHT = "Distance weight";
    public static final String END_WEIGHT = "End weight";
    public static final String BINARY_LOGIC = "Binary logic";

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public FixSkeletonBreaks(ModuleCollection modules) {
        super("Fix skeleton breaks", modules);
    }

    public static void fixBreaks(Image inputImage, int nPx, int maxDist, double maxAngle, boolean onlyLinkEnds,
            double angleWeight, double distanceWeight, double endWeight, boolean blackBackground) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        if (blackBackground)
            InvertIntensity.process(inputImagePlus);
        
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);

                    BreakFixer.process(inputImagePlus.getProcessor(), nPx, maxDist, maxAngle, onlyLinkEnds, angleWeight,
                            distanceWeight, endWeight);

                }
            }
        }

        if (blackBackground)
            InvertIntensity.process(inputImagePlus);

        inputImagePlus.setPosition(1, 1, 1);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    protected Status process(Workspace workspace) {
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
        double angleWeight = parameters.getValue(ANGLE_WEIGHT);
        double distanceWeight = parameters.getValue(DISTANCE_WEIGHT);
        double endWeight = parameters.getValue(END_WEIGHT);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // Applying calibration
        if (calibratedUnits)
            maxDist = inputImagePlus.getCalibration().getRawX(maxDist);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImage = new Image("Temp", inputImagePlus.duplicate());
        }

        // Running skeleton break fixing
        int maxDistPx = (int) Math.round(maxDist);
        fixBreaks(inputImage, nPx, maxDistPx, maxAngle, onlyLinkEnds, angleWeight, distanceWeight, endWeight, blackBackground);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = new Image(outputImageName, inputImage.getImagePlus());
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();
        } else {
            if (showOutput)
                inputImage.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply break correction to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true,
                "Select if the correction should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "",
                "Name of the output image created during processing.  This image will be added to the workspace."));
        parameters.add(new SeparatorP(PROCESSING_SEPARATOR, this));
        parameters.add(new IntegerP(N_PX_FOR_FITTING, this, 5,
                "Number of pixels at the end of a branch to be used for determination of branch orientation."));
        parameters.add(new DoubleP(MAX_LINKING_DISTANCE, this, 10,
                "Maximum break distance that can be bridged.  Specified in pixels unless \"Calibrated units\" is enabled."));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false,
                "Select whether \"Maximum linking distance\" should be specified in pixel (false) or calibrated (true) units."));
        parameters.add(new DoubleP(MAX_LINKING_ANGLE, this, 45,
                "Maximum angular deviation of linking region relative to orientation of existing branch end.  Specified in degrees."));
        parameters.add(new BooleanP(ONLY_LINK_ENDS, this, false,
                "Only remove breaks between pixels at branch ends.  When disabled, an end can link into the middle of another branch."));
        parameters.add(new DoubleP(ANGLE_WEIGHT, this, 1,
                "Weight applied to orientation mismatch of ends.  This controls how important orientation mismatches are when considering multiple candidate fixes.  The larger this is, the more likely ends need to be well aligned to be chosen for linking."));
        parameters.add(new DoubleP(DISTANCE_WEIGHT, this, 1,
                "Weight applied to distance between candidate ends.  This controls how important minimising the distance between candidate ends is when multiple candidate fixes are available.  The larger than is, the more likely ends will need to be in close proximity to be chosen for linking."));
        parameters.add(new DoubleP(END_WEIGHT, this, 20,
                "Weight applied to preference for linking end points. The larger this is, the more likely the points chosen for linking will be ends of the skeleton (rather than mid-points)."));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

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
        returnedParameters.add(parameters.getParameter(ANGLE_WEIGHT));
        returnedParameters.add(parameters.getParameter(DISTANCE_WEIGHT));

        if (!((boolean) parameters.getValue(ONLY_LINK_ENDS)))
            returnedParameters.add(parameters.getParameter(END_WEIGHT));
        
            returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Fixes breaks (gaps) in binary skeleton images.  This considers each end point of the skeleton and tests it against multiple distance and orientation criteria to see if it can be linked to any other ends (or even midpoints) of the skeleton.  The path between linked ends is added to the binary image as a straight line.  The input image must be 8-bit and have the logic black foreground (intensity 0) and white background (intensity 255)";
    }

    @Override
    public boolean verify() {
        return true;
    }
}
