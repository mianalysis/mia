// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package io.github.mianalysis.mia.module.images.process.threshold;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
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
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ImgPlus;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by sc13967 on 06/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ManualThreshold<T extends RealType<T> & NativeType<T>> extends Module {
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

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public ManualThreshold(Modules modules) {
        super("Manual threshold", modules);
        il2Support = IL2Support.FULL;
    }

    public static <T extends RealType<T> & NativeType<T>> void applyThreshold(Image image, double threshold,
            String binaryLogic) {
        ImgPlus<T> img = image.getImgPlus();

        int high = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND) ? 255 : 0;
        int low = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND) ? 0 : 255;

        LoopBuilder.setImages(img).forEachPixel(v -> v.setReal(v.getRealDouble() > threshold ? (int) high : (int) low));

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return "Binarises an image (or image stack) using a fixed intensity threshold.  The input threshold can be a single value (same for all images) or taken from a measurement associated with the image to be binarised.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        String thresholdSource = parameters.getValue(THRESHOLD_SOURCE);
        double thresholdValue = parameters.getValue(THRESHOLD_VALUE);
        String measurementName = parameters.getValue(MEASUREMENT);

        if (thresholdSource.equals(ThresholdSources.IMAGE_MEASUREMENT))
            thresholdValue = (int) Math.round(image.getMeasurement(measurementName).getValue());

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            image = image.duplicate(outputImageName);

        // Calculating the threshold based on the selected algorithm
        applyThreshold(image, thresholdValue, binaryLogic);

        // If necessary, adding output image to workspace
        if (!applyToInput)
            workspace.addImage(image);

        if (showOutput)
            image.showImage();

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
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

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
