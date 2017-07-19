package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.ParameterCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Projects xy coordinates into a single plane.  Duplicates of xy coordinates at different heights are removed.
 */
public class ProjectObjects extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";


    @Override
    public String getTitle() {
        return "Project objects";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjSet outputObjects = new ObjSet(outputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getCoordinates().get(Obj.X);
            ArrayList<Integer> y = inputObject.getCoordinates().get(Obj.Y);

            // All coordinate pairs will be stored in a HashMap, which will prevent coordinate duplication.  The keys will
            // correspond to the 2D index, for which we need to know the maximum x coordinate
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
            Obj outputObject = new Obj(outputObjectsName,inputObject.getID());
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);

            // Adding coordinates to the projected object
            for (Double key : projCoords.keySet()) {
                int i = projCoords.get(key);
                outputObject.addCoordinate(Obj.X,x.get(i));
                outputObject.addCoordinate(Obj.Y,y.get(i));
                outputObject.addCoordinate(Obj.Z,0);
            }

            // Copying additional dimensions from inputObject
            HashMap<Integer,Integer> positions = inputObject.getPositions();
            for (Map.Entry<Integer,Integer> entry:positions.entrySet()) {
                outputObject.setPosition(entry.getKey(),entry.getValue());
            }

            // Inheriting calibration from parent
            outputObject.addCalibration(Obj.X,outputObject.getParent(inputObjectsName).getCalibration(Obj.X));
            outputObject.addCalibration(Obj.Y,outputObject.getParent(inputObjectsName).getCalibration(Obj.Y));
            outputObject.addCalibration(Obj.Z,outputObject.getParent(inputObjectsName).getCalibration(Obj.Z));
            outputObject.setCalibratedUnits(outputObject.getParent(inputObjectsName).getCalibratedUnits());

            // Adding current object to object set
            outputObjects.put(outputObject.getID(),outputObject);

        }

        workspace.addObjects(outputObjects);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

    }
}
