package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
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
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 19/09/2017.
 */
public class ImageCalculator extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE1 = "Input image 1";
    public static final String INPUT_IMAGE2 = "Input image 2";
    public static final String OVERWRITE_MODE = "Overwrite mode";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_32BIT = "Output 32-bit image";

    public static final String CALCULATION_SEPARATOR = "Image calculation";
    public static final String CALCULATION_METHOD = "Calculation method";
    public static final String SET_NAN_TO_ZERO = "Set NaN values to zero";

    public ImageCalculator(ModuleCollection modules) {
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
        String DIFFERENCE = "Difference of image 1 and image 2";
        String DIVIDE = "Divide image 1 by image 2";
        String MAX = "Maximum of image 1 and image 2";
        String MEAN = "Mean of image 1 and image 2";
        String MIN = "Minimum of image 1 and image 2";
        String MULTIPLY = "Multiply image 1 and image 2";
        String SUBTRACT = "Subtract image 2 from image 1";

        String[] ALL = new String[] { ADD, DIFFERENCE, DIVIDE, MAX, MEAN, MIN, MULTIPLY, SUBTRACT };

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
        ImagePlus ipl1 = inputImage1.getImagePlus();
        ImagePlus ipl2 = inputImage2.getImagePlus();
        ImagePlus iplOut = process(ipl1, ipl2, calculationMethod, overwriteMode, output32Bit, setNaNToZero);

        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
            default:
                return new Image(outputImageName, iplOut);
            case OverwriteModes.OVERWRITE_IMAGE1:
                return inputImage1;
            case OverwriteModes.OVERWRITE_IMAGE2:
                return inputImage2;
        }
    }

    public static ImagePlus process(ImagePlus imagePlus1, ImagePlus imagePlus2, String calculationMethod,
            String overwriteMode, boolean output32Bit, boolean setNaNToZero) {
        // If applying to a new image, the input image is duplicated
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                imagePlus1 = new Duplicator().run(imagePlus1);
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
        if (setNaNToZero && (imagePlus1.getBitDepth() == 32 || imagePlus2.getBitDepth() == 32)) {
            removeNaNs(imagePlus1, imagePlus2);
        }

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
                                    val = (imageProcessor1.getPixelValue(x, y) + imageProcessor2.getPixelValue(x, y))
                                            / 2;
                                    break;

                                case CalculationMethods.MIN:
                                    val = Math.min(imageProcessor1.getPixelValue(x, y),
                                            imageProcessor2.getPixelValue(x, y));
                                    break;

                                case CalculationMethods.MULTIPLY:
                                    val = imageProcessor1.getPixelValue(x, y) * imageProcessor2.getPixelValue(x, y);
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
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "Apply pixel-wise intensity calculations for two images of matching dimensions.<br><br>"
                + "Note: Images to be processed must have matching spatial dimensions and intensity bit-depths.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input images
        String inputImageName1 = parameters.getValue(INPUT_IMAGE1);
        Image inputImage1 = workspace.getImages().get(inputImageName1);
        ImagePlus inputImagePlus1 = inputImage1.getImagePlus();

        String inputImageName2 = parameters.getValue(INPUT_IMAGE2);
        Image inputImage2 = workspace.getImages().get(inputImageName2);
        ImagePlus inputImagePlus2 = inputImage2.getImagePlus();

        // Getting parameters
        String overwriteMode = parameters.getValue(OVERWRITE_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean output32Bit = parameters.getValue(OUTPUT_32BIT);
        String calculationMethod = parameters.getValue(CALCULATION_METHOD);
        boolean setNaNToZero = parameters.getValue(SET_NAN_TO_ZERO);

        ImagePlus newIpl = process(inputImagePlus1, inputImagePlus2, calculationMethod, overwriteMode, output32Bit,
                setNaNToZero);

        // If the image is being saved as a new image, adding it to the workspace
        switch (overwriteMode) {
            case OverwriteModes.CREATE_NEW:
                Image outputImage = new Image(outputImageName, newIpl);
                workspace.addImage(outputImage);
                if (showOutput)
                    outputImage.showImage();
                break;

            case OverwriteModes.OVERWRITE_IMAGE1:
                if (showOutput)
                    inputImage1.showImage();
                break;

            case OverwriteModes.OVERWRITE_IMAGE2:
                if (showOutput)
                    inputImage2.showImage();
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
        parameters.add(new BooleanP(SET_NAN_TO_ZERO, this, false,
                "If input images are 32-bit (or are being converted to 32-bit via \"" + OUTPUT_32BIT
                        + "\" option) the output image can contain NaN (not a number) values in place of any zeros."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE1));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE2));
        returnedParameters.add(parameters.getParameter(OVERWRITE_MODE));

        if (parameters.getValue(OVERWRITE_MODE).equals(OverwriteModes.CREATE_NEW)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_32BIT));

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_METHOD));
        returnedParameters.add(parameters.getParameter(SET_NAN_TO_ZERO));

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
