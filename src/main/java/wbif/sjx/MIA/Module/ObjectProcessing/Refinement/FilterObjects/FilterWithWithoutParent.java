package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import java.util.Iterator;
import java.util.LinkedHashMap;

import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParentObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;

public class FilterWithWithoutParent extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String STORE_RESULTS = "Store filter results";

    public FilterWithWithoutParent(ModuleCollection modules) {
        super("With / without parent",modules);
    }


    public interface FilterMethods {
        String WITH_PARENT = "Remove objects with parent";
        String WITHOUT_PARENT = "Remove objects without parent";

        String[] ALL = new String[]{WITH_PARENT,WITHOUT_PARENT};

    }


    public static String getMetadataName(String inputObjectsName, String filterMethod, String parentObjectsName) {
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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT_FILTER_OBJECTS;
    }

    @Override
    public String getDescription() {
        return "Filter an object collection based on the presence of a specific parent for each object.  Objects which do/don't have the relevant parent can be removed from the input collection, moved to another collection (and removed from the input collection) or simply counted (but retained in the input collection).  The number of objects failing the filter can be stored as a metadata value.";
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
        String parentObjectName = parameters.getValue(PARENT_OBJECT);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName,inputObjects) : null;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            LinkedHashMap<String,Obj> parents = inputObject.getParents(true);
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

        // If moving objects, addRef them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        if (storeResults) {
            String metadataName = getMetadataName(inputObjectsName, filterMethod, parentObjectName);
            workspace.getMetadata().put(metadataName, count);
        }

        // Showing objects
        if (showOutput) inputObjects.convertToImageRandomColours().showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        
        parameters.add(new ParamSeparatorP(FILTER_SEPARATOR,this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.WITH_PARENT, FilterMethods.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new BooleanP(STORE_RESULTS, this, false));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addAll(super.updateAndGetParameters());
        
        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
        returnedParameters.add(parameters.getParameter(STORE_RESULTS));
        ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);

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
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole set
        if ((boolean) parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String filterMethod = parameters.getValue(FILTER_METHOD);
            String parentObjectsName = parameters.getValue(PARENT_OBJECT);

            String metadataName = getMetadataName(inputObjectsName,filterMethod,parentObjectsName);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;

    }

    void addParameterDescriptions() {
        parameters.get(FILTER_METHOD).setDescription(
                "Controls whether objects are removed when a specific parent object is present or not:<br>"
        
                        + "<br>- \"" + FilterMethods.WITHOUT_PARENT + "\" Objects without the parent specified by \""
                        + PARENT_OBJECT + "\" are removed, counted or moved (depending on the \"" + FILTER_MODE
                        + "\" parameter).<br>"
        
                        + "<br>- \"" + FilterMethods.WITH_PARENT + "\" Objects with the parent specified by \""
                        + PARENT_OBJECT + "\" are removed, counted or moved (depending on the \"" + FILTER_MODE
                        + "\" parameter).<br>"
                        
                );

        parameters.get(PARENT_OBJECT).setDescription("Parent object to filter by.  The presence or absence of this relationship will determine which of the input objects are counted, removed or moved (depending on the \""+FILTER_MODE+"\" parameter).");

        String metadataName = getMetadataName("[inputObjectsName]", FilterMethods.WITHOUT_PARENT, "[parentObjectsName]");
        parameters.get(STORE_RESULTS).setDescription("When selected, the number of removed (or moved) objects is counted and stored as a metadata item (name in the format \""+metadataName+"\").");

    }
}
