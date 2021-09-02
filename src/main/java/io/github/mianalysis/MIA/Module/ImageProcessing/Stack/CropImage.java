package io.github.mianalysis.mia.module.imageprocessing.Stack;

import ij.ImagePlus;
import ij.measure.Calibration;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Image;
import io.github.mianalysis.mia.Object.Objs;
import io.github.mianalysis.mia.Object.Status;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Parameters.BooleanP;
import io.github.mianalysis.mia.Object.Parameters.ChoiceP;
import io.github.mianalysis.mia.Object.Parameters.InputImageP;
import io.github.mianalysis.mia.Object.Parameters.InputObjectsP;
import io.github.mianalysis.mia.Object.Parameters.OutputImageP;
import io.github.mianalysis.mia.Object.Parameters.Parameters;
import io.github.mianalysis.mia.Object.Parameters.SeparatorP;
import io.github.mianalysis.mia.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.mia.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.PartnerRefs;
import io.github.sjcross.common.Process.ImgPlusTools;

public class CropImage<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CROP_SEPARATOR = "Crop selection";
    public static final String LIMITS_MODE = "Limits mode";
    public static final String LEFT = "Left coordinate";
    public static final String TOP = "Top coordinate";
    public static final String WIDTH = "Width";
    public static final String HEIGHT = "Height";
    public static final String INPUT_OBJECTS = "Input objects";

    public interface LimitsModes {
        String FIXED_VALUES = "Fixed values";
        String FROM_OBJECTS = "Object collection limits";

        String[] ALL = new String[] { FIXED_VALUES, FROM_OBJECTS };

    }

    public CropImage(Modules modules) {
        super("Crop image", modules);
    }

    public static <T extends RealType<T> & NativeType<T>> Image cropImage(Image inputImage, String outputImageName,
            int top, int left, int width, int height) {
        Calibration calibration = inputImage.getImagePlus().getCalibration();
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
        // strangely, but this can be remedied by duplicating it
        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg, outputImageName).duplicate();
        outputImagePlus.setCalibration(calibration);
        ImgPlusTools.applyAxes(outputImg, outputImagePlus);

        return new Image(outputImageName, outputImagePlus);

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Crop an image in X and Y using pre-defined limits or limits based on the extents of objects in a collection.  Any pixels outside the specified limits are discarded.<br><br>Note: The x-min, y-min, width and height limits used here are in the same order and format as those output by ImageJ's default rectangle region of interest tool (i.e. displayed in the status bar of the ImageJ control panel).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String limitsMode = parameters.getValue(LIMITS_MODE);
        int left = parameters.getValue(LEFT);
        int top = parameters.getValue(TOP);
        int width = parameters.getValue(WIDTH);
        int height = parameters.getValue(HEIGHT);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        switch (limitsMode) {
            case LimitsModes.FROM_OBJECTS:
                Objs inputObjects = workspace.getObjectSet(inputObjectsName);
                int[][] extents = inputObjects.getSpatialExtents();

                if (extents == null) {
                    MIA.log.writeWarning("No objects to crop image from");
                    return Status.PASS;
                }
                left = extents[0][0];
                top = extents[1][0];
                width = extents[0][1] - extents[0][0] + 1;
                height = extents[1][1] - extents[1][0] + 1;
                break;
        }

        // Applying crop
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
        parameters.add(new ChoiceP(LIMITS_MODE, this, LimitsModes.FIXED_VALUES, LimitsModes.ALL));
        parameters.add(new IntegerP(LEFT, this, 0));
        parameters.add(new IntegerP(TOP, this, 0));
        parameters.add(new IntegerP(WIDTH, this, 512));
        parameters.add(new IntegerP(HEIGHT, this, 512));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CROP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LIMITS_MODE));
        switch ((String) parameters.getValue(LIMITS_MODE)) {
            case LimitsModes.FIXED_VALUES:
                returnedParameters.add(parameters.getParameter(LEFT));
                returnedParameters.add(parameters.getParameter(TOP));
                returnedParameters.add(parameters.getParameter(WIDTH));
                returnedParameters.add(parameters.getParameter(HEIGHT));
                break;
            case LimitsModes.FROM_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                break;
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
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply crop process to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the crop should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the cropping process if storing the cropped image as a new image in the workspace (\""
                        + APPLY_TO_INPUT + "\" parameter).");

        parameters.get(LIMITS_MODE)
                .setDescription("Controls how the limits for the cropped region are specified:<br><ul>"

                        + "<li>\"" + LimitsModes.FIXED_VALUES
                        + "\" The input image will be cropped to the region specified by the fixed values, \"" + LEFT
                        + "\", \"" + TOP + "\", \"" + WIDTH + "\" and \"" + HEIGHT + "\".</li>"

                        + "<li>\"" + LimitsModes.FROM_OBJECTS
                        + "\" The input image will be cropped to the region corresponding to the limits of the object collection specified by \""
                        + INPUT_OBJECTS + "\"</li></ul>");

        parameters.get(LEFT).setDescription(
                "Left crop coordinate.  All pixels with x-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.");

        parameters.get(TOP).setDescription(
                "Top crop coordinate.  All pixels with y-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.");

        parameters.get(WIDTH)
                .setDescription("Width (number of columns) of the output cropped region.  Specified in pixel units.");

        parameters.get(HEIGHT)
                .setDescription("Height (number of rows) of the output cropped region.  Specified in pixel units.");

        parameters.get(INPUT_OBJECTS)
                .setDescription("When \"" + LIMITS_MODE + "\" is set to \"" + LimitsModes.FROM_OBJECTS
                        + "\", these are the objects that will be used to define the cropped region.");
    }
}
