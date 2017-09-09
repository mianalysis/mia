package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.binary.distmap.DistanceTransform3DShort;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.ArrayList;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class RelateObjects extends HCModule {
    public final static String PARENT_OBJECTS = "Parent (larger) objects";
    public final static String CHILD_OBJECTS = "Child (smaller) objects";
    public final static String RELATE_MODE = "Method to relate objects";
    public final static String REFERENCE_POINT = "Reference point";
    public final static String TEST_CHILD_OBJECTS = "Child objects to test against";
    public final static String LINKING_DISTANCE = "Linking distance";

    private final static String MATCHING_IDS = "Matching IDs";
    private final static String PROXIMITY = "Proximity";
    private final static String PROXIMITY_TO_CHILDREN = "Proximity to children";
    private final static String SPATIAL_OVERLAP = "Spatial overlap";
    public final static String[] RELATE_MODES = new String[]{MATCHING_IDS,PROXIMITY,PROXIMITY_TO_CHILDREN,SPATIAL_OVERLAP};

    private final static String CENTROID = "Centroid";
    private final static String SURFACE = "Surface";
    public final static String[] REFERENCE_POINTS = new String[]{CENTROID,SURFACE};

    private final static String DIST_EDGE_PX_MEAS = "Distance from parent edge (px)";
    private final static String DIST_SURFACE_PX_MEAS = "Distance to parent surface (px)";
    private final static String DIST_CENTROID_PX_MEAS = "Distance to parent centroid (px)";
//    private final static String DIST_EDGE_CAL_MEAS = "Distance from parent edge (cal)";
    private final static String DIST_SURFACE_CAL_MEAS = "Distance to parent surface (cal)";
    private final static String DIST_CENTROID_CAL_MEAS = "Distance to parent centroid (cal)";


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

    /**
     * Iterates over each testObject, calculating getting the smallest distance to a parentObject.  If this is smaller
     * than linkingDistance the link is assigned.
     * @param parentObjects
     * @param childObjects
     * @param linkingDistance
     */
    public static void proximity(ObjSet parentObjects, ObjSet childObjects, double linkingDistance, String referencePoint, boolean verbose) {
        for (Obj childObject:childObjects.values()) {
            double minDist = Double.MAX_VALUE;
            double dpp = parentObjects.values().iterator().next().getDistPerPxXY();
            Obj currentLink = null;

            for (Obj parentObject:parentObjects.values()) {
                // Calculating the object spacing
                if (referencePoint.equals(CENTROID)) {
                    double xDist = childObject.getXMean(true) - parentObject.getXMean(true);
                    double yDist = childObject.getYMean(true) - parentObject.getYMean(true);
                    double zDist = childObject.getZMean(true, true) - parentObject.getZMean(true, true);
                    double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                    if (dist < minDist && dist < linkingDistance) {
                        minDist = dist;
                        currentLink = parentObject;
                    }

                } else if (referencePoint.equals(SURFACE)) {
                    // Getting coordinates for the surface points (6-way connectivity)
                    double[] parentX = parentObject.getSurfaceX(true);
                    double[] parentY = parentObject.getSurfaceY(true);
                    double[] parentZ = parentObject.getSurfaceZ(true,true);

                    double[] childX = childObject.getSurfaceX(true);
                    double[] childY = childObject.getSurfaceY(true);
                    double[] childZ = childObject.getSurfaceZ(true,true);

                    // Measuring point-to-point distances on both object surfaces
                    for (int i = 0;i<parentX.length;i++) {
                        for (int j = 0;j<childX.length;j++) {
                            double xDist = childX[j] - parentX[i];
                            double yDist = childY[j] - parentY[i];
                            double zDist = childZ[j] - parentZ[i];
                            double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                            if (dist < minDist && dist < linkingDistance) {
                                minDist = dist;
                                currentLink = parentObject;

                            }
                        }
                    }
                }
            }

            if (currentLink != null) {
                childObject.addParent(currentLink);
                currentLink.addChild(childObject);

                if (referencePoint.equals(CENTROID)) {
                    childObject.addMeasurement(new MIAMeasurement(DIST_CENTROID_PX_MEAS,minDist));
                    childObject.addMeasurement(new MIAMeasurement(DIST_CENTROID_CAL_MEAS,minDist*dpp));

                } else if (referencePoint.equals(SURFACE)) {
                    childObject.addMeasurement(new MIAMeasurement(DIST_SURFACE_PX_MEAS,minDist));
                    childObject.addMeasurement(new MIAMeasurement(DIST_SURFACE_CAL_MEAS,minDist*dpp));

                }
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
                double xCentTest = testChild.getXMean(true);
                double yCentTest = testChild.getYMean(true);
                double zCentTest = testChild.getZMean(true,true);

                // Running through all children to relate
                for (Obj childObject : childObjects.values()) {
                    double xDist = xCentTest - childObject.getXMean(true);
                    double yDist = yCentTest - childObject.getYMean(true);
                    double zDist = zCentTest - childObject.getZMean(true,true);

                    // If the test object and the current object is less than the linking distance, assign the relationship
                    double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
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
            ArrayList<Integer> parentX = parentObject.getXCoords();
            ArrayList<Integer> parentY = parentObject.getYCoords();
            ArrayList<Integer> parentZ = parentObject.getZCoords();

            // Creating a Hyperstack to hold the distance transform
            int[][] range = parentObject.getCoordinateRange();
            ImagePlus ipl = IJ.createHyperStack("Objects", range[0][1]-range[0][0] + 1,
                    range[1][1]-range[1][0] + 1, 1, range[2][1]-range[2][0], 1, 8);

            // Setting pixels corresponding to the parent object to 1
            for (int i=0;i<parentX.size();i++) {
                ipl.setPosition(1,parentZ.get(i)-range[2][0]+1,1);
                ipl.getProcessor().set(parentX.get(i)-range[0][0],parentY.get(i)-range[1][0],255);

            }

            // Creating distance map using MorphoLibJ
            short[] weights = ChamferWeights3D.BORGEFORS.getShortWeights();
            DistanceTransform3DShort distTransform = new DistanceTransform3DShort(weights,true);
            ImageStack distanceMap = distTransform.distanceMap(ipl.getStack());

            for (Obj childObject:childObjects.values()) {
                // Only testing if the child is present in the same timepoint as the parent
                if (parentObject.getT() != childObject.getT()) continue;

                // Getting the child centroid location
                int xCent = (int) Math.round(childObject.getXMean(true));
                int yCent = (int) Math.round(childObject.getYMean(true));
                int zCent = (int) Math.round(childObject.getZMean(true,false)); // Relates to image location

                // Testing if the child centroid exists in the object
                for (int i=0;i<parentX.size();i++) {
                    if (parentX.get(i)==xCent & parentY.get(i)==yCent & parentZ.get(i)==zCent) {
                        parentObject.addChild(childObject);
                        childObject.addParent(parentObject);

                        // Getting position within current parent object
//                        MIAMeasurement absDistanceFromEdge = new MIAMeasurement(DIST_EDGE_PX_MEAS);
//                        int xPos = xCent-range[0][0];
//                        int yPos = yCent-range[1][0];
//                        int zPos = zCent-range[2][0];
//
//                        if (xPos < 0 | xPos > distanceMap.getWidth() | yPos < 0 | yPos > distanceMap.getHeight() | zPos < 0 | zPos >= distanceMap.size()) {
//                            absDistanceFromEdge.setValue(Double.NaN);
//                        } else {
//                            absDistanceFromEdge.setValue(distanceMap.getVoxel(xCent - range[0][0], yCent - range[1][0], zCent - range[2][0]));
//                        }
//                        childObject.addMeasurement(absDistanceFromEdge);

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
    public void run(Workspace workspace, boolean verbose) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjSet parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjSet childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);
        String testChildObjectsName = parameters.getValue(TEST_CHILD_OBJECTS);
        String referencePoint = parameters.getValue(REFERENCE_POINT);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);

        switch (relateMode) {
            case MATCHING_IDS:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case PROXIMITY:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by proximity");
                proximity(parentObjects,childObjects,linkingDistance,referencePoint,verbose);
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
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(PARENT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(CHILD_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(RELATE_MODE, Parameter.CHOICE_ARRAY,RELATE_MODES[0],RELATE_MODES));
        parameters.addParameter(new Parameter(TEST_CHILD_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.addParameter(new Parameter(LINKING_DISTANCE,Parameter.DOUBLE,1.0));
        parameters.addParameter(new Parameter(REFERENCE_POINT,Parameter.CHOICE_ARRAY,REFERENCE_POINTS[0],REFERENCE_POINTS));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(RELATE_MODE));

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case PROXIMITY:
                returnedParameters.addParameter(parameters.getParameter(REFERENCE_POINT));
                returnedParameters.addParameter(parameters.getParameter(LINKING_DISTANCE));

                break;

            case PROXIMITY_TO_CHILDREN:
                returnedParameters.addParameter(parameters.getParameter(TEST_CHILD_OBJECTS));
                returnedParameters.addParameter(parameters.getParameter(LINKING_DISTANCE));

                String parentObjectNames = parameters.getValue(PARENT_OBJECTS);
                parameters.updateValueSource(TEST_CHILD_OBJECTS,parentObjectNames);

                break;
        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        switch ((String) parameters.getValue(RELATE_MODE)) {
//            case SPATIAL_OVERLAP:
//                measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_EDGE_PX_MEAS);
//                measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_EDGE_CAL_MEAS);
//                break;

            case PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_POINT)) {
                    case CENTROID:
                        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_CENTROID_PX_MEAS);
                        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_CENTROID_CAL_MEAS);
                        break;

                    case SURFACE:
                        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_SURFACE_PX_MEAS);
                        measurements.addMeasurement(parameters.getValue(CHILD_OBJECTS), DIST_SURFACE_CAL_MEAS);
                        break;
                }
                break;
        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS));

    }
}

