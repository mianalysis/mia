package io.github.mianalysis.mia.module.images.process.threshold;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
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
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
 * Binarise an image in the workspace such that the output only has pixel values
 * of 0 and 255. Uses the built-in ImageJ global
 * <a href="https://imagej.net/Auto_Threshold">auto-thresholding
 * algorithms</a>.<br>
 * <br>
 * Note: Currently only works on 8-bit images. Images with other bit depths will
 * be automatically converted to 8-bit based on the "Fill target range
 * (normalise)" scaling method from the "Image type converter" module.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GlobalAutoThreshold extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
     * Image to apply threshold to.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Controls if the threshold is applied to the input image or only calculated
     * and stored as a measurement:<br>
     * <ul>
     * <li>"Calculate and apply" Calculate the threshold and apply it to the input
     * image. Whether the binarised image updates the input image or is saved as a
     * separate image to the workspace is controlled by the "Apply to input image"
     * parameter. In this mode the calculated threshold is still stored as a
     * measurement of the input image.</li>
     * <li>"Calculate only" Calculate the threshold, but do not apply it to the
     * input image. The calculated threshold is only stored as a measurement of the
     * input image.</li>
     * </ul>
     */
    public static final String OUTPUT_MODE = "Output mode";

    /**
     * Select if the threshold should be applied directly to the input image, or if
     * it should be applied to a duplicate, then stored as a different image in the
     * workspace.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * Name of the output image created during the thresholding process. This image
     * will be added to the workspace.
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String THRESHOLD_SEPARATOR = "Threshold controls";

    /**
     * Global thresholding algorithm to use. Choices are: Huang, Intermodes,
     * IsoData, Li, MaxEntropy, Mean, MinError, Minimum, Moments, Otsu, Percentile,
     * RenyiEntropy, Shanbhag, Triangle, Yen.
     */
    public static final String ALGORITHM = "Algorithm";

    /**
     * Prior to application of automatically-calculated thresholds the threshold
     * value is multiplied by this value. This allows the threshold to be
     * systematically increased or decreased. For example, a "Threshold multiplier"
     * of 0.9 applied to an automatically-calculated threshold of 200 will see the
     * image thresholded at the level 180.
     */
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";

    /**
     * Limit the lowest threshold that can be applied to the image. This is used to
     * prevent unintentional segmentation of an image containing only background
     * (i.e. no features present).
     */
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";

    /**
     * Lowest absolute threshold value that can be applied.
     */
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";

    /**
     * Controls whether objects are considered to be white (255 intensity) on a
     * black (0 intensity) background, or black on a white background.
     */
    public static final String BINARY_LOGIC = "Binary logic";

    /**
    * 
    */
    public static final String MEASURE_ON_OBJECTS = "Measure on objects";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    public GlobalAutoThreshold(Modules modules) {
        super("Global auto-threshold", modules);
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

    public int calculateThreshold(ImagePlus inputImagePlus, String algorithm, double thrMult, boolean useLowerLim,
            double lowerLim, Objs inputObjects) {

        // Image must be 8-bit
        if (inputImagePlus.getBitDepth() != 8) {
            inputImagePlus = inputImagePlus.duplicate();
            ImageTypeConverter.process(inputImagePlus, 8, ImageTypeConverter.ScalingModes.FILL);
        }

        // Compiling stack histogram. This is stored as long to prevent the Integer
        // overflowing.
        long[] histogram = null;
        int count = 0;
        int total = inputImagePlus.getNChannels() * inputImagePlus.getNSlices() * inputImagePlus.getNFrames();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    ImageProcessor ipr = inputImagePlus.getProcessor();

                    if (inputObjects != null)
                        ipr.setRoi(getRoi(inputObjects, t, z));

                    int[] tempHist = ipr.getHistogram();

                    if (inputObjects != null)
                        inputImagePlus.killRoi();

                    if (histogram == null)
                        histogram = new long[tempHist.length];
                    for (int i = 0; i < histogram.length; i++)
                        histogram[i] = histogram[i] + tempHist[i];

                    writeProgressStatus(++count, total, "images");

                }
            }
        }

        if (histogram == null)
            return 0;

        // Calculating the maximum value in any bin
        long maxVal = Long.MIN_VALUE;
        for (long val : histogram)
            maxVal = Math.max(maxVal, val);
        if (maxVal <= Integer.MAX_VALUE)
            maxVal = Integer.MAX_VALUE;

        // Normalising histogram, so it will fit in an int[]
        int[] normHist = new int[histogram.length];
        for (int i = 0; i < histogram.length; i++)
            normHist[i] = (int) Math.round(Integer.MAX_VALUE * ((double) histogram[i]) / ((double) maxVal));

        // Applying the threshold
        int threshold = new AutoThresholder().getThreshold(algorithm, normHist);

        // Applying limits, where applicable
        if (useLowerLim && threshold < lowerLim)
            threshold = (int) Math.round(lowerLim);

        // Applying threshold scaling
        threshold = (int) Math.round(threshold * thrMult);

        return threshold;

    }

    public static Roi getRoi(Objs inputObjects, int t, int z) {
        ShapeRoi roi = null;

        for (Obj inputObject : inputObjects.values()) {
            if (inputObject.getT() != t - 1)
                continue;

            double[][] extents = inputObject.getExtents(true, false);
            if ((z - 1) < extents[2][0] || (z - 1) > extents[2][1])
                continue;

            Roi currRoi = inputObject.getRoi(z - 1);

            if (currRoi == null)
                continue;

            if (roi == null)
                roi = new ShapeRoi(currRoi);
            else
                roi.xor(new ShapeRoi(currRoi));

        }

        return roi;

    }

    public void addMeasurements(ImageI image, double threshold, String algorithm) {
        String measurementName = getFullName(Measurements.GLOBAL_VALUE, algorithm);

        image.addMeasurement(new Measurement(measurementName, threshold));

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_THRESHOLD;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
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
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputMode = parameters.getValue(OUTPUT_MODE, workspace);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String algorithm = parameters.getValue(ALGORITHM, workspace);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT, workspace);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT, workspace);
        boolean measureOnObjects = parameters.getValue(MEASURE_ON_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Objs inputObjects = measureOnObjects ? workspace.getObjects(inputObjectsName) : null;
        int threshold = 0;

        // Calculating the threshold based on the selected algorithm
        writeStatus("Applying " + algorithm + " threshold (multiplier = " + thrMult + " x)");
        threshold = calculateThreshold(inputImagePlus, algorithm, thrMult, useLowerLim, lowerLim, inputObjects);

        if (outputMode.equals(OutputModes.CALCULATE_AND_APPLY)) {
            // If applying to a new image, the input image is duplicated
            if (!applyToInput)
                inputImagePlus = new Duplicator().run(inputImagePlus);

            // Image must be 8-bit
            if (inputImagePlus.getBitDepth() != 8)
                ImageTypeConverter.process(inputImagePlus, 8, ImageTypeConverter.ScalingModes.FILL);

            // Applying threshold
            ManualThreshold.applyThreshold(inputImagePlus, threshold);

            if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
                InvertIntensity.process(inputImagePlus);

            // If the image is being saved as a new image, adding it to the workspace
            if (applyToInput) {
                inputImage.setImagePlus(inputImagePlus);
                addMeasurements(inputImage, threshold, algorithm);
                if (showOutput)
                    inputImage.showAsIs();
                if (showOutput)
                    inputImage.showMeasurements(this);

            } else {
                String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
                ImageI outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
                workspace.addImage(outputImage);

                addMeasurements(outputImage, threshold, algorithm);
                if (showOutput)
                    outputImage.showAsIs();
                if (showOutput)
                    outputImage.showMeasurements(this);
            }
        } else {
            addMeasurements(inputImage, threshold, algorithm);
            if (showOutput)
                inputImage.showMeasurements(this);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
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
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_MODE));

        if (parameters.getValue(OUTPUT_MODE, workspace).equals(OutputModes.CALCULATE_AND_APPLY)) {
            returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

            if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
        returnedParameters.add(parameters.getParameter(ALGORITHM));

        returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));
        if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT, workspace))
            returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));

        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

        returnedParameters.add(parameters.getParameter(MEASURE_ON_OBJECTS));
        if ((boolean) parameters.getValue(MEASURE_ON_OBJECTS, workspace))
            returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String imageName = "";
        switch ((String) parameters.getValue(OUTPUT_MODE, workspace)) {
            case OutputModes.CALCULATE_AND_APPLY:
                if ((boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
                    imageName = parameters.getValue(INPUT_IMAGE, workspace);
                } else {
                    imageName = parameters.getValue(OUTPUT_IMAGE, workspace);
                }
                break;
            case OutputModes.CALCULATE_ONLY:
                imageName = parameters.getValue(INPUT_IMAGE, workspace);
                break;
        }

        String method = parameters.getValue(ALGORITHM, workspace);
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
