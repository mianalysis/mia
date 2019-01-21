package wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification.ExtractObjectEdges;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Analysis.EllipseCalculator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
public class FitEllipse extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";
    public static final String FITTING_MODE = "Fitting mode";
    public static final String LIMIT_AXIS_LENGTH = "Limit axis length";
    public static final String MAXIMUM_AXIS_LENGTH = "Maximum axis length";

    public interface FittingModes {
        String FIT_TO_WHOLE = "Fit to whole";
        String FIT_TO_SURFACE = "Fit to surface";

        String[] ALL = new String[]{FIT_TO_SURFACE,FIT_TO_WHOLE};

    }

    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[]{DO_NOT_STORE,CREATE_NEW_OBJECT,UPDATE_INPUT};

    }

    public interface Measurements {
        String X_CENTRE_PX = "ELLIPSE // X_CENTRE_(PX)";
        String X_CENTRE_CAL = "ELLIPSE // X_CENTRE_(${CAL})";
        String Y_CENTRE_PX = "ELLIPSE // Y_CENTRE_(PX)";
        String Y_CENTRE_CAL = "ELLIPSE // Y_CENTRE_(${CAL})";
        String SEMI_MAJOR_PX = "ELLIPSE // SEMI_MAJOR_AXIS_LENGTH_(PX)";
        String SEMI_MAJOR_CAL = "ELLIPSE // SEMI_MAJOR_AXIS_LENGTH_(${CAL})";
        String SEMI_MINOR_PX = "ELLIPSE // SEMI_MINOR_AXIS_LENGTH_(PX)";
        String SEMI_MINOR_CAL = "ELLIPSE // SEMI_MINOR_AXIS_LENGTH_(${CAL})";
        String ECCENTRICITY = "ELLIPSE // ECCENTRICITY";
        String MAJOR_MINOR_RATIO = "ELLIPSE // MAJOR_MINOR_RATIO";
        String ORIENTATION_DEGS = "ELLIPSE // ORIENTATION_(DEGS)";

    }


    public void processObject(Obj inputObject, ObjCollection outputObjects, String objectOutputMode, Image templateImage, double maxAxisLength, String fittingMode) throws IntegerOverflowException {
        EllipseCalculator calculator = null;
        switch (fittingMode) {
            case FitEllipsoid.FittingModes.FIT_TO_WHOLE:
                calculator = new EllipseCalculator(inputObject,maxAxisLength);
                break;

            case FitEllipsoid.FittingModes.FIT_TO_SURFACE:
                ObjCollection edgeObjects = new ObjCollection("Edge");
                String edgeMode = ExtractObjectEdges.EdgeModes.DISTANCE_FROM_EDGE;
                Obj edgeObject = ExtractObjectEdges.getObjectEdge(inputObject,edgeObjects,edgeMode,1.0,1.0);
                calculator = new EllipseCalculator(edgeObject,maxAxisLength);
                break;
        }

        addMeasurements(inputObject,calculator);

        if (calculator == null || Double.isNaN(calculator.getXCentre())) return;

        Volume ellipse = calculator.getContainedPoints();

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Obj ellipseObject = createNewObject(inputObject,ellipse,outputObjects);
                if (ellipseObject != null) {
                    outputObjects.add(ellipseObject);
                    ellipseObject.cropToImageSize(templateImage);
                }
                break;
            case OutputModes.UPDATE_INPUT:
                updateInputObject(inputObject,ellipse);
                inputObject.cropToImageSize(templateImage);
                break;
        }
    }

    public Obj createNewObject (Obj inputObject, Volume ellipsoid, ObjCollection outputObjects) {
        if (ellipsoid == null) return null;

        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();
        String units = inputObject.getCalibratedUnits();
        boolean is2D = inputObject.is2D();

        Obj ellipsoidObject = new Obj(outputObjects.getName(),outputObjects.getNextID(),dppXY,dppZ,units,is2D);
        ellipsoidObject.setPoints(ellipsoid.getPoints());

        ellipsoidObject.addParent(inputObject);
        inputObject.addChild(ellipsoidObject);
        outputObjects.add(ellipsoidObject);

        return ellipsoidObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipsoid) {
        inputObject.setPoints(ellipsoid.getPoints());
    }

    public void addMeasurements(Obj inputObject, EllipseCalculator calculator) {
        if (calculator.getEllipseFit() == null) {
            inputObject.addMeasurement(new Measurement(Measurements.MAJOR_MINOR_RATIO,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.ECCENTRICITY,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_DEGS,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_PX,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.X_CENTRE_CAL),Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_PX,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_CENTRE_CAL),Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_PX,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SEMI_MAJOR_CAL),Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_PX,Double.NaN,this));
            inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SEMI_MINOR_CAL),Double.NaN,this));
            return;
        }

        double dppXY = inputObject.getDistPerPxXY();
        double dppZ = inputObject.getDistPerPxZ();

         double xCent = calculator.getXCentre();
        inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_PX,xCent,this));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.X_CENTRE_CAL),xCent*dppXY,this));

        double yCent = calculator.getYCentre();
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_PX,yCent,this));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.Y_CENTRE_CAL),yCent*dppXY,this));

        double semiMajor = calculator.getSemiMajorAxis();
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_PX,semiMajor,this));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SEMI_MAJOR_CAL),semiMajor*dppXY,this));

        double semiMinor = calculator.getSemiMinorAxis();
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_PX,semiMinor,this));
        inputObject.addMeasurement(new Measurement(Units.replace(Measurements.SEMI_MINOR_CAL),semiMinor*dppXY,this));

        double eccentricity = Math.sqrt(1-(semiMinor*semiMinor)/(semiMajor*semiMajor));
        inputObject.addMeasurement(new Measurement(Measurements.ECCENTRICITY,eccentricity,this));

        double ratio = semiMajor/semiMinor;
        inputObject.addMeasurement(new Measurement(Measurements.MAJOR_MINOR_RATIO,ratio,this));

        double theta = Math.toDegrees(calculator.getEllipseThetaRads());
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_DEGS,theta,this));

    }


    @Override
    public String getTitle() {
        return "Fit ellipse";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        String fittingMode = parameters.getValue(FITTING_MODE);
        boolean limitAxisLength = parameters.getValue(LIMIT_AXIS_LENGTH);
        double maxAxisLength = limitAxisLength ? parameters.getValue(MAXIMUM_AXIS_LENGTH) : Double.MAX_VALUE;

        // If necessary, creating a new ObjCollection and adding it to the Workspace
        ObjCollection outputObjects = null;
        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
            outputObjects = new ObjCollection(outputObjectsName);
            workspace.addObjects(outputObjects);
        }

        // Getting template image
        Image templateImage = workspace.getImage(templateImageName);

        // Running through each object, taking measurements and adding new object to the workspace where necessary
        int count = 0;
        int nTotal = inputObjects.size();
        for (Obj inputObject:inputObjects.values()) {
            try {
                processObject(inputObject,outputObjects,objectOutputMode,templateImage,maxAxisLength,fittingMode);
            } catch (IntegerOverflowException e) {
                return false;
            }
            writeMessage("Processed object "+(++count)+" of "+nTotal);
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE,this, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS,this));
        parameters.add(new ChoiceP(FITTING_MODE,this,FittingModes.FIT_TO_SURFACE,FittingModes.ALL));
        parameters.add(new BooleanP(LIMIT_AXIS_LENGTH,this,false));
        parameters.add(new DoubleP(MAXIMUM_AXIS_LENGTH,this,1000d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));

        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(FITTING_MODE));
        returnedParameters.add(parameters.getParameter(LIMIT_AXIS_LENGTH));
        if (parameters.getValue(LIMIT_AXIS_LENGTH)) returnedParameters.add(parameters.getParameter(MAXIMUM_AXIS_LENGTH));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
        objectMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference reference = objectMeasurementReferences.getOrPut(Measurements.X_CENTRE_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the " +
                "object, \""+inputObjectsName+"\".  Measured in pixels.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.X_CENTRE_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the " +
                "object, \""+inputObjectsName+"\".  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                "units.");

        reference = objectMeasurementReferences.getOrPut(Measurements.Y_CENTRE_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the " +
                "object, \""+inputObjectsName+"\".  Measured in pixels.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.Y_CENTRE_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the " +
                "object, \""+inputObjectsName+"\".  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") " +
                "units.");

        reference = objectMeasurementReferences.getOrPut(Measurements.SEMI_MAJOR_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""+
                inputObjectsName+"\".  The semi-major axis passes from the centre of the ellipse to the furthest " +
                "point on it's perimeter.  Measured in pixels.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.SEMI_MAJOR_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""+
                inputObjectsName+"\".  The semi-major axis passes from the centre of the ellipse to the furthest " +
                "point on it's perimeter.  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") units.");

        reference = objectMeasurementReferences.getOrPut(Measurements.SEMI_MINOR_PX);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""+
                inputObjectsName+"\".  The semi-minor axis passes from the centre of the ellipse in the direction " +
                "perpendiculart to the semi-major axis.  Measured in pixels.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.SEMI_MINOR_CAL));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""+
                inputObjectsName+"\".  The semi-minor axis passes from the centre of the ellipse in the direction" +
                "perpendiculart to the semi-major axis.  Measured in calibrated ("+Units.getOMEUnits().getSymbol()+") "+
                "units.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.ECCENTRICITY));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Measurement of how much the ellipse fit to the 2D Z-projection of the object, \"" +
                inputObjectsName+"\", deviates from a perfect circle.  Eccentricity is calculated as sqrt(1-b^2/a^2)" +
                ", where a and b are the lengths of the semi-major and semi-minor axes, respectively.  Eccentricity " +
                "has no units.");

        reference = objectMeasurementReferences.getOrPut(Units.replace(Measurements.MAJOR_MINOR_RATIO));
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Ratio of semi-major axis length to semi-minor axis length for the ellipse fit to " +
                "the 2D Z-projection of the object, \""+inputObjectsName+"\".  This measure has no units.");

        reference = objectMeasurementReferences.getOrPut(Measurements.ORIENTATION_DEGS);
        reference.setCalculated(true);
        reference.setImageObjName(inputObjectsName);
        reference.setDescription("Orientation of ellipse fit to 2D Z-projection of the object, \""+
                inputObjectsName+"\".  Measured in degrees, relative to positive x-axis (positive above x-axis, " +
                "negative below x-axis).");


        return objectMeasurementReferences;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
                relationships.addRelationship(inputObjectsName,outputObjectsName);

                break;
        }
    }
}
