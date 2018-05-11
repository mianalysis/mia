package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import ij.IJ;
import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;

import java.util.ArrayList;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class RelateObjects extends Module {
    public final static String PARENT_OBJECTS = "Parent (larger) objects";
    public final static String CHILD_OBJECTS = "Child (smaller) objects";
    public final static String RELATE_MODE = "Method to relate objects";
    public final static String REFERENCE_POINT = "Reference point";
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
        String CENTROID_TO_SURFACE = "Child centroid to parent surface";

        String[] ALL = new String[]{CENTROID, SURFACE, CENTROID_TO_SURFACE};

    }

    public interface Measurements {
        String DIST_SURFACE_PX = "DIST_TO_${PARENT}_SURF_(PX)";
        String DIST_CENTROID_PX = "DIST_TO_${PARENT}_CENT_(PX)";
        String DIST_SURFACE_CAL = "DIST_TO_${PARENT}_SURF_(${CAL})";
        String DIST_CENTROID_CAL = "DIST_TO_${PARENT}_CENT_(${CAL})";
        String DIST_CENT_SURF_PX = "DIST_FROM_CENT_TO_${PARENT}_SURF_(PX)";
        String DIST_CENT_SURF_CAL = "DIST_FROM_CENT_TO_${PARENT}_SURF_(${CAL})";

    }

    public static String getFullName(String measurement,String parentName) {
        return Units.replace("RELATE_OBJ//"+measurement.replace("${PARENT}",parentName));
    }


    public static void linkMatchingIDs(ObjCollection parentObjects, ObjCollection childObjects) {
        for (Obj parentObject:parentObjects.values()) {
            int ID = parentObject.getID();

            Obj childObject = childObjects.get(ID);

            if (childObject != null) {
                parentObject.addChild(childObject, childObjects.is2D());
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
    public void proximity(ObjCollection parentObjects, ObjCollection childObjects, double linkingDistance, String referencePoint, boolean linkInSameFrame) {
        String moduleName = RelateObjects.class.getSimpleName();

        int iter = 1;
        int numberOfChildren = childObjects.size();

        for (Obj childObject:childObjects.values()) {
            writeMessage("Processing object "+(iter++)+" of "+numberOfChildren);

            double minDist = Double.MAX_VALUE;
            Obj minLink = null;
            double dpp = childObject.getDistPerPxXY();

            // If no parent objects were detected
            if (parentObjects.size() != 0) {
                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                    // Calculating the object spacing
                    switch (referencePoint) {
                        case ReferencePoints.CENTROID:
                            double xDist = childObject.getXMean(true) - parentObject.getXMean(true);
                            double yDist = childObject.getYMean(true) - parentObject.getYMean(true);
                            double zDist = childObject.getZMean(true, true) - parentObject.getZMean(true, true);
                            double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                            if (dist < minDist && dist <= linkingDistance) {
                                minDist = dist;
                                minLink = parentObject;
                            }

                            break;

                        case ReferencePoints.SURFACE:
                            // Getting coordinates for the surface points (6-way connectivity)
                            double[] parentX = parentObject.getSurfaceX(true);
                            double[] parentY = parentObject.getSurfaceY(true);
                            double[] parentZ = parentObject.getSurfaceZ(true, true);

                            double[] childX = childObject.getSurfaceX(true);
                            double[] childY = childObject.getSurfaceY(true);
                            double[] childZ = childObject.getSurfaceZ(true, true);
                            double[] childZSlice = childObject.getSurfaceZ(true, false);

                            // Measuring point-to-point distances on both object surfaces
                            for (int j = 0; j < childX.length; j++) {
                                double currMinDist = Double.MAX_VALUE;
                                Obj currMinLink = null;
                                boolean isInside = false;

                                for (int i = 0; i < parentX.length; i++) {
                                    xDist = childX[j] - parentX[i];
                                    yDist = childY[j] - parentY[i];
                                    zDist = childZ[j] - parentZ[i];
                                    dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                                    if (dist < currMinDist && dist <= linkingDistance) {
                                        currMinDist = dist;
                                        currMinLink = parentObject;

                                        // If this point is inside the parent the distance should be negative
                                        Point<Integer> currentPoint = new Point<>((int) childX[j], (int) childY[j], (int) childZSlice[j]);
                                        isInside = parentObject.getPoints().contains(currentPoint);
                                    }
                                }

                                // Comparing the closest distance for this child point to the previous minimum distance
                                if (isInside) currMinDist = -currMinDist;
                                if (currMinDist < minDist) {
                                    minDist = currMinDist;
                                    minLink = currMinLink;
                                }
                            }

                            break;

                        case ReferencePoints.CENTROID_TO_SURFACE:
                            double childXCent = childObject.getXMean(true);
                            double childYCent = childObject.getYMean(true);
                            double childZCent = childObject.getZMean(true, true);

                            parentX = parentObject.getSurfaceX(true);
                            parentY = parentObject.getSurfaceY(true);
                            parentZ = parentObject.getSurfaceZ(true, true);

                            double currMinDist = Double.MAX_VALUE;
                            Obj currMinLink = null;
                            boolean isInside = false;

                            for (int i = 0; i < parentX.length; i++) {
                                xDist = childXCent - parentX[i];
                                yDist = childYCent - parentY[i];
                                zDist = childZCent - parentZ[i];
                                dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

                                if (dist < currMinDist && dist <= linkingDistance) {
                                    currMinDist = dist;
                                    currMinLink = parentObject;

                                    // If this point is inside the parent the distance should be negative
                                    Point<Integer> currentPoint = new Point<>((int) Math.round(childXCent), (int) Math.round(childYCent), (int) Math.round(childZCent));
                                    isInside = parentObject.getPoints().contains(currentPoint);
                                }
                            }

                            // Comparing the closest distance for this child point to the previous minimum distance
                            if (isInside) currMinDist = -currMinDist;
                            if (currMinDist < minDist) {
                                minDist = currMinDist;
                                minLink = currMinLink;
                            }

                            break;

                    }
                }
            }

            if (minLink != null) {
                childObject.addParent(minLink);
                minLink.addChild(childObject, childObjects.is2D());

                switch (referencePoint) {
                    case ReferencePoints.CENTROID: {
                        String measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist));
                        measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                        break;
                    }
                    case ReferencePoints.SURFACE: {
                        String measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist));
                        measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                        break;
                    }
                    case ReferencePoints.CENTROID_TO_SURFACE: {
                        String measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist));
                        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                        break;
                    }
                }

            } else {
                switch (referencePoint) {
                    case ReferencePoints.CENTROID: {
                        String measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                        measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                        break;
                    }
                    case ReferencePoints.SURFACE: {
                        String measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                        measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                        break;
                    }
                    case ReferencePoints.CENTROID_TO_SURFACE: {
                        String measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());
                        childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                        break;
                    }
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
                    if (dist <= linkingDistance) {
                        childObject.addParent(parentObject);
                        parentObject.addChild(childObject, childObjects.is2D());

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
                        parentObject.addChild(childObject, childObjects.is2D());
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
        return "";
    }

    @Override
    public void run(Workspace workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        ObjCollection childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
        String testChildObjectsName = parameters.getValue(TEST_CHILD_OBJECTS);
        String referencePoint = parameters.getValue(REFERENCE_POINT);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);


        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                writeMessage("Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case RelateModes.PROXIMITY:
                writeMessage("Relating objects by proximity");
                proximity(parentObjects,childObjects,linkingDistance,referencePoint,linkInSameFrame);
                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                writeMessage("Relating objects by proximity to children");
                proximityToChildren(parentObjects,childObjects,testChildObjectsName,linkingDistance);
                break;

            case RelateModes.SPATIAL_OVERLAP:
                writeMessage("Relating objects by spatial overlap");
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
        objectMeasurementReferences.setAllCalculated(false);

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);

        String measurementName = getFullName(Measurements.DIST_SURFACE_PX,parentObjectName);
        MeasurementReference distSurfPx = objectMeasurementReferences.getOrPut(measurementName);
        measurementName = getFullName(Measurements.DIST_CENTROID_PX,parentObjectName);
        MeasurementReference distCentPx = objectMeasurementReferences.getOrPut(measurementName);
        measurementName = getFullName(Measurements.DIST_SURFACE_CAL,parentObjectName);
        MeasurementReference distSurfCal = objectMeasurementReferences.getOrPut(measurementName);
        measurementName = getFullName(Measurements.DIST_CENTROID_CAL,parentObjectName);
        MeasurementReference distCentCal = objectMeasurementReferences.getOrPut(measurementName);
        measurementName = getFullName(Measurements.DIST_CENT_SURF_PX,parentObjectName);
        MeasurementReference distCentSurfPx = objectMeasurementReferences.getOrPut(measurementName);
        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL,parentObjectName);
        MeasurementReference distCentSurfCal = objectMeasurementReferences.getOrPut(measurementName);


        distSurfPx.setImageObjName(childObjectsName);
        distCentPx.setImageObjName(childObjectsName);
        distSurfCal.setImageObjName(childObjectsName);
        distCentCal.setImageObjName(childObjectsName);
        distCentSurfPx.setImageObjName(childObjectsName);
        distCentSurfCal.setImageObjName(childObjectsName);

        distCentPx.setCalculated(false);
        distCentCal.setCalculated(false);
        distSurfPx.setCalculated(false);
        distSurfCal.setCalculated(false);
        distCentSurfPx.setCalculated(false);
        distCentSurfCal.setCalculated(false);

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_POINT)) {
                    case ReferencePoints.CENTROID:
                        distCentPx.setCalculated(true);
                        distCentCal.setCalculated(true);
                        break;

                    case ReferencePoints.SURFACE:
                        distSurfPx.setCalculated(true);
                        distSurfCal.setCalculated(true);
                        break;

                    case ReferencePoints.CENTROID_TO_SURFACE:
                        distCentSurfPx.setCalculated(true);
                        distCentSurfCal.setCalculated(true);
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
