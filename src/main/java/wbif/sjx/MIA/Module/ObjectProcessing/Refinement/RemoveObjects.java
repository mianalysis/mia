package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import java.util.LinkedHashMap;

import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.RemovedObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class RemoveObjects extends Module {
    public static final String REMOVAL_SEPARATOR = "Objects to remove";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";
    public static final String REMOVE_ANOTHER_OBJECT_SET = "Remove another object set";

    public RemoveObjects(ModuleCollection modules) {
        super("Remove objects",modules);
    }



    @Override
    public Category getCategory() {
        return Categories.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "Removes the specified object set(s) from the workspace.  Doing this helps keep memory usage down.  Measurements associated with an object set can be retained for further use.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        ParameterGroup parameterGroup = parameters.getParameter(REMOVE_ANOTHER_OBJECT_SET);
        LinkedHashMap<Integer,ParameterCollection> collections = parameterGroup.getCollections(false);

        for (ParameterCollection collection : collections.values()) {
            String inputObjectsName = collection.getValue(INPUT_OBJECTS);
            boolean retainMeasurements = collection.getValue(RETAIN_MEASUREMENTS);

            // Removing the relevant object set from the workspace
            writeStatus("Removing objects (" + inputObjectsName + ") from workspace");
            workspace.removeObjects(inputObjectsName, retainMeasurements);

        }
        
        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(REMOVAL_SEPARATOR,this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new RemovedObjectsP(INPUT_OBJECTS,this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_OBJECT_SET, this, collection, 1));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
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

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        ParameterCollection collection = ((ParameterGroup) parameters.get(REMOVE_ANOTHER_OBJECT_SET))
                .getTemplateParameters();

        collection.get(INPUT_OBJECTS).setDescription("Name of the object set to be removed from the workspace.");

        collection.get(RETAIN_MEASUREMENTS).setDescription("Retain measurements for this object set, or remove everything.  When selected, the object coordinates will be removed, as this is typically where most memory us used, however any measurements associated with each object will be retained.");

        parameters.get(REMOVE_ANOTHER_OBJECT_SET).setDescription("Mark another object set from the workspace for removal.");

    }
}
