package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;

import java.util.Iterator;

public class NumberOfChildren extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String STORE_RESULTS = "Store filter results";


    public String getMetadataName(String inputObjectsName, String filterMethod, String childObjectsName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);

        return "FILTER // NUM_" + inputObjectsName + " " + filterMethodSymbol + " " + referenceValue + "_" + childObjectsName;

    }


    @Override
    public String getTitle() {
        return "Number of children";
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
        String childObjectsName = parameters.getValue(CHILD_OBJECTS);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            ObjCollection childObjects = inputObject.getChildren(childObjectsName);

            // Removing the object if it has no children
            if (childObjects == null) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
                continue;
            }

            // Removing the object if it has too few children
            if (testFilter(childObjects.size(),referenceValue,filterMethod)) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getMetadataName(inputObjectsName,filterMethod,childObjectsName,String.valueOf(referenceValue));
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
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS, this));
        parameters.add(new DoubleP(REFERENCE_VALUE, this,1d));
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
        returnedParameters.add(parameters.getParameter(CHILD_OBJECTS));
        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
        returnedParameters.add(parameters.getParameter(STORE_RESULTS));
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        objectMeasurementRefs.setAllCalculated(false);

        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);

            // Getting object measurement references associated with this object set
            MeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName,this);

            for (MeasurementRef reference:references.values()) {
                MeasurementRef newRef = reference.duplicate();
                newRef.setImageObjName(filteredObjectsName);
                objectMeasurementRefs.add(newRef);
            }

            return objectMeasurementRefs;

        }

        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection metadataReferences = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole set
        if (parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String childObjectsName = parameters.getValue(CHILD_OBJECTS);
            String referenceValue = parameters.getValue(REFERENCE_VALUE).toString();

            String metadataName = getMetadataName(inputObjectsName,filterMethod,childObjectsName,referenceValue);

            metadataReferences.add(new MetadataReference(metadataName));

        }

        return metadataReferences;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }
}
