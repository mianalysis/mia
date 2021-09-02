package io.github.mianalysis.mia.module.objectprocessing.refinement;

import java.util.LinkedHashMap;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.RemovedObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

/**
 * Created by sc13967 on 07/02/2018.
 */
public class RemoveObjects extends Module {
    public static final String REMOVAL_SEPARATOR = "Objects to remove";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";
    public static final String REMOVE_ANOTHER_OBJECT_SET = "Remove another object set";

    public RemoveObjects(Modules modules) {
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
        LinkedHashMap<Integer,Parameters> collections = parameterGroup.getCollections(false);

        for (Parameters collection : collections.values()) {
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

        Parameters collection = new Parameters();
        collection.add(new RemovedObjectsP(INPUT_OBJECTS,this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_OBJECT_SET, this, collection, 1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        Parameters collection = ((ParameterGroup) parameters.get(REMOVE_ANOTHER_OBJECT_SET))
                .getTemplateParameters();

        collection.get(INPUT_OBJECTS).setDescription("Name of the object set to be removed from the workspace.");

        collection.get(RETAIN_MEASUREMENTS).setDescription("Retain measurements for this object set, or remove everything.  When selected, the object coordinates will be removed, as this is typically where most memory us used, however any measurements associated with each object will be retained.");

        parameters.get(REMOVE_ANOTHER_OBJECT_SET).setDescription("Mark another object set from the workspace for removal.");

    }
}
