package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.GetLocalObjectRegion;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Similar to MeasureObjectIntensity, but performed on circular (or spherical) regions of interest around each point in
 * 3D.  Allows the user to specify the region around each point to be measured.  Intensity traces are stored as
 * HCMultiMeasurements
 */
public class MeasureSpotIntensity extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input spot objects";
    public static final String MEASUREMENT_RADIUS = "Measurement radius";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";
    public static final String MEASURE_SUM = "Measure sum";

    private Reference inputObjects;
    private MeasurementReference mean;
    private MeasurementReference min;
    private MeasurementReference max;
    private MeasurementReference stdev;
    private MeasurementReference sum;

    private interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String STDEV = "STDEV";
        String SUM = "SUM";

    }
    
    
    private String getFullName(String imageName, String measurement) {
        return "SPOT_INTENSITY//"+imageName+"_"+measurement;
    }
    
    @Override
    public String getTitle() {
        return "Measure spot intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting image to measure spot intensity for
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        double radius = parameters.getValue(MEASUREMENT_RADIUS);
        boolean calibrated = parameters.getValue(CALIBRATED_UNITS);

        // Checking if there are any objects to measure
        if (inputObjects.size() == 0) {
            for (Obj inputObject:inputObjects.values()) {
                if (parameters.getValue(MEASURE_MEAN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MEAN), Double.NaN));
                if (parameters.getValue(MEASURE_MIN))
                    inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MIN), Double.NaN));
                if (parameters.getValue(MEASURE_MAX))
                    inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MAX), Double.NaN));
                if (parameters.getValue(MEASURE_STDEV))
                    inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.STDEV), Double.NaN));
                if (parameters.getValue(MEASURE_SUM))
                    inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.SUM), Double.NaN));

            }

            return;

        }

        // Getting local object region (this overwrites the original inputObjects)
        ObjSet spotObjects = new ObjSet("test");
        spotObjects.add(new Obj("dsf",1,1d,1d,"sdf"));//GetLocalObjectRegion.getLocalRegions(inputObjects, inputObjectsName, radius, calibrated);

        // Running through each object's timepoints, getting intensity measurements
        for (Obj spotObject:spotObjects.values()) {
            // Getting pixel coordinates
            ArrayList<Integer> x = spotObject.getXCoords();
            ArrayList<Integer> y = spotObject.getYCoords();
            ArrayList<Integer> z = spotObject.getZCoords();
            Integer t = spotObject.getT();

            // Initialising the cumulative statistics object to store pixel intensities.  Unlike MeasureObjectIntensity,
            // this uses a multi-element MultiCumStat where each element corresponds to a different frame
            CumStat cs = new CumStat();

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i=0;i<x.size();i++) {
                ipl.setPosition(1,z.get(i)+1,t+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity and adding to the parent (we will discard the expanded
            // objects after this module has run)
            if (parameters.getValue(MEASURE_MEAN))
                spotObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MEAN), cs.getMean()));
            if (parameters.getValue(MEASURE_MIN))
                spotObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MIN), cs.getMin()));
            if (parameters.getValue(MEASURE_MAX))
                spotObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.MAX), cs.getMax()));
            if (parameters.getValue(MEASURE_STDEV))
                spotObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
            if (parameters.getValue(MEASURE_SUM))
                spotObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(getFullName(inputImageName,Measurements.SUM), cs.getSum()));

        }

        spotObjects = null;

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(MEASUREMENT_RADIUS, Parameter.DOUBLE,2.0));
        parameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_SUM, Parameter.BOOLEAN, true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;

    }

    @Override
    public void initialiseReferences() {
        inputObjects = new Reference();
        objectReferences.add(inputObjects);

        mean = new MeasurementReference(Measurements.MEAN);
        min = new MeasurementReference(Measurements.MIN);
        max = new MeasurementReference(Measurements.MAX);
        stdev = new MeasurementReference(Measurements.STDEV);
        sum = new MeasurementReference(Measurements.SUM);
        inputObjects.addMeasurementReference(mean);
        inputObjects.addMeasurementReference(min);
        inputObjects.addMeasurementReference(max);
        inputObjects.addMeasurementReference(stdev);
        inputObjects.addMeasurementReference(sum);

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        inputObjects.setName(parameters.getValue(INPUT_OBJECTS));

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        mean.setCalculated(false);
        min.setCalculated(false);
        max.setCalculated(false);
        stdev.setCalculated(false);
        sum.setCalculated(false);

        if (parameters.getValue(MEASURE_MEAN)) {
            mean.setCalculated(true);
            mean.setMeasurementName(getFullName(inputImageName, Measurements.MEAN));
        }

        if (parameters.getValue(MEASURE_MIN)) {
            min.setCalculated(true);
            min.setMeasurementName(getFullName(inputImageName, Measurements.MIN));
        }

        if (parameters.getValue(MEASURE_MAX)) {
            max.setCalculated(true);
            max.setMeasurementName(getFullName(inputImageName, Measurements.MAX));
        }

        if (parameters.getValue(MEASURE_STDEV)) {
            stdev.setCalculated(true);
            stdev.setMeasurementName(getFullName(inputImageName, Measurements.STDEV));
        }

        if (parameters.getValue(MEASURE_SUM)) {
            sum.setCalculated(true);
            sum.setMeasurementName(getFullName(inputImageName, Measurements.SUM));
        }

        return objectReferences;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
