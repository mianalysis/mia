package io.github.mianalysis.mia.module.images.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetLookupTable;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.image.ImgPlusTools;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;


/**
* Crop an image in X and Y using pre-defined limits or limits based on the extents of objects in a collection.  Any pixels outside the specified limits are discarded.<br><br>Note: The x-min, y-min, width and height limits used here are in the same order and format as those output by ImageJ's default rectangle region of interest tool (i.e. displayed in the status bar of the ImageJ control panel).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CropImage<T extends RealType<T> & NativeType<T>> extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from workspace to apply crop process to.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Select if the crop should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* Name of the output image created during the cropping process if storing the cropped image as a new image in the workspace ("Apply to input image" parameter).
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* 
	*/
    public static final String CROP_SEPARATOR = "Crop selection";

	/**
	* Controls how the limits for the cropped region are specified:<br><ul><li>"Fixed values" The input image will be cropped to the region specified by the fixed values, "Left coordinate", "Top coordinate", "Width" and "Height".</li><li>"Object collection limits" The input image will be cropped to the region corresponding to the limits of the object collection specified by "Input objects"</li></ul>
	*/
    public static final String LIMITS_MODE = "Limits mode";

	/**
	* Left crop coordinate.  All pixels with x-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.
	*/
    public static final String LEFT = "Left coordinate";

	/**
	* Top crop coordinate.  All pixels with y-coordinates lower than this will be removed.  Specified in pixel units with indexing starting at 0.
	*/
    public static final String TOP = "Top coordinate";

	/**
	* Width (number of columns) of the output cropped region.  Specified in pixel units.
	*/
    public static final String WIDTH = "Width";

	/**
	* Height (number of rows) of the output cropped region.  Specified in pixel units.
	*/
    public static final String HEIGHT = "Height";

	/**
	* When "Limits mode" is set to "Object collection limits", these are the objects that will be used to define the cropped region.
	*/
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
            int left, int top, int width, int height) {
        Calibration calibration = inputImage.getImagePlus().getCalibration();
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        int xIdx = inputImg.dimensionIndex(Axes.X);
        int yIdx = inputImg.dimensionIndex(Axes.Y);

        // Ensuring crop region is within limits
        int right = left + width;
        int bottom = top + height;
        top = Math.max(0, top);
        left = Math.max(0, left);
        right = Math.min((int) inputImg.dimension(xIdx), right);
        bottom = Math.min((int) inputImg.dimension(yIdx), bottom);
        width = right - left;
        height = bottom - top;

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
        DiskCachedCellImgFactory<T> factory = new DiskCachedCellImgFactory<>(inputImg.firstElement());
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
        ImgPlusTools.applyDimensions(outputImg, outputImagePlus);

        Image outputImage = ImageFactory.createImage(outputImageName, outputImagePlus);
        SetLookupTable.copyLUTFromImage(outputImage,inputImage);
        
        return outputImage;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Crop an image in X and Y using pre-defined limits or limits based on the extents of objects in a collection.  Any pixels outside the specified limits are discarded.<br><br>Note: The x-min, y-min, width and height limits used here are in the same order and format as those output by ImageJ's default rectangle region of interest tool (i.e. displayed in the status bar of the ImageJ control panel).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String limitsMode = parameters.getValue(LIMITS_MODE, workspace);
        int left = parameters.getValue(LEFT, workspace);
        int top = parameters.getValue(TOP, workspace);
        int width = parameters.getValue(WIDTH, workspace);
        int height = parameters.getValue(HEIGHT, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        switch (limitsMode) {
            case LimitsModes.FROM_OBJECTS:
                Objs inputObjects = workspace.getObjects(inputObjectsName);
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

        if (width < 0 || height < 0) {
            MIA.log.writeWarning("Crop width or height was less than 0.  Module execution failed.");
            return Status.FAIL;
        }

        // Applying crop
        Image outputImage = cropImage(inputImage, outputImageName, left, top, width, height);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(outputImage.getImagePlus());
            if (showOutput)
                inputImage.show();

        } else {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show();
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
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CROP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LIMITS_MODE));
        switch ((String) parameters.getValue(LIMITS_MODE, workspace)) {
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
