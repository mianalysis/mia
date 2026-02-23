package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.metadata.ObjMetadataFactories;
import io.github.mianalysis.mia.object.metadata.ObjMetadataI;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AdoptParentMetadata extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String PARENT_OBJECT = "Parent objects";

    /**
    * 
    */
    public static final String METADATA_SEPARATOR = "Metadata items";

    /**
    * 
    */
    public static final String ADD_METADATA_ITEM = "Add metadata item";

    /**
    * 
    */
    public static final String METADATA_ITEM = "Metadata item";

    public static String getFullName(String parentObjectName, String metadataItem) {
        return "PARENT_STATS // " + parentObjectName + " // [" + metadataItem + "]";
    }

    public AdoptParentMetadata(ModulesI modules) {
        super("Adopt parent metadata", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String objectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_METADATA_ITEM, workspace);

        ObjsI objects = workspace.getObjects(objectName);

        if (objects == null)
            return Status.PASS;

        int count = 0;
        int total = objects.size();
        for (ObjI obj : objects.values()) {
            count++;

            ObjI parentObj = obj.getParent(parentObjectsName);
            if (parentObj == null)
                continue;

            for (Parameters collection : collections.values()) {
                String metadataItemName = collection.getValue(METADATA_ITEM, workspace);
                ObjMetadataI parentMetadataItem = parentObj.getMetadataItem(metadataItemName);
                if (parentMetadataItem == null)
                    continue;

                obj.addMetadataItem(ObjMetadataFactories.getDefaultFactory().createMetadata(
                        getFullName(parentObjectsName, metadataItemName), parentMetadataItem.getValue()));

            }

            writeProgressStatus(count, total, "objects");

        }

        if (showOutput)
            objects.showMetadata(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));

        parameters.add(new SeparatorP(METADATA_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new ObjectMetadataP(METADATA_ITEM, this));
        parameters.add(new ParameterGroup(ADD_METADATA_ITEM, this, collection));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECT));

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((ParentObjectsP) parameters.get(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(METADATA_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_METADATA_ITEM));

        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);
        ParameterGroup parameterGroup = parameters.getParameter(ADD_METADATA_ITEM);
        for (Parameters collection : parameterGroup.getCollections(true).values())
            ((ObjectMetadataP) collection.get(METADATA_ITEM)).setObjectName(parentObjectsName);

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        WorkspaceI workspace = null;
        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT, workspace);

        ParameterGroup parameterGroup = parameters.getParameter(ADD_METADATA_ITEM);
        for (Parameters collection : parameterGroup.getCollections(true).values()) {
            String metadataItemName = collection.getValue(METADATA_ITEM, workspace);

            ObjMetadataRef ref = objectMetadataRefs.getOrPut(getFullName(parentObjectsName, metadataItemName));
            ref.setObjectsName(inputObjectsName);
            returnedRefs.add(ref);

        }

        return returnedRefs;

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

    }
}
