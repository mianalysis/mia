// TODO: Add measurements

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.MultiCumStat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CENTROID_METHOD = "Centroid method";

    public static final String MEAN = "Mean";
    public static final String MEDIAN = "Median";
    public static final String ALL = "Both";

    private static String[] methodChoices = new String[]{MEAN,MEDIAN,ALL};


    public static double calculateCentroid(ArrayList<Integer> values) {
        return calculateCentroid(values,MEAN);

    }

    public static double calculateCentroid(ArrayList<Integer> values, String method) {
        if (method.equals(MEAN)) {
            MultiCumStat cs = new MultiCumStat(1);
            for (int value:values) {
                cs.addMeasure(value);
            }

            return cs.getMean()[0];
        }

        if (method.equals(MEDIAN)) {
            // Sorting values in ascending order
            Collections.sort(values);

            // Taking the central value
            double nValues = values.size();
            return values.get((int) Math.floor(nValues/2));

        }

        return 0;

    }

    @Override
    public String getTitle() {
        return "Measure object centroid";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting current objects
        HCName inputObjectName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting which centroid measures to calculate
        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(MEAN) | choice.equals(ALL);
        boolean useMedian = choice.equals(MEDIAN) | choice.equals(ALL);
        if (verbose) System.out.println("["+moduleName+"] Calculating centroid as "+choice);

        // Getting the centroids of each and saving them to the objects
        for (HCObject object:inputObjects.values()) {
            ArrayList<Integer> x = object.getCoordinates(HCObject.X);
            ArrayList<Integer> y = object.getCoordinates(HCObject.Y);
            ArrayList<Integer> z = object.getCoordinates(HCObject.Z);

            if (useMean) {
                if (x != null) {
                    double xMean = calculateCentroid(x,MEAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.X_CENTROID_MEAN,xMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMean = calculateCentroid(y,MEAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.Y_CENTROID_MEAN,yMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMean = calculateCentroid(z,MEAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.Z_CENTROID_MEAN,zMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
            }

            if (useMedian) {
                if (x != null) {
                    double xMedian = calculateCentroid(x,MEDIAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.X_CENTROID_MEDIAN,xMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMedian = calculateCentroid(y,MEDIAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.Y_CENTROID_MEDIAN,yMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMedian = calculateCentroid(z,MEDIAN);
                    HCMeasurement measurement = new HCMeasurement(HCMeasurement.Z_CENTROID_MEDIAN,zMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CENTROID_METHOD, HCParameter.CHOICE_ARRAY,methodChoices[0],methodChoices));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
