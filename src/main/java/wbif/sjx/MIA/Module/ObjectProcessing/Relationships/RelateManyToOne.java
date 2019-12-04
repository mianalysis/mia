package wbif.sjx.MIA.Module.ObjectProcessing.Relationships;

import ij.ImagePlus;
import ij.Prefs;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Deprecated.RelateObjects;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Object.Point;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RelateManyToOne extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String PARENT_OBJECTS = "Parent (larger) objects";
    public static final String CHILD_OBJECTS = "Child (smaller) objects";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";
    public static final String RELATE_MODE = "Method to relate objects";
    public static final String REFERENCE_POINT = "Reference point";
    public static final String TEST_CHILD_OBJECTS = "Child objects to test against";
    public static final String LIMIT_LINKING_BY_DISTANCE = "Limit linking by distance";
    public static final String LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";
    public static final String MINIMUM_PERCENTAGE_OVERLAP = "Minimum percentage overlap";
    public static final String REQUIRE_CENTROID_OVERLAP = "Require centroid overlap";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


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
        String WAS_LINKED = "WAS_LINKED_${PARENT}";

    }


    public RelateManyToOne(ModuleCollection modules) {
        super("Relate many-to-one", modules);
    }


    public static String getFullName(String measurement,String parentName) {
        return "RELATE_MANY_TO_ONE // "+measurement.replace("${PARENT}",parentName);
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

    public static void linkByCentroidProximity(ObjCollection parentObjects, ObjCollection childObjects, boolean linkInSameFrame, double linkingDistance, int nThreads) {
        String moduleName = RelateObjects.class.getSimpleName();
        String measurementNamePx = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());

        AtomicInteger count = new AtomicInteger(1);
        int numberOfChildren = childObjects.size();

        // Ensuring all parent objects have a calculated centroid
        for (Obj parent:parentObjects.values()) parent.getMeanCentroid();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        for (Obj childObject:childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                    double dist = childObject.getCentroidSeparation(parentObject,true);

                    if (dist < minDist && dist <= linkingDistance) {
                        minDist = dist;
                        minLink = parentObject;
                    }
                }

                // Adding measurements to the input object
                if (minLink != null) {
                    childObject.addParent(minLink);
                    minLink.addChild(childObject);

                    childObject.addMeasurement(new Measurement(measurementNamePx, minDist));
                    childObject.addMeasurement(new Measurement(measurementNameCal, minDist * dpp));
                } else {
                    childObject.addMeasurement(new Measurement(measurementNamePx, Double.NaN));
                    childObject.addMeasurement(new Measurement(measurementNameCal, Double.NaN));
                }

                writeMessage("Processed "+(count.getAndIncrement())+" of "+numberOfChildren+" objects",moduleName);

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            MIA.log.writeError(e);
        }
    }

    public static void linkBySurfaceProximity(ObjCollection parentObjects, ObjCollection childObjects, boolean linkInSameFrame, double linkingDistance, String insideOutsideMode, int nThreads) {
        String moduleName = RelateObjects.class.getSimpleName();
        String measurementNamePx = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());

        AtomicInteger count = new AtomicInteger(1);
        int numberOfChildren = childObjects.size();

        // Ensuring all parent objects have a calculated surface
        for (Obj parent:parentObjects.values()) parent.getSurface();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        for (Obj childObject:childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                    // Calculating the object spacing
                    double dist = childObject.getSurfaceSeparation(parentObject,true);

                    if (Math.abs(dist) < Math.abs(minDist) && Math.abs(dist) <= linkingDistance) {
                        minDist = dist;
                        minLink = parentObject;
                    }
                }

                // Applying the inside outside mode (doesn't apply for centroid-centroid linking)
                minDist = applyInsideOutsidePolicy(minDist,insideOutsideMode);

                // Adding measurements to the input object
                if (minLink != null) {
                    childObject.addParent(minLink);
                    minLink.addChild(childObject);
                    childObject.addMeasurement(new Measurement(measurementNamePx, minDist));
                    childObject.addMeasurement(new Measurement(measurementNameCal, minDist * dpp));
                } else {
                    childObject.addMeasurement(new Measurement(measurementNamePx, Double.NaN));
                    childObject.addMeasurement(new Measurement(measurementNameCal, Double.NaN));
                }
                writeMessage("Processed "+(count.getAndIncrement())+" of "+numberOfChildren+" objects",moduleName);
            };
            pool.submit(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            MIA.log.writeError(e);
        }
    }

    public static void linkByCentroidToSurfaceProximity(ObjCollection parentObjects, ObjCollection childObjects, boolean linkInSameFrame, double linkingDistance, String insideOutsideMode, int nThreads) {
        String moduleName = RelateObjects.class.getSimpleName();
        String measurementNamePx = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());

        AtomicInteger count = new AtomicInteger(1);
        int numberOfChildren = childObjects.size();

        // Ensuring all parent objects have a calculated surface
        for (Obj parent:parentObjects.values()) parent.getSurface();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        for (Obj childObject:childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                // Calculating the object spacing
                double childXCent = childObject.getXMean(true);
                double childYCent = childObject.getYMean(true);
                double childZCent = childObject.getZMean(true, true);
                double childZCentSlice = childObject.getZMean(true, false);

                Point<Integer> currentPoint = new Point<>((int) Math.round(childXCent), (int) Math.round(childYCent), (int) childZCentSlice);

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT()) continue;

                    boolean isInside = false;
                    Iterator<Point<Double>> iterator = parentObject.getSurface().getCalibratedIterator(true,true);
                    while (iterator.hasNext()) {
                        Point<Double> point = iterator.next();
                        double xDist = childXCent - point.getX();
                        double yDist = childYCent - point.getY();
                        double zDist = childZCent - point.getZ();
                        double dist = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
                        if (dist < Math.abs(minDist) && dist <= linkingDistance) {
                            minDist = dist;
                            minLink = parentObject;
                            isInside = parentObject.getCoordinateSet().contains(currentPoint);
                        }
                    }

                    // If this point is inside the parent the distance should be negative
                    if (isInside) minDist = -minDist;

                }

                // If using centroid to surface proximity and inside only, calculate the fractional distance
                if (minLink != null) calculateFractionalDistance(childObject,minLink,minDist);

                // Applying the inside outside mode (doesn't apply for centroid-centroid linking)
                minDist = applyInsideOutsidePolicy(minDist,insideOutsideMode);

                // Adding measurements to the input object
                if (minLink != null) {
                    childObject.addParent(minLink);
                    minLink.addChild(childObject);

                    childObject.addMeasurement(new Measurement(measurementNamePx, minDist));
                    childObject.addMeasurement(new Measurement(measurementNameCal, minDist * dpp));

                } else {
                    childObject.addMeasurement(new Measurement(measurementNamePx, Double.NaN));
                    childObject.addMeasurement(new Measurement(measurementNameCal, Double.NaN));
                }

                writeMessage("Processed "+(count.getAndIncrement())+" of "+numberOfChildren+" objects",moduleName);
            };
            pool.submit(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            MIA.log.writeError(e);
        }
    }

    public static void calculateFractionalDistance(Obj childObject, Obj parentObject, double minDist) {
        // Calculating the furthest distance to the edge
        if (parentObject.getMeasurement("MAX_DIST") == null) {
            // Creating an image for the parent object
            Image parentImage = parentObject.convertObjToImage("Parent");
            InvertIntensity.process(parentImage.getImagePlus());

            ImagePlus distIpl = DistanceMap.getDistanceMap(parentImage.getImagePlus(),true);

            Image projectedImage = ProjectImage.projectImageInZ(new Image("Dist", distIpl), "Projected", ProjectImage.ProjectionModes.MAX);
            double maxDist = projectedImage.getImagePlus().getStatistics().max;

            parentObject.addMeasurement(new Measurement("MAX_DIST",maxDist));

        }

        // Adding measurement
        double maxDist = parentObject.getMeasurement("MAX_DIST").getValue();
        double frac = Math.abs(minDist/maxDist);
        String measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC, parentObject.getName());
        childObject.addMeasurement(new Measurement(measurementName, frac));

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

        long nCombined = parentObjects.size()*childObjects.size();
        long count = 0;
        String overlapMeasurementName = getFullName(Measurements.OVERLAP_PC,parentObjects.getName());

        if (nCombined == 0) return;

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
                    if (!parentObject.contains(centroid)) continue;

                }

                // Calculates the percentage overlap
                double nTotal = (double) childObject.size();
                double nOverlap = (double) parentObject.getOverlap(childObject);
                double overlap  = (nOverlap/nTotal)*100;

                // Testing the minimum overlap requirement
                if (overlap == 0 || overlap < minOverlap) continue;

                // If the tests are successful, addRef the link.  If the child has already been linked, but with a smaller
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

            writeMessage("Compared "+Math.floorDiv(100*childObjects.size()*++count,nCombined)+"% of pairs");

        }
    }

    public static double applyInsideOutsidePolicy(double minDist, String insideOutsideMode) {
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

    static void applyLinkMeasurements(ObjCollection parentObjects, ObjCollection childObjects) {
        String parentMeasurementName = getFullName(Measurements.WAS_LINKED,childObjects.getName());
        String childMeasurementName = getFullName(Measurements.WAS_LINKED,parentObjects.getName());

        for (Obj parentObject:parentObjects.values()) {
            if (parentObject.getChildren(childObjects.getName()).size() == 0) {
                parentObject.addMeasurement(new Measurement(parentMeasurementName,0));
            } else {
                parentObject.addMeasurement(new Measurement(parentMeasurementName,1));
            }
        }

        for (Obj childObject:childObjects.values()) {
            if (childObject.getParent(parentObjects.getName()) == null) {
                childObject.addMeasurement(new Measurement(childMeasurementName,0));
            } else {
                childObject.addMeasurement(new Measurement(childMeasurementName,1));
            }
        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_RELATIONSHIPS;
    }

    @Override
    protected boolean process(Workspace workspace) {
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
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE);
        double minOverlap = parameters.getValue(MINIMUM_PERCENTAGE_OVERLAP);
        boolean centroidOverlap = parameters.getValue(REQUIRE_CENTROID_OVERLAP);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (!limitLinking) linkingDistance = Double.MAX_VALUE;

        int nThreads = multithread ? Prefs.getThreads() : 1;

        // Removing previous relationships
        parentObjects.removeChildren(childObjectName);
        childObjects.removeParents(parentObjectName);

        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                linkMatchingIDs(parentObjects,childObjects);
                break;

            case RelateModes.PROXIMITY:
                switch (referencePoint) {
                    case ReferencePoints.CENTROID:
                        linkByCentroidProximity(parentObjects,childObjects,linkInSameFrame,linkingDistance,nThreads);
                        break;

                    case ReferencePoints.SURFACE:
                        linkBySurfaceProximity(parentObjects,childObjects,linkInSameFrame,linkingDistance,insideOutsideMode,nThreads);
                        break;

                    case ReferencePoints.CENTROID_TO_SURFACE:
                        linkByCentroidToSurfaceProximity(parentObjects,childObjects,linkInSameFrame,linkingDistance,insideOutsideMode,nThreads);
                        break;

                }
                break;

            case RelateModes.PROXIMITY_TO_CHILDREN:
                proximityToChildren(parentObjects,childObjects);
                break;

            case RelateModes.SPATIAL_OVERLAP:
                spatialOverlap(parentObjects,childObjects,minOverlap,centroidOverlap,linkInSameFrame);
                break;

        }

        applyLinkMeasurements(parentObjects,childObjects);

        if (showOutput) {
            childObjects.showMeasurements(this,modules);
            parentObjects.showMeasurements(this,modules);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new InputObjectsP(CHILD_OBJECTS, this));

        parameters.add(new ParamSeparatorP(RELATIONSHIP_SEPARATOR,this));
        parameters.add(new ChoiceP(RELATE_MODE, this,RelateObjects.RelateModes.MATCHING_IDS,RelateObjects.RelateModes.ALL));
        parameters.add(new ChoiceP(REFERENCE_POINT,this,RelateObjects.ReferencePoints.CENTROID,RelateObjects.ReferencePoints.ALL));
        parameters.add(new ChildObjectsP(TEST_CHILD_OBJECTS,this));
        parameters.add(new BooleanP(LIMIT_LINKING_BY_DISTANCE,this,false));
        parameters.add(new DoubleP(LINKING_DISTANCE,this,1.0));
        parameters.add(new ChoiceP(INSIDE_OUTSIDE_MODE,this,RelateObjects.InsideOutsideModes.INSIDE_AND_OUTSIDE,RelateObjects.InsideOutsideModes.ALL));
        parameters.add(new DoubleP(MINIMUM_PERCENTAGE_OVERLAP,this,0d,"Percentage of total child volume overlapping with the parent object."));
        parameters.add(new BooleanP(REQUIRE_CENTROID_OVERLAP,this,true));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME,this,true));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
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
                ((ChildObjectsP) parameters.getParameter(TEST_CHILD_OBJECTS)).setParentObjectsName(parentObjectNames);

                break;

            case RelateModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_PERCENTAGE_OVERLAP));
                returnedParameters.add(parameters.getParameter(REQUIRE_CENTROID_OVERLAP));
                break;
        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);

        if (parentObjectName == null || childObjectsName == null) return returnedRefs;

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_POINT)) {
                    case ReferencePoints.CENTROID:
                        String measurementName = getFullName(Measurements.DIST_CENTROID_PX,parentObjectName);
                        ObjMeasurementRef distCentPx = objectMeasurementRefs.getOrPut(measurementName);
                        distCentPx.setDescription("Distance between the centroid of this object and that of the closest \""
                                + parentObjectName+"\"object.  Measured in pixel units.");
                        distCentPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentPx);

                        measurementName = getFullName(Measurements.DIST_CENTROID_CAL,parentObjectName);
                        ObjMeasurementRef distCentCal = objectMeasurementRefs.getOrPut(measurementName);
                        distCentCal.setDescription("Distance between the centroid of this object and that of the closest \""
                                + parentObjectName+"\"object.  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
                        distCentCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentCal);
                        break;

                    case ReferencePoints.SURFACE:
                        measurementName = getFullName(Measurements.DIST_SURFACE_PX,parentObjectName);
                        ObjMeasurementRef distSurfPx = objectMeasurementRefs.getOrPut(measurementName);
                        distSurfPx.setDescription("Shortest distance between the surface of this object and that of the closest \""
                                + parentObjectName+"\" object.  Negative values indicate this object is inside the relevant \""
                                +parentObjectName+"\" object. Measured in pixel units.");
                        distSurfPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distSurfPx);

                        measurementName = getFullName(Measurements.DIST_SURFACE_CAL,parentObjectName);
                        ObjMeasurementRef distSurfCal = objectMeasurementRefs.getOrPut(measurementName);
                        distSurfCal.setDescription("Shortest distance between the surface of this object and that of the closest \""
                                + parentObjectName+"\" object.  Negative values indicate this object is inside the relevant \""
                                +parentObjectName+"\" object. Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");
                        distSurfCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distSurfCal);
                        break;

                    case ReferencePoints.CENTROID_TO_SURFACE:
                        measurementName = getFullName(Measurements.DIST_CENT_SURF_PX,parentObjectName);
                        ObjMeasurementRef distCentSurfPx = objectMeasurementRefs.getOrPut(measurementName);
                        distCentSurfPx.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                                "closest \""+ parentObjectName+"\" object.  Negative values indicate this object is inside the " +
                                "relevant \""+parentObjectName+"\" object. Measured in pixel units.");
                        distCentSurfPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentSurfPx);

                        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL,parentObjectName);
                        ObjMeasurementRef distCentSurfCal = objectMeasurementRefs.getOrPut(measurementName);
                        distCentSurfCal.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                                "closest \""+ parentObjectName+"\" object.  Negative values indicate this object is inside the " +
                                "relevant \""+parentObjectName+"\" object. Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                                "units.");
                        distCentSurfCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentSurfCal);

                        if (parameters.getValue(INSIDE_OUTSIDE_MODE).equals(InsideOutsideModes.INSIDE_ONLY)) {
                            measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC,parentObjectName);
                            ObjMeasurementRef distCentSurfFrac = objectMeasurementRefs.getOrPut(measurementName);
                            distCentSurfFrac.setDescription("Shortest distance between the centroid of this object and the surface of the " +
                                    "closest \""+ parentObjectName+"\" object.  Calculated as a fraction of the furthest possible distance " +
                                    "to the \""+parentObjectName+"\" surface.");
                            distCentSurfFrac.setObjectsName(childObjectsName);
                            returnedRefs.add(distCentSurfFrac);
                        }
                        break;
                }
                break;

            case RelateModes.SPATIAL_OVERLAP:
                String measurementName = getFullName(Measurements.OVERLAP_PC,parentObjectName);
                ObjMeasurementRef overlapPercentage  = objectMeasurementRefs.getOrPut(measurementName);
                overlapPercentage.setDescription("Percentage of pixels that overlap with the \""+ parentObjectName+"\" object "+
                        "with which it has the largest overlap.");
                overlapPercentage.setObjectsName(childObjectsName);
                returnedRefs.add(overlapPercentage);
                break;
        }

        String measurementName = getFullName(Measurements.WAS_LINKED,parentObjectName);
        ObjMeasurementRef wasLinked = objectMeasurementRefs.getOrPut(measurementName);
        wasLinked.setDescription("Was this \""+childObjectsName+"\" child object linked with a \""+parentObjectName+"\" parent object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");
        wasLinked.setObjectsName(childObjectsName);
        returnedRefs.add(wasLinked);

        measurementName = getFullName(Measurements.WAS_LINKED,childObjectsName);
        wasLinked = objectMeasurementRefs.getOrPut(measurementName);
        wasLinked.setDescription("Was this \""+parentObjectName+"\" parent object linked with a \""+childObjectsName+"\" child object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");
        wasLinked.setObjectsName(parentObjectName);
        returnedRefs.add(wasLinked);

        return returnedRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        returnedRelationships.add(relationshipRefs.getOrPut(parameters.getValue(PARENT_OBJECTS),parameters.getValue(CHILD_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "";
    }
}
