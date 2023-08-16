package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 19/09/2017.
 */

/**
* Apply pixel-wise intensity calculations for two images of matching dimensions.<br><br>Note: Images to be processed must have matching spatial dimensions and intensity bit-depths.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImageCalculator extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* First image to be processed as part of calculation.
	*/
    public static final String INPUT_IMAGE1 = "Input image 1";

	/**
	* Second image to be processed as part of calculation.
	*/
    public static final String INPUT_IMAGE2 = "Input image 2";

	/**
	* Controls how the resultant image should be output:<br><ul><li>"Create new image" (default) will create a new image and save it to the workspace.</li><li>"Overwrite image 1" will overwrite the first input image with the output image.  The output image will retain all measurements from the first input image.</li><li>"Overwrite image 2" will overwrite the second input image with the output image.  The output image will retain all measurements from the second input image.</li></ul>
	*/
    public static final String OVERWRITE_MODE = "Overwrite mode";

	/**
	* Name of the output image created during the image calculation.  This image will be added to the workspace.
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* When enabled, the calculation will be performed on 32-bit float values.  This is useful if the calculation is likely to create negative or decimal values.  The output image will also be stored in the workspace as a 32-bit float image.
	*/
    public static final String OUTPUT_32BIT = "Output 32-bit image";


	/**
	* 
	*/
    public static final String CALCULATION_SEPARATOR = "Image calculation";

	/**
	* The calculation to apply to the two input images.
	*/
    public static final String CALCULATION_METHOD = "Calculation method";

	/**
	* 
	*/
    public static final String IMAGE_2_CONTRIBUTION = "Image 2 relative contribution";

	/**
	* If input images are 32-bit (or are being converted to 32-bit via "Output 32-bit image" option) the output image can contain NaN (not a number) values in place of any zeros.
	*/
    public static final String SET_NAN_TO_ZERO = "Set NaN values to zero";

    public ImageCalculator(Modules modules) {
        super("Image calculator", modules);
    }

    public interface OverwriteModes {
        String CREATE_NEW = "Create new image";
        String OVERWRITE_IMAGE1 = "Overwrite image 1";
        String OVERWRITE_IMAGE2 = "Overwrite image 2";

        String[] ALL = new String[] { CREATE_NEW, OVERWRITE_IMAGE1, OVERWRITE_IMAGE2 };

    }

    public interface CalculationMethods {
        String ADD = "Add image 1 and image 2";
        String AND = "Image 1 AND image 2 (binary)";
        String DIFFERENCE = "Difference of image 1 and image 2";
        String DIVIDE = "Divide image 1 by image 2";
        String MAX = "Maximum of image 1 and image 2";
        String MEAN = "Mean of image 1 and image 2";
        String MIN = "Minimum of image 1 and image 2";
        String NOT = "Image 1 NOT image 2 (binary)";
        String MULTIPLY = "Multiply image 1 and image 2";
        String SUBTRACT = "Subtract image 2 from image 1";

        String[] ALL = new String[] { ADD, AND, DIFFERENCE, DIVIDE, MAX, MEAN, MIN, MULTIPLY, NOT, SUBTRACT };

    }

    private static void removeNaNs(ImagePlus inputImagePlus1, ImagePlus inputImagePlus2) {
        int width = inputImagePlus1.getWidth();
        int height = inputImagePlus1.getHeight();
        int nChannels = inputImagePlus1.getNChannels();
        int nSlices = inputImagePlus1.getNSlices();
        int nFrames = inputImagePlus1.getNFrames();

        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    inputImagePlus1.setPosition(c, z, t);
                    ImageProcessor imageProcessor1 = inputImagePlus1.getProcessor();

                    inputImagePlus2.setPosition(c, z, t);
                    ImageProcessor imageProcessor2 = inputImagePlus2.getProcessor();

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (Float.isNaN(imageProcessor1.getf(x, y))) {
                                imageProcessor1.set(x, y, 0);
                            }

                            if (Float.isNaN(imageProcessor2.getf(x, y))) {
                                imageProcessor2.set(x, y, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    public static Image process(Image inputImage1, Image inputImage2, String calculationMethod, String overwriteMode,
            @Nullable String outputImageName, boolean output32Bit, boolean setNaNToZero) {
        return process(inputImage1, inputImage2, calculationMethod, overwriteMode, outputImageName, output32Bit,
                setNaNToZero, 1);
    }

    public static Image process(Image inputImage1, Image inputImage2, String calculationMethod, String overwriteMode,
            @Nullable String outputImageName, boolean output32Bit, boolean setNaNToZero, double im2Contibution) {
        ImagePlus ipl1 = inputImage1.getImagePlus();
        ImagePlus ipl2 = inputImage2.getImagePlus();
        ImagePlus iplOut = process(ipl1, ipl2, calculationMethod, overwriteMode, outputImageName, output32Bit,
                setNaNToZero, im2Contibution);

        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
            default:
                return ImageFactory.createImage(outputImageName, iplOut);
            case OverwriteModes.OVERWRITE_IMAGE1:
                return inputImage1;
            case OverwriteModes.OVERWRITE_IMAGE2:
                return inputImage2;
        }
    }

    public static ImagePlus process(ImagePlus imagePlus1, ImagePlus imagePlus2, String calculationMethod,
            String overwriteMode, @Nullable String outputImageName, boolean output32Bit, boolean setNaNToZero) {

        return process(imagePlus1, imagePlus2, calculationMethod, overwriteMode, outputImageName, output32Bit,
                setNaNToZero, 1);

    }

    public static ImagePlus process(ImagePlus imagePlus1, ImagePlus imagePlus2, String calculationMethod,
            String overwriteMode, @Nullable String outputImageName, boolean output32Bit, boolean setNaNToZero,
            double im2Contibution) {
        // If applying to a new image, the input image is duplicated
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                imagePlus1 = new Duplicator().run(imagePlus1);
                imagePlus1.setTitle(outputImageName);
                break;
        }

        // If necessary, converting to 32-bit image
        if (output32Bit) {
            switch (overwriteMode) {
                case OverwriteModes.CREATE_NEW:
                case OverwriteModes.OVERWRITE_IMAGE1:
                    IJ.run(imagePlus1, "32-bit", null);
                    break;

                case OverwriteModes.OVERWRITE_IMAGE2:
                    IJ.run(imagePlus2, "32-bit", null);
                    break;
            }
        }

        int width = imagePlus1.getWidth();
        int height = imagePlus1.getHeight();
        int nChannels = imagePlus1.getNChannels();
        int nSlices = imagePlus1.getNSlices();
        int nFrames = imagePlus1.getNFrames();

        // If necessary, converting NaN values to zero
        if (setNaNToZero && (imagePlus1.getBitDepth() == 32 || imagePlus2.getBitDepth() == 32))
            removeNaNs(imagePlus1, imagePlus2);

        // Getting max val for masking operations
        int maxVal = (int) Math.round(Math.pow(2, imagePlus2.getBitDepth()) - 1);

        // Checking the number of dimensions. If a dimension of image2 is 1 this
        // dimension is used for all images.
        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    imagePlus1.setPosition(c, z, t);
                    ImageProcessor imageProcessor1 = imagePlus1.getProcessor();

                    imagePlus2.setPosition(c, z, t);
                    ImageProcessor imageProcessor2 = imagePlus2.getProcessor();

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            double val = 0;
                            switch (calculationMethod) {
                                case CalculationMethods.ADD:
                                    val = imageProcessor1.getPixelValue(x, y) + imageProcessor2.getPixelValue(x, y);
                                    break;

                                case CalculationMethods.AND:
                                    val = imageProcessor2.getPixelValue(x, y) == maxVal
                                            ? imageProcessor1.getPixelValue(x, y)
                                            : 0;
                                    break;

                                case CalculationMethods.DIFFERENCE:
                                    val = Math.abs(
                                            imageProcessor1.getPixelValue(x, y) - imageProcessor2.getPixelValue(x, y));
                                    break;

                                case CalculationMethods.DIVIDE:
                                    if (output32Bit) {
                                        val = imageProcessor1.getPixelValue(x, y) / imageProcessor2.getPixelValue(x, y);
                                    } else {
                                        if (imageProcessor2.getPixelValue(x, y) == 0) {
                                            val = Math.pow(2, imageProcessor1.getBitDepth()) - 1;
                                        } else {
                                            // Using "floor" to maintain consistency with ImageJ's calculator
                                            val = Math.floor(imageProcessor1.getPixelValue(x, y)
                                                    / imageProcessor2.getPixelValue(x, y));
                                        }
                                    }
                                    break;

                                case CalculationMethods.MAX:
                                    val = Math.max(imageProcessor1.getPixelValue(x, y),
                                            imageProcessor2.getPixelValue(x, y));
                                    break;

                                case CalculationMethods.MEAN:
                                    val = (imageProcessor1.getPixelValue(x, y)
                                            + imageProcessor2.getPixelValue(x, y) * im2Contibution)
                                            / (1 + im2Contibution);
                                    break;

                                case CalculationMethods.MIN:
                                    val = Math.min(imageProcessor1.getPixelValue(x, y),
                                            imageProcessor2.getPixelValue(x, y));
                                    break;

                                case CalculationMethods.MULTIPLY:
                                    val = imageProcessor1.getPixelValue(x, y) * imageProcessor2.getPixelValue(x, y);
                                    break;

                                case CalculationMethods.NOT:
                                    val = imageProcessor2.getPixelValue(x, y) != maxVal
                                            ? imageProcessor1.getPixelValue(x, y)
                                            : 0;
                                    break;

                                case CalculationMethods.SUBTRACT:
                                    val = imageProcessor1.getPixelValue(x, y) - imageProcessor2.getPixelValue(x, y);
                                    break;

                            }

                            switch (overwriteMode) {
                                case OverwriteModes.CREATE_NEW:
                                case OverwriteModes.OVERWRITE_IMAGE1:
                                    imageProcessor1.putPixelValue(x, y, val);
                                    break;

                                case OverwriteModes.OVERWRITE_IMAGE2:
                                    imageProcessor2.putPixelValue(x, y, val);
                                    break;

                            }
                        }
                    }
                }
            }
        }

        imagePlus1.setPosition(1, 1, 1);
        imagePlus2.setPosition(1, 1, 1);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                return imagePlus1;
            case OverwriteModes.OVERWRITE_IMAGE1:
                return imagePlus1;
            case OverwriteModes.OVERWRITE_IMAGE2:
                return imagePlus2;
        }

        return null;

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Apply pixel-wise intensity calculations for two images of matching dimensions.<br><br>"
                + "Note: Images to be processed must have matching spatial dimensions and intensity bit-depths.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String inputImageName1 = parameters.getValue(INPUT_IMAGE1, workspace);
        Image inputImage1 = workspace.getImages().get(inputImageName1);
        ImagePlus inputImagePlus1 = inputImage1.getImagePlus();

        String inputImageName2 = parameters.getValue(INPUT_IMAGE2, workspace);
        Image inputImage2 = workspace.getImages().get(inputImageName2);
        ImagePlus inputImagePlus2 = inputImage2.getImagePlus();

        // Getting parameters
        String overwriteMode = parameters.getValue(OVERWRITE_MODE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        boolean output32Bit = parameters.getValue(OUTPUT_32BIT, workspace);
        String calculationMethod = parameters.getValue(CALCULATION_METHOD, workspace);
        double im2Contibution = parameters.getValue(IMAGE_2_CONTRIBUTION, workspace);
        boolean setNaNToZero = parameters.getValue(SET_NAN_TO_ZERO, workspace);

        ImagePlus newIpl = process(inputImagePlus1, inputImagePlus2, calculationMethod, overwriteMode, outputImageName,
                output32Bit, setNaNToZero, im2Contibution);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                newIpl.updateChannelAndDraw();
                Image outputImage = ImageFactory.createImage(outputImageName, newIpl);
                workspace.addImage(outputImage);
                if (showOutput)
                    outputImage.show();
                break;

            case OverwriteModes.OVERWRITE_IMAGE1:
                inputImage1.getImagePlus().updateChannelAndDraw();
                if (showOutput)
                    inputImage1.show();
                break;

            case OverwriteModes.OVERWRITE_IMAGE2:
                inputImage2.getImagePlus().updateChannelAndDraw();
                if (showOutput)
                    inputImage2.show();
                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE1, this, "", "First image to be processed as part of calculation."));
        parameters.add(new InputImageP(INPUT_IMAGE2, this, "", "Second image to be processed as part of calculation."));
        parameters.add(new ChoiceP(OVERWRITE_MODE, this, OverwriteModes.CREATE_NEW, OverwriteModes.ALL,
                "Controls how the resultant image should be output:<br><ul>"

                        + "<li>\"" + OverwriteModes.CREATE_NEW
                        + "\" (default) will create a new image and save it to the workspace.</li>"

                        + "<li>\"" + OverwriteModes.OVERWRITE_IMAGE1
                        + "\" will overwrite the first input image with the output image.  The output image will retain all measurements from the first input image.</li>"

                        + "<li>\"" + OverwriteModes.OVERWRITE_IMAGE2
                        + "\" will overwrite the second input image with the output image.  The output image will retain all measurements from the second input image.</li></ul>"));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "",
                "Name of the output image created during the image calculation.  This image will be added to the workspace."));
        parameters.add(new BooleanP(OUTPUT_32BIT, this, false,
                "When enabled, the calculation will be performed on 32-bit float values.  This is useful if the calculation is likely to create negative or decimal values.  The output image will also be stored in the workspace as a 32-bit float image."));
        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_METHOD, this, CalculationMethods.ADD, CalculationMethods.ALL,
                "The calculation to apply to the two input images."));
        parameters.add(new DoubleP(IMAGE_2_CONTRIBUTION, this, 1d));
        parameters.add(new BooleanP(SET_NAN_TO_ZERO, this, false,
                "If input images are 32-bit (or are being converted to 32-bit via \"" + OUTPUT_32BIT
                        + "\" option) the output image can contain NaN (not a number) values in place of any zeros."));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE2));
        returnedParameters.add(parameters.getParameter(OVERWRITE_MODE));

        if (parameters.getValue(OVERWRITE_MODE, workspace).equals(OverwriteModes.CREATE_NEW)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_32BIT));

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_METHOD));
        if (((String) parameters.getValue(CALCULATION_METHOD, workspace)).equals(CalculationMethods.MEAN))
            returnedParameters.add(parameters.getParameter(IMAGE_2_CONTRIBUTION));
        returnedParameters.add(parameters.getParameter(SET_NAN_TO_ZERO));

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
}
