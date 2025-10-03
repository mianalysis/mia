package io.github.mianalysis.mia.module.objects.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Filter an object collection to remove/retain the object with the
 * largest/smallest value for a specific measurement. The objects identified for
 * removal can be indeed removed from the input collection, moved to another
 * collection (and removed from the input collection) or simply counted (but
 * retained in the input collection).
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FilterByMeasurementExtremes extends AbstractObjectFilter {

    /**
    * 
    */
    public static final String FILTER_SEPARATOR = "Object filtering";

    /**
     * Controls what happens to objects which don't pass the filter:<br>
     * <br>
     * - "Remove with largest measurements" Remove the object with the largest value
     * measurement specified by "Measurement to filter on".<br>
     * <br>
     * - "Remove with smallest measurements" Remove the object with the smallest
     * value measurement specified by "Measurement to filter on".<br>
     * <br>
     * - "Retain with largest measurements" Retain only the object with the largest
     * value measurement specified by "Measurement to filter on".<br>
     * <br>
     * - "Retain with smallest measurements" Retain only the object with the
     * smallest value measurement specified by "Measurement to filter on".<br>
     */
    public static final String FILTER_METHOD = "Method for filtering";

    /**
     * When selected, the measurements will be considered on a
     * timepoint-by-timepoint basis. For example, if retaining the object with the
     * largest measurement, the object in each timepoint with the largest
     * measurement would be retained; however, when not selected, only one object in
     * the entire timeseries would be retained.
     */
    public static final String PER_TIMEPOINT = "Filter per timepoint";

    /**
    * 
    */
    public static final String PER_PARENT = "Filter per parent object";

    /**
    * 
    */
    public static final String PARENT_OBJECT = "Parent object";

    /**
     * Objects will be filtered against their value of this measurement. Objects
     * missing this measurement are not removed; however, they can be removed by
     * using the module "With / without measurement".
     */
    public static final String MEASUREMENT = "Measurement to filter on";

    /**
     * Number of objects with the most extreme measurements to remove or retain. If
     * an insufficient number of objects are available, the most possible will be
     * used.
     */
    public static final String N_MEASUREMENTS = "Number of measurements";

    public FilterByMeasurementExtremes(Modules modules) {
        super("Measurement extremes", modules);
    }

    public interface FilterMethods {
        String REMOVE_LARGEST = "Remove with largest measurements";
        String REMOVE_SMALLEST = "Remove with smallest measurements";
        String RETAIN_LARGEST = "Retain with largest measurements";
        String RETAIN_SMALLEST = "Retain with smallest measurements";

        String[] ALL = new String[] { REMOVE_LARGEST, REMOVE_SMALLEST, RETAIN_LARGEST, RETAIN_SMALLEST };

    }

    public static ArrayList<Integer> getIDsToRemove(Objs inputObjects, String measName, String filterMethod,
            boolean perTimepoint, int nMeas) {
        ArrayList<Integer> toRemove = new ArrayList<>();

        if (inputObjects.size() == 0)
            return toRemove;

        // Getting reference limits
        HashMap<Integer, double[]> minMax = getMeasurementExtremes(inputObjects, measName, perTimepoint, nMeas);

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null)
                continue;

            // Getting the values to filter on
            double value = measurement.getValue();
            int t = perTimepoint ? inputObject.getT() : 0;
            double[] currMinMax = minMax.get(t);

            if (testFilter(value, currMinMax, filterMethod))
                toRemove.add(inputObject.getID());

        }

        return toRemove;

    }

    public static HashMap<Integer, double[]> getMeasurementExtremes(Objs objects, String measurementName,
            boolean perTimepoint, int nMeas) {
        HashMap<Integer, double[]> minMax = new HashMap<>();

        // Iterating over all frames, getting the objects and identifying limits for min
        // and max
        int nFrames = perTimepoint ? objects.getNFrames() : 1;

        for (int t = 0; t < nFrames; t++) {
            // Adding measurements to TreeSet (sorted)
            TreeSet<Double> measurementsSet = new TreeSet<>();
            for (Obj obj : objects.values()) {
                if (perTimepoint && obj.getT() != t)
                    continue;

                Measurement measurement = obj.getMeasurement(measurementName);
                if (measurement == null)
                    continue;

                measurementsSet.add(measurement.getValue());

            }

            if (measurementsSet.size() == 0)
                continue;

            // Converting to an ArrayList for easy access
            ArrayList<Double> measurementsList = new ArrayList<>(measurementsSet);

            // Getting top and bottom nMeas measurements
            int minIdx = Math.min(nMeas - 1, measurementsSet.size() - 1);
            int maxIdx = Math.max(0, measurementsSet.size() - nMeas);

            // Storing limits for this timepoint
            minMax.put(t, new double[] { measurementsList.get(minIdx), measurementsList.get(maxIdx) });

        }

        return minMax;

    }

    public static boolean testFilter(double value, double[] minMax, String filterMethod) {
        switch (filterMethod) {
            default:
                return true;
            case FilterMethods.REMOVE_LARGEST:
                return (value >= minMax[1]);
            case FilterMethods.REMOVE_SMALLEST:
                return (value <= minMax[0]);
            case FilterMethods.RETAIN_LARGEST:
                return (value < minMax[1]);
            case FilterMethods.RETAIN_SMALLEST:
                return (value > minMax[0]);
        }
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
        return "Filter an object collection to remove/retain the object with the largest/smallest value for a specific measurement.  The objects identified for removal can be indeed removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        boolean perTimepoint = parameters.getValue(PER_TIMEPOINT, workspace);
        boolean perParent = parameters.getValue(PER_PARENT, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);
        String measName = parameters.getValue(MEASUREMENT, workspace);
        int nMeas = parameters.getValue(N_MEASUREMENTS, workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        // Identifying object IDs to remove
        ArrayList<Integer> toRemove = new ArrayList<>();
        if (perParent) {
            // These are selected in a slightly unusual way, so we have to reconfigure the
            // parentObjectsName into a childObjectsName form
            String[] names = parentObjectsName.split(" // ");
            String childObjectsName = "";
            for (int i = names.length - 2; i >= 0; i--)
                childObjectsName = childObjectsName + names[i] + " // ";
            childObjectsName = childObjectsName + inputObjectsName;

            Objs parentObjects = workspace.getObjects(names[names.length - 1]);
            for (Obj parentObject : parentObjects.values()) {
                Objs childObjects = parentObject.getChildren(childObjectsName);
                toRemove.addAll(getIDsToRemove(childObjects, measName, filterMethod, perTimepoint, nMeas));
            }
        } else {
            toRemove.addAll(getIDsToRemove(inputObjects, measName, filterMethod, perTimepoint, nMeas));
        }

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it failed the test
            if (toRemove.contains(inputObject.getID()) && remove)
                processRemoval(inputObject, outputObjects, iterator);
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
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.REMOVE_LARGEST, FilterMethods.ALL));
        parameters.add(new BooleanP(PER_TIMEPOINT, this, false));
        parameters.add(new BooleanP(PER_PARENT, this, false));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new IntegerP(N_MEASUREMENTS, this, "1"));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(PER_TIMEPOINT));
        returnedParameters.add(parameters.getParameter(PER_PARENT));
        if ((boolean) parameters.getValue(PER_PARENT, workspace)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
            ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
        }
        returnedParameters.add(parameters.getParameter(PER_TIMEPOINT));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
        returnedParameters.add(parameters.getParameter(N_MEASUREMENTS));

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

        parameters.get(FILTER_METHOD).setDescription("Controls what happens to objects which don't pass the filter:<br>"
                + "<br>- \"" + FilterMethods.REMOVE_LARGEST
                + "\" Remove the object with the largest value measurement specified by \"" + MEASUREMENT + "\".<br>"

                + "<br>- \"" + FilterMethods.REMOVE_SMALLEST
                + "\" Remove the object with the smallest value measurement specified by \"" + MEASUREMENT + "\".<br>"

                + "<br>- \"" + FilterMethods.RETAIN_LARGEST
                + "\" Retain only the object with the largest value measurement specified by \"" + MEASUREMENT
                + "\".<br>"

                + "<br>- \"" + FilterMethods.RETAIN_SMALLEST
                + "\" Retain only the object with the smallest value measurement specified by \"" + MEASUREMENT
                + "\".<br>");

        parameters.get(PER_TIMEPOINT).setDescription(
                "When selected, the measurements will be considered on a timepoint-by-timepoint basis.  For example, if retaining the object with the largest measurement, the object in each timepoint with the largest measurement would be retained; however, when not selected, only one object in the entire timeseries would be retained.");

        parameters.get(MEASUREMENT).setDescription(
                "Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""
                        + new FilterWithWithoutMeasurement(null).getName() + "\".");

        parameters.get(N_MEASUREMENTS).setDescription(
                "Number of objects with the most extreme measurements to remove or retain.  If an insufficient number of objects are available, the most possible will be used.");

    }
}
