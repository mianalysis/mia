package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;

import java.util.TreeSet;

/**
 * Created by sc13967 on 04/05/2017.
 */
public class RelateObjects extends Module {
    public final static String PARENT_OBJECTS = "Parent (larger) objects";
    public final static String CHILD_OBJECTS = "Child (smaller) objects";
    public final static String RELATE_MODE = "Method to relate objects";
    public final static String REFERENCE_POINT = "Reference point";
    public final static String TEST_CHILD_OBJECTS = "Child objects to test against";
    public static final String LIMIT_LINKING_BY_DISTANCE = "Limit linking by distance";
    public final static String LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";
    public static final String MINIMUM_PERCENTAGE_OVERLAP = "Minimum percentage overlap";
    public static final String REQUIRE_CENTROID_OVERLAP = "Require centroid overlap";
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

    public interface InsideOutsideModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_ONLY = "Inside only (set outside to zero)";
        String OUTSIDE_ONLY = "Outside only (set inside to zero)";

        String[] ALL = new String[]{INSIDE_AND_OUTSIDE,INSIDE_ONLY,OUTSIDE_ONLY};

    }

    public interface Measurements {
        String DIST_SURFACE_PX = "DIST_TO_${PARENT}_SURF_(PX)";
        String DIST_CENTROID_PX = "DIST_TO_${PARENT}_CENT_(PX)";
        String DIST_SURFACE_CAL = "DIST_TO_${PARENT}_SURF_(${CAL})";
        String DIST_CENTROID_CAL = "DIST_TO_${PARENT}_CENT_(${CAL})";
        String DIST_CENT_SURF_PX = "DIST_FROM_CENT_TO_${PARENT}_SURF_(PX)";
        String DIST_CENT_SURF_CAL = "DIST_FROM_CENT_TO_${PARENT}_SURF_(${CAL})";
        String DIST_CENT_SURF_FRAC = "DIST_FROM_CENT_TO_${PARENT}_SURF_(FRAC)";
        String OVERLAP_PC = "OVERLAP_WITH_${PARENT}_PERCENTAGE";

    }


    public static String getFullName(String measurement,String parentName) {
        return Units.replace("RELATE_OBJ // "+measurement.replace("${PARENT}",parentName));
    }

    public void linkMatchingIDs(ObjCollection parentObjects, ObjCollection childObjects) {
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
     */
    public void proximity(ObjCollection parentObjects, ObjCollection childObjects) {
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
        String referencePoint = parameters.getValue(REFERENCE_POINT);
        boolean limitLinking = parameters.getValue(LIMIT_LINKING_BY_DISTANCE);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);

        String moduleName = RelateObjects.class.getSimpleName();

        int iter = 1;
        int numberOfChildren = childObjects.size();

        for (Obj childObject:childObjects.values()) {
            double minDist = Double.MAX_VALUE;
            Obj minLink = null;
            double dpp = childObject.getDistPerPxXY();

            for (Obj parentObject : parentObjects.values()) {
                if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                // Calculating the object spacing
                switch (referencePoint) {
                    case ReferencePoints.CENTROID:
                        double dist = childObject.getCentroidSeparation(parentObject,true);

                        if (dist < minDist) {
                            if (limitLinking && dist > linkingDistance) continue;
                            minDist = dist;
                            minLink = parentObject;
                        }

                        break;

                    case ReferencePoints.SURFACE:
                        dist = childObject.getSurfaceSeparation(parentObject,true);

                        if (Math.abs(dist) < Math.abs(minDist)) {
                            if (limitLinking && Math.abs(dist) > linkingDistance) continue;
                            minDist = dist;
                            minLink = parentObject;
                        }

                        break;

                    case ReferencePoints.CENTROID_TO_SURFACE:
                        double childXCent = childObject.getXMean(true);
                        double childYCent = childObject.getYMean(true);
                        double childZCent = childObject.getZMean(true, true);
                        double childZCentSlice = childObject.getZMean(true, false);

                        Point<Integer> currentPoint = new Point<>((int) Math.round(childXCent), (int) Math.round(childYCent), (int) childZCentSlice);

                        double[] parentX = parentObject.getSurfaceX(true);
                        double[] parentY = parentObject.getSurfaceY(true);
                        double[] parentZ = parentObject.getSurfaceZ(true, true);

                        boolean isInside = false;

                        for (int i = 0; i < parentX.length; i++) {
                            double xDist = childXCent - parentX[i];
                            double yDist = childYCent - parentY[i];
                            double zDist = childZCent - parentZ[i];
                            dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
                            if (dist < Math.abs(minDist)) {
                                if (limitLinking && dist > linkingDistance) continue;

                                minDist = dist;
                                minLink = parentObject;
                                isInside = parentObject.getPoints().contains(currentPoint);
                            }
                        }

                        // If this point is inside the parent the distance should be negative
                        if (isInside) minDist = -minDist;

                        break;

                }
            }

            // If using centroid to surface proximity and inside only, calculate the fractional distance
            if (referencePoint.equals(ReferencePoints.CENTROID_TO_SURFACE)
                    && parameters.getValue(INSIDE_OUTSIDE_MODE).equals(InsideOutsideModes.INSIDE_ONLY)) {
                calculateFractionalDistance(childObject,minLink,minDist);
            }

            // Applying the inside outside mode (doesn't apply for centroid-centroid linking)
            if (referencePoint.equals(ReferencePoints.CENTROID_TO_SURFACE)
                    || referencePoint.equals(ReferencePoints.SURFACE)) {
                minDist = applyInsideOutsidePolicy(minDist);
            }

            // Adding measurements to the input object
            applyMeasurements(childObject,parentObjects,minDist,minLink);

            writeMessage("Processed "+(iter++)+" of "+numberOfChildren+" objects");

        }
    }

    public void calculateFractionalDistance(Obj childObject, Obj parentObject, double minDist) {
        // Calculating the furthest distance to the edge
        if (parentObject.getMeasurement("MAX_DIST") == null) {
            // Creating an image for the parent object
            Image parentImage = parentObject.convertObjToImage("Parent");
            InvertIntensity.process(parentImage.getImagePlus());

            ImagePlus distIpl = DistanceMap.getDistanceMap(parentImage.getImagePlus(),true);

            Image projectedImage = new ProjectImage().projectImageInZ(new Image("Dist", distIpl), "Projected", ProjectImage.ProjectionModes.MAX);
            double maxDist = projectedImage.getImagePlus().getStatistics().max;

            parentObject.addMeasurement(new Measurement("MAX_DIST",maxDist));

        }

        // Adding measurement
        double maxDist = parentObject.getMeasurement("MAX_DIST").getValue();
        double frac = Math.abs(minDist/maxDist);
        String measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC, parentObject.getName());
        childObject.addMeasurement(new Measurement(measurementName, frac));

    }

    public void applyMeasurements(Obj childObject, ObjCollection parentObjects, double minDist, Obj minLink) {
        String referencePoint = parameters.getValue(REFERENCE_POINT);

        if (minLink != null) {
            double dpp = childObject.getDistPerPxXY();
            childObject.addParent(minLink);
            minLink.addChild(childObject);

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

    public void proximityToChildren(ObjCollection parentObjects, ObjCollection childObjects) {
        String testChildObjectsName = parameters.getValue(TEST_CHILD_OBJECTS);
        boolean limitLinking = parameters.getValue(LIMIT_LINKING_BY_DISTANCE);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);

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
                    if (limitLinking && dist <= linkingDistance) {
                        childObject.addParent(parentObject);
                        parentObject.addChild(childObject);

                    }
                }
            }
        }
    }

    public void spatialOverlap(ObjCollection parentObjects, ObjCollection childObjects, double minOverlap,
                               boolean centroidOverlap, boolean linkInSameFrame) {

        int nCombi = parentObjects.size()*childObjects.size();
        int count = 0;
        String overlapMeasurementName = getFullName(Measurements.OVERLAP_PC,parentObjects.getName());

        // Runs through each child object against each parent object
        for (Obj parentObject:parentObjects.values()) {
            for (Obj childObject:childObjects.values()) {
                // Testing if the two objects are in the same frame (if this matters)
                if (linkInSameFrame && parentObject.getT() != childObject.getT()) continue;

                // If requiring the child centroid is overlapped with the parent object
                if (centroidOverlap) {
                    int xCent = (int) Math.round(childObject.getXMean(true));
                    int yCent = (int) Math.round(childObject.getYMean(true));
                    int zCent = (int) Math.round(childObject.getZMean(true, false)); // Relates to image location
                    Point<Integer> centroid = new Point<>(xCent, yCent, zCent);

                    // If the centroid doesn't overlap, skip this link
                    if (!parentObject.containsPoint(centroid)) continue;

                }

                // Calculates the percentage overlap
                double nTotal = (double) childObject.getNVoxels();
                double nOverlap = (double) parentObject.getOverlap(childObject);
                double overlap  = (nOverlap/nTotal)*100;

                // Testing the minimum overlap requirement
                if (overlap < minOverlap) continue;

                // If the tests are successful, add the link.  If the child has already been linked, but with a smaller
                // overlap, remove that link.
                Obj oldParent = childObject.getParent(parentObject.getName());
                if (oldParent != null) {
                    if (childObject.getMeasurement(overlapMeasurementName).getValue() < overlap) {
                        oldParent.removeChild(childObject);
                    } else {
                        // If the previous link had a better overlap, skip the assignment
                        continue;
                    }
                }

                // Creating the link
                parentObject.addChild(childObject);
                childObject.addParent(parentObject);

                // Adding the overlap as a measurement
                Measurement measurement = new Measurement(getFullName(Measurements.OVERLAP_PC,parentObject.getName()));
                measurement.setValue(overlap);
                childObject.addMeasurement(measurement);

            }

            writeMessage("Compared "+(childObjects.size()*++count)+" of "+nCombi+" pairs");

        }
    }

    public double applyInsideOutsidePolicy(double minDist) {
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE);

        switch (insideOutsideMode) {
            case InsideOutsideModes.INSIDE_AND_OUTSIDE:
                return minDist;

            case InsideOutsideModes.INSIDE_ONLY:
                return Math.min(0,minDist);

            case InsideOutsideModes.OUTSIDE_ONLY:
                return Math.max(0,minDist);

        }

        return 0;

    }


    @Override
    public String getTitle() {
        return "Relate objects";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
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
        boolean limitLinking = parameters.getValue(LIMIT_LINKING_BY_DISTANCE);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);
        double minOverlap = parameters.getValue(MINIMUM_PERCENTAGE_OVERLAP);
        boolean centroidOverlap = parameters.getValue(REQUIRE_CENTROID_OVERLAP);

        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                writeMessage("Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case RelateModes.PROXIMITY:
                writeMessage("Relating objects by proximity");
                proximity(parentObjects,childObjects);
                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                writeMessage("Relating objects by proximity to children");
                proximityToChildren(parentObjects,childObjects);
                break;

            case RelateModes.SPATIAL_OVERLAP:
                writeMessage("Relating objects by spatial overlap");
                spatialOverlap(parentObjects,childObjects,minOverlap,centroidOverlap,linkInSameFrame);
                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(PARENT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CHILD_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(RELATE_MODE, Parameter.CHOICE_ARRAY,RelateModes.MATCHING_IDS,RelateModes.ALL));
        parameters.add(new Parameter(REFERENCE_POINT,Parameter.CHOICE_ARRAY,ReferencePoints.CENTROID,ReferencePoints.ALL));
        parameters.add(new Parameter(TEST_CHILD_OBJECTS,Parameter.CHILD_OBJECTS,null));
        parameters.add(new Parameter(LIMIT_LINKING_BY_DISTANCE,Parameter.BOOLEAN,false));
        parameters.add(new Parameter(LINKING_DISTANCE,Parameter.DOUBLE,1.0));
        parameters.add(new Parameter(INSIDE_OUTSIDE_MODE,Parameter.CHOICE_ARRAY,InsideOutsideModes.INSIDE_AND_OUTSIDE,InsideOutsideModes.ALL));
        parameters.add(new Parameter(MINIMUM_PERCENTAGE_OVERLAP,Parameter.DOUBLE,0d));
        parameters.add(new Parameter(REQUIRE_CENTROID_OVERLAP,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(LINK_IN_SAME_FRAME,Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.add(parameters.getParameter(RELATE_MODE));

        String referencePoint = parameters.getValue(REFERENCE_POINT);
        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                returnedParameters.add(parameters.getParameter(REFERENCE_POINT));
                returnedParameters.add(parameters.getParameter(LIMIT_LINKING_BY_DISTANCE));
                if (parameters.getValue(LIMIT_LINKING_BY_DISTANCE)) {
                    returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));
                }

                if (referencePoint.equals(ReferencePoints.CENTROID_TO_SURFACE)
                        || referencePoint.equals(ReferencePoints.SURFACE)) {
                    returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));
                }

                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                returnedParameters.add(parameters.getParameter(TEST_CHILD_OBJECTS));
                returnedParameters.add(parameters.getParameter(LIMIT_LINKING_BY_DISTANCE));
                if (parameters.getValue(LIMIT_LINKING_BY_DISTANCE)) {
                    returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));
                }

                if (referencePoint.equals(ReferencePoints.CENTROID_TO_SURFACE)
                        || referencePoint.equals(ReferencePoints.SURFACE)) {
                    returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));
                }

                String parentObjectNames = parameters.getValue(PARENT_OBJECTS);
                parameters.updateValueSource(TEST_CHILD_OBJECTS,parentObjectNames);

                break;

            case RelateModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_PERCENTAGE_OVERLAP));
                returnedParameters.add(parameters.getParameter(REQUIRE_CENTROID_OVERLAP));
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

        if (parentObjectName == null || childObjectsName == null) return objectMeasurementReferences;

        String measurementName = getFullName(Measurements.DIST_SURFACE_PX,parentObjectName);
        MeasurementReference distSurfPx = objectMeasurementReferences.getOrPut(measurementName);
        distSurfPx.setDescription("Shortest distance between the surface of this object and that of the closest \""
                + parentObjectName+"\" object.  Negative values indicate this object is inside the relevant \""
                +parentObjectName+"\" object. Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_SURFACE_CAL,parentObjectName);
        MeasurementReference distSurfCal = objectMeasurementReferences.getOrPut(measurementName);
        distSurfCal.setDescription("Shortest distance between the surface of this object and that of the closest \""
                + parentObjectName+"\" object.  Negative values indicate this object is inside the relevant \""
                +parentObjectName+"\" object. Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

        measurementName = getFullName(Measurements.DIST_CENTROID_PX,parentObjectName);
        MeasurementReference distCentPx = objectMeasurementReferences.getOrPut(measurementName);
        distCentPx.setDescription("Distance between the centroid of this object and that of the closest \""
                + parentObjectName+"\"object.  Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_CENTROID_CAL,parentObjectName);
        MeasurementReference distCentCal = objectMeasurementReferences.getOrPut(measurementName);
        distCentCal.setDescription("Distance between the centroid of this object and that of the closest \""
                + parentObjectName+"\"object.  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_PX,parentObjectName);
        MeasurementReference distCentSurfPx = objectMeasurementReferences.getOrPut(measurementName);
        distCentSurfPx.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                "closest \""+ parentObjectName+"\" object.  Negative values indicate this object is inside the " +
                "relevant \""+parentObjectName+"\" object. Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL,parentObjectName);
        MeasurementReference distCentSurfCal = objectMeasurementReferences.getOrPut(measurementName);
        distCentSurfCal.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                "closest \""+ parentObjectName+"\" object.  Negative values indicate this object is inside the " +
                "relevant \""+parentObjectName+"\" object. Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                "units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC,parentObjectName);
        MeasurementReference distCentSurfFrac = objectMeasurementReferences.getOrPut(measurementName);
        distCentSurfFrac.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                "closest \""+ parentObjectName+"\" object.  Calculated as a fraction of the furthest possible distance " +
                "to the \""+parentObjectName+"\" surface.");

        measurementName = getFullName(Measurements.OVERLAP_PC,parentObjectName);
        MeasurementReference overlapPercentage  = objectMeasurementReferences.getOrPut(measurementName);
        overlapPercentage.setDescription("Percentage of pixels that overlap with the \""+ parentObjectName+"\" object "+
                "with which it has the largest overlap.");

        distSurfPx.setImageObjName(childObjectsName);
        distCentPx.setImageObjName(childObjectsName);
        distSurfCal.setImageObjName(childObjectsName);
        distCentCal.setImageObjName(childObjectsName);
        distCentSurfPx.setImageObjName(childObjectsName);
        distCentSurfCal.setImageObjName(childObjectsName);
        distCentSurfFrac.setImageObjName(childObjectsName);
        overlapPercentage.setImageObjName(childObjectsName);

        distCentPx.setCalculated(false);
        distCentCal.setCalculated(false);
        distSurfPx.setCalculated(false);
        distSurfCal.setCalculated(false);
        distCentSurfPx.setCalculated(false);
        distCentSurfCal.setCalculated(false);
        distCentSurfFrac.setCalculated(false);
        overlapPercentage.setCalculated(false);

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

                        if (parameters.getValue(INSIDE_OUTSIDE_MODE).equals(InsideOutsideModes.INSIDE_ONLY)) {
                            distCentSurfFrac.setCalculated(true);
                        }
                        break;
                }
                break;

            case RelateModes.SPATIAL_OVERLAP:
                overlapPercentage.setCalculated(true);
                break;
        }

        return objectMeasurementReferences;

    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS));

    }
}
