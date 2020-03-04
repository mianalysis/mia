package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.StringP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.SpatCal;

public class MeasureSpecificWidth extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String REFERENCE_SEPARATOR = "Reference selection";
    public static final String REFERENCE_MODE_1 = "Reference mode 1";
    public static final String X_POSITION_MEASUREMENT_1 = "X-position measurement 1 (px)";
    public static final String Y_POSITION_MEASUREMENT_1 = "Y-position measurement 1 (px)";
    public static final String Z_POSITION_MEASUREMENT_1 = "Z-position measurement 1 (slice)";
    public static final String REFERENCE_MODE_2 = "Reference mode 2";
    public static final String X_POSITION_MEASUREMENT_2 = "X-position measurement 2 (px)";
    public static final String Y_POSITION_MEASUREMENT_2 = "Y-position measurement 2 (px)";
    public static final String Z_POSITION_MEASUREMENT_2 = "Z-position measurement 2 (slice)";

    public static final String MISCELLANEOUS_SEPARATOR = "Miscellaneous controls";
    public static final String MEASUREMENT_PREFIX = "Measurement prefix";
    
    public interface ReferenceModes {
        String CENTROID = "Object centroid";
        String MEASUREMENT = "From measurement";
        
        String[] ALL = new String[]{CENTROID,MEASUREMENT};
        
    }
    
    public interface Measurements {
        String WIDTH_PX = "WIDTH_(PX)";
        String WIDTH_CAL = "WIDTH_(${CAL})";
        String X1_PX = "X1_(PX)";
        String Y1_PX = "Y1_(PX)";
        String Z1_SLICE = "Z1_(SLICE)";
        String X2_PX = "X2_(PX)";
        String Y2_PX = "Y2_(PX)";
        String Z2_SLICE = "Z2_(SLICE)";

    }
    
    public MeasureSpecificWidth(final ModuleCollection modules) {
        super("Measure specific width", modules);
    }
    
    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    public static String getFullName(String measurementName, String prefix) {
        return "SPECIFIC_WIDTH // " + prefix + measurementName;
    }

    public static Point<Double> getCentroid(final Obj obj) {
        final double xMeas = obj.getXMean(true);
        final double yMeas = obj.getYMean(true);
        final double zMeas = obj.getZMean(true,true);
        
        return new Point<Double>(xMeas,yMeas,zMeas);
        
    }
    
    public static Point<Double> getReference(final Obj obj, final String xMeasName, final String yMeasName, final String zMeasName) {
        final SpatCal spatCal = obj.getSpatialCalibration();
        
        final double xMeas = obj.getMeasurement(xMeasName).getValue();
        final double yMeas = obj.getMeasurement(yMeasName).getValue();
        final double zMeas = obj.getMeasurement(zMeasName).getValue()*(spatCal.dppZ/spatCal.dppXY);
        
        return new Point<Double>(xMeas,yMeas,zMeas);
        
    }
    
    public static WidthMeasurementResult getExtentAlongAxis(final Obj obj, final Point<Double> ref1, final Point<Double> ref2) {
        // Getting calibration to convert z into px
        final double dppXY = obj.getSpatialCalibration().dppXY;
        final double dppZ = obj.getSpatialCalibration().dppZ;
        
        // Getting the vector pointing to p2
        final Vector3D vector1 = new Vector3D(ref1.x*dppXY, ref1.y*dppXY, ref1.z*dppZ);
        final Vector3D vector2 = new Vector3D(ref2.x*dppXY, ref2.y*dppXY, ref2.z*dppZ);
        final Line primaryLine = new Line(vector1,vector2,1.0E-10D);
        
        Point<Integer> end1 = null;
        Point<Integer> end2 = null;
        double dist1 = 0;
        double dist2 = 0;
        
        // Iterating over all points in the input object, calculating their distance from the line.  If they are within
        // 0.5 pixels, testing for distance from centroid.  The furthest will be retained.
        for (final Point<Integer> point:obj.getCoordinateSet()) {
            // Vector representation of current coordinate
            final Vector3D testVector = new Vector3D(point.x*dppXY, point.y*dppXY, point.z*dppZ);
            
            // Distance of current point to line.  If less than 0.1, we will consider it
            final double distance = primaryLine.distance(testVector);
            if (distance >= 0.5) continue;
            
            // Calculating distance to centroid and reference point.  This shows which end we are targetting.
            final double distTo1 = testVector.distance(vector1);
            final double distTo2 = testVector.distance(vector2);
            
            if (distTo1 < distTo2 && distTo2 > dist1) {
                // Point is closer to the ref1 and further than ever from ref2
                dist1 = distTo2;
                end1 = point;
            } else if (distTo1 >= distTo2 && distTo1 > dist2) {
                // Point is closer to ref2 and further than ever from ref1
                dist2 = distTo1;
                end2 = point;
            } 
        }
        
        return new WidthMeasurementResult(end1, end2, obj.getSpatialCalibration());
        
    }
    
    static void addMeasurements(Obj obj, WidthMeasurementResult result, String prefix) {
        obj.addMeasurement(new Measurement(getFullName(Measurements.WIDTH_PX,prefix),result.calculateWidth(true)));
        obj.addMeasurement(new Measurement(Units.replace(getFullName(Measurements.WIDTH_CAL,prefix)),result.calculateWidth(false)));
        obj.addMeasurement(new Measurement(getFullName(Measurements.X1_PX,prefix),result.getEnd1().x));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Y1_PX,prefix),result.getEnd1().y));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Z1_SLICE,prefix),result.getEnd1().z));
        obj.addMeasurement(new Measurement(getFullName(Measurements.X2_PX,prefix),result.getEnd2().x));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Y2_PX,prefix),result.getEnd2().y));
        obj.addMeasurement(new Measurement(getFullName(Measurements.Z2_SLICE,prefix),result.getEnd2().z));

    }

    @Override
    protected boolean process(final Workspace workspace) {
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        final String refMode1 = parameters.getValue(REFERENCE_MODE_1);
        final String xPosMeas1 = parameters.getValue(X_POSITION_MEASUREMENT_1);
        final String yPosMeas1 = parameters.getValue(Y_POSITION_MEASUREMENT_1);
        final String zPosMeas1 = parameters.getValue(Z_POSITION_MEASUREMENT_1);
        final String refMode2 = parameters.getValue(REFERENCE_MODE_2);
        final String xPosMeas2 = parameters.getValue(X_POSITION_MEASUREMENT_2);
        final String yPosMeas2 = parameters.getValue(Y_POSITION_MEASUREMENT_2);
        final String zPosMeas2 = parameters.getValue(Z_POSITION_MEASUREMENT_2);
        final String prefix = parameters.getValue(MEASUREMENT_PREFIX);
        
        final ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        
        for (final Obj inputObject:inputObjects.values()) {
            final Point<Double> ref1;
            final Point<Double> ref2;
            
            // Getting reference points
            switch (refMode1) {
                case ReferenceModes.CENTROID:
                default:
                    ref1 = getCentroid(inputObject);
                break;
                case ReferenceModes.MEASUREMENT:
                    ref1 = getReference(inputObject, xPosMeas1, yPosMeas1, zPosMeas1);
                break;
            }
            
            switch (refMode2) {
                case ReferenceModes.CENTROID:
                default:
                    ref2 = getCentroid(inputObject);
                break;
                case ReferenceModes.MEASUREMENT:
                    ref2 = getReference(inputObject, xPosMeas2, yPosMeas2, zPosMeas2);
                break;
            }
            
            // Getting first extent (from specified reference through centroid)
            final WidthMeasurementResult result = getExtentAlongAxis(inputObject,ref1,ref2);
            
            // Adding measurements
            addMeasurements(inputObject, result, prefix);
            
        }
        
        if (showOutput) inputObjects.showMeasurements(this, modules);
        
        return true;
        
    }
    
    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));

        parameters.add(new ParamSeparatorP(REFERENCE_SEPARATOR,this));
        parameters.add(new ChoiceP(REFERENCE_MODE_1,this,ReferenceModes.CENTROID,ReferenceModes.ALL));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_1,this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_1,this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_1,this));
        parameters.add(new ChoiceP(REFERENCE_MODE_2,this,ReferenceModes.CENTROID,ReferenceModes.ALL));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT_2,this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT_2,this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT_2,this));
        
        parameters.add(new ParamSeparatorP(MISCELLANEOUS_SEPARATOR,this));
        parameters.add(new StringP(MEASUREMENT_PREFIX,this));

    }
    
    @Override
    public ParameterCollection updateAndGetParameters() {
        final ParameterCollection returnedParameters = new ParameterCollection();
        
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        
        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_1));
        switch ((String) parameters.getValue(REFERENCE_MODE_1)) {
            case ReferenceModes.MEASUREMENT:
            returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_1));
            returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_1));
            returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_1));
            break;            
        }
        
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE_2));
        switch ((String) parameters.getValue(REFERENCE_MODE_2)) {
            case ReferenceModes.MEASUREMENT:
            returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT_2));
            returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT_2));
            returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT_2));
            break;            
        }
        
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_1)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT_2)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT_2)).setObjectName(inputObjectsName);
        ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT_2)).setObjectName(inputObjectsName);
        
        returnedParameters.add(parameters.getParameter(MISCELLANEOUS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_PREFIX));

        return returnedParameters;
        
    }
    
    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }
    
    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        final ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        final String prefix = parameters.getValue(MEASUREMENT_PREFIX);
        
        ObjMeasurementRef ref = new ObjMeasurementRef(getFullName(Measurements.WIDTH_PX,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);
        
        ref = new ObjMeasurementRef(Units.replace(getFullName(Measurements.WIDTH_CAL,prefix)));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);
        
        ref = new ObjMeasurementRef(getFullName(Measurements.X1_PX,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(getFullName(Measurements.Y1_PX,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(getFullName(Measurements.Z1_SLICE,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(getFullName(Measurements.X2_PX,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(getFullName(Measurements.Y2_PX,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(getFullName(Measurements.Z2_SLICE,prefix));
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;
        
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
        String refMode1 = parameters.getValue(REFERENCE_MODE_1);
        String refMode2 = parameters.getValue(REFERENCE_MODE_2);

        if (refMode1.equals(ReferenceModes.CENTROID) && refMode2.equals(ReferenceModes.CENTROID)) {
            MIA.log.writeWarning("Both references can't be set to \"centroid\"");
            return false;
        }

        return true;

    }
    
    @Override
    public String getDescription() {
        return "Measures the width of the object along the axis passing through the centroid and specified point.";
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
        final double ratio = dppZ/dppXY;
        
        if (pixelDistances) {
            final Point<Double> p1 = new Point<Double>((double) end1.x, (double) end1.y,end1.z*ratio);
            final Point<Double> p2 = new Point<Double>(end2.x*dppXY,end2.y*dppXY,end2.z*ratio);
            
            return p1.calculateDistanceToPoint(p2);
            
        } else {
            final Point<Double> p1 = new Point<Double>(end1.x*dppXY,end1.y*dppXY,end1.z*dppZ);
            final Point<Double> p2 = new Point<Double>(end2.x*dppXY,end2.y*dppXY,end2.z*dppZ);
            
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