package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class RelateObjects extends HCModule {
    public final static String PARENT_OBJECTS = "Parent (larger) objects";
    public final static String CHILD_OBJECTS = "Child (smaller) objects";
    public final static String RELATE_MODE = "Method to relate objects";
    public final static String TEST_CHILD_OBJECTS = "Child objects to test against";
    public final static String LINKING_DISTANCE = "Linking distance";

    private final static String MATCHING_IDS = "Matching IDs";
    private final static String SPATIAL_OVERLAP = "Spatial overlap";
    private final static String PROXIMITY_TO_CHILDREN = "Proximity to children";
    private final static String[] RELATE_MODES = new String[]{MATCHING_IDS,PROXIMITY_TO_CHILDREN,SPATIAL_OVERLAP};

    private final static String DIST_EDGE_PX_MEAS = "Distance from parent edge (px)";

    public static void linkMatchingIDs(ObjSet parentObjects, ObjSet childObjects) {
        for (Obj parentObject:parentObjects.values()) {
            int ID = parentObject.getID();

            Obj childObject = childObjects.get(ID);

            if (childObject != null) {
                parentObject.addChild(childObject);
                childObject.addParent(parentObject);

            }
        }
    }

    public static void proximityToChildren(ObjSet parentObjects, ObjSet childObjects, String testChildObjectsName, double linkingDistance) {
        // Runs through each child object against each parent object
        for (Obj parentObject:parentObjects.values()) {
            // Getting children of the parent to be used as references
            ObjSet testChildren = parentObject.getChildren(testChildObjectsName);

            // Running through all proximal children
            for (Obj testChild : testChildren.values()) {
                // Getting centroid of the current child
                double xCentTest = testChild.getCentroid(Obj.X);
                double yCentTest = testChild.getCentroid(Obj.Y);

                // Running through all children to relate
                for (Obj childObject : childObjects.values()) {
                    double xDist = xCentTest - childObject.getCentroid(Obj.X);
                    double yDist = yCentTest - childObject.getCentroid(Obj.Y);

                    // If the test object and the current object is less than the linking distance, assign the relationship
                    double dist = Math.sqrt(xDist * xDist + yDist * yDist);
                    if (dist < linkingDistance) {
                        childObject.addParent(parentObject);
                        parentObject.addChild(childObject);

                    }
                }
            }
        }
    }

    public static void spatialLinking(ObjSet parentObjects, ObjSet childObjects) {
        // Runs through each child object against each parent object
        for (Obj parentObject:parentObjects.values()) {
            // Getting parent coordinates
            ArrayList<Integer> parentX = parentObject.getCoordinates(Obj.X);
            ArrayList<Integer> parentY = parentObject.getCoordinates(Obj.Y);
            ArrayList<Integer> parentZ = parentObject.getCoordinates(Obj.Z);

            // Creating a Hyperstack to hold the distance transform
            int[][] range = parentObject.getCoordinateRange();
            ImagePlus ipl = IJ.createHyperStack("Objects", range[Obj.X][1]-range[Obj.X][0] + 1,
                    range[Obj.Y][1]-range[Obj.Y][0] + 1, 1, range[Obj.Z][1]-range[Obj.Z][0], 1, 8);

            // Setting pixels corresponding to the parent object to 1
            for (int i=0;i<parentX.size();i++) {
                ipl.setPosition(1,parentZ.get(i)-range[Obj.Z][0]+1,1);
                ipl.getProcessor().set(parentX.get(i)-range[Obj.X][0],parentY.get(i)-range[Obj.Y][0],255);

            }

            // Creating distance map using MorphoLibJ
            short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
            DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights,true);
            ImageStack distanceMap = distTransform.distanceMap(ipl.getStack());

            for (Obj childObject:childObjects.values()) {
                // Only testing if the child is present in the same dimensions as the parent
                HashMap<Integer,Integer> parentPositions = parentObject.getPositions();
                HashMap<Integer,Integer> childPositions = childObject.getPositions();

                boolean matchingDimensions = true;
                for (int dim:parentPositions.keySet()) {
                    if (!parentPositions.get(dim).equals(childPositions.get(dim))) {
                        matchingDimensions = false;
                        break;
                    }
                }
                if (!matchingDimensions) continue;

                // Getting the child centroid location
                ArrayList<Integer> childX = childObject.getCoordinates(Obj.X);
                ArrayList<Integer> childY = childObject.getCoordinates(Obj.Y);
                ArrayList<Integer> childZ = childObject.getCoordinates(Obj.Z);

                int xCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childX));
                int yCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childY));
                int zCent = (int) Math.round(MeasureObjectCentroid.calculateCentroid(childZ));

                // Testing if the child centroid exists in the object
                for (int i=0;i<parentX.size();i++) {
                    if (parentX.get(i)==xCent & parentY.get(i)==yCent & parentZ.get(i)==zCent) {
                        parentObject.addChild(childObject);
                        childObject.addParent(parentObject);

                        // Getting position within current parent object
                        MIAMeasurement absDistanceFromEdge = new MIAMeasurement(DIST_EDGE_PX_MEAS);
                        int xPos = xCent-range[Obj.X][0];
                        int yPos = yCent-range[Obj.Y][0];
                        int zPos = zCent-range[Obj.Z][0];

                        if (xPos < 0 | xPos > distanceMap.getWidth() | yPos < 0 | yPos > distanceMap.getHeight() | zPos < 0 | zPos >= distanceMap.size()) {
                            absDistanceFromEdge.setValue(Double.NaN);
                        } else {
                            absDistanceFromEdge.setValue(distanceMap.getVoxel(xCent - range[Obj.X][0], yCent - range[Obj.Y][0], zCent - range[Obj.Z][0]));
                        }
                        childObject.addMeasurement(absDistanceFromEdge);

                        break;

                    }
                }
            }
        }

        // Applying a blank measurement to any children missing one
        for (Obj childObject:childObjects.values()) {
            if (childObject.getParent(parentObjects.getName()) == null) {
                MIAMeasurement absDistanceFromEdge = new MIAMeasurement(DIST_EDGE_PX_MEAS);
                absDistanceFromEdge.setValue(Double.NaN);
                childObject.addMeasurement(absDistanceFromEdge);
                childObject.addParent(parentObjects.getName(),null);

            }
        }
    }

    @Override
    public String getTitle() {
        return "Relate objects";

    }

    @Override
    public String getHelp() {
        return "****Currently distance map (location of children within parents) doesn't take difference in XY and Z calibration into account***";
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjSet parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjSet childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);
        String testChildObjectsName = parameters.getValue(TEST_CHILD_OBJECTS);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);

        switch (relateMode) {
            case MATCHING_IDS:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case PROXIMITY_TO_CHILDREN:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by proximity to children");
                proximityToChildren(parentObjects,childObjects,testChildObjectsName,linkingDistance);
                break;

            case SPATIAL_OVERLAP:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by spatial overlap");
                spatialLinking(parentObjects,childObjects);
                break;
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(PARENT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CHILD_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(RELATE_MODE, Parameter.CHOICE_ARRAY,RELATE_MODES[0],RELATE_MODES));
        parameters.addParameter(new Parameter(TEST_CHILD_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.addParameter(new Parameter(LINKING_DISTANCE,Parameter.DOUBLE,1.0));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RELATE_MODE));

        if (parameters.getValue(RELATE_MODE).equals(PROXIMITY_TO_CHILDREN)) {
            returnedParameters.addParameter(parameters.getParameter(TEST_CHILD_OBJECTS));
            returnedParameters.addParameter(parameters.getParameter(LINKING_DISTANCE));

            String parentObjectNames = parameters.getValue(PARENT_OBJECTS);
            parameters.updateValueSource(TEST_CHILD_OBJECTS,parentObjectNames);

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS),DIST_EDGE_PX_MEAS);

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS));

    }
}

