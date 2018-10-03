package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class MeasureRelativeOrientation extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String ORIENTATION_MODE = "Orientation mode";
    public static final String ORIENTATION_IN_X_Y_MEASUREMENT = "Orientation in X/Y measurement";
    public static final String ORIENTATION_IN_XY_Z_MEASUREMENT = "Orientation in XY/Z measurement";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String MEASUREMENT_OBJECTS = "Measurement objects";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String OBJECT_CHOICE_MODE = "Object choice mode";


    public interface OrientationModes {
        String X_Y_PLANE = "Orientation in X-Y plane";
        String XY_Z_PLANE = "Orientation in XY-Z plane";
        String BOTH_X_Y_AND_XY_Z_PLANES = "Orientation in both X-Y and XY-Z planes";
        String FULL_3D = "Orientation in 3D";

//        String[] ALL = new String[]{X_Y_PLANE,XY_Z_PLANE,BOTH_X_Y_AND_XY_Z_PLANES,FULL_3D};
        String[] ALL = new String[]{X_Y_PLANE};

    }

    public interface ReferenceModes {
        String IMAGE_CENTRE = "Image centre";
        String POSITION_MEASUREMENTS = "Position measurements";

        String[] ALL = new String[]{IMAGE_CENTRE,POSITION_MEASUREMENTS};

    }

    public interface ObjectChoiceModes {
        String LARGEST_OBJECT = "Largest object";
        String SMALLEST_OBJECT = "Smallest object";

        String[] ALL = new String[]{LARGEST_OBJECT,SMALLEST_OBJECT};

    }

    public interface Measurements {
        String X_Y_REL_ORIENTATION = "REL_ORIENTATION // X-Y_PLANE (DEGREES)";
        String XY_Z_REL_ORIENTATION = "REL_ORIENTATION // XY-Z_PLANE (DEGREES)";

    }


    static HashMap<Integer,Point<Double>> getImageCentre(Image image, String orientationMode) {
        boolean useZ = !orientationMode.equals(OrientationModes.X_Y_PLANE);
        double width = image.getImagePlus().getWidth();
        double height = image.getImagePlus().getHeight();
        double nSlices = image.getImagePlus().getNSlices();
        double nFrames = image.getImagePlus().getNFrames();

        double xc = width/2 - 0.5;
        double yc = height/2 - 0.5;
        double zc = useZ ? nSlices/2 - 0.5 : 0;

        HashMap<Integer,Point<Double>> centres = new HashMap<>();
        for (int i=0;i<nFrames;i++) centres.put(i,new Point<>(xc,yc,zc));

        return centres;

    }

    static ArrayList<Point<Double>> getObjectCentroid(ObjCollection objects, String choiceMode, String xMeas, String yMeas, String zMeas, String orientationMode) {

        // NEEDS TO GET CENTROID ACROSS ALL FRAMES.  IF THERE ISN'T A REFERENCE OBJECT IN A PARTIULAR FRAME, TAKE THE
        // CLOSEST POINT IN TIME.  IF THERE ARE NO REFERENCE OBJECTS, RETURN DOUBLE.NAN VALUES FOR MEASUREMENTS.
//        switch (choiceMode) {
//            case ObjectChoiceModes.LARGEST_OBJECT:
//                int objSize = Integer.MIN_VALUE;
//                Obj largestObject = null;
//                for (Obj object:objects.values()) {
//                    if (object.getNVoxels() > objSize) {
//                        objSize = object.getNVoxels();
//                        largestObject = object;
//                    }
//                }
//                return new Point<Double>();
//            case ObjectChoiceModes.SMALLEST_OBJECT:
//
//        }

        return null;

    }

    public static void processObject(Obj object, String xyOriMeasName, String xzOriMeasName, Point<Double> referencePoint, String orientationMode) {
        switch (orientationMode) {
            case OrientationModes.X_Y_PLANE:
                double xyOrientation = object.getMeasurement(xyOriMeasName).getValue();
                double xyAngle = getXYAngle(object,xyOrientation,referencePoint);
                // Now add it as a measurement
                break;
        }
    }

    public static double getXYAngle(Obj object, double xyOrientation, Point<Double> referencePoint) {
//        System.err.println("Input orientation "+xyOrientation);
////        if (xyOrientation < 0) xyOrientation = xyOrientation + 180;
////
////        // Converting xyOrientation to 0-180 range
////        if (xyOrientation < 0) xyOrientation =
//
////         We have no idea of the absolute object direction, so want it between -90 and 90
//        xyOrientation = xyOrientation%180;
//
////         Get angle between object and reference point
//        double angleToReference = Math.toDegrees(object.calculateAngle2D(referencePoint));
//        if (angleToReference > 90) angleToReference = angleToReference%180-180;
//
//        double relativeOrientation = (xyOrientation-angleToReference);
//        if (Math.abs(relativeOrientation) != 90) relativeOrientation = (xyOrientation-angleToReference)%90;
//
//        relativeOrientation = Math.abs(relativeOrientation);
//
//        System.err.println("    Orientation = "+xyOrientation+", angle to reference = "+angleToReference);
//
//        // Calculating angle between orientation and vector from object to reference
//        return relativeOrientation;

        xyOrientation = Math.toRadians(xyOrientation + 180);
        double angleToReference = object.calculateAngle2D(referencePoint)+Math.PI;
        double rel = Math.toDegrees(Math.atan2(Math.sin(xyOrientation-angleToReference),Math.cos(xyOrientation-angleToReference)));

        if (Math.abs(Math.abs(rel)-90) > 1E-10) rel = rel%90;
        rel = Math.abs(rel);
        return rel;

    }


    @Override
    public String getTitle() {
        return "Measure relative orientation";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return "Currently only works for X-Y plane measurements";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting other parameters
        String orientationMode= parameters.getValue(ORIENTATION_MODE);
        String xyOriMeasName = parameters.getValue(ORIENTATION_IN_X_Y_MEASUREMENT);
        String xzOriMeasName = parameters.getValue(ORIENTATION_IN_XY_Z_MEASUREMENT);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String measurementObjectsName = parameters.getValue(MEASUREMENT_OBJECTS);
        String xPosMeasurementName = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeasurementName = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeasurementName = parameters.getValue(Z_POSITION_MEASUREMENT);
        String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);

        // Get reference point as Point for each frame the input images are present for (frame number as HashMap key)
        HashMap<Integer,Point<Double>> referencePoints = null;
        switch (referenceMode) {
            case ReferenceModes.IMAGE_CENTRE:
                Image referenceImage = workspace.getImage(referenceImageName);
                referencePoints = getImageCentre(referenceImage,orientationMode);
                break;

            case ReferenceModes.POSITION_MEASUREMENTS:

                break;
        }

        if (referencePoints == null) return;

        // Processing each object
        for (Obj inputObject:inputObjects.values()) {
            int t = inputObject.getT();
            Point<Double> referencePoint = referencePoints.get(t);
        }
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(ORIENTATION_MODE,Parameter.CHOICE_ARRAY,OrientationModes.X_Y_PLANE,OrientationModes.ALL));
        parameters.add(new Parameter(ORIENTATION_IN_X_Y_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(ORIENTATION_IN_XY_Z_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(REFERENCE_MODE,Parameter.CHOICE_ARRAY,ReferenceModes.IMAGE_CENTRE,ReferenceModes.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(X_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(Y_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(Z_POSITION_MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(OBJECT_CHOICE_MODE,Parameter.CHOICE_ARRAY,ObjectChoiceModes.LARGEST_OBJECT,ObjectChoiceModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
        switch ((String) parameters.getValue(ORIENTATION_MODE)) {
            case OrientationModes.X_Y_PLANE:
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
                break;

            case OrientationModes.XY_Z_PLANE:
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT));
                break;

            case OrientationModes.BOTH_X_Y_AND_XY_Z_PLANES:
            case OrientationModes.FULL_3D:
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT));
                break;
        }

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        parameters.updateValueSource(ORIENTATION_IN_X_Y_MEASUREMENT,inputObjectsName);
        parameters.updateValueSource(ORIENTATION_IN_XY_Z_MEASUREMENT,inputObjectsName);

        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;

            case ReferenceModes.POSITION_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_OBJECTS));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));

                switch ((String) parameters.getValue(ORIENTATION_MODE)) {
                    case OrientationModes.XY_Z_PLANE:
                    case OrientationModes.BOTH_X_Y_AND_XY_Z_PLANES:
                    case OrientationModes.FULL_3D:
                        returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));
                        break;
                }

                String measurementObjectName = parameters.getValue(MEASUREMENT_OBJECTS);
                parameters.updateValueSource(X_POSITION_MEASUREMENT,measurementObjectName);
                parameters.updateValueSource(Y_POSITION_MEASUREMENT,measurementObjectName);
                parameters.updateValueSource(Z_POSITION_MEASUREMENT,measurementObjectName);

                returnedParameters.add(parameters.getParameter(OBJECT_CHOICE_MODE));
                break;
        }

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

    }
}
