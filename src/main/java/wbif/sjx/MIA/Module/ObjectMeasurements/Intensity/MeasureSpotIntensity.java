package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetLocalObjectRegion;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;

/**
 * Similar to MeasureObjectIntensity, but performed on circular (or spherical) regions of interest around each point in
 * 3D.  Allows the user to specify the region around each point to be measured.  Intensity traces are stored as
 * HCMultiMeasurements
 */
public class MeasureSpotIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input spot objects";
    public static final String RADIUS_SOURCE = "Radius value source";
    public static final String FIXED_VALUE= "Fixed value";
    public static final String RADIUS_MEASUREMENT = "Radius measurement";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";

    public MeasureSpotIntensity(ModuleCollection modules) {
        super("Measure spot intensity",modules);
    }

    public interface RadiusSources {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[]{FIXED_VALUE,MEASUREMENT};

    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String STDEV = "STDEV";
        String SUM = "SUM";

    }


    private String getFullName(String imageName, String measurement) {
        return "SPOT_INTENSITY // "+imageName+"_"+measurement;
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting image to measure spot intensity for
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(FIXED_VALUE);
        boolean calibrated = parameters.getValue(CALIBRATED_UNITS);
        String radiusSource = parameters.getValue(RADIUS_SOURCE);
        String radiusMeasurement = parameters.getValue(RADIUS_MEASUREMENT);
        boolean useMeasurement = radiusSource.equals(RadiusSources.MEASUREMENT);

        // Checking if there are any objects to measure
        if (inputObjects.size() == 0) {
            for (Obj inputObject:inputObjects.values()) {
                if ((boolean) parameters.getValue(MEASURE_MEAN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MEAN), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_MIN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MIN), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_MAX))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MAX), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_STDEV))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.STDEV), Double.NaN));
                if ((boolean) parameters.getValue(MEASURE_SUM))
                    inputObject.getParent(inputObjectsName).addMeasurement(new Measurement(getFullName(inputImageName,Measurements.SUM), Double.NaN));

            }

            return true;

        }

        for (Obj inputObject:inputObjects.values()) {
            if (useMeasurement) radius = inputObject.getMeasurement(radiusMeasurement).getValue();
            Obj spotObject = GetLocalObjectRegion.getLocalRegion(inputObject,inputObjectsName,radius,calibrated,false);

            CumStat cs = new CumStat();

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            Integer t = spotObject.getT();
            for (Point<Integer> point:spotObject.getCoordinateSet()) {
                ipl.setPosition(1,point.z+1,t+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(point.x,point.y));
            }

            if ((boolean) parameters.getValue(MEASURE_MEAN))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MEAN), cs.getMean()));
            if ((boolean) parameters.getValue(MEASURE_MIN))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MIN), cs.getMin()));
            if ((boolean) parameters.getValue(MEASURE_MAX))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName,Measurements.MAX), cs.getMax()));
            if ((boolean) parameters.getValue(MEASURE_STDEV))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            if ((boolean) parameters.getValue(MEASURE_SUM))
                inputObject.addMeasurement(new Measurement(getFullName(inputImageName,Measurements.SUM), cs.getSum()));

        }

        if (showOutput) inputObjects.showMeasurements(this,modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this,false));
        parameters.add(new ChoiceP(RADIUS_SOURCE,this,RadiusSources.FIXED_VALUE,RadiusSources.ALL));
        parameters.add(new DoubleP(FIXED_VALUE, this,2.0));
        parameters.add(new ObjectMeasurementP(RADIUS_MEASUREMENT, this));
        parameters.add(new BooleanP(MEASURE_MEAN, this, true));
        parameters.add(new BooleanP(MEASURE_MIN, this, true));
        parameters.add(new BooleanP(MEASURE_MAX, this, true));
        parameters.add(new BooleanP(MEASURE_STDEV, this, true));
        parameters.add(new BooleanP(MEASURE_SUM, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(RADIUS_SOURCE));

        switch ((String) parameters.getValue(RADIUS_SOURCE)) {
            case RadiusSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(FIXED_VALUE));
                break;

            case RadiusSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(RADIUS_MEASUREMENT));
                ((ObjectMeasurementP) parameters.getParameter(RADIUS_MEASUREMENT)).setObjectName(inputObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.add(parameters.getParameter(MEASURE_MIN));
        returnedParameters.add(parameters.getParameter(MEASURE_MAX));
        returnedParameters.add(parameters.getParameter(MEASURE_STDEV));
        returnedParameters.add(parameters.getParameter(MEASURE_SUM));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        if ((boolean) parameters.getValue(MEASURE_MEAN)) {
            String name = getFullName(inputImageName, Measurements.MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MIN)) {
            String name = getFullName(inputImageName, Measurements.MIN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_MAX)) {
            String name = getFullName(inputImageName, Measurements.MAX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_STDEV)) {
            String name = getFullName(inputImageName, Measurements.STDEV);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        if ((boolean) parameters.getValue(MEASURE_SUM)) {
            String name = getFullName(inputImageName, Measurements.SUM);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
