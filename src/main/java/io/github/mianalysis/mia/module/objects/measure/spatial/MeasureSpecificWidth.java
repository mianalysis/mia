package io.github.mianalysis.mia.module.objects.measure.spatial;

import java.util.HashSet;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.measure.Calibration;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.GetObjectSurface;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;


/**
* Measures the width of the object along the axis passing through two specified reference points.  Reference points can be the object centroid, an image measurement or a measurement associated with the object.  Widths are determined as the distance between the two points along the reference line with the greatest separation (i.e. it doesn't matter if there are gaps between them).
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureSpecificWidth extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Objects for which specific widths will be calculated.  Width measurements will be associated with these objects.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String REFERENCE_SEPARATOR_1 = "Reference 1 selection";

	/**
	* The source of the first reference point coordinates:<br><ul><li>"Object centroid" Reference point will be positioned coincident with the centroid of the object being measured.</li><li>"Image measurement" X, Y and Z location of the reference point will be equal to specified measurement values associated with the image (set by "Reference image 1").</li><li>"Object measurement" X, Y and Z location of the reference point will be equal to the specified measurememnt values associated with the object being measured.</li></ul>
	*/
    public static final String REFERENCE_MODE_1 = "Reference mode 1";

	/**
	* If "Reference mode 1" is set to "Image measurement", this is the image that will provide the measurements determining the X, Y and Z location of the first reference point.
	*/
    public static final String REFERENCE_IMAGE_1 = "Reference image 1";
    public static final String X_POSITION_MEASUREMENT_IM_1 = "X-pos. image meas. 1 (px)";
    public static final String Y_POSITION_MEASUREMENT_IM_1 = "Y-pos. image meas. 1 (px)";
    public static final String Z_POSITION_MEASUREMENT_IM_1 = "Z-pos. image meas. 1 (slice)";
    public static final String X_POSITION_MEASUREMENT_OBJ_1 = "X-pos. object meas. 1 (px)";
    public static final String Y_POSITION_MEASUREMENT_OBJ_1 = "Y-pos. object meas. 1 (px)";
    public static final String Z_POSITION_MEASUREMENT_OBJ_1 = "Z-pos. object meas. 1 (slice)";


	/**
	* 
	*/
    public static final String REFERENCE_SEPARATOR_2 = "Reference 2 selection";

	/**
	* The source of the second reference point coordinates:<br><ul><li>"Object centroid" Reference point will be positioned coincident with the centroid of the object being measured.</li><li>"Image measurement" X, Y and Z location of the reference point will be equal to specified measurement values associated with the image (set by "Reference image 1").</li><li>"Object measurement" X, Y and Z location of the reference point will be equal to the specified measurememnt values associated with the object being measured.</li></ul>
	*/
    public static final String REFERENCE_MODE_2 = "Reference mode 2";

	/**
	* If "Reference mode 2" is set to "Image measurement", this is the image that will provide the measurements determining the X, Y and Z location of the second reference point.
	*/
    public static final String REFERENCE_IMAGE_2 = "Reference image 2";
    public static final String X_POSITION_MEASUREMENT_IM_2 = "X-pos. image meas. 2 (px)";
    public static final String Y_POSITION_MEASUREMENT_IM_2 = "Y-pos. image meas. 2 (px)";
    public static final String Z_POSITION_MEASUREMENT_IM_2 = "Z-pos. image meas. 2 (slice)";
    public static final String X_POSITION_MEASUREMENT_OBJ_2 = "X-pos. object meas. 2 (px)";
    public static final String Y_POSITION_MEASUREMENT_OBJ_2 = "Y-pos. object meas. 2 (px)";
    public static final String Z_POSITION_MEASUREMENT_OBJ_2 = "Z-pos. object meas. 2 (slice)";


	/**
	* 
	*/
    public static final String MISCELLANEOUS_SEPARATOR = "Miscellaneous controls";

	/**
	* The output measurement names will be prefixed with this value (although it can be left blank).  Using a prefix for these measurements allows multiple different widths to be output by multiple copies of this module.
	*/
    public static final String MEASUREMENT_PREFIX = "Measurement prefix";

    public interface ReferenceModes {
        String CENTROID = "Object centroid";
        String IMAGE_MEASUREMENT = "Image measurement";
        String OBJECT_MEASUREMENT = "Object measurement";

        String[] ALL = new String[] { CENTROID, IMAGE_MEASUREMENT, OBJECT_MEASUREMENT };

    }

    public interface Measurements {
        String WIDTH_PX = "WIDTH_(PX)";
        String WIDTH_CAL = "WIDTH_(${SCAL})";
        String X1_PX = "X1_(PX)";
        String Y1_PX = "Y1_(PX)";
        String Z1_SLICE = "Z1_(SLICE)";
        String X2_PX = "X2_(PX)";
        String Y2_PX = "Y2_(PX)";
        String Z2_SLICE = "Z2_(SLICE)";

    }

    public MeasureSpecificWidth(final Modules modules) {
        super("Measure specific width", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measures the width of the object along the axis passing through two specified reference points.  Reference points can be the object centroid, an image measurement or a measurement associated with the object.  Widths are determined as the distance between the two points along the reference line with the greatest separation (i.e. it doesn't matter if there are gaps between them).";
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_SPATIAL;
    }

    public static String getFullName(String measurementName, String prefix) {
        return "SPECIFIC_WIDTH // " + prefix + measurementName;
    }

    public static Point<Double> getCentroid(final Obj obj) {
        final double xMeas = obj.getXMean(true);
        final double yMeas = obj.getYMean(true);
        final double zMeas = obj.getZMean(true, true);

        return new Point<Double>(xMeas, yMeas, zMeas);

    }

    public static Point<Double> getImageReference(final Image image, final String xMeasName, final String yMeasName,
            final String zMeasName) {
        final Calibration calibration = image.getImagePlus().getCalibration();

        final double xMeas = image.getMeasurement(xMeasName).getValue();
        final double yMeas = image.getMeasurement(yMeasName).getValue();
        final double zMeas = image.getMeasurement(zMeasName).getValue()
                * (calibration.pixelDepth / calibration.pixelWidth);

        return new Point<Double>(xMeas, yMeas, zMeas);

    }

    public static Point<Double> getObjectReference(final Obj obj, final String xMeasName, final String yMeasName,
            final String zMeasName) {
        final SpatCal spatCal = obj.getSpatialCalibration();

        final double xMeas = obj.getMeasurement(xMeasName).getValue();
        final double yMeas = obj.getMeasurement(yMeasName).getValue();
        final double zMeas = obj.getMeasurement(zMeasName).getValue() * (spatCal.dppZ / spatCal.dppXY);

        return new Point<Double>(xMeas, yMeas, zMeas);

    }

    public static WidthMeasurementResult getExtentAlongAxis(final Obj obj, final Point<Double> ref1,
            final Point<Double> ref2) {
        // Getting calibration to convert z into px
        final double dppXY = obj.getSpatialCalibration().dppXY;
        final double dppZ = obj.getSpatialCalibration().dppZ;

        // Getting the vector pointing to p2
        final Vector3D vector1 = new Vector3D(ref1.x, ref1.y, ref1.z * dppZ / dppXY);
        final Vector3D vector2 = new Vector3D(ref2.x, ref2.y, ref2.z * dppZ / dppXY);
        final Line primaryLine = new Line(vector1, vector2, 1.0E-10D);

        // Getting surface points
        Objs tempCollection = new Objs("Surfaces", obj.getObjectCollection());
        Obj surface = GetObjectSurface.getSurface(obj, tempCollection, false);

        // Storing candidate points
        HashSet<Point<Integer>> candidates = new HashSet<Point<Integer>>();

        // Iterating over all points in the input object, calculating their distance
        // from the line. If they are within
        // 0.5 pixels, testing for distance from centroid. The furthest will be
        // retained.
        for (final Point<Integer> point : surface.getCoordinateSet()) {
            // Vector representation of current coordinate
            final Vector3D testVector = new Vector3D(point.x, point.y, point.z * dppZ / dppXY);

            // Distance of current point to line. If less than 0.1, we will consider it
            final double distance = primaryLine.distance(testVector);
            if (distance <= 0.75)
                candidates.add(point);

        }

        Point<Integer> end1 = null;
        Point<Integer> end2 = null;
        double dist = 0;

        // Iterating over all point combinations, determining the greatest separation
        for (Point<Integer> p1 : candidates) {
            for (Point<Integer> p2 : candidates) {
                if (p1 == p2)
                    continue;

                double testDist = p1.calculateDistanceToPoint(p2);
                if (testDist > dist) {
                    end1 = p1;
                    end2 = p2;
                    dist = testDist;
                }
            }
        }

        return new WidthMeasurementResult(end1, end2, obj.getSpatialCalibration());

    }

    static void addMeasurements(Obj obj, WidthMeasurementResult result, String prefix) {
        obj.addMeasurement(new Measurement(getFullName(Measurements.WIDTH_PX, prefix), result.calculateWidth(true)));
        obj.addMeasurement(new Measurement(getFullName(Measurements.WIDTH_CAL, prefix), result.calculateWidth(false)));
        obj.addMeasurement(new Measurement(getFullName(Measurements.X1_PX, prefix), result.getEnd1().x));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Y1_PX, prefix), result.getEnd1().y));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Z1_SLICE, prefix), result.getEnd1().z));
        obj.addMeasurement(new Measurement(getFullName(Measurements.X2_PX, prefix), result.getEnd2().x));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Y2_PX, prefix), result.getEnd2().y));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Z2_SLICE, prefix), result.getEnd2().z));

    }

    @Override
    protected Status process(final Workspace workspace) {
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        final String refMode1 = parameters.getValue(REFERENCE_MODE_1, workspace);
        final String referenceImageName1 = parameters.getValue(REFERENCE_IMAGE_1, workspace);
        final String xPosMeasIm1 = parameters.getValue(X_POSITION_MEASUREMENT_IM_1, workspace);
        final String yPosMeasIm1 = parameters.getValue(Y_POSITION_MEASUREMENT_IM_1, workspace);
        final String zPosMeasIm1 = parameters.getValue(Z_POSITION_MEASUREMENT_IM_1, workspace);
        final String xPosMeasObj1 = parameters.getValue(X_POSITION_MEASUREMENT_OBJ_1, workspace);
        final String yPosMeasObj1 = parameters.getValue(Y_POSITION_MEASUREMENT_OBJ_1, workspace);
        final String zPosMeasObj1 = parameters.getValue(Z_POSITION_MEASUREMENT_OBJ_1, workspace);

        final String refMode2 = parameters.getValue(REFERENCE_MODE_2, workspace);
        final String referenceImageName2 = parameters.getValue(REFERENCE_IMAGE_2, workspace);
        final String xPosMeasIm2 = parameters.getValue(X_POSITION_MEASUREMENT_IM_2, workspace);
        final String yPosMeasIm2 = parameters.getValue(Y_POSITION_MEASUREMENT_IM_2, workspace);
        final String zPosMeasIm2 = parameters.getValue(Z_POSITION_MEASUREMENT_IM_2, workspace);
        final String xPosMeasObj2 = parameters.getValue(X_POSITION_MEASUREMENT_OBJ_2, workspace);
        final String yPosMeasObj2 = parameters.getValue(Y_POSITION_MEASUREMENT_OBJ_2, workspace);
        final String zPosMeasObj2 = parameters.getValue(Z_POSITION_MEASUREMENT_OBJ_2, workspace);

        final String prefix = parameters.getValue(MEASUREMENT_PREFIX, workspace);

        final Objs inputObjects = workspace.getObjects(inputObjectsName);

        Image referenceImage1 = null;
        if (refMode1.equals(ReferenceModes.IMAGE_MEASUREMENT)) {
            referenceImage1 = workspace.getImage(referenceImageName1);
        }

        Image referenceImage2 = null;
        if (refMode2.equals(ReferenceModes.IMAGE_MEASUREMENT)) {
            referenceImage2 = workspace.getImage(referenceImageName2);
        }

        for (final Obj inputObject : inputObjects.values()) {
            final Point<Double> ref1;
            final Point<Double> ref2;

            // Getting reference points
            switch (refMode1) {
                case ReferenceModes.CENTROID:
                default:
                    ref1 = getCentroid(inputObject);
                    break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                    ref1 = getImageReference(referenceImage1, xPosMeasIm1, yPosMeasIm1, zPosMeasIm1);
                    break;
                case ReferenceModes.OBJECT_MEASUREMENT:
                    ref1 = getObjectReference(inputObject, xPosMeasObj1, yPosMeasObj1, zPosMeasObj1);
                    break;
            }

            switch (refMode2) {
                case ReferenceModes.CENTROID:
                default:
                    ref2 = getCentroid(inputObject);
                    break;
                case ReferenceModes.IMAGE_MEASUREMENT:
                    ref2 = getImageReference(referenceImage2, xPosMeasIm2, yPosMeasIm2, zPosMeasIm2);
                    break;
                case ReferenceModes.OBJECT_MEASUREMENT:
                    ref2 = getObjectReference(inputObject, xPosMeasObj2, yPosMeasObj2, zPosMeasObj2);
                    break;
            }

            // Getting first extent (from specified reference through centroid)
            final WidthMeasurementResult result = getExtentAlongAxis(inputObject, ref1, ref2);

            // Adding measurements
            addMeasurements(inputObject, result, prefix);

        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(REFERENCE_MODE_1, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE_1, this));
        parameters.add(new ImageMeasurementP(X_POSITION_MEASUREMENT_IM_1, this));
        parameters.add(new ImageMeasurementP(Y_POSITION_MEASUREMENT_IM_1, this));
        parameters.add(new ImageMeasurementP(Z_POSITION_MEASUREMENT_IM_1, this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_OBJ_1, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_OBJ_1, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_OBJ_1, this));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(REFERENCE_MODE_2, this, ReferenceModes.CENTROID, ReferenceModes.ALL));
        parameters.add(new InputImageP(REFERENCE_IMAGE_2, this));
        parameters.add(new ImageMeasurementP(X_POSITION_MEASUREMENT_IM_2, this));
        parameters.add(new ImageMeasurementP(Y_POSITION_MEASUREMENT_IM_2, this));
        parameters.add(new ImageMeasurementP(Z_POSITION_MEASUREMENT_IM_2, this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_OBJ_2, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_OBJ_2, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_OBJ_2, this));

        parameters.add(new SeparatorP(MISCELLANEOUS_SEPARATOR, this));
        parameters.add(new StringP(MEASUREMENT_PREFIX, this));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        final Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR_1));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_1));
        switch ((String) parameters.getValue(REFERENCE_MODE_1, workspace)) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_1));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_IM_1));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_IM_1));
                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_IM_1));
                break;
            case ReferenceModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_1));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_1));
                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_OBJ_1));
                break;
        }

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR_2));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_2));
        switch ((String) parameters.getValue(REFERENCE_MODE_2, workspace)) {
            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_2));
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_IM_2));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_IM_2));
                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_IM_2));
                break;
            case ReferenceModes.OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_2));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_2));
                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_OBJ_2));
                break;
        }

        returnedParameters.add(parameters.getParameter(MISCELLANEOUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_PREFIX));

        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_OBJ_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_OBJ_2)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_OBJ_2)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_OBJ_2)).setObjectName(inputObjectsName);

        final String referenceImageName1 = parameters.getValue(REFERENCE_IMAGE_1, workspace);
        ((ImageMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_IM_1)).setImageName(referenceImageName1);
        ((ImageMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_IM_1)).setImageName(referenceImageName1);
        ((ImageMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_IM_1)).setImageName(referenceImageName1);

        final String referenceImageName2 = parameters.getValue(REFERENCE_IMAGE_2, workspace);
        ((ImageMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_IM_2)).setImageName(referenceImageName2);
        ((ImageMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_IM_2)).setImageName(referenceImageName2);
        ((ImageMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_IM_2)).setImageName(referenceImageName2);

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        final ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        final String prefix = parameters.getValue(MEASUREMENT_PREFIX, workspace);

        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.WIDTH_PX, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.WIDTH_CAL, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.X1_PX, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.Y1_PX, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.Z1_SLICE, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.X2_PX, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.Y2_PX, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = objectMeasurementRefs.getOrPut(getFullName(Measurements.Z2_SLICE, prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

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
        String refMode1 = parameters.getValue(REFERENCE_MODE_1, null);
        String refMode2 = parameters.getValue(REFERENCE_MODE_2, null);

        if (refMode1.equals(ReferenceModes.CENTROID) && refMode2.equals(ReferenceModes.CENTROID)) {
            MIA.log.writeWarning("Both references can't be set to \"centroid\"");
            return false;
        }

        return true;

    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects for which specific widths will be calculated.  Width measurements will be associated with these objects.");

        parameters.get(REFERENCE_MODE_1).setDescription("The source of the first reference point coordinates:<br><ul>"

                + "<li>\"" + ReferenceModes.CENTROID
                + "\" Reference point will be positioned coincident with the centroid of the object being measured.</li>"

                + "<li>\"" + ReferenceModes.IMAGE_MEASUREMENT
                + "\" X, Y and Z location of the reference point will be equal to specified measurement values associated with the image (set by \""
                + REFERENCE_IMAGE_1 + "\").</li>"

                + "<li>\"" + ReferenceModes.OBJECT_MEASUREMENT
                + "\" X, Y and Z location of the reference point will be equal to the specified measurememnt values associated with the object being measured.</li></ul>");

        parameters.get(REFERENCE_IMAGE_1).setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \""
                + ReferenceModes.IMAGE_MEASUREMENT
                + "\", this is the image that will provide the measurements determining the X, Y and Z location of the first reference point.");

        parameters.get(X_POSITION_MEASUREMENT_IM_1)
                .setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_1
                        + "\", that will be used as the X-axis location of the first reference point.");

        parameters.get(Y_POSITION_MEASUREMENT_IM_1)
                .setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_1
                        + "\", that will be used as the Y-axis location of the first reference point.");

        parameters.get(Z_POSITION_MEASUREMENT_IM_1)
                .setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_1
                        + "\", that will be used as the Z-axis location of the first reference point.");

        parameters.get(X_POSITION_MEASUREMENT_OBJ_1).setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the X-axis location of the first reference point.");

        parameters.get(Y_POSITION_MEASUREMENT_OBJ_1).setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the Y-axis location of the first reference point.");

        parameters.get(Z_POSITION_MEASUREMENT_OBJ_1).setDescription("If \"" + REFERENCE_MODE_1 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the Z-axis location of the first reference point.");

        parameters.get(REFERENCE_MODE_2).setDescription("The source of the second reference point coordinates:<br><ul>"

                + "<li>\"" + ReferenceModes.CENTROID
                + "\" Reference point will be positioned coincident with the centroid of the object being measured.</li>"

                + "<li>\"" + ReferenceModes.IMAGE_MEASUREMENT
                + "\" X, Y and Z location of the reference point will be equal to specified measurement values associated with the image (set by \""
                + REFERENCE_IMAGE_1 + "\").</li>"

                + "<li>\"" + ReferenceModes.OBJECT_MEASUREMENT
                + "\" X, Y and Z location of the reference point will be equal to the specified measurememnt values associated with the object being measured.</li></ul>");

        parameters.get(REFERENCE_IMAGE_2).setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \""
                + ReferenceModes.IMAGE_MEASUREMENT
                + "\", this is the image that will provide the measurements determining the X, Y and Z location of the second reference point.");

        parameters.get(X_POSITION_MEASUREMENT_IM_2)
                .setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_2
                        + "\", that will be used as the X-axis location of the second reference point.");

        parameters.get(Y_POSITION_MEASUREMENT_IM_2)
                .setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_2
                        + "\", that will be used as the Y-axis location of the second reference point.");

        parameters.get(Z_POSITION_MEASUREMENT_IM_2)
                .setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", this is the measurement, associated with the image specified by \"" + REFERENCE_IMAGE_2
                        + "\", that will be used as the Z-axis location of the second reference point.");

        parameters.get(X_POSITION_MEASUREMENT_OBJ_2).setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the X-axis location of the second reference point.");

        parameters.get(Y_POSITION_MEASUREMENT_OBJ_2).setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the Y-axis location of the second reference point.");

        parameters.get(Z_POSITION_MEASUREMENT_OBJ_2).setDescription("If \"" + REFERENCE_MODE_2 + "\" is set to \""
                + ReferenceModes.OBJECT_MEASUREMENT
                + "\", this is the measurement, associated with the current object, that will be used as the Z-axis location of the second reference point.");

        parameters.get(MEASUREMENT_PREFIX).setDescription(
                "The output measurement names will be prefixed with this value (although it can be left blank).  Using a prefix for these measurements allows multiple different widths to be output by multiple copies of this module.");

    }
}

class WidthMeasurementResult {
    private final Point<Integer> end1;
    private final Point<Integer> end2;
    private final SpatCal spatCal;

    WidthMeasurementResult(final Point<Integer> end1, final Point<Integer> end2, final SpatCal spatCal) {
        this.end1 = end1;
        this.end2 = end2;
        this.spatCal = spatCal;
    }

    public double calculateWidth(final boolean pixelDistances) {
        final double dppXY = spatCal.dppXY;
        final double dppZ = spatCal.dppZ;
        final double ratio = dppZ / dppXY;

        if (pixelDistances) {
            final Point<Double> p1 = new Point<Double>((double) end1.x, (double) end1.y, end1.z * ratio);
            final Point<Double> p2 = new Point<Double>(end2.x * dppXY, end2.y * dppXY, end2.z * ratio);

            return p1.calculateDistanceToPoint(p2);

        } else {
            final Point<Double> p1 = new Point<Double>(end1.x * dppXY, end1.y * dppXY, end1.z * dppZ);
            final Point<Double> p2 = new Point<Double>(end2.x * dppXY, end2.y * dppXY, end2.z * dppZ);

            return p1.calculateDistanceToPoint(p2);

        }
    }

    public Point<Integer> getEnd1() {
        return end1;
    }

    public Point<Integer> getEnd2() {
        return end2;
    }
}
