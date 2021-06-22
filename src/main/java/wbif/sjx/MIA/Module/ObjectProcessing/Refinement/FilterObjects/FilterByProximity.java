package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.sisu.Nullable;

import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.CalculateNearestNeighbour;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;

public class FilterByProximity extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String MINIMUM_SEPARATION = "Minimum separation";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String LINK_IN_SAME_FRAME = "Only link objects in same frame";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";

    public FilterByProximity(ModuleCollection modules) {
        super("Object proximity", modules);
    }

    public interface ReferenceModes extends CalculateNearestNeighbour.ReferenceModes {};


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

    // public static void calculateAllOverlaps(Obj inputObject, ObjCollection
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
        return Categories.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        double minSeparation = parameters.getValue(MINIMUM_SEPARATION);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean linkInSameFrame = parameters.getValue(LINK_IN_SAME_FRAME);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String measName = parameters.getValue(MEASUREMENT);
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        if (calibratedUnits)
            minSeparation = minSeparation / inputObjects.getDppXY();

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName, inputObjects) : null;

        // Ordering objects based on their measurement
        MeasurementComparator comparator = getComparator(filterMethod, measName);
        ArrayList<Obj> sortedObjects = new ArrayList<>(inputObjects.values());
        sortedObjects.sort(comparator);

        // Iterating over each object, identifying any objects within the mimimum
        // distance and removing them
        int count = 0;
        int total = sortedObjects.size();
        for (Obj sortedObject : sortedObjects) {
            // Checking that this object hasn't already been removed
            if (!inputObjects.containsValue(sortedObject)) {
                writeProgressStatus(++count, total, "objects");
                continue;
            }

            LinkedHashMap<Obj, Double> scores = new LinkedHashMap<>();

            // Calculating all nearest neighbour distances (minSeparation parameter doesn't
            // influence result here)
                    CalculateNearestNeighbour.getNearestNeighbour(sortedObject, inputObjects, referenceMode,
                            minSeparation, linkInSameFrame, scores);

            // Iterating over each neighbour, removing it if it's closer than the minimum
            // separation
            Iterator<Obj> iterator = inputObjects.values().iterator();
            while (iterator.hasNext()) {
                Obj inputObject = iterator.next();

                // Don't compare an object to itself
                if (inputObject == sortedObject)
                    continue;

                if (scores.get(inputObject) < minSeparation && remove)
                    processRemoval(inputObject, outputObjects, iterator);

            }

            writeProgressStatus(++count, total, "objects");

        }

        // If moving objects, addRef them to the workspace
        if (moveObjects)
            workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput)
            inputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
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
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return super.updateAndGetObjectMeasurementRefs();

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(FILTER_METHOD).setDescription("");

        parameters.get(MEASUREMENT).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""
                        + new FilterWithWithoutMeasurement(null).getName() + "\".");

    }
}

class MeasurementComparator implements Comparator<Obj> {
    private String measurementName;
    private boolean ascending;

    public MeasurementComparator(String measurementName, boolean ascending) {
        this.measurementName = measurementName;
        this.ascending = ascending;
    }

    @Override
    public int compare(Obj o1, Obj o2) {
        Double val1 = Double.NaN;
        Double val2 = Double.NaN;

        Measurement meas1 = o1.getMeasurement(measurementName);
        if (meas1 != null)
            val1 = meas1.getValue();

        Measurement meas2 = o2.getMeasurement(measurementName);
        if (meas2 != null)
            val2 = meas2.getValue();

        return ascending ? val1.compareTo(val2) : val2.compareTo(val1);

    }
}