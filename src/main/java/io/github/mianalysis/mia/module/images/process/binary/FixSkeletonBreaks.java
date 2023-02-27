package io.github.mianalysis.mia.module.images.process.binary;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.process.skeletontools.BreakFixer;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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

    public FixSkeletonBreaks(Modules modules) {
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
        return Categories.IMAGES_PROCESS_BINARY;
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        int nPx = parameters.getValue(N_PX_FOR_FITTING,workspace);
        double maxDist = parameters.getValue(MAX_LINKING_DISTANCE,workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS,workspace);
        double maxAngle = parameters.getValue(MAX_LINKING_ANGLE,workspace);
        boolean onlyLinkEnds = parameters.getValue(ONLY_LINK_ENDS,workspace);
        double angleWeight = parameters.getValue(ANGLE_WEIGHT,workspace);
        double distanceWeight = parameters.getValue(DISTANCE_WEIGHT,workspace);
        double endWeight = parameters.getValue(END_WEIGHT,workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC,workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // Applying calibration
        if (calibratedUnits)
            maxDist = inputImagePlus.getCalibration().getRawX(maxDist);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImage = ImageFactory.createImage("Temp", inputImagePlus.duplicate());
        }

        // Running skeleton break fixing
        int maxDistPx = (int) Math.round(maxDist);
        fixBreaks(inputImage, nPx, maxDistPx, maxAngle, onlyLinkEnds, angleWeight, distanceWeight, endWeight,
                blackBackground);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = ImageFactory.createImage(outputImageName, inputImage.getImagePlus());
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show();
        } else {
            if (showOutput)
                inputImage.show();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(PROCESSING_SEPARATOR, this));
        parameters.add(new IntegerP(N_PX_FOR_FITTING, this, 5));
        parameters.add(new DoubleP(MAX_LINKING_DISTANCE, this, 10));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new DoubleP(MAX_LINKING_ANGLE, this, 45));
        parameters.add(new BooleanP(ONLY_LINK_ENDS, this, false));
        parameters.add(new DoubleP(ANGLE_WEIGHT, this, 1));
        parameters.add(new DoubleP(DISTANCE_WEIGHT, this, 1));
        parameters.add(new DoubleP(END_WEIGHT, this, 20));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        
        returnedParameters.add(parameters.getParameter(PROCESSING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(N_PX_FOR_FITTING));
        returnedParameters.add(parameters.getParameter(MAX_LINKING_DISTANCE));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(MAX_LINKING_ANGLE));
        returnedParameters.add(parameters.getParameter(ONLY_LINK_ENDS));
        returnedParameters.add(parameters.getParameter(ANGLE_WEIGHT));
        returnedParameters.add(parameters.getParameter(DISTANCE_WEIGHT));

        if (!((boolean) parameters.getValue(ONLY_LINK_ENDS,workspace)))
            returnedParameters.add(parameters.getParameter(END_WEIGHT));

        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
return null;
    }

    @Override
    public String getDescription() {
        return "Fixes breaks (gaps) in binary skeleton images.  This considers each end point of the skeleton and tests it against multiple distance and orientation criteria to see if it can be linked to any other ends (or even midpoints) of the skeleton.  The path between linked ends is added to the binary image as a straight line.  This image will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.";
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.getParameter(INPUT_IMAGE).setDescription(
                "Image to apply break correction to.  This image will be 8-bit with binary logic determined by the \""
                        + BINARY_LOGIC + "\" parameter.");

        parameters.getParameter(APPLY_TO_INPUT).setDescription(
                "Select if the correction should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.getParameter(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during processing.  This image will be added to the workspace.");

        parameters.getParameter(N_PX_FOR_FITTING).setDescription(
                "Number of pixels at the end of a branch to be used for determination of branch orientation.");

        parameters.getParameter(MAX_LINKING_DISTANCE).setDescription(
                "Maximum break distance that can be bridged.  Specified in pixels unless \"Calibrated units\" is enabled.");

        parameters.getParameter(CALIBRATED_UNITS).setDescription(
                "Select whether \"Maximum linking distance\" should be specified in pixel (false) or calibrated (true) units.");

        parameters.getParameter(MAX_LINKING_ANGLE).setDescription(
                "Maximum angular deviation of linking region relative to orientation of existing branch end.  Specified in degrees.");

        parameters.getParameter(ONLY_LINK_ENDS).setDescription(
                "Only remove breaks between pixels at branch ends.  When disabled, an end can link into the middle of another branch.");

        parameters.getParameter(ANGLE_WEIGHT).setDescription(
                "Weight applied to orientation mismatch of ends.  This controls how important orientation mismatches are when considering multiple candidate fixes.  The larger this is, the more likely ends need to be well aligned to be chosen for linking.");

        parameters.getParameter(DISTANCE_WEIGHT).setDescription(
                "Weight applied to distance between candidate ends.  This controls how important minimising the distance between candidate ends is when multiple candidate fixes are available.  The larger than is, the more likely ends will need to be in close proximity to be chosen for linking.");

        parameters.getParameter(END_WEIGHT).setDescription(
                "Weight applied to preference for linking end points. The larger this is, the more likely the points chosen for linking will be ends of the skeleton (rather than mid-points).");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
