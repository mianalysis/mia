package io.github.mianalysis.mia.module.objects.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.measure.spatial.CalculateNearestNeighbour;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Filters objects in close XY proximity based on a specific measurement. For
 * two, or more, objects within close proximity of each other the object with
 * the largest (or smallest) measurement will be retained, whilst the others
 * will be removed. This can be used to filter instances of the same object
 * being detected multiple times. Distances are only considered in XY. Any
 * Z-axis information on object position will be ignored.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterByProximity extends AbstractObjectFilter {

    /**
    * 
    */
    public static final String FILTER_SEPARATOR = "Object filtering";

    /**
     * Controls the method used for determining the nearest neighbour distances:<br>
     * <ul>
     * <li>"Centroid (2D)" Distances are between the input and neighbour object
     * centroids, but only in the XY plane. These distances are always positive;
     * increasing as the distance between centroids increases.</li>
     * <li>"Centroid (3D)" Distances are between the input and neighbour object
     * centroids. These distances are always positive; increasing as the distance
     * between centroids increases.</li>
     * <li>"Surface (2D)" Distances are between the closest points on the input and
     * neighbour surfaces, but only in the XY plane. These distances increase in
     * magnitude the greater the minimum input-neighbour object surface distance is;
     * however, they are assigned a positive value if the closest input object
     * surface point is outside the neighbour and a negative value if the closest
     * input object surface point is inside the neighbour. For example, a closest
     * input object surface point 5px outside the neighbour will be simply "5px",
     * whereas a closest input object surface point 5px from the surface, but
     * contained within the neighbour object will be recorded as "-5px". Note: Any
     * instances where the input and neighbour object surfaces overlap will be
     * recorded as "0px" distance.</li>
     * <li>"Surface (3D)" Distances are between the closest points on the input and
     * neighbour surfaces. These distances increase in magnitude the greater the
     * minimum input-neighbour object surface distance is; however, they are
     * assigned a positive value if the closest input object surface point is
     * outside the neighbour and a negative value if the closest input object
     * surface point is inside the neighbour. For example, a closest input object
     * surface point 5px outside the neighbour will be simply "5px", whereas a
     * closest input object surface point 5px from the surface, but contained within
     * the neighbour object will be recorded as "-5px". Note: Any instances where
     * the input and neighbour object surfaces overlap will be recorded as "0px"
     * distance.</li>
     * </ul>
     */
    public static final String REFERENCE_MODE = "Reference mode";

    /**
     * Minimum allowed distance in XY plane for two objects to co-exist. Any objects
     * with XY separation smaller than this value will be subject to filtering,
     * where the "less suitable" (depending on filter settings) object will be
     * removed.
     */
    public static final String MINIMUM_SEPARATION = "Minimum separation";

    /**
     * When selected, object-object distances are to be specified in calibrated
     * units; otherwise, units are specified in pixels.
     */
    public static final String CALIBRATED_UNITS = "Calibrated units";

    /**
     * When selected, objects must be in the same time frame for them to be linked.
     */
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";

    /**
     * For objects closer than the value specified by "Minimum separation" this
     * parameter controls which will be retained.
     */
    public static final String FILTER_METHOD = "Method for filtering";

    /**
     * Objects will be filtered against their value of this measurement. Objects
     * missing this measurement are not removed; however, they can be removed by
     * using the module "With / without measurement".
     */
    public static final String MEASUREMENT = "Measurement to filter on";

    public FilterByProximity(Modules modules) {
        super("Object proximity", modules);
    }

    public interface ReferenceModes extends CalculateNearestNeighbour.ReferenceModes {
    };

    public interface FilterMethods {
        String PRIORITISE_LARGER_MEASUREMENT = "Prioritise larger measurement";
        String PRIORITISE_SMALLER_MEASUREMENT = "Prioritise smaller measurement";

        String[] ALL = new String[] { PRIORITISE_LARGER_MEASUREMENT, PRIORITISE_SMALLER_MEASUREMENT };

    }

    public static MeasurementComparator getComparator(String filterMethod, String measName) {
        switch (filterMethod) {
            case FilterMethods.PRIORITISE_LARGER_MEASUREMENT:
            default:
                return new MeasurementComparator(measName, false);
            case FilterMethods.PRIORITISE_SMALLER_MEASUREMENT:
                return new MeasurementComparator(measName, true);
        }
    }

    // public static void calculateAllOverlaps(Obj inputObject, Objs
    // testObjects, boolean linkInSameFrame,
    // @Nullable LinkedHashMap<Obj, Double> currOverlaps) {
    // for (Obj testObject : testObjects.values()) {
    // // Don't compare an object to itself
    // if (testObject == inputObject)
    // continue;

    // // Check if we should only be comparing objects in same timepoint
    // if (linkInSameFrame && (inputObject.getT() != testObject.getT()))
    // continue;

    // // Calculating and storing overlap
    // double overlap = inputObject.getOverlap(testObject);
    // currOverlaps.put(testObject, overlap);

    // }
    // }

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
        return "Filters objects in close XY proximity based on a specific measurement.  For two, or more, objects within close proximity of each other the object with the largest (or smallest) measurement will be retained, whilst the others will be removed.  This can be used to filter instances of the same object being detected multiple times.  Distances are only considered in XY.  Any Z-axis information on object position will be ignored.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        double minSeparation = parameters.getValue(MINIMUM_SEPARATION, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        String measName = parameters.getValue(MEASUREMENT, workspace);
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        if (calibratedUnits)
            minSeparation = minSeparation / inputObjects.getDppXY();

        if (inputObjects == null)
            return Status.PASS;

        ObjsI outputObjects = moveObjects ? ObjsFactories.getDefaultFactory().createFromExample(outputObjectsName, inputObjects) : null;

        // Ordering objects based on their measurement
        MeasurementComparator comparator = getComparator(filterMethod, measName);
        ArrayList<ObjI> sortedObjects = new ArrayList<>(inputObjects.values());
        sortedObjects.sort(comparator);

        // Iterating over each object, identifying any objects within the mimimum
        // distance and removing them
        int count = 0;
        int total = sortedObjects.size();
        for (ObjI sortedObject : sortedObjects) {
            // Checking that this object hasn't already been removed
            if (!inputObjects.containsValue(sortedObject)) {
                writeProgressStatus(++count, total, "objects");
                continue;
            }

            LinkedHashMap<ObjI, Double> scores = new LinkedHashMap<>();

            // Calculating all nearest neighbour distances (minSeparation parameter doesn't
            // influence result here)
            CalculateNearestNeighbour.getNearestNeighbour(sortedObject, inputObjects, referenceMode, minSeparation,
                    linkInSameFrame, scores);

            // Iterating over each neighbour, removing it if it's closer than the minimum
            // separation
            Iterator<ObjI> iterator = inputObjects.values().iterator();
            while (iterator.hasNext()) {
                ObjI inputObject = iterator.next();

                // Don't compare an object to itself
                if (inputObject == sortedObject)
                    continue;

                if (scores.containsKey(inputObject) && scores.get(inputObject) < minSeparation && remove)
                    processRemoval(inputObject, outputObjects, iterator);

            }

            writeProgressStatus(++count, total, "objects");

        }

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageIDColours().showWithNormalisation(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.CENTROID_3D, ReferenceModes.ALL));
        parameters.add(new DoubleP(MINIMUM_SEPARATION, this, 0.0));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new BooleanP(LINK_IN_SAME_FRAME, this, false));
        parameters
                .add(new ChoiceP(FILTER_METHOD, this, FilterMethods.PRIORITISE_LARGER_MEASUREMENT, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        returnedParameters.add(parameters.getParameter(MINIMUM_SEPARATION));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        returnedParameters.add(parameters.getParameter(LINK_IN_SAME_FRAME));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return super.updateAndGetObjectMeasurementRefs();

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return super.updateAndGetObjectMetadataRefs();
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(REFERENCE_MODE)
                .setDescription("Controls the method used for determining the nearest neighbour distances:<br><ul>"

                        + "<li>\"" + ReferenceModes.CENTROID_2D
                        + "\" Distances are between the input and neighbour object centroids, but only in the XY plane.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + ReferenceModes.CENTROID_3D
                        + "\" Distances are between the input and neighbour object centroids.  These distances are always positive; increasing as the distance between centroids increases.</li>"

                        + "<li>\"" + ReferenceModes.SURFACE_2D
                        + "\" Distances are between the closest points on the input and neighbour surfaces, but only in the XY plane.  These distances increase in magnitude the greater the minimum input-neighbour object surface distance is; however, they are assigned a positive value if the closest input object surface point is outside the neighbour and a negative value if the closest input object surface point is inside the neighbour.  For example, a closest input object surface point 5px outside the neighbour will be simply \"5px\", whereas a closest input object surface point 5px from the surface, but contained within the neighbour object will be recorded as \"-5px\".  Note: Any instances where the input and neighbour object surfaces overlap will be recorded as \"0px\" distance.</li>"

                        + "<li>\"" + ReferenceModes.SURFACE_3D
                        + "\" Distances are between the closest points on the input and neighbour surfaces.  These distances increase in magnitude the greater the minimum input-neighbour object surface distance is; however, they are assigned a positive value if the closest input object surface point is outside the neighbour and a negative value if the closest input object surface point is inside the neighbour.  For example, a closest input object surface point 5px outside the neighbour will be simply \"5px\", whereas a closest input object surface point 5px from the surface, but contained within the neighbour object will be recorded as \"-5px\".  Note: Any instances where the input and neighbour object surfaces overlap will be recorded as \"0px\" distance.</li></ul>");

        parameters.get(MINIMUM_SEPARATION).setDescription(
                "Minimum allowed distance in XY plane for two objects to co-exist.  Any objects with XY separation smaller than this value will be subject to filtering, where the \"less suitable\" (depending on filter settings) object will be removed.");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, object-object distances are to be specified in calibrated units; otherwise, units are specified in pixels.");

        parameters.get(LINK_IN_SAME_FRAME)
                .setDescription("When selected, objects must be in the same time frame for them to be linked.");

        parameters.get(FILTER_METHOD).setDescription("For objects closer than the value specified by \""
                + MINIMUM_SEPARATION + "\" this parameter controls which will be retained.");

        parameters.get(MEASUREMENT).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""
                        + new FilterWithWithoutMeasurement(null).getName() + "\".");

    }
}

class MeasurementComparator implements Comparator<ObjI> {
    private String measurementName;
    private boolean ascending;

    public MeasurementComparator(String measurementName, boolean ascending) {
        this.measurementName = measurementName;
        this.ascending = ascending;
    }

    @Override
    public int compare(ObjI o1, ObjI o2) {
        Double val1 = Double.NaN;
        Double val2 = Double.NaN;

        MeasurementI meas1 = o1.getMeasurement(measurementName);
        if (meas1 != null)
            val1 = meas1.getValue();

        MeasurementI meas2 = o2.getMeasurement(measurementName);
        if (meas2 != null)
            val2 = meas2.getValue();

        return ascending ? val1.compareTo(val2) : val2.compareTo(val1);

    }
}
