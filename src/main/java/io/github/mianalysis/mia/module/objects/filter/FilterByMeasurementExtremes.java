package io.github.mianalysis.mia.module.objects.filter;

import java.util.HashMap;
import java.util.Iterator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FilterByMeasurementExtremes extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String PER_TIMEPOINT = "Filter per timepoint";
    public static final String MEASUREMENT = "Measurement to filter on";

    public FilterByMeasurementExtremes(Modules modules) {
        super("Measurement extremes", modules);
    }

    public interface FilterMethods {
        String REMOVE_LARGEST = "Remove object with largest measurement";
        String REMOVE_SMALLEST = "Remove object with smallest measurement";
        String RETAIN_LARGEST = "Retain object with largest measurement";
        String RETAIN_SMALLEST = "Retain object with smallest measurement";

        String[] ALL = new String[] { REMOVE_LARGEST, REMOVE_SMALLEST, RETAIN_LARGEST, RETAIN_SMALLEST };

    }

    public static HashMap<Integer,double[]> getMeasurementExtremes(Objs objects, String measurementName, boolean perTimepoint) {
        HashMap<Integer, double[]> minMax = new HashMap<>();

        for (Obj obj : objects.values()) {
            Measurement measurement = obj.getMeasurement(measurementName);
            if (measurement == null)
                continue;

            // Getting the values to filter on
            double value = measurement.getValue();
            int t = perTimepoint ? obj.getT() : 0;

            minMax.putIfAbsent(t, new double[] { Double.MAX_VALUE, -Double.MAX_VALUE });
            double[] currMinMax = minMax.get(t); 

            currMinMax[0] = Math.min(currMinMax[0], value);
            currMinMax[1] = Math.max(currMinMax[1], value);

        }

        return minMax;

    }

    public static boolean testFilter(double value, double[] minMax, String filterMethod) {
        switch (filterMethod) {
            default:
                return true;
            case FilterMethods.REMOVE_LARGEST:
                return (value == minMax[1]);
            case FilterMethods.REMOVE_SMALLEST:
                return (value == minMax[0]);
            case FilterMethods.RETAIN_LARGEST:
                return !(value == minMax[1]);
            case FilterMethods.RETAIN_SMALLEST:
                return !(value == minMax[0]);
        }
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECTS_FILTER;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection to remove/retain the object with the largest/smallest value for a specific measurement.  The objects identified for removal can be indeed removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE,workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS,workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD,workspace);
        boolean perTimepoint = parameters.getValue(PER_TIMEPOINT,workspace);
        String measName = parameters.getValue(MEASUREMENT,workspace);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        Objs outputObjects = moveObjects ? new Objs(outputObjectsName, inputObjects) : null;

        // Getting reference limits
        HashMap<Integer,double[]> minMax = getMeasurementExtremes(inputObjects, measName, perTimepoint);

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

            boolean conditionMet = testFilter(value, currMinMax, filterMethod);

            // Removing the object if it failed the test
            if (conditionMet && remove)
                processRemoval(inputObject, outputObjects, iterator);

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
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.REMOVE_LARGEST, FilterMethods.ALL));
        parameters.add(new BooleanP(PER_TIMEPOINT, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(PER_TIMEPOINT));
        returnedParameters.add(parameters.getParameter(MEASUREMENT));
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return super.updateAndGetObjectMeasurementRefs();

    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.get(FILTER_METHOD).setDescription("Controls what happens to objects which don't pass the filter:<br>"
                + "<br>- \"" + FilterMethods.REMOVE_LARGEST
                + "\" Remove the object with the largest value measurement specified by \""+MEASUREMENT+"\".<br>"

                + "<br>- \"" + FilterMethods.REMOVE_SMALLEST
                + "\" Remove the object with the smallest value measurement specified by \""+MEASUREMENT+"\".<br>"

                + "<br>- \"" + FilterMethods.RETAIN_LARGEST
                + "\" Retain only the object with the largest value measurement specified by \""+MEASUREMENT+"\".<br>"

                + "<br>- \"" + FilterMethods.RETAIN_SMALLEST
                + "\" Retain only the object with the smallest value measurement specified by \""+MEASUREMENT+"\".<br>"
        );

        parameters.get(PER_TIMEPOINT).setDescription(
                "When selected, the measurements will be considered on a timepoint-by-timepoint basis.  For example, if retaining the object with the largest measurement, the object in each timepoint with the largest measurement would be retained; however, when not selected, only one object in the entire timeseries would be retained.");
        
        parameters.get(MEASUREMENT).setDescription("Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""+new FilterWithWithoutMeasurement(null).getName()+"\".");

    }
}
