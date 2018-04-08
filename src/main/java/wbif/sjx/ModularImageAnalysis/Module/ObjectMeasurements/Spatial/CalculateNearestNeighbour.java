package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 22/06/2017.
 */
public class CalculateNearestNeighbour extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";
    public static final String PARENT_OBJECTS = "Parent objects";

    public interface Measurements {
        String NN_DISTANCE = "NN_DISTANCE";
        String NN_ID = "NN_ID";

    }

    @Override
    public String getTitle() {
        return "Calculate nearest neighbour";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace) {
        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);

        // Running through each object, calculating the nearest neighbour distance
        for (Obj inputObject:inputObjects.values()) {
            double minDist = Double.MAX_VALUE;
            Obj nearestNeighbour = null;

            // Getting the object centroid
            double xCent = inputObject.getXMean(true);
            double yCent = inputObject.getYMean(true);
            double zCent = inputObject.getZMean(true,true);

            if (calculateWithinParent) {
                Obj parentObject = inputObject.getParent(parentObjectsName);

                // Some objects may not have a parent
                if (parentObject != null) {
                    ObjCollection neighbourObjects = parentObject.getChildren(inputObjectsName);

                    for (Obj testObject : neighbourObjects.values()) {
                        if (testObject != inputObject) {
                            double xCentTest = testObject.getXMean(true);
                            double yCentTest = testObject.getYMean(true);
                            double zCentTest = testObject.getZMean(true,true);

                            double dist = Math.sqrt((xCentTest - xCent) * (xCentTest - xCent)
                                    + (yCentTest - yCent) * (yCentTest - yCent)
                                    + (zCentTest - zCent) * (zCentTest - zCent));

                            if (dist < minDist) {
                                minDist = dist;
                                nearestNeighbour = testObject;

                            }
                        }
                    }
                }

            } else {
                for (Obj testObject:inputObjects.values()) {
                    if (testObject != inputObject) {
                        double xCentTest = testObject.getXMean(true);
                        double yCentTest = testObject.getYMean(true);
                        double zCentTest = testObject.getZMean(true,true);

                        double dist = Math.sqrt((xCentTest - xCent) * (xCentTest - xCent)
                                + (yCentTest - yCent) * (yCentTest - yCent)
                                + (zCentTest - zCent) * (zCentTest - zCent));

                        if (dist < minDist) {
                            minDist = dist;
                            nearestNeighbour = testObject;

                        }
                    }
                }
            }

            // Adding details of the nearest neighbour to the input object's measurements
            if (nearestNeighbour != null) {
                inputObject.addMeasurement(new Measurement(Measurements.NN_ID, nearestNeighbour.getID()));
                inputObject.addMeasurement(new Measurement(Measurements.NN_DISTANCE, minDist));

            } else {
                inputObject.addMeasurement(new Measurement(Measurements.NN_ID, Double.NaN));
                inputObject.addMeasurement(new Measurement(Measurements.NN_DISTANCE, Double.NaN));

            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(CALCULATE_WITHIN_PARENT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(PARENT_OBJECTS, Parameter.PARENT_OBJECTS,null,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {
//        objectMeasurementReferences.add(new MeasurementReference(Measurements.NN_DISTANCE));
//        objectMeasurementReferences.add(new MeasurementReference(Measurements.NN_ID));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(CALCULATE_WITHIN_PARENT));

        if (parameters.getValue(CALCULATE_WITHIN_PARENT)) {
            returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(PARENT_OBJECTS,inputObjectsName);

        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference nnDistance = objectMeasurementReferences.get(Measurements.NN_DISTANCE);
        nnDistance.setImageObjName(inputObjectsName);

        MeasurementReference nnID = objectMeasurementReferences.get(Measurements.NN_ID);
        nnID.setImageObjName(inputObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}

