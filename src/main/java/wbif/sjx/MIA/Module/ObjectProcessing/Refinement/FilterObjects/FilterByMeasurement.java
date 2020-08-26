package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

public class FilterByMeasurement extends AbstractObjectFilter {    
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String MEASUREMENT = "Measurement to filter on";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REFERENCE_VAL_IMAGE = "Reference value image";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_VAL_PARENT_OBJECT = "Reference value parent object";
    public static final String REFERENCE_OBJECT_MEASUREMENT = "Reference object measurement";
    public static final String REFERENCE_MULTIPLIER = "Reference value multiplier";
    
    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";
    
    
    public FilterByMeasurement(ModuleCollection modules) {
        super("Based on measurement",modules);
    }
    
    
    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";
        
        String[] ALL = new String[]{FIXED_VALUE,IMAGE_MEASUREMENT,PARENT_OBJECT_MEASUREMENT};
        
    }
    
    
    public String getIndividualFixedValueFullName(String filterMethod, String measName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + measName + " " + filterMethodSymbol + " " + referenceValue;
    }
    
    public String getSummaryFixedValueFullName(String inputObjectsName, String filterMethod, String measName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + measName + " " + filterMethodSymbol + " " + referenceValue;
    }
    
    public String getIndividualImageMeasFullName(String filterMethod, String measName, String imageName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + imageName + " " + measName + " " + filterMethodSymbol + " " + refName;
    }
    
    public String getSummaryImageMeasFullName(String inputObjectsName, String filterMethod, String measName, String imageName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + imageName + " " + measName + " " + filterMethodSymbol + " " + refName;
    }
    
    public String getIndividualParentMeasFullName(String filterMethod, String measName, String parentName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + parentName + " " + measName + " " + filterMethodSymbol + " " + refName;
    }
    
    public String getSummaryParentMeasFullName(String inputObjectsName, String filterMethod, String measName, String parentName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + parentName + " " + measName + " " + filterMethodSymbol + " " + refName;
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
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        double fixedValue = parameters.getValue(REFERENCE_VALUE);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
        String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);
        double refMultiplier = parameters.getValue(REFERENCE_MULTIPLIER);
        boolean storeSummary = parameters.getValue(STORE_SUMMARY_RESULTS);
        boolean storeIndividual = parameters.getValue(STORE_INDIVIDUAL_RESULTS);
        
        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);
        
        
        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName,inputObjects) : null;
        
        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();
            
            // Removing the object if it has no children
            Measurement measurement = inputObject.getMeasurement(measName);
            if (measurement == null) continue;
            
            // Getting the values to filter on
            double value = measurement.getValue();
            double refValue;
            switch (referenceMode) {
                case ReferenceModes.FIXED_VALUE:
                refValue = fixedValue;
                refMultiplier = 1;
                break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                refValue = workspace.getImage(refImage).getMeasurement(refImageMeas).getValue();
                break;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                Obj parentObject = inputObject.getParent(refParent);
                if (parentObject == null) continue;
                refValue = parentObject.getMeasurement(refParentMeas).getValue();
                break;
                default:
                return Status.FAIL;
            }
            refValue = refValue*refMultiplier;
            
            // Checking for blank measurements
            if (Double.isNaN(refValue) || Double.isNaN(value)) continue;
                       
            // Checking the main filter
            boolean conditionMet = testFilter(value,refValue,filterMethod);

            // Adding measurements
            if (storeIndividual) {
                String measurementName = "";
                switch (referenceMode) {
                    case ReferenceModes.FIXED_VALUE:
                    measurementName = getIndividualFixedValueFullName(filterMethod,measName,String.valueOf(fixedValue));
                    break;
                    case ReferenceModes.IMAGE_MEASUREMENT:
                    measurementName = getIndividualImageMeasFullName(filterMethod,measName,refImage,refImageMeas);
                    break;
                    case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                    measurementName = getIndividualParentMeasFullName(filterMethod,measName,refParent,refParentMeas);
                    break;
                }
                inputObject.addMeasurement(new Measurement(measurementName,conditionMet ? 1 : 0));
            }

            if (conditionMet) {
                count++;
                if (remove) processRemoval(inputObject,outputObjects,iterator);
            }
        }
        
        // If moving objects, addRef them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);
        
        // If storing the result, create a new metadata item for it
        if (storeSummary) {
            String metadataName = "";
            switch (referenceMode) {
                case ReferenceModes.FIXED_VALUE:
                metadataName = getSummaryFixedValueFullName(inputObjectsName,filterMethod,measName,String.valueOf(fixedValue));
                break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                metadataName = getSummaryImageMeasFullName(inputObjectsName,filterMethod,measName,refImage,refImageMeas);
                break;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                metadataName = getSummaryParentMeasFullName(inputObjectsName,filterMethod,measName,refParent,refParentMeas);
                break;
            }
            workspace.getMetadata().put(metadataName,count);
        }
        
        // Showing objects
        if (showOutput) inputObjects.convertToImageRandomColours().showImage();
        
        return Status.PASS;
        
    }
    
    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        
        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIXED_VALUE,ReferenceModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this,1d));
        parameters.add(new InputImageP(REFERENCE_VAL_IMAGE, this));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(REFERENCE_VAL_PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_OBJECT_MEASUREMENT, this));
        parameters.add(new DoubleP(REFERENCE_MULTIPLIER, this, 1d));
        
        parameters.add(new ParamSeparatorP(MEASUREMENT_SEPARATOR,this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));
        
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
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.FIXED_VALUE:
            returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
            break;
            
            case ReferenceModes.IMAGE_MEASUREMENT:
            returnedParameters.add(parameters.getParameter(REFERENCE_VAL_IMAGE));
            returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
            String referenceValueImageName = parameters.getValue(REFERENCE_VAL_IMAGE);
            ((ImageMeasurementP) parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT)).setImageName(referenceValueImageName);
            break;
            
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
            returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
            returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
            String referenceValueParentObjectsName = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
            ((ParentObjectsP) parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
            ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT)).setObjectName(referenceValueParentObjectsName);
            break;
        }
        
        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));
        
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
            String referenceMode = parameters.getValue(REFERENCE_MODE);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String measName = parameters.getValue(MEASUREMENT);
            double fixedValue = parameters.getValue(REFERENCE_VALUE);
            String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
            String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
            String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
            String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);
            
            String measurementName = "";
            switch (referenceMode) {
                case ReferenceModes.FIXED_VALUE:
                measurementName = getIndividualFixedValueFullName(filterMethod,measName,String.valueOf(fixedValue));
                break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                measurementName = getIndividualImageMeasFullName(filterMethod,measName,refImage,refImageMeas);
                break;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                measurementName = getIndividualParentMeasFullName(filterMethod,measName,refParent,refParentMeas);
                break;
            }
            
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
            String referenceMode = parameters.getValue(REFERENCE_MODE);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String measName = parameters.getValue(MEASUREMENT);
            double fixedValue = parameters.getValue(REFERENCE_VALUE);
            String refImage = parameters.getValue(REFERENCE_VAL_IMAGE);
            String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT);
            String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT);
            String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT);
            
            String metadataName = "";
            switch (referenceMode) {
                case ReferenceModes.FIXED_VALUE:
                metadataName = getSummaryFixedValueFullName(inputObjectsName,filterMethod,measName,String.valueOf(fixedValue));
                break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                metadataName = getSummaryImageMeasFullName(inputObjectsName,filterMethod,measName,refImage,refImageMeas);
                break;
                case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                metadataName = getSummaryParentMeasFullName(inputObjectsName,filterMethod,measName,refParent,refParentMeas);
                break;
            }
            
            returnedRefs.add(metadataRefs.getOrPut(metadataName));
            
        }
        
        return returnedRefs;
        
    }
}
