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

    public interface Measurements {
        String MEAN_X = "CENTROID//MEAN_X_PX";
        String MEAN_Y = "CENTROID//MEAN_Y_PX";
        String MEAN_Z = "CENTROID//MEAN_Z_SLICE";
        String MEDIAN_X = "CENTROID//MEDIAN_X_PX";
        String MEDIAN_Y = "CENTROID//MEDIAN_Y_PX";
        String MEDIAN_Z = "CENTROID//MEDIAN_Z_SLICE";

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
                    double xMean = object.getXMean(true);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEAN_X,xMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMean = object.getYMean(true);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEAN_Y,yMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMean = object.getZMean(true,false);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEAN_Z,zMean);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
            }

            if (useMedian) {
                if (x != null) {
                    double xMedian = object.getXMedian(true);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEDIAN_X,xMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (y!= null) {
                    double yMedian = object.getYMedian(true);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEDIAN_Y,yMedian);
                    measurement.setSource(this);
                    object.addMeasurement(measurement);
                }
                if (z!= null) {
                    double zMedian = object.getZMedian(true,false);
                    MIAMeasurement measurement = new MIAMeasurement(Measurements.MEDIAN_Z,zMedian);
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
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String choice = parameters.getValue(CENTROID_METHOD);
        boolean useMean = choice.equals(Methods.MEAN) | choice.equals(Methods.BOTH);
        boolean useMedian = choice.equals(Methods.MEDIAN) | choice.equals(Methods.BOTH);


        if (useMean) {
            measurements.addObjectMeasurement(inputObjectsName,Measurements.MEAN_X);
            measurements.addObjectMeasurement(inputObjectsName,Measurements.MEAN_Y);
            measurements.addObjectMeasurement(inputObjectsName,Measurements.MEAN_Z);
        }

        if (useMedian) {
            measurements.addObjectMeasurement(inputObjectsName, Measurements.MEDIAN_X);
            measurements.addObjectMeasurement(inputObjectsName, Measurements.MEDIAN_X);
            measurements.addObjectMeasurement(inputObjectsName, Measurements.MEDIAN_Z);
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
