package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;

public class FilterByMeasurementExtremes extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
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
        // if (showOutput) showRemainingObjects(inputObjects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE, this, FilterModes.REMOVE_FILTERED, FilterModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.REMOVE_LARGEST, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(FILTER_MODE));
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_FILTERED_OBJECTS));
        }

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
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        // If the filtered objects are to be moved to a new class, assign them the
        // measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName, this);

            for (ObjMeasurementRef reference : references.values()) {
                returnedRefs
                        .add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }

            return returnedRefs;

        }

        return null;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }
}
