package io.github.mianalysis.mia.module.images.process.threshold;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.threshold.Auto_Local_Threshold;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.SpatialUnitsInterface;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.filters.AutoLocalThreshold3D;


/**
* Binarise an image in the workspace such that the output only has pixel values of 0 and 255.  Uses the built-in ImageJ global and 2D local auto-thresholding algorithms.<br><br>Note: Currently only works on 8-bit images.  Images with other bit depths will be automatically converted to 8-bit based on the "Fill target range (normalise)" scaling method from the "Image type converter" module.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class LocalAutoThreshold extends Module {

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
	* Local thresholding algorithm mode to use.<br><ul><li>"Slice-by-slice" Local thresholds are applied to a multidimensional image stack one 2D image at a time.  Images are processed independently.</li><li>"3D" Local threshold algorithms are calculated in 3D and applied to all slices of an image in a single run.  This is more computationally expensive.</li></ul>
	*/
    public static final String THRESHOLD_MODE = "Threshold mode";
    public static final String ALGORITHM_3D = "Algorithm (3D)";
    public static final String ALGORITHM_SLICE = "Algorithm (slice-by-slice)";

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
    public static final String USE_GLOBAL_Z = "Use full Z-range (\"Global Z\")";

	/**
	* Controls whether objects are considered to be white (255 intensity) on a black (0 intensity) background, or black on a white background.
	*/
    public static final String BINARY_LOGIC = "Binary logic";


    public LocalAutoThreshold(Modules modules) {
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


    public void applyLocalThresholdToStack(ImagePlus inputImagePlus, String algorithm, double localRadius, boolean blackBackground) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    Object[] results = new Auto_Local_Threshold().exec(inputImagePlus,algorithm,(int) localRadius,0,0,blackBackground);
                    inputImagePlus.setProcessor(((ImagePlus) results[0]).getProcessor());

                }
            }
        }
        inputImagePlus.setPosition(1, 1, 1);
        inputImagePlus.updateAndDraw();
    }

    public void applyLocalThreshold3D(ImagePlus inputImagePlus, String algorithm, double localRadius, double thrMult,
                                      boolean useLowerLim, double lowerLim, boolean globalZ, boolean blackBackground) {

        double localRadiusZ;
        if (globalZ) {
            localRadiusZ = inputImagePlus.getNSlices()/2;
        } else {
            localRadiusZ = localRadius*inputImagePlus.getCalibration().pixelWidth / inputImagePlus.getCalibration().pixelDepth;
        }

        AutoLocalThreshold3D alt3D = new AutoLocalThreshold3D();
        if (useLowerLim) alt3D.setLowerThreshold((int) lowerLim);

        alt3D.exec(inputImagePlus,algorithm,(int) Math.round(localRadius),(int) Math.round(localRadiusZ),thrMult,0,0,blackBackground);

    }



    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS_THRESHOLD;
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
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        String thresholdMode = parameters.getValue(THRESHOLD_MODE,workspace);
        String algorithm3D = parameters.getValue(ALGORITHM_3D,workspace);
        String algorithmSlice = parameters.getValue(ALGORITHM_SLICE,workspace);
        double thrMult = parameters.getValue(THRESHOLD_MULTIPLIER,workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC,workspace);
        boolean useLowerLim = parameters.getValue(USE_LOWER_THRESHOLD_LIMIT,workspace);
        double lowerLim = parameters.getValue(LOWER_THRESHOLD_LIMIT,workspace);
        double localRadius = parameters.getValue(LOCAL_RADIUS,workspace);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE,workspace);
        boolean useGlobalZ = parameters.getValue(USE_GLOBAL_Z,workspace);

        if (spatialUnits.equals(SpatialUnitsModes.CALIBRATED))
            localRadius = inputImagePlus.getCalibration().getRawX(localRadius);
        
        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Image must be 8-bit
        if (inputImagePlus.getBitDepth() != 8)
            ImageTypeConverter.process(inputImagePlus,8,ImageTypeConverter.ScalingModes.FILL);
        
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);

        switch (thresholdMode) {
            case ThresholdModes.SLICE:
                writeStatus("Applying "+algorithmSlice+" threshold slice-by-slice");
                applyLocalThresholdToStack(inputImagePlus,algorithmSlice,localRadius,blackBackground);
                break;

            case ThresholdModes.THREE_D:
                writeStatus("Applying "+algorithm3D+" threshold in 3D");
                applyLocalThreshold3D(inputImagePlus,algorithm3D,localRadius,thrMult,useLowerLim,lowerLim,useGlobalZ,blackBackground);
                break;
        }

        // if (binaryLogic.equals(BinaryLogic.WHITE_BACKGROUND))
        //         InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            inputImage.setImagePlus(inputImagePlus);
            if (showOutput) inputImage.show();
        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
            Image outputImage = ImageFactory.createImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.show();
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
        parameters.add(new DoubleP(LOCAL_RADIUS, this, 24));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitsModes.PIXELS, SpatialUnitsModes.ALL));
        parameters.add(new BooleanP(USE_GLOBAL_Z,this,false));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_MODE));
        switch ((String) parameters.getValue(THRESHOLD_MODE,workspace)) {
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
                if ((boolean) parameters.getValue(USE_LOWER_THRESHOLD_LIMIT,workspace)) {
                    returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD_LIMIT));
                }

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
