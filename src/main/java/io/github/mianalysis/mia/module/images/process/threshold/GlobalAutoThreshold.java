// TODO: Enable each thresholding method

package io.github.mianalysis.mia.module.images.process.threshold;

import com.drew.lang.annotations.Nullable;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import net.imagej.ops.OpService;
import net.imglib2.histogram.BinMapper1d;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by sc13967 on 06/06/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GlobalAutoThreshold<T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String ALGORITHM = "Algorithm";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";
    public static final String BINARY_LOGIC = "Binary logic";
    public static final String MEASURE_ON_OBJECTS = "Measure on objects";
    public static final String INPUT_OBJECTS = "Input objects";

    public GlobalAutoThreshold(Modules modules) {
        super("Global auto-threshold", modules);
        il2Support = IL2Support.FULL;
    }

    public interface OutputModes {
        String CALCULATE_AND_APPLY = "Calculate and apply";
        String CALCULATE_ONLY = "Calculate only";

        String[] ALL = new String[] { CALCULATE_AND_APPLY, CALCULATE_ONLY };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface Algorithms {
        String HUANG = "Huang";
        String INTERMODES = "Intermodes";
        String ISO_DATA = "IsoData";
        String LI = "Li";
        String MAX_ENTROPY = "MaxEntropy";
        String MEAN = "Mean";
        String MIN_ERROR = "MinError";
        String MINIMUM = "Minimum";
        String MOMENTS = "Moments";
        String OTSU = "Otsu";
        String PERCENTILE = "Percentile";
        String RENYI_ENTROPY = "RenyiEntropy";
        String SHANBHAG = "Shanbhag";
        String TRIANGLE = "Triangle";
        String YEN = "Yen";

        String[] ALL = new String[] { HUANG, INTERMODES, ISO_DATA, LI, MAX_ENTROPY, MEAN, MIN_ERROR, MINIMUM, MOMENTS,
                OTSU, PERCENTILE, RENYI_ENTROPY, SHANBHAG, TRIANGLE, YEN };

    }

    public interface Measurements {
        String GLOBAL_VALUE = "GLOBAL";
    }

    public static String getFullName(String measurement, String method) {
        return "THRESHOLD // " + measurement + " " + method;
    }

    public Histogram1d<T> getHistogram(Image<T> image) {
        BinMapper1d<T> binMapper = new Real1dBinMapper<>(0, 255, 256, true);
        Histogram1d<T> histogram = new Histogram1d<>(image.getImgPlus(), binMapper);

        return histogram;

    }

    public Histogram1d<T> getHistogram(Image<T> image, Objs objects) {
        if (objects.size() == 0)
            return null;

        BinMapper1d<T> binMapper = new Real1dBinMapper<>(0, 255, 256, true);

        Obj firstObject = objects.getFirst();
        Histogram1d<T> histogram = new Histogram1d<T>(firstObject.getPixelIterable(image, 0), binMapper);

        for (Obj object : objects.values())
            if (object != firstObject) // Data for the first object has already been added
                histogram.addData(object.getPixelIterable(image, 0));

        return histogram;

    }

    public int calculateThreshold(Image<T> image, String algorithm, double thrMult, boolean useLowerLim,
            double lowerLim, @Nullable Objs objects) {
        OpService ops = MIA.ijService.getContext().getService(OpService.class);

        // Getting the histogram
        Histogram1d<T> histogram = objects == null ? getHistogram(image) : getHistogram(image, objects);

        T rawThreshold;
        switch (algorithm) {
            case Algorithms.HUANG:
            default:
                rawThreshold = ops.threshold().huang(histogram);
                break;
            case Algorithms.INTERMODES:
                rawThreshold = (T) ops.threshold().intermodes(histogram).get(0);
                break;
            case Algorithms.ISO_DATA:
                rawThreshold = (T) ops.threshold().isoData(histogram).get(0);
                break;
            case Algorithms.LI:
                rawThreshold = ops.threshold().li(histogram);
                break;
            case Algorithms.MAX_ENTROPY:
                rawThreshold = ops.threshold().maxEntropy(histogram);
                break;
            case Algorithms.MEAN:
                rawThreshold = ops.threshold().mean(histogram);
                break;
            case Algorithms.MINIMUM:
                rawThreshold = (T) ops.threshold().minimum(histogram).get(0);
                break;
            case Algorithms.MIN_ERROR:
                rawThreshold = (T) ops.threshold().minError(histogram).get(0);
                break;
            case Algorithms.MOMENTS:
                rawThreshold = ops.threshold().moments(histogram);
                break;
            case Algorithms.OTSU:
                rawThreshold = ops.threshold().otsu(histogram);
                break;
            case Algorithms.PERCENTILE:
                rawThreshold = ops.threshold().percentile(histogram);
                break;
            case Algorithms.RENYI_ENTROPY:
                rawThreshold = ops.threshold().renyiEntropy(histogram);
                break;
            case Algorithms.SHANBHAG:
                rawThreshold = ops.threshold().shanbhag(histogram);
                break;
            case Algorithms.TRIANGLE:
                rawThreshold = ops.threshold().triangle(histogram);
                break;
            case Algorithms.YEN:
                rawThreshold = ops.threshold().yen(histogram);
                break;
        }

        int threshold = (int) Math.round(rawThreshold.getRealDouble());

        // Applying limits, where applicable
        if (useLowerLim && threshold < lowerLim)
            threshold = (int) Math.round(lowerLim);

        // Applying threshold scaling
        threshold = (int) Math.round(threshold * thrMult);

        return threshold;

    }

    public void addMeasurements(Image<T> image, double threshold) {
        String method = parameters.getValue(ALGORITHM);
        String measurementName = getFullName(Measurements.GLOBAL_VALUE, method);

        image.addMeasurement(new Measurement(measurementName, threshold));

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return "Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the "
                + "built-in ImageJ global <a href=\"https://imagej.net/Auto_Threshold\">auto-thresholding algorithms</a>."
                + "<br>"
                + "<br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically "
                + "converted to 8-bit based on the \"" + ImageTypeConverter.ScalingModes.FILL
                + "\" scaling method from the " + "\"" + new ImageTypeConverter(null).getName() + "\" module.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(inputImageName);

        // Getting parameters
        String outputMode = parameters.getValue(OUTPUT_MODE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String algorithm = parameters.getValue(ALGORITHM);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT);
        boolean measureOnObjects = parameters.getValue(MEASURE_ON_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        Objs inputObjects = measureOnObjects ? workspace.getObjectSet(inputObjectsName) : null;

        // Calculating the threshold based on the selected algorithm
        int threshold = calculateThreshold(image, algorithm, thrMult, useLowerLim, lowerLim, inputObjects);

        if (outputMode.equals(OutputModes.CALCULATE_AND_APPLY)) {
            if (applyToInput) {
                ManualThreshold.applyThreshold(image, threshold, binaryLogic);

                addMeasurements(image, threshold);

                if (showOutput)
                    image.showImage();
                if (showOutput)
                    image.showMeasurements(this);

            } else {
                Image outputImage = ManualThreshold.applyThresholdCreate(image, threshold, binaryLogic,
                        outputImageName);
                workspace.addImage(outputImage);

                addMeasurements(outputImage, threshold);
                if (showOutput)
                    outputImage.showImage();
                if (showOutput)
                    outputImage.showMeasurements(this);

            }
        } else {
            addMeasurements(image, threshold);
            if (showOutput)
                image.showMeasurements(this);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CALCULATE_AND_APPLY, OutputModes.ALL));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(ALGORITHM, this, Algorithms.HUANG, Algorithms.ALL));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this, 1.0));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false));
        parameters.add(new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new BooleanP(MEASURE_ON_OBJECTS, this, false));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_MODE));

        if (parameters.getValue(OUTPUT_MODE).equals(OutputModes.CALCULATE_AND_APPLY)) {
            returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

            if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.add(parameters.getParameter(ALGORITHM));

        returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));
        if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT))
            returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));

        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

        returnedParameters.add(parameters.getParameter(MEASURE_ON_OBJECTS));
        if ((boolean) parameters.getValue(MEASURE_ON_OBJECTS))
            returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String imageName = "";
        switch ((String) parameters.getValue(OUTPUT_MODE)) {
            case OutputModes.CALCULATE_AND_APPLY:
                if ((boolean) parameters.getValue(APPLY_TO_INPUT)) {
                    imageName = parameters.getValue(INPUT_IMAGE);
                } else {
                    imageName = parameters.getValue(OUTPUT_IMAGE);
                }
                break;
            case OutputModes.CALCULATE_ONLY:
                imageName = parameters.getValue(INPUT_IMAGE);
                break;
        }

        String method = parameters.getValue(ALGORITHM);
        String measurementName = getFullName(Measurements.GLOBAL_VALUE, method);

        ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(measurementName);
        reference.setImageName(imageName);
        reference.setDescription(
                "Threshold value applied to the image during binarisation. Specified in intensity units.");
        returnedRefs.add(reference);

        return returnedRefs;

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

        parameters.get(OUTPUT_MODE).setDescription(
                "Controls if the threshold is applied to the input image or only calculated and stored as a measurement:<br><ul>"

                        + "<li>\"" + OutputModes.CALCULATE_AND_APPLY
                        + "\" Calculate the threshold and apply it to the input image.  Whether the binarised image updates the input image or is saved as a separate image to the workspace is controlled by the \""
                        + APPLY_TO_INPUT
                        + "\" parameter.  In this mode the calculated threshold is still stored as a measurement of the input image.</li>"

                        + "<li>\"" + OutputModes.CALCULATE_ONLY
                        + "\" Calculate the threshold, but do not apply it to the input image.  The calculated threshold is only stored as a measurement of the input image.</li>");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the thresholding process.  This image will be added to the workspace.");

        parameters.get(ALGORITHM).setDescription(
                "Global thresholding algorithm to use.  Choices are: " + String.join(", ", Algorithms.ALL) + ".");

        parameters.get(THRESHOLD_MULTIPLIER).setDescription(
                "Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a \""
                        + THRESHOLD_MULTIPLIER
                        + "\" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180.");

        parameters.get(USE_LOWER_THRESHOLD_LIMIT).setDescription(
                "Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present).");

        parameters.get(LOWER_THRESHOLD_LIMIT).setDescription("Lowest absolute threshold value that can be applied.");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
