package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 31/01/2018.
 */
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

    public CombineObjectSets(ModuleCollection modules) {
        super("Combine object sets", modules);
    }

    public static void combineAndCreate(ObjCollection inputObjects, ObjCollection outputObjects) {
        for (Obj obj : inputObjects.values()) {
            int ID = outputObjects.getAndIncrementID();
            Obj newObj = new Obj(outputObjects.getName(), ID, obj);
            newObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
            newObj.setT(obj.getT());
            outputObjects.add(obj);
        }
    }

    static void combineAndAdd(ObjCollection targetObjects, ObjCollection sourceObjects) {
        for (Obj obj : sourceObjects.values()) {
            int ID = targetObjects.getAndIncrementID();
            Obj newObj = new Obj(targetObjects.getName(), ID, obj);
            newObj.setCoordinateSet(obj.getCoordinateSet().duplicate());
            newObj.setT(obj.getT());
            targetObjects.add(obj);
        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);
        String outputMode = parameters.getValue(OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Doing object merging
        switch (outputMode) {
            case OutputModes.ADD_TO_OBJECTS_1:
                combineAndAdd(inputObjects1, inputObjects2);

                if (showOutput)
                    inputObjects1.convertToImageRandomColours().showImage();

                break;

            case OutputModes.ADD_TO_OBJECTS_2:
                combineAndAdd(inputObjects2, inputObjects1);

                if (showOutput)
                    inputObjects2.convertToImageRandomColours().showImage();

                break;

            case OutputModes.CREATE_NEW:
                ObjCollection outputObjects = new ObjCollection(outputObjectsName, inputObjects1);
                combineAndCreate(inputObjects1, outputObjects);
                combineAndCreate(inputObjects2, outputObjects);

                // Adding the combined objects to the workspace
                workspace.addObjects(outputObjects);

                if (showOutput)
                    outputObjects.convertToImageRandomColours().showImage();

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
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_OBJECTS_1));
        returnedParameters.add(parameters.get(INPUT_OBJECTS_2));

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_MODE));
        switch ((String) parameters.getValue(OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW:
                returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
                break;
        }

        return returnedParameters;

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
        // parameters.get(INPUT_OBJECTS_1).setDescription();

        // parameters.get(INPUT_OBJECTS_2).setDescription();

        // parameters.get(OUTPUT_MODE).setDescription();

        // parameters.get(OUTPUT_OBJECTS).setDescription();

    }
}
