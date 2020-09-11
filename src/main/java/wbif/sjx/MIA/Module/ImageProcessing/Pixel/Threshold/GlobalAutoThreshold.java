// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Threshold;

import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class GlobalAutoThreshold extends Module {
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
    public static final String WHITE_BACKGROUND = "Black objects/white background";

    public GlobalAutoThreshold(ModuleCollection modules) {
        super("Global auto-threshold", modules);
    }

    public interface OutputModes {
        String CALCULATE_AND_APPLY = "Calculate and apply";
        String CALCULATE_ONLY = "Calculate only";

        String[] ALL = new String[] { CALCULATE_AND_APPLY, CALCULATE_ONLY };

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
            double lowerLim) {
        // Compiling stack histogram. This is stored as long to prevent the Integer
        // overflowing.
        long[] histogram = null;
        int count = 0;
        int total = inputImagePlus.getNChannels() * inputImagePlus.getNSlices() * inputImagePlus.getNFrames();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    writeStatus("Processing image " + (++count) + " of " + total);
                    inputImagePlus.setPosition(c, z, t);

                    int[] tempHist = inputImagePlus.getProcessor().getHistogram();

                    if (histogram == null)
                        histogram = new long[tempHist.length];
                    for (int i = 0; i < histogram.length; i++)
                        histogram[i] = histogram[i] + tempHist[i];

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

    public void addMeasurements(Image image, double threshold) {
        String method = parameters.getValue(ALGORITHM);
        String measurementName = getFullName(Measurements.GLOBAL_VALUE, method);

        image.addMeasurement(new Measurement(measurementName, threshold));

    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return "Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the "
                + "built-in ImageJ global auto-thresholding algorithms (https://imagej.net/Auto_Threshold)." + "<br>"
                + "<br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically "
                + "converted to 8-bit based on the \"" + ImageTypeConverter.ScalingModes.FILL
                + "\" scaling method from the " + "\"" + new ImageTypeConverter(null).getName() + "\" module.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        String outputMode = parameters.getValue(OUTPUT_MODE);
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String algorithm = parameters.getValue(ALGORITHM);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT);

        int threshold = 0;

        Prefs.blackBackground = !whiteBackground;

        // Image must be 8-bit
        if (inputImagePlus.getBitDepth() != 8) {
            ImageTypeConverter.applyConversion(inputImagePlus, 8, ImageTypeConverter.ScalingModes.FILL);
        }

        // Calculating the threshold based on the selected algorithm
        writeStatus("Applying " + algorithm + " threshold (multiplier = " + thrMult + " x)");
        threshold = calculateThreshold(inputImagePlus, algorithm, thrMult, useLowerLim, lowerLim);

        if (outputMode.equals(OutputModes.CALCULATE_AND_APPLY)) {
            // If applying to a new image, the input image is duplicated
            if (!applyToInput)
                inputImagePlus = new Duplicator().run(inputImagePlus);

            // Applying threshold
            ManualThreshold.applyThreshold(inputImagePlus, threshold);

            if (whiteBackground)
                InvertIntensity.process(inputImagePlus);

            // If the image is being saved as a new image, adding it to the workspace
            if (applyToInput) {
                addMeasurements(inputImage, threshold);
                if (showOutput)
                    inputImage.showImage();
                if (showOutput)
                    inputImage.showMeasurements(this);

            } else {
                String outputImageName = parameters.getValue(OUTPUT_IMAGE);
                Image outputImage = new Image(outputImageName, inputImagePlus);
                workspace.addImage(outputImage);

                addMeasurements(outputImage, threshold);
                if (showOutput)
                    outputImage.showImage();
                if (showOutput)
                    outputImage.showMeasurements(this);
            }
        } else {
            addMeasurements(inputImage, threshold);
            if (showOutput)
                inputImage.showMeasurements(this);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, ""));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CALCULATE_AND_APPLY, OutputModes.ALL));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, ""));

        parameters.add(new ParamSeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(ALGORITHM, this, Algorithms.HUANG, Algorithms.ALL));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this, 1.0));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false));
        parameters.add(new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0));
        parameters.add(new BooleanP(WHITE_BACKGROUND, this, true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
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
        if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
            returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

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
                "Global thresholding algorithm to use.  Options are: " + String.join(", ", Algorithms.ALL) + ".");

        parameters.get(THRESHOLD_MULTIPLIER).setDescription(
                "Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a \""
                        + THRESHOLD_MULTIPLIER
                        + "\" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180.");

        parameters.get(USE_LOWER_THRESHOLD_LIMIT).setDescription(
                "Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present).");

        parameters.get(LOWER_THRESHOLD_LIMIT).setDescription("Lowest absolute threshold value that can be applied.");

        parameters.get(WHITE_BACKGROUND).setDescription(
                "Controls the logic of the output image in terms of what is considered foreground and background.");

    }
}
