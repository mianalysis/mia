// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package io.github.mianalysis.mia.module.images.process.threshold;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.threshold.Auto_Local_Threshold;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.SpatialUnitsInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.imagej.AutoLocalThreshold3D;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
* Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the built-in ImageJ global and 2D local auto-thresholding algorithms.<br><br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically converted to 8-bit based on the "Fill target range (normalise)" scaling method from the "Image type converter" module.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ThresholdImage extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image to apply threshold to.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* Name of the output image created during the thresholding process.  This image will be added to the workspace.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String THRESHOLD_SEPARATOR = "Threshold controls";

	/**
	* Class of threshold to be applied.<br><br> - "Global" (default) will apply a constant, automatically-determined threshold value to all pixels in the image.  This is best when the image is uniformly illuminated.<br><br> - " Local" will apply a variable threshold to each pixel in the image based on the local intensity around that pixel.  This is best when one region of the image is brighter than another, for example, due to heterogeneous illumination.  The size of the local region is determined by the user.<br><br> - " Manual" will apply a fixed threshold value to all pixels in the image.<br>
	*/
    public static final String THRESHOLD_TYPE = "Threshold type";

	/**
	* Global thresholding algorithm to use.
	*/
    public static final String GLOBAL_ALGORITHM = "Global threshold algorithm";

	/**
	* Local thresholding algorithm to use.
	*/
    public static final String LOCAL_ALGORITHM = "Local threshold algorithm";

	/**
	* Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a "Threshold multiplier" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180.
	*/
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";

	/**
	* Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present).
	*/
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";

	/**
	* Lowest absolute threshold value that can be applied.
	*/
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";

	/**
	* Radius of region to be used when calculating local intensity thresholds.  Units controlled by "Spatial units mode" control.
	*/
    public static final String LOCAL_RADIUS = "Local radius";

	/**
	* Controls whether spatial values are assumed to be specified in calibrated units (as defined by the "Input control" parameter "Spatial unit") or pixel units.
	*/
    public static final String SPATIAL_UNITS_MODE = "Spatial units mode";

	/**
	* Absolute manual threshold value that will be applied to all pixels.
	*/
    public static final String THRESHOLD_VALUE = "Threshold value";
    public static final String USE_GLOBAL_Z = "Use full Z-range (\"Global Z\")";

	/**
	* Controls the logic of the output image in terms of what is considered foreground and background.
	*/
    public static final String WHITE_BACKGROUND = "Black objects/white background";

    public ThresholdImage(Modules modules) {
        super("Threshold image", modules);
        deprecated = true;
    }

    public interface ThresholdTypes {
        String GLOBAL = "Global";
        String LOCAL = "Local";
        String MANUAL = "Manual";

        String[] ALL = new String[] { GLOBAL, LOCAL, MANUAL };

    }

    public interface GlobalAlgorithms {
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

    public interface LocalAlgorithms {
        String BERNSEN_3D = "Bernsen (3D)";
        String CONTRAST_3D = "Contrast (3D)";
        String MEAN_3D = "Mean (3D)";
        String MEDIAN_3D = "Median (3D)";
        String PHANSALKAR_3D = "Phansalkar (3D)";
        String PHANSALKAR_SLICE = "Phansalkar (slice-by-slice)";

        String[] ALL = new String[] { BERNSEN_3D, CONTRAST_3D, MEAN_3D, MEDIAN_3D, PHANSALKAR_3D, PHANSALKAR_SLICE };

    }

    public interface SpatialUnitModes extends SpatialUnitsInterface {
    }

    public interface Measurements {
        String GLOBAL_VALUE = "GLOBAL";
    }

    public static String getFullName(String measurement, String method) {
        return "THRESHOLD // " + measurement + " " + method;
    }

    public int runGlobalThresholdOnStack(ImagePlus inputImagePlus, String algorithm, double thrMult,
            boolean useLowerLim, double lowerLim) {
        // Compiling stack histogram. This is stored as long to prevent the Integer
        // overflowing.
        long[] histogram = null;
        int count = 0;
        int total = inputImagePlus.getNChannels() * inputImagePlus.getNSlices() * inputImagePlus.getNFrames();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);

                    int[] tempHist = inputImagePlus.getProcessor().getHistogram();

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
        applyGlobalThresholdToStack(inputImagePlus, threshold);

        return threshold;

    }

    public void applyGlobalThresholdToStack(ImagePlus inputImagePlus, int threshold) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    inputImagePlus.getProcessor().threshold(threshold);
                }
            }
        }

        inputImagePlus.setPosition(1, 1, 1);
    }

    public void applyLocalThresholdToStack(ImagePlus inputImagePlus, String algorithm, double localRadius) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    Object[] results = new Auto_Local_Threshold().exec(inputImagePlus, algorithm, (int) localRadius, 0,
                            0, true);
                    inputImagePlus.setProcessor(((ImagePlus) results[0]).getProcessor());

                }
            }
        }
        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateAndDraw();
    }

    public void applyLocalThreshold3D(ImagePlus inputImagePlus, String algorithm, double localRadius, double thrMult,
            boolean useLowerLim, double lowerLim, boolean globalZ) {

        double localRadiusZ;
        if (globalZ) {
            localRadiusZ = inputImagePlus.getNSlices() / 2;
        } else {
            localRadiusZ = localRadius * inputImagePlus.getCalibration().pixelWidth
                    / inputImagePlus.getCalibration().pixelDepth;
        }

        AutoLocalThreshold3D alt3D = new AutoLocalThreshold3D();
        if (useLowerLim)
            alt3D.setLowerThreshold((int) lowerLim);

        alt3D.exec(inputImagePlus, algorithm, (int) Math.round(localRadius), (int) Math.round(localRadiusZ), thrMult, 0,
                0, true);

    }

    public void addGlobalThresholdMeasurement(ImageI image, double threshold, String algorithm) {
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
                +
                "built-in ImageJ global and 2D local auto-thresholding algorithms." +
                "<br>" +
                "<br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically " +
                "converted to 8-bit based on the \"" + ImageTypeConverter.ScalingModes.FILL
                + "\" scaling method from the " +
                "\"" + new ImageTypeConverter(null).getName() + "\" module.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String thresholdType = parameters.getValue(THRESHOLD_TYPE, workspace);
        String globalThresholdAlgorithm = parameters.getValue(GLOBAL_ALGORITHM, workspace);
        String localThresholdAlgorithm = parameters.getValue(LOCAL_ALGORITHM, workspace);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER, workspace);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND, workspace);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT, workspace);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT, workspace);
        double localRadius = parameters.getValue(LOCAL_RADIUS, workspace);
        int thresholdValue = parameters.getValue(THRESHOLD_VALUE, workspace);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE, workspace);
        boolean useGlobalZ = parameters.getValue(USE_GLOBAL_Z, workspace);

        int threshold = 0;

        if (spatialUnits.equals(SpatialUnitModes.CALIBRATED)) {
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        }

        Prefs.blackBackground = !whiteBackground;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImagePlus = new Duplicator().run(inputImagePlus);
        }

        // Image must be 8-bit
        if (!thresholdType.equals(ThresholdTypes.MANUAL) && inputImagePlus.getBitDepth() != 8) {
            ImageTypeConverter.process(inputImagePlus, 8, ImageTypeConverter.ScalingModes.FILL);
        }

        // Calculating the threshold based on the selected algorithm
        switch (thresholdType) {
            case ThresholdTypes.GLOBAL:
                writeStatus(
                        "Applying global " + globalThresholdAlgorithm + " threshold (multiplier = " + thrMult + " x)");
                threshold = runGlobalThresholdOnStack(inputImagePlus, globalThresholdAlgorithm, thrMult, useLowerLim,
                        lowerLim);

                break;

            case ThresholdTypes.LOCAL:
                switch (localThresholdAlgorithm) {
                    case LocalAlgorithms.BERNSEN_3D:
                        writeStatus("Applying local Bernsen threshold (radius = " + localRadius + " px)");
                        applyLocalThreshold3D(inputImagePlus, AutoLocalThreshold3D.BERNSEN, localRadius, thrMult,
                                useLowerLim, lowerLim, useGlobalZ);
                        break;

                    case LocalAlgorithms.CONTRAST_3D:
                        writeStatus("Applying local Contrast threshold (radius = " + localRadius + " px)");
                        applyLocalThreshold3D(inputImagePlus, AutoLocalThreshold3D.CONTRAST, localRadius, thrMult,
                                useLowerLim, lowerLim, useGlobalZ);
                        break;

                    case LocalAlgorithms.MEAN_3D:
                        writeStatus("Applying local Mean threshold (radius = " + localRadius + " px)");
                        applyLocalThreshold3D(inputImagePlus, AutoLocalThreshold3D.MEAN, localRadius, thrMult,
                                useLowerLim,
                                lowerLim, useGlobalZ);
                        break;

                    case LocalAlgorithms.MEDIAN_3D:
                        writeStatus("Applying local Median threshold (radius = " + localRadius + " px)");
                        applyLocalThreshold3D(inputImagePlus, AutoLocalThreshold3D.MEDIAN, localRadius, thrMult,
                                useLowerLim,
                                lowerLim, useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_3D:
                        writeStatus("Applying local Phansalkar threshold (radius = " + localRadius + " px)");
                        applyLocalThreshold3D(inputImagePlus, AutoLocalThreshold3D.PHANSALKAR, localRadius, thrMult,
                                useLowerLim, lowerLim, useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_SLICE:
                        writeStatus("Applying local Phansalkar threshold (radius = " + localRadius + " px)");
                        applyLocalThresholdToStack(inputImagePlus, "Phansalkar", localRadius);
                        break;

                }
                break;

            case ThresholdTypes.MANUAL:
                applyGlobalThresholdToStack(inputImagePlus, thresholdValue);
                break;

        }

        if (whiteBackground)
            InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            if (showOutput)
                inputImage.showAsIs();

            if (thresholdType.equals(ThresholdTypes.GLOBAL))
                addGlobalThresholdMeasurement(inputImage, threshold, globalThresholdAlgorithm);

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
            ImageI outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showAsIs();

            if (thresholdType.equals(ThresholdTypes.GLOBAL))
                addGlobalThresholdMeasurement(outputImage, threshold, globalThresholdAlgorithm);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply threshold to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true,
                "Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "",
                "Name of the output image created during the thresholding process.  This image will be added to the workspace."));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR, this));
        parameters.add(new ChoiceP(THRESHOLD_TYPE, this, ThresholdTypes.GLOBAL, ThresholdTypes.ALL,
                "Class of threshold to be applied.<br>" +
                        "<br> - \"" + ThresholdTypes.GLOBAL
                        + "\" (default) will apply a constant, automatically-determined threshold value to all pixels in the image.  This is best when the image is uniformly illuminated.<br>"
                        +
                        "<br> - \" " + ThresholdTypes.LOCAL
                        + "\" will apply a variable threshold to each pixel in the image based on the local intensity around that pixel.  This is best when one region of the image is brighter than another, for example, due to heterogeneous illumination.  The size of the local region is determined by the user.<br>"
                        +
                        "<br> - \" " + ThresholdTypes.MANUAL
                        + "\" will apply a fixed threshold value to all pixels in the image.<br>"));
        parameters.add(new ChoiceP(GLOBAL_ALGORITHM, this, GlobalAlgorithms.HUANG, GlobalAlgorithms.ALL,
                "Global thresholding algorithm to use."));
        parameters.add(new ChoiceP(LOCAL_ALGORITHM, this, LocalAlgorithms.PHANSALKAR_3D, LocalAlgorithms.ALL,
                "Local thresholding algorithm to use."));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this, 1.0,
                "Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a \""
                        + THRESHOLD_MULTIPLIER
                        + "\" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180."));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false,
                "Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present)."));
        parameters.add(
                new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0, "Lowest absolute threshold value that can be applied."));
        parameters.add(new DoubleP(LOCAL_RADIUS, this, 1.0,
                "Radius of region to be used when calculating local intensity thresholds.  Units controlled by \""
                        + SPATIAL_UNITS_MODE + "\" control."));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitModes.PIXELS, SpatialUnitModes.ALL,
                SpatialUnitsInterface.getDescription()));
        parameters.add(new IntegerP(THRESHOLD_VALUE, this, 1,
                "Absolute manual threshold value that will be applied to all pixels."));
        parameters.add(new BooleanP(USE_GLOBAL_Z, this, false,
                "When performing 3D local thresholding, this takes all z-values at a location into account.  If disabled, pixels will be sampled in z according to the \""
                        + LOCAL_RADIUS + "\" setting."));
        parameters.add(new BooleanP(WHITE_BACKGROUND, this, true,
                "Controls the logic of the output image in terms of what is considered foreground and background."));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_TYPE));

        switch ((String) parameters.getValue(THRESHOLD_TYPE, workspace)) {
            case ThresholdTypes.GLOBAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
                returnedParameters.add(parameters.getParameter(GLOBAL_ALGORITHM));
                break;

            case ThresholdTypes.LOCAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
                returnedParameters.add(parameters.getParameter(LOCAL_ALGORITHM));
                returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS_MODE));
                returnedParameters.add(parameters.getParameter(USE_GLOBAL_Z));
                break;

            case ThresholdTypes.MANUAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_VALUE));
                break;

        }

        // If using an automatic threshold algorithm, we can set a lower threshold limit
        if (!parameters.getValue(THRESHOLD_TYPE, workspace).equals(ThresholdTypes.MANUAL)) {
            returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));
            if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT, workspace)) {
                returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
            }
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        if (parameters.getValue(THRESHOLD_TYPE, workspace).equals(ThresholdTypes.GLOBAL)) {
            String imageName = (boolean) parameters.getValue(APPLY_TO_INPUT, workspace)
                    ? parameters.getValue(INPUT_IMAGE, workspace)
                    : parameters.getValue(OUTPUT_IMAGE, workspace);
            String method = parameters.getValue(GLOBAL_ALGORITHM, workspace);
            String measurementName = getFullName(Measurements.GLOBAL_VALUE, method);

            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(measurementName);
            reference.setImageName(imageName);
            reference.setDescription(
                    "Threshold value applied to the image during binarisation. Specified in intensity units.");
            returnedRefs.add(reference);

        }

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
}
