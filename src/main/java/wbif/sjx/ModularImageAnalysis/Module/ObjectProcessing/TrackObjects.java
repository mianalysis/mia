package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.apache.hadoop.hbase.util.MunkresAssignment;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sc13967 on 20/09/2017.
 */
public class TrackObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_METHOD = "Linking method";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String MINIMUM_OVERLAP = "Minimum overlap";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";

    private static final String TRACK_PREV_ID = "PREVIOUS_OBJECT_IN_TRACK_ID";
    private static final String TRACK_NEXT_ID = "NEXT_OBJECT_IN_TRACK_ID";

    public interface LinkingMethods {
        String ABSOLUTE_OVERLAP = "Absolute overlap";
        String CENTROID = "Centroid";

        String[] ALL = new String[]{ABSOLUTE_OVERLAP,CENTROID};

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
        ArrayList<Point<Integer>> prevPoints = prevObj.getPoints();
        ArrayList<Point<Integer>> currPoints = currObj.getPoints();

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

    @Override
    public String getTitle() {
        return "Track objects";
    }

    @Override
    public String getHelp() {
        return "Uses Munkres Assignment Algorithm implementation from Apache HBase library";
    }

    @Override
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        ObjSet trackObjects = new ObjSet(trackObjectsName);
        String linkingMethod = parameters.getValue(LINKING_METHOD);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP);
        double maxDist = parameters.getValue(MAXIMUM_LINKING_DISTANCE);
        int maxMissingFrames = parameters.getValue(MAXIMUM_MISSING_FRAMES);

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
            inputObj.removeMeasurement(TRACK_NEXT_ID);
            inputObj.removeMeasurement(TRACK_PREV_ID);
            inputObj.removeParent(trackObjectsName);

        }

        // Finding the spatial and frame frame limits of all objects in the inputObjects set
        int[][] spatialLimits = inputObjects.getSpatialLimits();
        int[] frameLimits = inputObjects.getTimepointLimits();

        for (int t2=frameLimits[0]+1;t2<=frameLimits[1];t2++) {
            if (verbose) System.out.println("["+moduleName+"] Tracking to frame "+(t2+1)+" of "+frameLimits[1]);

            for (int t1 = t2-1;t1>=t2-1-maxMissingFrames;t1--) {
                // Creating a pair of ArrayLists to store the current and previous objects
                ArrayList<Obj> prevObjects = new ArrayList<>();
                ArrayList<Obj> currObjects = new ArrayList<>();

                // Include objects from the previous and current frames that haven't been linked
                for (Obj inputObject:inputObjects.values()) {
                    if (inputObject.getT() == t1 && inputObject.getMeasurement(TRACK_NEXT_ID) == null) {
                        prevObjects.add(inputObject);

                    } else if (inputObject.getT() == t2 && inputObject.getMeasurement(TRACK_PREV_ID) == null) {
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
                                prevObj.addMeasurement(new MIAMeasurement(TRACK_NEXT_ID, currObj.getID()));
                                currObj.addMeasurement(new MIAMeasurement(TRACK_PREV_ID, prevObj.getID()));

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

        // Adding track objects to the workspace
        workspace.addObjects(trackObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(TRACK_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(LINKING_METHOD,Parameter.CHOICE_ARRAY,LinkingMethods.CENTROID,LinkingMethods.ALL));
        parameters.addParameter(new Parameter(MINIMUM_OVERLAP,Parameter.DOUBLE,1.0));
        parameters.addParameter(new Parameter(MAXIMUM_LINKING_DISTANCE,Parameter.DOUBLE,20.0));
        parameters.addParameter(new Parameter(MAXIMUM_MISSING_FRAMES,Parameter.INTEGER,0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParamters = new ParameterCollection();

        returnedParamters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParamters.addParameter(parameters.getParameter(TRACK_OBJECTS));
        returnedParamters.addParameter(parameters.getParameter(LINKING_METHOD));

        switch ((String) parameters.getValue(LINKING_METHOD)) {
            case LinkingMethods.ABSOLUTE_OVERLAP:
                returnedParamters.addParameter(parameters.getParameter(MINIMUM_OVERLAP));
                break;

            case LinkingMethods.CENTROID:
                returnedParamters.addParameter(parameters.getParameter(MAXIMUM_LINKING_DISTANCE));
                break;
        }

        returnedParamters.addParameter(parameters.getParameter(MAXIMUM_MISSING_FRAMES));

        return returnedParamters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        measurements.addObjectMeasurement(inputObjectsName,TRACK_PREV_ID);
        measurements.addObjectMeasurement(inputObjectsName, TRACK_NEXT_ID);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(trackObjectsName,inputObjectsName);

    }
}
