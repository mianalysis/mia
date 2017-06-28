package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 22/06/2017.
 */
public class CalculateNearestNeighbour extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String CALCULATE_WITHIN_PARENT = "Only calculate for objects in same parent";
    public static final String PARENT_OBJECTS = "Parent objects";

    private static final String NN_DISTANCE = "NN_DISTANCE";
    private static final String NN_ID = "NN_ID";

    @Override
    public String getTitle() {
        return "Calculate nearest neighbour";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting objects to measure
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean calculateWithinParent = parameters.getValue(CALCULATE_WITHIN_PARENT);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);

        // Running through each object, calculating the nearest neighbour distance
        for (HCObject inputObject:inputObjects.values()) {
            double minDist = Double.MAX_VALUE;
            HCObject nearestNeighbour = null;

            // Getting the object centroid
            double xCent = inputObject.getCentroid(HCObject.X);
            double yCent = inputObject.getCentroid(HCObject.Y);
            double zCent = inputObject.getCentroid(HCObject.Z);

            if (calculateWithinParent) {
                HCObject parentObject = inputObject.getParent(parentObjectsName);
                HCObjectSet neighbourObjects = parentObject.getChildren(inputObjectsName);

                for (HCObject testObject:neighbourObjects.values()) {
                    if (testObject != inputObject) {
                        double xCentTest = testObject.getCentroid(HCObject.X);
                        double yCentTest = testObject.getCentroid(HCObject.Y);
                        double zCentTest = testObject.getCentroid(HCObject.Z);

                        double dist = Math.sqrt((xCentTest - xCent) * (xCentTest - xCent)
                                + (yCentTest - yCent) * (yCentTest - yCent)
                                + (zCentTest - zCent) * (zCentTest - zCent));

                        if (dist < minDist) {
                            minDist = dist;
                            nearestNeighbour = testObject;

                        }
                    }
                }

            } else {
                for (HCObject testObject:inputObjects.values()) {
                    if (testObject != inputObject) {
                        double xCentTest = testObject.getCentroid(HCObject.X);
                        double yCentTest = testObject.getCentroid(HCObject.Y);
                        double zCentTest = testObject.getCentroid(HCObject.Z);

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
                inputObject.addMeasurement(new HCMeasurement(NN_ID, nearestNeighbour.getID()));
                inputObject.addMeasurement(new HCMeasurement(NN_DISTANCE, minDist));

            } else {
                inputObject.addMeasurement(new HCMeasurement(NN_ID, Double.NaN));
                inputObject.addMeasurement(new HCMeasurement(NN_DISTANCE, Double.NaN));

            }
        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(CALCULATE_WITHIN_PARENT,HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(PARENT_OBJECTS,HCParameter.PARENT_OBJECTS,null,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(CALCULATE_WITHIN_PARENT));

        if (parameters.getValue(CALCULATE_WITHIN_PARENT)) {
            returnedParameters.addParameter(parameters.getParameter(PARENT_OBJECTS));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueRange(PARENT_OBJECTS,inputObjectsName);

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        measurements.addMeasurement(inputObjectsName,NN_DISTANCE);
        measurements.addMeasurement(inputObjectsName,NN_ID);

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}

