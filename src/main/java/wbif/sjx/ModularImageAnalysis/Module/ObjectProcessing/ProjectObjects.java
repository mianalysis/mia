package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.HCParameterCollection;

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
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCName outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);
        HCObjectSet outputObjects = new HCObjectSet(outputObjectsName);

        for (HCObject inputObject:inputObjects.values()) {
            ArrayList<Integer> x = inputObject.getCoordinates().get(HCObject.X);
            ArrayList<Integer> y = inputObject.getCoordinates().get(HCObject.Y);

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
            HCObject outputObject = new HCObject(inputObject.getID());
            outputObject.setParent(inputObject);
            inputObject.addChild(outputObjectsName,outputObject);

            // Adding coordinates to the projected object
            for (Double key : projCoords.keySet()) {
                int i = projCoords.get(key);
                outputObject.addCoordinate(HCObject.X,x.get(i));
                outputObject.addCoordinate(HCObject.Y,y.get(i));
                outputObject.addCoordinate(HCObject.Z,0);
            }

            // Copying additional dimensions from inputObject
            HashMap<Integer,Integer> positions = inputObject.getPositions();
            for (Map.Entry<Integer,Integer> entry:positions.entrySet()) {
                outputObject.setPosition(entry.getKey(),entry.getValue());
            }

            // Inheriting calibration from parent
            outputObject.addCalibration(HCObject.X,outputObject.getParent().getCalibration(HCObject.X));
            outputObject.addCalibration(HCObject.Y,outputObject.getParent().getCalibration(HCObject.Y));
            outputObject.addCalibration(HCObject.Z,outputObject.getParent().getCalibration(HCObject.Z));
            outputObject.setCalibratedUnits(outputObject.getParent().getCalibratedUnits());

            // Adding current object to object set
            outputObjects.put(outputObject.getID(),outputObject);

        }

        workspace.addObjects(outputObjects);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS, HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(OUTPUT_OBJECTS, HCParameter.OUTPUT_OBJECTS,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

    }
}
