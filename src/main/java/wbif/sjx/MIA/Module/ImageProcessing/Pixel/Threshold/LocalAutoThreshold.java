package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Threshold;

import fiji.threshold.Auto_Local_Threshold;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import wbif.sjx.MIA.Object.Parameters.ChoiceInterfaces.SpatialUnitsInterface;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Filters.AutoLocalThreshold3D;

public class LocalAutoThreshold extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLD_MODE = "Threshold mode";
    public static final String ALGORITHM_3D = "Algorithm (3D)";
    public static final String ALGORITHM_SLICE = "Algorithm (slice-by-slice)";
    public static final String THRESHOLD_MULTIPLIER = "Threshold multiplier";
    public static final String USE_LOWER_THRESHOLD_LIMIT = "Use lower threshold limit";
    public static final String LOWER_THRESHOLD_LIMIT = "Lower threshold limit";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String SPATIAL_UNITS_MODE = "Spatial units mode";
    public static final String USE_GLOBAL_Z = "Use full Z-range (\"Global Z\")";
    public static final String BINARY_LOGIC = "Binary logic";


    public LocalAutoThreshold(ModuleCollection modules) {
        super("Local auto-threshold",modules);
    }


    public interface ThresholdModes {
        String SLICE = "Slice-by-slice";
        String THREE_D = "3D";

        String[] ALL = new String[]{SLICE, THREE_D};

    }

    public interface Algorithms3D {
        String BERNSEN = "Bernsen";
        String CONTRAST = "Contrast";
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String PHANSALKAR = "Phansalkar";

        String[] ALL = new String[]{BERNSEN, CONTRAST, MEAN, MEDIAN, PHANSALKAR};

    }

    public interface AlgorithmsSlice {
        String BERNSEN = "Bernsen";
        String CONTRAST = "Contrast";
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String MIDGREY = "MidGrey";
        String NIBLACK = "Niblack";
        String OTSU = "Otsu";
        String PHANSALKAR = "Phansalkar";
        String SAUVOLA = "Sauvola";

        String[] ALL = new String[]{BERNSEN, CONTRAST, MEAN, MEDIAN, MIDGREY, NIBLACK, OTSU, PHANSALKAR, SAUVOLA};

    }

    public interface SpatialUnitsModes extends SpatialUnitsInterface {}
    
    public interface BinaryLogic extends BinaryLogicInterface {}


    public static String getFullName(String measurement, String method) {
        return  "THRESHOLD // "+measurement+" "+method;
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
        inputImagePlus.setPosition(1, 1, 1);
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



    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return "Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the " +
                "built-in ImageJ global and 2D local auto-thresholding algorithms.<br><br>" +
                
                "Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically " +
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
        String thresholdMode = parameters.getValue(THRESHOLD_MODE);
        String algorithm3D = parameters.getValue(ALGORITHM_3D);
        String algorithmSlice = parameters.getValue(ALGORITHM_SLICE);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER);
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT);
        double localRadius = parameters.getValue(LOCAL_RADIUS);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE);
        boolean useGlobalZ = parameters.getValue(USE_GLOBAL_Z);

        if (spatialUnits.equals(SpatialUnitsModes.CALIBRATED)) {
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        }

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        if (inputImagePlus.getBitDepth() != 8) {
            ImageTypeConverter.process(inputImagePlus,8,ImageTypeConverter.ScalingModes.FILL);
        }

        switch (thresholdMode) {
            case ThresholdModes.SLICE:
                writeStatus("Applying "+algorithmSlice+" threshold slice-by-slice");
                applyLocalThresholdToStack(inputImagePlus,algorithmSlice,localRadius);
                break;

            case ThresholdModes.THREE_D:
                writeStatus("Applying "+algorithm3D+" threshold in 3D");
                applyLocalThreshold3D(inputImagePlus,algorithm3D,localRadius,thrMult,useLowerLim,lowerLim,useGlobalZ);
                break;
        }

        if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
                InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            if (showOutput) inputImage.showImage();
        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(THRESHOLD_SEPARATOR,this));
        parameters.add(new ChoiceP(THRESHOLD_MODE,this,ThresholdModes.SLICE,ThresholdModes.ALL));
        parameters.add(new ChoiceP(ALGORITHM_SLICE,this,AlgorithmsSlice.BERNSEN,AlgorithmsSlice.ALL));
        parameters.add(new ChoiceP(ALGORITHM_3D,this,Algorithms3D.BERNSEN,Algorithms3D.ALL));
        parameters.add(new DoubleP(THRESHOLD_MULTIPLIER, this,1.0));
        parameters.add(new BooleanP(USE_LOWER_THRESHOLD_LIMIT, this, false));
        parameters.add(new DoubleP(LOWER_THRESHOLD_LIMIT, this, 0.0));
        parameters.add(new DoubleP(LOCAL_RADIUS, this, 1.0));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitsModes.PIXELS, SpatialUnitsModes.ALL));
        parameters.add(new BooleanP(USE_GLOBAL_Z,this,false));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

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
        returnedParameters.add(parameters.getParameter(THRESHOLD_MODE));
        switch ((String) parameters.getValue(THRESHOLD_MODE)) {
            case ThresholdModes.SLICE:
                returnedParameters.add(parameters.getParameter(ALGORITHM_SLICE));
                returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS_MODE));
                break;

            case ThresholdModes.THREE_D:
                returnedParameters.add(parameters.getParameter(ALGORITHM_3D));
                returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS_MODE));
                returnedParameters.add(parameters.getParameter(THRESHOLD_MULTIPLIER));
                returnedParameters.add(parameters.getParameter(USE_GLOBAL_Z));

                // If using an automatic threshold algorithm, we can set a lower threshold limit
                returnedParameters.add(parameters.getParameter(USE_LOWER_THRESHOLD_LIMIT));
                if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT)) {
                    returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
                }

                break;
        }

        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to apply threshold to.");

        parameters.get(APPLY_TO_INPUT).setDescription("Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription("Name of the output image created during the thresholding process.  This image will be added to the workspace.");

        parameters.get(THRESHOLD_MODE).setDescription("Local thresholding algorithm mode to use.<br><ul>"
        
        + "<li>\""+ThresholdModes.SLICE+"\" Local thresholds are applied to a multidimensional image stack one 2D image at a time.  Images are processed independently.</li>"
        
        + "<li>\""+ThresholdModes.THREE_D+"\" Local threshold algorithms are calculated in 3D and applied to all slices of an image in a single run.  This is more computationally expensive.</li></ul>");

        parameters.get(ALGORITHM_SLICE).setDescription("Algorithms available for calculating local threshold on a slice-by-slice basis.  These are described at <a href=\"https://imagej.net/Auto_Local_Threshold\">https://imagej.net/Auto_Local_Threshold</a>.  Algorithms available: "+String.join(", ",AlgorithmsSlice.ALL));

        parameters.get(ALGORITHM_3D).setDescription("Algorithms available for calculating local threshold on 3D stack in a single run (all slices processed together).  These are 3D modifications of the algorithms described at <a href=\"https://imagej.net/Auto_Local_Threshold\">https://imagej.net/Auto_Local_Threshold</a>.  Algorithms available: "+String.join(", ",Algorithms3D.ALL));

        parameters.get(THRESHOLD_MULTIPLIER).setDescription("Prior to application of automatically-calculated thresholds the threshold value is multiplied by this value.  This allows the threshold to be systematically increased or decreased.  For example, a \""+THRESHOLD_MULTIPLIER+"\" of 0.9 applied to an automatically-calculated threshold of 200 will see the image thresholded at the level 180.");

        parameters.get(USE_LOWER_THRESHOLD_LIMIT).setDescription("Limit the lowest threshold that can be applied to the image.  This is used to prevent unintentional segmentation of an image containing only background (i.e. no features present).");

        parameters.get(LOWER_THRESHOLD_LIMIT).setDescription("Lowest absolute threshold value that can be applied.");

        parameters.get(LOCAL_RADIUS).setDescription("Radius of region to be used when calculating local intensity thresholds.  Units controlled by \""+SPATIAL_UNITS_MODE+"\" control.");

        parameters.get(SPATIAL_UNITS_MODE).setDescription(SpatialUnitsInterface.getDescription());

        parameters.get(USE_GLOBAL_Z).setDescription("When performing 3D local thresholding, this takes all z-values at a location into account.  If disabled, pixels will be sampled in z according to the \""+LOCAL_RADIUS+"\" setting.");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

    }
}
