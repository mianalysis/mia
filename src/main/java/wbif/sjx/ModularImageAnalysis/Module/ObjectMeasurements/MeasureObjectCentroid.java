// TODO: Add measurements

package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by sc13967 on 11/05/2017.
 */
public class MeasureObjectCentroid extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CENTROID_METHOD = "Centroid method";

    public interface Methods {
        String MEAN = "Mean";
        String MEDIAN = "Median";
        String BOTH = "Both";

        String[] ALL = new String[]{MEAN, MEDIAN, BOTH};

    }

    public static double calculateCentroid(ArrayList<Integer> values, String method) {
        if (method.equals(Methods.MEAN)) {
            CumStat cs = new CumStat();
            for (int value:values) {
                cs.addMeasure(value);
            }

            return cs.getMean();
        }

        if (method.equals(Methods.MEDIAN)) {
            // Sorting values in ascending order
            Collections.sort(values);

            // Taking the central value
            int nValues = values.size();

            if (nValues%2==0) {
                return ((double)values.get(nValues/2-1)+(double)values.get(nValues/2))/2;

            } else {
                return values.get(nValues/2);

            }
        }

        return 0;

    }

    @Override
    public String getTitle() {
        return "Measure object centroid";

    }

    @Override
    public String getHelp() {
        return "Z-coordinates are specified in terms of slices (not pixels)";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting current objects
        String inputObjectName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectName);

        // Getting which centroid measures to calculate
        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);
        if (verbose) System.out.println("["+moduleName+"] Calculating centroid as "+choice);

        // Getting the centroids of each and saving them to the objects
        for (Obj object:inputObjects.values()) {
            ArrayList<Integer> x = object.getXCoords();
            ArrayList<Integer> y = object.getYCoords();
            ArrayList<Integer> z = object.getZCoords();

            if (useMean) {
                if (x != null) {
                    double xMean = calculateCentroid(x,Methods.MEAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.X_CENTROID_MEAN_PX,xMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMean = calculateCentroid(y,Methods.MEAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.Y_CENTROID_MEAN_PX,yMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMean = calculateCentroid(z,Methods.MEAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.Z_CENTROID_MEAN_SLICE,zMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
            }

            if (useMedian) {
                if (x != null) {
                    double xMedian = calculateCentroid(x,Methods.MEDIAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.X_CENTROID_MEDIAN_PX,xMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMedian = calculateCentroid(y,Methods.MEDIAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.Y_CENTROID_MEDIAN_PX,yMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMedian = calculateCentroid(z,Methods.MEDIAN);
                    MIAMeasurement measurement = new MIAMeasurement(MIAMeasurement.Z_CENTROID_MEDIAN_SLICE,zMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CENTROID_METHOD, Parameter.CHOICE_ARRAY,Methods.MEAN,Methods.ALL));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);


        if (useMean) {
            measurements.addMeasurement(inputObjectsName,MIAMeasurement.X_CENTROID_MEAN_PX);
            measurements.addMeasurement(inputObjectsName,MIAMeasurement.Y_CENTROID_MEAN_PX);
            measurements.addMeasurement(inputObjectsName,MIAMeasurement.Z_CENTROID_MEAN_SLICE);
        }

        if (useMedian) {
            measurements.addMeasurement(inputObjectsName, MIAMeasurement.X_CENTROID_MEDIAN_PX);
            measurements.addMeasurement(inputObjectsName, MIAMeasurement.Y_CENTROID_MEDIAN_PX);
            measurements.addMeasurement(inputObjectsName, MIAMeasurement.Z_CENTROID_MEDIAN_SLICE);
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
