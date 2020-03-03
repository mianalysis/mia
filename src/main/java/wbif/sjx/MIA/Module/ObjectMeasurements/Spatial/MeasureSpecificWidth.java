package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Volume.SpatCal;

public class MeasureSpecificWidth extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String REFERENCE_SEPARATOR = "Reference selection";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement (px)";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement (px)";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement (slice)";
    
    // public interface ReferenceModes {
    //     String CENTROID = "Object centroid";
    //     String MEASUREMENT = "From measurement";

    //     String[] ALL = new String[]{CENTROID,MEASUREMENT};

    // }
    
    public interface Measurements {
        String WIDTH_PX = "SPECIFIC_WIDTH // WIDTH_(PX)";
        String WIDTH_CAL = "SPECIFIC_WIDTH // WIDTH_($CAL)";

    }

    public MeasureSpecificWidth(final ModuleCollection modules) {
        super("Measure specific width", modules);
    }
    
    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
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
    
    public static WidthMeasurementResult getExtentAlongAxis(final Obj obj, final Point<Double> centPoint, final Point<Double> refPoint) {
        // Getting calibration to convert z into px
        final double dppXY = obj.getSpatialCalibration().dppXY;
        final double dppZ = obj.getSpatialCalibration().dppZ;
                
        // Getting the vector pointing to p2
        final Vector3D centVector = new Vector3D(centPoint.x*dppXY, centPoint.y*dppXY, centPoint.z*dppZ);
        final Vector3D refVector = new Vector3D(refPoint.x*dppXY, refPoint.y*dppXY, refPoint.z*dppZ);
        final Line primaryLine = new Line(refVector,centVector,1.0E-10D);
    
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
            final double distToCent = testVector.distance(centVector);
            final double distToRef = testVector.distance(refVector);
            
            if (distToCent >= distToRef && distToCent > dist1) {
                // Point is closer to the reference (so is at the "front" end)
                dist1 = distToCent;
                end1 = point;
            } else if (distToCent < distToRef && distToCent > dist2) {
                // Point is closer to the centroid (so is at the "back" end)
                dist2 = distToCent;
                end2 = point;
            } 
        }

        return new WidthMeasurementResult(end1, end2, obj.getSpatialCalibration());
        
    }
    
    @Override
    protected boolean process(final Workspace workspace) {
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        final String xPosMeas1 = parameters.getValue(X_POSITION_MEASUREMENT);
        final String yPosMeas1 = parameters.getValue(Y_POSITION_MEASUREMENT);
        final String zPosMeas1 = parameters.getValue(Z_POSITION_MEASUREMENT);
        
        final ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        
        for (final Obj inputObject:inputObjects.values()) {
            // Getting reference points
            final Point<Double> ref = getReference(inputObject, xPosMeas1, yPosMeas1, zPosMeas1);
            final Point<Double> cent = getCentroid(inputObject);
            
            // Getting first extent (from specified reference through centroid)
            final WidthMeasurementResult result = getExtentAlongAxis(inputObject,cent,ref);

            // Adding measurements
            final Measurement measurementPx = new Measurement(Measurements.WIDTH_PX,result.calculateWidth(true));
            inputObject.addMeasurement(measurementPx);
            
            final Measurement measurementCal = new Measurement(Units.replace(Measurements.WIDTH_CAL),result.calculateWidth(false));
            inputObject.addMeasurement(measurementCal);

        }
        
        if (showOutput) inputObjects.showMeasurements(this, modules);

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
        final ParameterCollection returnedParameters = new ParameterCollection();
        
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        
        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
        returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));
        
        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
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
        final ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        final String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef ref = new ObjMeasurementRef(Measurements.WIDTH_PX);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        ref = new ObjMeasurementRef(Units.replace(Measurements.WIDTH_CAL));
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
}