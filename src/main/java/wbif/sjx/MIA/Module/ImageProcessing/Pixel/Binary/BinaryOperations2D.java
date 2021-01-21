// TODO: What happens when 3D distance map is generateModuleList on 4D or 5D image hyperstack?

package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class BinaryOperations2D extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OPERATION_SEPARATOR = "Operation controls";
    public static final String OPERATION_MODE = "Filter mode";
    public static final String NUM_ITERATIONS = "Number of iterations";
    public static final String COUNT = "Count";

    public BinaryOperations2D(ModuleCollection modules) {
        super("Binary operations 2D", modules);
    }

    public interface OperationModes {
        String DILATE = "Dilate";
        String DISTANCE_MAP = "Distance map";
        String ERODE = "Erode";
        String FILL_HOLES = "Fill holes";
        String OUTLINE = "Outline";
        String SKELETONISE = "Skeletonise";
        String ULTIMATE_POINTS = "Ultimate points";
        String VORONOI = "Voronoi";
        String WATERSHED = "Watershed";

        String[] ALL = new String[] { DILATE, DISTANCE_MAP, ERODE, FILL_HOLES, OUTLINE, SKELETONISE, ULTIMATE_POINTS,
                VORONOI, WATERSHED };

    }

    public static void process(ImagePlus ipl, String operationMode, int numIterations, int count) {
        process(new Image("Image", ipl), operationMode, numIterations, count);
    }

    public static void process(Image image, String operationMode, int numIterations, int count) {
        ImagePlus ipl = image.getImagePlus();

        // Applying processAutomatic to stack
        switch (operationMode) {
            case OperationModes.DILATE:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Dilate stack");
                break;

            case OperationModes.ERODE:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Erode stack");
                break;

            case OperationModes.FILL_HOLES:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=[Fill Holes] stack");
                break;

            case OperationModes.OUTLINE:
                IJ.run(ipl, "Outline", "stack");
                break;

            case OperationModes.SKELETONISE:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Skeletonize stack");
                break;

            case OperationModes.VORONOI:
                IJ.run(ipl, "Voronoi", "stack");
                break;

            case OperationModes.ULTIMATE_POINTS:
                IJ.run(ipl, "Ultimate Points", "stack");
                break;

            case OperationModes.WATERSHED:
                IJ.run(ipl, "Watershed", "stack");
                break;

        }
    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Applies stock ImageJ binary operations to an image in the workspace.  This image must be 8-bit and have the logic black foreground (intensity 0) and white background (intensity 255).  All operations are performed in 2D, with higher dimensionality stacks being processed slice-by-slice.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String operationMode = parameters.getValue(OPERATION_MODE);
        int numIterations = parameters.getValue(NUM_ITERATIONS);
        int count = parameters.getValue(COUNT);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        if (operationMode.equals(OperationModes.DISTANCE_MAP)) {
            IJ.run(inputImagePlus, "Distance Map", "stack");
        } else {
            process(inputImagePlus, operationMode, numIterations, count);
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            Image outputImage = new Image(outputImageName, inputImagePlus);
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
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(OPERATION_SEPARATOR, this));
        parameters.add(new ChoiceP(OPERATION_MODE, this, OperationModes.DILATE, OperationModes.ALL));
        parameters.add(new IntegerP(NUM_ITERATIONS, this, 1));
        parameters.add(new IntegerP(COUNT, this, 1));

        addParameterDescriptions();

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

        returnedParameters.add(parameters.getParameter(OPERATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OPERATION_MODE));
        switch ((String) parameters.getValue(OPERATION_MODE)) {
            case OperationModes.DILATE:
            case OperationModes.ERODE:
            case OperationModes.FILL_HOLES:
            case OperationModes.SKELETONISE:
            case OperationModes.WATERSHED:
                returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
                returnedParameters.add(parameters.getParameter(COUNT));
                break;
        }

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace to apply binary operation to.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(OPERATION_MODE).setDescription(
                "Controls which binary operation will be applied.  All operations assume the default ImageJ logic of black objects on a white background.  The operations are described in full at [WEBSITE]:<br><ul>"

                        + "<li>\"" + OperationModes.DILATE
                        + "\" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.</li>"

                        + "<li>\"" + OperationModes.DISTANCE_MAP
                        + "\" Create a 32-bit greyscale image where the value of each foreground pixel is equal to its Euclidean distance to the nearest background pixel.</li>"

                        + "<li>\"" + OperationModes.ERODE
                        + "\" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.</li>"

                        + "<li>\"" + OperationModes.FILL_HOLES
                        + "\" Change all background pixels in a region which is fully enclosed by foreground pixels to foreground.</li>"

                        + "<li>\"" + OperationModes.OUTLINE
                        + "\" Convert all non-background-connected foreground pixels to background.  This effectively creates a fully-background image, except for the outer band of foreground pixels.</li>"

                        + "<li>\"" + OperationModes.SKELETONISE
                        + "\" Repeatedly applies the erode process until each foreground region is a single pixel wide.</li>"

                        + "<li>\"" + OperationModes.ULTIMATE_POINTS
                        + "\" Repeatedly applies the erode process until each foreground is reduced to a single pixel.  The value of the remaining, isolated foreground pixels are equal to their equivalent, pre-erosion distance map values.  This process outputs a 32-bit greyscale image.</li>"

                        + "<li>\"" + OperationModes.VORONOI
                        + "\" Creates an image subdivided by lines such that all pixels contained within an enclosed region are closest to the same contiguous object in the input binary image.</li>"

                        + "<li>\"" + OperationModes.WATERSHED
                        + "\" Peforms a distance-based watershed transform on the image.  This process is able to split separate regions of a single connected foreground region as long as the sub-regions are connected by narrow necks (e.g. snowman shape).  Background lines are drawn between each sub-region such that they are no longer connected.</li></ul>");

        parameters.get(NUM_ITERATIONS).setDescription(
                "Number of times the operation will be run on a single image.  For example, this allows objects to be eroded further than one pixel in a single step.");

        parameters.get(COUNT).setDescription(
                "The minimum number of connected background or foreground for an erosion or dilation process to occur, respectively.");

    }
}
