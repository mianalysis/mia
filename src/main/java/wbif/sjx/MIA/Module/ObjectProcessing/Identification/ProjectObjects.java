package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.LUTs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Projects xy coordinates into a single plane.  Duplicates of xy coordinates at different heights are removed.
 */
public class ProjectObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static Obj createProjection(Obj inputObject, String outputObjectsName, boolean is2D) throws IntegerOverflowException {
        ArrayList<Integer> x = inputObject.getXCoords();
        ArrayList<Integer> y = inputObject.getYCoords();

        // All coordinate pairs will be stored in a HashMap, which will prevent coordinate duplication.  The keys
        // will correspond to the 2D index, for which we need to know the maximum x coordinate.
        double maxX = -Double.MAX_VALUE;
        for (double currX : x) {
            if (currX > maxX) {
                maxX = currX;
            }
        }

        // Running through all coordinates, adding them to the HashMap
        HashMap<Double, Integer> projCoords = new HashMap<>();
        for (int i = 0; i < x.size(); i++) {
            Double key = y.get(i) * maxX + x.get(i);
            projCoords.put(key, i);
        }

        // Creating the new HCObject and assigning the parent-child relationship
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits,is2D);
        outputObject.addParent(inputObject);
        inputObject.addChild(outputObject);

        // Adding coordinates to the projected object
        for (Double key : projCoords.keySet()) {
            int i = projCoords.get(key);
            outputObject.addCoord(x.get(i),y.get(i),0);
        }
        outputObject.setT(inputObject.getT());

        return outputObject;

    }

    @Override
    public String getTitle() {
        return "Project objects";

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            Obj outputObject = null;
            try {
                outputObject = createProjection(inputObject,outputObjectsName, inputObject.is2D());
            } catch (IntegerOverflowException e) {
                return false;
            }
            outputObjects.put(outputObject.getID(),outputObject);
        }

        workspace.addObjects(outputObjects);

        // Showing objects
        if (showOutput) {
            HashMap<Integer,Float> hues = ColourFactory.getRandomHues(outputObjects);
            String mode = ConvertObjectsToImage.ColourModes.RANDOM_COLOUR;
            ImagePlus dispIpl = outputObjects.convertObjectsToImage("Objects",null,hues,8,false).getImagePlus();
            dispIpl.setLut(LUTs.Random(true));
            dispIpl.setPosition(1,1,1);
            dispIpl.updateChannelAndDraw();
            dispIpl.show();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

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
        RelationshipCollection relationships = new RelationshipCollection();

        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

        return relationships;

    }

}
