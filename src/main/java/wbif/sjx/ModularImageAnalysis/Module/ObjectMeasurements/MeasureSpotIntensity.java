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
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName + "_"+ MIAMeasurement.MEAN_INTENSITY, Double.NaN));
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName + "_"+ MIAMeasurement.MIN_INTENSITY, Double.NaN));
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName + "_"+ MIAMeasurement.MAX_INTENSITY, Double.NaN));
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName + "_"+ MIAMeasurement.STD_INTENSITY, Double.NaN));
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName + "_"+ MIAMeasurement.SUM_INTENSITY, Double.NaN));

            }

            return;

        }

        // Getting local object region (this overwrites the original inputObjects)
        inputObjects = GetLocalObjectRegion.getLocalRegions(inputObjects, inputObjectsName, radius, calibrated);

        // Running through each object's timepoints, getting intensity measurements
        for (Obj inputObject:inputObjects.values()) {
            // Getting pixel coordinates
            ArrayList<Integer> x = inputObject.getXCoords();
            ArrayList<Integer> y = inputObject.getYCoords();
            ArrayList<Integer> z = inputObject.getZCoords();
            Integer t = inputObject.getT();

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
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName+ "_"+ MIAMeasurement.MEAN_INTENSITY, cs.getMean()));
            if (parameters.getValue(MEASURE_MIN))
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName+ "_"+ MIAMeasurement.MIN_INTENSITY, cs.getMin()));
            if (parameters.getValue(MEASURE_MAX))
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName+ "_"+ MIAMeasurement.MAX_INTENSITY, cs.getMax()));
            if (parameters.getValue(MEASURE_STDEV))
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName+ "_"+ MIAMeasurement.STD_INTENSITY, cs.getStd(CumStat.SAMPLE)));
            if (parameters.getValue(MEASURE_SUM))
                inputObject.getParent(inputObjectsName).addMeasurement(new MIAMeasurement(inputImageName+ "_"+ MIAMeasurement.SUM_INTENSITY, cs.getSum()));

        }
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
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        if (parameters.getValue(MEASURE_MEAN))
            measurements.addMeasurement(inputObjectsName,inputImageName+ "_"+ MIAMeasurement.MEAN_INTENSITY);

        if (parameters.getValue(MEASURE_MIN))
            measurements.addMeasurement(inputObjectsName,inputImageName+ "_"+ MIAMeasurement.MIN_INTENSITY);

        if (parameters.getValue(MEASURE_MAX))
            measurements.addMeasurement(inputObjectsName,inputImageName+ "_"+ MIAMeasurement.MAX_INTENSITY);

        if (parameters.getValue(MEASURE_STDEV))
            measurements.addMeasurement(inputObjectsName,inputImageName+ "_"+ MIAMeasurement.STD_INTENSITY);

        if (parameters.getValue(MEASURE_SUM))
            measurements.addMeasurement(inputObjectsName,inputImageName+ "_"+ MIAMeasurement.SUM_INTENSITY);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
