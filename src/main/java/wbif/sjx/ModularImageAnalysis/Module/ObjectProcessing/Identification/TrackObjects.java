// TODO: Could do with spinning the core element of this into a series of Track classes in the Common library
// TODO: Get direction costs working in 3D

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import blogspot.software_and_algorithms.stern_library.optimization.HungarianAlgorithm;
import ij.ImagePlus;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.LUTs;
import wbif.sjx.common.Object.Point;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by sc13967 on 20/09/2017.
 */
public class TrackObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_METHOD = "Linking method";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String USE_VOLUME = "Use volume (minimise volume change)";
    public static final String VOLUME_WEIGHTING = "Volume weighting";
    public static final String DIRECTION_WEIGHTING_MODE = "Direction weighting mode";
    public static final String PREFERRED_DIRECTION = "Preferred direction (-180 to 180 degs)";
    public static final String DIRECTION_TOLERANCE = "Direction tolerance";
    public static final String DIRECTION_WEIGHTING = "Direction weighting";
    public static final String USE_MEASUREMENT = "Use measurement (minimise change)";
    public static final String MEASUREMENT = "Measurement";
    public static final String MEASUREMENT_WEIGHTING = "Measurement weighting";
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";
    public static final String IDENTIFY_LEADING_POINT = "Identify leading point";
    public static final String ORIENTATION_MODE = "Orientation mode";


    public interface LinkingMethods {
        String ABSOLUTE_OVERLAP = "Absolute overlap";
        String CENTROID = "Centroid";

        String[] ALL = new String[]{ABSOLUTE_OVERLAP,CENTROID};
    }

    public interface DirectionWeightingModes {
        String NONE = "None";
        String ABSOLUTE_ORIENTATION = "Absolute orientation 2D";
        String RELATIVE_TO_PREVIOUS_STEP = "Relative to previous step";

        String[] ALL = new String[]{NONE,ABSOLUTE_ORIENTATION,RELATIVE_TO_PREVIOUS_STEP};

    }

    public interface OrientationModes {
        String RELATIVE_TO_BOTH = "Relative to both points";
        String RELATIVE_TO_PREV = "Relative to previous point";
        String RELATIVE_TO_NEXT = "Relative to next point";

        String[] ALL = new String[]{RELATIVE_TO_BOTH,RELATIVE_TO_PREV,RELATIVE_TO_NEXT};
    }

    public interface Measurements {
        String TRACK_PREV_ID = "TRACKING // PREVIOUS_OBJECT_IN_TRACK_ID";
        String TRACK_NEXT_ID = "TRACKING // NEXT_OBJECT_IN_TRACK_ID";
        String ORIENTATION = "TRACKING // ORIENTATION";
        String LEADING_X_PX = "TRACKING // LEADING_POINT_X_(PX)";
        String LEADING_Y_PX = "TRACKING // LEADING_POINT_Y_(PX)";
        String LEADING_Z_PX = "TRACKING // LEADING_POINT_Z_(PX)";

    }


    public ArrayList<Obj>[] getCandidateObjects(ObjCollection inputObjects, int t1, int t2) {
        // Creating a pair of ArrayLists to store the current and previous objects
        ArrayList<Obj>[] objects = new ArrayList[2];
        objects[0] = new ArrayList<>();
        objects[1] = new ArrayList<>();

        // Include objects from the previous and current frames that haven't been linked
        for (Obj inputObject:inputObjects.values()) {
            if (inputObject.getT() == t1 && inputObject.getMeasurement(Measurements.TRACK_NEXT_ID) == null) {
                objects[0].add(inputObject);

            } else if (inputObject.getT() == t2 && inputObject.getMeasurement(Measurements.TRACK_PREV_ID) == null) {
                objects[1].add(inputObject);

            }
        }

        return objects;

    }

    public double[][] calculateCostMatrix(ArrayList<Obj> prevObjects, ArrayList<Obj> currObjects, @Nullable ObjCollection inputObjects, @Nullable int[][] spatialLimits) {
        boolean useVolume = parameters.getValue(USE_VOLUME);
        double volumeWeighting = parameters.getValue(VOLUME_WEIGHTING);
        String directionWeightingMode = parameters.getValue(DIRECTION_WEIGHTING_MODE);
        double preferredDirection = parameters.getValue(PREFERRED_DIRECTION);
        double directionTolerance = parameters.getValue(DIRECTION_TOLERANCE);
        double directionWeighting = parameters.getValue(DIRECTION_WEIGHTING);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT);
        String measurement = parameters.getValue(MEASUREMENT);
        double measurementWeighting = parameters.getValue(MEASUREMENT_WEIGHTING);

        String linkingMethod = parameters.getValue(LINKING_METHOD);

        // Creating the cost matrix
        double[][] cost = new double[currObjects.size()][prevObjects.size()];

        for (int curr = 0; curr < cost.length; curr++) {
            for (int prev = 0; prev < cost[curr].length; prev++) {
                Obj prevObj = prevObjects.get(prev);
                Obj currObj = currObjects.get(curr);

                // Calculating main spatial cost
                double spatialCost = 0;
                switch (linkingMethod) {
                    case LinkingMethods.CENTROID:
                        spatialCost = getCentroidSeparationCost(prevObj, currObj);
                        break;

                    case LinkingMethods.ABSOLUTE_OVERLAP:
                        float overlap = getAbsoluteOverlap(prevObjects.get(prev), currObjects.get(curr), spatialLimits);
                        spatialCost = overlap == 0 ? Float.MAX_VALUE : 1/overlap;
                        break;
                }

                // Calculating additional costs
                double volumeCost = useVolume ? getVolumeCost(prevObj, currObj) : 0;
                double measurementCost = useMeasurement ? getMeasurementCost(prevObj,currObj,measurement) : 0;
                double directionCost = 0;
                switch (directionWeightingMode) {
                    case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                        directionCost = getAbsoluteOrientationCost(prevObj,currObj,preferredDirection);
                        if (!testDirectionTolerance(directionCost,directionTolerance)) directionCost = Float.MAX_VALUE;
                        break;

                    case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                        directionCost = getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
                        if (!testDirectionTolerance(directionCost,directionTolerance)) directionCost = Float.MAX_VALUE;
                        break;
                }

                boolean linkValid = testLinkValidity(prevObj,currObj,inputObjects,spatialLimits);

                cost[curr][prev] = linkValid ? (spatialCost + volumeCost*volumeWeighting +
                        directionCost*directionWeighting + measurementCost*measurementWeighting) : Double.MAX_VALUE;

            }
        }

        return cost;

    }

    public boolean testLinkValidity(Obj prevObj, Obj currObj, @Nullable ObjCollection inputObjects, @Nullable int[][] spatialLimits) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String linkingMethod = parameters.getValue(LINKING_METHOD);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP);
        double maxDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE);
        String directionWeightingMode = parameters.getValue(DIRECTION_WEIGHTING_MODE);
        double preferredDirection = parameters.getValue(PREFERRED_DIRECTION);
        double directionTolerance = parameters.getValue(DIRECTION_TOLERANCE);

        // Checking they are within the user-specified maximum distance.  If not, no link is made.
        boolean allow = true;
        switch (linkingMethod) {
            case LinkingMethods.CENTROID:
                double dist = getCentroidSeparationCost(prevObj,currObj);
                allow = dist <= maxDist;
                break;
            case LinkingMethods.ABSOLUTE_OVERLAP:
                float overlap = getAbsoluteOverlap(prevObj,currObj,spatialLimits);
                allow = overlap != 0 && overlap >= minOverlap;
                break;
        }

        // If it's failed here, return false
        if (!allow) return false;

        // Checking additional costs
        switch (directionWeightingMode) {
            case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                double directionCost = getAbsoluteOrientationCost(prevObj,currObj,preferredDirection);
                allow = testDirectionTolerance(directionCost,directionTolerance);
                break;
            case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                directionCost = getPreviousStepDirectionCost(prevObj,currObj,inputObjects);
                allow = testDirectionTolerance(directionCost,directionTolerance);
                break;
        }

        return allow;

    }

    public static double getCentroidSeparationCost(Obj prevObj, Obj currObj) {
        double prevXCent = prevObj.getXMean(true);
        double prevYCent = prevObj.getYMean(true);
        double prevZCent = prevObj.getZMean(true,true);

        double currXCent = currObj.getXMean(true);
        double currYCent = currObj.getYMean(true);
        double currZCent = currObj.getZMean(true,true);

        return Math.sqrt((prevXCent - currXCent) * (prevXCent - currXCent) +
                (prevYCent - currYCent) * (prevYCent - currYCent) +
                (prevZCent - currZCent) * (prevZCent - currZCent));

    }

    public static float getAbsoluteOverlap(Obj prevObj, Obj currObj, int[][] spatialLimits) {
        // Getting coordinates for each object
        TreeSet<Point<Integer>> prevPoints = prevObj.getPoints();
        TreeSet<Point<Integer>> currPoints = currObj.getPoints();

        // Indexer gives a single value for coordinates.  Will use a HashSet to prevent index duplicates.
        Indexer indexer = new Indexer(spatialLimits[0][1]+1,spatialLimits[1][1]+1,spatialLimits[2][1]+1);

        int prevSize = prevPoints.size();
        int currSize = currPoints.size();

        // Combining the coordinates into a single ArrayList.  This will prevent duplicates
        HashSet<Integer> indices = new HashSet<>();
        for (Point<Integer> prevPoint:prevPoints) {
            int[] point = new int[]{prevPoint.getX(),prevPoint.getY(),prevPoint.getZ()};
            indices.add(indexer.getIndex(point));
        }

        for (Point<Integer> currPoint:currPoints) {
            int[] point = new int[]{currPoint.getX(),currPoint.getY(),currPoint.getZ()};
            indices.add(indexer.getIndex(point));
        }

        return prevSize+currSize-indices.size();

    }

    public static double getVolumeCost(Obj prevObj, Obj currObj) {
        // Calculating volume weighting
        double prevVol = prevObj.getNVoxels();
        double currVol = currObj.getNVoxels();

        if (currObj.is2D()) {
            prevVol = Math.sqrt(prevVol);
            currVol = Math.sqrt(currVol);
        } else {
            prevVol = Math.cbrt(prevVol);
            currVol = Math.cbrt(currVol);
        }

        return (prevVol-currVol);

    }

    public static double getMeasurementCost(Obj prevObj, Obj currObj, String measurement) {
        // Calculating measurement weighting
        double prevMeas = prevObj.getMeasurement(measurement).getValue();
        double currMeas = currObj.getMeasurement(measurement).getValue();

        return (prevMeas-currMeas);

    }

    public static double getAbsoluteOrientationCost(Obj prevObj, Obj currObj, double preferredDirection) {
        // Getting centroid coordinates for three points
        double prevXCent = prevObj.getXMean(true);
        double prevYCent = prevObj.getYMean(true);
        double currXCent = currObj.getXMean(true);
        double currYCent = currObj.getYMean(true);
        double preferredX = Math.cos(Math.toRadians(preferredDirection));
        double preferredY = Math.sin(Math.toRadians(preferredDirection));

        Vector2D v1 = new Vector2D(preferredX,preferredY);
        Vector2D v2 = new Vector2D(currXCent-prevXCent,currYCent-prevYCent);

        // MathArithmeticException thrown if two points are coincident.  In these cases, give a cost of 0.
        try {
            return Math.abs(Vector2D.angle(v1, v2));
        } catch (MathArithmeticException e) {
            return 0;
        }
    }

    public static double getPreviousStepDirectionCost(Obj prevObj, Obj currObj, ObjCollection inputObjects) {
        // Get direction of previous object
        Measurement prevPrevObjMeas = prevObj.getMeasurement(Measurements.TRACK_PREV_ID);

        // If the previous object doesn't have a previous object (i.e. it was the first), return a score of 0
        if (prevPrevObjMeas == null) return 0;

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

        Vector2D v1 = new Vector2D(prevXCent-prevPrevXCent,prevYCent-prevPrevYCent);
        Vector2D v2 = new Vector2D(currXCent-prevXCent,currYCent-prevYCent);

        // MathArithmeticException thrown if two points are coincident.  In these cases, give a cost of 0.
        try {
            return Math.abs(Vector2D.angle(v1, v2));
        } catch (MathArithmeticException e) {
            return 0;
        }
    }

    public static boolean testDirectionTolerance(double directionCost, double directionTolerance) {
        return Math.abs(Math.toDegrees(directionCost)) <= directionTolerance;
    }

    public void linkObjects(Obj prevObj, Obj currObj) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);

        // Getting the track object from the previous-frame object
        Obj track = prevObj.getParent(trackObjectsName);

        // Setting relationship between the current object and track
        track.addChild(currObj);
        currObj.addParent(track);

        // Adding references to each other
        prevObj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, currObj.getID()));
        currObj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, prevObj.getID()));

    }

    public void createNewTrack(Obj currObj, ObjCollection trackObjects) {
        String trackObjectsName = trackObjects.getName();
        double dppXY = currObj.getDistPerPxXY();
        double dppZ = currObj.getDistPerPxZ();
        String units = currObj.getCalibratedUnits();

        // Creating a new track object
        Obj track = new Obj(trackObjectsName, trackObjects.getNextID(), dppXY, dppZ, units, currObj.is2D());

        // Setting relationship between the current object and track
        track.addChild(currObj);
        currObj.addParent(track);

        // Adding the track to the track collection
        trackObjects.add(track);

    }

    public void identifyLeading(ObjCollection objects) {
        String orientationMode = parameters.getValue(ORIENTATION_MODE);

        for (Obj obj:objects.values()) {
            double prevAngle = Double.NaN;
            double nextAngle = Double.NaN;

            if ((orientationMode.equals(OrientationModes.RELATIVE_TO_PREV)
                    || orientationMode.equals(OrientationModes.RELATIVE_TO_BOTH))
                    && obj.getMeasurement(Measurements.TRACK_PREV_ID) != null) {
                Obj prevObj = objects.get((int) obj.getMeasurement(Measurements.TRACK_PREV_ID).getValue());
                prevAngle = prevObj.calculateAngle2D(obj);
            }

            if ((orientationMode.equals(OrientationModes.RELATIVE_TO_NEXT)
                    || orientationMode.equals(OrientationModes.RELATIVE_TO_BOTH))
                    && obj.getMeasurement(Measurements.TRACK_NEXT_ID) != null) {
                Obj nextObj = objects.get((int) obj.getMeasurement(Measurements.TRACK_NEXT_ID).getValue());
                nextAngle = obj.calculateAngle2D(nextObj);
            }

            if (Double.isNaN(prevAngle) && Double.isNaN(nextAngle)) {
                // Adding furthest point coordinates to measurements
                obj.addMeasurement(new Measurement(Measurements.ORIENTATION, Double.NaN));
                obj.addMeasurement(new Measurement(Measurements.LEADING_X_PX, Double.NaN));
                obj.addMeasurement(new Measurement(Measurements.LEADING_Y_PX, Double.NaN));
                obj.addMeasurement(new Measurement(Measurements.LEADING_Z_PX, Double.NaN));

            } else {
                double angle;
                if (!Double.isNaN(prevAngle) && Double.isNaN(nextAngle)) {
                    angle = prevAngle;
                } else if (Double.isNaN(prevAngle) && !Double.isNaN(nextAngle)) {
                    angle = nextAngle;
                } else {
                    angle = (prevAngle+nextAngle)/2;
                }

                double xCent = obj.getXMean(true);
                double yCent = obj.getYMean(true);

                // Calculate line perpendicular to the direction of motion, passing through the origin.
                Vector2D p = new Vector2D(xCent,yCent);
                Line midLine = new Line(p,angle+Math.PI/2,1E-2);

                // Calculating second line perpendicular to the direction of motion, but shifted one pixel in the
                // direction of motion
                p = new Vector2D(xCent+Math.cos(angle),yCent+Math.sin(angle));
                Line refLine = new Line(p,angle+Math.PI/2,1E-2);

                // Iterating over all points, measuring their distance from the midLine
                Point<Integer> furthestPoint = null;
                double largestOffset = Double.NEGATIVE_INFINITY;
                for (Point<Integer> point:obj.getPoints()) {
                    double offset = midLine.getOffset(new Vector2D(point.getX(),point.getY()));

                    // Determining if the offset is positive or negative
                    double refOffset = refLine.getOffset(new Vector2D(point.getX(),point.getY()));
                    if (refOffset > offset) offset = -offset;

                    if (offset > largestOffset) {
                        largestOffset = offset;
                        furthestPoint = point;
                    }
                }

                // Adding furthest point coordinates to measurements
                obj.addMeasurement(new Measurement(Measurements.ORIENTATION,Math.toDegrees(angle)));
                obj.addMeasurement(new Measurement(Measurements.LEADING_X_PX,furthestPoint.getX()));
                obj.addMeasurement(new Measurement(Measurements.LEADING_Y_PX,furthestPoint.getY()));
                obj.addMeasurement(new Measurement(Measurements.LEADING_Z_PX,0));

            }
        }
    }

    public void showObjects(ObjCollection spotObjects, String trackObjectsName) {
        String colourMode = ObjCollection.ColourModes.PARENT_ID;
        HashMap<Integer, Float> hues = spotObjects.getHues(colourMode, trackObjectsName, true);

        // Creating a parent-ID encoded image of the objects
        Image dispImage = spotObjects.convertObjectsToImage(spotObjects.getName(),null,colourMode,hues);

        // Displaying the overlay
        ImagePlus ipl = dispImage.getImagePlus();
        ipl.setLut(LUTs.Random(true));
        ipl.show();

    }


    @Override
    public String getTitle() {
        return "Track objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "Uses Munkres Assignment Algorithm implementation from Apache HBase library" +
                "\nLeading point currently only works in 2D";
    }

    @Override
    public void run(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        int maxMissingFrames = parameters.getValue(MAXIMUM_MISSING_FRAMES);
        boolean identifyLeading = parameters.getValue(IDENTIFY_LEADING_POINT);

        // Getting objects
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection trackObjects = new ObjCollection(trackObjectsName);
        workspace.addObjects(trackObjects);

        // If there are no input objects, create a blank track set and skip this module
        if (inputObjects.size() == 0) return;

        // Clearing previous relationships and measurements (in case module has been run before)
        for (Obj inputObj:inputObjects.values()) {
            inputObj.removeMeasurement(Measurements.TRACK_NEXT_ID);
            inputObj.removeMeasurement(Measurements.TRACK_PREV_ID);
            inputObj.removeParent(trackObjectsName);
        }

        // Finding the spatial and frame frame limits of all objects in the inputObjects set
        int[][] spatialLimits = inputObjects.getSpatialLimits();
        int[] frameLimits = inputObjects.getTemporalLimits();

        // Creating new track objects for all objects in the first frame
        for (Obj inputObj:inputObjects.values()) {
            if (inputObj.getT() == frameLimits[0]) {
                createNewTrack(inputObj,trackObjects);
            }
        }

        for (int t2=frameLimits[0]+1;t2<=frameLimits[1];t2++) {
            writeMessage("Tracking to frame "+(t2+1)+" of "+(frameLimits[1]+1));

            // Testing the previous permitted frames for links
            for (int t1 = t2-1;t1>=t2-1-maxMissingFrames;t1--) {
                ArrayList<Obj>[] nPObjects = getCandidateObjects(inputObjects,t1,t2);

                // If no previous or current objects were found no linking takes place
                if (nPObjects[0].size() == 0 || nPObjects[1].size() == 0) {
                    if (t1==t2-1-maxMissingFrames || t1 == 0) {
                        //Creating new tracks for current objects that have no chance of being linked in other frames
                        for (int curr = 0; curr < nPObjects[1].size(); curr++) {
                            createNewTrack(nPObjects[1].get(curr), trackObjects);
                        }
                        break;
                    }
                    continue;
                }

                // Calculating distances between objects and populating the cost matrix
                double[][] cost = calculateCostMatrix(nPObjects[0],nPObjects[1],inputObjects,spatialLimits);
                HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(cost);
                int[] assignment = hungarianAlgorithm.execute();

                // Applying the calculated assignments as relationships
                for (int curr = 0; curr < assignment.length; curr++) {
                    // Getting the object from the current frame
                    Obj currObj = nPObjects[1].get(curr);

                    // Checking if the previous and current objects can be linked
                    boolean successfulLink = false;
                    if (assignment[curr] != -1) {
                        // Checking if the link is within the user-defined limits (max linking distance, etc.)
                        Obj prevObj = nPObjects[0].get(assignment[curr]);
                        successfulLink = testLinkValidity(prevObj,currObj,inputObjects,spatialLimits);
                    }

                    // Creating new links and tracks where appropriate
                    if (successfulLink) {
                        Obj prevObj = nPObjects[0].get(assignment[curr]);
                        linkObjects(prevObj,currObj);
                    } else if (t1==t2-1-maxMissingFrames) {
                        createNewTrack(currObj,trackObjects);
                    }
                }
            }
        }

        // Determining the leading point in the object (to next object)
        if (identifyLeading) identifyLeading(inputObjects);

        // If selected, showing an overlay of the tracked objects
        if (showOutput) showObjects(inputObjects,trackObjectsName);

        // Adding track objects to the workspace
        writeMessage("Assigned "+trackObjects.size()+" tracks");

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TRACK_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(LINKING_METHOD,Parameter.CHOICE_ARRAY,LinkingMethods.CENTROID,LinkingMethods.ALL));
        parameters.add(new Parameter(MINIMUM_OVERLAP,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(MAXIMUM_LINKING_DISTANCE,Parameter.DOUBLE,20.0));
        parameters.add(new Parameter(USE_VOLUME,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(VOLUME_WEIGHTING, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(DIRECTION_WEIGHTING_MODE,Parameter.CHOICE_ARRAY,DirectionWeightingModes.NONE,DirectionWeightingModes.ALL));
        parameters.add(new Parameter(PREFERRED_DIRECTION, Parameter.DOUBLE,0.0));
        parameters.add(new Parameter(DIRECTION_TOLERANCE, Parameter.DOUBLE,180.0));
        parameters.add(new Parameter(DIRECTION_WEIGHTING, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(USE_MEASUREMENT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(MEASUREMENT_WEIGHTING, Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(MAXIMUM_MISSING_FRAMES,Parameter.INTEGER,0));
        parameters.add(new Parameter(IDENTIFY_LEADING_POINT,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ORIENTATION_MODE,Parameter.CHOICE_ARRAY,OrientationModes.RELATIVE_TO_BOTH,OrientationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();

        returnedParamters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParamters.add(parameters.getParameter(TRACK_OBJECTS));
        returnedParamters.add(parameters.getParameter(LINKING_METHOD));

        switch ((String) parameters.getValue(LINKING_METHOD)) {
            case LinkingMethods.ABSOLUTE_OVERLAP:
                returnedParamters.add(parameters.getParameter(MINIMUM_OVERLAP));
                break;

            case LinkingMethods.CENTROID:
                returnedParamters.add(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
                break;
        }

        returnedParamters.add(parameters.getParameter(USE_VOLUME));
        if (returnedParamters.getValue(USE_VOLUME)) {
            returnedParamters.add(parameters.getParameter(VOLUME_WEIGHTING));
        }

        returnedParamters.add(parameters.getParameter(DIRECTION_WEIGHTING_MODE));
        switch ((String) parameters.getValue(DIRECTION_WEIGHTING_MODE)) {
            case DirectionWeightingModes.ABSOLUTE_ORIENTATION:
                returnedParamters.add(parameters.getParameter(PREFERRED_DIRECTION));
                returnedParamters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParamters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;

            case DirectionWeightingModes.RELATIVE_TO_PREVIOUS_STEP:
                returnedParamters.add(parameters.getParameter(DIRECTION_TOLERANCE));
                returnedParamters.add(parameters.getParameter(DIRECTION_WEIGHTING));
                break;
        }

        returnedParamters.add(parameters.getParameter(USE_MEASUREMENT));
        if (returnedParamters.getValue(USE_MEASUREMENT)) {
            returnedParamters.add(parameters.getParameter(MEASUREMENT));
            returnedParamters.add(parameters.getParameter(MEASUREMENT_WEIGHTING));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(MEASUREMENT,inputObjectsName);
        }

        returnedParamters.add(parameters.getParameter(MAXIMUM_MISSING_FRAMES));
        returnedParamters.add(parameters.getParameter(IDENTIFY_LEADING_POINT));

        if (parameters.getValue(IDENTIFY_LEADING_POINT)) {
            returnedParamters.add(parameters.getParameter(ORIENTATION_MODE));
        }

        return returnedParamters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference trackPrevID = objectMeasurementReferences.getOrPut(Measurements.TRACK_PREV_ID);
        MeasurementReference trackNextID = objectMeasurementReferences.getOrPut(Measurements.TRACK_NEXT_ID);
        MeasurementReference angleMeasurement = objectMeasurementReferences.getOrPut(Measurements.ORIENTATION);
        MeasurementReference leadingXPx= objectMeasurementReferences.getOrPut(Measurements.LEADING_X_PX);
        MeasurementReference leadingYPx= objectMeasurementReferences.getOrPut(Measurements.LEADING_Y_PX);
        MeasurementReference leadingZPx= objectMeasurementReferences.getOrPut(Measurements.LEADING_Z_PX);

        trackPrevID.setImageObjName(inputObjectsName);
        trackNextID.setImageObjName(inputObjectsName);

        trackPrevID.setCalculated(true);
        trackNextID.setCalculated(true);

        if (parameters.getValue(IDENTIFY_LEADING_POINT)) {
            angleMeasurement.setCalculated(true);
            leadingXPx.setCalculated(true);
            leadingYPx.setCalculated(true);
            leadingZPx.setCalculated(true);

            angleMeasurement.setImageObjName(inputObjectsName);
            leadingXPx.setImageObjName(inputObjectsName);
            leadingYPx.setImageObjName(inputObjectsName);
            leadingZPx.setImageObjName(inputObjectsName);

        } else {
            angleMeasurement.setCalculated(false);
            leadingXPx.setCalculated(false);
            leadingYPx.setCalculated(false);
            leadingZPx.setCalculated(false);
        }

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(trackObjectsName,inputObjectsName);

    }
}
