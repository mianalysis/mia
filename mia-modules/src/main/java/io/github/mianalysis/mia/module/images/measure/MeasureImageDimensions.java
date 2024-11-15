package io.github.mianalysis.mia.module.images.measure;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Measure dimensions of an image and store the values as measurements associated with that image.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureImageDimensions extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input";

	/**
	* Image to measure dimensions for.
	*/
    public final static String INPUT_IMAGE = "Input image";

    public MeasureImageDimensions(Modules modules) {
        super("Measure image dimensions", modules);
    }

    public interface Measurements {
        String WIDTH = "WIDTH (PX)";
        String HEIGHT = "HEIGHT (PX)";
        String N_CHANNELS = "NUMBER_OF_CHANNELS";
        String N_SLICES = "NUMBER_OF_SLICES";
        String N_FRAMES = "NUMBER_OF_FRAMES";
        String DIST_PER_PX_XY = "DISTANCE_PER_PX_XY_(${SCAL})";
        String DIST_PER_SLICE_Z = "DISTANCE_PER_SLICE_Z_(${SCAL})";
        String FRAME_INTERVAL = "FRAME_INTERVAL_(${TCAL})";
        String FPS = "FRAMES_PER_SECOND";
        String BIT_DEPTH = "BIT_DEPTH";

    }

    public static String getFullName(String measurement) {
        return "DIMENSIONS // " + measurement;
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure dimensions of an image and store the values as measurements associated with that image.";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        int width = inputImagePlus.getWidth();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.WIDTH), width));

        int height = inputImagePlus.getHeight();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.HEIGHT), height));

        int nChannels = inputImagePlus.getNChannels();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_CHANNELS), nChannels));

        int nSlices = inputImagePlus.getNSlices();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_SLICES), nSlices));

        int nFrames = inputImagePlus.getNFrames();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_FRAMES), nFrames));

        double distXY = inputImagePlus.getCalibration().pixelWidth;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.DIST_PER_PX_XY), distXY));

        double distZ = inputImagePlus.getCalibration().pixelDepth;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.DIST_PER_SLICE_Z), distZ));

        double frameIntervalSecs = inputImagePlus.getCalibration().frameInterval;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.FRAME_INTERVAL), frameIntervalSecs));

        double fps = inputImagePlus.getCalibration().fps;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.FPS), fps));

        int bitDepth = inputImagePlus.getBitDepth();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.BIT_DEPTH), bitDepth));

        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to measure dimensions for."));
    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        Workspace workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);

        String name = getFullName(Measurements.WIDTH);
        ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription(
                "Width (number of columns) of the image \"" + inputImageName + "\".  Measured in pixel units.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.HEIGHT);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription(
                "Height (number of rows) of the image \"" + inputImageName + "\".  Measured in pixel units.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_CHANNELS);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription(
                "Number of channels in the image \"" + inputImageName + "\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_SLICES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription(
                "Number of slices (Z-axis) in the image \"" + inputImageName + "\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_FRAMES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of frames (time axis) in the image \"" + inputImageName
                + "\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.DIST_PER_PX_XY);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("XY-axis spatial calibration for the image \"" + inputImageName
                + "\".  MIA currently does not support different spatial calibration in X and Y.  Measured in calibrated_units/pixel units, where <i>calibrated_units</i> is determined by the \""
                + InputControl.SPATIAL_UNIT + "\" parameter of the \"" + new InputControl(null).getName()
                + "\" module.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.DIST_PER_SLICE_Z);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Z-axis spatial calibration for the image \"" + inputImageName
                + "\". Measured in calibrated_units/slice units, where <i>calibrated_units</i> is determined by the \""
                + InputControl.SPATIAL_UNIT + "\" parameter of the \"" + new InputControl(null).getName()
                + "\" module.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.FRAME_INTERVAL);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Time between successive frames in a timeseries (measured in "
                + InputControl.TEMPORAL_UNIT + " units).");
        returnedRefs.add(reference);

        name = getFullName(Measurements.FPS);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of frames per second.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.BIT_DEPTH);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of bits used to store each pixel.");
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
}
