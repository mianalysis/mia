package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class MeasureImageDimensions extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public final static String INPUT_IMAGE = "Input image";

    public MeasureImageDimensions(ModuleCollection modules) {
        super("Measure image dimensions",modules);
    }


    public interface Measurements {
        String WIDTH = "WIDTH (PX)";
        String HEIGHT = "HEIGHT (PX)";
        String N_CHANNELS = "NUMBER_OF_CHANNELS";
        String N_SLICES = "NUMBER_OF_SLICES";
        String N_FRAMES= "NUMBER_OF_FRAMES";
        String DIST_PER_PX_XY = "DISTANCE_PER_PX_XY_(${CAL})";
        String DIST_PER_SLICE_Z = "DISTANCE_PER_SLICE_Z_(${CAL})";

    }


    public static String getFullName(String measurement) {
        return "DIMENSIONS // "+measurement;
    }



    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Measure dimensions of an image and store the values as measurements associated with that image.";
    }

    @Override
    public Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        int width = inputImagePlus.getWidth();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.WIDTH),width));

        int height = inputImagePlus.getHeight();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.HEIGHT),height));

        int nChannels = inputImagePlus.getNChannels();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_CHANNELS),nChannels));

        int nSlices = inputImagePlus.getNSlices();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_SLICES),nSlices));

        int nFrames = inputImagePlus.getNFrames();
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.N_FRAMES),nFrames));

        double distXY = inputImagePlus.getCalibration().pixelWidth;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.DIST_PER_PX_XY),distXY));

        double distZ = inputImagePlus.getCalibration().pixelDepth;
        inputImage.addMeasurement(new Measurement(getFullName(Measurements.DIST_PER_SLICE_Z),distZ));

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Image to measure dimensions for."));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        String name = getFullName(Measurements.WIDTH);
        ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Width (number of columns) of the image \""+parameters.getValue(INPUT_IMAGE)+"\".  Measured in pixel units.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.HEIGHT);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Height (number of rows) of the image \""+parameters.getValue(INPUT_IMAGE)+"\".  Measured in pixel units.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_CHANNELS);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of channels in the image \""+parameters.getValue(INPUT_IMAGE)+"\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_SLICES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of slices (Z-axis) in the image \""+parameters.getValue(INPUT_IMAGE)+"\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.N_FRAMES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Number of frames (time axis) in the image \""+parameters.getValue(INPUT_IMAGE)+"\" hyperstack.  Minimum value is 1.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.DIST_PER_PX_XY);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("XY-axis spatial calibration for the image \""+parameters.getValue(INPUT_IMAGE)+"\".  MIA currently does not support different spatial calibration in X and Y.  Measured in calibrated_units/pixel units, where <i>calibrated_units</i> is determined by the \""+InputControl.SPATIAL_UNITS+"\" parameter of the \""+new InputControl(null).getName()+"\" module.");
        returnedRefs.add(reference);

        name = getFullName(Measurements.DIST_PER_SLICE_Z);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageName(inputImageName);
        reference.setDescription("Z-axis spatial calibration for the image \""+parameters.getValue(INPUT_IMAGE)+"\". Measured in calibrated_units/slice units, where <i>calibrated_units</i> is determined by the \""+InputControl.SPATIAL_UNITS+"\" parameter of the \""+new InputControl(null).getName()+"\" module.");
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
}
