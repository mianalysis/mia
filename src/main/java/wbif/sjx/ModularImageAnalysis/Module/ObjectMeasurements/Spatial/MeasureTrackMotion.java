package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Timepoint;
import wbif.sjx.common.Object.Track;

import java.util.TreeMap;

/**
 * Created by steph on 24/05/2017.
 */
public class MeasureTrackMotion extends Module {
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";
    public static final String SUBTRACT_AVERAGE_MOTION = "Subtract average motion";


    public interface Measurements {
        String DURATION = "DURATION_(FRAMES)";
        String FIRST_FRAME = "FIRST_FRAME";
        String X_VELOCITY_PX = "X_VELOCITY_(PX/FRAME)";
        String X_VELOCITY_CAL = "X_VELOCITY_(${CAL}/FRAME)";
        String Y_VELOCITY_PX = "Y_VELOCITY_(PX/FRAME)";
        String Y_VELOCITY_CAL = "Y_VELOCITY_(${CAL}/FRAME)";
        String Z_VELOCITY_SLICES = "Z_VELOCITY_(SLICES/FRAME)";
        String Z_VELOCITY_CAL = "Z_VELOCITY_(${CAL}/FRAME)";
        String MEAN_X_VELOCITY_PX = "MEAN_X_VELOCITY_(PX/FRAME)";
        String MEAN_X_VELOCITY_CAL = "MEAN_X_VELOCITY_(${CAL}/FRAME)";
        String MEAN_Y_VELOCITY_PX = "MEAN_Y_VELOCITY_(PX/FRAME)";
        String MEAN_Y_VELOCITY_CAL = "MEAN_Y_VELOCITY_(${CAL}/FRAME)";
        String MEAN_Z_VELOCITY_SLICES = "MEAN_Z_VELOCITY_(SLICES/FRAME)";
        String MEAN_Z_VELOCITY_CAL = "MEAN_Z_VELOCITY_(${CAL}/FRAME)";
        String MEAN_INSTANTANEOUS_SPEED_PX = "MEAN_INSTANTANEOUS_SPEED_(PX/FRAME)";
        String MEAN_INSTANTANEOUS_SPEED_CAL = "MEAN_INSTANTANEOUS_SPEED_(${CAL}/FRAME)";
        String TOTAL_PATH_LENGTH_PX = "TOTAL_PATH_LENGTH_(PX)";
        String TOTAL_PATH_LENGTH_CAL = "TOTAL_PATH_LENGTH_(${CAL})";
        String EUCLIDEAN_DISTANCE_PX = "EUCLIDEAN_DISTANCE_(PX)";
        String EUCLIDEAN_DISTANCE_CAL = "EUCLIDEAN_DISTANCE_(${CAL})";
        String DIRECTIONALITY_RATIO = "DIRECTIONALITY_RATIO";
        String INSTANTANEOUS_SPEED_PX = "INSTANTANEOUS_SPEED_(PX/FRAME)";
        String INSTANTANEOUS_SPEED_CAL = "INSTANTANEOUS_SPEED_(${CAL}/FRAME)";
        String CUMULATIVE_PATH_LENGTH_PX = "CUMULATIVE_PATH_LENGTH_(PX)";
        String CUMULATIVE_PATH_LENGTH_CAL = "CUMULATIVE_PATH_LENGTH_(${CAL})";
        String ROLLING_EUCLIDEAN_DISTANCE_PX = "ROLLING_EUCLIDEAN_DISTANCE_(PX)";
        String ROLLING_EUCLIDEAN_DISTANCE_CAL = "ROLLING_EUCLIDEAN_DISTANCE_(${CAL})";
        String ROLLING_DIRECTIONALITY_RATIO = "ROLLING_DIRECTIONALITY_RATIO";
        String DETECTION_FRACTION = "DETECTION_FRACTION";
        String RELATIVE_FRAME = "RELATIVE_FRAME";

    }


    public static String getFullName(String measurement, boolean subtractAverageMotion) {
        if (subtractAverageMotion) return Units.replace("TRACK_ANALYSIS // (AV_SUB) " + measurement);
        return Units.replace("TRACK_ANALYSIS // " + measurement);
    }


    public static Track createTrack(Obj trackObject, String spotObjectsName) {
        // Getting the corresponding spots for this track
        Track track = new Track("px");
        for (Obj spotObject : trackObject.getChildren(spotObjectsName).values()) {
            double x = spotObject.getXMean(true);
            double y = spotObject.getYMean(true);
            double z = spotObject.getZMean(true,true);

            int t = spotObject.getT();
            track.addTimepoint(x,y,z,t);
        }

        // Create track object
        return track;

    }

    public static Track createAverageTrack(ObjCollection tracks, String spotObjectsName) {
        TreeMap<Integer,CumStat> x = new TreeMap<>();
        TreeMap<Integer,CumStat> y = new TreeMap<>();
        TreeMap<Integer,CumStat> z = new TreeMap<>();

        for (Obj track:tracks.values()) {
            for (Obj spot:track.getChildren(spotObjectsName).values()) {
                int t = spot.getT();

                // Adding new CumStats to store coordinates at this timepoint if there isn't one already
                x.putIfAbsent(t,new CumStat());
                y.putIfAbsent(t,new CumStat());
                z.putIfAbsent(t,new CumStat());

                // Adding current coordinates
                x.get(t).addMeasure(spot.getXMean(true));
                y.get(t).addMeasure(spot.getYMean(true));
                z.get(t).addMeasure(spot.getZMean(true,true));

            }
        }

        // Creating the average track
        Track averageTrack = new Track("px");
        for (int t:x.keySet()) averageTrack.addTimepoint(x.get(t).getMean(),y.get(t).getMean(),z.get(t).getMean(),t);

        return averageTrack;

    }

    public static void subtractAverageMotion(Track track, Track averageTrack) {
        // Iterating over each frame, subtracting the average motion
        for (int f:track.getF()) {
            Point<Double> point = track.getPointAtFrame(f);
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();

            double xAv = averageTrack.getX(f);
            double yAv = averageTrack.getY(f);
            double zAv = averageTrack.getZ(f);

            point.setX(x-xAv);
            point.setY(y-yAv);
            point.setZ(z-zAv);

        }
    }

    public static void calculateTemporalMeasurements(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            String name = getFullName(Measurements.DURATION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.FIRST_FRAME,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DETECTION_FRACTION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            Timepoint<Double> firstPoint = track.values().iterator().next();

            int duration = track.getDuration();
            String name = getFullName(Measurements.DURATION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, duration));
            name = getFullName(Measurements.FIRST_FRAME,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, firstPoint.getF()));

            int nSpots = track.values().size();
            double detectionFraction = (double) nSpots/(double) duration;
            name = getFullName(Measurements.DETECTION_FRACTION,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, detectionFraction));

        }
    }

    public static void calculateVelocity(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() <= 1) {
            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // Calculating track motion
            double distPerPxXY = trackObject.getDistPerPxXY();
            double distPerPxZ = trackObject.getDistPerPxZ();
            double ratio = distPerPxZ/distPerPxXY;

            TreeMap<Integer, Double> xVelocity = track.getInstantaneousXVelocity();
            TreeMap<Integer, Double> yVelocity = track.getInstantaneousYVelocity();
            TreeMap<Integer, Double> zVelocity = track.getInstantaneousZVelocity();
            TreeMap<Integer, Double> speed = track.getInstantaneousSpeed();

            CumStat cumStatX = new CumStat();
            CumStat cumStatY = new CumStat();
            CumStat cumStatZ = new CumStat();
            CumStat cumStatSpeed = new CumStat();

            for (int frame:xVelocity.keySet()) {
                // The first value is set to zero
                if (frame == 0) continue;
                cumStatX.addMeasure(xVelocity.get(frame));
                cumStatY.addMeasure(yVelocity.get(frame));
                cumStatZ.addMeasure(zVelocity.get(frame));
                cumStatSpeed.addMeasure(speed.get(frame));
            }

            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean()));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean()));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY/distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY/distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean()));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean() * distPerPxXY));

        }
    }

    public static void calculateRelativeTimepoint(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        if (track.size() == 0) return;

        int firstTimepoint = track.values().iterator().next().getF();

        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int currentTimepoint = spotObject.getT();
            String name = getFullName(Measurements.RELATIVE_FRAME, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, currentTimepoint-firstTimepoint));

        }
    }

    public static void calculateSpatialMeasurements(Obj trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            // Adding measurements to track objects
            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // If the track has a single time-point there's no velocity to measure
            double distPerPxXY = trackObject.getDistPerPxXY();
            double euclideanDistance = track.getEuclideanDistance();
            double totalPathLength = track.getTotalPathLength();

            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance*distPerPxXY));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength*distPerPxXY));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO,averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, track.getDirectionalityRatio()));

        }
    }

    public static void calculateInstantaneousVelocity(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDistPerPxXY();
        double distPerPxZ = trackObject.getDistPerPxZ();

        TreeMap<Integer, Double> xVelocity = track.getInstantaneousXVelocity();
        TreeMap<Integer, Double> yVelocity = track.getInstantaneousYVelocity();
        TreeMap<Integer, Double> zVelocity = track.getInstantaneousZVelocity();
        TreeMap<Integer, Double> speed = track.getInstantaneousSpeed();

        // Getting the first timepoint
        int minT = Integer.MAX_VALUE;
        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            minT = Math.min(minT, spotObject.getT());
        }

        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // For the first time-point set certain velocity measurements to Double.NaN (rather than zero)
            if (t == minT) {
                String name = getFullName(Measurements.X_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.X_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_SLICES,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));

            } else {
                String name = getFullName(Measurements.X_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t)));
                name = getFullName(Measurements.X_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Y_VELOCITY_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t)));
                name = getFullName(Measurements.Y_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Z_VELOCITY_SLICES,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY/distPerPxZ));
                name = getFullName(Measurements.Z_VELOCITY_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t)));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t) * distPerPxXY));

            }
        }
    }

    public static void calculateInstantaneousSpatialMeasurements(Obj trackObject, Track track, String inputSpotObjectsName, boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDistPerPxXY();

        // Calculating rolling values
        TreeMap<Integer, Double> pathLength = track.getRollingTotalPathLength();
        TreeMap<Integer, Double> euclidean = track.getRollingEuclideanDistance();
        TreeMap<Integer, Double> dirRatio = track.getRollingDirectionalityRatio();

        // Applying the relevant measurement to each spot
        for (Obj spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // The remaining measurements are unaffected by whether it's the first time-point
            String name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t)));
            name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t)*distPerPxXY));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t)));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t)*distPerPxXY));
            name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO,averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, dirRatio.get(t)));

        }
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
        ObjCollection trackObjects = workspace.getObjects().get(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS);
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION);

        // If necessary, creating the average track
        Track averageTrack = null;
        if (subtractAverage) averageTrack = createAverageTrack(trackObjects,inputSpotObjectsName);

        // Converting objects to Track class object
        for (Obj trackObject:trackObjects.values()) {
            Track track = createTrack(trackObject,inputSpotObjectsName);

            // If necessary, applying the motion correction to the object
            if (subtractAverage) subtractAverageMotion(track,averageTrack);

            // Calculating the measurements
            calculateTemporalMeasurements(trackObject,track,subtractAverage);
            calculateVelocity(trackObject,track,subtractAverage);
            calculateSpatialMeasurements(trackObject,track,subtractAverage);
            calculateInstantaneousVelocity(trackObject,track,inputSpotObjectsName,subtractAverage);
            calculateInstantaneousSpatialMeasurements(trackObject,track,inputSpotObjectsName,subtractAverage);
            calculateRelativeTimepoint(trackObject,track,inputSpotObjectsName,subtractAverage);

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_TRACK_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_SPOT_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.add(new Parameter(SUBTRACT_AVERAGE_MOTION,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        String objectName = parameters.getValue(INPUT_TRACK_OBJECTS);
        parameters.updateValueSource(INPUT_SPOT_OBJECTS, objectName);

        returnedParameters.add(parameters.getParameter(SUBTRACT_AVERAGE_MOTION));

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
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION);

        String name = getFullName(Measurements.DIRECTIONALITY_RATIO,subtractAverage);
        MeasurementReference reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.DURATION,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.FIRST_FRAME,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_X_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_X_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.DETECTION_FRACTION,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputTrackObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.X_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.X_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.Y_VELOCITY_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.Y_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.Z_VELOCITY_SLICES,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.Z_VELOCITY_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        name = getFullName(Measurements.RELATIVE_FRAME,subtractAverage);
        reference = objectMeasurementReferences.getOrPut(name);
        reference.setImageObjName(inputSpotObjects);
        reference.setCalculated(true);

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
