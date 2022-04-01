package io.github.mianalysis.mia.module.objects.relate;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.images.transform.ProjectImage;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.sjcross.common.object.Point;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class RelateManyToOne extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String PARENT_OBJECTS = "Parent (larger) objects";
    public static final String CHILD_OBJECTS = "Child (smaller) objects";

    public static final String RELATIONSHIP_SEPARATOR = "Relationship settings";
    public static final String RELATE_MODE = "Method to relate objects";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String LIMIT_LINKING_BY_DISTANCE = "Limit linking by distance";
    public static final String LINKING_DISTANCE = "Maximum linking distance (px)";
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";
    public static final String MINIMUM_OVERLAP = "Minimum overlap (%)";
    public static final String REQUIRE_CENTROID_OVERLAP = "Require centroid overlap";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";
    public static final String CALCULATE_FRACTIONAL_DISTANCE = "Calculate fractional distance";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface RelateModes {
        String MATCHING_IDS = "Matching IDs";
        String PROXIMITY = "Proximity";
        String SPATIAL_OVERLAP = "Spatial overlap";

        String[] ALL = new String[] { MATCHING_IDS, PROXIMITY, SPATIAL_OVERLAP };

    }

    public interface ReferenceModes {
        String CENTROID = "Centroid";
        String SURFACE = "Surface";
        String CENTROID_TO_SURFACE = "Child centroid to parent surface";

        String[] ALL = new String[] { CENTROID, SURFACE, CENTROID_TO_SURFACE };

    }

    public interface InsideOutsideModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside (all distances)";
        String INSIDE_ONLY = "Inside only (distances < 0)";
        String INSIDE_AND_ON_SURFACE = "Inside and on surface (distances <= 0)";
        String ON_SURFACE_ONLY = "On surface only (distances = 0)";
        String OUTSIDE_AND_ON_SURFACE = "Outside and on surface (distances >= 0)";
        String OUTSIDE_ONLY = "Outside only (distances > 0)";

        String[] ALL = new String[] { INSIDE_AND_OUTSIDE, INSIDE_ONLY, INSIDE_AND_ON_SURFACE, ON_SURFACE_ONLY,
                OUTSIDE_AND_ON_SURFACE, OUTSIDE_ONLY };

    }

    public interface Measurements {
        String DIST_SURFACE_PX = "DIST_TO_${PARENT}_SURF_(PX)";
        String DIST_CENTROID_PX = "DIST_TO_${PARENT}_CENT_(PX)";
        String DIST_SURFACE_CAL = "DIST_TO_${PARENT}_SURF_(${SCAL})";
        String DIST_CENTROID_CAL = "DIST_TO_${PARENT}_CENT_(${SCAL})";
        String DIST_CENT_SURF_PX = "DIST_FROM_CENT_TO_${PARENT}_SURF_(PX)";
        String DIST_CENT_SURF_CAL = "DIST_FROM_CENT_TO_${PARENT}_SURF_(${SCAL})";
        String DIST_CENT_SURF_FRAC = "DIST_FROM_CENT_TO_${PARENT}_SURF_(FRAC)";
        String OVERLAP_PC = "OVERLAP_WITH_${PARENT}_PERCENTAGE";
        String WAS_LINKED = "WAS_LINKED_${PARENT}";

    }

    public RelateManyToOne(Modules modules) {
        super("Relate many-to-one", modules);
    }

    public static String getFullName(String measurement, String parentName) {
        return "RELATE_MANY_TO_ONE // " + measurement.replace("${PARENT}", parentName);
    }

    public static void linkMatchingIDs(Objs parentObjects, Objs childObjects) {
        for (Obj parentObject : parentObjects.values()) {
            int ID = parentObject.getID();

            Obj childObject = childObjects.get(ID);

            if (childObject != null) {
                parentObject.addChild(childObject);
                childObject.addParent(parentObject);

            }
        }
    }

    public static void linkByCentroidProximity(Objs parentObjects, Objs childObjects,
            boolean linkInSameFrame, double linkingDistance, int nThreads) {
        String moduleName = RelateObjects.class.getSimpleName();
        String measurementNamePx = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());

        AtomicInteger count = new AtomicInteger(1);
        int numberOfChildren = childObjects.size();

        // Ensuring all parent objects have a calculated centroid
        for (Obj parent : parentObjects.values())
            parent.getMeanCentroid();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        for (Obj childObject : childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT())
                        continue;

                    double dist = childObject.getCentroidSeparation(parentObject, true);

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

                writeProgressStatus(count.getAndIncrement(), numberOfChildren, "objects", moduleName);

            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }
    }

    public static void linkBySurfaceProximity(Objs parentObjects, Objs childObjects,
            boolean linkInSameFrame, double linkingDistance, String insideOutsideMode, int nThreads) {
        String moduleName = RelateObjects.class.getSimpleName();
        String measurementNamePx = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());

        AtomicInteger count = new AtomicInteger(1);
        int numberOfChildren = childObjects.size();

        // Ensuring all parent objects have a calculated surface
        for (Obj parent : parentObjects.values())
            if (!parent.hasCalculatedSurface())
                parent.getCoordinateSet().calculateSurface(parent.is2D());

        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        for (Obj childObject : childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT())
                        continue;

                    // Calculating the object spacing
                    double dist = childObject.getSurfaceSeparation(parentObject, true);

                    if (Math.abs(dist) < Math.abs(minDist) && Math.abs(dist) <= linkingDistance) {
                        minDist = dist;
                        minLink = parentObject;
                    }
                }

                // Applying the inside outside mode
                if (!applyInsideOutsidePolicy(minDist, insideOutsideMode)) {
                    minDist = 0;
                    minLink = null;
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
                writeProgressStatus(count.getAndIncrement(), numberOfChildren, "objects", moduleName);
            };
            pool.submit(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }
    }

    public static void linkByCentroidToSurfaceProximity(Objs parentObjects, Objs childObjects,
            boolean linkInSameFrame, double linkingDistance, String insideOutsideMode, boolean calcFrac, int nThreads) {
        String moduleName = new RelateManyToOne(null).getName();
        String measurementNamePx = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
        String measurementNameCal = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());

        // Ensuring all parent objects have a calculated surface
        writeStatus("Initialising object surfaces", moduleName);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        final AtomicInteger count = new AtomicInteger(1);
        int numberOfParents = parentObjects.size();
        int numberOfChildren = childObjects.size();

        for (Obj parent : parentObjects.values()) {
            if (!parent.hasCalculatedSurface()) {
                Runnable task = () -> {
                    parent.getCoordinateSet().calculateSurface(parent.is2D());
                    writeProgressStatus(count.getAndIncrement(), numberOfParents, "objects", moduleName);
                };
                pool.submit(task);
            }
        }
        pool.shutdown();

        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        count.set(1);

        writeStatus("Processing objects", moduleName);
        for (Obj childObject : childObjects.values()) {
            Runnable task = () -> {
                double minDist = Double.MAX_VALUE;
                Obj minLink = null;
                double dpp = childObject.getDppXY();

                Point<Double> childCentPx = childObject.getMeanCentroid(true, false);

                for (Obj parentObject : parentObjects.values()) {
                    if (linkInSameFrame & parentObject.getT() != childObject.getT())
                        continue;

                    double dist = parentObject.getPointSurfaceSeparation(childCentPx, true);

                    if (Math.abs(dist) < Math.abs(minDist) && Math.abs(dist) <= linkingDistance) {
                        minDist = dist;
                        minLink = parentObject;
                    }
                }

                // If using centroid to surface proximity and inside only, calculate the
                // fractional distance
                if (minLink != null && calcFrac)
                    calculateFractionalDistance(childObject, minLink, minDist);

                // Applying the inside outside mode
                if (!applyInsideOutsidePolicy(minDist, insideOutsideMode)) {
                    minDist = 0;
                    minLink = null;
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

                writeProgressStatus(count.getAndIncrement(), numberOfChildren, "objects", moduleName);

            };
            pool.submit(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }
    }

    public static void calculateFractionalDistance(Obj childObject, Obj parentObject, double minDist) {
        // Calculating the furthest distance to the edge
        if (parentObject.getMeasurement("MAX_DIST") == null) {
            // Creating an image for the parent object
            Image parentImage = parentObject.getAsImage("Parent", false);
            Image distImage = DistanceMap.process(parentImage, "Distance", true,
                    DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false);

            Image projectedImage = ProjectImage.projectImageInZ(distImage, "Projected",
                    ProjectImage.ProjectionModes.MAX);
            double maxDist = projectedImage.getImagePlus().getStatistics().max;

            parentObject.addMeasurement(new Measurement("MAX_DIST", maxDist));

        }

        // Adding measurement
        double maxDist = parentObject.getMeasurement("MAX_DIST").getValue();
        double frac = Math.abs(minDist / maxDist);
        String measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC, parentObject.getName());
        childObject.addMeasurement(new Measurement(measurementName, frac));

    }

    public void spatialOverlap(Objs parentObjects, Objs childObjects, double minOverlap,
            boolean centroidOverlap, boolean linkInSameFrame) {

        long nCombined = parentObjects.size() * childObjects.size();
        long count = 0;
        String overlapMeasurementName = getFullName(Measurements.OVERLAP_PC, parentObjects.getName());

        // If there are no objects to link, just set all children to no link
        if (nCombined == 0)
            return;

        // Runs through each child object against each parent object
        for (Obj parentObject : parentObjects.values()) {
            for (Obj childObject : childObjects.values()) {
                // Testing if the two objects are in the same frame (if this matters)
                if (linkInSameFrame && parentObject.getT() != childObject.getT())
                    continue;

                // If requiring the child centroid is overlapped with the parent object
                if (centroidOverlap) {
                    int xCent = (int) Math.round(childObject.getXMean(true));
                    int yCent = (int) Math.round(childObject.getYMean(true));
                    int zCent = (int) Math.round(childObject.getZMean(true, false)); // Relates to image location
                    Point<Integer> centroid = new Point<>(xCent, yCent, zCent);

                    // If the centroid doesn't overlap, skip this link
                    if (!parentObject.contains(centroid))
                        continue;

                }

                // Calculates the percentage overlap
                double nTotal = (double) childObject.size();
                double nOverlap = (double) parentObject.getOverlap(childObject);
                double overlap = (nOverlap / nTotal) * 100;

                // Testing the minimum overlap requirement
                if (overlap == 0 || overlap <= minOverlap)
                    continue;

                // If the tests are successful, addRef the link. If the child has already been
                // linked, but with a smaller
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
                Measurement measurement = new Measurement(getFullName(Measurements.OVERLAP_PC, parentObject.getName()));
                measurement.setValue(overlap);
                childObject.addMeasurement(measurement);

            }

            writeStatus("Compared " + Math.floorDiv(100 * childObjects.size() * ++count, nCombined) + "% of pairs");

        }
    }

    /**
     * Returns false if the inside/outside policy fails (i.e. the policy is "inside
     * only" and minDist is positive)
     */
    public static boolean applyInsideOutsidePolicy(double minDist, String insideOutsideMode) {
        switch (insideOutsideMode) {
            case InsideOutsideModes.INSIDE_ONLY:
                if (minDist >= 0)
                    return false;
                break;

            case InsideOutsideModes.INSIDE_AND_ON_SURFACE:
                if (minDist > 0)
                    return false;
                break;

            case InsideOutsideModes.ON_SURFACE_ONLY:
                if (minDist != 0)
                    return false;
                break;

            case InsideOutsideModes.OUTSIDE_AND_ON_SURFACE:
                if (minDist < 0)
                    return false;
                break;

            case InsideOutsideModes.OUTSIDE_ONLY:
                if (minDist <= 0)
                    return false;
                break;

        }

        return true;

    }

    static void applyLinkMeasurements(Objs parentObjects, Objs childObjects) {
        String parentMeasurementName = getFullName(Measurements.WAS_LINKED, childObjects.getName());
        String childMeasurementName = getFullName(Measurements.WAS_LINKED, parentObjects.getName());

        for (Obj parentObject : parentObjects.values()) {
            if (parentObject.getChildren(childObjects.getName()).size() == 0) {
                parentObject.addMeasurement(new Measurement(parentMeasurementName, 0));
            } else {
                parentObject.addMeasurement(new Measurement(parentMeasurementName, 1));
            }
        }

        for (Obj childObject : childObjects.values()) {
            if (childObject.getParent(parentObjects.getName()) == null) {
                childObject.addMeasurement(new Measurement(childMeasurementName, 0));
            } else {
                childObject.addMeasurement(new Measurement(childMeasurementName, 1));
            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getDescription() {
        return "Relate objects of two classes based on a variety of metrics (e.g. spatial overlap or proximity).  The assigned relationships are of the form many-to-one, where many input \"child\" objects can be related to at most, one \"parent\" object (see \""
                + new RelateManyToMany(null).getName() + "\" and \"" + new RelateOneToOne(null).getName()
                + "\" modules for alternatives).  Measurements associated with this relationship (e.g. distance from child to parent surface) are stored as measurements of the relevant child object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);
        Objs parentObjects = workspace.getObjects().get(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS);
        Objs childObjects = workspace.getObjects().get(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        boolean limitLinking = parameters.getValue(LIMIT_LINKING_BY_DISTANCE);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE);
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE);
        double minOverlap = parameters.getValue(MINIMUM_OVERLAP);
        boolean centroidOverlap = parameters.getValue(REQUIRE_CENTROID_OVERLAP);
        boolean calcFrac = parameters.getValue(CALCULATE_FRACTIONAL_DISTANCE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (!limitLinking)
            linkingDistance = Double.MAX_VALUE;

        int nThreads = multithread ? Prefs.getThreads() : 1;

        // Removing previous relationships
        parentObjects.removeChildren(childObjectName);
        childObjects.removeParents(parentObjectName);

        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                linkMatchingIDs(parentObjects, childObjects);
                break;

            case RelateModes.PROXIMITY:
                switch (referenceMode) {
                    case ReferenceModes.CENTROID:
                        linkByCentroidProximity(parentObjects, childObjects, linkInSameFrame, linkingDistance,
                                nThreads);
                        break;

                    case ReferenceModes.SURFACE:
                        linkBySurfaceProximity(parentObjects, childObjects, linkInSameFrame, linkingDistance,
                                insideOutsideMode, nThreads);
                        break;

                    case ReferenceModes.CENTROID_TO_SURFACE:
                        linkByCentroidToSurfaceProximity(parentObjects, childObjects, linkInSameFrame, linkingDistance,
                                insideOutsideMode, calcFrac, nThreads);
                        break;

                }
                break;

            case RelateModes.SPATIAL_OVERLAP:
                spatialOverlap(parentObjects, childObjects, minOverlap, centroidOverlap, linkInSameFrame);
                break;

        }

        applyLinkMeasurements(parentObjects, childObjects);

        if (showOutput) {
            childObjects.showMeasurements(this, modules);
            parentObjects.showMeasurements(this, modules);
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new InputObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(RELATIONSHIP_SEPARATOR, this));
        parameters.add(new ChoiceP(RELATE_MODE, this, RelateModes.MATCHING_IDS, RelateModes.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new BooleanP(LIMIT_LINKING_BY_DISTANCE, this, false));
        parameters.add(new DoubleP(LINKING_DISTANCE, this, 1.0));
        parameters.add(
                new ChoiceP(INSIDE_OUTSIDE_MODE, this, InsideOutsideModes.INSIDE_AND_OUTSIDE, InsideOutsideModes.ALL));
        parameters.add(new DoubleP(MINIMUM_OVERLAP, this, 0d));
        parameters.add(new BooleanP(REQUIRE_CENTROID_OVERLAP, this, true));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));
        parameters.add(new BooleanP(CALCULATE_FRACTIONAL_DISTANCE, this, true));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));

        returnedParameters.add(parameters.getParameter(RELATIONSHIP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATE_MODE));

        String referenceMode = parameters.getValue(REFERENCE_MODE);
        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
                returnedParameters.add(parameters.getParameter(LIMIT_LINKING_BY_DISTANCE));
                if ((boolean) parameters.getValue(LIMIT_LINKING_BY_DISTANCE))
                    returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));
                
                if (referenceMode.equals(ReferenceModes.CENTROID_TO_SURFACE)) {
                    returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));
                    returnedParameters.add(parameters.getParameter(CALCULATE_FRACTIONAL_DISTANCE));
                }

                break;

            case RelateModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_OVERLAP));
                returnedParameters.add(parameters.getParameter(REQUIRE_CENTROID_OVERLAP));
                break;
        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        String parentObjectName = parameters.getValue(PARENT_OBJECTS);

        if (parentObjectName == null || childObjectsName == null)
            return returnedRefs;

        switch ((String) parameters.getValue(RELATE_MODE)) {
            case RelateModes.PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_MODE)) {
                    case ReferenceModes.CENTROID:
                        String measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjectName);
                        ObjMeasurementRef distCentPx = objectMeasurementRefs.getOrPut(measurementName);
                        distCentPx.setDescription(
                                "Distance between the centroid of this object and that of the closest \""
                                        + parentObjectName + "\"object.  Measured in pixel units.");
                        distCentPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentPx);

                        measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjectName);
                        ObjMeasurementRef distCentCal = objectMeasurementRefs.getOrPut(measurementName);
                        distCentCal.setDescription(
                                "Distance between the centroid of this object and that of the closest \""
                                        + parentObjectName + "\"object.  Measured in calibrated ("
                                        + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
                        distCentCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentCal);
                        break;

                    case ReferenceModes.SURFACE:
                        measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjectName);
                        ObjMeasurementRef distSurfPx = objectMeasurementRefs.getOrPut(measurementName);
                        distSurfPx.setDescription(
                                "Shortest distance between the surface of this object and that of the closest \""
                                        + parentObjectName
                                        + "\" object.  Negative values indicate this object is inside the relevant \""
                                        + parentObjectName + "\" object. Measured in pixel units.");
                        distSurfPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distSurfPx);

                        measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjectName);
                        ObjMeasurementRef distSurfCal = objectMeasurementRefs.getOrPut(measurementName);
                        distSurfCal.setDescription(
                                "Shortest distance between the surface of this object and that of the closest \""
                                        + parentObjectName
                                        + "\" object.  Negative values indicate this object is inside the relevant \""
                                        + parentObjectName + "\" object. Measured in calibrated ("
                                        + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
                        distSurfCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distSurfCal);
                        break;

                    case ReferenceModes.CENTROID_TO_SURFACE:
                        measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjectName);
                        ObjMeasurementRef distCentSurfPx = objectMeasurementRefs.getOrPut(measurementName);
                        distCentSurfPx.setDescription(
                                "Shortest distance between the centroid of this object and the surface of the "
                                        + "closest \"" + parentObjectName
                                        + "\" object.  Negative values indicate this object is inside the "
                                        + "relevant \"" + parentObjectName + "\" object. Measured in pixel units.");
                        distCentSurfPx.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentSurfPx);

                        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjectName);
                        ObjMeasurementRef distCentSurfCal = objectMeasurementRefs.getOrPut(measurementName);
                        distCentSurfCal.setDescription(
                                "Shortest distance between the centroid of this object and the surface of the "
                                        + "closest \"" + parentObjectName
                                        + "\" object.  Negative values indicate this object is inside the "
                                        + "relevant \"" + parentObjectName + "\" object. Measured in calibrated ("
                                        + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
                        distCentSurfCal.setObjectsName(childObjectsName);
                        returnedRefs.add(distCentSurfCal);

                        if ((boolean) parameters.getValue(CALCULATE_FRACTIONAL_DISTANCE)) {
                            measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC, parentObjectName);
                            ObjMeasurementRef distCentSurfFrac = objectMeasurementRefs.getOrPut(measurementName);
                            distCentSurfFrac.setDescription(
                                    "Shortest distance between the centroid of this object and the surface of the "
                                            + "closest \"" + parentObjectName
                                            + "\" object.  Calculated as a fraction of the furthest possible distance "
                                            + "to the \"" + parentObjectName + "\" surface.");
                            distCentSurfFrac.setObjectsName(childObjectsName);
                            returnedRefs.add(distCentSurfFrac);
                        }
                        break;
                }
                break;

            case RelateModes.SPATIAL_OVERLAP:
                String measurementName = getFullName(Measurements.OVERLAP_PC, parentObjectName);
                ObjMeasurementRef overlapPercentage = objectMeasurementRefs.getOrPut(measurementName);
                overlapPercentage.setDescription("Percentage of pixels that overlap with the \"" + parentObjectName
                        + "\" object " + "with which it has the largest overlap.");
                overlapPercentage.setObjectsName(childObjectsName);
                returnedRefs.add(overlapPercentage);
                break;
        }

        String measurementName = getFullName(Measurements.WAS_LINKED, parentObjectName);
        ObjMeasurementRef wasLinked = objectMeasurementRefs.getOrPut(measurementName);
        wasLinked.setDescription("Was this \"" + childObjectsName + "\" child object linked with a \""
                + parentObjectName
                + "\" parent object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");
        wasLinked.setObjectsName(childObjectsName);
        returnedRefs.add(wasLinked);

        measurementName = getFullName(Measurements.WAS_LINKED, childObjectsName);
        wasLinked = objectMeasurementRefs.getOrPut(measurementName);
        wasLinked.setDescription("Was this \"" + parentObjectName + "\" parent object linked with a \""
                + childObjectsName
                + "\" child object.  Linked objects have a value of \"1\" and unlinked objects have a value of \"0\".");
        wasLinked.setObjectsName(parentObjectName);
        returnedRefs.add(wasLinked);

        return returnedRefs;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(PARENT_OBJECTS), parameters.getValue(CHILD_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }

    void addParameterDescriptions() {
        parameters.get(PARENT_OBJECTS).setDescription("Input reference objects.  The \"" + CHILD_OBJECTS
                + "\" will be related to these, with children assigned one parent based on, for example, closest proximity or maximum spatial overlap.  There's no guarantee of each parent object being assigned at least one child.");

        parameters.get(CHILD_OBJECTS).setDescription(
                "Objects to relate to the parents.  Each child will be assigned at most one parent.  There's no guarantee a child will be assigned any parent, especially when using options such as \""
                        + LIMIT_LINKING_BY_DISTANCE + "\".");

        parameters.get(RELATE_MODE)
                .setDescription("The metric by which parent and child objects will be related:<br><ul>"

                        + "<li>\"" + RelateModes.MATCHING_IDS
                        + "\" Parents and children will be related if they have the same ID number.  Since object ID numbers are unique within an object collection there will always be no more than one parent to a child.</li>"

                        + "<li>\"" + RelateModes.PROXIMITY
                        + "\" Children are related to the spatially-closest object from the parent collection.  The exact distances used (e.g. centroid to centroid or surface to surface) are controlled by the \""
                        + REFERENCE_MODE + "\" parameter.</li>"

                        + "<li>\"" + RelateModes.SPATIAL_OVERLAP
                        + "\" Children are related to the object from the parent collection they have the greatest spatial overlap with.  Spatial overlap is defined as the number of coincident object coordinates.</li></ul>");

        parameters.get(REFERENCE_MODE)
                .setDescription("Controls the method used for determining proximity-based relationships:<br><ul>"

                        + "<li>\"" + ReferenceModes.CENTROID
                        + "\" Distances are from child object centroids to parent object centroids.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + ReferenceModes.CENTROID_TO_SURFACE
                        + "\" Distances are from child object centroids to the closest point on parent object surfaces.  These distances increase in magnitude the further from the parent surface a child centroid is; however, they are assigned a positive value if the child is outside the parent and a negative value if the child is inside the parent.  For example, a centroid 5px outside the object will be simply \"5px\", whereas a centroid 5px from the surface, but contained within the parent object will be recorded as \"-5px\".</li>"

                        + "<li>\"" + ReferenceModes.SURFACE
                        + "\" Distances are between the closest points on the child and parent surfaces.  These distances increase in magnitude the greater the minimum parent-child surface distance is; however, they are assigned a positive value if the closest child surface point is outside the parent and a negative value if the closest child surface point is inside the parent.  For example, a closest child surface point 5px outside the object will be simply \"5px\", whereas a closest child surface point 5px from the surface, but contained within the parent object will be recorded as \"-5px\".  Note: Any instances where the child and parent surfaces overlap will be recorded as \"0px\" distance.</li></ul>");

        parameters.get(LIMIT_LINKING_BY_DISTANCE).setDescription(
                "When selected, objects will only be related if the distance between them (as calculated by the \""
                        + REFERENCE_MODE + "\" metric) is less than or equal to the distance defined by \""
                        + LINKING_DISTANCE + "\".");

        parameters.get(LINKING_DISTANCE).setDescription("If \"" + LIMIT_LINKING_BY_DISTANCE
                + "\" is selected, this is the maximum permitted distance between objects for them to be assigned a relationship.");

        parameters.get(INSIDE_OUTSIDE_MODE).setDescription(
                "When relating children to parent surfaces it's possible to only include children inside, outside or on the edge of the parent.This parameter controls which children are allowed to be related to the parents.  Choices are: "
                        + String.join(", ", InsideOutsideModes.ALL) + ".");

        parameters.get(MINIMUM_OVERLAP)
                .setDescription("Percentage of total child volume overlapping with the parent object.");

        parameters.get(REQUIRE_CENTROID_OVERLAP).setDescription(
                "When selected, child objects are only related to a parent if their centroid is inside the parent object (i.e. the child object centroid is coincident with a parent object coordinate).");

        parameters.get(LINK_IN_SAME_FRAME).setDescription(
                "When selected, child and parent objects must be in the same time frame for them to be linked.");

        parameters.get(CALCULATE_FRACTIONAL_DISTANCE).setDescription(
                "When selected, the fractional distance of the child object between the centre and surface of the parent is calculated.  This option is only available when relating children to the parent surface.  The calculation can be computationally intensive when dealing with many objects.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple object relationships simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
