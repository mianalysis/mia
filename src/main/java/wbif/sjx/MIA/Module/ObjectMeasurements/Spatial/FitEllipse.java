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
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.GetObjectSurface;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.ProjectObjects;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Units;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Objects.OutputObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Analysis.EllipseCalculator;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Object.Volume.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
public class FitEllipse extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String FITTING_SEPARATOR = "Ellipse fitting";
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

        // Get projected object
        Obj projObj = ProjectObjects.process(inputObject, "Proj", false);

        try {
            switch (fittingMode) {
                case FittingModes.FIT_TO_WHOLE:
                    calculator = new EllipseCalculator(projObj, maxAxisLength);
                    break;

                case FittingModes.FIT_TO_SURFACE:
                    Obj edgeObject = GetObjectSurface.getSurface(projObj, "Edge", 1);
                    calculator = new EllipseCalculator(edgeObject, maxAxisLength);
                    break;
            }
        } catch (RuntimeException e) {}

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
        ellipseObject.setCoordinateSet(ellipse.getCoordinateSet());
        ellipseObject.setT(inputObject.getT());

        ellipseObject.addParent(inputObject);
        inputObject.addChild(ellipseObject);
        outputObjects.add(ellipseObject);

        return ellipseObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipse) {
        inputObject.getCoordinateSet().clear();
        inputObject.setCoordinateSet(ellipse.getCoordinateSet());

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
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_SPATIAL;
    }

    @Override
    public String getDescription() {
        return "Fit ellipses to all objects in a collection using \"<a href=\"https://imagej.net/BoneJ\">BoneJ</a>\".  For 3D objects, a 2D projection in the XY plane is used for fitting.  Fit ellipses can be stored either as new objects, or replacing the input object coordinates.<br><br>Note: If updating input objects with ellipse coordinates, measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.";
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
        int total = inputObjects.size();
        ObjCollection finalOutputObjects = outputObjects;
        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                try {
                    processObject(inputObject, finalOutputObjects, objectOutputMode, maxAxisLength, fittingMode);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeWarning("Integer overflow exception for object " + inputObject.getID()
                            + " during ellipse fitting.");
                }
                writeStatus("Rendered " + count + " of " + total + " ("
                                    + Math.floorDiv(100 * count.getAndIncrement(), total) + "%)", name);
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
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(FITTING_SEPARATOR, this));
        parameters.add(new ChoiceP(FITTING_MODE, this, FittingModes.FIT_TO_SURFACE, FittingModes.ALL));
        parameters.add(new BooleanP(LIMIT_AXIS_LENGTH, this, false));
        parameters.add(new DoubleP(MAXIMUM_AXIS_LENGTH, this, 1000d));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

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
                + "point on its perimeter.  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SEMI_MAJOR_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Semi-major axis length of ellipse fit to 2D Z-projection of the object, \""
                + inputObjectsName + "\".  The semi-major axis passes from the centre of the ellipse to the furthest "
                + "point on its perimeter.  Measured in calibrated (" + Units.getOMEUnits().getSymbol() + ") units.");
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
                + "negative below x-axis).  Note: ImageJ displays images with an inverted y-axis.");
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

    void addParameterDescriptions() {
      parameters.get(INPUT_OBJECTS).setDescription("Objects from workspace to which ellipses will be fit.  Objects stored in 3D will be projected into the XY 2D plane (using the \"" +  new ProjectObjects(null).getName() + "\" module) prior to fitting.  If a projected object is used, any output measurements and relationships are still applied to the input object (the projected object is discarded after use).  Measurements made by this module are associated with these input objects, irrespective of whether the fit ellipses are also stored as objects.");

      parameters.get(FITTING_MODE).setDescription("Controls which object coordinates are used for ellipse fitting:<br><ul>"

      +"<li>\""+FittingModes.FIT_TO_WHOLE+"\" All coordinates for the input object are passed to the ellipse fitter.<.li>"

      +"<li>\""+FittingModes.FIT_TO_SURFACE+"\" (default) Only surface coordinates of the input object are passed to the ellipse fitter.  Surface coordinates are calculated using 4-way connectivity.</li></ul>");

      parameters.get(LIMIT_AXIS_LENGTH).setDescription("When selected, all axes of the the fit ellipses must be shorter than the length specified by \""+MAXIMUM_AXIS_LENGTH+"\".  This helps filter out mis-fit ellipses and prevents unnecessary, massive memory use when storing ellipses.");

      parameters.get(MAXIMUM_AXIS_LENGTH).setDescription("Maximum length of any fit ellipse axis as measured in pixel units.  This is onyl used if \""+LIMIT_AXIS_LENGTH+"\" is selected.");

      parameters.get(OBJECT_OUTPUT_MODE).setDescription("Controls whether the fit ellipse is stored as an object in the workspace:<br><ul>"

      +"<li>\""+OutputModes.CREATE_NEW_OBJECT+"\" Fit ellipses are stored as new objects in the workspace (name specified by \""+OUTPUT_OBJECTS+"\").  Ellipses are \"solid\" objects, irrespective of whether they were only fit to input object surface coordinates.  Ellipse objects are children of the input objects to which they were fit.  If outputting ellipse objects, any measurements are still only applied to the corresponding input objects.</li>"

      +"<li>\""+OutputModes.DO_NOT_STORE+"\" (default) The ellipse coordinates are not stored.</li>"

      +"<li>\""+OutputModes.UPDATE_INPUT+"\" The coordinates of the input object are removed and replaced with the fit ellipse coordinates.  Note: Measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.</li></ul>");

      parameters.get(OUTPUT_OBJECTS).setDescription("Name assigned to output ellipse objects if \""+OBJECT_OUTPUT_MODE+"\" is in \""+OutputModes.CREATE_NEW_OBJECT+"\" mode.");

      parameters.get(ENABLE_MULTITHREADING).setDescription("Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
