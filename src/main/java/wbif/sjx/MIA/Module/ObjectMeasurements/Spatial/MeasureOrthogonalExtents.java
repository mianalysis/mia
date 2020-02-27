package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Point;

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
        double zMeas = obj.getZMean(true,false);

        return new Point<Double>(xMeas,yMeas,zMeas);

    }

    public static Point<Double> getReference(Obj obj, String xMeasName, String yMeasName, String zMeasName) {
        double xMeas = obj.getMeasurement(xMeasName).getValue();
        double yMeas = obj.getMeasurement(yMeasName).getValue();
        double zMeas = obj.getMeasurement(zMeasName).getValue();

        return new Point<Double>(xMeas,yMeas,zMeas);

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
            Point<Double> cent = getCentroid(inputObject);
            Point<Double> ref = getReference(inputObject, xPosMeas1, yPosMeas1, zPosMeas1);


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
