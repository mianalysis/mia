package wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects;

import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;

import java.util.ArrayList;
import java.util.Iterator;

public class FilterOnImageEdge extends CoreFilter {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String FILTER_MODE = "Filter mode";
    public static final String OUTPUT_FILTERED_OBJECTS = "Output (filtered) objects";

    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String INCLUDE_Z_POSITION = "Include Z-position";
    public static final String STORE_RESULTS = "Store filter results";

    public FilterOnImageEdge(ModuleCollection modules) {
        super("Remove on image edge",modules);
    }


    public String getMetadataName(String inputObjectsName, String referenceImagename, boolean includeZ) {
        if (includeZ) {
            return "FILTER // NUM_" + inputObjectsName + " TOUCHING " + referenceImagename + " EDGE (3D)";
        } else {
            return "FILTER // NUM_" + inputObjectsName + " TOUCHING " + referenceImagename + " EDGE (2D)";
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
    protected boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String filterMode = parameters.getValue(FILTER_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_FILTERED_OBJECTS);
        String inputImageName = parameters.getValue(REFERENCE_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        boolean includeZ = parameters.getValue(INCLUDE_Z_POSITION);
        boolean storeResults = parameters.getValue(STORE_RESULTS);

        boolean moveObjects = filterMode.equals(FilterModes.MOVE_FILTERED);
        boolean remove = !filterMode.equals(FilterModes.DO_NOTHING);

        ObjCollection outputObjects = moveObjects ? new ObjCollection(outputObjectsName) : null;

        int maxX = inputImage.getImagePlus().getWidth()-1;
        int maxY = inputImage.getImagePlus().getHeight()-1;
        int maxZ = inputImage.getImagePlus().getNSlices()-1;

        int count = 0;
        Iterator<Obj> iterator = inputObjects.values().iterator();
        while (iterator.hasNext()) {
            Obj inputObject = iterator.next();

            ArrayList<Integer> x = inputObject.getXCoords();
            ArrayList<Integer> y = inputObject.getYCoords();
            ArrayList<Integer> z = inputObject.getZCoords();

            for (int i=0;i<x.size();i++) {
                if (x.get(i) == 0 | x.get(i) == maxX | y.get(i) == 0 | y.get(i) == maxY) {
                    count++;
                    if (remove) processRemoval(inputObject,outputObjects,iterator);
                    break;
                }

                // Only consider Z if the user requested this
                if (includeZ && (z.get(i) == 0 | z.get(i) == maxZ)) {
                    count++;
                    if (remove) processRemoval(inputObject,outputObjects,iterator);
                    break;
                }
            }
        }

        // If moving objects, add them to the workspace
        if (moveObjects) workspace.addObjects(outputObjects);

        // If storing the result, create a new metadata item for it
        String metadataName = getMetadataName(inputObjectsName,inputImageName,includeZ);
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
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new BooleanP(INCLUDE_Z_POSITION,this,false));
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
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
        returnedParameters.add(parameters.getParameter(INCLUDE_Z_POSITION));
        returnedParameters.add(parameters.getParameter(STORE_RESULTS));

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

        return null;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        // Filter results are stored as a metadata item since they apply to the whole set
        if (parameters.getValue(STORE_RESULTS)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
            boolean includeZ = parameters.getValue(INCLUDE_Z_POSITION);

            String metadataName = getMetadataName(inputObjectsName,referenceImageName,includeZ);

            returnedRefs.add(metadataRefs.getOrPut(metadataName));

        }

        return returnedRefs;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
