package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.SpatCal;

public class MeasureOrthogonalExtents extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_SEPARATOR = "Reference selection";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement (px)";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement (px)";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement (slice)";


    public MeasureOrthogonalExtents(ModuleCollection modules) {
        super("Measure point-point distance", modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    public static Point<Double> getCentroid(Obj obj) {
        double xMeas = obj.getXMean(true);
        double yMeas = obj.getYMean(true);
        double zMeas = obj.getZMean(true,true);

        return new Point<Double>(xMeas,yMeas,zMeas);

    }

    public static Point<Double> getReference(Obj obj, String xMeasName, String yMeasName, String zMeasName) {
        SpatCal spatCal = obj.getSpatialCalibration();

        double xMeas = obj.getMeasurement(xMeasName).getValue();
        double yMeas = obj.getMeasurement(yMeasName).getValue();
        double zMeas = obj.getMeasurement(zMeasName).getValue()*(spatCal.dppZ/spatCal.dppXY);

        return new Point<Double>(xMeas,yMeas,zMeas);

    }

    public static double getExtentAlongAxis(Obj obj, Point<Double> p1, Point<Double> p2) {
        Image image = obj.getAsImage("Im");
        ImagePlus ipl = image.getImagePlus();

        // Getting calibration to convert z into px
        SpatCal spatCal = obj.getSpatialCalibration();
        double conv = spatCal.dppZ/spatCal.dppXY;

        // Getting the vector pointing to p2
        Vector3D vector1 = new Vector3D(p1.x, p1.y, p1.z);
        Vector3D vector2 = new Vector3D(p2.x, p2.y, p2.z);
        Line primaryLine = new Line(vector2,vector1,1.0E-10D);

        for (int i = -1000;i<1000;i = i+10) {
            Vector3D end = primaryLine.pointAt(i);
            MIA.log.writeDebug("END = "+i+"_" + end.getX() + "_" + end.getY());
        }

        // Iterating over all points in the input object, calculating their distance from the line.  If they are within
        // 0.5 pixels, testing for distance from centroid.  The furthest will be retained.
//        for (Point<Integer> point:obj.getCoordinateSet()) {
//            Vector3D testVector = new Vector3D(point.x, point.y, point.z*conv);
//
//            double distance = primaryLine.distance(testVector);
//
//            ipl.getProcessor().setf(point.x,point.y,(float) distance);
//
//        }

        new ImageJ();
        ipl.show();
        IJ.runMacro("waitForUser");

        return Double.NaN;

    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String xPosMeas1 = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeas1 = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeas1 = parameters.getValue(Z_POSITION_MEASUREMENT);

        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        for (Obj inputObject:inputObjects.values()) {
            // Getting reference points
            Point<Double> ref = getReference(inputObject, xPosMeas1, yPosMeas1, zPosMeas1);
            Point<Double> cent = getCentroid(inputObject);

            // Getting first extent (from specified reference through centroid)
            double extent1 = getExtentAlongAxis(inputObject,ref,cent);

            // Getting first orthogonal extent

            // Getting second orthogonal extent (3D objects only)

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new ParamSeparatorP(REFERENCE_SEPARATOR,this));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT,this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT,this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);

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
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Measures the diameter of the object along the axis passing through the centroid and specified point.  " +
                "In 2D, also measures along the axis perpendicular to this.  In 3D, measures along both perpendicular " +
                "axes.";
    }
}
