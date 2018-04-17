package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Timepoint;
import wbif.sjx.common.Object.Track;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by steph on 24/05/2017.
 */
public class MeasureTrackMotion extends Module {
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";


    private interface Measurements {
        String DURATION = "TRACK_ANALYSIS//DURATION_(FRAMES)";
        String FIRST_FRAME = "TRACK_ANALYSIS//FIRST_FRAME";
        String MEAN_X_STEP_PX = "TRACK_ANALYSIS//MEAN_X_STEP_(PX)";
        String MEAN_X_STEP_CAL = "TRACK_ANALYSIS//MEAN_X_STEP_(CAL)";
        String MEAN_Y_STEP_PX = "TRACK_ANALYSIS//MEAN_Y_STEP_(PX)";
        String MEAN_Y_STEP_CAL = "TRACK_ANALYSIS//MEAN_Y_STEP_(CAL)";
        String TOTAL_PATH_LENGTH_PX = "TRACK_ANALYSIS//TOTAL_PATH_LENGTH_(PX)";
        String TOTAL_PATH_LENGTH_CAL = "TRACK_ANALYSIS//TOTAL_PATH_LENGTH_(CAL)";
        String EUCLIDEAN_DISTANCE_PX = "TRACK_ANALYSIS//EUCLIDEAN_DISTANCE_(PX)";
        String EUCLIDEAN_DISTANCE_CAL = "TRACK_ANALYSIS//EUCLIDEAN_DISTANCE_(CAL)";
        String DIRECTIONALITY_RATIO = "TRACK_ANALYSIS//DIRECTIONALITY_RATIO";
        String INSTANTANEOUS_VELOCITY_PX = "TRACK_ANALYSIS//INSTANTANEOUS_VELOCITY_(PX/FRAME)";
        String INSTANTANEOUS_VELOCITY_CAL = "TRACK_ANALYSIS//INSTANTANEOUS_VELOCITY_(CAL/FRAME)";
        String CUMULATIVE_PATH_LENGTH_PX = "TRACK_ANALYSIS//CUMULATIVE_PATH_LENGTH_(PX)";
        String CUMULATIVE_PATH_LENGTH_CAL = "TRACK_ANALYSIS//CUMULATIVE_PATH_LENGTH_(CAL)";
        String ROLLING_EUCLIDEAN_DISTANCE_PX = "TRACK_ANALYSIS//ROLLING_EUCLIDEAN_DISTANCE_(PX)";
        String ROLLING_EUCLIDEAN_DISTANCE_CAL = "TRACK_ANALYSIS//ROLLING_EUCLIDEAN_DISTANCE_(CAL)";
        String ROLLING_DIRECTIONALITY_RATIO = "TRACK_ANALYSIS//ROLLING_DIRECTIONALITY_RATIO";

    }


    @Override
    public String getTitle() {
        return "Measure track motion";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS);
        ObjCollection inputTrackObjects = workspace.getObjects().get(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);

        // Getting spatial calibration
        double distPerPxXY = workspace.getObjectSet(inputSpotObjectsName).values().iterator().next().getDistPerPxXY();

        // Converting objects to Track class object
        for (Obj inputTrackObject:inputTrackObjects.values()) {
            // Initialising stores for coordinates
            double[] x = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            double[] y = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            double[] z = new double[inputTrackObject.getChildren(inputSpotObjectsName).size()];
            int[] f = new int[inputTrackObject.getChildren(inputSpotObjectsName).size()];

            // Getting the corresponding spots for this track
            int iter = 0;
            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                x[iter] = spotObject.getXMean(true);
                y[iter] = spotObject.getYMean(true);
                z[iter] = spotObject.getZMean(true,true);
                f[iter] = spotObject.getT();
                iter++;

            }

            // Create track object
            Track track = new Track(x, y, z, f);

            if (x.length == 0) {
                // Adding measurements to track objects
                inputTrackObject.addMeasurement(new Measurement(Measurements.DURATION, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.FIRST_FRAME, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_STEP_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_STEP_CAL, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_STEP_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_STEP_CAL, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_CAL, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_CAL, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.DIRECTIONALITY_RATIO, Double.NaN));

            } else {
                double euclideanDistance = track.getEuclideanDistance(true);
                double totalPathLength = track.getTotalPathLength(true);

                // Calculating track motion
                Timepoint<Double> firstPoint = track.values().iterator().next();

                CumStat cumStatX = new CumStat();
                CumStat cumStatY = new CumStat();
                Timepoint<Double> prev = null;
                for (Timepoint<Double> timepoint:track.values()) {
                    if (prev != null) {
                        cumStatX.addMeasure(timepoint.getX()-prev.getX());
                        cumStatY.addMeasure(timepoint.getY()-prev.getY());
                    }
                    prev = timepoint;
                }

                // Adding measurements to track objects
                inputTrackObject.addMeasurement(new Measurement(Measurements.DURATION, track.getDuration()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.FIRST_FRAME, firstPoint.getF()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_STEP_PX, cumStatX.getMean()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_STEP_CAL, cumStatX.getMean()*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_STEP_PX, cumStatY.getMean()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_STEP_CAL, cumStatY.getMean()*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_PX, euclideanDistance));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_CAL, euclideanDistance*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_PX, totalPathLength));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_CAL, totalPathLength*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(true)));
            }

            // Calculating rolling values
            TreeMap<Integer, Double> velocity = track.getInstantaneousVelocity(true);
            TreeMap<Integer, Double> pathLength = track.getRollingTotalPathLength(true);
            TreeMap<Integer, Double> euclidean = track.getRollingEuclideanDistance(true);
            TreeMap<Integer, Double> dirRatio = track.getRollingDirectionalityRatio(true);

            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                int t = spotObject.getT();
                spotObject.addMeasurement(new Measurement(Measurements.INSTANTANEOUS_VELOCITY_PX, velocity.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.INSTANTANEOUS_VELOCITY_CAL, velocity.get(t)*distPerPxXY));
                spotObject.addMeasurement(new Measurement(Measurements.CUMULATIVE_PATH_LENGTH_PX, pathLength.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.CUMULATIVE_PATH_LENGTH_CAL, pathLength.get(t)*distPerPxXY));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX, euclidean.get(t)));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL, euclidean.get(t)*distPerPxXY));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_DIRECTIONALITY_RATIO, dirRatio.get(t)));
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_TRACK_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_SPOT_OBJECTS, Parameter.CHILD_OBJECTS,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        if (objectName != null) {
            parameters.updateValueSource(INPUT_SPOT_OBJECTS, objectName);

        } else {
            parameters.updateValueSource(INPUT_SPOT_OBJECTS, null);

        }

        return returnedParameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputTrackObjects = parameters.getValue(INPUT_TRACK_OBJECTS);
        String inputSpotObjects  = parameters.getValue(INPUT_SPOT_OBJECTS);

        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.DIRECTIONALITY_RATIO);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.EUCLIDEAN_DISTANCE_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.EUCLIDEAN_DISTANCE_CAL);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.TOTAL_PATH_LENGTH_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.TOTAL_PATH_LENGTH_CAL);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.DURATION);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.FIRST_FRAME);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_X_STEP_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_X_STEP_CAL);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_Y_STEP_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_Y_STEP_CAL);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.INSTANTANEOUS_VELOCITY_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.INSTANTANEOUS_VELOCITY_CAL);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.CUMULATIVE_PATH_LENGTH_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.CUMULATIVE_PATH_LENGTH_CAL);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.ROLLING_DIRECTIONALITY_RATIO);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
