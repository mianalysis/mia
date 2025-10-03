package io.github.mianalysis.mia.module.objects.filter;

import java.util.ArrayList;
import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Filter an object collection based on contact of each object with the image
 * edge. Contact is considered as a case where an object pixel is in the
 * outer-most row, column or slice of an image (e.g. x = 0 or y = max_value).
 * The maximum number of contact pixels before an object is removed can be set
 * to permit a degree of contact. Objects identified as being in contact with
 * the image edge can be removed from the input collection, moved to another
 * collection (and removed from the input collection) or simply counted (but
 * retained in the input collection). The number of objects failing the filter
 * can be stored as a metadata value. <br>
 * <br>
 * Image edge filters can be used when counting the number of objects in a field
 * of view - in this case, typically two adjacent edges are removed (e.g. bottom
 * and right) to prevent over-counting. Alternatively, removing objects on all
 * edges can be performed when measuring whole-object properties such as area or
 * volume to prevent under-measuring values.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterOnImageEdge extends AbstractObjectFilter {

    /**
    * 
    */
    public static final String FILTER_SEPARATOR = "Object filtering";

    /**
     * Maximum number of object pixels which can lie along any of the specified
     * edges without the object being removed. This provides tolerance for objects
     * which only just make contact with the image edge.
     */
    public static final String MAXIMUM_CONTACT = "Maximum permitted contact";

    /**
     * When selected, object pixels which make contact with the top of the image (y
     * = 0) will count towards the "Maximum permitted contact" limit. If not
     * selected, pixels along this edge will be ignored (i.e. contact won't lead to
     * object removal).
     */
    public static final String REMOVE_ON_TOP = "Remove on top";

    /**
     * When selected, object pixels which make contact with the left side of the
     * image (x = 0) will count towards the "Maximum permitted contact" limit. If
     * not selected, pixels along this edge will be ignored (i.e. contact won't lead
     * to object removal).
     */
    public static final String REMOVE_ON_LEFT = "Remove on left";

    /**
     * When selected, object pixels which make contact with the bottom of the image
     * (y = max_value) will count towards the "Maximum permitted contact" limit. If
     * not selected, pixels along this edge will be ignored (i.e. contact won't lead
     * to object removal).
     */
    public static final String REMOVE_ON_BOTTOM = "Remove on bottom";

    /**
     * When selected, object pixels which make contact with the right side of the
     * image (x = max_value) will count towards the "Maximum permitted contact"
     * limit. If not selected, pixels along this edge will be ignored (i.e. contact
     * won't lead to object removal).
     */
    public static final String REMOVE_ON_RIGHT = "Remove on right";

    /**
     * When selected, object pixels which make contact with the lower (z = 0) and
     * upper (z = max_value) slices of the image stack will count towards the
     * "Maximum permitted contact" limit. If not selected, pixels along this edge
     * will be ignored (i.e. contact won't lead to object removal). If enabled for
     * single slice stacks all objects will removed.
     */
    public static final String INCLUDE_Z_POSITION = "Include Z-position";

    /**
     * When selected, the number of removed (or moved) objects is counted and stored
     * as a metadata item (name in the format "FILTER // NUM_[inputObjectsName]
     * TOUCHING_IM_EDGE (3D)").
     */
    public static final String STORE_RESULTS = "Store filter results";

    public FilterOnImageEdge(Modules modules) {
        super("Remove on image edge", modules);
    }

    public String getMetadataName(String inputObjectsName, boolean includeZ) {
        if (includeZ) {
            return "FILTER // NUM_" + inputObjectsName + " TOUCHING_IM_EDGE (3D)";
        } else {
            return "FILTER // NUM_" + inputObjectsName + " TOUCHING_IM_EDGE (2D)";
        }
    }

    public static int process(Objs inputObjects, int maxContact, @Nullable boolean[] removalEdges,
            boolean includeZ, boolean remove, @Nullable Objs outputObjects) {
        if (removalEdges == null)
            removalEdges = new boolean[] { true, true, true, true };

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // If the following is negative, there's no need to remove the object
            if (!hasContactWithEdge(inputObject, maxContact, removalEdges, includeZ))
                continue;

            if (remove)
                processRemoval(inputObject, outputObjects, iterator);

            // Incrementing the counter
            count++;

        }

        return count;

    }

    public static boolean hasContactWithEdge(Obj obj, int maxContact, boolean[] removalEdges, boolean includeZ) {
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = obj.getSpatialCalibration().getWidth() - 1;
        int maxY = obj.getSpatialCalibration().getHeight() - 1;
        int maxZ = obj.getSpatialCalibration().getNSlices() - 1;

        if (!removalEdges[0])
            minY = -Integer.MAX_VALUE;
        if (!removalEdges[1])
            minX = -Integer.MAX_VALUE;
        if (!removalEdges[2])
            maxY = Integer.MAX_VALUE;
        if (!removalEdges[3])
            maxX = Integer.MAX_VALUE;
            
        int count = 0;
        for (Point<Integer> pt:obj.getCoordinateSet()) {
            if (pt.x == minX || pt.x == maxX || pt.y == minY || pt.y == maxY)
                count++;

            // Only consider Z if the user requested this
            if (includeZ && (pt.z == minZ | pt.z == maxZ))
                count++;

            // Check if the maximum number of contacts with the edge has been made
            if (count > maxContact)
                return true;

        }

        return false;

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on contact of each object with the image edge.  Contact is considered as a case where an object pixel is in the outer-most row, column or slice  of an image (e.g. x = 0 or y = max_value).  The maximum number of contact pixels before an object is removed can be set to permit a degree of contact.  Objects identified as being in contact with the image edge can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.  <br><br>Image edge filters can be used when counting the number of objects in a field of view - in this case, typically two adjacent edges are removed (e.g. bottom and right) to prevent over-counting.  Alternatively, removing objects on all edges can be performed when measuring whole-object properties such as area or volume to prevent under-measuring values.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        int maxContact = parameters.getValue(MAXIMUM_CONTACT, workspace);
        boolean removeTop = parameters.getValue(REMOVE_ON_TOP, workspace);
        boolean removeLeft = parameters.getValue(REMOVE_ON_LEFT, workspace);
        boolean removeBottom = parameters.getValue(REMOVE_ON_BOTTOM, workspace);
        boolean removeRight = parameters.getValue(REMOVE_ON_RIGHT, workspace);
        boolean includeZ = parameters.getValue(INCLUDE_Z_POSITION, workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        boolean[] removalEdges = new boolean[] { removeTop, removeLeft, removeBottom, removeRight };

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        int count = process(inputObjects, maxContact, removalEdges, includeZ, remove, outputObjects);

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getMetadataName(inputObjectsName, includeZ);
        workspace.getMetadata().put(metadataName, count);

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new IntegerP(MAXIMUM_CONTACT, this, 0));
        parameters.add(new BooleanP(REMOVE_ON_TOP, this, true));
        parameters.add(new BooleanP(REMOVE_ON_LEFT, this, true));
        parameters.add(new BooleanP(REMOVE_ON_BOTTOM, this, true));
        parameters.add(new BooleanP(REMOVE_ON_RIGHT, this, true));
        parameters.add(new BooleanP(INCLUDE_Z_POSITION, this, false));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MAXIMUM_CONTACT));
        returnedParameters.add(parameters.getParameter(REMOVE_ON_TOP));
        returnedParameters.add(parameters.getParameter(REMOVE_ON_LEFT));
        returnedParameters.add(parameters.getParameter(REMOVE_ON_BOTTOM));
        returnedParameters.add(parameters.getParameter(REMOVE_ON_RIGHT));
        returnedParameters.add(parameters.getParameter(INCLUDE_Z_POSITION));
        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        WorkspaceI workspace = null;
        return super.updateAndGetObjectMeasurementRefs();

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return super.updateAndGetObjectMetadataRefs();
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        WorkspaceI workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        // Filter results are stored as a metadata item since they apply to the whole
        // set
        if ((boolean) parameters.getValue(STORE_RESULTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            boolean includeZ = parameters.getValue(INCLUDE_Z_POSITION, workspace);

            String metadataName = getMetadataName(inputObjectsName, includeZ);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(MAXIMUM_CONTACT).setDescription(
                "Maximum number of object pixels which can lie along any of the specified edges without the object being removed.  This provides tolerance for objects which only just make contact with the image edge.");

        parameters.get(REMOVE_ON_TOP).setDescription(
                "When selected, object pixels which make contact with the top of the image (y = 0) will count towards the \""
                        + MAXIMUM_CONTACT
                        + "\" limit.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).");

        parameters.get(REMOVE_ON_LEFT).setDescription(
                "When selected, object pixels which make contact with the left side of the image (x = 0) will count towards the \""
                        + MAXIMUM_CONTACT
                        + "\" limit.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).");

        parameters.get(REMOVE_ON_BOTTOM).setDescription(
                "When selected, object pixels which make contact with the bottom of the image (y = max_value) will count towards the \""
                        + MAXIMUM_CONTACT
                        + "\" limit.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).");

        parameters.get(REMOVE_ON_RIGHT).setDescription(
                "When selected, object pixels which make contact with the right side of the image (x = max_value) will count towards the \""
                        + MAXIMUM_CONTACT
                        + "\" limit.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).");

        parameters.get(INCLUDE_Z_POSITION).setDescription(
                "When selected, object pixels which make contact with the lower (z = 0) and upper (z = max_value) slices of the image stack will count towards the \""
                        + MAXIMUM_CONTACT
                        + "\" limit.  If not selected, pixels along this edge will be ignored (i.e. contact won't lead to object removal).  If enabled for single slice stacks all objects will removed.");

        String metadataName = getMetadataName("[inputObjectsName]", true);
        parameters.get(STORE_RESULTS).setDescription(
                "When selected, the number of removed (or moved) objects is counted and stored as a metadata item (name in the format \""
                        + metadataName + "\").");

    }
}
