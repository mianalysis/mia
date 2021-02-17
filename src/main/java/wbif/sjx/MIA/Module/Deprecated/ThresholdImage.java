// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package wbif.sjx.MIA.Module.Deprecated;

import fiji.threshold.Auto_Local_Threshold;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Filters.AutoLocalThreshold3D;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ThresholdImage extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    
    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLD_TYPE = "Threshold type";
    public static final String GLOBAL_ALGORITHM = "Global threshold algorithm";
    public static final String LOCAL_ALGORITHM = "Local threshold algorithm";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String THRESHOLD_VALUE = "Threshold value";
    public static final String USE_GLOBAL_Z = "Use full Z-range (\"Global Z\")";
    public static final String WHITE_BACKGROUND = "Black objects/white background";


    public ThresholdImage(ModuleCollection modules) {
        super("Threshold image",modules);
    }

    public interface OutputModes {
        String CALCULATE_ONLY = "Calculate only";
        String CALCULATE_AND_APPLY = "Calculate and apply";

    }

    public interface ThresholdTypes {
        String GLOBAL = "Global";
        String LOCAL = "Local";
        String MANUAL = "Manual";

        String[] ALL = new String[]{GLOBAL, LOCAL, MANUAL};

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

        String[] ALL = new String[]{HUANG, INTERMODES, ISO_DATA, LI, MAX_ENTROPY, MEAN, MIN_ERROR, MINIMUM, MOMENTS, OTSU, PERCENTILE, RENYI_ENTROPY, SHANBHAG, TRIANGLE, YEN};

    }

    public interface LocalAlgorithms {
        String BERNSEN_3D = "Bernsen (3D)";
        String CONTRAST_3D = "Contrast (3D)";
        String MEAN_3D = "Mean (3D)";
        String MEDIAN_3D = "Median (3D)";
        String PHANSALKAR_3D = "Phansalkar (3D)";
        String PHANSALKAR_SLICE = "Phansalkar (slice-by-slice)";

        String[] ALL = new String[]{BERNSEN_3D,CONTRAST_3D,MEAN_3D,MEDIAN_3D,PHANSALKAR_3D,PHANSALKAR_SLICE};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }

    public interface Measurements{
        String GLOBAL_VALUE = "GLOBAL";
    }


    public static String getFullName(String measurement, String method) {
        return  "THRESHOLD // "+measurement+" "+method;
    }

    public int runGlobalThresholdOnStack(ImagePlus inputImagePlus, String algorithm, double thrMult,
                                         boolean useLowerLim, double lowerLim) {
        // Compiling stack histogram.  This is stored as long to prevent the Integer overflowing.
        long[] histogram = null;
        int count = 0;
        int total = inputImagePlus.getNChannels()*inputImagePlus.getNSlices()*inputImagePlus.getNFrames();
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    writeStatus("Processing image " + (++count) + " of " + total);
                    inputImagePlus.setPosition(c, z, t);

                    int[] tempHist = inputImagePlus.getProcessor().getHistogram();

                    if (histogram == null) histogram = new long[tempHist.length];
                    for (int i=0;i<histogram.length;i++) histogram[i] = histogram[i] + tempHist[i];

                }
            }
        }

        if (histogram == null) return 0;

        // Calculating the maximum value in any bin
        long maxVal = Long.MIN_VALUE;
        for (long val:histogram) maxVal = Math.max(maxVal,val);
        if (maxVal <= Integer.MAX_VALUE) maxVal = Integer.MAX_VALUE;

        // Normalising histogram, so it will fit in an int[]
        int[] normHist = new int[histogram.length];
        for (int i=0;i<histogram.length;i++) normHist[i] = (int) Math.round(Integer.MAX_VALUE*((double) histogram[i])/((double) maxVal));

        // Applying the threshold
        int threshold = new AutoThresholder().getThreshold(algorithm,normHist);

        // Applying limits, where applicable
        if (useLowerLim && threshold < lowerLim) threshold = (int) Math.round(lowerLim);

        // Applying threshold scaling
        threshold = (int) Math.round(threshold*thrMult);
        applyGlobalThresholdToStack(inputImagePlus,threshold);

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

        inputImagePlus.setPosition(1,1,1);
    }

    public void applyLocalThresholdToStack(ImagePlus inputImagePlus, String algorithm, double localRadius) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    Object[] results = new Auto_Local_Threshold().exec(inputImagePlus,algorithm,(int) localRadius,0,0,true);
                    inputImagePlus.setProcessor(((ImagePlus) results[0]).getProcessor());

                }
            }
        }
        inputImagePlus.setPosition(1,1,1);
        inputImagePlus.updateAndDraw();
    }

    public void applyLocalThreshold3D(ImagePlus inputImagePlus, String algorithm, double localRadius, double thrMult,
                                      boolean useLowerLim, double lowerLim, boolean globalZ) {

        double localRadiusZ;
        if (globalZ) {
            localRadiusZ = inputImagePlus.getNSlices()/2;
        } else {
            localRadiusZ = localRadius*inputImagePlus.getCalibration().pixelWidth / inputImagePlus.getCalibration().pixelDepth;
        }

        AutoLocalThreshold3D alt3D = new AutoLocalThreshold3D();
        if (useLowerLim) alt3D.setLowerThreshold((int) lowerLim);

        alt3D.exec(inputImagePlus,algorithm,(int) Math.round(localRadius),(int) Math.round(localRadiusZ),thrMult,0,0,true);

    }

    public void addGlobalThresholdMeasurement(Image image, double threshold) {
        String method = parameters.getValue(GLOBAL_ALGORITHM);
        String measurementName = getFullName(Measurements.GLOBAL_VALUE,method);

        image.addMeasurement(new Measurement(measurementName,threshold));

    }



    @Override
    public Category getCategory() {
        return Categories.DEPRECATED;
    }

    @Override
    public String getDescription() {
        return "Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the " +
                "built-in ImageJ global and 2D local auto-thresholding algorithms." +
                "<br>" +
                "<br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically " +
                "converted to 8-bit based on the \""+ImageTypeConverter.ScalingModes.FILL+"\" scaling method from the " +
                "\""+new ImageTypeConverter(null).getName()+"\" module.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String thresholdType = parameters.getValue(THRESHOLD_TYPE);
        String globalThresholdAlgorithm = parameters.getValue(GLOBAL_ALGORITHM);
        String localThresholdAlgorithm = parameters.getValue(LOCAL_ALGORITHM);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT);
        double localRadius = parameters.getValue(LOCAL_RADIUS);
        int thresholdValue = parameters.getValue(THRESHOLD_VALUE);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
        boolean useGlobalZ = parameters.getValue(USE_GLOBAL_Z);

        int threshold = 0;

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        }

        Prefs.blackBackground = !whiteBackground;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        if (!thresholdType.equals(ThresholdTypes.MANUAL) && inputImagePlus.getBitDepth() != 8) {
            ImageTypeConverter.process(inputImagePlus,8,ImageTypeConverter.ScalingModes.FILL);
        }

        // Calculating the threshold based on the selected algorithm
        switch (thresholdType) {
            case ThresholdTypes.GLOBAL:
                writeStatus("Applying global "+globalThresholdAlgorithm+" threshold (multiplier = "+thrMult+" x)");
                threshold = runGlobalThresholdOnStack(inputImagePlus,globalThresholdAlgorithm,thrMult,useLowerLim,lowerLim);

                break;

            case ThresholdTypes.LOCAL:
                switch (localThresholdAlgorithm) {
                    case LocalAlgorithms.BERNSEN_3D:
                        writeStatus("Applying local Bernsen threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.BERNSEN,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.CONTRAST_3D:
                        writeStatus("Applying local Contrast threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.CONTRAST,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEAN_3D:
                        writeStatus("Applying local Mean threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEDIAN_3D:
                        writeStatus("Applying local Median threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEDIAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_3D:
                        writeStatus("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.PHANSALKAR,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_SLICE:
                        writeStatus("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
                        applyLocalThresholdToStack(inputImagePlus,"Phansalkar",localRadius);
                        break;

                }
                break;

            case ThresholdTypes.MANUAL:
                applyGlobalThresholdToStack(inputImagePlus,thresholdValue);
                break;

        }

        if (whiteBackground) InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            if (showOutput) inputImage.showImage();

            if (thresholdType.equals(ThresholdTypes.GLOBAL)) addGlobalThresholdMeasurement(inputImage,threshold);

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

            if (thresholdType.equals(ThresholdTypes.GLOBAL)) addGlobalThresholdMeasurement(outputImage,threshold);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply threshold to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output image created during the thresholding process.  This image will be added to the workspace."));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR,this));
        parameters.add(new ChoiceP(THRESHOLD_TYPE,this,ThresholdTypes.GLOBAL,ThresholdTypes.ALL, "Class of threshold to be applied.<br>" +
                "<br> - \""+ThresholdTypes.GLOBAL+"\" (default) will apply a constant, automatically-determined threshold value to all pixels in the image.  This is best when the image is uniformly illuminated.<br>" +
                "<br> - \" "+ThresholdTypes.LOCAL+"\" will apply a variable threshold to each pixel in the image based on the local intensity around that pixel.  This is best when one region of the image is brighter than another, for example, due to heterogeneous illumination.  The size of the local region is determined by the user.<br>" +
                "<br> - \" "+ThresholdTypes.MANUAL+"\" will apply a fixed threshold value to all pixels in the image.<br>"));
        parameters.add(new ChoiceP(GLOBAL_ALGORITHM,this,GlobalAlgorithms.HUANG,GlobalAlgorithms.ALL,"Global thresholding algorithm to use."));
        parameters.add(new ChoiceP(LOCAL_ALGORITHM,this,LocalAlgorithms.PHANSALKAR_3D,LocalAlgorithms.ALL,"Local thresholding algorithm to use."));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this,1.0,"Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a \""+THRESHOLD_MULTIPLIER+"\" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180."));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false,"Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present)."));
        parameters.add(new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0, "Lowest absolute threshold value that can be applied."));
        parameters.add(new DoubleP(LOCAL_RADIUS, this, 1.0, "Radius of region to be used when calculating local intensity thresholds.  Units controlled by \""+SPATIAL_UNITS+"\" control."));
        parameters.add(new ChoiceP(SPATIAL_UNITS, this, SpatialUnits.PIXELS, SpatialUnits.ALL, "Units that the local radius is specified using."));
        parameters.add(new IntegerP(THRESHOLD_VALUE, this, 1, "Absolute manual threshold value that will be applied to all pixels."));
        parameters.add(new BooleanP(USE_GLOBAL_Z,this,false, "When performing 3D local thresholding, this takes all z-values at a location into account.  If disabled, pixels will be sampled in z according to the \""+LOCAL_RADIUS+"\" setting."));
        parameters.add(new BooleanP(WHITE_BACKGROUND, this,true, "Controls the logic of the output image in terms of what is considered foreground and background."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_TYPE));

        switch ((String) parameters.getValue(THRESHOLD_TYPE)) {
            case ThresholdTypes.GLOBAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
                returnedParameters.add(parameters.getParameter(GLOBAL_ALGORITHM));
                break;

            case ThresholdTypes.LOCAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
                returnedParameters.add(parameters.getParameter(LOCAL_ALGORITHM));
                returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
                returnedParameters.add(parameters.getParameter(USE_GLOBAL_Z));
                break;

            case ThresholdTypes.MANUAL:
                returnedParameters.add(parameters.getParameter(THRESHOLD_VALUE));
                break;

        }

        // If using an automatic threshold algorithm, we can set a lower threshold limit
        if (!parameters.getValue(THRESHOLD_TYPE).equals(ThresholdTypes.MANUAL)) {
            returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));
            if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
                returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
            }
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        if (parameters.getValue(THRESHOLD_TYPE).equals(ThresholdTypes.GLOBAL)) {
            String imageName = (boolean) parameters.getValue(APPLY_TO_INPUT) ? parameters.getValue(INPUT_IMAGE) : parameters.getValue(OUTPUT_IMAGE);
            String method = parameters.getValue(GLOBAL_ALGORITHM);
            String measurementName = getFullName(Measurements.GLOBAL_VALUE,method);

            ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(measurementName);
            reference.setImageName(imageName);
            reference.setDescription("Threshold value applied to the image during binarisation. Specified in intensity units.");
            returnedRefs.add(reference);

        }

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
}
