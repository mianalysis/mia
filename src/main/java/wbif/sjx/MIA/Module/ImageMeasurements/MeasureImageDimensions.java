package wbif.sjx.MIA.Module.ImageMeasurements;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

public class MeasureImageDimensions extends Module {
    public final static String INPUT_IMAGE = "Input image";


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
        return Units.replace("DIMENSIONS // "+measurement);
    }

    @Override
    public String getTitle() {
        return "Measure image dimensions";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
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

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        imageMeasurementRefs.setAllCalculated(false);

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        String name = getFullName(Measurements.WIDTH);
        MeasurementRef reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.HEIGHT);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.N_CHANNELS);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.N_SLICES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.N_FRAMES);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.DIST_PER_PX_XY);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

        name = getFullName(Measurements.DIST_PER_SLICE_Z);
        reference = imageMeasurementRefs.getOrPut(name);
        reference.setImageObjName(inputImageName);
        reference.setCalculated(true);

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

}
