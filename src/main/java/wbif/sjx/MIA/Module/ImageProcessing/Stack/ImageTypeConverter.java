package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Converter;
import ij.plugin.Duplicator;
import ij.process.StackStatistics;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ImageTypeConverter extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_TYPE = "Output image type";

    public static final String CONVERSION_SEPARATOR = "Image type conversion";
    public static final String SCALING_MODE = "Scaling mode";


    public interface ScalingModes {
        String CLIP = "Clip (direct conversion)";
        String FILL = "Fill target range (normalise)";
        String SCALE = "Scale proportionally";

        String[] ALL = new String[]{CLIP,FILL,SCALE};

    }

    public interface OutputTypes {
        String INT8 = "8-bit integer";
        String INT16 = "16-bit integer";
        String FLOAT32 = "32-bit float";

        String[] ALL = new String[]{INT8, INT16, FLOAT32};

    }


    static int getOutputBitDepth(String outputType) {
        switch (outputType) {
            case OutputTypes.INT8:
                return 8;
            case OutputTypes.INT16:
                return 16;
            case OutputTypes.FLOAT32:
                return 32;
        }

        return 0;

    }

    public static void applyConversion(ImagePlus inputImagePlus, int outputBitDepth, String scalingMode) {
        int bitDepth = inputImagePlus.getBitDepth();

        // If the image is already the output bit depth, skip this
        if (bitDepth == outputBitDepth) return;

        switch (scalingMode) {
            case ScalingModes.CLIP:
                applyClippedRange(inputImagePlus,outputBitDepth);
                break;
            case ScalingModes.FILL:
                applyFilledRange(inputImagePlus);
                break;

            case ScalingModes.SCALE:
                applyScaledRange(inputImagePlus);
                break;
        }

        // Converting to requested type
        switch (outputBitDepth) {
            case 8:
                IJ.run(inputImagePlus,"8-bit",null);
                break;
            case 16:
                IJ.run(inputImagePlus,"16-bit",null);
                break;
            case 32:
                IJ.run(inputImagePlus,"32-bit",null);
                break;
        }
    }

    /**
     * Expand the display range of the image to the max range for the output image type (or the max range for 32-bit)
     * @param imagePlus
     * @param bitDepth
     */
    static void applyClippedRange(ImagePlus imagePlus, int bitDepth) {
        switch (bitDepth) {
            case 8:
                imagePlus.setDisplayRange(0,255);
                break;

            case 16:
                imagePlus.setDisplayRange(0,65535);
                break;

            case 32:
                StackStatistics stackStatistics = new StackStatistics(imagePlus);
                imagePlus.setDisplayRange(stackStatistics.min,stackStatistics.max);
                break;
        }
    }

    /**
     * Expand the display range of the image to the max range for the input image type (or 0-1 for 32-bit).
     * This way the intensities will fill the same proportion of the dynamic range in the new bit depth.
     * @param imagePlus
     */
    static void applyScaledRange(ImagePlus imagePlus) {
        int bitDepth = imagePlus.getBitDepth();

        switch (bitDepth) {
            case 8:
                imagePlus.setDisplayRange(0,255);
                break;

            case 16:
                imagePlus.setDisplayRange(0,65535);
                break;

            case 32:
                imagePlus.setDisplayRange(0,1);
                break;
        }
    }

    /**
     * Expand the display range of the image to the max range for the input image type (or the max range for 32-bit)
     * @param imagePlus
     */
    static void applyFilledRange(ImagePlus imagePlus) {
        StackStatistics stackStatistics = new StackStatistics(imagePlus);
        imagePlus.setDisplayRange(stackStatistics.min,stackStatistics.max);
    }

    @Override
    public String getTitle() {
        return "Image type converter";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputType = parameters.getValue(OUTPUT_TYPE);
        String scalingMode = parameters.getValue(SCALING_MODE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Applying the type conversion
        int outputBitDepth = getOutputBitDepth(outputType);
        applyConversion(inputImagePlus,outputBitDepth,scalingMode);

        // Adding output image to workspace if necessary
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            writeMessage("Adding image ("+outputImageName+") to workspace");
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
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(OUTPUT_TYPE,this,OutputTypes.INT8,OutputTypes.ALL));

        parameters.add(new ParamSeparatorP(CONVERSION_SEPARATOR,this));
        parameters.add(new ChoiceP(SCALING_MODE,this,ScalingModes.CLIP,ScalingModes.ALL));

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

        returnedParameters.add(parameters.getParameter(OUTPUT_TYPE));

        returnedParameters.add(parameters.getParameter(CONVERSION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SCALING_MODE));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
