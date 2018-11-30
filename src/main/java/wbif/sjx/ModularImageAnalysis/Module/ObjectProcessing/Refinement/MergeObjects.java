package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;

import java.util.HashMap;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class MergeObjects extends Module {
    public static final String INPUT_OBJECTS_1 = "Input objects 1";
    public static final String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String DELETE_INPUTS = "Remove input objects";

    @Override
    public String getTitle() {
        return "Merge objects";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_REFINEMENT;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting parameters
        boolean deleteInputs = parameters.getValue(DELETE_INPUTS);

        // Doing object merging
        for (Obj obj1:inputObjects1.values()) {
            int ID = outputObjects.getNextID();
            double distXY = obj1.getDistPerPxXY();
            double distZ = obj1.getDistPerPxZ();
            String units = obj1.getCalibratedUnits();
            boolean twoD = obj1.is2D();

            Obj newObj = new Obj(outputObjectsName,ID,distXY,distZ,units,twoD);
            newObj.setPoints(obj1.getPoints());
            newObj.setT(obj1.getT());
            outputObjects.add(obj1);

        }

        for (Obj obj2:inputObjects2.values()) {
            int ID = outputObjects.getNextID();
            double distXY = obj2.getDistPerPxXY();
            double distZ = obj2.getDistPerPxZ();
            String units = obj2.getCalibratedUnits();
            boolean twoD = obj2.is2D();

            Obj newObj = new Obj(outputObjectsName,ID,distXY,distZ,units,twoD);
            newObj.setPoints(obj2.getPoints());
            newObj.setT(obj2.getT());
            outputObjects.add(obj2);

        }

        // Adding the combined objects to the workspace
        workspace.addObjects(outputObjects);

        // Removing the relevant image from the workspace
        if (deleteInputs) {
            workspace.removeObject(inputObjects1Name);
            workspace.removeObject(inputObjects2Name);
        }

        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            outputObjects.convertObjectsToImage("Objects", null, hues, 8).getImagePlus().show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS_1,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(INPUT_OBJECTS_2,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS,Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(DELETE_INPUTS,Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        boolean deleteInputs = parameters.getValue(DELETE_INPUTS);

        if (deleteInputs) {
            parameters.getParameter(INPUT_OBJECTS_1).setType(Parameter.REMOVED_OBJECTS);
            parameters.getParameter(INPUT_OBJECTS_2).setType(Parameter.REMOVED_OBJECTS);
        } else {
            parameters.getParameter(INPUT_OBJECTS_1).setType(Parameter.INPUT_OBJECTS);
            parameters.getParameter(INPUT_OBJECTS_2).setType(Parameter.INPUT_OBJECTS);
        }

        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
