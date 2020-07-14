package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import javax.annotation.Nullable;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;


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
            int xMin = Math.max((int) Math.floor(xCent - radius/dppXY),0);
            int xMax = Math.min((int) Math.ceil(xCent + radius/dppXY),inputObject.getWidth()-1);
            int yMin = Math.max((int) Math.floor(yCent - radius/dppXY),0);
            int yMax = Math.min((int) Math.ceil(yCent + radius/dppXY),inputObject.getHeight()-1);
            int zMin = Math.max((int) Math.floor(zCent - radius/dppZ),0);
            int zMax = Math.min((int) Math.ceil(zCent + radius/dppZ),inputObject.getNSlices()-1);

            for (int x=xMin; x<xMax; x++) {
                double xx = (xCent - x) * dppXY;

                for (int y=yMin; y<yMax; y++) {
                    double yy = (yCent - y) * dppXY;

                    if (inputObject.is2D()) {
                        if (Math.sqrt(xx*xx + yy*yy) < radius) {
                            try {
                                outputObject.add(x, y, 0);
                            } catch (PointOutOfRangeException e) {}
                        }
                    } else {
                        for (int z=zMin; z<zMax; z++) {
                            double zz = (zCent - z) * dppZ;
                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) {
                                try {
                                    outputObject.add(x, y, z);
                                } catch (PointOutOfRangeException e) {}
                            }
                        }
                    }
                }
            }
        } else {
            int xMin = Math.max((int) Math.floor(xCent - radius), 0);
            int xMax = Math.min((int) Math.ceil(xCent + radius), inputObject.getWidth()-1);
            int yMin = Math.max((int) Math.floor(yCent - radius), 0);
            int yMax = Math.min((int) Math.ceil(yCent + radius), inputObject.getHeight()-1);
            int zMin = Math.max((int) Math.floor(zCent - radius * xy_z_ratio),0);
            int zMax = Math.min((int) Math.ceil(zCent + radius * xy_z_ratio), inputObject.getNSlices()-1);

            for (int x=xMin; x<xMax; x++) {
                double xx = xCent - x;

                for (int y=yMin; y<yMax; y++) {
                    double yy = yCent - y;

                    if (inputObject.is2D()) {
                        if (Math.sqrt(xx*xx + yy*yy) < radius) {
                            try {
                                outputObject.add(x, y, 0);
                            } catch (PointOutOfRangeException e) {}
                        }
                    } else {
                        for (int z=zMin; z<zMax; z++) {
                            double zz = (zCent - z) / xy_z_ratio;
                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) {
                                try {
                                    outputObject.add(x, y, z);
                                } catch (PointOutOfRangeException e) {}
                            }
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
        ObjCollection outputObjects = new ObjCollection(outputObjectsName,inputObjects);

        if (inputObjects.values().size() == 0) return outputObjects;

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
    public Status process(Workspace workspace) {
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
            return Status.FAIL;
        }

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);
        writeStatus("Adding objects ("+outputObjectsName+") to workspace");

        return Status.PASS;

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

        if ((boolean) parameters.getValue(USE_MEASUREMENT)) {
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(INPUT_OBJECTS),parameters.getValue(OUTPUT_OBJECTS)));

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
