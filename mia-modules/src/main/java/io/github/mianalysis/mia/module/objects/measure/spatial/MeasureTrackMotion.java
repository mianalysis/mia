package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.interfaces.MeasurementPositionProvider;
import io.github.mianalysis.mia.module.objects.track.TrackObjects;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.tracks.Timepoint;
import io.github.mianalysis.mia.object.coordinates.tracks.Track;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.analysis.AngularPersistenceCalculator;
import io.github.mianalysis.mia.process.analysis.CumulativePathLengthCalculator;
import io.github.mianalysis.mia.process.analysis.DirectionalityRatioCalculator;
import io.github.mianalysis.mia.process.analysis.EuclideanDistanceCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousSpeedCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousStepSizeCalculator;
import io.github.mianalysis.mia.process.analysis.InstantaneousVelocityCalculator;
import io.github.mianalysis.mia.process.analysis.TotalPathLengthCalculator;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by Stephen Cross on 24/05/2017.
 */

/**
 * Measures various motion metrics for tracked objects. Global motion statistics
 * (e.g. total path length) are stored as measurements associated with the input
 * track objects, whilst instantaneous motion statistics (e.g. instantaneous
 * x-velocity) are associated with the input spot objects.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureTrackMotion extends Module implements MeasurementPositionProvider {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Input objects";

    /**
     * Input track objects to measure motion for. These must be specific "track"
     * class objects as output by modules such as "Track objects". The track objects
     * are parents of individual timepoint instance objects, which are specified
     * using the "Input spot objects" parameter. Global track measurements (e.g.
     * total path length) are associated with the corresponding track objects.
     */
    public static final String INPUT_TRACK_OBJECTS = "Input track objects";

    /**
     * Input individual timepoint instance objects for the track. These are the
     * spatial records of the tracked objects in a single timepoint and are children
     * of the track object specified by "Input track objects". Instantaneous track
     * measurements (e.g. instantaneous x-velociyty) are associated with the
     * corresponding spot objects.
     */
    public static final String INPUT_SPOT_OBJECTS = "Input spot objects";

    /**
    * 
    */
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";

    /**
     * When selected, the average motion of all points between two frames is
     * subtracted from the motion prior to calculation of any track measurements.
     * This can be used as a crude form of drift correction; however, it only works
     * for global drift (where the whole sample moved together) and is less robust
     * with few tracked objects. Ideally, drift would be removed from the input
     * images using image registration prior to object detection.
     */
    public static final String SUBTRACT_AVERAGE_MOTION = "Subtract average motion";

    public static final String CALCULATE_ORIENTATION = "Calculate orientation";

    /**
     * When selected, the "leading point" of each object in the track will be
     * determined and stored as X,Y,Z coordinate measurements associated with the
     * relevant object. The orientation of the track at that location will also be
     * calculated. In this instance, "leading point" refers to the front-most point
     * in the object when considering the direction the object is moving in that
     * frame. This can be calculated relative to the previous frame, next frame or
     * both previous and next frames (controlled by the "Orientation mode"
     * parameter).
     */
    public static final String IDENTIFY_LEADING_POINT = "Identify leading point";

    /**
     * If calculating the leading point of each object in each track ("Identify
     * leading point" selected), this controls whether the instantaneous orientation
     * of the track is determined relative to the previous frame, next frame or both
     * previous and next frames.
     */
    public static final String ORIENTATION_MODE = "Orientation mode";

    public MeasureTrackMotion(Modules modules) {
        super("Measure track motion", modules);
    }

    public interface OrientationModes {
        String RELATIVE_TO_BOTH = "Relative to both points";
        String RELATIVE_TO_PREV = "Relative to previous point";
        String RELATIVE_TO_NEXT = "Relative to next point";

        String[] ALL = new String[] { RELATIVE_TO_BOTH, RELATIVE_TO_PREV, RELATIVE_TO_NEXT };
    }

    public interface Measurements {
        String DURATION = "DURATION_(FRAMES)";
        String FIRST_FRAME = "FIRST_FRAME";
        String LAST_FRAME = "LAST_FRAME";
        String X_VELOCITY_PX = "X_VELOCITY_(PX/FRAME)";
        String X_VELOCITY_CAL = "X_VELOCITY_(${SCAL}/FRAME)";
        String Y_VELOCITY_PX = "Y_VELOCITY_(PX/FRAME)";
        String Y_VELOCITY_CAL = "Y_VELOCITY_(${SCAL}/FRAME)";
        String Z_VELOCITY_SLICES = "Z_VELOCITY_(SLICES/FRAME)";
        String Z_VELOCITY_CAL = "Z_VELOCITY_(${SCAL}/FRAME)";
        String MEAN_X_VELOCITY_PX = "MEAN_X_VELOCITY_(PX/FRAME)";
        String MEAN_X_VELOCITY_CAL = "MEAN_X_VELOCITY_(${SCAL}/FRAME)";
        String MEAN_Y_VELOCITY_PX = "MEAN_Y_VELOCITY_(PX/FRAME)";
        String MEAN_Y_VELOCITY_CAL = "MEAN_Y_VELOCITY_(${SCAL}/FRAME)";
        String MEAN_Z_VELOCITY_SLICES = "MEAN_Z_VELOCITY_(SLICES/FRAME)";
        String MEAN_Z_VELOCITY_CAL = "MEAN_Z_VELOCITY_(${SCAL}/FRAME)";
        String MEAN_INSTANTANEOUS_SPEED_PX = "MEAN_INSTANTANEOUS_SPEED_(PX/FRAME)";
        String MEAN_INSTANTANEOUS_SPEED_CAL = "MEAN_INSTANTANEOUS_SPEED_(${SCAL}/FRAME)";
        String TOTAL_PATH_LENGTH_PX = "TOTAL_PATH_LENGTH_(PX)";
        String TOTAL_PATH_LENGTH_CAL = "TOTAL_PATH_LENGTH_(${SCAL})";
        String EUCLIDEAN_DISTANCE_PX = "EUCLIDEAN_DISTANCE_(PX)";
        String EUCLIDEAN_DISTANCE_CAL = "EUCLIDEAN_DISTANCE_(${SCAL})";
        String DIRECTIONALITY_RATIO = "DIRECTIONALITY_RATIO";
        String STDEV_ANGULAR_PERSISTENCE = "STDEV_ANGULAR_PERSISTENCE";
        String INSTANTANEOUS_SPEED_PX = "INSTANTANEOUS_SPEED_(PX/FRAME)";
        String INSTANTANEOUS_SPEED_CAL = "INSTANTANEOUS_SPEED_(${SCAL}/FRAME)";
        String CUMULATIVE_PATH_LENGTH_PX = "CUMULATIVE_PATH_LENGTH_(PX)";
        String CUMULATIVE_PATH_LENGTH_CAL = "CUMULATIVE_PATH_LENGTH_(${SCAL})";
        String ROLLING_EUCLIDEAN_DISTANCE_PX = "ROLLING_EUCLIDEAN_DISTANCE_(PX)";
        String ROLLING_EUCLIDEAN_DISTANCE_CAL = "ROLLING_EUCLIDEAN_DISTANCE_(${SCAL})";
        String ROLLING_DIRECTIONALITY_RATIO = "ROLLING_DIRECTIONALITY_RATIO";
        String ANGULAR_PERSISTENCE = "ANGULAR_PERSISTENCE";
        String DETECTION_FRACTION = "DETECTION_FRACTION";
        String RELATIVE_FRAME = "RELATIVE_FRAME";
        String ORIENTATION = "ORIENTATION_(DEGS)";
        String LEADING_X_PX = "LEADING_POINT_X_(PX)";
        String LEADING_Y_PX = "LEADING_POINT_Y_(PX)";
        String LEADING_Z_PX = "LEADING_POINT_Z_(PX)";

    }

    public static String getFullName(String measurement, boolean subtractAverageMotion) {
        if (subtractAverageMotion)
            return "TRACK_ANALYSIS // (AV_SUB) " + measurement;
        return "TRACK_ANALYSIS // " + measurement;
    }

    Track createTrack(ObjI trackObject, String spotObjectsName) {
        // Getting the corresponding spots for this track
        Track track = new Track("px");
        for (ObjI spotObject : trackObject.getChildren(spotObjectsName).values()) {
            double[] position = getObjectPosition(spotObject, parameters, true, true);

            int t = spotObject.getT();
            track.addTimepoint(position[0], position[1], position[2], t);

        }

        // Create track object
        return track;

    }

    public static Track createAverageTrack(ObjsI tracks, String spotObjectsName) {
        TreeMap<Integer, CumStat> x = new TreeMap<>();
        TreeMap<Integer, CumStat> y = new TreeMap<>();
        TreeMap<Integer, CumStat> z = new TreeMap<>();

        for (ObjI track : tracks.values()) {
            for (ObjI spot : track.getChildren(spotObjectsName).values()) {
                int t = spot.getT();

                // Adding new CumStats to store coordinates at this timepoint if there isn't one
                // already
                x.putIfAbsent(t, new CumStat());
                y.putIfAbsent(t, new CumStat());
                z.putIfAbsent(t, new CumStat());

                // Adding current coordinates
                x.get(t).addMeasure(spot.getXMean(true));
                y.get(t).addMeasure(spot.getYMean(true));
                z.get(t).addMeasure(spot.getZMean(true, true));

            }
        }

        // Creating the average track
        Track averageTrack = new Track("px");
        for (int t : x.keySet())
            averageTrack.addTimepoint(x.get(t).getMean(), y.get(t).getMean(), z.get(t).getMean(), t);

        return averageTrack;

    }

    public static void subtractAverageMotion(Track track, Track averageTrack) {
        // Iterating over each frame, subtracting the average motion
        for (int f : track.getF()) {
            Point<Double> point = track.getPointAtFrame(f);
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();

            double xAv = averageTrack.getX(f);
            double yAv = averageTrack.getY(f);
            double zAv = averageTrack.getZ(f);

            point.setX(x - xAv);
            point.setY(y - yAv);
            point.setZ(z - zAv);

        }
    }

    public static void calculateTemporalMeasurements(ObjI trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            String name = getFullName(Measurements.DURATION, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.FIRST_FRAME, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.LAST_FRAME, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DETECTION_FRACTION, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            Timepoint<Double> firstPoint = track.values().iterator().next();
            Timepoint<Double> lastPoint = null;
            Iterator<Timepoint<Double>> iterator = track.values().iterator();
            while (iterator.hasNext())
                lastPoint = iterator.next();

            int duration = track.getDuration();
            String name = getFullName(Measurements.DURATION, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, duration));
            name = getFullName(Measurements.FIRST_FRAME, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, firstPoint.getF()));
            name = getFullName(Measurements.LAST_FRAME, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, lastPoint.getF()));

            int nSpots = track.values().size();
            double detectionFraction = (double) nSpots / ((double) duration + 1);
            name = getFullName(Measurements.DETECTION_FRACTION, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, detectionFraction));
        }
    }

    public static void calculateVelocity(ObjI trackObject, Track track, boolean averageSubtracted) {
        if (track.size() <= 1) {
            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // Calculating track motion
            double distPerPxXY = trackObject.getDppXY();
            double distPerPxZ = trackObject.getDppZ();

            TreeMap<Integer, Double> xVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                    track.getX());
            TreeMap<Integer, Double> yVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                    track.getY());
            TreeMap<Integer, Double> zVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                    track.getZ());
            TreeMap<Integer, Double> speed = new InstantaneousSpeedCalculator().calculate(track);

            CumStat cumStatX = new CumStat();
            CumStat cumStatY = new CumStat();
            CumStat cumStatZ = new CumStat();
            CumStat cumStatSpeed = new CumStat();

            for (int frame : xVelocity.keySet()) {
                // The first value is set to zero
                if (frame == 0)
                    continue;
                cumStatX.addMeasure(xVelocity.get(frame));
                cumStatY.addMeasure(yVelocity.get(frame));
                cumStatZ.addMeasure(zVelocity.get(frame));
                cumStatSpeed.addMeasure(speed.get(frame));
            }

            String name = getFullName(Measurements.MEAN_X_VELOCITY_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean()));
            name = getFullName(Measurements.MEAN_X_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatX.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean()));
            name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatY.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY / distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY / distPerPxZ));
            name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatZ.getMean() * distPerPxXY));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean()));
            name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, cumStatSpeed.getMean() * distPerPxXY));

        }
    }

    public static void calculateRelativeTimepoint(ObjI trackObject, Track track, String inputSpotObjectsName,
            boolean averageSubtracted) {
        if (track.size() == 0)
            return;

        int firstTimepoint = track.values().iterator().next().getF();

        for (ObjI spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int currentTimepoint = spotObject.getT();
            String name = getFullName(Measurements.RELATIVE_FRAME, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, currentTimepoint - firstTimepoint));

        }
    }

    public static void calculateSpatialMeasurements(ObjI trackObject, Track track, boolean averageSubtracted) {
        if (track.size() == 0) {
            // Adding measurements to track objects
            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, Double.NaN));

        } else {
            // If the track has a single time-point there's no velocity to measure
            double distPerPxXY = trackObject.getDppXY();
            double euclideanDistance = track.getEuclideanDistance();
            double totalPathLength = new TotalPathLengthCalculator().calculate(track);

            String name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance));
            name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance * distPerPxXY));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength));
            name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, totalPathLength * distPerPxXY));
            name = getFullName(Measurements.DIRECTIONALITY_RATIO, averageSubtracted);
            trackObject.addMeasurement(new Measurement(name, euclideanDistance / totalPathLength));

        }
    }

    public double getTotalPathLength(Track track) {
        TreeMap<Integer, Double> steps = new InstantaneousStepSizeCalculator().calculate(track);

        double totalPathLength = 0;
        for (double value : steps.values())
            totalPathLength += value;

        return totalPathLength;

    }

    public static void calculateInstantaneousVelocity(ObjI trackObject, Track track, String inputSpotObjectsName,
            boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDppXY();
        double distPerPxZ = trackObject.getDppZ();

        TreeMap<Integer, Double> xVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                track.getX());
        TreeMap<Integer, Double> yVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                track.getY());
        TreeMap<Integer, Double> zVelocity = new InstantaneousVelocityCalculator().calculate(track.getF(),
                track.getZ());
        TreeMap<Integer, Double> speed = new InstantaneousSpeedCalculator().calculate(track);

        // Getting the first timepoint
        int minT = Integer.MAX_VALUE;
        for (ObjI spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            minT = Math.min(minT, spotObject.getT());
        }

        for (ObjI spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // For the first time-point set certain velocity measurements to Double.NaN
            // (rather than zero)
            if (t == minT) {
                String name = getFullName(Measurements.X_VELOCITY_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.X_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Y_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_SLICES, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.Z_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, Double.NaN));

            } else {
                String name = getFullName(Measurements.X_VELOCITY_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t)));
                name = getFullName(Measurements.X_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, xVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Y_VELOCITY_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t)));
                name = getFullName(Measurements.Y_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, yVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.Z_VELOCITY_SLICES, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY / distPerPxZ));
                name = getFullName(Measurements.Z_VELOCITY_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, zVelocity.get(t) * distPerPxXY));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t)));
                name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL, averageSubtracted);
                spotObject.addMeasurement(new Measurement(name, speed.get(t) * distPerPxXY));

            }
        }
    }

    public static void calculateInstantaneousSpatialMeasurements(ObjI trackObject, Track track,
            String inputSpotObjectsName, boolean averageSubtracted) {
        double distPerPxXY = trackObject.getDppXY();

        // Calculating rolling values
        TreeMap<Integer, Double> pathLength = new CumulativePathLengthCalculator().calculate(track);
        TreeMap<Integer, Double> euclidean = new EuclideanDistanceCalculator().calculate(track);
        TreeMap<Integer, Double> dirRatio = new DirectionalityRatioCalculator().calculate(track);
        TreeMap<Integer, Double> angularPersistence = new AngularPersistenceCalculator().calculate(track);

        // Applying the relevant measurement to each spot
        for (ObjI spotObject : trackObject.getChildren(inputSpotObjectsName).values()) {
            int t = spotObject.getT();

            // The remaining measurements are unaffected by whether it's the first
            // time-point
            String name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t)));
            name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, pathLength.get(t) * distPerPxXY));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t)));
            name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, euclidean.get(t) * distPerPxXY));
            name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, dirRatio.get(t)));
            name = getFullName(Measurements.ANGULAR_PERSISTENCE, averageSubtracted);
            spotObject.addMeasurement(new Measurement(name, angularPersistence.get(t)));

        }
    }

    public static void calculateOrientation(ObjsI objects, String orientationMode, boolean averageSubtracted) {
        String name = getFullName(Measurements.ORIENTATION, averageSubtracted);

        for (ObjI obj : objects.values()) {
            double angle = getInstantaneousOrientationRads(obj, objects, orientationMode);

            if (Double.isNaN(angle))
                obj.addMeasurement(new Measurement(name, Double.NaN));
            else
                obj.addMeasurement(new Measurement(name, Math.toDegrees(angle)));

        }
    }

    public static double getInstantaneousOrientationRads(ObjI object, ObjsI objects, String orientationMode) {
        double prevAngle = Double.NaN;
        double nextAngle = Double.NaN;

        if ((orientationMode.equals(OrientationModes.RELATIVE_TO_PREV)
                || orientationMode.equals(OrientationModes.RELATIVE_TO_BOTH))
                && object.getPreviousPartners(object.getName()) != null
                && object.getPreviousPartners(object.getName()).size() == 1) {
            ObjI prevObj = object.getPreviousPartners(object.getName()).getFirst();
            prevAngle = prevObj.calculateAngle2D(object);
        }

        if ((orientationMode.equals(OrientationModes.RELATIVE_TO_NEXT)
                || orientationMode.equals(OrientationModes.RELATIVE_TO_BOTH))
                && object.getNextPartners(object.getName()) != null
                && object.getNextPartners(object.getName()).size() == 1) {
            ObjI nextObj = object.getNextPartners(object.getName()).getFirst();
            nextAngle = object.calculateAngle2D(nextObj);
        }

        if (Double.isNaN(prevAngle) && Double.isNaN(nextAngle))
            return Double.NaN;
        else if (!Double.isNaN(prevAngle) && Double.isNaN(nextAngle))
            return prevAngle;
        else if (Double.isNaN(prevAngle) && !Double.isNaN(nextAngle))
            return nextAngle;
        else if (!Double.isNaN(prevAngle) && !Double.isNaN(nextAngle))
            return (prevAngle + nextAngle) / 2;

        return Double.NaN;

    }

    public static void identifyLeading(ObjsI objects, String orientationMode, boolean averageSubtracted) {
        for (ObjI obj : objects.values()) {
            // Get previously-calculated orientation
            String name = getFullName(Measurements.ORIENTATION, averageSubtracted);
            Measurement orientation = obj.getMeasurement(name);
            double angle = Double.NaN;
            if (orientation != null)
                angle = orientation.getValue();

            if (Double.isNaN(angle)) {
                // Adding furthest point coordinates to measurements
                name = getFullName(Measurements.LEADING_X_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, Double.NaN));

                name = getFullName(Measurements.LEADING_Y_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, Double.NaN));

                name = getFullName(Measurements.LEADING_Z_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, Double.NaN));

            } else {
                double xCent = obj.getXMean(true);
                double yCent = obj.getYMean(true);

                // Calculate line perpendicular to the direction of motion, passing through the
                // origin.
                Vector2D p = new Vector2D(xCent, yCent);
                Line midLine = new Line(p, angle + Math.PI / 2, 1E-2);

                // Calculating second line perpendicular to the direction of motion, but shifted
                // one pixel in the
                // direction of motion
                p = new Vector2D(xCent + Math.cos(angle), yCent + Math.sin(angle));
                Line refLine = new Line(p, angle + Math.PI / 2, 1E-2);

                // Iterating over all points, measuring their distance from the midLine
                Point<Integer> furthestPoint = null;
                double largestOffset = Double.NEGATIVE_INFINITY;
                for (Point<Integer> point : obj.getCoordinateSet()) {
                    double offset = midLine.getOffset(new Vector2D(point.getX(), point.getY()));

                    // Determining if the offset is positive or negative
                    double refOffset = refLine.getOffset(new Vector2D(point.getX(), point.getY()));
                    if (refOffset > offset)
                        offset = -offset;

                    if (offset > largestOffset) {
                        largestOffset = offset;
                        furthestPoint = point;
                    }
                }

                // Adding furthest point coordinates to measurements
                name = getFullName(Measurements.LEADING_X_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, furthestPoint.getX()));

                name = getFullName(Measurements.LEADING_Y_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, furthestPoint.getY()));

                name = getFullName(Measurements.LEADING_Z_PX, averageSubtracted);
                obj.addMeasurement(new Measurement(name, 0));

            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measures various motion metrics for tracked objects.  Global motion statistics (e.g. total path length) are stored as measurements associated with the input track objects, whilst instantaneous motion statistics (e.g. instantaneous x-velocity) are associated with the input spot objects.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input track objects
        String inputTrackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS, workspace);
        ObjsI trackObjects = workspace.getObjects(inputTrackObjectsName);

        // Getting input spot objects
        String inputSpotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS, workspace);
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION, workspace);
        boolean calculateOrientation = parameters.getValue(CALCULATE_ORIENTATION, workspace);
        boolean identifyLeading = parameters.getValue(IDENTIFY_LEADING_POINT, workspace);
        String orientationMode = parameters.getValue(ORIENTATION_MODE, workspace);

        // Always need to calculate orientation if identifying the leading point
        if (identifyLeading)
            calculateOrientation = true;

        // If necessary, creating the average track
        Track averageTrack = null;
        if (subtractAverage)
            averageTrack = createAverageTrack(trackObjects, inputSpotObjectsName);

        // Converting objects to Track class object
        for (ObjI trackObject : trackObjects.values()) {
            Track track = createTrack(trackObject, inputSpotObjectsName);

            // If necessary, applying the motion correction to the object
            if (subtractAverage)
                subtractAverageMotion(track, averageTrack);

            // Calculating the measurements
            calculateTemporalMeasurements(trackObject, track, subtractAverage);
            calculateVelocity(trackObject, track, subtractAverage);
            calculateSpatialMeasurements(trackObject, track, subtractAverage);
            calculateInstantaneousVelocity(trackObject, track, inputSpotObjectsName, subtractAverage);
            calculateInstantaneousSpatialMeasurements(trackObject, track, inputSpotObjectsName, subtractAverage);
            calculateRelativeTimepoint(trackObject, track, inputSpotObjectsName, subtractAverage);

        }

        // Determining the orientation
        if (calculateOrientation) {
            ObjsI spotObjects = workspace.getObjects(inputSpotObjectsName);
            calculateOrientation(spotObjects, orientationMode, subtractAverage);
        }

        // Determining the leading point in the object (to next object)
        if (identifyLeading) {
            ObjsI spotObjects = workspace.getObjects(inputSpotObjectsName);
            identifyLeading(spotObjects, orientationMode, subtractAverage);
        }

        if (showOutput)
            workspace.getObjects(inputSpotObjectsName).showMeasurements(this, modules);
        if (showOutput)
            trackObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_TRACK_OBJECTS, this));
        parameters.add(new ChildObjectsP(INPUT_SPOT_OBJECTS, this));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.addAll(initialisePositionParameters(this));
        parameters.add(new BooleanP(SUBTRACT_AVERAGE_MOTION, this, false));
        parameters.add(new BooleanP(CALCULATE_ORIENTATION, this, false));
        parameters.add(new BooleanP(IDENTIFY_LEADING_POINT, this, false));
        parameters.add(new ChoiceP(ORIENTATION_MODE, this, OrientationModes.RELATIVE_TO_BOTH, OrientationModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACK_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_SPOT_OBJECTS));

        String trackObjectsName = parameters.getValue(INPUT_TRACK_OBJECTS, workspace);
        ((ChildObjectsP) parameters.getParameter(INPUT_SPOT_OBJECTS)).setParentObjectsName(trackObjectsName);

        String spotObjectsName = parameters.getValue(INPUT_SPOT_OBJECTS, workspace);
        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.addAll(updateAndGetPositionParameters(spotObjectsName, parameters));
        returnedParameters.add(parameters.getParameter(SUBTRACT_AVERAGE_MOTION));
        returnedParameters.add(parameters.getParameter(CALCULATE_ORIENTATION));
        if ((boolean) parameters.getValue(IDENTIFY_LEADING_POINT, workspace)
                || (boolean) parameters.getValue(CALCULATE_ORIENTATION, workspace))
            returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
        returnedParameters.add(parameters.getParameter(IDENTIFY_LEADING_POINT));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputTrackObjects = parameters.getValue(INPUT_TRACK_OBJECTS, workspace);
        String inputSpotObjects = parameters.getValue(INPUT_SPOT_OBJECTS, workspace);
        boolean subtractAverage = parameters.getValue(SUBTRACT_AVERAGE_MOTION, workspace);

        String name = getFullName(Measurements.DIRECTIONALITY_RATIO, subtractAverage);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.EUCLIDEAN_DISTANCE_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.TOTAL_PATH_LENGTH_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.DURATION, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.FIRST_FRAME, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.LAST_FRAME, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_X_VELOCITY_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_X_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Y_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_SLICES, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_Z_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MEAN_INSTANTANEOUS_SPEED_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.DETECTION_FRACTION, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputTrackObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.X_VELOCITY_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.X_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Y_VELOCITY_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Y_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Z_VELOCITY_SLICES, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.Z_VELOCITY_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.INSTANTANEOUS_SPEED_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.CUMULATIVE_PATH_LENGTH_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_PX, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_EUCLIDEAN_DISTANCE_CAL, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ROLLING_DIRECTIONALITY_RATIO, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.ANGULAR_PERSISTENCE, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.RELATIVE_FRAME, subtractAverage);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(inputSpotObjects);
        returnedRefs.add(reference);

        if ((boolean) parameters.getValue(IDENTIFY_LEADING_POINT, workspace)
                || (boolean) parameters.getValue(CALCULATE_ORIENTATION, workspace)) {
            name = getFullName(Measurements.ORIENTATION, subtractAverage);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputSpotObjects);
            returnedRefs.add(reference);

        }

        if ((boolean) parameters.getValue(IDENTIFY_LEADING_POINT, workspace)) {
            name = getFullName(Measurements.LEADING_X_PX, subtractAverage);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputSpotObjects);
            returnedRefs.add(reference);

            name = getFullName(Measurements.LEADING_Y_PX, subtractAverage);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputSpotObjects);
            returnedRefs.add(reference);

            name = getFullName(Measurements.LEADING_Z_PX, subtractAverage);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputSpotObjects);
            returnedRefs.add(reference);

        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_TRACK_OBJECTS).setDescription(
                "Input track objects to measure motion for.  These must be specific \"track\" class objects as output by modules such as \""
                        + new TrackObjects(null).getName()
                        + "\".  The track objects are parents of individual timepoint instance objects, which are specified using the \""
                        + INPUT_SPOT_OBJECTS
                        + "\" parameter.  Global track measurements (e.g. total path length) are associated with the corresponding track objects.");

        parameters.get(INPUT_SPOT_OBJECTS).setDescription(
                "Input individual timepoint instance objects for the track.  These are the spatial records of the tracked objects in a single timepoint and are children of the track object specified by \""
                        + INPUT_TRACK_OBJECTS
                        + "\".  Instantaneous track measurements (e.g. instantaneous x-velociyty) are associated with the corresponding spot objects.");

        parameters.get(SUBTRACT_AVERAGE_MOTION).setDescription(
                "When selected, the average motion of all points between two frames is subtracted from the motion prior to calculation of any track measurements.  This can be used as a crude form of drift correction; however, it only works for global drift (where the whole sample moved together) and is less robust with few tracked objects.  Ideally, drift would be removed from the input images using image registration prior to object detection.");

        parameters.get(IDENTIFY_LEADING_POINT).setDescription(
                "When selected, the \"leading point\" of each object in the track will be determined and stored as X,Y,Z coordinate measurements associated with the relevant object.  The orientation of the track at that location will also be calculated.  In this instance, \"leading point\" refers to the front-most point in the object when considering the direction the object is moving in that frame.  This can be calculated relative to the previous frame, next frame or both previous and next frames (controlled by the \""
                        + ORIENTATION_MODE + "\" parameter).");

        parameters.get(ORIENTATION_MODE)
                .setDescription("If calculating the leading point of each object in each track (\""
                        + IDENTIFY_LEADING_POINT
                        + "\" selected), this controls whether the instantaneous orientation of the track is determined relative to the previous frame, next frame or both previous and next frames.");

    }
}
