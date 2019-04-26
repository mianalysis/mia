package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import javax.annotation.Nullable;import ij.ImagePlus;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.common.Exceptions.IntegerOverflowException;


/**
 * Returns a spherical object around a point object.  This is useful for calculating local object features.
 */
public class GetLocalObjectRegion extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String LOCAL_RADIUS = "Local radius";
    public static final String CALIBRATED_RADIUS = "Calibrated radius";
    public static final String USE_MEASUREMENT = "Use measurement for radius";
    public static final String MEASUREMENT_NAME = "Measurement name";


    public static Obj getLocalRegion(Obj inputObject, String outputObjectsName, @Nullable ImagePlus referenceImage, double radius, boolean calibrated) throws IntegerOverflowException {
        // If no reference image is supplied, it's possible to have negative coordinates
        int xMin, xMax, yMin, yMax, zMin, zMax;
        if (referenceImage == null) {
            xMin = -Integer.MAX_VALUE;
            xMax = Integer.MAX_VALUE;
            yMin = -Integer.MAX_VALUE;
            yMax = Integer.MAX_VALUE;
            zMin = -Integer.MAX_VALUE;
            zMax = Integer.MAX_VALUE;
        } else {
            xMin = 0;
            xMax = referenceImage.getWidth()-1;
            yMin = 0;
            yMax = referenceImage.getHeight()-1;
            zMin = 0;
            zMax = referenceImage.getNSlices()-1;
        }

        // Getting spatial calibration
        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String calibratedUnits = inputObject.getCalibratedUnits();
        double xy_z_ratio = dppXY/dppZ;

        // Creating new object and assigning relationship to input objects
        Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits,inputObject.is2D());

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
                        if (Math.sqrt(xx*xx + yy*yy) < radius) outputObject.addCoord(x, y, 0);

                    } else {
                        for (int z = zMin; z <= zMin; z++) {
                            double zz = (zCent - z) * dppZ;
                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) outputObject.addCoord(x, y, z);
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
                        if (Math.sqrt(xx*xx + yy*yy) < radius) outputObject.addCoord(x, y, 0);

                    } else {
                        for (int z = zMin; z <= zMax; z++) {
                            double zz = (zCent - z) / xy_z_ratio;

                            if (Math.sqrt(xx*xx + yy*yy +  zz*zz) < radius) outputObject.addCoord(x, y, z);
                        }
                    }
                }
            }
        }

        // Copying timepoint of input object
        outputObject.setT(inputObject.getT());

        // Adding relationships
        outputObject.addParent(inputObject);
        inputObject.addChild(outputObject);

        return outputObject;

    }

    public ObjCollection getLocalRegions(ObjCollection inputObjects, String outputObjectsName, ImagePlus referenceImage, boolean useMeasurement, String measurementName, double radius, boolean calibrated) throws IntegerOverflowException {
        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        if (inputObjects.values().size() == 0) return outputObjects;

        int count = 0;
        int startingNumber = inputObjects.size();
        // Running through each object, calculating the local texture
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Calculating for object " + (++count) + " of " + startingNumber);

            if (useMeasurement) radius = inputObject.getMeasurement(measurementName).getValue();
            Obj outputObject = getLocalRegion(inputObject,outputObjectsName,referenceImage,radius,calibrated);

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
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(REFERENCE_IMAGE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT);
        String measurementName = parameters.getValue(MEASUREMENT_NAME);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);
        double radius = parameters.getValue(LOCAL_RADIUS);

        ImagePlus referenceImage = workspace.getImage(inputImageName).getImagePlus();

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection outputObjects = null;
        try {
            outputObjects = getLocalRegions(inputObjects,outputObjectsName,referenceImage,useMeasurement,measurementName,radius,calibrated);
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
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
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
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
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
