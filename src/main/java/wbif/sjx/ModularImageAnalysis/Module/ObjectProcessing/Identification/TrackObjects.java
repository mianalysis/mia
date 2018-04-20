package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.hadoop.hbase.util.MunkresAssignment;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;
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
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";
    public static final String IDENTIFY_LEADING_POINT = "Identify leading point";
    public static final String ORIENTATION_MODE = "Orientation mode";


    public interface LinkingMethods {
        String ABSOLUTE_OVERLAP = "Absolute overlap";
        String CENTROID = "Centroid";

        String[] ALL = new String[]{ABSOLUTE_OVERLAP,CENTROID};
    }

    public interface OrientationModes {
        String RELATIVE_TO_BOTH = "Relative to both points";
        String RELATIVE_TO_PREV = "Relative to previous point";
        String RELATIVE_TO_NEXT = "Relative to next point";

        String[] ALL = new String[]{RELATIVE_TO_BOTH,RELATIVE_TO_PREV,RELATIVE_TO_NEXT};
    }

    public interface Measurements {
        String TRACK_PREV_ID = "TRACKING//PREVIOUS_OBJECT_IN_TRACK_ID";
        String TRACK_NEXT_ID = "TRACKING//NEXT_OBJECT_IN_TRACK_ID";
        String ORIENTATION = "TRACKING//ORIENTATION";
        String LEADING_X_PX = "TRACKING//LEADING_POINT_X_(PX)";
        String LEADING_Y_PX = "TRACKING//LEADING_POINT_Y_(PX)";
        String LEADING_Z_PX = "TRACKING//LEADING_POINT_Z_(PX)";

    }

    private static float getCentroidSeparation(Obj prevObj, Obj currObj) {
        double prevXCent = prevObj.getXMean(true);
        double prevYCent = prevObj.getYMean(true);
        double prevZCent = prevObj.getZMean(true,true);

        double currXCent = currObj.getXMean(true);
        double currYCent = currObj.getYMean(true);
        double currZCent = currObj.getZMean(true,true);

        return (float) Math.sqrt((prevXCent - currXCent) * (prevXCent - currXCent) +
                (prevYCent - currYCent) * (prevYCent - currYCent) +
                (prevZCent - currZCent) * (prevZCent - currZCent));

    }

    private static float getAbsoluteOverlap(Obj prevObj, Obj currObj, int[][] spatialLimits) {
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

    private void identifyLeading(ObjCollection objects, String orientationMode) {
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

    @Override
    public String getTitle() {
        return "Track objects";
    }

    @Override
    public String getHelp() {
        return "Uses Munkres Assignment Algorithm implementation from Apache HBase library" +
                "\nLeading point currently only works in 2D";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        ObjCollection trackObjects = new ObjCollection(trackObjectsName);
        String linkingMethod = parameters.getValue(LINKING_METHOD);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP);
        double maxDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE);
        int maxMissingFrames = parameters.getValue(MAXIMUM_MISSING_FRAMES);
        boolean identifyLeading = parameters.getValue(IDENTIFY_LEADING_POINT);
        String orientationMode = parameters.getValue(ORIENTATION_MODE);

        // Getting calibration from the first input object
        double dppXY = 1;
        double dppZ = 1;
        String units = "pixels";
        if (inputObjects.values().size() != 0) {
            dppXY = inputObjects.values().iterator().next().getDistPerPxXY();
            dppZ = inputObjects.values().iterator().next().getDistPerPxZ();
            units = inputObjects.values().iterator().next().getCalibratedUnits();

        }

        // Clearing previous relationships and measurements (in case module has been run before)
        for (Obj inputObj:inputObjects.values()) {
            inputObj.removeMeasurement(Measurements.TRACK_NEXT_ID);
            inputObj.removeMeasurement(Measurements.TRACK_PREV_ID);
            inputObj.removeParent(trackObjectsName);

        }

        // Finding the spatial and frame frame limits of all objects in the inputObjects set
        int[][] spatialLimits = inputObjects.getSpatialLimits();
        int[] frameLimits = inputObjects.getTimepointLimits();

        for (int t2=frameLimits[0]+1;t2<=frameLimits[1];t2++) {
            writeMessage("Tracking to frame "+(t2+1)+" of "+(frameLimits[1]+1));

            for (int t1 = t2-1;t1>=t2-1-maxMissingFrames;t1--) {
                // Creating a pair of ArrayLists to store the current and previous objects
                ArrayList<Obj> prevObjects = new ArrayList<>();
                ArrayList<Obj> currObjects = new ArrayList<>();

                // Include objects from the previous and current frames that haven't been linked
                for (Obj inputObject:inputObjects.values()) {
                    if (inputObject.getT() == t1 && inputObject.getMeasurement(Measurements.TRACK_NEXT_ID) == null) {
                        prevObjects.add(inputObject);

                    } else if (inputObject.getT() == t2 && inputObject.getMeasurement(Measurements.TRACK_PREV_ID) == null) {
                        currObjects.add(inputObject);

                    }
                }

                // Calculating distances between objects and populating the cost matrix
                if (currObjects.size() > 0 && prevObjects.size() > 0) {
                    // Creating the cost matrix
                    float[][] cost = new float[currObjects.size()][prevObjects.size()];
                    for (int curr = 0; curr < cost.length; curr++) {
                        for (int prev = 0; prev < cost[curr].length; prev++) {
                            switch (linkingMethod) {
                                case LinkingMethods.CENTROID:
                                    cost[curr][prev] = getCentroidSeparation(prevObjects.get(prev), currObjects.get(curr));
                                    break;

                                case LinkingMethods.ABSOLUTE_OVERLAP:
                                    float overlap = getAbsoluteOverlap(prevObjects.get(prev), currObjects.get(curr), spatialLimits);

                                    cost[curr][prev] = overlap == 0 ? Float.MAX_VALUE : 1/overlap;
                                    break;

                            }
                        }
                    }

                    // Running the Munkres algorithm to assign matches
                    int[] assignment = new MunkresAssignment(cost).solve();

                    // Applying the calculated assignments as relationships
                    for (int curr = 0; curr < assignment.length; curr++) {
                        // Getting the object from the current frame
                        Obj track = null;
                        Obj currObj = currObjects.get(curr);
                        Obj prevObj;

                        if (assignment[curr] == -1) {
                            // This is a new track, so assigning it to a new track object
                            track = new Obj(trackObjectsName,trackObjects.getNextID(),dppXY,dppZ,units);

                        } else {
                            // Getting the object from the previous frame
                            prevObj = prevObjects.get(assignment[curr]);

                            // Checking they are within the user-specified maximum distance.  If not, no link is made
                            switch (linkingMethod) {
                                case LinkingMethods.CENTROID:
                                    float dist = getCentroidSeparation(prevObj,currObj);

                                    if (dist > maxDist) {
                                        track = new Obj(trackObjectsName, trackObjects.getNextID(), dppXY, dppZ, units);
                                    }

                                    break;

                                case LinkingMethods.ABSOLUTE_OVERLAP:
                                    float overlap = getAbsoluteOverlap(prevObj,currObj,spatialLimits);
                                    if (overlap == 0 || overlap < minOverlap) {
                                        track = new Obj(trackObjectsName, trackObjects.getNextID(), dppXY, dppZ, units);
                                    }

                                    break;

                            }

                            // If a new track wasn't created
                            if (track == null) {
                                // Adding references to each other
                                prevObj.addMeasurement(new Measurement(Measurements.TRACK_NEXT_ID, currObj.getID()));
                                currObj.addMeasurement(new Measurement(Measurements.TRACK_PREV_ID, prevObj.getID()));

                                // Getting the track object from the previous-frame object
                                track = prevObj.getParent(trackObjectsName);

                            }

                            // If the previous object hasn't been assigned a track
                            if (track == null) {
                                // This is a new track, so assigning it to a new track object
                                track = new Obj(trackObjectsName, trackObjects.getNextID(), dppXY, dppZ, units);

                                prevObj.addParent(track);
                                track.addChild(prevObj);

                            }
                        }

                        // Setting relationship between the current object and track
                        track.addChild(currObj);
                        currObj.addParent(track);

                        // Adding the track to the track collection
                        trackObjects.add(track);

                    }
                }
            }
        }

        // Determining the leading point in the object (to next object)
        if (identifyLeading) identifyLeading(inputObjects,orientationMode);

        // Adding track objects to the workspace
        workspace.addObjects(trackObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TRACK_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(LINKING_METHOD,Parameter.CHOICE_ARRAY,LinkingMethods.CENTROID,LinkingMethods.ALL));
        parameters.add(new Parameter(MINIMUM_OVERLAP,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(MAXIMUM_LINKING_DISTANCE,Parameter.DOUBLE,20.0));
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
    public void addRelationships(RelationshipCollection relationships) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(trackObjectsName,inputObjectsName);

    }
}
