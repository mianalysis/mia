package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.util.HashMap;
import java.util.Map.Entry;


/**
 * Returns a spherical object around a point object.  This is useful for calculating local object features.
 */
public class GetLocalObjectRegion extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";

    public static ObjSet getLocalRegions(ObjSet inputObjects, String outputObjectsName, double radius, boolean calibrated) {
        // Creating store for output objects
        ObjSet outputObjects = new ObjSet(outputObjectsName);

        double dppXY = inputObjects.values().iterator().next().getDistPerPxXY();
        double dppZ = inputObjects.values().iterator().next().getDistPerPxZ();
        String calibratedUnits = inputObjects.values().iterator().next().getCalibratedUnits();

        // Running through each object, calculating the local texture
        for (Obj inputObject:inputObjects.values()) {
            // Creating new object and assigning relationship to input objects
            Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits);
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);

            double xy_z_ratio = dppXY/dppZ;

            // Getting centroid coordinates
            double xCent = inputObject.getXMean(true);
            double yCent = inputObject.getYMean(true);
            double zCent = inputObject.getZMean(true,false);

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
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        // Getting parameters
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);
        double radius = parameters.getValue(LOCAL_RADIUS);
        if (verbose) System.out.println("["+moduleName+"] Using local radius of "+radius+" px");
        if (verbose) System.out.println("["+moduleName+"] Using local radius of "+radius+" ");

        // Getting local region
        ObjSet outputObjects = getLocalRegions(inputObjects, outputObjectsName, radius, calibrated);

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName+") to workspace");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(LOCAL_RADIUS, Parameter.DOUBLE,10.0));
        parameters.addParameter(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;
    }

    @Override
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        relationships.addRelationship(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS));

    }
}
