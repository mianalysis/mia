package wbif.sjx.MIA.Module.ObjectProcessing.Refinement;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Process.ColourFactory;

import java.util.HashMap;

/**
 * Created by sc13967 on 31/01/2018.
 */
public class MergeObjects extends Module {
    public static final String INPUT_OBJECTS_1 = "Input objects 1";
    public static final String INPUT_OBJECTS_2 = "Input objects 2";
    public static final String OUTPUT_OBJECTS = "Output objects";


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
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjects1Name = parameters.getValue(INPUT_OBJECTS_1);
        ObjCollection inputObjects1 = workspace.getObjectSet(inputObjects1Name);
        String inputObjects2Name = parameters.getValue(INPUT_OBJECTS_2);
        ObjCollection inputObjects2 = workspace.getObjectSet(inputObjects2Name);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

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

        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            outputObjects.convertObjectsToImage("Objects", null, hues, 8,false).getImagePlus().show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS_1,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS_2,this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
