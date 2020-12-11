package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;

public class FilterByMeasurementExtremes extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";

    public FilterByMeasurementExtremes(ModuleCollection modules) {
        super("Measurement extremes", modules);
    }

    public interface FilterMethods {
        String REMOVE_LARGEST = "Remove object with largest measurement";
        String REMOVE_SMALLEST = "Remove object with smallest measurement";
        String RETAIN_LARGEST = "Retain object with largest measurement";
        String RETAIN_SMALLEST = "Retain object with smallest measurement";

        String[] ALL = new String[] { REMOVE_LARGEST, REMOVE_SMALLEST, RETAIN_LARGEST, RETAIN_SMALLEST };

    }

    public static double[] getMeasurementExtremes(ObjCollection objects, String measurementName) {
        double[] minMax = new double[] { Double.MAX_VALUE, -Double.MAX_VALUE };

        for (Obj obj : objects.values()) {
            Measurement measurement = obj.getMeasurement(measurementName);
            if (measurement == null)
                continue;

            // Getting the values to filter on
            double value = measurement.getValue();

            minMax[0] = Math.min(minMax[0], value);
            minMax[1] = Math.max(minMax[1], value);

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
        return Categories.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection to remove/retain the object with the largest/smallest value for a specific measurement.  The objects identified for removal can be indeed removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String measName = parameters.getValue(MEASUREMENT);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName, inputObjects) : null;

        // Getting reference limits
        double[] minMax = getMeasurementExtremes(inputObjects, measName);

        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null)
                continue;

            // Getting the values to filter on
            double value = measurement.getValue();
            boolean conditionMet = testFilter(value, minMax, filterMethod);

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
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
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

        parameters.get(MEASUREMENT).setDescription("Objects will be filtered against their value of this measurement.  Objects missing this measurement are not removed; however, they can be removed by using the module \""+new FilterWithWithoutMeasurement(null).getName()+"\".");

    }
}
