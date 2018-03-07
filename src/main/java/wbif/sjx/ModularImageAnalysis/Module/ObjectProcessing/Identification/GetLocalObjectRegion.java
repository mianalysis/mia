package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;


/**
 * Returns a spherical object around a point object.  This is useful for calculating local object features.
 */
public class GetLocalObjectRegion extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";
    public static final String USE_MEASUREMENT = "Use measurement for radius";
    public static final String MEASUREMENT_NAME = "Measurement name";

    public static ObjCollection getLocalRegions(ObjCollection inputObjects, String outputObjectsName, double radius, boolean calibrated, boolean useMeasurement, String measurementName) {
        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        if (inputObjects.values().size() == 0) return outputObjects;

        double dppXY = inputObjects.values().iterator().next().getDistPerPxXY();
        double dppZ = inputObjects.values().iterator().next().getDistPerPxZ();
        String calibratedUnits = inputObjects.values().iterator().next().getCalibratedUnits();
        double xy_z_ratio = dppXY/dppZ;

        // Running through each object, calculating the local texture
        for (Obj inputObject:inputObjects.values()) {
            // Creating new object and assigning relationship to input objects
            Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits);

            // Getting centroid coordinates
            double xCent = inputObject.getXMean(true);
            double yCent = inputObject.getYMean(true);
            double zCent = inputObject.getZMean(true,false);

            if (useMeasurement) radius = inputObject.getMeasurement(measurementName).getValue();

            if (calibrated) {
                for (int x = (int) Math.floor(xCent - radius/dppXY); x <= (int) Math.ceil(xCent + radius/dppXY); x++) {
                    for (int y = (int) Math.floor(yCent - radius/dppXY); y <= (int) Math.ceil(yCent + radius/dppXY); y++) {
                        for (int z = (int) Math.floor(zCent - radius/dppZ); z <= (int) Math.ceil(zCent + radius/dppZ); z++) {
                            if (Math.sqrt((xCent-x)*dppXY*(xCent-x)*dppXY + (yCent-y)*dppXY*(yCent-y)*dppXY + (zCent-z)*dppZ*(zCent-z)*dppZ) < radius) {
                                outputObject.addCoord(x,y,z);

                            }
                        }
                    }
                }

            } else {
                for (int x = (int) Math.floor(xCent - radius); x <= (int) Math.ceil(xCent + radius); x++) {
                    for (int y = (int) Math.floor(yCent - radius); y <= (int) Math.ceil(yCent + radius); y++) {
                        for (int z = (int) Math.floor(zCent - radius * xy_z_ratio); z <= (int) Math.ceil(zCent + radius * xy_z_ratio); z++) {
                            if (Math.sqrt((xCent-x)*(xCent-x) + (yCent-y)*(yCent-y) + (zCent-z)*(zCent-z)/(xy_z_ratio*xy_z_ratio)) < radius) {
                                outputObject.addCoord(x,y,z);

                            }
                        }
                    }
                }
            }

            // Copying timepoint of input object
            outputObject.setT(inputObject.getT());

            // Adding object to HashMap
            outputObjects.put(outputObject.getID(),outputObject);

            // Adding relationships
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);

        }

        return outputObjects;

    }

    @Override
    public String getTitle() {
        return "Get local object region";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);
        double radius = parameters.getValue(LOCAL_RADIUS);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT);
        String measurementName = parameters.getValue(MEASUREMENT_NAME);

        if (verbose) System.out.println("["+moduleName+"] Using local radius of "+radius+" px");
        if (verbose) System.out.println("["+moduleName+"] Using local radius of "+radius+" ");

        // Getting local region
        ObjCollection outputObjects = getLocalRegions(inputObjects, outputObjectsName, radius, calibrated,useMeasurement,measurementName);

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(LOCAL_RADIUS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(USE_MEASUREMENT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT_NAME, Parameter.OBJECT_MEASUREMENT,null,null));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(USE_MEASUREMENT));

        if (parameters.getValue(USE_MEASUREMENT)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME));
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(MEASUREMENT_NAME,inputObjectsName);
        } else {
            returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));

        }

        returnedParameters.add(parameters.getParameter(CALIBRATED_RADIUS));

        return returnedParameters;

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
