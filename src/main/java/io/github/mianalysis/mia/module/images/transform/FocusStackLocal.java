package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.SubHyperstackMaker;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.thirdparty.Stack_Focuser_;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FocusStackLocal extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String OUTPUT_FOCUSED_IMAGE = "Output focused image";
    public static final String OUTPUT_HEIGHT_IMAGE = "Output height image";

    public static final String FOCUS_SEPARATOR = "Focus controls";
    public static final String USE_EXISTING_HEIGHT_IMAGE = "Use existing height image";
    public static final String INPUT_HEIGHT_IMAGE = "Input height image";
    public static final String RANGE = "Range";
    public static final String SMOOTH_HEIGHT_MAP = "Smooth height map";

    public interface OutputModes {
        String FOCUSED_IMAGE_AND_HEIGHT_MAP = "Focused image and height map";
        String FOCUSED_IMAGE_ONLY = "Focused image only";
        String HEIGHT_MAP_ONLY = "Height map only";

        String[] ALL = new String[] { FOCUSED_IMAGE_AND_HEIGHT_MAP, FOCUSED_IMAGE_ONLY, HEIGHT_MAP_ONLY };

    }

    public FocusStackLocal(Modules modules) {
        super("Focus stack (local)", modules);
    }

    public static Image getHeightMap(Image inputImage, String outputHeightImageName, int range, boolean smooth) {
        String moduleName = new FocusStackLocal(null).getName();

        ImagePlus inputIpl = inputImage.getImagePlus();
        ImageProcessor ipr = inputIpl.getProcessor();

        // If necessary, creating the height image
        ImagePlus heightIpl = createEmptyImage(inputIpl, outputHeightImageName, 16);
        heightIpl.setCalibration(inputIpl.getCalibration());
        Image heightImage = ImageFactory.createImage(outputHeightImageName, heightIpl);

        // Getting the image type
        int type = getImageType(ipr);

        // Initialising the stack focusser. This requires an example stack.
        int nSlices = inputIpl.getNSlices();
        ImagePlus stack = SubHyperstackMaker.makeSubhyperstack(inputIpl, "1-1", "1-" + nSlices, "1-1");

        Stack_Focuser_ focuser = new Stack_Focuser_();
        focuser.setup("ksize=" + range + " hmap=" + 0 + " rgbone=" + 1 + " smooth=" + smooth, stack);

        // Iterating over all timepoints and channels
        int nStacks = inputIpl.getNChannels() * inputIpl.getNFrames();
        int count = 0;
        for (int c = 1; c <= inputIpl.getNChannels(); c++) {
            for (int t = 1; t <= inputIpl.getNFrames(); t++) {
                stack = SubHyperstackMaker.makeSubhyperstack(inputIpl, c + "-" + c, 1 + "-" + nSlices, t + "-" + t);

                // Adding the focused image to the output image stack
                focuser.focusGreyStack(stack.getStack(), type);

                // If necessary, adding the height image
                ImageProcessor heightIpr = focuser.getHeightImage();
                heightIpl.setPosition(c, 1, t);
                heightIpl.setProcessor(heightIpr);

                writeProgressStatus(++count, nStacks, "stacks", moduleName);

            }
        }

        heightIpl.setPosition(1, 1, 1);
        heightIpl.updateAndDraw();

        return heightImage;

    }

    public static Image focusStack(Image inputImage, String outputImageName, Image inputHeightImage) {
        String moduleName = new FocusStackLocal(null).getName();

        ImagePlus inputIpl = inputImage.getImagePlus();
        ImageProcessor ipr = inputIpl.getProcessor();

        // Creating array to hold [0] the focused image and [1] the height map
        ImagePlus outputIpl = createEmptyImage(inputIpl, outputImageName, inputIpl.getBitDepth());
        outputIpl.setCalibration(inputIpl.getCalibration());
        Image outputImage = ImageFactory.createImage(outputImageName, outputIpl);

        // Getting the image type
        int type = getImageType(ipr);

        // Initialising the stack focusser. This requires an example stack.
        int nSlices = inputIpl.getNSlices();
        ImagePlus stack = SubHyperstackMaker.makeSubhyperstack(inputIpl, "1-1", "1-" + nSlices, "1-1");

        Stack_Focuser_ focuser = new Stack_Focuser_();
        focuser.setup("ksize=" + 1 + " hmap=" + 0 + " rgbone=" + 1 + " smooth=" + false, stack);

        // Iterating over all timepoints and channels
        int nStacks = inputIpl.getNChannels() * inputIpl.getNFrames();
        int count = 0;
        for (int c = 1; c <= inputIpl.getNChannels(); c++) {
            for (int t = 1; t <= inputIpl.getNFrames(); t++) {
                stack = SubHyperstackMaker.makeSubhyperstack(inputIpl, c + "-" + c, 1 + "-" + nSlices, t + "-" + t);

                // If using an existing map, adding that now
                inputHeightImage.getImagePlus().setPosition(c, 1, t);
                focuser.setExistingHeightMap(inputHeightImage.getImagePlus().getProcessor());

                // Adding the focused image to the output image stack
                ImageProcessor focusedIpr = focuser.focusGreyStack(stack.getStack(), type);
                outputIpl.setPosition(c, 1, t);
                outputIpl.setProcessor(focusedIpr);

                writeProgressStatus(++count, nStacks, "stacks", moduleName);

            }
        }

        outputIpl.setPosition(1, 1, 1);
        outputIpl.updateAndDraw();

        return outputImage;

    }

    public static int getImageType(ImageProcessor ipr) {
        // Determining the type of image
        int type = Stack_Focuser_.RGB;
        if (ipr instanceof ByteProcessor)
            type = Stack_Focuser_.BYTE;
        else if (ipr instanceof ShortProcessor)
            type = Stack_Focuser_.SHORT;
        else if (ipr instanceof FloatProcessor)
            type = Stack_Focuser_.FLOAT;

        return type;

    }

    public static ImagePlus createEmptyImage(ImagePlus inputImagePlus, String outputImageName, int bitDepth) {
        // Getting image dimensions
        int width = inputImagePlus.getWidth();
        int height = inputImagePlus.getHeight();
        int nChannels = inputImagePlus.getNChannels();
        int nFrames = inputImagePlus.getNFrames();

        return IJ.createHyperStack(outputImageName, width, height, nChannels, 1, nFrames, bitDepth);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "Focuses a Z-stack into a single plane using the StackFocuser ImageJ plugin.  Best focus position is determined at each 2D pixel location, with the final image being comprised of the pixels from the slice with the best focus at that location.  Each channel and timepoint is focused separately.  Prior to application, the focus map can be median filtered to remove outliers.  Height maps can be stored and used in additional \""
                + new FocusStackLocal(null).getName()
                + "\" instances, thus allowing height maps to be edited prior to use."

                + "<br><br>Uses the <a href=\"https://imagej.nih.gov/ij/plugins/download/Stack_Focuser_.java\">StackFocuser</a> plugin created by Mikhail Umorin (source code downloaded on 06-June-2018).";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        String outputMode = parameters.getValue(OUTPUT_MODE,workspace);
        String outputFocusedImageName = parameters.getValue(OUTPUT_FOCUSED_IMAGE,workspace);
        String outputHeightImageName = parameters.getValue(OUTPUT_HEIGHT_IMAGE,workspace);
        boolean useExisting = parameters.getValue(USE_EXISTING_HEIGHT_IMAGE,workspace);
        String inputHeightImageName = parameters.getValue(INPUT_HEIGHT_IMAGE,workspace);
        int range = parameters.getValue(RANGE,workspace);
        boolean smooth = parameters.getValue(SMOOTH_HEIGHT_MAP,workspace);

        Image inputImage = workspace.getImage(inputImageName);

        // Getting height map
        Image heightMap = null;
        if (useExisting) {
            // Duplicating the image, so the addition doesn't affect it
            heightMap = workspace.getImage(inputHeightImageName);
            heightMap = ImageFactory.createImage(heightMap.getName(), heightMap.getImagePlus().duplicate());

            // StackFocuser plugin wants height image indices 1-based, but they're output to
            // the workspace as 0-based for consistency with MIA.
            ImageMath.process(heightMap, ImageMath.CalculationModes.ADD, 1);
        } else {
            heightMap = getHeightMap(inputImage, outputHeightImageName, range, smooth);
        }

        // Running stack focusing
        switch (outputMode) {
            case OutputModes.FOCUSED_IMAGE_AND_HEIGHT_MAP:
            case OutputModes.FOCUSED_IMAGE_ONLY:
                Image outputImage = focusStack(inputImage, outputFocusedImageName, heightMap);
                workspace.addImage(outputImage);
                if (showOutput)
                    outputImage.show();

                break;
        }

        // Adding output image to Workspace
        switch (outputMode) {
            case OutputModes.FOCUSED_IMAGE_AND_HEIGHT_MAP:
            case OutputModes.HEIGHT_MAP_ONLY:
                // Converting StackFocuser's 1-based height map image back to 0-based for
                // consistency with MIA
                ImageMath.process(heightMap, ImageMath.CalculationModes.SUBTRACT, 1);

                if (showOutput)
                    heightMap.show();

                workspace.addImage(heightMap);
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.FOCUSED_IMAGE_ONLY, OutputModes.ALL));
        parameters.add(new OutputImageP(OUTPUT_FOCUSED_IMAGE, this));
        parameters.add(new OutputImageP(OUTPUT_HEIGHT_IMAGE, this));

        parameters.add(new SeparatorP(FOCUS_SEPARATOR, this));
        parameters.add(new BooleanP(USE_EXISTING_HEIGHT_IMAGE, this, false));
        parameters.add(new InputImageP(INPUT_HEIGHT_IMAGE, this));
        parameters.add(new IntegerP(RANGE, this, 11));
        parameters.add(new BooleanP(SMOOTH_HEIGHT_MAP, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE,workspace)) {
            case OutputModes.FOCUSED_IMAGE_AND_HEIGHT_MAP:
                returnedParameters.add(parameters.getParameter(OUTPUT_FOCUSED_IMAGE));
                returnedParameters.add(parameters.getParameter(OUTPUT_HEIGHT_IMAGE));
                break;
            case OutputModes.FOCUSED_IMAGE_ONLY:
                returnedParameters.add(parameters.getParameter(OUTPUT_FOCUSED_IMAGE));
                break;
            case OutputModes.HEIGHT_MAP_ONLY:
                returnedParameters.add(parameters.getParameter(OUTPUT_HEIGHT_IMAGE));
                break;
        }

        returnedParameters.add(parameters.getParameter(FOCUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_EXISTING_HEIGHT_IMAGE));
        if ((boolean) parameters.getValue(USE_EXISTING_HEIGHT_IMAGE,workspace)) {
            returnedParameters.add(parameters.getParameter(INPUT_HEIGHT_IMAGE));
        } else {
            returnedParameters.add(parameters.getParameter(RANGE));
            returnedParameters.add(parameters.getParameter(SMOOTH_HEIGHT_MAP));
        }

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
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE)
                .setDescription("Image stack from the workspace which will be focused into a single plane.");

        parameters.get(OUTPUT_FOCUSED_IMAGE).setDescription(
                "Output focused image which will be added to the workspace.  This image will have the same number of channels and timepoints as the input image, but will always only have a single Z-slice.");

        // parameters.get(ADD_HEIGHT_MAP_TO_WORKSPACE).setDescription(
        // "When selected, the height map image will be added to the workspace with the
        // name specified by \""
        // + OUTPUT_HEIGHT_IMAGE
        // + "\". Since the height map image can be used as an input to the
        // \"FocusStack\" module, the height map can be edited prior to being used to
        // generate a focused image.");

        // parameters.get(OUTPUT_HEIGHT_IMAGE).setDescription("If \"" +
        // ADD_HEIGHT_MAP_TO_WORKSPACE
        // + "\" is selected, the height map will be added to the workspace with this
        // name. The height map image has a single slice and equal number of channels
        // and timepoints to the input image. The value of each pixel corresponds to the
        // best-focused Z-position of the input stack. Slice indices are zero-indexed
        // (i.e. first slice has an index of 0).");

        parameters.get(USE_EXISTING_HEIGHT_IMAGE)
                .setDescription("When selected, the height map image will be loaded from the workspace (\""
                        + INPUT_HEIGHT_IMAGE + "\" parameter) rather than being calculated based on the input image.");

        parameters.get(INPUT_HEIGHT_IMAGE).setDescription(
                "The name of the height map image in the workspace if the height map has been pre-determined.");

        parameters.get(RANGE).setDescription("If calculating a new height image (\"" + USE_EXISTING_HEIGHT_IMAGE
                + "\" parameter isn't selected), the best focus slice at each pixel will be based on pixel intensities within this range (specified in pixel units).");

        parameters.get(SMOOTH_HEIGHT_MAP).setDescription(
                "When selected, the height map will be passed through a 2D median filter (range specified by \"" + RANGE
                        + "\" parameter) to remove outliers.");

    }
}
