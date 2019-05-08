package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;

import java.util.Iterator;

public class WithWithoutMeasurement extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String STORE_RESULTS = "Store filter results";


    public interface FilterMethods {
        String WITH_MEASUREMENT = "Remove objects with measurement";
        String WITHOUT_MEASUREMENT = "Remove objects without measurement";

        String[] ALL = new String[]{WITH_MEASUREMENT, WITHOUT_MEASUREMENT};

    }


    public String getFullName(String inputObjectsName, String filterMethod, String measName) {
        switch (filterMethod) {
            case FilterMethods.WITH_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITH " + measName + " MEASUREMENT";
            case FilterMethods.WITHOUT_MEASUREMENT:
                return "FILTER // NUM_" + inputObjectsName + " WITHOUT " + measName + " MEASUREMENT";
            default:
                return "";
        }
    }


    @Override
    public String getTitle() {
        return "With / without measurement";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String filterMethod = parameters.getValue(FILTER_METHOD);
        String measName = parameters.getValue(MEASUREMENT);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            // Removing the object if it has no children
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null || Double.isNaN(measurement.getValue())) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getFullName(inputObjectsName,filterMethod,measName);
        workspace.getMetadata().put(metadataName,count);

        // Showing objects
        if (showOutput) showRemainingObjects(inputObjects);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChoiceP(FILTER_MODE,this, FilterModes.REMOVE_FILTERED, FilterModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_FILTERED_OBJECTS, this));

        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.WITHOUT_MEASUREMENT, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

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

        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        objectMeasurementRefs.setAllAvailable(false);

        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            MeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName,this);

            for (MeasurementRef reference:references.values()) {
                MeasurementRef.Type type = MeasurementRef.Type.OBJECT;
                objectMeasurementRefs.getOrPut(reference.getName(), type).setImageObjName(filteredObjectsName);
            }

            return objectMeasurementRefs;

        }

        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        // Filter results are stored as a metadata item since they apply to the whole set
        if (parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String measName = parameters.getValue(MEASUREMENT);

            String metadataName = getFullName(inputObjectsName,filterMethod,measName);

            metadataRefs.getOrPut(metadataName).setAvailable(true);

        }

        return metadataRefs;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
