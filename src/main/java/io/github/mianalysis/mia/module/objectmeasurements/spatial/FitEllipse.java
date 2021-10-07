package io.github.mianalysis.mia.module.objectmeasurements.spatial;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.objectprocessing.identification.GetObjectSurface;
import io.github.mianalysis.mia.module.objectprocessing.identification.ProjectObjects;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.analysis.EllipseCalculator;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.volume.Volume;

/**
 * Created by sc13967 on 19/06/2018.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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

    public FitEllipse(Modules modules) {
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
        String X_CENTRE_CAL = "ELLIPSE // X_CENTRE_(${SCAL})";
        String Y_CENTRE_PX = "ELLIPSE // Y_CENTRE_(PX)";
        String Y_CENTRE_CAL = "ELLIPSE // Y_CENTRE_(${SCAL})";
        String SEMI_MAJOR_PX = "ELLIPSE // SEMI_MAJOR_AXIS_LENGTH_(PX)";
        String SEMI_MAJOR_CAL = "ELLIPSE // SEMI_MAJOR_AXIS_LENGTH_(${SCAL})";
        String SEMI_MINOR_PX = "ELLIPSE // SEMI_MINOR_AXIS_LENGTH_(PX)";
        String SEMI_MINOR_CAL = "ELLIPSE // SEMI_MINOR_AXIS_LENGTH_(${SCAL})";
        String ECCENTRICITY = "ELLIPSE // ECCENTRICITY";
        String MAJOR_MINOR_RATIO = "ELLIPSE // MAJOR_MINOR_RATIO";
        String ORIENTATION_DEGS = "ELLIPSE // ORIENTATION_(DEGS)";

    }

    public void processObject(Obj inputObject, Objs outputObjects, String objectOutputMode,
            double maxAxisLength, String fittingMode) throws IntegerOverflowException {
        EllipseCalculator calculator = null;

        // Get projected object
        Objs projectedObjects = new Objs("Projected", inputObject.getObjectCollection());
        Obj projObj = ProjectObjects.process(inputObject, projectedObjects, false);

        try {
            switch (fittingMode) {
                case FittingModes.FIT_TO_WHOLE:
                    calculator = new EllipseCalculator(projObj, maxAxisLength);
                    break;

                case FittingModes.FIT_TO_SURFACE:
                    Objs tempObjects = new Objs("Edge", inputObject.getObjectCollection());
                    Obj edgeObject = GetObjectSurface.getSurface(projObj, tempObjects, false);
                    calculator = new EllipseCalculator(edgeObject, maxAxisLength);
                    break;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        addMeasurements(inputObject, calculator);

        if (calculator == null || Double.isNaN(calculator.getXCentre())
                || objectOutputMode.equals(OutputModes.DO_NOT_STORE))
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

    public Obj createNewObject(Obj inputObject, Volume ellipse, Objs outputObjects) {
        if (ellipse == null)
            return null;

        Obj ellipseObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
        ellipseObject.setCoordinateSet(ellipse.getCoordinateSet());
        ellipseObject.setT(inputObject.getT());

        ellipseObject.addParent(inputObject);
        inputObject.addChild(ellipseObject);

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
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String fittingMode = parameters.getValue(FITTING_MODE);
        boolean limitAxisLength = parameters.getValue(LIMIT_AXIS_LENGTH);
        double maxAxisLength = limitAxisLength ? parameters.getValue(MAXIMUM_AXIS_LENGTH) : Double.MAX_VALUE;
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If necessary, creating a new Objs and adding it to the Workspace
        Objs outputObjects = null;
        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
            outputObjects = new Objs(outputObjectsName, inputObjects);
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
        Objs finalOutputObjects = outputObjects;

        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                try {
                    processObject(inputObject, finalOutputObjects, objectOutputMode, maxAxisLength, fittingMode);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeWarning("Integer overflow exception for object " + inputObject.getID()
                            + " during ellipse fitting.");
                }

                writeProgressStatus(count.getAndIncrement(), total, "objects");
            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            MIA.log.writeError(e);
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
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the ellipse fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
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
                + "point on its perimeter.  Measured in calibrated (" + SpatialUnit.getOMEUnit().getSymbol()
                + ") units.");
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
                + "perpendiculart to the semi-major axis.  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
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
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRelationships = new ParentChildRefs();

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
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from workspace to which ellipses will be fit.  Objects stored in 3D will be projected into the XY 2D plane (using the \""
                        + new ProjectObjects(null).getName()
                        + "\" module) prior to fitting.  If a projected object is used, any output measurements and relationships are still applied to the input object (the projected object is discarded after use).  Measurements made by this module are associated with these input objects, irrespective of whether the fit ellipses are also stored as objects.");

        parameters.get(FITTING_MODE)
                .setDescription("Controls which object coordinates are used for ellipse fitting:<br><ul>"

                        + "<li>\"" + FittingModes.FIT_TO_WHOLE
                        + "\" All coordinates for the input object are passed to the ellipse fitter.<.li>"

                        + "<li>\"" + FittingModes.FIT_TO_SURFACE
                        + "\" (default) Only surface coordinates of the input object are passed to the ellipse fitter.  Surface coordinates are calculated using 4-way connectivity.</li></ul>");

        parameters.get(LIMIT_AXIS_LENGTH).setDescription(
                "When selected, all axes of the the fit ellipses must be shorter than the length specified by \""
                        + MAXIMUM_AXIS_LENGTH
                        + "\".  This helps filter out mis-fit ellipses and prevents unnecessary, massive memory use when storing ellipses.");

        parameters.get(MAXIMUM_AXIS_LENGTH).setDescription(
                "Maximum length of any fit ellipse axis as measured in pixel units.  This is onyl used if \""
                        + LIMIT_AXIS_LENGTH + "\" is selected.");

        parameters.get(OBJECT_OUTPUT_MODE)
                .setDescription("Controls whether the fit ellipse is stored as an object in the workspace:<br><ul>"

                        + "<li>\"" + OutputModes.CREATE_NEW_OBJECT
                        + "\" Fit ellipses are stored as new objects in the workspace (name specified by \""
                        + OUTPUT_OBJECTS
                        + "\").  Ellipses are \"solid\" objects, irrespective of whether they were only fit to input object surface coordinates.  Ellipse objects are children of the input objects to which they were fit.  If outputting ellipse objects, any measurements are still only applied to the corresponding input objects.</li>"

                        + "<li>\"" + OutputModes.DO_NOT_STORE
                        + "\" (default) The ellipse coordinates are not stored.</li>"

                        + "<li>\"" + OutputModes.UPDATE_INPUT
                        + "\" The coordinates of the input object are removed and replaced with the fit ellipse coordinates.  Note: Measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.</li></ul>");

        parameters.get(OUTPUT_OBJECTS).setDescription("Name assigned to output ellipse objects if \""
                + OBJECT_OUTPUT_MODE + "\" is in \"" + OutputModes.CREATE_NEW_OBJECT + "\" mode.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
