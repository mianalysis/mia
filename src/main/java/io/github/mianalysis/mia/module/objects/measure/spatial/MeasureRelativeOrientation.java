package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.sjcommon.analysis.volume.SurfaceSeparationCalculator;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.Volume;
import io.github.sjcross.sjcommon.object.volume.VolumeType;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureRelativeOrientation extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String CALCULATION_SEPARATOR = "Calculation controls";
    // public static final String ORIENTATION_MODE = "Orientation mode";
    public static final String ORIENTATION_IN_X_Y_MEASUREMENT = "Orientation in X/Y measurement";
    // public static final String ORIENTATION_IN_XY_Z_MEASUREMENT = "Orientation in
    // XY/Z measurement";
    public static final String MEASUREMENT_RANGE = "Measurement range";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_OBJECTS = "Reference objects";
    public static final String OBJECT_CHOICE_MODE = "Object choice mode";
    public static final String MUST_BE_SAME_FRAME = "Reference must be in same frame";

    public MeasureRelativeOrientation(Modules modules) {
        super("Measure relative orientation", modules);
    }

    public interface OrientationModes {
        String X_Y_PLANE = "Orientation in X-Y plane";
        // String XY_Z_PLANE = "Orientation in XY-Z plane";
        // String BOTH_X_Y_AND_XY_Z_PLANES = "Orientation in both X-Y and XY-Z planes";
        // String FULL_3D = "Orientation in 3D";

        // String[] ALL = new
        // String[]{X_Y_PLANE,XY_Z_PLANE,BOTH_X_Y_AND_XY_Z_PLANES,FULL_3D};
        String[] ALL = new String[] { X_Y_PLANE };

    }

    public interface MeasurementRanges {
        String ZERO_NINETY = "0-90 degrees";
        String ZERO_ONE_EIGHTY = "0-180 degrees";

        String[] ALL = new String[] { ZERO_NINETY, ZERO_ONE_EIGHTY };

    }

    public interface ReferenceModes {
        String IMAGE_CENTRE = "Object centroid to image centre";
        String OBJECT_CENTROID = "Object centroid to target centroid";
        String OBJECT_SURFACE = "Object centroid to target surface";

        String[] ALL = new String[] { IMAGE_CENTRE, OBJECT_CENTROID, OBJECT_SURFACE };

    }

    public interface ObjectChoiceModes {
        String LARGEST_OBJECT = "Largest object";
        String SMALLEST_OBJECT = "Smallest object";

        String[] ALL = new String[] { LARGEST_OBJECT, SMALLEST_OBJECT };

    }

    public interface Measurements {
        String X_Y_REL_ORIENTATION = "X-Y PLANE RELATIVE TO \"${REFERENCE}\" (DEGS)";

    }

    String getMeasurementRef() {
        String reference = null;
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                reference = "IM_CENTRE";
                break;
            case ReferenceModes.OBJECT_CENTROID:
                String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                String choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                reference = referenceObjectsName + "_" + choice + "_OBJ_CENTROID";
                break;
            case ReferenceModes.OBJECT_SURFACE:
                referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                reference = referenceObjectsName + "_" + choice + "_OBJ_SURFACE";
                break;
        }

        return reference;

    }

    public static String getFullName(String measurement, String reference) {
        return "REL_ORIENTATION // " + measurement.replace("${REFERENCE}", reference);

    }

    static HashMap<Integer, Point<Double>> getImageCentreRefs(SpatCal cal, int nFrames, String orientationMode) {
        boolean useZ = !orientationMode.equals(OrientationModes.X_Y_PLANE);
        double width = cal.getWidth();
        double height = cal.getHeight();
        double nSlices = cal.getNSlices();

        double xc = width / 2 - 0.5;
        double yc = height / 2 - 0.5;
        double zc = useZ ? nSlices / 2 - 0.5 : 0;

        HashMap<Integer, Point<Double>> centres = new HashMap<>();
        for (int i = 0; i < nFrames; i++)
            centres.put(i, new Point<>(xc, yc, zc));

        return centres;

    }

    static Obj getReferenceObject(Objs objects, int t, String choiceMode) {
        switch (choiceMode) {
            default:
                return null;
            case ObjectChoiceModes.LARGEST_OBJECT:
                return objects.getLargestObject(t);
            case ObjectChoiceModes.SMALLEST_OBJECT:
                return objects.getSmallestObject(t);
        }
    }

    static HashMap<Integer, Point<Double>> getObjectCentroidRefs(Objs objects, String choiceMode,
            String orientationMode, int nFrames, boolean mustBeSameFrame) {
        HashMap<Integer, Point<Double>> centres = new HashMap<>();

        if (mustBeSameFrame) {
            for (int t = 0; t < nFrames; t++) {
                // Initialising the references for this timepoint
                Obj referenceObject = getReferenceObject(objects, t, choiceMode);

                if (referenceObject != null) {
                    double x = referenceObject.getXMean(true);
                    double y = referenceObject.getYMean(true);
                    double z = referenceObject.getZMean(true, false);

                    // Creating a new reference point and adding it to this timepoint
                    Point<Double> referencePoint = new Point<>(x, y, z);
                    centres.put(t, referencePoint);

                }
            }

        } else {
            // Initialising the references for this timepoint
            Obj referenceObject = getReferenceObject(objects, -1, choiceMode);

            // Getting reference object centroid
            if (referenceObject != null) {
                double x = referenceObject.getXMean(true);
                double y = referenceObject.getYMean(true);
                double z = referenceObject.getZMean(true, false);

                // Creating a new reference point and adding it to this timepoint
                Point<Double> referencePoint = new Point<>(x, y, z);
                for (int t = 0; t < nFrames; t++)
                    centres.put(t, referencePoint);

            }
        }

        // Checking each frame has a reference. For those that don't taking the closest
        // reference in time.
        for (int t = 0; t < nFrames; t++) {
            if (centres.get(t) != null)
                continue;

            int dt = Integer.MAX_VALUE;
            Point<Double> reference = null;
            for (int tt = 0; tt < nFrames; tt++) {
                // Only proceed if there's a reference at this timepoint
                if (centres.get(tt) == null)
                    continue;

                // Calculating the time gap to this frame and storing the object if it's the
                // smallest gap yet
                if (Math.abs(t - tt) < dt) {
                    dt = Math.abs(t - tt);
                    reference = centres.get(tt);
                }
            }

            centres.put(t, reference);

        }

        return centres;

    }

    static HashMap<Integer, Point<Double>> getObjectCentroidSurfaceRefs(Objs inputObjects, Objs referenceObjects,
            String choiceMode, String orientationMode, int nFrames, boolean mustBeSameFrame) {
        HashMap<Integer, Point<Double>> centres = new HashMap<>();

        for (Obj inputObject : inputObjects.values()) {
            int t = inputObject.getT();

            // If frame doesn't matter, t is set to -1
            if (!mustBeSameFrame)
                t = -1;

            // Initialising the references for this timepoint
            Obj referenceObject = getReferenceObject(referenceObjects, t, choiceMode);

            if (referenceObject != null) {
                // Get the centroid of the current object
                int x1 = (int) inputObject.getXMean(true);
                int y1 = (int) inputObject.getYMean(true);
                int z1 = (int) inputObject.getZMean(true, false);

                Volume centroidVol = new Volume(VolumeType.POINTLIST, inputObject.getSpatialCalibration());
                try {
                    centroidVol.add(x1, y1, z1);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeError(e);
                } catch (PointOutOfRangeException e) {
                }

                SurfaceSeparationCalculator calculator = new SurfaceSeparationCalculator(centroidVol, referenceObject);
                Point<Integer> p2 = calculator.getP2();
                Point<Double> referencePoint = new Point<>((double) p2.getX(), (double) p2.getY(), (double) p2.getZ());

                centres.put(inputObject.getID(), referencePoint);

            }
        }

        return centres;

    }

    public static void processObject(Obj object, String xyOriMeasName, String xzOriMeasName, String measurementRange,
            Point<Double> referencePoint, String orientationMode, String measurementReference) {
        switch (orientationMode) {
            case OrientationModes.X_Y_PLANE:
                Measurement measurement = object.getMeasurement(xyOriMeasName);
                if (measurement == null) {
                    assignMissingMeasurements(object, xyOriMeasName, xzOriMeasName, orientationMode,
                            measurementReference);
                    return;
                }
                double xyOrientation = measurement.getValue();
                double xyAngle = getXYAngle(object, xyOrientation, measurementRange, referencePoint);

                // Adding the measurement
                String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION, measurementReference);
                object.addMeasurement(new Measurement(measurementName, xyAngle));

                break;
        }
    }

    static void assignMissingMeasurements(Obj object, String xyOriMeasName, String xzOriMeasName,
            String orientationMode, String measurementReference) {
        switch (orientationMode) {
            case OrientationModes.X_Y_PLANE:
                String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION, measurementReference);
                object.addMeasurement(new Measurement(measurementName, Double.NaN));

                break;
        }
    }

    public static double getXYAngle(Obj object, double xyOrientation, String measurementRange,
            Point<Double> referencePoint) {
        xyOrientation = Math.toRadians(xyOrientation);
        if (xyOrientation == -Math.PI)
            xyOrientation = Math.PI;
        double angleToReference = object.calculateAngle2D(referencePoint);

        double angle = xyOrientation - angleToReference;
        angle = Math.abs((angle + Math.PI) % (2 * Math.PI) - Math.PI);

        // Putting it into the range -90 to +90 degrees (or radian equivalent)
        switch (measurementRange) {
            case MeasurementRanges.ZERO_NINETY:
                if (angle >= Math.PI / 2)
                    angle = angle - Math.PI;
                break;

            case MeasurementRanges.ZERO_ONE_EIGHTY:
                if (angle >= Math.PI)
                    angle = angle - 2 * Math.PI;
                break;

        }

        // We are only interested in the deviation (i.e. absolute value)
        return Math.abs(Math.toDegrees(angle));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Measures the orientation of each object relative to a specific point.  Orientation of the objects themselves is provided by a previously-calculated measurement associated with each object.  The relative orientation is the angle between the vector specified by this measurement and a vector passing from the object centroid to a specific point."

                + "<br><br>Note: Currently only works for X-Y plane measurements";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting other parameters
        // String orientationMode = parameters.getValue(ORIENTATION_MODE);
        String orientationMode = OrientationModes.X_Y_PLANE;
        String xyOriMeasName = parameters.getValue(ORIENTATION_IN_X_Y_MEASUREMENT);
        // String xzOriMeasName = parameters.getValue(ORIENTATION_IN_XY_Z_MEASUREMENT);
        String measurementRange = parameters.getValue(MEASUREMENT_RANGE);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
        String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
        boolean mustBeSameFrame = parameters.getValue(MUST_BE_SAME_FRAME);

        // Getting measurement reference name
        String measurementReference = getMeasurementRef();

        // Get reference point as Point for each frame the input images are present for
        // (frame number as HashMap key)
        HashMap<Integer, Point<Double>> referencePoints = null;
        switch (referenceMode) {
            case ReferenceModes.IMAGE_CENTRE:
                referencePoints = getImageCentreRefs(inputObjects.getSpatialCalibration(), inputObjects.getNFrames(),
                        orientationMode);
                break;

            case ReferenceModes.OBJECT_CENTROID:
                int nFrames = inputObjects.getTemporalLimits()[1] + 1;
                Objs referenceObjects = workspace.getObjectSet(referenceObjectsName);
                referencePoints = getObjectCentroidRefs(referenceObjects, objectChoiceMode, orientationMode, nFrames,
                        mustBeSameFrame);
                break;

            case ReferenceModes.OBJECT_SURFACE:
                nFrames = inputObjects.getTemporalLimits()[1] + 1;
                referenceObjects = workspace.getObjectSet(referenceObjectsName);
                referencePoints = getObjectCentroidSurfaceRefs(inputObjects, referenceObjects, objectChoiceMode,
                        orientationMode, nFrames, mustBeSameFrame);
                break;
        }

        if (referencePoints == null)
            return Status.PASS;

        // Processing each object
        Point<Double> referencePoint = null;
        for (Obj inputObject : inputObjects.values()) {
            switch (referenceMode) {
                case ReferenceModes.IMAGE_CENTRE:
                case ReferenceModes.OBJECT_CENTROID:
                    // Timepoint-specific reference point modes
                    referencePoint = referencePoints.getOrDefault(inputObject.getT(), null);
                    break;

                case ReferenceModes.OBJECT_SURFACE:
                    // Object-specific reference modes
                    referencePoint = referencePoints.get(inputObject.getID());
                    break;
            }

            if (referencePoint == null) {
                assignMissingMeasurements(inputObject, xyOriMeasName, null, orientationMode, measurementReference);
            } else {
                processObject(inputObject, xyOriMeasName, null, measurementRange, referencePoint, orientationMode,
                        measurementReference);
            }
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        // parameters.add(new ChoiceP(ORIENTATION_MODE, this,
        // OrientationModes.X_Y_PLANE, OrientationModes.ALL));
        parameters.add(new ObjectMeasurementP(ORIENTATION_IN_X_Y_MEASUREMENT, this));
        // parameters.add(new ObjectMeasurementP(ORIENTATION_IN_XY_Z_MEASUREMENT,
        // this));
        parameters.add(new ChoiceP(MEASUREMENT_RANGE, this, MeasurementRanges.ZERO_NINETY, MeasurementRanges.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.IMAGE_CENTRE, ReferenceModes.ALL));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS, this));
        parameters.add(new ChoiceP(OBJECT_CHOICE_MODE, this, ObjectChoiceModes.LARGEST_OBJECT, ObjectChoiceModes.ALL));
        parameters.add(new BooleanP(MUST_BE_SAME_FRAME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        // returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
        // switch ((String) parameters.getValue(ORIENTATION_MODE)) {
        // case OrientationModes.X_Y_PLANE:
        // returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
        // break;

        // case OrientationModes.XY_Z_PLANE:
        // returnedParameters.add(parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT));
        // break;

        // case OrientationModes.BOTH_X_Y_AND_XY_Z_PLANES:
        // case OrientationModes.FULL_3D:
        // returnedParameters.add(parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT));
        // returnedParameters.add(parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT));
        // break;
        // }

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(ORIENTATION_IN_X_Y_MEASUREMENT)).setObjectName(inputObjectsName);
        // ((ObjectMeasurementP)
        // parameters.getParameter(ORIENTATION_IN_XY_Z_MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(MEASUREMENT_RANGE));

        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.OBJECT_CENTROID:
            case ReferenceModes.OBJECT_SURFACE:
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECTS));
                returnedParameters.add(parameters.getParameter(OBJECT_CHOICE_MODE));
                returnedParameters.add(parameters.getParameter(MUST_BE_SAME_FRAME));
                break;
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String reference = getMeasurementRef();

        if (reference == null)
            return returnedRefs;

        String referenceDescription = null;
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.IMAGE_CENTRE:
                referenceDescription = "the centre of the image";
                break;
            case ReferenceModes.OBJECT_CENTROID:
                String referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                String objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                String choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                referenceDescription = "the centroid of the " + choice + " object in the set " + referenceObjectsName;
                break;
            case ReferenceModes.OBJECT_SURFACE:
                referenceObjectsName = parameters.getValue(REFERENCE_OBJECTS);
                objectChoiceMode = parameters.getValue(OBJECT_CHOICE_MODE);
                choice = objectChoiceMode.equals(ObjectChoiceModes.LARGEST_OBJECT) ? "LARGEST" : "SMALLEST";
                referenceDescription = "the closest point of the " + choice + " object in the set "
                        + referenceObjectsName + " to the centroid of the target object";
                break;
        }

        // switch ((String) parameters.getValue(ORIENTATION_MODE)) {
        // case OrientationModes.X_Y_PLANE:
        String measurementName = getFullName(Measurements.X_Y_REL_ORIENTATION, reference);
        ObjMeasurementRef measurementReference = objectMeasurementRefs.getOrPut(measurementName);
        measurementReference.setObjectsName(inputObjectsName);
        returnedRefs.add(measurementReference);

        String xyOriMeasName = parameters.getValue(ORIENTATION_IN_X_Y_MEASUREMENT);
        measurementReference
                .setDescription("Orientation of the object (specified by the measurements \"" + xyOriMeasName
                        + "\") relative to " + referenceDescription + ". Measured in degrees between 0 and 90.");
        returnedRefs.add(measurementReference);
        // break;
        // }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace for which orientation relative to a point will be measured.  Orientation of the objects themselves is provided by a previously-calculated measurement associated with each object.  The relative orientation is the angle between the vector specified by this measurement and a vector passing from the object centroid to a specific point.  Output relative orientation measurements are associated with the relevant input object.");

        parameters.get(ORIENTATION_IN_X_Y_MEASUREMENT).setDescription(
                "Measurement associated with each input object which defines the orientation of the first vector for the relative orientation calculation.  The other vector passes between the object centroid and a specific point (controlled by the other parameters).  This orientation measurement is assumed to be specified in degree units.");

        parameters.get(MEASUREMENT_RANGE).setDescription(
                "The range over which output relative orientation measurements are specified.  For cases where the orientation of the object could be inverted without consequence (e.g. fitting orientation to an ellipse), the range \""
                        + MeasurementRanges.ZERO_NINETY
                        + "\" can be used; however, in cases where the orientation is more specific (e.g. having come from object tracking), the range \""
                        + MeasurementRanges.ZERO_ONE_EIGHTY
                        + "\" can be used.  Relative orientation measurements are absolute, in that there's no distinction between positive and negative angular differences.");

        parameters.get(REFERENCE_MODE).setDescription(
                "Controls the source of the reference point to which object orientations are compared.  :<br><ul>"
                        + "<li>\"" + ReferenceModes.IMAGE_CENTRE
                        + "\" Object orientations are compared to the centre of the object collection (i.e. to the image from which they were first detected).</li>"

                        + "<li>\"" + ReferenceModes.OBJECT_CENTROID
                        + "\" Object orientations are compared to the centre of a specific object.  Since only a single object can be used per timepoint, this will be either the smallest or largest object (controlled by \""
                        + OBJECT_CHOICE_MODE + "\") in the specified collection (\"" + REFERENCE_OBJECTS + "\").  If \""+MUST_BE_SAME_FRAME+"\" is not selected, the same object will be used in all timepoints.</li>"

                        + "<li>\"" + ReferenceModes.OBJECT_SURFACE
                        + "\" Object orientations are compared to the closest point on the surface of a specific object.  In this mode, the reference points can be different for all objects.  Since only a single object can be used per timepoint, this will be either the smallest or largest object (controlled by \""
                        + OBJECT_CHOICE_MODE + "\") in the specified collection (\"" + REFERENCE_OBJECTS
                        + "\").  If \""+MUST_BE_SAME_FRAME+"\" is not selected, the same object will be used in all timepoints.</li></ul>");

        parameters.get(REFERENCE_OBJECTS).setDescription("If \""+REFERENCE_MODE+"\" is set to \""+ReferenceModes.OBJECT_CENTROID+"\" or \""+ReferenceModes.OBJECT_SURFACE+"\", this is the object collection from which the reference object(s) will be drawn.");

        parameters.get(OBJECT_CHOICE_MODE).setDescription("If \""+REFERENCE_MODE+"\" is set to \""+ReferenceModes.OBJECT_CENTROID+"\" or \""+ReferenceModes.OBJECT_SURFACE+"\", this controls whether the smallest or largest object from that collection will be used as the reference point for relative orientation calculations.");

        parameters.get(MUST_BE_SAME_FRAME).setDescription("When selected, the reference objects must be in the same frame as the input object being measured.  As such, all the objects in one frame will be compared to a common reference, but won't necessarily have the same reference as an object in another frame.");

    }
}
