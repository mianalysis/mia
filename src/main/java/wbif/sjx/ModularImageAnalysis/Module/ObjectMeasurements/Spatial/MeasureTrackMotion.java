package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import sun.reflect.generics.tree.Tree;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
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
        String DURATION = "TRACK_ANALYSIS // DURATION_(FRAMES)";
        String FIRST_FRAME = "TRACK_ANALYSIS // FIRST_FRAME";
        String X_VELOCITY_PX = "TRACK_ANALYSIS // X_VELOCITY_(PX/FRAME)";
        String X_VELOCITY_CAL = "TRACK_ANALYSIS // X_VELOCITY_(${CAL}/FRAME)";
        String Y_VELOCITY_PX = "TRACK_ANALYSIS // Y_VELOCITY_(PX/FRAME)";
        String Y_VELOCITY_CAL = "TRACK_ANALYSIS // Y_VELOCITY_(${CAL}/FRAME)";
        String MEAN_X_VELOCITY_PX = "TRACK_ANALYSIS // MEAN_X_VELOCITY_(PX/FRAME)";
        String MEAN_X_VELOCITY_CAL = "TRACK_ANALYSIS // MEAN_X_VELOCITY_(${CAL}/FRAME)";
        String MEAN_Y_VELOCITY_PX = "TRACK_ANALYSIS // MEAN_Y_VELOCITY_(PX/FRAME)";
        String MEAN_Y_VELOCITY_CAL = "TRACK_ANALYSIS // MEAN_Y_VELOCITY_(${CAL}/FRAME)";
        String TOTAL_PATH_LENGTH_PX = "TRACK_ANALYSIS // TOTAL_PATH_LENGTH_(PX)";
        String TOTAL_PATH_LENGTH_CAL = "TRACK_ANALYSIS // TOTAL_PATH_LENGTH_(${CAL})";
        String EUCLIDEAN_DISTANCE_PX = "TRACK_ANALYSIS // EUCLIDEAN_DISTANCE_(PX)";
        String EUCLIDEAN_DISTANCE_CAL = "TRACK_ANALYSIS // EUCLIDEAN_DISTANCE_(${CAL})";
        String DIRECTIONALITY_RATIO = "TRACK_ANALYSIS // DIRECTIONALITY_RATIO";
        String INSTANTANEOUS_SPEED_PX = "TRACK_ANALYSIS // INSTANTANEOUS_SPEED_(PX/FRAME)";
        String INSTANTANEOUS_SPEED_CAL = "TRACK_ANALYSIS // INSTANTANEOUS_SPEED_(${CAL}/FRAME)";
        String CUMULATIVE_PATH_LENGTH_PX = "TRACK_ANALYSIS // CUMULATIVE_PATH_LENGTH_(PX)";
        String CUMULATIVE_PATH_LENGTH_CAL = "TRACK_ANALYSIS // CUMULATIVE_PATH_LENGTH_(${CAL})";
        String ROLLING_EUCLIDEAN_DISTANCE_PX = "TRACK_ANALYSIS // ROLLING_EUCLIDEAN_DISTANCE_(PX)";
        String ROLLING_EUCLIDEAN_DISTANCE_CAL = "TRACK_ANALYSIS // ROLLING_EUCLIDEAN_DISTANCE_(${CAL})";
        String ROLLING_DIRECTIONALITY_RATIO = "TRACK_ANALYSIS // ROLLING_DIRECTIONALITY_RATIO";

    }


    @Override
    public String getTitle() {
        return "Measure track motion";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
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
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_VELOCITY_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_X_VELOCITY_CAL), Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_VELOCITY_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Y_VELOCITY_CAL), Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.EUCLIDEAN_DISTANCE_CAL), Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_PX, Double.NaN));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.TOTAL_PATH_LENGTH_CAL), Double.NaN));
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
                        cumStatX.addMeasure((timepoint.getX()-prev.getX())/(timepoint.getF()-prev.getF()));
                        cumStatY.addMeasure((timepoint.getY()-prev.getY())/(timepoint.getF()-prev.getF()));
                    }
                    prev = timepoint;
                }

                // Adding measurements to track objects
                inputTrackObject.addMeasurement(new Measurement(Measurements.DURATION, track.getDuration()));
                inputTrackObject.addMeasurement(new Measurement(Measurements.FIRST_FRAME, firstPoint.getF()));

                // If the track has a single time-point there's no velocity to measure
                if (x.length == 1) {
                    inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_VELOCITY_PX, Double.NaN));
                    inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_X_VELOCITY_CAL), Double.NaN));
                    inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_VELOCITY_PX, Double.NaN));
                    inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Y_VELOCITY_CAL), Double.NaN));
                } else {
                    inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_X_VELOCITY_PX, cumStatX.getMean()));
                    inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_X_VELOCITY_CAL), cumStatX.getMean() * distPerPxXY));
                    inputTrackObject.addMeasurement(new Measurement(Measurements.MEAN_Y_VELOCITY_PX, cumStatY.getMean()));
                    inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.MEAN_Y_VELOCITY_CAL), cumStatY.getMean() * distPerPxXY));
                }

                inputTrackObject.addMeasurement(new Measurement(Measurements.EUCLIDEAN_DISTANCE_PX, euclideanDistance));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.EUCLIDEAN_DISTANCE_CAL), euclideanDistance*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.TOTAL_PATH_LENGTH_PX, totalPathLength));
                inputTrackObject.addMeasurement(new Measurement(Units.replace(Measurements.TOTAL_PATH_LENGTH_CAL), totalPathLength*distPerPxXY));
                inputTrackObject.addMeasurement(new Measurement(Measurements.DIRECTIONALITY_RATIO, track.getDirectionalityRatio(true)));

            }

            // Calculating rolling values
            TreeMap<Integer, Double> xVelocity = track.getInstantaneousXVelocity(true);
            TreeMap<Integer, Double> yVelocity = track.getInstantaneousYVelocity(true);
            TreeMap<Integer, Double> speed = track.getInstantaneousSpeed(true);
            TreeMap<Integer, Double> pathLength = track.getRollingTotalPathLength(true);
            TreeMap<Integer, Double> euclidean = track.getRollingEuclideanDistance(true);
            TreeMap<Integer, Double> dirRatio = track.getRollingDirectionalityRatio(true);

            // Finding first time-point
            int minT = Integer.MAX_VALUE;
            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                minT = Math.min(minT, spotObject.getT());
            }

            for (Obj spotObject : inputTrackObject.getChildren(inputSpotObjectsName).values()) {
                int t = spotObject.getT();

                // For the first time-point set certain velocity measurements to Double.NaN (rather than zero)
                if (t == minT) {
                    spotObject.addMeasurement(new Measurement(Measurements.X_VELOCITY_PX, Double.NaN));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.X_VELOCITY_CAL), Double.NaN));
                    spotObject.addMeasurement(new Measurement(Measurements.Y_VELOCITY_PX, Double.NaN));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_VELOCITY_CAL), Double.NaN));
                    spotObject.addMeasurement(new Measurement(Measurements.INSTANTANEOUS_SPEED_PX, Double.NaN));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.INSTANTANEOUS_SPEED_CAL), Double.NaN));
                } else {
                    spotObject.addMeasurement(new Measurement(Measurements.X_VELOCITY_PX, xVelocity.get(t)));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.X_VELOCITY_CAL), xVelocity.get(t) * distPerPxXY));
                    spotObject.addMeasurement(new Measurement(Measurements.Y_VELOCITY_PX, yVelocity.get(t)));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_VELOCITY_CAL), yVelocity.get(t) * distPerPxXY));
                    spotObject.addMeasurement(new Measurement(Measurements.INSTANTANEOUS_SPEED_PX, speed.get(t)));
                    spotObject.addMeasurement(new Measurement(Units.replace(Measurements.INSTANTANEOUS_SPEED_CAL), speed.get(t) * distPerPxXY));
                }

                // The remaining measurements are unaffected by whether it's the first time-point
                spotObject.addMeasurement(new Measurement(Measurements.CUMULATIVE_PATH_LENGTH_PX, pathLength.get(t)));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.CUMULATIVE_PATH_LENGTH_CAL), pathLength.get(t)*distPerPxXY));
                spotObject.addMeasurement(new Measurement(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX, euclidean.get(t)));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL), euclidean.get(t)*distPerPxXY));
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

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.EUCLIDEAN_DISTANCE_CAL));
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.TOTAL_PATH_LENGTH_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.TOTAL_PATH_LENGTH_CAL));
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.DURATION);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.FIRST_FRAME);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_X_VELOCITY_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_X_VELOCITY_CAL));
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.MEAN_Y_VELOCITY_PX);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MEAN_Y_VELOCITY_CAL));
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.X_VELOCITY_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.X_VELOCITY_CAL));
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.Y_VELOCITY_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.Y_VELOCITY_CAL));
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.INSTANTANEOUS_SPEED_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.INSTANTANEOUS_SPEED_CAL));
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.CUMULATIVE_PATH_LENGTH_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.CUMULATIVE_PATH_LENGTH_CAL));
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL));
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
