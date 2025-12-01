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
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

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
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple input objects simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public FitStraightLine(Modules modules) {
        super("Fit straight line", modules);
    }

    public interface Measurements {
        String ORIENTATION = "STRAIGHT_LINE // ORIENTATION_XY_(DEGS)";
        String GRADIENT = "STRAIGHT_LINE // GRADIENT";
        String INTERCEPT = "STRAIGHT_LINE // INTERCEPT";

    }

    public void processObject(Obj inputObject) {
        // Get projected object
        Objs projectedObjects = new Objs("Projected", inputObject.getObjectCollection());
        Obj projObj = ProjectObjects.process(inputObject, projectedObjects, false);

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        Collection<WeightedObservedPoint> points = new ArrayList<>();
        for (Point<Integer> point:projObj.getCoordinateSet())
            points.add(new WeightedObservedPoint(1, point.getX(), point.getY()));
        
        double[] fit = fitter.fit(points);
        double orientation = Math.toDegrees(Math.atan(fit[1]));
        
        inputObject.addMeasurement(new Measurement(Measurements.GRADIENT, fit[1]));
        inputObject.addMeasurement(new Measurement(Measurements.INTERCEPT, fit[0]));
        inputObject.addMeasurement(new Measurement(Measurements.ORIENTATION, orientation));
        
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
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // Getting input objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Setting up multithreading options
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, taking measurements and adding new object to the
        // workspace where necessary
        AtomicInteger count = new AtomicInteger(1);
        int total = inputObjects.size();

        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                processObject(inputObject);
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

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

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
       return null;
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
