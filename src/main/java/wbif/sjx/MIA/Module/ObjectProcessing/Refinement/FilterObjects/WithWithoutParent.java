package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class WithWithoutParent extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String STORE_RESULTS = "Store filter results";

    public WithWithoutParent(ModuleCollection modules) {
        super(modules);
    }


    public interface FilterMethods {
        String WITH_PARENT = "Remove objects with parent";
        String WITHOUT_PARENT = "Remove objects without parent";

        String[] ALL = new String[]{WITH_PARENT,WITHOUT_PARENT};

    }


    public static String getMetadataName(String inputObjectsName, String filterMethod, String parentObjectsName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);

        switch (filterMethod) {
            case FilterMethods.WITH_PARENT:
                return "FILTER // NUM_" + inputObjectsName + " WITH " + parentObjectsName + " PARENT";
            case FilterMethods.WITHOUT_PARENT:
                return "FILTER // NUM_" + inputObjectsName + " WITHOUT " + parentObjectsName + " PARENT";
            default:
                return "";
        }
    }


    @Override
    public String getTitle() {
        return "With / without parent";
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
        String parentObjectName = parameters.getValue(PARENT_OBJECT);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
            boolean toRemove = false;
            switch (filterMethod) {
                case FilterMethods.WITH_PARENT:
                    if (parents.get(parentObjectName) != null) {
                        count++;
                        if (remove) processRemoval(inputObject,outputObjects,iterator);
                    }
                    break;
                case FilterMethods.WITHOUT_PARENT:
                    if (parents.get(parentObjectName) == null) {
                        count++;
                        if (remove) processRemoval(inputObject,outputObjects,iterator);
                    }
                    break;
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getMetadataName(inputObjectsName,filterMethod,parentObjectName);
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
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.WITH_PARENT, FilterMethods.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
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
        returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
        returnedParameters.add(parameters.getParameter(STORE_RESULTS));
        ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
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
            String parentObjectsName = parameters.getValue(PARENT_OBJECT);

            String metadataName = getMetadataName(inputObjectsName,filterMethod,parentObjectsName);

            metadataRefs.getOrPut(metadataName).setAvailable(true);

        }

        return metadataRefs;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
