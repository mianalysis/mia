package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import ij.ImagePlus;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;


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

    public ObjCollection getLocalRegions(ObjCollection inputObjects, ImagePlus referenceImage) {
        // Getting output objects name
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        boolean calibrated = parameters.getValue(CALIBRATED_RADIUS);
        double radius = parameters.getValue(LOCAL_RADIUS);
        boolean useMeasurement = parameters.getValue(USE_MEASUREMENT);
        String measurementName = parameters.getValue(MEASUREMENT_NAME);

        // Creating store for output objects
        ObjCollection outputObjects = new ObjCollection(outputObjectsName);

        // Getting image range
        int width = referenceImage.getWidth();
        int height = referenceImage.getHeight();
        int nChannels = referenceImage.getNChannels();
        int nSlices = referenceImage.getNSlices();
        int nFrames = referenceImage.getNFrames();

        if (inputObjects.values().size() == 0) return outputObjects;

        double dppXY = inputObjects.values().iterator().next().getDistPerPxXY();
        double dppZ = inputObjects.values().iterator().next().getDistPerPxZ();
        String calibratedUnits = inputObjects.values().iterator().next().getCalibratedUnits();
        double xy_z_ratio = dppXY/dppZ;

        int count = 0;
        int startingNumber = inputObjects.size();
        // Running through each object, calculating the local texture
        for (Obj inputObject:inputObjects.values()) {
            writeMessage("Calculating for object " + (++count) + " of " + startingNumber);
            // Creating new object and assigning relationship to input objects
            Obj outputObject = new Obj(outputObjectsName,inputObject.getID(),dppXY,dppZ,calibratedUnits,inputObject.is2D());

            // Getting centroid coordinates
            double xCent = inputObject.getXMean(true);
            double yCent = inputObject.getYMean(true);
            double zCent = inputObject.getZMean(true,false);

            if (useMeasurement) radius = inputObject.getMeasurement(measurementName).getValue();

            if (calibrated) {
                int xMin = Math.max((int) Math.floor(xCent - radius/dppXY), 0);
                int xMax = Math.min((int) Math.ceil(xCent + radius/dppXY), width-1);
                int yMin = Math.max((int) Math.floor(yCent - radius/dppXY), 0);
                int yMax = Math.min((int) Math.ceil(yCent + radius/dppXY), height-1);
                int zMin = Math.max((int) Math.floor(zCent - radius/dppZ),0);
                int zMax = Math.min((int) Math.ceil(zCent + radius/dppZ), nSlices-1);

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
                int xMin = Math.max((int) Math.floor(xCent - radius), 0);
                int xMax = Math.min((int) Math.ceil(xCent + radius), width-1);
                int yMin = Math.max((int) Math.floor(yCent - radius), 0);
                int yMax = Math.min((int) Math.ceil(yCent + radius), height-1);
                int zMin = Math.max((int) Math.floor(zCent - radius * xy_z_ratio),0);
                int zMax = Math.min((int) Math.ceil(zCent + radius * xy_z_ratio), nSlices-1);

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
    public void run(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(REFERENCE_IMAGE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ImagePlus referenceImage = workspace.getImage(inputImageName).getImagePlus();

        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
        ObjCollection outputObjects = getLocalRegions(inputObjects,referenceImage);

        // Adding output objects to workspace
        workspace.addObjects(outputObjects);
        writeMessage("Adding objects ("+outputObjectsName+") to workspace");

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(OUTPUT_OBJECTS, Parameter.OUTPUT_OBJECTS,null));
        parameters.add(new Parameter(REFERENCE_IMAGE, Parameter.INPUT_IMAGE, null));
        parameters.add(new Parameter(LOCAL_RADIUS, Parameter.DOUBLE,10.0));
        parameters.add(new Parameter(CALIBRATED_RADIUS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(USE_MEASUREMENT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(MEASUREMENT_NAME, Parameter.OBJECT_MEASUREMENT,null,null));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
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
