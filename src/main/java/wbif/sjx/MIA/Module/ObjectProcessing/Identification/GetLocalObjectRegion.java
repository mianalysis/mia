package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import javax.annotation.Nullable;import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;


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

    public GetLocalObjectRegion(ModuleCollection modules) {
        super("Get local object region",modules);
    }


    public static Obj getLocalRegion(Obj inputObject, String outputObjectsName, double radius, boolean calibrated, boolean addRelationship) throws IntegerOverflowException {
        // If no reference image is supplied, it's possible to have negative coordinates
        int xMin = 0;
        int xMax = Integer.MAX_VALUE;
        int yMin = 0;
        int yMax = Integer.MAX_VALUE;
        int zMin = 0;
        int zMax = Integer.MAX_VALUE;

        // Getting spatial calibration
        double dppXY = inputObject.getDppXY();
        double dppZ = inputObject.getDppZ();
        double xy_z_ratio = dppXY/dppZ;

        // Creating new object and assigning relationship to input objects
        Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),inputObject);

        // Getting centroid coordinates
        double xCent = inputObject.getXMean(true);
        double yCent = inputObject.getYMean(true);
        double zCent = inputObject.getZMean(true,false);

        if (calibrated) {
            xMin = Math.max((int) Math.floor(xCent - radius/dppXY), xMin);
            xMax = Math.min((int) Math.ceil(xCent + radius/dppXY), xMax);
            yMin = Math.max((int) Math.floor(yCent - radius/dppXY), yMin);
            yMax = Math.min((int) Math.ceil(yCent + radius/dppXY), yMax);
            zMin = Math.max((int) Math.floor(zCent - radius/dppZ),zMin);
            zMax = Math.min((int) Math.ceil(zCent + radius/dppZ), zMax);

            for (int x = xMin; x <= xMax; x++) {
                double xx = (xCent - x) * dppXY;

                for (int y = yMin; y <= yMax; y++) {
                    double yy = (yCent - y) * dppXY;

                    if (inputObject.is2D()) {
                        if (Math.sqrt(xx*xx + yy*yy) < radius) outputObject.add(x, y, 0);

                    } else {
                        for (int z = zMin; z <= zMax; z++) {
                            double zz = (zCent - z) * dppZ;
                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) outputObject.add(x, y, z);
                        }
                    }
                }
            }
        } else {
            xMin = Math.max((int) Math.floor(xCent - radius), xMin);
            xMax = Math.min((int) Math.ceil(xCent + radius), xMax);
            yMin = Math.max((int) Math.floor(yCent - radius), yMin);
            yMax = Math.min((int) Math.ceil(yCent + radius), yMax);
            zMin = Math.max((int) Math.floor(zCent - radius * xy_z_ratio),zMin);
            zMax = Math.min((int) Math.ceil(zCent + radius * xy_z_ratio), zMax);

            for (int x = xMin; x <= xMax; x++) {
                double xx = xCent - x;

                for (int y = yMin; y <= yMax; y++) {
                    double yy = yCent - y;

                    if (inputObject.is2D()) {
                        if (Math.sqrt(xx*xx + yy*yy) < radius) outputObject.add(x, y, 0);

                    } else {
                        for (int z = zMin; z <= zMax; z++) {
                            double zz = (zCent - z) / xy_z_ratio;

                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) outputObject.add(x, y, z);
                        }
                    }
                }
            }
        }

        // Copying timepoint of input object
        outputObject.setT(inputObject.getT());

        // If adding relationships
        if (addRelationship) {
            outputObject.addParent(inputObject);
            inputObject.addChild(outputObject);
        }

        return outputObject;

    }

    public static ObjCollection getLocalRegions(ObjCollection inputObjects, String outputObjectsName, @Nullable String measurementName, double radius, boolean calibrated, boolean addRelationship) throws IntegerOverflowException {
        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        if (inputObjects.values().size() == 0) return outputObjects;

        int count = 0;
        int startingNumber = inputObjects.size();
        // Running through each object, calculating the local texture
        for (Obj inputObject:inputObjects.values()) {
            if (measurementName != null) radius = inputObject.getMeasurement(measurementName).getValue();
            Obj outputObject = getLocalRegion(inputObject,outputObjectsName,radius,calibrated,addRelationship);

            // Adding object to HashMap
            outputObjects.put(outputObject.getID(),outputObject);

        }

        return outputObjects;

    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT);
        String measurementName = parameters.getValue(MEASUREMENT_NAME);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);
        double radius = parameters.getValue(LOCAL_RADIUS);

        if (!useMeasurement) measurementName = null;

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection outputObjects = null;
        try {
            outputObjects = getLocalRegions(inputObjects,outputObjectsName,measurementName,radius,calibrated,true);
        } catch (IntegerOverflowException e) {
            return false;
        }

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new DoubleP(LOCAL_RADIUS, this,10.0));
        parameters.add(new BooleanP(CALIBRATED_RADIUS, this,false));
        parameters.add(new BooleanP(USE_MEASUREMENT, this,false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(USE_MEASUREMENT));

        if (parameters.getValue(USE_MEASUREMENT)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME));
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_NAME)).setObjectName(inputObjectsName);
        } else {
            returnedParameters.add(parameters.getParameter(LOCAL_RADIUS));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATED_RADIUS));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        RelationshipRefCollection returnedRelationships = new RelationshipRefCollection();

        returnedRelationships.add(relationshipRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public boolean verify() {
        return true;
    }
}
