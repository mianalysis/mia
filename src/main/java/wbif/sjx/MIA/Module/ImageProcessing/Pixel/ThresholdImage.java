// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import fiji.threshold.Auto_Local_Threshold;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.process.AutoThresholder;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.common.Filters.AutoLocalThreshold3D;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ThresholdImage extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
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
    public static final String STORE_THRESHOLD_AS_MEASUREMENT = "Store threshold as measurement";


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
                    writeMessage("Processing image " + (++count) + " of " + total);
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
    }

    public void applyLocalThreshold3D(ImagePlus inputImagePlus, String algorithm, double localRadius, double thrMult,
                                      boolean useLowerLim, double lowerLim, boolean globalZ) {

        double localRadiusZ;
        if (globalZ) {
            localRadiusZ = inputImagePlus.getNSlices()/2;
        } else {
            localRadiusZ = localRadius*inputImagePlus.getCalibration().getX(1) / inputImagePlus.getCalibration().getZ(1);
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
    public String getTitle() {
        return "Threshold image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
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
        boolean storeMeasurement = parameters.getValue(STORE_THRESHOLD_AS_MEASUREMENT);
        int threshold = 0;

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        }

        Prefs.blackBackground = !whiteBackground;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        if (!thresholdType.equals(ThresholdTypes.MANUAL) && inputImagePlus.getBitDepth() != 8) {
            IntensityMinMax.run(inputImagePlus, true, 0, IntensityMinMax.PROCESS_PRECISE);
            IJ.run(inputImagePlus, "8-bit", null);
        }

        // Calculating the threshold based on the selected algorithm
        switch (thresholdType) {
            case ThresholdTypes.GLOBAL:
                writeMessage("Applying global "+globalThresholdAlgorithm+" threshold (multplier = "+thrMult+" x)");
                threshold = runGlobalThresholdOnStack(inputImagePlus,globalThresholdAlgorithm,thrMult,useLowerLim,lowerLim);

                break;

            case ThresholdTypes.LOCAL:
                switch (localThresholdAlgorithm) {
                    case LocalAlgorithms.BERNSEN_3D:
                        writeMessage("Applying local Bernsen threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.BERNSEN,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.CONTRAST_3D:
                        writeMessage("Applying local Contrast threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.CONTRAST,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEAN_3D:
                        writeMessage("Applying local Mean threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.MEDIAN_3D:
                        writeMessage("Applying local Median threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.MEDIAN,localRadius,thrMult,useLowerLim,
                                lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_3D:
                        writeMessage("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
                        applyLocalThreshold3D(inputImagePlus,AutoLocalThreshold3D.PHANSALKAR,localRadius,thrMult,
                                useLowerLim,lowerLim,useGlobalZ);
                        break;

                    case LocalAlgorithms.PHANSALKAR_SLICE:
                        writeMessage("Applying local Phansalkar threshold (radius = "+localRadius+" px)");
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

            if (thresholdType.equals(ThresholdTypes.GLOBAL) && storeMeasurement) addGlobalThresholdMeasurement(inputImage,threshold);

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

            if (thresholdType.equals(ThresholdTypes.GLOBAL) && storeMeasurement) addGlobalThresholdMeasurement(outputImage,threshold);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(THRESHOLD_SEPARATOR,this));
        parameters.add(new ChoiceP(THRESHOLD_TYPE,this,ThresholdTypes.GLOBAL,ThresholdTypes.ALL));
        parameters.add(new ChoiceP(GLOBAL_ALGORITHM,this,GlobalAlgorithms.HUANG,GlobalAlgorithms.ALL));
        parameters.add(new ChoiceP(LOCAL_ALGORITHM,this,LocalAlgorithms.PHANSALKAR_3D,LocalAlgorithms.ALL));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this,1.0));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false));
        parameters.add(new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0));
        parameters.add(new DoubleP(LOCAL_RADIUS, this, 1.0));
        parameters.add(new ChoiceP(SPATIAL_UNITS, this, SpatialUnits.PIXELS, SpatialUnits.ALL));
        parameters.add(new IntegerP(THRESHOLD_VALUE, this, 1));
        parameters.add(new BooleanP(USE_GLOBAL_Z,this,false));
        parameters.add(new BooleanP(WHITE_BACKGROUND, this,true));
        parameters.add(new BooleanP(STORE_THRESHOLD_AS_MEASUREMENT, this,false));

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
                returnedParameters.add(parameters.getParameter(STORE_THRESHOLD_AS_MEASUREMENT));
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
            if (parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
                returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
            }
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        imageMeasurementRefs.setAllCalculated(false);

        if (parameters.getValue(THRESHOLD_TYPE).equals(ThresholdTypes.GLOBAL)
                && (boolean) parameters.getValue(STORE_THRESHOLD_AS_MEASUREMENT)) {
            String imageName = parameters.getValue(APPLY_TO_INPUT) ? parameters.getValue(INPUT_IMAGE) : parameters.getValue(OUTPUT_IMAGE);
            String method = parameters.getValue(GLOBAL_ALGORITHM);
            String measurementName = getFullName(Measurements.GLOBAL_VALUE,method);

            MeasurementRef reference = imageMeasurementRefs.getOrPut(measurementName);
            reference.setImageObjName(imageName);
            reference.setCalculated(true);

        }

        return imageMeasurementRefs;

    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
