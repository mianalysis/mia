package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASURE_EDGE = "Measure edge intensity";
    public static final String MEASURE_INTERIOR = "Measure interior intensity";
    public static final String EDGE_MODE = "Edge determination";
    public static final String EDGE_DISTANCE = "Distance";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String EDGE_PERCENTAGE = "Percentage";
    public static final String MEASURE_MEAN = "Measure mean";
    public static final String MEASURE_STDEV = "Measure standard deviation";
    public static final String MEASURE_MIN = "Measure minimum";
    public static final String MEASURE_MAX = "Measure maximum";

    private static final String DISTANCE_FROM_EDGE = "Distance to edge";
    private static final String PERCENTAGE_FROM_EDGE = "Percentage of maximum distance to edge";
    private static final String[] EDGE_MODES = new String[]{DISTANCE_FROM_EDGE,PERCENTAGE_FROM_EDGE};


    @Override
    public String getTitle() {
        return "Measure object intensity";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Getting parameters
        boolean calcMean = parameters.getValue(MEASURE_MEAN);
        boolean calcMin = parameters.getValue(MEASURE_MIN);
        boolean calcMax = parameters.getValue(MEASURE_MAX);
        boolean calcStdev = parameters.getValue(MEASURE_STDEV);
        boolean measureEdge = parameters.getValue(MEASURE_EDGE);
        boolean measureInterior = parameters.getValue(MEASURE_INTERIOR);
        String edgeMode = parameters.getValue(EDGE_MODE);
        double edgeDistance = parameters.getValue(EDGE_DISTANCE);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        double edgePercentage = parameters.getValue(EDGE_PERCENTAGE);

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) {
            // Initialising the cumulative statistics object to store pixel intensities
            CumStat cs = new CumStat();

            // Getting pixel coordinates
            ArrayList<Integer> x = object.getCoordinates(Obj.X);
            ArrayList<Integer> y = object.getCoordinates(Obj.Y);
            ArrayList<Integer> z = object.getCoordinates(Obj.Z);
            int cPos = object.getCoordinates(Obj.C);
            int tPos = object.getCoordinates(Obj.T);

            // Running through all pixels in this object and adding the intensity to the MultiCumStat object
            for (int i=0;i<x.size();i++) {
                int zPos = z==null ? 0 : z.get(i);

                ipl.setPosition(cPos+1,zPos+1,tPos+1);
                cs.addMeasure(ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

            }

            // Calculating mean, std, min and max intensity
            if (calcMean) object.addMeasurement(new MIAMeasurement(imageName+"_MEAN", cs.getMean()));
            if (calcMin) object.addMeasurement(new MIAMeasurement(imageName+"_MIN", cs.getMin()));
            if (calcMax) object.addMeasurement(new MIAMeasurement(imageName+"_MAX", cs.getMax()));
            if (calcStdev) object.addMeasurement(new MIAMeasurement(imageName+"_STD", cs.getStd(CumStat.SAMPLE)));

        }


        if (verbose) System.out.println("["+moduleName+"] Complete");
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(MEASURE_MEAN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MIN, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_MAX, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_STDEV, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_EDGE, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(MEASURE_INTERIOR, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(EDGE_MODE, Parameter.CHOICE_ARRAY, EDGE_MODES[0], EDGE_MODES));
        parameters.addParameter(new Parameter(EDGE_DISTANCE, Parameter.DOUBLE, 1.0));
        parameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN, true));
        parameters.addParameter(new Parameter(EDGE_PERCENTAGE, Parameter.DOUBLE, 1.0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MEAN));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MIN));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_MAX));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_STDEV));
        returnedParameters.addParameter(parameters.getParameter(MEASURE_EDGE));

        if (parameters.getValue(MEASURE_EDGE)) {
            returnedParameters.addParameter(parameters.getParameter(MEASURE_INTERIOR));
            returnedParameters.addParameter(parameters.getParameter(EDGE_MODE));

            if (parameters.getValue(EDGE_MODE).equals(DISTANCE_FROM_EDGE)) {
                returnedParameters.addParameter(parameters.getParameter(EDGE_DISTANCE));
                returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));

            } else if (parameters.getValue(EDGE_MODE).equals(PERCENTAGE_FROM_EDGE)) {
                returnedParameters.addParameter(parameters.getParameter(EDGE_PERCENTAGE));

            }
        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        boolean calcMean = parameters.getValue(MEASURE_MEAN);
        boolean calcMin = parameters.getValue(MEASURE_MIN);
        boolean calcMax = parameters.getValue(MEASURE_MAX);
        boolean calcStdev = parameters.getValue(MEASURE_STDEV);

        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        if (calcMean) measurements.addMeasurement(inputObjectsName,inputImageName+"_MEAN");
        if (calcMin) measurements.addMeasurement(inputObjectsName,inputImageName+"_MIN");
        if (calcMax) measurements.addMeasurement(inputObjectsName,inputImageName+"_MAX");
        if (calcStdev) measurements.addMeasurement(inputObjectsName,inputImageName+"_STD");

        if (parameters.getValue(MEASURE_EDGE)) {
            if (calcMean) measurements.addMeasurement(inputObjectsName,inputImageName+"_EDGE_MEAN");
            if (calcMin) measurements.addMeasurement(inputObjectsName,inputImageName+"_EDGE_MIN");
            if (calcMax) measurements.addMeasurement(inputObjectsName,inputImageName+"_EDGE_MAX");
            if (calcStdev) measurements.addMeasurement(inputObjectsName,inputImageName+"_EDGE_STD");

            if (parameters.getValue(MEASURE_INTERIOR)) {
                if (calcMean) measurements.addMeasurement(inputObjectsName,inputImageName+"_INTERIOR_MEAN");
                if (calcMin) measurements.addMeasurement(inputObjectsName,inputImageName+"_INTERIOR_MIN");
                if (calcMax) measurements.addMeasurement(inputObjectsName,inputImageName+"_INTERIOR_MAX");
                if (calcStdev) measurements.addMeasurement(inputObjectsName,inputImageName+"_INTERIOR_STD");

            }
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
