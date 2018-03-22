package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Projects xy coordinates into a single plane.  Duplicates of xy coordinates at different heights are removed.
 */
public class ProjectObjects extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static Obj createProjection(Obj inputObject, String outputObjectsName) {
        ArrayList<Integer> x = inputObject.getXCoords();
        ArrayList<Integer> y = inputObject.getYCoords();

        // All coordinate pairs will be stored in a HashMap, which will prevent coordinate duplication.  The keys
        // will correspond to the 2D index, for which we need to know the maximum x coordinate.
        double maxX = Double.MIN_VALUE;
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
        Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits);
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
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            Obj outputObject = createProjection(inputObject,outputObjectsName);
            outputObjects.put(outputObject.getID(),outputObject);

        }

        workspace.addObjects(outputObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
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
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

    }
}
