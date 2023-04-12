package io.github.mianalysis.mia.module.objects.relate.mergeobjects;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 31/01/2018.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CombineObjectSets extends Module {
    public static final String INPUT_SEPARATOR = "Objects input";
    public static final String INPUT_OBJECTS_1 = "Input objects 1";
    public static final String INPUT_OBJECTS_2 = "Input objects 2";

    public static final String OUTPUT_SEPARATOR = "Objects output";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public interface OutputModes {
        String ADD_TO_OBJECTS_1 = "Add set objects 2 to set 1";
        String ADD_TO_OBJECTS_2 = "Add set objects 1 to set 2";
        String CREATE_NEW = "Create new object set";

        String[] ALL = new String[] { ADD_TO_OBJECTS_1, ADD_TO_OBJECTS_2, CREATE_NEW };

    }

    public CombineObjectSets(Modules modules) {
        super("Combine object sets", modules);
    }

    public static void addObjects(Objs targetObjects, Objs sourceObjects) {
        for (Obj obj : sourceObjects.values()) {
            Obj newObj = targetObjects.createAndAddNewObject(obj.getVolumeType());
            newObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
            newObj.setT(obj.getT());

            // Transferring parents
            LinkedHashMap<String, Obj> parents = obj.getParents(false);
            newObj.setParents(parents);
            for (Obj parent : parents.values())
                parent.addChild(newObj);

            // Transferring children
            LinkedHashMap<String, Objs> children = obj.getChildren();
            newObj.setChildren(children);
            for (Objs childSet : children.values())
                for (Obj child : childSet.values())
                    child.addParent(newObj);

            // Transferring partners
            LinkedHashMap<String, Objs> partners = obj.getPartners();
            newObj.setPartners(partners);
            for (Objs partnerSet : partners.values())
                for (Obj partner : partnerSet.values())
                    partner.addPartner(newObj);

            // Transferring measurements
            LinkedHashMap<String, Measurement> measurements = obj.getMeasurements();
            newObj.setMeasurements(measurements);

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_RELATE_MERGE;
    }

    @Override
    public String getDescription() {
        return "Combines the objects from two collections stored in the workspace.  Either the objects from one collection can be added to the other or they can both be combined into a new collection, which is added to the workspace.<br><br>Note: Any objects added to another collection (either the \"other\" object collection or to a new collection) are duplicates of the original objects.  These duplicates contain the same spatial and temporal information as well as any relationship connections and measurements.  The original objects are unaffected by this module.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1, workspace);
        Objs inputObjects1 = workspace.getObjects(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2, workspace);
        Objs inputObjects2 = workspace.getObjects(inputObjects2Name);
        String outputMode = parameters.getValue(OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);

        // Doing object merging
        switch (outputMode) {
            case OutputModes.ADD_TO_OBJECTS_1:
                addObjects(inputObjects1, inputObjects2);

                if (showOutput)
                    inputObjects1.convertToImageIDColours().show();

                break;

            case OutputModes.ADD_TO_OBJECTS_2:
                addObjects(inputObjects2, inputObjects1);

                if (showOutput)
                    inputObjects2.convertToImageIDColours().show();

                break;

            case OutputModes.CREATE_NEW:
                Objs outputObjects = new Objs(outputObjectsName, inputObjects1);
                addObjects(inputObjects1, outputObjects);
                addObjects(inputObjects2, outputObjects);

                // Adding the combined objects to the workspace
                workspace.addObjects(outputObjects);

                if (showOutput)
                    outputObjects.convertToImageIDColours().show();

                break;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_1, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_2, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CREATE_NEW, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_OBJECTS_1));
        returnedParameters.add(parameters.get(INPUT_OBJECTS_2));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW:
                returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
                break;
        }

        return returnedParameters;

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
        parameters.get(INPUT_OBJECTS_1)
                .setDescription("First of two object collections to combine.  Depending on the choice for parameter \""
                        + OUTPUT_MODE
                        + "\", this collection may be updated to include the objects from the second collection (\""
                        + INPUT_OBJECTS_2 + "\").");

        parameters.get(INPUT_OBJECTS_2)
                .setDescription("Second of two object collections to combine.  Depending on the choice for parameter \""
                        + OUTPUT_MODE
                        + "\", this collection may be updated to include the objects from the first collection (\""
                        + INPUT_OBJECTS_2 + "\").");

        parameters.get(OUTPUT_MODE).setDescription("Controls where the combined object collections are stored:<br><ul>"

                + "<li>\"" + OutputModes.ADD_TO_OBJECTS_1 + "\" Duplicates of all objects in the second collection (\""
                + INPUT_OBJECTS_2 + "\") are made and added to the first collection (\"" + INPUT_OBJECTS_1 + "\").</li>"

                + "<li>\"" + OutputModes.ADD_TO_OBJECTS_2 + "\" Duplicates of all objects in the first collection (\""
                + INPUT_OBJECTS_1 + "\") are made and added to the second collection (\"" + INPUT_OBJECTS_1
                + "\").</li>"

                + "<li>\"" + OutputModes.CREATE_NEW + "\". Duplicates of all objects in the first (\"" + INPUT_OBJECTS_1
                + "\") and second (\"" + INPUT_OBJECTS_2
                + "\") collections are made and added to a new collection with name specified by \"" + OUTPUT_OBJECTS
                + "\"</li></ul>");

        parameters.get(OUTPUT_OBJECTS)
                .setDescription("Name of the combined output collection to be added to the workspace if \""
                        + OUTPUT_MODE + "\" is set to \"" + OutputModes.CREATE_NEW + "\".");

    }
}
