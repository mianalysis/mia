package io.github.mianalysis.mia.module.objects.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import ij.gui.Line;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.transform.ProjectObjects;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitStraightLine extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Object input";

    /**
     * 
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
     * 
     */
    public static final String OBJECT_OUTPUT_MODE = "Object output mode";

    /**
     * 
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple input objects simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { DO_NOT_STORE, CREATE_NEW_OBJECT, UPDATE_INPUT };

    }

    public interface Measurements {
        String ORIENTATION = "STRAIGHT_LINE // ORIENTATION_XY_(DEGS)";
        String GRADIENT = "STRAIGHT_LINE // GRADIENT";
        String INTERCEPT = "STRAIGHT_LINE // INTERCEPT";

    }

    public FitStraightLine(Modules modules) {
        super("Fit straight line", modules);
    }

    public void processObject(Obj inputObject, Objs outputObjects, String objectOutputMode)
            throws IntegerOverflowException {
        // Get projected object
        Objs projectedObjects = new Objs("Projected", inputObject.getObjectCollection());
        Obj projObj = ProjectObjects.process(inputObject, projectedObjects, false);

        // Fit straight line
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        Collection<WeightedObservedPoint> points = new ArrayList<>();
        for (Point<Integer> point : projObj.getCoordinateSet())
            points.add(new WeightedObservedPoint(1, point.getX(), point.getY()));

        // Store results
        double[] fit = fitter.fit(points);
        double orientation = Math.toDegrees(Math.atan(fit[1]));
        inputObject.addMeasurement(new Measurement(Measurements.GRADIENT, fit[1]));
        inputObject.addMeasurement(new Measurement(Measurements.INTERCEPT, fit[0]));
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION, orientation));

        if (objectOutputMode.equals(OutputModes.DO_NOT_STORE))
            return;

        // Export line object
        double[][] extents = projObj.getExtents(true, false);

        int x1 = (int) extents[0][0];
        int y1 = (int) Math.round(fit[1] * x1 + fit[0]);
        int x2 = (int) extents[0][1];
        int y2 = (int) Math.round(fit[1] * x2 + fit[0]);

        Line line = new Line(x1, y1, x2, y2);

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                Obj lineObject = outputObjects.createAndAddNewObject(inputObject.getVolumeType());
                lineObject.addPointsFromRoi(line, (int) Math.round(inputObject.getZMean(true, false)));
                lineObject.setT(inputObject.getT());

                lineObject.addParent(inputObject);
                inputObject.addChild(lineObject);

                if (lineObject != null) {
                    outputObjects.add(lineObject);
                    lineObject.removeOutOfBoundsCoords();
                }
                break;
            case OutputModes.UPDATE_INPUT:
                int z = (int) Math.round(inputObject.getZMean(true, false));
                inputObject.clearAllCoordinates();
                inputObject.addPointsFromRoi(line, z);
                inputObject.removeOutOfBoundsCoords();
                break;
        }
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
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // Getting input objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);

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
                    processObject(inputObject, finalOutputObjects, objectOutputMode);
                } catch (IntegerOverflowException e) {
                    MIA.log.writeWarning("Integer overflow exception for object " + inputObject.getID()
                            + " during circle fitting.");
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
        }

        if (showOutput) {
            inputObjects.showMeasurements(this, modules);
            switch (objectOutputMode) {
                case OutputModes.CREATE_NEW_OBJECT:
                    outputObjects.convertToImageIDColours().show(false);
                    break;
                case OutputModes.UPDATE_INPUT:
                    inputObjects.convertToImageIDColours().show(false);
                    break;
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(OBJECT_OUTPUT_MODE, this, OutputModes.DO_NOT_STORE, OutputModes.ALL));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

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

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.GRADIENT);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.INTERCEPT);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.ORIENTATION);
        reference.setObjectsName(inputObjectsName);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {
        return null;
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
}
