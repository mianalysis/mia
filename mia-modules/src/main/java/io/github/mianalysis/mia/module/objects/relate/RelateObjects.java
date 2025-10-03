package io.github.mianalysis.mia.module.objects.relate;

import java.util.HashMap;
import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.configure.SetDisplayRange;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.images.transform.ProjectImage;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.imagej.LUTs;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.process.ColourFactory;

/**
 * Created by sc13967 on 04/05/2017.
 */

/**
 * Relate objects of two classes based on a variety of metrics (e.g. spatial
 * overlap or proximity). The assigned relationships are of the form
 * many-to-one, where many input "child" objects can be related to at most, one
 * "parent" object. Measurements associated with this relationship (e.g.
 * distance from child to parent surface) are stored as measurements of the
 * relevant child object.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RelateObjects extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String PARENT_OBJECTS = "Parent (larger) objects";
    public static final String CHILD_OBJECTS = "Child (smaller) objects";

    /**
    * 
    */
    public static final String RELATE_SEPARATOR = "Relation controls";

    /**
     * The metric by which parent and child objects will be related:<br>
     * <ul>
     * <li>"Matching IDs" Parents and children will be related if they have the same
     * ID number. Since object ID numbers are unique within an object collection
     * there will always be no more than one parent to a child.</li>
     * <li>"Proximity" Children are related to the spatially-closest object from the
     * parent collection. The exact distances used (e.g. centroid to centroid or
     * surface to surface) are controlled by the "Reference point" parameter.</li>
     * <li>"Spatial overlap" Children are related to the object from the parent
     * collection they have the greatest spatial overlap with. Spatial overlap is
     * defined as the number of coincident object coordinates.</li>
     * </ul>
     */
    public static final String RELATE_MODE = "Method to relate objects";

    /**
     * Controls the method used for determining proximity-based relationships:<br>
     * <ul>
     * <li>"Centroid" Distances are from child object centroids to parent object
     * centroids. These distances are always positive; increasing as the distance
     * between centroids increases.</li>
     * <li>"Child centroid to parent surface" Distances are from child object
     * centroids to the closest point on parent object surfaces. These distances
     * increase in magnitude the further from the parent surface a child centroid
     * is; however, they are assigned a positive value if the child is outside the
     * parent and a negative value if the child is inside the parent. For example, a
     * centroid 5px outside the object will be simply "5px", whereas a centroid 5px
     * from the surface, but contained within the parent object will be recorded as
     * "-5px".</li>
     * <li>"Surface" Distances are between the closest points on the child and
     * parent surfaces. These distances increase in magnitude the greater the
     * minimum parent-child surface distance is; however, they are assigned a
     * positive value if the closest child surface point is outside the parent and a
     * negative value if the closest child surface point is inside the parent. For
     * example, a closest child surface point 5px outside the object will be simply
     * "5px", whereas a closest child surface point 5px from the surface, but
     * contained within the parent object will be recorded as "-5px". Note: Any
     * instances where the child and parent surfaces overlap will be recorded as
     * "0px" distance.</li>
     * </ul>
     */
    public static final String REFERENCE_MODE = "Reference point";

    /**
     * When selected, objects will only be related if the distance between them (as
     * calculated by the "Reference point" metric) is less than or equal to the
     * distance defined by "Maximum linking distance (px)".
     */
    public static final String LIMIT_LINKING_BY_DISTANCE = "Limit linking by distance";
    public static final String LINKING_DISTANCE = "Maximum linking distance (px)";

    /**
     * When relating children to parent surfaces it's possible to only include
     * children inside, outside or on the edge of the parent.This parameter controls
     * which children are allowed to be related to the parents. Choices are: Inside
     * and outside (all distances), Inside only (distances less than 0), Inside and
     * on surface (distances less than or equal to 0), On surface only (distances
     * equal to 0), Outside and on surface (distances greater than or equal to 0),
     * Outside only (distances greater than 0).
     */
    public static final String INSIDE_OUTSIDE_MODE = "Inside/outside mode";

    /**
     * Percentage of total child volume overlapping with the parent object.
     */
    public static final String MINIMUM_PERCENTAGE_OVERLAP = "Minimum percentage overlap";

    /**
     * When selected, child objects are only related to a parent if their centroid
     * is inside the parent object (i.e. the child object centroid is coincident
     * with a parent object coordinate).
     */
    public static final String REQUIRE_CENTROID_OVERLAP = "Require centroid overlap";

    /**
     * When selected, child and parent objects must be in the same time frame for
     * them to be linked.
     */
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    /**
    * 
    */
    public static final String IGNORE_EDGES_XY = "Ignore XY edges";

    /**
     * 
     */
    public static final String IGNORE_EDGES_Z = "Ignore Z edges";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
     * When selected, any merged children and parents will be removed from their
     * respective object collections, combined into a single object (one merged
     * object per parent and associated children) and stored in a new object
     * collection.
     */
    public static final String MERGE_RELATED_OBJECTS = "Merge related objects";

    /**
     * If "Merge related objects" is selected, this is the name of the output
     * related objects collection that will be stored in the workspace.
     */
    public static final String RELATED_OBJECTS = "Output overlapping objects";

    public RelateObjects(Modules modules) {
        super("Relate objects", modules);
        deprecated = true;
    }

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

    }

    public static String getFullName(String measurement, String parentName) {
        return "RELATE_OBJ // " + measurement.replace("${PARENT}", parentName);
    }

    public void linkMatchingIDs(Objs parentObjects, Objs childObjects) {
        for (Obj parentObject : parentObjects.values()) {
            int ID = parentObject.getID();

            Obj childObject = childObjects.get(ID);

            if (childObject != null) {
                parentObject.addChild(childObject);
                childObject.addParent(parentObject);

            }
        }
    }

    /*
     * Iterates over each testObject, calculating getting the smallest distance to a
     * parentObject. If this is smaller than linkingDistance the link is assigned.
     */
    public void proximity(Objs parentObjects, Objs childObjects, WorkspaceI workspace, boolean ignoreEdgesXY,
            boolean ignoreEdgesZ) {
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        boolean limitLinking = parameters.getValue(LIMIT_LINKING_BY_DISTANCE, workspace);
        double linkingDistance = parameters.getValue(LINKING_DISTANCE, workspace);
        String insideOutsideMode = parameters.getValue(INSIDE_OUTSIDE_MODE, workspace);

        int iter = 1;
        int numberOfChildren = childObjects.size();

        for (Obj childObject : childObjects.values()) {
            double minDist = Double.MAX_VALUE;
            Obj minLink = null;

            for (Obj parentObject : parentObjects.values()) {
                if (linkInSameFrame & parentObject.getT() != childObject.getT())
                    continue;

                // Calculating the object spacing
                switch (referenceMode) {
                    case ReferenceModes.CENTROID:
                        double dist = childObject.getCentroidSeparation(parentObject, true, false);

                        if (dist < minDist) {
                            if (limitLinking && dist > linkingDistance)
                                continue;
                            minDist = dist;
                            minLink = parentObject;
                        }

                        break;

                    case ReferenceModes.SURFACE:
                        dist = childObject.getSurfaceSeparation(parentObject, true, false, ignoreEdgesXY, ignoreEdgesZ);

                        if (Math.abs(dist) < Math.abs(minDist)) {
                            if (limitLinking && Math.abs(dist) > linkingDistance)
                                continue;
                            minDist = dist;
                            minLink = parentObject;
                        }

                        break;

                    case ReferenceModes.CENTROID_TO_SURFACE:
                        double childXCent = childObject.getXMean(true);
                        double childYCent = childObject.getYMean(true);
                        double childZCentSlice = childObject.getZMean(true, false);

                        Point<Double> currentPoint = new Point<>(childXCent, childYCent, childZCentSlice);
                        dist = parentObject.getPointSurfaceSeparation(currentPoint, true, false, ignoreEdgesXY,
                                ignoreEdgesZ);

                        if (Math.abs(dist) < Math.abs(minDist)) {
                            if (limitLinking && Math.abs(dist) > linkingDistance)
                                continue;
                            minDist = dist;
                            minLink = parentObject;
                        }

                        break;

                }
            }

            // If using centroid to surface proximity and inside only, calculate the
            // fractional distance
            if (referenceMode.equals(ReferenceModes.CENTROID_TO_SURFACE)
                    && parameters.getValue(INSIDE_OUTSIDE_MODE, workspace).equals(InsideOutsideModes.INSIDE_ONLY)) {
                calculateFractionalDistance(childObject, minLink, minDist);
            }

            // Applying the inside outside mode (doesn't apply for centroid-centroid
            // linking)
            if (referenceMode.equals(ReferenceModes.CENTROID_TO_SURFACE)
                    || referenceMode.equals(ReferenceModes.SURFACE)) {
                if (!applyInsideOutsidePolicy(minDist, insideOutsideMode)) {
                    minDist = 0;
                    minLink = null;
                }
            }

            // Adding measurements to the input object
            applyMeasurements(childObject, parentObjects, minDist, minLink, workspace);

            writeProgressStatus(iter++, numberOfChildren, "objects");

        }
    }

    public void calculateFractionalDistance(Obj childObject, Obj parentObject, double minDist) {
        // Calculating the furthest distance to the edge
        if (parentObject.getMeasurement("MAX_DIST") == null) {
            // Creating an image for the parent object
            ImageI parentImage = parentObject.getAsImage("Parent", false);
            ImageI distImage = DistanceMap.process(parentImage, "Distance", true,
                    DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false);
            ImageI projectedImage = ProjectImage.projectImageInZ(distImage, "Projected",
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

    public void applyMeasurements(Obj childObject, Objs parentObjects, double minDist, Obj minLink,
            WorkspaceI workspace) {
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);

        if (minLink != null) {
            double dpp = childObject.getDppXY();
            childObject.addParent(minLink);
            minLink.addChild(childObject);

            switch (referenceMode) {
                case ReferenceModes.CENTROID: {
                    String measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist));
                    measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                    break;
                }
                case ReferenceModes.SURFACE: {
                    String measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist));
                    measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                    break;
                }
                case ReferenceModes.CENTROID_TO_SURFACE: {
                    String measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist));
                    measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, minDist * dpp));

                    break;
                }
            }

        } else {
            switch (referenceMode) {
                case ReferenceModes.CENTROID: {
                    String measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                    measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                    break;
                }
                case ReferenceModes.SURFACE: {
                    String measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                    measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                    break;
                }
                case ReferenceModes.CENTROID_TO_SURFACE: {
                    String measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));
                    measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjects.getName());
                    childObject.addMeasurement(new Measurement(measurementName, Double.NaN));

                    break;
                }
            }
        }
    }

    public void spatialOverlap(Objs parentObjects, Objs childObjects, double minOverlap,
            boolean centroidOverlap, boolean linkInSameFrame) {

        long nCombined = parentObjects.size() * childObjects.size();
        long count = 0;
        String overlapMeasurementName = getFullName(Measurements.OVERLAP_PC, parentObjects.getName());

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
                if (overlap == 0 || overlap < minOverlap)
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

    public Objs mergeRelatedObjects(Objs parentObjects, Objs childObjects,
            String relatedObjectsName) {
        Obj exampleParent = parentObjects.getFirst();
        Objs relatedObjects = new Objs(relatedObjectsName, parentObjects);

        if (exampleParent == null)
            return relatedObjects;

        Iterator<Obj> parentIterator = parentObjects.values().iterator();
        while (parentIterator.hasNext()) {
            Obj parentObj = parentIterator.next();

            // Collecting all children for this parent. If none are present, skip to the
            // next parent
            Objs currChildObjects = parentObj.getChildren(childObjects.getName());
            if (currChildObjects.size() == 0)
                continue;

            // Creating a new Obj and assigning pixels from the parent and all children
            Obj relatedObject = relatedObjects.createAndAddNewObject(exampleParent.getCoordinateSetFactory());
            relatedObject.setT(parentObj.getT());

            for (Obj childObject : currChildObjects.values()) {
                // Transferring points from the child object to the new object
                relatedObject.getCoordinateSet().addAll(childObject.getCoordinateSet());

                // Removing the child object from its original collection
                childObjects.values().remove(childObject);

            }

            // Transferring points from the parent object to the new object
            relatedObject.getCoordinateSet().addAll(parentObj.getCoordinateSet());

            // Removing the parent object from its original collection
            parentIterator.remove();

        }

        return relatedObjects;

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Relate objects of two classes based on a variety of metrics (e.g. spatial overlap or proximity).  The assigned relationships are of the form many-to-one, where many input \"child\" objects can be related to at most, one \"parent\" object.  Measurements associated with this relationship (e.g. distance from child to parent surface) are stored as measurements of the relevant child object.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String parentObjectName = parameters.getValue(PARENT_OBJECTS, workspace);
        Objs parentObjects = workspace.getObjects(parentObjectName);

        String childObjectName = parameters.getValue(CHILD_OBJECTS, workspace);
        Objs childObjects = workspace.getObjects(childObjectName);

        // Getting parameters
        String relateMode = parameters.getValue(RELATE_MODE, workspace);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);
        double minOverlap = parameters.getValue(MINIMUM_PERCENTAGE_OVERLAP, workspace);
        boolean centroidOverlap = parameters.getValue(REQUIRE_CENTROID_OVERLAP, workspace);
        boolean ignoreEdgesXY = parameters.getValue(IGNORE_EDGES_XY, workspace);
        boolean ignoreEdgesZ = parameters.getValue(IGNORE_EDGES_Z, workspace);
        boolean mergeRelatedObjects = parameters.getValue(MERGE_RELATED_OBJECTS, workspace);
        String relatedObjectsName = parameters.getValue(RELATED_OBJECTS, workspace);

        // Removing previous relationships
        parentObjects.removeChildren(childObjectName);
        childObjects.removeParents(parentObjectName);

        switch (relateMode) {
            case RelateModes.MATCHING_IDS:
                writeStatus("Relating objects by matching ID numbers");
                linkMatchingIDs(parentObjects, childObjects);
                break;

            case RelateModes.PROXIMITY:
                writeStatus("Relating objects by proximity");
                proximity(parentObjects, childObjects, workspace, ignoreEdgesXY, ignoreEdgesZ);
                break;

            case RelateModes.SPATIAL_OVERLAP:
                writeStatus("Relating objects by spatial overlap");
                spatialOverlap(parentObjects, childObjects, minOverlap, centroidOverlap, linkInSameFrame);
                break;

        }

        if (mergeRelatedObjects) {
            Objs relatedObjects = mergeRelatedObjects(parentObjects, childObjects, relatedObjectsName);
            if (relatedObjects != null)
                workspace.addObjects(relatedObjects);

        }

        if (showOutput) {
            int maxID = 0;
            for (int ID : parentObjects.keySet())
                maxID = Math.max(ID, maxID);

            ImageI parentImage = parentObjects.convertToImageIDColours();
            SetDisplayRange.setDisplayRangeManual(parentImage, new double[] { 0, maxID });
            parentImage.showWithNormalisation(false);

            HashMap<Integer, Float> hues2 = ColourFactory.getParentIDHues(childObjects, parentObjectName, false);
            ImageI childImage = childObjects.convertToImage(childObjectName, hues2, 32, false);
            SetDisplayRange.setDisplayRangeManual(childImage, new double[] { 0, maxID });
            childImage.show(childObjectName, LUTs.Random(true, false), false, ImageI.DisplayModes.COLOUR, null);

        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new InputObjectsP(CHILD_OBJECTS, this));

        parameters.add(new SeparatorP(RELATE_SEPARATOR, this));
        parameters.add(new ChoiceP(RELATE_MODE, this, RelateModes.MATCHING_IDS, RelateModes.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new BooleanP(LIMIT_LINKING_BY_DISTANCE, this, false));
        parameters.add(new DoubleP(LINKING_DISTANCE, this, 1.0));
        parameters.add(
                new ChoiceP(INSIDE_OUTSIDE_MODE, this, InsideOutsideModes.INSIDE_AND_OUTSIDE, InsideOutsideModes.ALL));
        parameters.add(new DoubleP(MINIMUM_PERCENTAGE_OVERLAP, this, 0d));
        parameters.add(new BooleanP(REQUIRE_CENTROID_OVERLAP, this, true));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, true));
        parameters.add(new BooleanP(IGNORE_EDGES_XY, this, false));
        parameters.add(new BooleanP(IGNORE_EDGES_Z, this, false));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(MERGE_RELATED_OBJECTS, this, false));
        parameters.add(new OutputObjectsP(RELATED_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));

        returnedParameters.add(parameters.getParameter(RELATE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RELATE_MODE));

        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        switch ((String) parameters.getValue(RELATE_MODE, workspace)) {
            case RelateModes.PROXIMITY:
                returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
                returnedParameters.add(parameters.getParameter(LIMIT_LINKING_BY_DISTANCE));
                if ((boolean) parameters.getValue(LIMIT_LINKING_BY_DISTANCE, workspace)) {
                    returnedParameters.add(parameters.getParameter(LINKING_DISTANCE));
                }

                if (referenceMode.equals(ReferenceModes.CENTROID_TO_SURFACE)
                        || referenceMode.equals(ReferenceModes.SURFACE)) {
                    returnedParameters.add(parameters.getParameter(INSIDE_OUTSIDE_MODE));
                    returnedParameters.add(parameters.getParameter(IGNORE_EDGES_XY));
                    returnedParameters.add(parameters.getParameter(IGNORE_EDGES_Z));
                }

                break;

            case RelateModes.SPATIAL_OVERLAP:
                returnedParameters.add(parameters.getParameter(MINIMUM_PERCENTAGE_OVERLAP));
                returnedParameters.add(parameters.getParameter(REQUIRE_CENTROID_OVERLAP));
                break;
        }

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MERGE_RELATED_OBJECTS));
        if ((boolean) parameters.getValue(MERGE_RELATED_OBJECTS, workspace)) {
            returnedParameters.add(parameters.getParameter(RELATED_OBJECTS));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String childObjectsName = parameters.getValue(CHILD_OBJECTS, workspace);
        String parentObjectName = parameters.getValue(PARENT_OBJECTS, workspace);

        if (parentObjectName == null || childObjectsName == null)
            return returnedRefs;

        String measurementName = getFullName(Measurements.DIST_SURFACE_PX, parentObjectName);
        ObjMeasurementRef distSurfPx = objectMeasurementRefs.getOrPut(measurementName);
        distSurfPx.setDescription("Shortest distance between the surface of this object and that of the closest \""
                + parentObjectName + "\" object.  Negative values indicate this object is inside the relevant \""
                + parentObjectName + "\" object. Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_SURFACE_CAL, parentObjectName);
        ObjMeasurementRef distSurfCal = objectMeasurementRefs.getOrPut(measurementName);
        distSurfCal.setDescription("Shortest distance between the surface of this object and that of the closest \""
                + parentObjectName + "\" object.  Negative values indicate this object is inside the relevant \""
                + parentObjectName + "\" object. Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol()
                + ") units.");

        measurementName = getFullName(Measurements.DIST_CENTROID_PX, parentObjectName);
        ObjMeasurementRef distCentPx = objectMeasurementRefs.getOrPut(measurementName);
        distCentPx.setDescription("Distance between the centroid of this object and that of the closest \""
                + parentObjectName + "\"object.  Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_CENTROID_CAL, parentObjectName);
        ObjMeasurementRef distCentCal = objectMeasurementRefs.getOrPut(measurementName);
        distCentCal.setDescription(
                "Distance between the centroid of this object and that of the closest \"" + parentObjectName
                        + "\"object.  Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_PX, parentObjectName);
        ObjMeasurementRef distCentSurfPx = objectMeasurementRefs.getOrPut(measurementName);
        distCentSurfPx.setDescription("Shortest distance between the centroid of this object and the surface of the "
                + "closest \"" + parentObjectName + "\" object.  Negative values indicate this object is inside the "
                + "relevant \"" + parentObjectName + "\" object. Measured in pixel units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_CAL, parentObjectName);
        ObjMeasurementRef distCentSurfCal = objectMeasurementRefs.getOrPut(measurementName);
        distCentSurfCal.setDescription("Shortest distance between the centroid of this object and the surface of the "
                + "closest \"" + parentObjectName + "\" object.  Negative values indicate this object is inside the "
                + "relevant \"" + parentObjectName + "\" object. Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");

        measurementName = getFullName(Measurements.DIST_CENT_SURF_FRAC, parentObjectName);
        ObjMeasurementRef distCentSurfFrac = objectMeasurementRefs.getOrPut(measurementName);
        distCentSurfFrac.setDescription(
                "Shortest distance between the centroid of this object and the surface of the " + "closest \""
                        + parentObjectName + "\" object.  Calculated as a fraction of the furthest possible distance "
                        + "to the \"" + parentObjectName + "\" surface.");

        measurementName = getFullName(Measurements.OVERLAP_PC, parentObjectName);
        ObjMeasurementRef overlapPercentage = objectMeasurementRefs.getOrPut(measurementName);
        overlapPercentage.setDescription("Percentage of pixels that overlap with the \"" + parentObjectName
                + "\" object " + "with which it has the largest overlap.");

        distSurfPx.setObjectsName(childObjectsName);
        distCentPx.setObjectsName(childObjectsName);
        distSurfCal.setObjectsName(childObjectsName);
        distCentCal.setObjectsName(childObjectsName);
        distCentSurfPx.setObjectsName(childObjectsName);
        distCentSurfCal.setObjectsName(childObjectsName);
        distCentSurfFrac.setObjectsName(childObjectsName);
        overlapPercentage.setObjectsName(childObjectsName);

        switch ((String) parameters.getValue(RELATE_MODE, workspace)) {
            case RelateModes.PROXIMITY:
                switch ((String) parameters.getValue(REFERENCE_MODE, workspace)) {
                    case ReferenceModes.CENTROID:
                        returnedRefs.add(distCentPx);
                        returnedRefs.add(distCentCal);
                        break;

                    case ReferenceModes.SURFACE:
                        returnedRefs.add(distSurfPx);
                        returnedRefs.add(distSurfCal);
                        break;

                    case ReferenceModes.CENTROID_TO_SURFACE:
                        returnedRefs.add(distCentSurfPx);
                        returnedRefs.add(distCentSurfCal);

                        if (parameters.getValue(INSIDE_OUTSIDE_MODE, workspace)
                                .equals(InsideOutsideModes.INSIDE_ONLY)) {
                            returnedRefs.add(distCentSurfFrac);
                        }
                        break;
                }
                break;

            case RelateModes.SPATIAL_OVERLAP:
                returnedRefs.add(overlapPercentage);
                break;
        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        WorkspaceI workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        returnedRelationships
                .add(parentChildRefs.getOrPut(parameters.getValue(PARENT_OBJECTS, workspace),
                        parameters.getValue(CHILD_OBJECTS, workspace)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
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

        parameters.get(MINIMUM_PERCENTAGE_OVERLAP)
                .setDescription("Percentage of total child volume overlapping with the parent object.");

        parameters.get(REQUIRE_CENTROID_OVERLAP).setDescription(
                "When selected, child objects are only related to a parent if their centroid is inside the parent object (i.e. the child object centroid is coincident with a parent object coordinate).");

        parameters.get(LINK_IN_SAME_FRAME).setDescription(
                "When selected, child and parent objects must be in the same time frame for them to be linked.");

        parameters.get(MERGE_RELATED_OBJECTS).setDescription(
                "When selected, any merged children and parents will be removed from their respective object collections, combined into a single object (one merged object per parent and associated children) and stored in a new object collection.");

        parameters.get(RELATED_OBJECTS).setDescription("If \"" + MERGE_RELATED_OBJECTS
                + "\" is selected, this is the name of the output related objects collection that will be stored in the workspace.");

    }
}
