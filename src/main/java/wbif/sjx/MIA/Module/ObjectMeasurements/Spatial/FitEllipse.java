package wbif.sjx.MIA.Module.ObjectMeasurements.Spatial;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.Prefs;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetObjectSurface;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Analysis.EllipseCalculator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.PointOutOfRangeException;
import wbif.sjx.common.Object.Volume.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
public class FitEllipse extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String FITTING_SEPARATOR = "Ellipsoid fitting";
    public static final String FITTING_MODE = "Fitting mode";
    public static final String LIMIT_AXIS_LENGTH = "Limit axis length";
    public static final String MAXIMUM_AXIS_LENGTH = "Maximum axis length";

    public static final String OUTPUT_SEPARATOR = "Object output";
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public FitEllipse(ModuleCollection modules) {
        super("Fit ellipse", modules);
    }

    public interface FittingModes {
        String FIT_TO_WHOLE = "Fit to whole";
        String FIT_TO_SURFACE = "Fit to surface";

        String[] ALL = new String[] { FIT_TO_SURFACE, FIT_TO_WHOLE };

    }

    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { DO_NOT_STORE, CREATE_NEW_OBJECT, UPDATE_INPUT };

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

    public void processObject(Obj inputObject, ObjCollection outputObjects, String objectOutputMode,
            double maxAxisLength, String fittingMode) throws IntegerOverflowException {
        EllipseCalculator calculator = null;
        try {
            switch (fittingMode) {
                case FitEllipsoid.FittingModes.FIT_TO_WHOLE:
                    calculator = new EllipseCalculator(inputObject, maxAxisLength);
                    break;

                case FitEllipsoid.FittingModes.FIT_TO_SURFACE:
                    Obj edgeObject = GetObjectSurface.getSurface(inputObject, "Edge", 1);
                    calculator = new EllipseCalculator(edgeObject, maxAxisLength);
                    break;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        addMeasurements(inputObject, calculator);

        if (calculator == null || Double.isNaN(calculator.getXCentre()))
            return;

        Volume ellipse = calculator.getContainedPoints();

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Obj ellipseObject = createNewObject(inputObject, ellipse, outputObjects);
                if (ellipseObject != null) {
                    outputObjects.add(ellipseObject);
                    ellipseObject.removeOutOfBoundsCoords();
                }
                break;
            case OutputModes.UPDATE_INPUT:
                updateInputObject(inputObject, ellipse);
                inputObject.removeOutOfBoundsCoords();
                break;
        }
    }

    public Obj createNewObject(Obj inputObject, Volume ellipse, ObjCollection outputObjects) {
        if (ellipse == null)
            return null;

        Obj ellipseObject = new Obj(outputObjects.getName(), outputObjects.getAndIncrementID(), inputObject);
        try {
            ellipseObject.setPoints(ellipse.getPoints());
        } catch (PointOutOfRangeException e) {
        }
        ellipseObject.setT(inputObject.getT());

        ellipseObject.addParent(inputObject);
        inputObject.addChild(ellipseObject);
        outputObjects.add(ellipseObject);

        return ellipseObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipsoid) {
        inputObject.getCoordinateSet().clear();
        try {
            inputObject.setPoints(ellipsoid.getPoints());
        } catch (PointOutOfRangeException e) {
        }
    }

    public void addMeasurements(Obj inputObject, EllipseCalculator calculator) {
        if (calculator.getEllipseFit() == null) {
            inputObject.addMeasurement(new Measurement(Measurements.MAJOR_MINOR_RATIO, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.ECCENTRICITY, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_DEGS, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_CAL, Double.NaN));
            return;
        }

        double dppXY = inputObject.getDppXY();

        double xCent = calculator.getXCentre();
        inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_PX, xCent));
        inputObject.addMeasurement(new Measurement(Measurements.X_CENTRE_CAL, xCent * dppXY));

        double yCent = calculator.getYCentre();
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_PX, yCent));
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENTRE_CAL, yCent * dppXY));

        double semiMajor = calculator.getSemiMajorAxis();
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_PX, semiMajor));
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MAJOR_CAL, semiMajor * dppXY));

        double semiMinor = calculator.getSemiMinorAxis();
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_PX, semiMinor));
        inputObject.addMeasurement(new Measurement(Measurements.SEMI_MINOR_CAL, semiMinor * dppXY));

        double eccentricity = Math.sqrt(1 - (semiMinor * semiMinor) / (semiMajor * semiMajor));
        inputObject.addMeasurement(new Measurement(Measurements.ECCENTRICITY, eccentricity));

        double ratio = semiMajor / semiMinor;
        inputObject.addMeasurement(new Measurement(Measurements.MAJOR_MINOR_RATIO, ratio));

        double theta = Math.toDegrees(calculator.getEllipseThetaRads());
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_DEGS, theta));

    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String fittingMode = parameters.getValue(FITTING_MODE);
        boolean limitAxisLength = parameters.getValue(LIMIT_AXIS_LENGTH);
        double maxAxisLength = limitAxisLength ? parameters.getValue(MAXIMUM_AXIS_LENGTH) : Double.MAX_VALUE;
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If necessary, creating a new ObjCollection and adding it to the Workspace
        ObjCollection outputObjects = null;
        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
            outputObjects = new ObjCollection(outputObjectsName, inputObjects);
            workspace.addObjects(outputObjects);
        }

        // Setting up multithreading options
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, taking measurements and adding new object to the
        // workspace where necessary
        AtomicInteger count = new AtomicInteger(1);
        int nTotal = inputObjects.size();
        ObjCollection finalOutputObjects = outputObjects;
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                try {
                    processObject(inputObject, finalOutputObjects, objectOutputMode, maxAxisLength, fittingMode);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeWarning("Integer overflow exception for object " + inputObject.getID()
                            + " during ellipsoid fitting.");
                }
                writeMessage("Processed object " + count.getAndIncrement() + " of " + nTotal);
            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            MIA.log.writeError(e.getStackTrace().toString());
        }

        if (showOutput) {
            inputObjects.showMeasurements(this, modules);
            if (!objectOutputMode.equals(OutputModes.DO_NOT_STORE)) {
                outputObjects.convertToImageRandomColours().showImage();
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(FITTING_SEPARATOR, this));
        parameters.add(new ChoiceP(FITTING_MODE, this, FittingModes.FIT_TO_SURFACE, FittingModes.ALL));
        parameters.add(new BooleanP(LIMIT_AXIS_LENGTH, this, false));
        parameters.add(new DoubleP(MAXIMUM_AXIS_LENGTH, this, 1000d));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(FITTING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FITTING_MODE));
        returnedParameters.add(parameters.getParameter(LIMIT_AXIS_LENGTH));
        if ((boolean) parameters.getValue(LIMIT_AXIS_LENGTH))
            returnedParameters.add(parameters.getParameter(MAXIMUM_AXIS_LENGTH));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated (" + Units.getOMEUnits().getSymbol()
                + ") " + "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated (" + Units.getOMEUnits().getSymbol()
                + ") " + "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SEMI_MAJOR_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""
                + inputObjectsName + "\".  The semi-major axis passes from the centre of the ellipse to the furthest "
                + "point on it's perimeter.  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SEMI_MAJOR_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""
                + inputObjectsName + "\".  The semi-major axis passes from the centre of the ellipse to the furthest "
                + "point on it's perimeter.  Measured in calibrated (" + Units.getOMEUnits().getSymbol() + ") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SEMI_MINOR_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""
                + inputObjectsName + "\".  The semi-minor axis passes from the centre of the ellipse in the direction "
                + "perpendiculart to the semi-major axis.  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SEMI_MINOR_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""
                + inputObjectsName + "\".  The semi-minor axis passes from the centre of the ellipse in the direction"
                + "perpendiculart to the semi-major axis.  Measured in calibrated (" + Units.getOMEUnits().getSymbol()
                + ") " + "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ECCENTRICITY);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Measurement of how much the ellipse fit to the 2D Z-projection of the object, \""
                + inputObjectsName
                + "\", deviates from a perfect circle.  Eccentricity is calculated as sqrt(1-b^2/a^2)"
                + ", where a and b are the lengths of the semi-major and semi-minor axes, respectively.  Eccentricity "
                + "has no units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.MAJOR_MINOR_RATIO);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Ratio of semi-major axis length to semi-minor axis length for the ellipse fit to "
                + "the 2D Z-projection of the object, \"" + inputObjectsName + "\".  This measure has no units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_DEGS);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Orientation of ellipse fit to 2D Z-projection of the object, \"" + inputObjectsName
                + "\".  Measured in degrees, relative to positive x-axis (positive above x-axis, "
                + "negative below x-axis).");
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        ParentChildRefCollection returnedRelationships = new ParentChildRefCollection();

        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
                returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

                break;
        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
