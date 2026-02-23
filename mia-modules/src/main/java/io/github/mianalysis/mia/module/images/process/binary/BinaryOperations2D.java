package io.github.mianalysis.mia.module.images.process.binary;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
* Applies stock ImageJ binary operations to an image in the workspace.  This image will be 8-bit with binary logic determined by the "Binary logic" parameter.  All operations are performed in 2D, with higher dimensionality stacks being processed slice-by-slice.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class BinaryOperations2D extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from workspace to apply binary operation to.  This image will be 8-bit with binary logic determined by the "Binary logic" parameter.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the "Output image" parameter.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If "Apply to input image" is not selected, the post-operation image will be saved to the workspace with this name.
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* 
	*/
    public static final String OPERATION_SEPARATOR = "Operation controls";

	/**
	* Controls which binary operation will be applied.  The operations are described in full <a href="https://imagej.nih.gov/ij/docs/guide/146-29.html#toc-Subsection-29.8">here</a>:<br><ul><li>"Dilate" Change any foreground-connected background pixels to foreground.  This effectively expands objects by one pixel.</li><li>"Distance map" Create a 32-bit greyscale image where the value of each foreground pixel is equal to its Euclidean distance to the nearest background pixel.</li><li>"Erode" Change any background-connected foreground pixels to background.  This effectively shrinks objects by one pixel.</li><li>"Fill holes" Change all background pixels in a region which is fully enclosed by foreground pixels to foreground.</li><li>"Outline" Convert all non-background-connected foreground pixels to background.  This effectively creates a fully-background image, except for the outer band of foreground pixels.</li><li>"Skeletonise" Repeatedly applies the erode process until each foreground region is a single pixel wide.</li><li>"Ultimate points" Repeatedly applies the erode process until each foreground is reduced to a single pixel.  The value of the remaining, isolated foreground pixels are equal to their equivalent, pre-erosion distance map values.  This process outputs a 32-bit greyscale image.</li><li>"Voronoi" Creates an image subdivided by lines such that all pixels contained within an enclosed region are closest to the same contiguous object in the input binary image.</li><li>"Watershed" Peforms a distance-based watershed transform on the image.  This process is able to split separate regions of a single connected foreground region as long as the sub-regions are connected by narrow necks (e.g. snowman shape).  Background lines are drawn between each sub-region such that they are no longer connected.</li></ul>
	*/
    public static final String OPERATION_MODE = "Filter mode";

	/**
	* Number of times the operation will be run on a single image.  For example, this allows objects to be eroded further than one pixel in a single step.
	*/
    public static final String NUM_ITERATIONS = "Number of iterations";

	/**
	* The minimum number of connected background or foreground for an erosion or dilation process to occur, respectively.
	*/
    public static final String COUNT = "Count";

	/**
	* Controls whether objects are considered to be white (255 intensity) on a black (0 intensity) background, or black on a white background.
	*/
    public static final String BINARY_LOGIC = "Binary logic";

    public BinaryOperations2D(ModulesI modules) {
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

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public static void process(ImagePlus ipl, String operationMode, int numIterations, int count,
            boolean blackBackground) {
        process(ImageFactories.getDefaultFactory().create("Image", ipl), operationMode, numIterations, count, blackBackground);
    }

    public static void process(ImageI image, String operationMode, int numIterations, int count,
            boolean blackBackground) {
        ImagePlus ipl = image.getImagePlus();

        String bg = blackBackground ? "black" : "";

        // Applying processAutomatic to stack
        switch (operationMode) {
            case OperationModes.DILATE:
                IJ.run(ipl, "Options...",
                        "iterations=" + numIterations + " count=" + count + " do=Dilate " + bg + " stack");
                break;

            case OperationModes.DISTANCE_MAP:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Nothing " + bg);
                IJ.run(ipl, "Distance Map", "stack");
                break;

            case OperationModes.ERODE:
                IJ.run(ipl, "Options...",
                        "iterations=" + numIterations + " count=" + count + " do=Erode " + bg + " stack");
                break;

            case OperationModes.FILL_HOLES:
                IJ.run(ipl, "Options...",
                        "iterations=" + numIterations + " count=" + count + " do=[Fill Holes] " + bg + " stack");
                break;

            case OperationModes.OUTLINE:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Nothing " + bg);
                IJ.run(ipl, "Outline", bg + " stack");
                break;

            case OperationModes.SKELETONISE:
                IJ.run(ipl, "Options...",
                        "iterations=" + numIterations + " count=" + count + " do=Skeletonize " + bg + " stack");
                break;

            case OperationModes.VORONOI:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Nothing " + bg);
                IJ.run(ipl, "Voronoi", " stack");
                break;

            case OperationModes.ULTIMATE_POINTS:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Nothing " + bg);
                IJ.run(ipl, "Ultimate Points", " stack");
                break;

            case OperationModes.WATERSHED:
                IJ.run(ipl, "Options...", "iterations=" + numIterations + " count=" + count + " do=Nothing " + bg);
                IJ.run(ipl, "Watershed", " stack");
                break;

        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_BINARY;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Applies stock ImageJ binary operations to an image in the workspace.  This image will be 8-bit with binary logic determined by the \""
                + BINARY_LOGIC
                + "\" parameter.  All operations are performed in 2D, with higher dimensionality stacks being processed slice-by-slice.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String operationMode = parameters.getValue(OPERATION_MODE, workspace);
        int numIterations = parameters.getValue(NUM_ITERATIONS, workspace);
        int count = parameters.getValue(COUNT, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        process(inputImagePlus, operationMode, numIterations, count, blackBackground);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(inputImagePlus);
            if (showOutput)
                inputImage.showAsIs();
        } else {
            ImageI outputImage = ImageFactories.getDefaultFactory().create(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showAsIs();
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(OPERATION_SEPARATOR, this));
        parameters.add(new ChoiceP(OPERATION_MODE, this, OperationModes.DILATE, OperationModes.ALL));
        parameters.add(new IntegerP(NUM_ITERATIONS, this, 1));
        parameters.add(new IntegerP(COUNT, this, 1));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OPERATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OPERATION_MODE));
        switch ((String) parameters.getValue(OPERATION_MODE, workspace)) {
            case OperationModes.DILATE:
            case OperationModes.ERODE:
            case OperationModes.FILL_HOLES:
            case OperationModes.SKELETONISE:
                returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));
                returnedParameters.add(parameters.getParameter(COUNT));
                break;
        }

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from workspace to apply binary operation to.  This image will be 8-bit with binary logic determined by the \""
                        + BINARY_LOGIC + "\" parameter.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(OPERATION_MODE).setDescription(
                "Controls which binary operation will be applied.  The operations are described in full <a href=\"https://imagej.nih.gov/ij/docs/guide/146-29.html#toc-Subsection-29.8\">here</a>:<br><ul>"

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

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
