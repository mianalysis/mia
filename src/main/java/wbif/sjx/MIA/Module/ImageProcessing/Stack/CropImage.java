package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Process.ImgPlusTools;

public class CropImage<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CROP_SEPARATOR = "Crop selection";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";

    public CropImage(ModuleCollection modules) {
        super("Crop image", modules);
    }

    public static <T extends RealType<T> & NativeType<T>> Image cropImage(Image<T> inputImage, String outputImageName,
            int top, int left, int width, int height) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        int xIdx = inputImg.dimensionIndex(Axes.X);
        int yIdx = inputImg.dimensionIndex(Axes.Y);

        long[] offsetIn = new long[inputImg.numDimensions()];
        long[] dimsIn = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dimsIn[i] = inputImg.dimension(i);
        offsetIn[xIdx] = left;
        offsetIn[yIdx] = top;
        dimsIn[xIdx] = width;
        dimsIn[yIdx] = height;

        long[] dimsOut = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dimsOut[i] = inputImg.dimension(i);
        dimsOut[xIdx] = width;
        dimsOut[yIdx] = height;

        // Creating the output image and copying over the pixel coordinates
        CellImgFactory<T> factory = new CellImgFactory<>(inputImg.firstElement());
        ImgPlus<T> outputImg = new ImgPlus<>(factory.create(dimsOut));
        ImgPlusTools.copyAxes(inputImg, outputImg);

        RandomAccess<T> randomAccessIn = Views.offsetInterval(inputImg, offsetIn, dimsIn).randomAccess();
        Cursor<T> cursorOut = outputImg.localizingCursor();

        while (cursorOut.hasNext()) {
            cursorOut.fwd();
            randomAccessIn.setPosition(cursorOut);
            cursorOut.get().set(randomAccessIn.get());
        }

        // For some reason the ImagePlus produced by ImageJFunctions.wrap() behaves
        // strangely, but this can be remedied
        // by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg, outputImageName);
        outputImagePlus.setCalibration(inputImagePlus.getCalibration());
        ImgPlusTools.applyAxes(outputImg, outputImagePlus);

        return new Image(outputImageName, outputImagePlus);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Crop an input image in X and Y using pre-defined limits.  Any pixels outside the specified limits are discarded.<br><br>Note: The x-min, y-min, width and height limits used here are in the same order and format as those output by ImageJ's default rectangle region of interest tool (i.e. displayed in the status bar of the ImageJ control panel).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);

        Image outputImage = cropImage(inputImage, outputImageName, top, left, width, height);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(outputImage.getImagePlus());
            if (showOutput)
                inputImage.showImage();

        } else {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new SeparatorP(CROP_SEPARATOR, this));
        parameters.add(new IntegerP(LEFT, this, 0));
        parameters.add(new IntegerP(TOP, this, 0));
        parameters.add(new IntegerP(WIDTH, this, 512));
        parameters.add(new IntegerP(HEIGHT, this, 512));

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

        returnedParameters.add(parameters.getParameter(CROP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LEFT));
        returnedParameters.add(parameters.getParameter(TOP));
        returnedParameters.add(parameters.getParameter(WIDTH));
        returnedParameters.add(parameters.getParameter(HEIGHT));

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
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply crop process to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the crop should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the cropping process if storing the cropped image as a new image in the workspace (\""
                        + APPLY_TO_INPUT + "\" parameter).");

        parameters.get(LEFT).setDescription(
                "Left crop coordinate.  All pixels with x-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.");

        parameters.get(TOP).setDescription(
                "Top crop coordinate.  All pixels with y-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.");

        parameters.get(WIDTH)
                .setDescription("Width (number of columns) of the output cropped region.  Specified in pixel units.");

        parameters.get(HEIGHT)
                .setDescription("Height (number of rows) of the output cropped region.  Specified in pixel units.");

    }
}
