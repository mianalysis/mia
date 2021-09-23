// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package io.github.mianalysis.mia.module.imageprocessing.pixel.threshold;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.imageprocessing.pixel.InvertIntensity;
import io.github.mianalysis.mia.module.imageprocessing.stack.ImageTypeConverter;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 06/06/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ManualThreshold extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLD_SOURCE = "Threshold source";
    public static final String THRESHOLD_VALUE = "Threshold value";
    public static final String MEASUREMENT = "Measurement";
    public static final String BINARY_LOGIC = "Binary logic";

    public interface ThresholdSources {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";

        String[] ALL = new String[] { FIXED_VALUE, IMAGE_MEASUREMENT };

    }

    public interface BinaryLogic extends BinaryLogicInterface {}

    public ManualThreshold(Modules modules) {
        super("Manual threshold", modules);
    }

    public static void applyThreshold(ImagePlus inputImagePlus, double threshold) {
        // Creating an integer threshold in case image is 8 or 16 bit
        int intThreshold = (int) Math.round(threshold);

        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    if (inputImagePlus.getBitDepth() == 32) {                        
                        for (int x = 0; x < inputImagePlus.getWidth(); x++) {
                            for (int y = 0; y < inputImagePlus.getHeight(); y++) {
                                float val = inputImagePlus.getProcessor().getf(x, y);
                                val = val <= threshold ? 0 : 255;
                                inputImagePlus.getProcessor().setf(x, y, val);
                            }
                        }
                    } else {
                        inputImagePlus.getProcessor().threshold(intThreshold);
                    }
                }
            }
        }

        inputImagePlus.setPosition(1, 1, 1);

        // If the input was 32-bit we can now convert it to 8-bit
        if (inputImagePlus.getBitDepth() == 32)
            ImageTypeConverter.process(inputImagePlus, 8, ImageTypeConverter.ScalingModes.CLIP);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return "Binarises an image (or image stack) using a fixed intensity threshold.  The input threshold can be a single value (same for all images) or taken from a measurement associated with the image to be binarised.";
        
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        String thresholdSource = parameters.getValue(THRESHOLD_SOURCE);
        double thresholdValue = parameters.getValue(THRESHOLD_VALUE);
        String measurementName = parameters.getValue(MEASUREMENT);

        if (thresholdSource.equals(ThresholdSources.IMAGE_MEASUREMENT)) {
            thresholdValue = (int) Math.round(inputImage.getMeasurement(measurementName).getValue());
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImagePlus = new Duplicator().run(inputImagePlus);
        }

        // Calculating the threshold based on the selected algorithm
        applyThreshold(inputImagePlus, thresholdValue);

        if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
                InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            if (showOutput)
                inputImage.showImage();

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName, inputImagePlus);
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
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(THRESHOLD_SOURCE, this, ThresholdSources.FIXED_VALUE, ThresholdSources.ALL));
        parameters.add(new DoubleP(THRESHOLD_VALUE, this, 1d));
        parameters.add(new ImageMeasurementP(MEASUREMENT, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

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

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_SOURCE));

        switch ((String) parameters.getValue(THRESHOLD_SOURCE)) {
            case ThresholdSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(THRESHOLD_VALUE));
                break;
            case ThresholdSources.IMAGE_MEASUREMENT:
                ImageMeasurementP parameter = parameters.getParameter(MEASUREMENT);
                parameter.setImageName(parameters.getValue(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
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
        parameters.get(INPUT_IMAGE).setDescription("Image to apply threshold to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the thresholding process.  This image will be added to the workspace.");

        parameters.get(THRESHOLD_SOURCE).setDescription("Source for the threshold value:<br><ul>"

                + "<li>\"" + ThresholdSources.FIXED_VALUE
                + "\" Uses a single, fixed value for all images.  This value is specified using the \""
                + THRESHOLD_VALUE + "\" parameter.</li>"

                + "<li>\"" + ThresholdSources.IMAGE_MEASUREMENT
                + "\" Threshold is set to the value of a measurement assoiated with the image to be binarised.  Measurement selected by \""
                + MEASUREMENT
                + "\" parameter.  In this mode a different threshold can be applied to each image.</li></ul>");

        parameters.get(THRESHOLD_VALUE)
                .setDescription("Absolute manual threshold value that will be applied to all pixels.");

        parameters.get(MEASUREMENT).setDescription(
                "Measurement to act as threshold value when in \"" + ThresholdSources.IMAGE_MEASUREMENT + "\" mode.");

                parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
