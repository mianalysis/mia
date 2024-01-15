package io.github.mianalysis.mia.module.objects.process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.Prefs;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.tools.EllipsoidCalculator;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.volume.Volume;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

/**
 * Created by sc13967 on 19/06/2018.
 */

/**
* Fit ellipsoids to all objects in a collection using "<a href="https://imagej.net/BoneJ">BoneJ</a>".  Fit ellipsoids can be stored either as new objects, or replacing the input object coordinates.<br><br>Note: If updating input objects with ellipsoid coordinates, measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitEllipsoid extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Object input";

	/**
	* Objects from workspace to which ellipsoids will be fit.  Measurements made by this module are associated with these input objects, irrespective of whether the fit ellipsoids are also stored as objects.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String FITTING_SEPARATOR = "Ellipsoid fitting";

	/**
	* Controls which object coordinates are used for ellipsoid fitting:<br><ul><li>"Fit to whole" All coordinates for the input object are passed to the ellipsoid fitter.</li><li>"Fit to surface" (default) Only surface coordinates of the input object are passed to the ellipsoid fitter.  Surface coordinates are calculated using 6-way connectivity.</li></ul>
	*/
    public static final String FITTING_MODE = "Fitting mode";

	/**
	* When selected, all axes of the the fit ellipsoids must be shorter than the length specified by "Maximum axis length".  This helps filter out mis-fit ellipsoids and prevents unnecessary, massive memory use when storing ellipsoids.
	*/
    public static final String LIMIT_AXIS_LENGTH = "Limit axis length";

	/**
	* Maximum length of any fit ellipsoid axis as measured in pixel units.  This is onyl used if "Limit axis length" is selected.
	*/
    public static final String MAXIMUM_AXIS_LENGTH = "Maximum axis length";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Object output";

	/**
	* Controls whether the fit ellipsoid is stored as an object in the workspace:<br><ul><li>"Create new objects" Fit ellipsoids are stored as new objects in the workspace (name specified by "Output objects").  Ellipsoids are "solid" objects, irrespective of whether they were only fit to input object surface coordinates.  Ellipsoid objects are children of the input objects to which they were fit.  If outputting ellipsoid objects, any measurements are still only applied to the corresponding input objects.</li><li>"Do not store" (default) The ellipsoid coordinates are not stored.</li><li>"Update input objects" The coordinates of the input object are removed and replaced with the fit ellipsoid coordinates.  Note: Measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.</li></ul>
	*/
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";

	/**
	* Name assigned to output ellipsoid objects if "Object output mode" is in "Create new objects" mode.
	*/
    public static final String OUTPUT_OBJECTS = "Output objects";


	/**
	* 
	*/
    public static final String EXECUTION_SEPARATOR = "Execution controls";

	/**
	* Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.
	*/
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public FitEllipsoid(Modules modules) {
        super("Fit ellipsoid", modules);
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
        String X_CENT_PX = "ELLIPSOID // X_CENTROID (PX)";
        String X_CENT_CAL = "ELLIPSOID // X_CENTROID (${SCAL})";
        String Y_CENT_PX = "ELLIPSOID // Y_CENTROID (PX)";
        String Y_CENT_CAL = "ELLIPSOID // Y_CENTROID (${SCAL})";
        String Z_CENT_SLICE = "ELLIPSOID // Z_CENTROID (SLICE)";
        String Z_CENT_CAL = "ELLIPSOID // Z_CENTROID (${SCAL})";
        String RADIUS_1_PX = "ELLIPSOID // RADIUS_1 (PX)";
        String RADIUS_1_CAL = "ELLIPSOID // RADIUS_1 (${SCAL})";
        String RADIUS_2_PX = "ELLIPSOID // RADIUS_2 (PX)";
        String RADIUS_2_CAL = "ELLIPSOID // RADIUS_2 (${SCAL})";
        String RADIUS_3_PX = "ELLIPSOID // RADIUS_3 (PX)";
        String RADIUS_3_CAL = "ELLIPSOID // RADIUS_3 (${SCAL})";
        String SURFACE_AREA_PX = "ELLIPSOID // SURFACE_AREA (PX²)";
        String SURFACE_AREA_CAL = "ELLIPSOID // SURFACE_AREA (${SCAL}²)";
        String VOLUME_PX = "ELLIPSOID // VOLUME (PX³)";
        String VOLUME_CAL = "ELLIPSOID // VOLUME (${SCAL}³)";
        String ORIENTATION_1 = "ELLIPSOID // ORIENTATION_1 (DEGS)";
        String ORIENTATION_2 = "ELLIPSOID // ORIENTATION_2 (DEGS)";
        String SPHERICITY = "ELLIPSOID // SPHERICITY";

    }

    public void processObject(Obj inputObject, Objs outputObjects, String objectOutputMode, String fittingMode,
            double maxAxisLength) throws IntegerOverflowException {
        EllipsoidCalculator calculator = null;
        try {
            switch (fittingMode) {
                case FittingModes.FIT_TO_WHOLE:
                    calculator = new EllipsoidCalculator(inputObject, maxAxisLength);
                    break;

                case FittingModes.FIT_TO_SURFACE:
                    Objs tempObjects = new Objs("Edge", outputObjects);
                    Obj edgeObject = GetObjectSurface.getSurface(inputObject, tempObjects, false);
                    calculator = new EllipsoidCalculator(edgeObject, maxAxisLength);
                    break;
            }
        } catch (RuntimeException e) {
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        addMeasurements(inputObject, calculator);

        if (calculator == null || calculator.getRadii() == null || objectOutputMode.equals(OutputModes.DO_NOT_STORE))
            return;

        Volume ellipsoid = calculator.getContainedPoints();
        if (ellipsoid.size() == 0)
            return;

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Obj ellipsoidObject = createNewObject(inputObject, ellipsoid, outputObjects);
                if (ellipsoidObject != null) {
                    outputObjects.add(ellipsoidObject);
                    ellipsoidObject.removeOutOfBoundsCoords();
                }
                break;
            case OutputModes.UPDATE_INPUT:
                updateInputObject(inputObject, ellipsoid);
                inputObject.removeOutOfBoundsCoords();
                break;
        }
    }

    public Obj createNewObject(Obj inputObject, Volume ellipsoid, Objs outputObjects) {
        if (ellipsoid == null)
            return null;

        Obj ellipsoidObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
        ellipsoidObject.setCoordinateSet(ellipsoid.getCoordinateSet());
        ellipsoidObject.setT(inputObject.getT());

        ellipsoidObject.addParent(inputObject);
        inputObject.addChild(ellipsoidObject);

        return ellipsoidObject;

    }

    public void updateInputObject(Obj inputObject, Volume ellipsoid) {
        inputObject.getCoordinateSet().clear();
        inputObject.getCoordinateSet().addAll(ellipsoid.getCoordinateSet());
        inputObject.clearCentroid();
        inputObject.clearProjected();
        inputObject.clearSurface();
        inputObject.clearROIs();

    }

    public void addMeasurements(Obj inputObject, EllipsoidCalculator calculator) {
        if (calculator == null || calculator.getCentroid() == null) {
            inputObject.addMeasurement(new Measurement(Measurements.X_CENT_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.X_CENT_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_SLICE, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_1_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_1_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_2_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_2_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_3_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.RADIUS_3_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.VOLUME_CAL, Double.NaN));
            inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_1, Math.toDegrees(Double.NaN)));
            inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_2, Math.toDegrees(Double.NaN)));
            inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, Double.NaN));

            return;

        }

        double dppXY = inputObject.getDppXY();
        double dppZ = inputObject.getDppZ();

        double[] centres = calculator.getCentroid();
        inputObject.addMeasurement(new Measurement(Measurements.X_CENT_PX, centres[0]));
        inputObject.addMeasurement(new Measurement(Measurements.X_CENT_CAL, centres[0] * dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_PX, centres[1]));
        inputObject.addMeasurement(new Measurement(Measurements.Y_CENT_CAL, centres[1] * dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_SLICE, centres[2] * dppXY / dppZ));
        inputObject.addMeasurement(new Measurement(Measurements.Z_CENT_CAL, centres[2] * dppZ));

        double[] radii = calculator.getRadii();
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_1_PX, radii[0]));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_1_CAL, radii[0] * dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_2_PX, radii[1]));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_2_CAL, radii[1] * dppXY));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_3_PX, radii[2]));
        inputObject.addMeasurement(new Measurement(Measurements.RADIUS_3_CAL, radii[2] * dppXY));

        double surfaceArea = calculator.getSurfaceArea();
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_PX, surfaceArea));
        inputObject.addMeasurement(new Measurement(Measurements.SURFACE_AREA_CAL, surfaceArea * dppXY * dppXY));

        double volume = calculator.getVolume();
        inputObject.addMeasurement(new Measurement(Measurements.VOLUME_PX, volume));
        inputObject.addMeasurement(new Measurement(Measurements.VOLUME_CAL, volume * dppXY * dppXY * dppXY));

        double[] orientations = calculator.getOrientationRads();
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_1, Math.toDegrees(orientations[0])));
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION_2, Math.toDegrees(orientations[1])));

        double sphericity = calculator.getSphericity();
        inputObject.addMeasurement(new Measurement(Measurements.SPHERICITY, sphericity));

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_PROCESS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Fit ellipsoids to all objects in a collection using \"<a href=\"https://imagej.net/BoneJ\">BoneJ</a>\".  Fit ellipsoids can be stored either as new objects, or replacing the input object coordinates.<br><br>Note: If updating input objects with ellipsoid coordinates, measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String fittingMode = parameters.getValue(FITTING_MODE, workspace);
        boolean limitAxisLength = parameters.getValue(LIMIT_AXIS_LENGTH, workspace);
        double maxAxisLength = limitAxisLength ? parameters.getValue(MAXIMUM_AXIS_LENGTH, workspace) : Double.MAX_VALUE;
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

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
                    processObject(inputObject, finalOutputObjects, objectOutputMode, fittingMode, maxAxisLength);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeWarning("Integer overflow exception for object " + inputObject.getID()
                            + " during ellipsoid fitting.");
                } catch (Exception e) {
                    MIA.log.writeError(e);
                }
                writeProgressStatus(count.getAndIncrement(), total, "objects");
            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        } catch (Exception e) {
            MIA.log.writeError(e);
        }

        if (showOutput) {
            inputObjects.showMeasurements(this, modules);
            if (!objectOutputMode.equals(OutputModes.DO_NOT_STORE)) {
                outputObjects.convertToImageIDColours().show();
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
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(FITTING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FITTING_MODE));
        returnedParameters.add(parameters.getParameter(LIMIT_AXIS_LENGTH));
        if ((boolean) parameters.getValue(LIMIT_AXIS_LENGTH, workspace))
            returnedParameters.add(parameters.getParameter(MAXIMUM_AXIS_LENGTH));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(OBJECT_OUTPUT_MODE));
        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE, workspace)) {
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
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X_CENT_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X_CENT_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENT_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENT_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z_CENT_SLICE);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Z_CENT_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_1_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_1_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_2_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_2_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_3_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_3_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SURFACE_AREA_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SURFACE_AREA_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_PX);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.VOLUME_CAL);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_1);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION_2);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.SPHERICITY);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        Workspace workspace = null;
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        switch ((String) parameters.getValue(OBJECT_OUTPUT_MODE, workspace)) {
            case OutputModes.CREATE_NEW_OBJECT:
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
                String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
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
                "Objects from workspace to which ellipsoids will be fit.  Measurements made by this module are associated with these input objects, irrespective of whether the fit ellipsoids are also stored as objects.");

        parameters.get(FITTING_MODE)
                .setDescription("Controls which object coordinates are used for ellipsoid fitting:<br><ul>"

                        + "<li>\"" + FittingModes.FIT_TO_WHOLE
                        + "\" All coordinates for the input object are passed to the ellipsoid fitter.</li>"

                        + "<li>\"" + FittingModes.FIT_TO_SURFACE
                        + "\" (default) Only surface coordinates of the input object are passed to the ellipsoid fitter.  Surface coordinates are calculated using 6-way connectivity.</li></ul>");

        parameters.get(LIMIT_AXIS_LENGTH).setDescription(
                "When selected, all axes of the the fit ellipsoids must be shorter than the length specified by \""
                        + MAXIMUM_AXIS_LENGTH
                        + "\".  This helps filter out mis-fit ellipsoids and prevents unnecessary, massive memory use when storing ellipsoids.");

        parameters.get(MAXIMUM_AXIS_LENGTH).setDescription(
                "Maximum length of any fit ellipsoid axis as measured in pixel units.  This is onyl used if \""
                        + LIMIT_AXIS_LENGTH + "\" is selected.");

        parameters.get(OBJECT_OUTPUT_MODE)
                .setDescription("Controls whether the fit ellipsoid is stored as an object in the workspace:<br><ul>"

                        + "<li>\"" + OutputModes.CREATE_NEW_OBJECT
                        + "\" Fit ellipsoids are stored as new objects in the workspace (name specified by \""
                        + OUTPUT_OBJECTS
                        + "\").  Ellipsoids are \"solid\" objects, irrespective of whether they were only fit to input object surface coordinates.  Ellipsoid objects are children of the input objects to which they were fit.  If outputting ellipsoid objects, any measurements are still only applied to the corresponding input objects.</li>"

                        + "<li>\"" + OutputModes.DO_NOT_STORE
                        + "\" (default) The ellipsoid coordinates are not stored.</li>"

                        + "<li>\"" + OutputModes.UPDATE_INPUT
                        + "\" The coordinates of the input object are removed and replaced with the fit ellipsoid coordinates.  Note: Measurements associated with the input object (e.g. spatial measurements) will still be available, but may no longer be valid.</li></ul>");

        parameters.get(OUTPUT_OBJECTS).setDescription("Name assigned to output ellipsoid objects if \""
                + OBJECT_OUTPUT_MODE + "\" is in \"" + OutputModes.CREATE_NEW_OBJECT + "\" mode.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
