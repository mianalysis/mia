package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import ij.IJ;
import ij.ImagePlus;
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
    public final static String REFERENCE_POINT = "ImageObjReference point";
    public final static String TEST_CHILD_OBJECTS = "Child objects to test against";
    public final static String LINKING_DISTANCE = "Maximum linking distance (px)";
    public final static String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public interface RelateModes {
        String MATCHING_IDS = "Matching IDs";
        String PROXIMITY = "Proximity";
        String PROXIMITY_TO_CHILDREN = "Proximity to children";
        String SPATIAL_OVERLAP = "Spatial overlap";

        String[] ALL = new String[]{MATCHING_IDS, PROXIMITY, PROXIMITY_TO_CHILDREN, SPATIAL_OVERLAP};

    }

    public interface ReferencePoints {
        String CENTROID = "Centroid";
        String SURFACE = "Surface";

        String[] ALL = new String[]{CENTROID, SURFACE};

    }

    public interface Measurements {
        String DIST_SURFACE_PX_MEAS = "RELATE_OBJ//DIST_TO_PARENT_SURF_PX";
        String DIST_CENTROID_PX_MEAS = "RELATE_OBJ//DIST_TO_PARENT_CENT_PX";
        String DIST_SURFACE_CAL_MEAS = "RELATE_OBJ//DIST_TO_PARENT_SURF_CAL";
        String DIST_CENTROID_CAL_MEAS = "RELATE_OBJ//DIST_TO_PARENT_CENT_CAL";

    }


    public static void linkMatchingIDs(ObjCollection parentObjects, ObjCollection childObjects) {
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
    public static void proximity(ObjCollection parentObjects, ObjCollection childObjects, double linkingDistance, String referencePoint, boolean linkInSameFrame, boolean verbose) {
        String moduleName = RelateObjects.class.getSimpleName();

        int iter = 1;
        int numberOfChildren = childObjects.size();

        for (Obj childObject:childObjects.values()) {
            if (verbose) System.out.println("["+moduleName+"] Processing object "+(iter++)+" of "+numberOfChildren);

            double minDist = Double.MAX_VALUE;
            double dpp = parentObjects.values().iterator().next().getDistPerPxXY();
            Obj currentLink = null;

            for (Obj parentObject:parentObjects.values()) {
                if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                // Calculating the object spacing
                switch (referencePoint) {
                    case ReferencePoints.CENTROID:
                        double xDist = childObject.getXMean(true) - parentObject.getXMean(true);
                        double yDist = childObject.getYMean(true) - parentObject.getYMean(true);
                        double zDist = childObject.getZMean(true, true) - parentObject.getZMean(true, true);
                        double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                        if (dist < minDist && dist < linkingDistance) {
                            minDist = dist;
                            currentLink = parentObject;
                        }

                        break;

                    case ReferencePoints.SURFACE:
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
                                xDist = childX[j] - parentX[i];
                                yDist = childY[j] - parentY[i];
                                zDist = childZ[j] - parentZ[i];
                                dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                                if (dist < minDist && dist < linkingDistance) {
                                    minDist = dist;
                                    currentLink = parentObject;

                                }
                            }
                        }

                        break;

                }
            }

            if (currentLink != null) {
                childObject.addParent(currentLink);
                currentLink.addChild(childObject);

                if (referencePoint.equals(ReferencePoints.CENTROID)) {
                    childObject.addMeasurement(new Measurement(Measurements.DIST_CENTROID_PX_MEAS,minDist));
                    childObject.addMeasurement(new Measurement(Measurements.DIST_CENTROID_CAL_MEAS,minDist*dpp));

                } else if (referencePoint.equals(ReferencePoints.SURFACE)) {
                    childObject.addMeasurement(new Measurement(Measurements.DIST_SURFACE_PX_MEAS,minDist));
                    childObject.addMeasurement(new Measurement(Measurements.DIST_SURFACE_CAL_MEAS,minDist*dpp));

                }
            }
        }
    }

    public static void proximityToChildren(ObjCollection parentObjects, ObjCollection childObjects, String testChildObjectsName, double linkingDistance) {
        // Runs through each child object against each parent object
        for (Obj parentObject:parentObjects.values()) {
            // Getting children of the parent to be used as references
            ObjCollection testChildren = parentObject.getChildren(testChildObjectsName);

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

    public static void spatialLinking(ObjCollection parentObjects, ObjCollection childObjects) {
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

                        break;

                    }
                }
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
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjCollection childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);
        String testChildObjectsName = parameters.getValue(TEST_CHILD_OBJECTS);
        String referencePoint = parameters.getValue(REFERENCE_POINT);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);

        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case RelateModes.PROXIMITY:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by proximity");
                proximity(parentObjects,childObjects,linkingDistance,referencePoint,linkInSameFrame,verbose);
                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by proximity to children");
                proximityToChildren(parentObjects,childObjects,testChildObjectsName,linkingDistance);
                break;

            case RelateModes.SPATIAL_OVERLAP:
                if (verbose) System.out.println("["+moduleName+"] Relating objects by spatial overlap");
                spatialLinking(parentObjects,childObjects);
                break;
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(PARENT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CHILD_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(RELATE_MODE, Parameter.CHOICE_ARRAY,RelateModes.MATCHING_IDS,RelateModes.ALL));
        parameters.add(new Parameter(TEST_CHILD_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.add(new Parameter(LINKING_DISTANCE,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(REFERENCE_POINT,Parameter.CHOICE_ARRAY,ReferencePoints.CENTROID,ReferencePoints.ALL));
        parameters.add(new Parameter(LINK_IN_SAME_FRAME,Parameter.BOOLEAN,true));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIST_SURFACE_PX_MEAS));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIST_CENTROID_PX_MEAS));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIST_SURFACE_CAL_MEAS));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.DIST_CENTROID_CAL_MEAS));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATE_MODE));

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                returnedParameters.add(parameters.getParameter(REFERENCE_POINT));
                returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));

                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                returnedParameters.add(parameters.getParameter(TEST_CHILD_OBJECTS));
                returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));

                String parentObjectNames = parameters.getValue(PARENT_OBJECTS);
                parameters.updateValueSource(TEST_CHILD_OBJECTS,parentObjectNames);

                break;
        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);

        MeasurementReference distSurfPx = objectMeasurementReferences.get(Measurements.DIST_SURFACE_PX_MEAS);
        MeasurementReference distCentPx = objectMeasurementReferences.get(Measurements.DIST_CENTROID_PX_MEAS);
        MeasurementReference distSurfCal = objectMeasurementReferences.get(Measurements.DIST_SURFACE_CAL_MEAS);
        MeasurementReference distCentCal = objectMeasurementReferences.get(Measurements.DIST_CENTROID_CAL_MEAS);

        distSurfPx.setImageObjName(childObjectsName);
        distCentPx.setImageObjName(childObjectsName);
        distSurfCal.setImageObjName(childObjectsName);
        distCentCal.setImageObjName(childObjectsName);

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_POINT)) {
                    case ReferencePoints.CENTROID:
                        distCentPx.setCalculated(true);
                        distCentCal.setCalculated(true);
                        distSurfPx.setCalculated(false);
                        distSurfCal.setCalculated(false);

                        break;

                    case ReferencePoints.SURFACE:
                        distCentPx.setCalculated(false);
                        distCentCal.setCalculated(false);
                        distSurfPx.setCalculated(true);
                        distSurfCal.setCalculated(true);

                        break;
                }
                break;
        }

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS));

    }
}
