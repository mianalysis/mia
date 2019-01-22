package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Object.Point;

import java.util.HashMap;

public class MeasureRelativeOrientation extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String ORIENTATION_MODE = "Orientation mode";
    public static final String ORIENTATION_IN_X_Y_MEASUREMENT = "Orientation in X/Y measurement";
    public static final String ORIENTATION_IN_XY_Z_MEASUREMENT = "Orientation in XY/Z measurement";
    public static final String MEASUREMENT_RANGE = "Measurement range";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String REFERENCE_OBJECTS = "Reference objects";
    public static final String OBJECT_CHOICE_MODE = "Object choice mode";


    public interface OrientationModes {
        String X_Y_PLANE = "Orientation in X-Y plane";
        String XY_Z_PLANE = "Orientation in XY-Z plane";
        String BOTH_X_Y_AND_XY_Z_PLANES = "Orientation in both X-Y and XY-Z planes";
        String FULL_3D = "Orientation in 3D";

        //        String[] ALL = new String[]{X_Y_PLANE,XY_Z_PLANE,BOTH_X_Y_AND_XY_Z_PLANES,FULL_3D};
        String[] ALL = new String[]{X_Y_PLANE};

    }

    public interface MeasurementRanges {
        String ZERO_NINETY = "0-90 degrees";
        String ZERO_ONE_EIGHTY = "0-180 degrees";

        String[] ALL = new String[]{ZERO_NINETY,ZERO_ONE_EIGHTY};

    }

    public interface ReferenceModes {
        String IMAGE_CENTRE = "Image centre";
        String OBJECT_CENTROID = "Object centroid";

        String[] ALL = new String[]{IMAGE_CENTRE, OBJECT_CENTROID};

    }

    public interface ObjectChoiceModes {
        String LARGEST_OBJECT = "Largest object";
        String SMALLEST_OBJECT = "Smallest object";

        String[] ALL = new String[]{LARGEST_OBJECT,SMALLEST_OBJECT};

    }


    public interface Measurements {
        String X_Y_REL_ORIENTATION = "X-Y PLANE RELATIVE TO \"${REFERENCE}\" (DEGS)";

    }


    String getMeasurementRef() {
        String reference = null;
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
                reference = referenceImageName+" IM_CENTRE";
                break;
            case ReferenceModes.OBJECT_CENTROID:
                String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                String choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                reference = referenceObjectsName+"_"+choice+"_OBJ_CENTROID";
                break;
        }

        return reference;

    }

    public static String getFullName(String measurement, String reference) {
        return "REL_ORIENTATION // "+measurement.replace("${REFERENCE}",reference);

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

    static HashMap<Integer,Point<Double>> getObjectCentroids(ObjCollection objects, String choiceMode, String orientationMode, int nFrames){
        HashMap<Integer,Point<Double>> centres = new HashMap<>();

        for (int t=0;t<nFrames;t++) {
            // Initialising the references for this timepoint
            Obj referenceObject = null;
            int objSize = 0;
            switch (choiceMode) {
                case ObjectChoiceModes.LARGEST_OBJECT:
                    objSize = Integer.MIN_VALUE;
                    break;
                case ObjectChoiceModes.SMALLEST_OBJECT:
                    objSize = Integer.MAX_VALUE;
                    break;
            }

            // Iterating over each object, checking it's size against the current reference values
            for (Obj object : objects.values()) {
                // Only check objects in the current frame
                if (object.getT() != t) continue;

                switch (choiceMode) {
                    case ObjectChoiceModes.LARGEST_OBJECT:
                        if (object.getNVoxels() > objSize) {
                            objSize = object.getNVoxels();
                            referenceObject = object;
                        }
                        break;
                    case ObjectChoiceModes.SMALLEST_OBJECT:
                        if (object.getNVoxels() < objSize) {
                            objSize = object.getNVoxels();
                            referenceObject = object;
                        }
                        break;
                }
            }

            // Getting reference object centroid
            if (referenceObject != null) {
                double x = referenceObject.getXMean(true);
                double y = referenceObject.getYMean(true);
                double z = referenceObject.getZMean(true, false);

                // Creating a new reference point and adding it to this timepoint
                Point<Double> referencePoint = new Point<>(x, y, z);
                centres.put(t, referencePoint);
            }
        }

        // Checking each frame has a reference.  For those that don't taking the closest reference in time.
        for (int t=0;t<nFrames;t++) {
            if (centres.get(t) != null) continue;

            int dt = Integer.MAX_VALUE;
            Point<Double> reference = null;
            for (int tt=0;tt<nFrames;tt++) {
                // Only proceed if there's a reference at this timepoint
                if (centres.get(tt) == null) continue;

                // Calculating the time gap to this frame and storing the object if it's the smallest gap yet
                if (Math.abs(t-tt) < dt) {
                    dt = Math.abs(t-tt);
                    reference = centres.get(tt);
                }
            }

            centres.put(t,reference);

        }

        return centres;

    }

    public static void processObject(Obj object, String xyOriMeasName, String xzOriMeasName, String measurementRange, Point<Double> referencePoint, String orientationMode, String measurementReference) {
        switch (orientationMode) {
            case OrientationModes.X_Y_PLANE:
                double xyOrientation = object.getMeasurement(xyOriMeasName).getValue();
                double xyAngle = getXYAngle(object,xyOrientation,measurementRange,referencePoint);

                // Adding the measurement
                String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION,measurementReference);
                object.addMeasurement(new Measurement(measurementName,xyAngle));

                break;
        }
    }

    static void assignMissingMeasurements(Obj object, String xyOriMeasName, String xzOriMeasName, String orientationMode, String measurementReference) {
        switch (orientationMode) {
            case OrientationModes.X_Y_PLANE:
                String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION,measurementReference);
                object.addMeasurement(new Measurement(measurementName,Double.NaN));

                break;
        }
    }

    public static double getXYAngle(Obj object, double xyOrientation, String measurementRange, Point<Double> referencePoint) {
        xyOrientation = Math.toRadians(xyOrientation);
        if (xyOrientation == -Math.PI) xyOrientation = Math.PI;
        double angleToReference = object.calculateAngle2D(referencePoint);

        double angle = xyOrientation - angleToReference;
        angle = Math.abs((angle + Math.PI) % (2*Math.PI) - Math.PI);

        // Putting it into the range -90 to +90 degrees (or radian equivalent)
        switch (measurementRange) {
            case MeasurementRanges.ZERO_NINETY:
                if (angle >= Math.PI / 2) angle = angle - Math.PI;
                break;

            case MeasurementRanges.ZERO_ONE_EIGHTY:
                if (angle >= Math.PI) angle = angle - 2*Math.PI;
                break;

        }

        // We are only interested in the deviation (i.e. absolute value)
        return Math.abs(Math.toDegrees(angle));

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
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting other parameters
        String orientationMode= parameters.getValue(ORIENTATION_MODE);
        String xyOriMeasName = parameters.getValue(ORIENTATION_IN_X_Y_MEASUREMENT);
        String xzOriMeasName = parameters.getValue(ORIENTATION_IN_XY_Z_MEASUREMENT);
        String measurementRange = parameters.getValue(MEASUREMENT_RANGE);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
        String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);

        // Getting measurement reference name
        String measurementReference = getMeasurementRef();

        // Get reference point as Point for each frame the input images are present for (frame number as HashMap key)
        HashMap<Integer,Point<Double>> referencePoints = null;
        switch (referenceMode) {
            case ReferenceModes.IMAGE_CENTRE:
                Image referenceImage = workspace.getImage(referenceImageName);
                referencePoints = getImageCentre(referenceImage,orientationMode);
                break;

            case ReferenceModes.OBJECT_CENTROID:
                int nFrames = inputObjects.getTemporalLimits()[1]+1;
                ObjCollection referenceObjects = workspace.getObjectSet(referenceObjectsName);
                referencePoints = getObjectCentroids(referenceObjects,objectChoiceMode,orientationMode,nFrames);
                break;
        }

        if (referencePoints == null) return true;

        // Processing each object
        for (Obj inputObject:inputObjects.values()) {
            int t = inputObject.getT();

            if (!referencePoints.containsKey(t)) {
                assignMissingMeasurements(inputObject, xyOriMeasName, xzOriMeasName, orientationMode, measurementReference);
            } else {
                Point<Double> referencePoint = referencePoints.get(t);
                processObject(inputObject, xyOriMeasName, xzOriMeasName, measurementRange, referencePoint, orientationMode, measurementReference);
            }
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ChoiceP(ORIENTATION_MODE,this,OrientationModes.X_Y_PLANE,OrientationModes.ALL));
        parameters.add(new ObjectMeasurementP(ORIENTATION_IN_X_Y_MEASUREMENT,this));
        parameters.add(new ObjectMeasurementP(ORIENTATION_IN_XY_Z_MEASUREMENT,this));
        parameters.add(new ChoiceP(MEASUREMENT_RANGE,this,MeasurementRanges.ZERO_NINETY,MeasurementRanges.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE,this,ReferenceModes.IMAGE_CENTRE,ReferenceModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE,this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS,this));
        parameters.add(new ChoiceP(OBJECT_CHOICE_MODE,this,ObjectChoiceModes.LARGEST_OBJECT,ObjectChoiceModes.ALL));

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
        ((ObjectMeasurementP) parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(MEASUREMENT_RANGE));

        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;

            case ReferenceModes.OBJECT_CENTROID:
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_CHOICE_MODE));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllCalculated(false);

        String inputObjectsName= parameters.getValue(INPUT_OBJECTS);

        String reference = getMeasurementRef();
        String referenceDescription = null;
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
                referenceDescription = "the centre of the image \""+referenceImageName+"\"";
                break;
            case ReferenceModes.OBJECT_CENTROID:
                String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                String choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                referenceDescription = "the centroid of the "+choice+" object in the set "+referenceObjectsName;
                break;
        }

        switch ((String) parameters.getValue(ORIENTATION_MODE)) {
            case OrientationModes.X_Y_PLANE:
                String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION,reference);
                MeasurementRef measurementReference = objectMeasurementRefs.getOrPut(measurementName);
                measurementReference.setImageObjName(inputObjectsName);
                measurementReference.setCalculated(true);

                String xyOriMeasName = parameters.getValue(ORIENTATION_IN_X_Y_MEASUREMENT);
                measurementReference.setDescription("Orientation of the object (specified by the measurements \""+
                        xyOriMeasName+"\") relative to "+referenceDescription+". Measured in degrees between 0 and 90.");
                break;
        }

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
