// TODO: Currently calculates centroid separation.  In the future could also calculate based on overlap (or others)

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import org.apache.hadoop.hbase.util.MunkresAssignment;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 20/09/2017.
 */
public class TrackObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TRACK_OBJECTS = "Output track objects";
    public static final String MAXIMUM_LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String MAXIMUM_MISSING_FRAMES = "Maximum number of missing frames";

    private static final String TRACK_PREV_ID = "PREVIOUS_OBJECT_IN_TRACK_ID";
    private static final String TRACK_NEXT_ID = "NEXT_OBJECT_IN_TRACK_ID";


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

        // Finding the first and last frame of all objects in the inputObjects set
        int[] limits = inputObjects.getTimepointLimits();

        // Calculating the cost matrix for each object pair
        for (int t=limits[0]+1;t<=limits[1];t++) {
            // Creating a pair of ArrayLists to store the current and previous objects
            ArrayList<Obj> prevObjects = new ArrayList<>();
            ArrayList<Obj> currObjects = new ArrayList<>();

            for (Obj inputObject:inputObjects.values()) {
                if (inputObject.getT() == t - 1) {
                    prevObjects.add(inputObject);
                } else if (inputObject.getT() == t) {
                    currObjects.add(inputObject);
                }
            }

            // At this point, could go through all previous frames (up to the user-defined threshold) and add any which
            // aren't part of a track
            for (int gap = 1;gap<=maxMissingFrames;gap++) {
                for (Obj inputObject:inputObjects.values()) {
                    if (inputObject.getMeasurement(TRACK_NEXT_ID) == null && inputObject.getT() == t - 1 - gap) {
                        prevObjects.add(inputObject);
                    }
                }
            }

            // Calculating distances between objects and populating the cost matrix
            if (currObjects.size() > 0 && prevObjects.size() > 0) {
                // Creating the cost matrix
                float[][] cost = new float[currObjects.size()][prevObjects.size()];

                for (int curr = 0; curr < cost.length; curr++) {
                    for (int prev = 0; prev < cost[0].length; prev++) {
                        double currXCent = currObjects.get(curr).getXMean(true);
                        double currYCent = currObjects.get(curr).getYMean(true);
                        double currZCent = currObjects.get(curr).getZMean(true,true);

                        double prevXCent = prevObjects.get(prev).getXMean(true);
                        double prevYCent = prevObjects.get(prev).getYMean(true);
                        double prevZCent = prevObjects.get(prev).getZMean(true,true);

                        double dist = Math.sqrt((prevXCent - currXCent) * (prevXCent - currXCent) +
                                (prevYCent - currYCent) * (prevYCent - currYCent) +
                                (prevZCent - currZCent) * (prevZCent - currZCent));

                        System.out.println(currObjects.get(curr).getT()+"_"+dist);

                        if (dist < maxDist) {
                            System.out.println("Within limit");
                            cost[curr][prev] = (float) dist;
                        } else {
                            System.out.println("Outside limit");
                            cost[curr][prev] = Float.MAX_VALUE;
                        }
                    }
                }

                // Running the Munkres algorithm to assign matches
                int[] assignment = new MunkresAssignment(cost).solve();

                Obj track;
                Obj currObj;
                // Applying the calculated assignments as relationships
                for (int curr = 0; curr < assignment.length; curr++) {
                    if (assignment[curr] == -1) {
                        // Getting the object from the current frame
                        currObj = currObjects.get(curr);

                        // This is a new track, so assigning it to a new track object
                        track = new Obj(trackObjectsName,trackObjects.getNextID(),dppXY,dppZ,units);

                    } else {
                        // Getting the two objects (previous and current frame)
                        Obj prevObj = prevObjects.get(assignment[curr]);
                        currObj = currObjects.get(curr);

                        // Adding references to each other
                        prevObj.addMeasurement(new MIAMeasurement(TRACK_NEXT_ID,currObj.getID()));
                        currObj.addMeasurement(new MIAMeasurement(TRACK_PREV_ID,prevObj.getID()));

                        // Getting the track object from the previous-frame object
                        track = prevObj.getParent(trackObjectsName);

                        // There will be cases where the previous object hasn't been assigned a track (i.e. if there
                        // were no objects in the preceding frame(s))
                        if (track == null) {
                            // This is a new track, so assigning it to a new track object
                            track = new Obj(trackObjectsName,trackObjects.getNextID(),dppXY,dppZ,units);

                            prevObj.addParent(track);
                            track.addChild(prevObj);

                        }
                    }

                    // Setting relationship between the current object and track
                    track.addChild(currObj);
                    currObj.addParent(track);

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
        parameters.addParameter(new Parameter(MAXIMUM_LINKING_DISTANCE,Parameter.DOUBLE,20.0));
        parameters.addParameter(new Parameter(MAXIMUM_MISSING_FRAMES,Parameter.INTEGER,0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        measurements.addMeasurement(inputObjectsName,TRACK_PREV_ID);
        measurements.addMeasurement(inputObjectsName, TRACK_NEXT_ID);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        relationships.addRelationship(trackObjectsName,inputObjectsName);

    }
}
