package io.github.mianalysis.mia.module.objects.transform;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.ObjMetadataRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Creates an exact copy of objects. Can optionally also duplicate
 * relationships, measurements and metadata. Duplicated objects are entirely
 * separate from their original copies (i.e. don't share coordinate sets), so
 * changes made to the original after duplication won't be reflected in the
 * duplicate copy.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DuplicateObjects extends Module {

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
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String DUPLICATE_RELATIONSHIPS = "Copy relationships";

    public static final String DUPLICATE_MEASUREMENTS = "Copy measurements";

    public static final String DUPLICATE_METADATA = "Copy metadata";

    public static final String ADD_ORIGINAL_DUPLICATE_RELATIONSHIP = "Add relationship to original";

    public DuplicateObjects(Modules modules) {
        super("Duplicate objects", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_TRANSFORM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.1";
    }

    @Override
    public String getDescription() {
        return "Creates an exact copy of objects. Can optionally also duplicate relationships, measurements and metadata. Duplicated objects are entirely separate from their original copies (i.e. don't share coordinate sets), so changes made to the original after duplication won't be reflected in the duplicate copy.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean duplicateRelationships = parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace);
        boolean duplicateMeasurements = parameters.getValue(DUPLICATE_MEASUREMENTS, workspace);
        boolean duplicateMetadata = parameters.getValue(DUPLICATE_METADATA, workspace);
        boolean addOriginalDuplicateRelationship = parameters.getValue(ADD_ORIGINAL_DUPLICATE_RELATIONSHIP, workspace);

        Objs inputObjects = workspace.getObjects(inputObjectName);

        Objs outputObjects = inputObjects.duplicate(outputObjectsName, duplicateRelationships, duplicateMeasurements,
                duplicateMetadata, addOriginalDuplicateRelationship);
        workspace.addObjects(outputObjects);

        if (showOutput)
            outputObjects.convertToImageIDColours().show(false);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new BooleanP(DUPLICATE_RELATIONSHIPS, this, true));
        parameters.add(new BooleanP(DUPLICATE_MEASUREMENTS, this, true));
        parameters.add(new BooleanP(DUPLICATE_METADATA, this, true));
        parameters.add(new BooleanP(ADD_ORIGINAL_DUPLICATE_RELATIONSHIP, this, false));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParams = new Parameters();

        returnedParams.add(parameters.get(INPUT_SEPARATOR));
        returnedParams.add(parameters.get(INPUT_OBJECTS));

        returnedParams.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParams.add(parameters.get(OUTPUT_OBJECTS));
        returnedParams.add(parameters.get(DUPLICATE_RELATIONSHIPS));
        returnedParams.add(parameters.get(DUPLICATE_MEASUREMENTS));
        returnedParams.add(parameters.get(DUPLICATE_METADATA));
        returnedParams.add(parameters.get(ADD_ORIGINAL_DUPLICATE_RELATIONSHIP));

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;

        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        if ((boolean) parameters.getValue(DUPLICATE_MEASUREMENTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

            // Getting object measurement references associated with this object set
            ObjMeasurementRefs references = modules.getObjectMeasurementRefs(inputObjectsName, this);

            for (ObjMeasurementRef reference : references.values())
                returnedRefs.add(objectMeasurementRefs.getOrPut(reference.getName()).setObjectsName(outputObjectsName));

        }

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        Workspace workspace = null;

        ObjMetadataRefs returnedRefs = new ObjMetadataRefs();

        if ((boolean) parameters.getValue(DUPLICATE_METADATA, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

            // Getting object measurement references associated with this object set
            ObjMetadataRefs references = modules.getObjectMetadataRefs(inputObjectsName, this);

            for (ObjMetadataRef reference : references.values())
                returnedRefs.add(objectMetadataRefs.getOrPut(reference.getName()).setObjectsName(outputObjectsName));

        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;

        ParentChildRefs returnedRefs = new ParentChildRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace)) {
            // Getting references up to this location
            ParentChildRefs currentRefs = modules.getParentChildRefs(this);

            // Adding relationships where the input object is the parent
            String[] childNames = currentRefs.getChildNames(inputObjectsName, true);
            for (String childName : childNames)
                returnedRefs.add(parentChildRefs.getOrPut(outputObjectsName, childName));

            // Adding relationships where the input object is the child
            String[] parentNames = currentRefs.getParentNames(inputObjectsName, true);
            for (String parentName : parentNames)
                returnedRefs.add(parentChildRefs.getOrPut(parentName, outputObjectsName));

        }

        if ((boolean) parameters.getValue(ADD_ORIGINAL_DUPLICATE_RELATIONSHIP, workspace))
            returnedRefs.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

        return returnedRefs;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        Workspace workspace = null;

        PartnerRefs returnedRefs = new PartnerRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        if ((boolean) parameters.getValue(DUPLICATE_RELATIONSHIPS, workspace)) {
            // Getting references up to this location
            PartnerRefs currentRefs = modules.getPartnerRefs(this);

            String[] partnerNames = currentRefs.getPartnerNamesArray(inputObjectsName);
            for (String partnerName : partnerNames)
                returnedRefs.add(partnerRefs.getOrPut(outputObjectsName, partnerName));

        }

        return returnedRefs;

    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {

    }
}
