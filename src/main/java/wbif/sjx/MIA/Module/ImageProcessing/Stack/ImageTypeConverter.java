package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import java.util.HashMap;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.LUT;
import ij.process.StackStatistics;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

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

    public ImageTypeConverter(ModuleCollection modules) {
        super("Image type converter",modules);
    }


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

    static HashMap<Integer, LUT> getLUTs(Image image) {
        ImagePlus ipl = image.getImagePlus();
        HashMap<Integer,LUT> luts = new HashMap<>();

        if (!ipl.isComposite()) return luts;

        for (int c=0;c<ipl.getNChannels();c++) {
            luts.put(c,((CompositeImage) ipl).getChannelLut(c+1));
        }

        return luts;

    }

    static void setLUTs(Image image, HashMap<Integer,LUT> luts) {
        ImagePlus ipl = image.getImagePlus();

        if (!ipl.isComposite()) return;

        for (int c:luts.keySet()) {
            ((CompositeImage) ipl).setChannelLut(luts.get(c),c+1);
        }
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
        return "Change the bit-depth of an image stack.  This module provides multiple ways to handle the intensity transformation from one bit-depth to another.<br>" +
                "<br>Note: Different scaling modes currently only apply when reducing the bit-depth of an image.  As such, converting from 8-bit to 16-bit will always result in direct conversion of intensities.";
    }

    @Override
    public Status process(Workspace workspace) {
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

        // Getting LUTs by channel index
        HashMap<Integer,LUT> luts = getLUTs(inputImage);

        // Applying the type conversion
        int outputBitDepth = getOutputBitDepth(outputType);
        applyConversion(inputImagePlus,outputBitDepth,scalingMode);

        // Adding output image to workspace if necessary
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            writeStatus("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            setLUTs(outputImage,luts);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            setLUTs(inputImage,luts);
            if (showOutput) inputImage.showImage();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Input image to be converted to another bit-depth."));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true,"If selected, the converted image will replace the input image in the workspace.  All measurements associated with the input image will be transferred to the converted image."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,"","Name of the output converted image."));
        parameters.add(new ChoiceP(OUTPUT_TYPE,this,OutputTypes.INT8,OutputTypes.ALL,"Target bit-depth to convert the image to.  Pixel intensities will lie within the following ranges for each bit-depth: 8-bit (0-255), 16-bit (0-65535), 32-bit (floating point precision)."));

        parameters.add(new SeparatorP(CONVERSION_SEPARATOR,this));
        parameters.add(new ChoiceP(SCALING_MODE,this,ScalingModes.CLIP,ScalingModes.ALL,"Method for calculating the intensity transformation between the input and output bit-depths<br>" +
                "<br> - \""+ScalingModes.CLIP+"\" will convert directly from the input to output bit-depth without performing any intensity scaling.  As such, any input intensities outside the available range of the output bit-depth will be clipped to the closest possible value.  For example, an input 16-bit image with intensity range 234-34563 converted to 8-bit will have an output range of 234-255.<br>" +
                "<br> - \""+ScalingModes.FILL+"\" will stretch the input intensity range to fill the available output intensity range.  For example, an input 16-bit image with intensity range 234-34563 converted to 8-bit will have an output range of 0-255.  Images converted to 32-bit will be scaled to the range 0-1.<br>" +
                "<br> - \""+ScalingModes.SCALE+"\" will proportionately scale intensities such that the input and output intensity ranges fill their respective bit-depths by equal amounts.  For example, an input 16-bit image with intensity range 234-34563 converted to 8-bit will have an output range of 1-135."));
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
}
