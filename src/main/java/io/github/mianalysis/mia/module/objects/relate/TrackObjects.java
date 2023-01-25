// TODO: Could do with spinning the core element of this into a series of Track classes in the Common library
// TODO: Get direction costs working in 3D

package io.github.mianalysis.mia.module.objects.relate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.DefaultCostMatrixCreator;
import ij.ImagePlus;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.imagej.LUTs;
import io.github.sjcross.sjcommon.mathfunc.Indexer;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

/**
 * Created by sc13967 on 20/09/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class TrackObjects extends Module {
    public static final String INPUT_SEPARATOR = "Object input/output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TRACK_OBJECTS = "Output track objects";

    public static final String SPATIAL_SEPARATOR = "Spatial cost";
    public static final String LINKING_METHOD = "Linking method";
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";

    public static final String TEMPORAL_SEPARATOR = "Temporal cost";
    public static final String FRAME_GAP_WEIGHTING = "Frame gap weighting";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";
    public static final String FAVOUR_ESTABLISHED_TRACKS = "Favour established tracks";
    public static final String TRACK_LENGTH_WEIGHTING = "Track length weighting";

    public static final String VOLUME_SEPARATOR = "Volume cost";
    public static final String USE_VOLUME = "Use volume (minimise volume change)";
    public static final String VOLUME_WEIGHTING = "Volume weighting";
    public static final String MAXIMUM_VOLUME_CHANGE = "Maximum volume change (px^3)";

    public static final String DIRECTION_SEPARATOR = "Direction cost";
    public static final String DIRECTION_WEIGHTING_MODE = "Direction weighting mode";
    public static final String ORIENTATION_RANGE_MODE = "Orientation range mode";
    public static final String PREFERRED_DIRECTION = "Preferred direction";
    public static final String DIRECTION_TOLERANCE = "Direction tolerance";
    public static final String DIRECTION_WEIGHTING = "Direction weighting";

    public static final String MEASUREMENT_SEPARATOR = "Measurement cost";
    public static final String USE_MEASUREMENT = "Use measurement (minimise change)";
    public static final String MEASUREMENT = "Measurement";
    public static final String MEASUREMENT_WEIGHTING = "Measurement weighting";
    public static final String MAXIMUM_MEASUREMENT_CHANGE = "Maximum measurement change";

    public TrackObjects(Modules modules) {
        super("Track objects", modules);
    }

    public interface LinkingMethods {
        String ABSOLUTE_OVERLAP = "Absolute overlap";
        String CENTROID = "Centroid";

        String[] ALL = new String[] { ABSOLUTE_OVERLAP, CENTROID };
    }

    public interface DirectionWeightingModes {
        String NONE = "None";
        String ABSOLUTE_ORIENTATION = "Absolute orientation 2D";
        String RELATIVE_TO_PREVIOUS_STEP = "Relative to previous step";

        String[] ALL = new String[] { NONE, ABSOLUTE_ORIENTATION, RELATIVE_TO_PREVIOUS_STEP };

    }

    public interface OrientationRangeModes {
        String NINETY = "-90 to 90 degs";
        String ONE_EIGHTY = "-180 to 180 degs";

        String[] ALL = new String[] { NINETY, ONE_EIGHTY };

    }

    public interface Measurements {
        String TRACK_PREV_ID = "TRACKING // PREVIOUS_OBJECT_IN_TRACK_ID";
        String TRACK_NEXT_ID = "TRACKING // NEXT_OBJECT_IN_TRACK_ID";

    }

    public ArrayList<Obj>[] getCandidateObjects(Objs inputObjects, int t1, int t2) {
        // Creating a pair of ArrayLists to store the current and previous objects
        ArrayList<Obj>[] objects = new ArrayList[2];
        objects[0] = new ArrayList<>(); // Previous objects
        objects[1] = new ArrayList<>(); // Current objects

        // Include objects from the previous and current frames that haven't been linked
        for (Obj inputObject : inputObjects.values()) {
            if (inputObject.getT() == t1 && inputObject.getMeasurement(Measurements.TRACK_NEXT_ID) == null) {
                objects[0].add(inputObject);

            } else if (inputObject.getT() == t2 && inputObject.getMeasurement(Measurements.TRACK_PREV_ID) == null) {
                objects[1].add(inputObject);

            }
        }

        return objects;

    }

    public ArrayList<Linkable> calculateCostMatrix(ArrayList<Obj> prevObjects, ArrayList<Obj> currObjects,
            Workspace workspace,
            @Nullable Objs inputObjects, @Nullable int[][] spatialLimits) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        boolean useVolume = parameters.getValue(USE_VOLUME, workspace);
        double frameGapWeighting = parameters.getValue(FRAME_GAP_WEIGHTING, workspace);
        double volumeWeighting = parameters.getValue(VOLUME_WEIGHTING, workspace);
        double maxVolumeChange = parameters.getValue(MAXIMUM_VOLUME_CHANGE, workspace);
        String directionWeightingMode = parameters.getValue(DIRECTION_WEIGHTING_MODE, workspace);
        String orientationRangeMode = parameters.getValue(ORIENTATION_RANGE_MODE, workspace);
        double preferredDirection = parameters.getValue(PREFERRED_DIRECTION, workspace);
        double directionTolerance = parameters.getValue(DIRECTION_TOLERANCE, workspace);
        double directionWeighting = parameters.getValue(DIRECTION_WEIGHTING, workspace);
        boolean favourEstablished = parameters.getValue(FAVOUR_ESTABLISHED_TRACKS, workspace);
        double durationWeighting = parameters.getValue(TRACK_LENGTH_WEIGHTING, workspace);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT, workspace);
        String measurementName = parameters.getValue(MEASUREMENT, workspace);
        double measurementWeighting = parameters.getValue(MEASUREMENT_WEIGHTING, workspace);
        double maxMeasurementChange = parameters.getValue(MAXIMUM_MEASUREMENT_CHANGE, workspace);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP, workspace);
        double maxDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE, workspace);

        String linkingMethod = parameters.getValue(LINKING_METHOD, workspace);

        // Creating the ArrayList containing linkables
        ArrayList<Linkable> linkables = new ArrayList<>();

        for (int curr = 0; curr < currObjects.size(); curr++) {
            for (int prev = 0; prev < prevObjects.size(); prev++) {
                Obj prevObj = prevObjects.get(prev);
                Obj currObj = currObjects.get(curr);

                // Calculating main spatial cost
                double spatialCost = 0;
                switch (linkingMethod) {
                    case LinkingMethods.CENTROID:
                        double separation = prevObj.getCentroidSeparation(currObj, true);
                        spatialCost = separation > maxDist ? Double.MAX_VALUE : separation;
                        break;
                    case LinkingMethods.ABSOLUTE_OVERLAP:
                        float overlap = getAbsoluteOverlap(prevObjects.get(prev), currObjects.get(curr), spatialLimits);
                        spatialCost = overlap == 0 ? Float.MAX_VALUE : 1 / overlap;
                        break;
                }

                // Calculating additional costs
                double frameGapCost = getFrameGapCost(prevObj, currObj);
                double durationCost = favourEstablished ? getTrackDurationCost(prevObj, trackObjectsName) : 0;
                double volumeCost = useVolume ? getVolumeCost(prevObj, currObj) : 0;
                double measurementCost = useMeasurement ? getMeasurementCost(prevObj, currObj, measurementName) : 0;
                double directionCost = 0;
                switch (directionWeightingMode) {
                    case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                        directionCost = getAbsoluteOrientationCost(prevObj, currObj, orientationRangeMode,
                                preferredDirection);
                        break;
                    case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                        directionCost = getPreviousStepDirectionCost(prevObj, currObj, inputObjects);
                        break;
                }

                // Testing spatial validity
                boolean linkValid = true;
                switch (linkingMethod) {
                    case LinkingMethods.ABSOLUTE_OVERLAP:
                        linkValid = testOverlapValidity(prevObj, currObj, minOverlap, spatialLimits);
                        break;
                    case LinkingMethods.CENTROID:
                        linkValid = testSeparationValidity(prevObj, currObj, maxDist);
                        break;
                }

                // Testing volume change
                if (linkValid && useVolume)
                    linkValid = testVolumeValidity(prevObj, currObj, maxVolumeChange);

                // Testing measurement change
                if (linkValid && useMeasurement)
                    linkValid = testMeasurementValidity(prevObj, currObj, measurementName, maxMeasurementChange);

                // Testing orientation
                if (linkValid) {
                    switch (directionWeightingMode) {
                        case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                            linkValid = testDirectionTolerance(directionCost, directionTolerance);
                            break;
                        case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                            linkValid = testDirectionTolerance(directionCost, directionTolerance);
                            break;
                    }
                }

                // Assigning costs if the link is valid (set to Double.NaN otherwise)
                if (linkValid) {
                    double cost = spatialCost
                            + frameGapCost * frameGapWeighting
                            + durationCost * durationWeighting
                            + volumeCost * volumeWeighting
                            + directionCost * directionWeighting
                            + measurementCost * measurementWeighting;

                    // Linker occasionally fails on zero-costs, so adding 0.1 to all values
                    cost = cost + 0.1;
                    linkables.add(new Linkable(cost, currObj.getID(), prevObj.getID()));
                }
            }
        }

        return linkables;

    }

    public static float getAbsoluteOverlap(Obj prevObj, Obj currObj, int[][] spatialLimits) {
        // Getting coordinates for each object
        TreeSet<Point<Integer>> prevPoints = prevObj.getPoints();
        TreeSet<Point<Integer>> currPoints = currObj.getPoints();

        // Indexer gives a single value for coordinates. Will use a HashSet to prevent
        // index duplicates.
        Indexer indexer = new Indexer(spatialLimits[0][1] + 1, spatialLimits[1][1] + 1, spatialLimits[2][1] + 1);

        int prevSize = prevPoints.size();
        int currSize = currPoints.size();

        // Combining the coordinates into a single ArrayList. This will prevent
        // duplicates
        HashSet<Integer> indices = new HashSet<>();
        for (Point<Integer> prevPoint : prevPoints) {
            int[] point = new int[] { prevPoint.getX(), prevPoint.getY(), prevPoint.getZ() };
            indices.add(indexer.getIndex(point));
        }

        for (Point<Integer> currPoint : currPoints) {
            int[] point = new int[] { currPoint.getX(), currPoint.getY(), currPoint.getZ() };
            indices.add(indexer.getIndex(point));
        }

        return prevSize + currSize - indices.size();

    }

    public static double getFrameGapCost(Obj prevObj, Obj currObj) {
        // Calculating volume weighting
        double prevT = prevObj.getT();
        double currT = currObj.getT();

        return Math.abs(prevT - currT);

    }

    public static double getTrackDurationCost(Obj prevObj, String trackObjectsName) {
        // Scores between 0 (track present since start of time-series) and 1 (no track
        // assigned)
        Obj prevTrack = prevObj.getParent(trackObjectsName);
        if (prevTrack == null)
            return 1;

        // Getting track length as a proportion of all previous frames
        double dur = (double) prevTrack.getChildren(prevObj.getName()).size();
        double maxDur = (double) prevObj.getT() + 1;

        return 1 - (dur / maxDur);

    }

    public static double getVolumeCost(Obj prevObj, Obj currObj) {
        // Calculating volume weighting
        double prevVol = prevObj.size();
        double currVol = currObj.size();

        return Math.abs(prevVol - currVol);

    }

    public static double getMeasurementCost(Obj prevObj, Obj currObj, String measurementName) {
        Measurement currMeasurement = currObj.getMeasurement(measurementName);
        Measurement prevMeasurement = prevObj.getMeasurement(measurementName);

        if (currMeasurement == null || prevMeasurement == null)
            return Double.NaN;
        double currMeasurementValue = currMeasurement.getValue();
        double prevMeasurementValue = prevMeasurement.getValue();

        return Math.abs(prevMeasurementValue - currMeasurementValue);

    }

    public static double getAbsoluteOrientationCost(Obj prevObj, Obj currObj, String orientationRangeMode,
            double preferredDirection) {
        // Getting centroid coordinates for three points
        double prevXCent = prevObj.getXMean(true);
        double prevYCent = prevObj.getYMean(true);
        double currXCent = currObj.getXMean(true);
        double currYCent = currObj.getYMean(true);
        double preferredX = Math.cos(Math.toRadians(preferredDirection));
        double preferredY = Math.sin(Math.toRadians(preferredDirection));

        Vector2D v1f = new Vector2D(preferredX, preferredY);
        Vector2D v1b = new Vector2D(-preferredX, -preferredY);

        // Having these in this order gives us positive preferred directions above the
        // x-axis
        Vector2D v2 = new Vector2D(currXCent - prevXCent, prevYCent - currYCent);

        // MathArithmeticException thrown if two points are coincident. In these cases,
        // give a cost of 0.
        try {
            switch (orientationRangeMode) {
                case OrientationRangeModes.NINETY:
                    return Math.min(Math.abs(Vector2D.angle(v1f, v2)), Math.abs(Vector2D.angle(v1b, v2)));
                case OrientationRangeModes.ONE_EIGHTY:
                default:
                    return Math.abs(Vector2D.angle(v1f, v2));
            }

        } catch (MathArithmeticException e) {
            return 0;
        }
    }

    public static double getPreviousStepDirectionCost(Obj prevObj, Obj currObj, Objs inputObjects) {
        // Get direction of previous object
        Measurement prevPrevObjMeas = prevObj.getMeasurement(Measurements.TRACK_PREV_ID);

        // If the previous object doesn't have a previous object (i.e. it was the
        // first), return a score of 0
        if (prevPrevObjMeas == null)
            return 0;

        // Getting the previous-previous object
        int prevPrevObjID = (int) prevPrevObjMeas.getValue();
        Obj prevPrevObj = inputObjects.get(prevPrevObjID);

        // Getting centroid coordinates for three points
        double prevXCent = prevObj.getXMean(true);
        double prevYCent = prevObj.getYMean(true);
        double currXCent = currObj.getXMean(true);
        double currYCent = currObj.getYMean(true);
        double prevPrevXCent = prevPrevObj.getXMean(true);
        double prevPrevYCent = prevPrevObj.getYMean(true);

        Vector2D v1 = new Vector2D(prevXCent - prevPrevXCent, prevYCent - prevPrevYCent);
        Vector2D v2 = new Vector2D(currXCent - prevXCent, currYCent - prevYCent);

        // MathArithmeticException thrown if two points are coincident. In these cases,
        // give a cost of 0.
        try {
            return Math.abs(Vector2D.angle(v1, v2));
        } catch (MathArithmeticException e) {
            return 0;
        }
    }

    public static boolean testDirectionTolerance(double directionCost, double directionTolerance) {
        return Math.abs(Math.toDegrees(directionCost)) <= directionTolerance;
    }

    public static boolean testSeparationValidity(Obj prevObj, Obj currObj, double maxDist) {
        double dist = prevObj.getCentroidSeparation(currObj, true);
        return dist <= maxDist;
    }

    public static boolean testOverlapValidity(Obj prevObj, Obj currObj, double minOverlap, int[][] spatialLimits) {
        double overlap = getAbsoluteOverlap(prevObj, currObj, spatialLimits);
        return overlap != 0 && overlap >= minOverlap;

    }

    public static boolean testVolumeValidity(Obj prevObj, Obj currObj, double maxVolumeChange) {
        double volumeChange = getVolumeCost(prevObj, currObj);
        return volumeChange <= maxVolumeChange;
    }

    public static boolean testMeasurementValidity(Obj prevObj, Obj currObj, String measurementName,
            double maxMeasurementChange) {
        double measurementChange = getMeasurementCost(prevObj, currObj, measurementName);
        return measurementChange <= maxMeasurementChange;
    }

    public static void linkObjects(Obj prevObj, Obj currObj, String trackObjectsName) {
        // Getting the track object from the previous-frame object
        Obj track = prevObj.getParent(trackObjectsName);

        // Setting relationship between the current object and track
        track.addChild(currObj);
        currObj.addParent(track);

        // Adding partner relationships between adjacent points in the track
        prevObj.addPartner(currObj);
        currObj.addPartner(prevObj);

        // Adding references to each other
        prevObj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, currObj.getID()));
        currObj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, prevObj.getID()));

    }

    public static void createNewTrack(Obj currObj, Objs trackObjects) {
        // Creating a new track object
        Obj track = trackObjects.createAndAddNewObject(VolumeType.POINTLIST);

        // Setting relationship between the current object and track
        track.addChild(currObj);
        currObj.addParent(track);

    }

    public static void showObjects(Objs spotObjects, String trackObjectsName) {
        HashMap<Integer, Float> hues = ColourFactory.getParentIDHues(spotObjects, trackObjectsName, true);

        // Creating a parent-ID encoded image of the objects
        Image dispImage = spotObjects.convertToImage(spotObjects.getName(), hues, 32, false);

        // Displaying the overlay
        ImagePlus ipl = dispImage.getImagePlus();
        ipl.setPosition(1, 1, 1);
        ipl.setLut(LUTs.Random(true));
        ipl.updateChannelAndDraw();
        ipl.show();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getDescription() {
        return "Track objects between frames.  Tracks are produced as separate \"parent\" objects to the \"child\" "
                + "spots.  Track objects only serve to link different timepoint instances of objects together.  As such, "
                + "track objects store no coordinate information." + "<br><br>"
                + "Uses the <a href=\"https://imagej.net/plugins/trackmate/\">TrackMate</a> implementation of the Jaqaman linear assignment problem solving algorithm "
                + "(Jaqaman, et al., Nature Methods, 2008).  The implementation utilises sparse matrices for calculating "
                + "costs in order to minimise memory overhead." + "<br><br>"
                + "Note: Leading point determination currently only works in 2D";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        int maxMissingFrames = parameters.getValue(MAXIMUM_MISSING_FRAMES, workspace);

        // Getting objects
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);
        Objs trackObjects = new Objs(trackObjectsName, inputObjects);
        workspace.addObjects(trackObjects);

        // If there are no input objects, create a blank track set and skip this module
        if (inputObjects == null)
            return Status.PASS;
        if (inputObjects.size() == 0)
            return Status.PASS;

        // Clearing previous relationships and measurements (in case module has been
        // generateModuleList before)
        for (Obj inputObj : inputObjects.values()) {
            inputObj.removeMeasurement(Measurements.TRACK_NEXT_ID);
            inputObj.removeMeasurement(Measurements.TRACK_PREV_ID);
            inputObj.removeParent(trackObjectsName);
        }

        // Finding the spatial and frame frame limits of all objects in the inputObjects
        // set
        int[][] spatialLimits = inputObjects.getSpatialLimits();
        int[] frameLimits = inputObjects.getTemporalLimits();

        // Creating new track objects for all objects in the first frame
        for (Obj inputObj : inputObjects.values()) {
            if (inputObj.getT() == frameLimits[0]) {
                createNewTrack(inputObj, trackObjects);
            }
        }

        for (int t2 = frameLimits[0] + 1; t2 <= frameLimits[1]; t2++) {
            writeProgressStatus(t2 + 1, frameLimits[1] + 1, "frames");

            // Testing the previous permitted frames for links
            for (int t1 = t2 - 1; t1 >= t2 - 1 - maxMissingFrames; t1--) {
                ArrayList<Obj>[] nPObjects = getCandidateObjects(inputObjects, t1, t2);

                // If no previous or current objects were found no linking takes place
                if (nPObjects[0].size() == 0 || nPObjects[1].size() == 0) {
                    if (t1 == t2 - 1 - maxMissingFrames || t1 == 0) {
                        // Creating new tracks for current objects that have no chance of being linked
                        // in other frames
                        for (int curr = 0; curr < nPObjects[1].size(); curr++)
                            createNewTrack(nPObjects[1].get(curr), trackObjects);

                        break;
                    }
                    continue;
                }

                // Calculating distances between objects and populating the cost matrix
                ArrayList<Linkable> linkables = calculateCostMatrix(nPObjects[0], nPObjects[1], workspace, inputObjects,
                        spatialLimits);
                // Check if there are potential links, if not, skip to the next frame
                if (linkables.size() > 0) {
                    DefaultCostMatrixCreator<Integer, Integer> creator = RelateOneToOne.getCostMatrixCreator(linkables);
                    JaqamanLinker<Integer, Integer> linker = new JaqamanLinker<>(creator);
                    if (!linker.checkInput()) {
                        MIA.log.writeError(linker.getErrorMessage());
                        return Status.FAIL;
                    }
                    if (!linker.process()) {
                        MIA.log.writeError(linker.getErrorMessage());
                        return Status.FAIL;
                    }
                    Map<Integer, Integer> assignment = linker.getResult();

                    // Applying the calculated assignments as relationships
                    for (int ID1 : assignment.keySet()) {
                        int ID2 = assignment.get(ID1);
                        Obj currObj = inputObjects.get(ID1);
                        Obj prevObj = inputObjects.get(ID2);
                        linkObjects(prevObj, currObj, trackObjectsName);
                    }
                }

                // Assigning any objects in the current frame without a track as new tracks
                for (Obj currObj : nPObjects[1]) {
                    if (currObj.getParent(trackObjectsName) == null)
                        createNewTrack(currObj, trackObjects);
                }
            }
        }

        // If selected, showing an overlay of the tracked objects
        if (showOutput)
            showObjects(inputObjects, trackObjectsName);

        // Adding track objects to the workspace
        writeStatus("Assigned " + trackObjects.size() + " tracks");

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputTrackObjectsP(TRACK_OBJECTS, this));

        parameters.add(new SeparatorP(SPATIAL_SEPARATOR, this));
        parameters.add(new ChoiceP(LINKING_METHOD, this, LinkingMethods.CENTROID, LinkingMethods.ALL));
        parameters.add(new DoubleP(MINIMUM_OVERLAP, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_LINKING_DISTANCE, this, 20.0));

        parameters.add(new SeparatorP(TEMPORAL_SEPARATOR, this));
        parameters.add(new IntegerP(MAXIMUM_MISSING_FRAMES, this, 0));
        parameters.add(new DoubleP(FRAME_GAP_WEIGHTING, this, 0.0));
        parameters.add(new BooleanP(FAVOUR_ESTABLISHED_TRACKS, this, false));
        parameters.add(new DoubleP(TRACK_LENGTH_WEIGHTING, this, 1.0));

        parameters.add(new SeparatorP(VOLUME_SEPARATOR, this));
        parameters.add(new BooleanP(USE_VOLUME, this, false));
        parameters.add(new DoubleP(VOLUME_WEIGHTING, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_VOLUME_CHANGE, this, 1.0));

        parameters.add(new SeparatorP(DIRECTION_SEPARATOR, this));
        parameters.add(
                new ChoiceP(DIRECTION_WEIGHTING_MODE, this, DirectionWeightingModes.NONE, DirectionWeightingModes.ALL));
        parameters.add(
                new ChoiceP(ORIENTATION_RANGE_MODE, this, OrientationRangeModes.ONE_EIGHTY, OrientationRangeModes.ALL));
        parameters.add(new DoubleP(PREFERRED_DIRECTION, this, 0.0));
        parameters.add(new DoubleP(DIRECTION_TOLERANCE, this, 90.0));
        parameters.add(new DoubleP(DIRECTION_WEIGHTING, this, 1.0));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(USE_MEASUREMENT, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new DoubleP(MEASUREMENT_WEIGHTING, this, 1.0));
        parameters.add(new DoubleP(MAXIMUM_MEASUREMENT_CHANGE, this, 1.0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPATIAL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINKING_METHOD));
        switch ((String) parameters.getValue(LINKING_METHOD, workspace)) {
            case LinkingMethods.ABSOLUTE_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP));
                break;

            case LinkingMethods.CENTROID:
                returnedParameters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(TEMPORAL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MAXIMUM_MISSING_FRAMES));
        returnedParameters.add(parameters.getParameter(FRAME_GAP_WEIGHTING));
        returnedParameters.add(parameters.getParameter(FAVOUR_ESTABLISHED_TRACKS));
        if ((boolean) returnedParameters.getValue(FAVOUR_ESTABLISHED_TRACKS, workspace))
            returnedParameters.add(parameters.getParameter(TRACK_LENGTH_WEIGHTING));

        returnedParameters.add(parameters.getParameter(VOLUME_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_VOLUME));
        if ((boolean) returnedParameters.getValue(USE_VOLUME, workspace)) {
            returnedParameters.add(parameters.getParameter(VOLUME_WEIGHTING));
            returnedParameters.add(parameters.getParameter(MAXIMUM_VOLUME_CHANGE));
        }

        returnedParameters.add(parameters.getParameter(DIRECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING_MODE));
        switch ((String) parameters.getValue(DIRECTION_WEIGHTING_MODE, workspace)) {
            case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                returnedParameters.add(parameters.getParameter(ORIENTATION_RANGE_MODE));
                returnedParameters.add(parameters.getParameter(PREFERRED_DIRECTION));
                returnedParameters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;

            case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                returnedParameters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParameters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;
        }

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(USE_MEASUREMENT));
        if ((boolean) returnedParameters.getValue(USE_MEASUREMENT, workspace)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT));
            returnedParameters.add(parameters.getParameter(MEASUREMENT_WEIGHTING));
            returnedParameters.add(parameters.getParameter(MAXIMUM_MEASUREMENT_CHANGE));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        ObjMeasurementRef trackPrevID = objectMeasurementRefs.getOrPut(Measurements.TRACK_PREV_ID);
        ObjMeasurementRef trackNextID = objectMeasurementRefs.getOrPut(Measurements.TRACK_NEXT_ID);

        trackPrevID.setObjectsName(inputObjectsName);
        trackNextID.setObjectsName(inputObjectsName);

        returnedRefs.add(trackPrevID);
        returnedRefs.add(trackNextID);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        String trackObjectsName = parameters.getValue(TRACK_OBJECTS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        returnedRelationships.add(parentChildRefs.getOrPut(trackObjectsName, inputObjectsName));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;
        PartnerRefs returnedRelationships = new PartnerRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        returnedRelationships.add(partnerRefs.getOrPut(inputObjectsName, inputObjectsName));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects present in individual timepoints which will be tracked across multiple frames.  These objects will become children of their assigned \"track\" parent.");

        parameters.get(TRACK_OBJECTS).setDescription(
                "Output track objects to be stored in the workspace.  These objects will contain no spatial information, rather they act as (parent) linking  objects for the individual timepoint instances of the tracked object.");

        parameters.get(MAXIMUM_MISSING_FRAMES).setDescription(
                "Maximum number of missing frames for an object to still be tracked.  A single object undetected for longer than this would be identified as two separate tracks.");

        parameters.get(LINKING_METHOD).setDescription("The spatial cost for linking objects together:<br><ul>"

                + "<li>\"" + LinkingMethods.ABSOLUTE_OVERLAP
                + "\" Tracks will be assigned in order to maxmimise the spatial overlap between objects in adjacent frames.  This linking method uses the full 3D volume of the objects being tracked.  Note: There is no consideration of distance between objects, so non-overlapping objects next to each other will score equally to non-overlapping objects far away (not taking additional linking weights and restrictions into account).</li>"

                + "<li>\"" + LinkingMethods.CENTROID
                + "\" Tracks will be assigned in order to minimise the total distance between object centroids.  This linking method doesn't take object size and shape into account (unless included via volume weighting), but will work at all object separations.</li></ul>");

        parameters.get(MINIMUM_OVERLAP).setDescription("If \"" + LINKING_METHOD + "\" is set to \""
                + LinkingMethods.ABSOLUTE_OVERLAP
                + "\", this is the minimum absolute spatial overlap (number of coincident pixels/voxels) two objects must have for them to be considered as candidates for linking.");

        parameters.get(MAXIMUM_LINKING_DISTANCE).setDescription("If \"" + LINKING_METHOD + "\" is set to \""
                + LinkingMethods.CENTROID
                + "\", this is the minimum spatial separation (pixel units) two objects must have for them to be considered as candidates for linking.");

        parameters.get(FRAME_GAP_WEIGHTING).setDescription(
                "When non-zero, an additional cost will be included that penalises linking objects with large temporal separations.  The frame gap between candidate objects will be multiplied by this weight.  For example, if calculating spatial costs using centroid spatial separation, a frame gap weight of 1 will equally weight 1 frame of temporal separation to 1 pixel of spatial separation.  The larger the weight, the more this frame gap will contribute towards the total linking cost.");

        parameters.get(FAVOUR_ESTABLISHED_TRACKS).setDescription(
                "When selected, points will be preferentially linked to tracks containing more previous points.  For example, in cases where an object was detected twice in one timepoint this will favour linking to the original track, rather than establishing the on-going track from the new point.");

        parameters.get(TRACK_LENGTH_WEIGHTING).setDescription("If \"" + FAVOUR_ESTABLISHED_TRACKS
                + "\" is selected this is the weight assigned to the existing track duration.  Track duration costs are calculated as 1 minus the ratio of frames in which the track was detected (up to the previous time-point).");

        parameters.get(USE_VOLUME).setDescription(
                "When enabled, the 3D volume of the objects being linked will contribute towards linking costs.");

        parameters.get(VOLUME_WEIGHTING).setDescription("If \"" + USE_VOLUME
                + "\" is enabled, this is the weight assigned to the difference in volume of the candidate objects for linking.  The difference in volume between candidate objects is multiplied by this weight.  The larger the weight, the more this difference in volume will contribute towards the total linking cost.");

        parameters.get(MAXIMUM_VOLUME_CHANGE).setDescription("If \"" + USE_VOLUME
                + "\" is enabled, the maximum difference in volume between candidate objects can be specified.  This maximum volume change is specified in pixel units.");

        parameters.get(DIRECTION_WEIGHTING_MODE).setDescription(
                "Controls whether cost terms will be included based on the direction a tracked object is moving in:<br><ul>"

                        + "<li>\"" + DirectionWeightingModes.NONE
                        + "\" No direction-based cost terms will be included.</li>"

                        + "<li>\"" + DirectionWeightingModes.ABSOLUTE_ORIENTATION
                        + "\" Costs will be calculated based on the absolute orientation a candidate object would be moving in.  For example, if objects are known to be moving in one particular direction, this can favour links moving that way rather than the opposite direction.</li>"

                        + "<li>\"" + DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP
                        + "\" Costs will be calculated based on the previous trajectory of a track.  This can be used to minimise rapid changes in direction if tracked objects are expected to move smoothly.</li></ul>");

        parameters.get(PREFERRED_DIRECTION).setDescription("If \"" + DIRECTION_WEIGHTING_MODE + "\" is set to \""
                + DirectionWeightingModes.ABSOLUTE_ORIENTATION
                + "\", this is the preferred direction that a track should be moving in.  Orientation is measured in degree units and is positive above the x-axis and negative below it.");

        parameters.get(DIRECTION_TOLERANCE).setDescription("If using directional weighting (\""
                + DIRECTION_WEIGHTING_MODE + "\" not set to \"" + DirectionWeightingModes.NONE
                + "\"), this is the maximum deviation in direction from the preferred direction that a candidate object can have.  For absolute linking, this is relative to the preferred direction and for relative linking, this is relative to the previous frame.");

        parameters.get(DIRECTION_WEIGHTING).setDescription("If using directional weighting (\""
                + DIRECTION_WEIGHTING_MODE + "\" not set to \"" + DirectionWeightingModes.NONE
                + "\"), the angular difference (in degrees) between the candidate track direction and the reference direction will be muliplied by this weight.  The larger the weight, the more this angular difference will contribute towards the total linking cost.");

        parameters.get(USE_MEASUREMENT).setDescription(
                "When selected, an additional cost can be included based on a measurement assigned to each object.  This allows for tracking to favour minimising variation in this measurement.");

        parameters.get(MEASUREMENT).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, this is the measurement (associated with the input objects) for which variation within a track will be minimised.");

        parameters.get(MEASUREMENT_WEIGHTING).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, the difference in measurement associated with a candidate object and the previous instance in a target track will be multiplied by this value.  The larger the weight, the more this difference in measurement will contribute towards the total linking cost.");

        parameters.get(MAXIMUM_MEASUREMENT_CHANGE).setDescription("If \"" + USE_MEASUREMENT
                + "\" is selected, this is the maximum amount the measurement can change between consecutive instances in a track.  Variations greater than this will result in the track being split into two.");

    }
}