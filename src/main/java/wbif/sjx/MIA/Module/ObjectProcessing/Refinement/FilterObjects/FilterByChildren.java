package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.*;

import java.util.Iterator;

public class FilterByChildren extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";
    
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String CHILD_OBJECTS = "Child objects";
    public static final String REFERENCE_VALUE = "Reference value";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";
    
    public FilterByChildren(ModuleCollection modules) {
        super("Number of children",modules);
    }
    
    
    public String getMetadataName(String inputObjectsName, String filterMethod, String childObjectsName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        
        return "FILTER // NUM_" + inputObjectsName + " " + filterMethodSymbol + " " + referenceValue + "_" + childObjectsName;
        
    }
    
    public String getMeasurementName(String filterMethod, String childObjectsName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        
        return "FILTER // " + filterMethodSymbol + " " + referenceValue + "_" + childObjectsName;
        
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
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS);
        
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);
        
        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName,inputObjects) : null;
        
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
            
            boolean conditionMet = testFilter(childObjects.size(),referenceValue,filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = getMeasurementName(filterMethod,childObjectsName,String.valueOf(referenceValue));
                inputObject.addMeasurement(new Measurement(measurementName,conditionMet ? 1 : 0));
            }

            // Removing the object if it has too few children
            if (conditionMet) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
        
        // If moving objects, addRef them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);
        
        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String metadataName = getMetadataName(inputObjectsName,filterMethod,childObjectsName,String.valueOf(referenceValue));
            workspace.getMetadata().put(metadataName,count);
        }
        
        // Showing objects
        if (showOutput) inputObjects.convertToImageRandomColours().showImage();
        
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
        
        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));
        
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
        
        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));
        
        ((ChildObjectsP) parameters.getParameter(CHILD_OBJECTS)).setParentObjectsName(inputObjectsName);
        
        return returnedParameters;
        
    }
    
    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }
    
    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        
        // If the filtered objects are to be moved to a new class, assign them the measurements they've lost
        if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filteredObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
            
            // Getting object measurement references associated with this object set
            ObjMeasurementRefCollection references = modules.getObjectMeasurementRefs(inputObjectsName,this);
            
            for (ObjMeasurementRef reference:references.values()) {
                returnedRefs.add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(filteredObjectsName));
            }
            
            return returnedRefs;
            
        }
        
        if ((boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String childObjectsName = parameters.getValue(CHILD_OBJECTS);
            double referenceValue = parameters.getValue(REFERENCE_VALUE);
            
            String measurementName = getMeasurementName(filterMethod,childObjectsName,String.valueOf(referenceValue));
            
            returnedRefs.add(new ObjMeasurementRef(measurementName,inputObjectsName));
            if (parameters.getValue(FILTER_MODE).equals(FilterModes.MOVE_FILTERED)) {
                String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
                returnedRefs.add(new ObjMeasurementRef(measurementName,outputObjectsName));
            }            
        }
        
        return returnedRefs;
        
    }
    
    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();
        
        // Filter results are stored as a metadata item since they apply to the whole set
        if ((boolean) parameters.getValue(STORE_SUMMARY_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String childObjectsName = parameters.getValue(CHILD_OBJECTS);
            String referenceValue = parameters.getValue(REFERENCE_VALUE).toString();
            
            String metadataName = getMetadataName(inputObjectsName,filterMethod,childObjectsName,referenceValue);
            
            returnedRefs.add(metadataRefs.getOrPut(metadataName));
            
        }
        
        return returnedRefs;
    }
    
    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
