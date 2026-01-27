package io.github.mianalysis.mia.module.objects.process;

import java.awt.Polygon;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.Prefs;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.transform.ProjectObjects;
import io.github.mianalysis.mia.object.ObjsFactories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
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
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.process.exceptions.IntegerOverflowException;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitCircle extends Module {

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

    public FitCircle(Modules modules) {
        super("Fit circle", modules);
    }

    public interface OutputModes {
        String DO_NOT_STORE = "Do not store";
        String CREATE_NEW_OBJECT = "Create new objects";
        String UPDATE_INPUT = "Update input objects";

        String[] ALL = new String[] { DO_NOT_STORE, CREATE_NEW_OBJECT, UPDATE_INPUT };

    }

    public interface Measurements {
        String X_CENTRE_PX = "CIRCLE // X_CENTRE_(PX)";
        String X_CENTRE_CAL = "CIRCLE // X_CENTRE_(${SCAL})";
        String Y_CENTRE_PX = "CIRCLE // Y_CENTRE_(PX)";
        String Y_CENTRE_CAL = "CIRCLE // Y_CENTRE_(${SCAL})";
        String RADIUS_PX = "CIRCLE // RADIUS_(PX)";
        String RADIUS_CAL = "CIRCLE // RADIUS_(${SCAL})";

    }

    public Roi fitCircle(Roi roi) {
        // Retaining the largest ROI if a composite ROI is provided
        if (roi.getType() == Roi.COMPOSITE) {
            ShapeRoi shapeRoi = new ShapeRoi(roi);

            Roi maxRoi = null;
            double maxLength = 0;
            for (Roi currRoi : shapeRoi.getRois())
                if (currRoi.getLength() > maxLength) {
                    maxRoi = currRoi;
                    maxLength = currRoi.getLength();
                }

            roi = maxRoi;

        }

        if (roi == null)
            return null;

        Polygon poly = roi.getPolygon();
        int n = poly.npoints;
        int[] x = poly.xpoints;
        int[] y = poly.ypoints;
        if (n < 3) {
            MIA.log.writeDebug("At least 3 points are required to fit a circle.");
            return null;
        }

        // calculate point centroid
        double sumx = 0, sumy = 0;
        for (int i = 0; i < n; i++) {
            sumx = sumx + poly.xpoints[i];
            sumy = sumy + poly.ypoints[i];
        }
        double meanx = sumx / n;
        double meany = sumy / n;

        // calculate moments
        double[] X = new double[n], Y = new double[n];
        double Mxx = 0, Myy = 0, Mxy = 0, Mxz = 0, Myz = 0, Mzz = 0;
        for (int i = 0; i < n; i++) {
            X[i] = x[i] - meanx;
            Y[i] = y[i] - meany;
            double Zi = X[i] * X[i] + Y[i] * Y[i];
            Mxy = Mxy + X[i] * Y[i];
            Mxx = Mxx + X[i] * X[i];
            Myy = Myy + Y[i] * Y[i];
            Mxz = Mxz + X[i] * Zi;
            Myz = Myz + Y[i] * Zi;
            Mzz = Mzz + Zi * Zi;
        }
        Mxx = Mxx / n;
        Myy = Myy / n;
        Mxy = Mxy / n;
        Mxz = Mxz / n;
        Myz = Myz / n;
        Mzz = Mzz / n;

        // calculate the coefficients of the characteristic polynomial
        double Mz = Mxx + Myy;
        double Cov_xy = Mxx * Myy - Mxy * Mxy;
        double Mxz2 = Mxz * Mxz;
        double Myz2 = Myz * Myz;
        double A2 = 4 * Cov_xy - 3 * Mz * Mz - Mzz;
        double A1 = Mzz * Mz + 4 * Cov_xy * Mz - Mxz2 - Myz2 - Mz * Mz * Mz;
        double A0 = Mxz2 * Myy + Myz2 * Mxx - Mzz * Cov_xy - 2 * Mxz * Myz * Mxy + Mz * Mz * Cov_xy;
        double A22 = A2 + A2;
        double epsilon = 1e-12;
        double ynew = 1e+20;
        int IterMax = 20;
        double xnew = 0;

        // Newton's method starting at x=0
        for (int iter = 1; iter <= IterMax; iter++) {
            double yold = ynew;
            ynew = A0 + xnew * (A1 + xnew * (A2 + 4. * xnew * xnew));
            if (Math.abs(ynew) > Math.abs(yold)) {
                MIA.log.writeDebug("Wrong direction: |ynew| > |yold|");
                xnew = 0;
                break;
            }
            double Dy = A1 + xnew * (A22 + 16 * xnew * xnew);
            double xold = xnew;
            xnew = xold - ynew / Dy;
            if (Math.abs((xnew - xold) / xnew) < epsilon)
                break;
            if (iter >= IterMax) {
                MIA.log.writeDebug("Will not converge");
                xnew = 0;
            }
            if (xnew < 0) {
                MIA.log.writeDebug("Negative root:  x = " + xnew);
                xnew = 0;
            }
        }

        // calculate the circle parameters
        double DET = xnew * xnew - xnew * Mz + Cov_xy;
        double centerX = (Mxz * (Myy - xnew) - Myz * Mxy) / (2 * DET);
        double centerY = (Myz * (Mxx - xnew) - Mxz * Mxy) / (2 * DET);
        double radius = Math.sqrt(centerX * centerX + centerY * centerY + Mz + 2 * xnew);
        if (Double.isNaN(radius)) {
            MIA.log.writeError("Points are collinear.");
            return null;
        }
        centerX = centerX + meanx - radius;
        centerY = centerY + meany - radius;

        return new OvalRoi(centerX, centerY, radius * 2, radius * 2);

    }

    public void processObject(ObjI inputObject, ObjsI outputObjects, String objectOutputMode)
            throws IntegerOverflowException {
        // Get projected object
        ObjsI projectedObjects = ObjsFactories.getDefaultFactory().createFromExample("Projected", inputObject.getObjectCollection());
        ObjI projObj = ProjectObjects.process(inputObject, projectedObjects, false);

        Roi inputObjectRoi = projObj.getRoi(0);
        Roi circleRoi = fitCircle(inputObjectRoi);

        addMeasurements(inputObject, circleRoi);

        if (circleRoi == null || objectOutputMode.equals(OutputModes.DO_NOT_STORE))
            return;

        switch (objectOutputMode) {
            case OutputModes.CREATE_NEW_OBJECT:
                ObjI circleObject = outputObjects.createAndAddNewObject(inputObject.getCoordinateSetFactory());
                circleObject.addPointsFromRoi(circleRoi, (int) Math.round(inputObject.getZMean(true, false)));
                circleObject.setT(inputObject.getT());

                circleObject.addParent(inputObject);
                inputObject.addChild(circleObject);

                if (circleObject != null) {
                    outputObjects.add(circleObject);
                    circleObject.removeOutOfBoundsCoords();
                }
                break;
            case OutputModes.UPDATE_INPUT:
                inputObject.getCoordinateSet().clear();
                inputObject.addPointsFromRoi(circleRoi, (int) Math.round(inputObject.getZMean(true, false)));
                inputObject.removeOutOfBoundsCoords();
                break;
        }
    }

    public void addMeasurements(ObjI inputObject, Roi circleRoi) {
        if (circleRoi == null) {
            inputObject.addMeasurement(new MeasurementI(Measurements.X_CENTRE_PX, Double.NaN));
            inputObject.addMeasurement(new MeasurementI(Measurements.X_CENTRE_CAL, Double.NaN));

            inputObject.addMeasurement(new MeasurementI(Measurements.Y_CENTRE_PX, Double.NaN));
            inputObject.addMeasurement(new MeasurementI(Measurements.Y_CENTRE_CAL, Double.NaN));

            inputObject.addMeasurement(new MeasurementI(Measurements.RADIUS_PX, Double.NaN));
            inputObject.addMeasurement(new MeasurementI(Measurements.RADIUS_CAL, Double.NaN));

        } else {
            double dppXY = inputObject.getDppXY();

            double xCent = circleRoi.getContourCentroid()[0];
            inputObject.addMeasurement(new MeasurementI(Measurements.X_CENTRE_PX, xCent));
            inputObject.addMeasurement(new MeasurementI(Measurements.X_CENTRE_CAL, xCent *
                    dppXY));

            double yCent = circleRoi.getContourCentroid()[1];
            inputObject.addMeasurement(new MeasurementI(Measurements.Y_CENTRE_PX, yCent));
            inputObject.addMeasurement(new MeasurementI(Measurements.Y_CENTRE_CAL, yCent *
                    dppXY));

            double radius = circleRoi.getFloatWidth() / 2;
            inputObject.addMeasurement(new MeasurementI(Measurements.RADIUS_PX, radius));
            inputObject.addMeasurement(new MeasurementI(Measurements.RADIUS_CAL, radius *
                    dppXY));

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
        return "Fit circles to all objects in a collection using ImageJ's built-in \"Fit circles\" functionality.  For 3D objects, a 2D projection in the XY plane is used for fitting.  If an object is comprised of multiple spatially isolated parts this will fit the circle to the largest.<br><br>This uses the <a href=\"https://github.com/imagej/ImageJ/blob/5f3faaf4be7a5ec82778c2a7f348c782044a888b/ij/plugin/Selection.java#L119\">ImageJ implementation</a> of <a href=\"https://github.com/mdoube/BoneJ/blob/master/src/org/doube/geometry/FitCircle.java\">BoneJ's Java interpretation</a> of <a href=\"http://www.math.uab.edu/~chernov/cl/MATLABcircle.html\">Nikolai Chernov's MATALAB implementation</a> of the Pratt method (Pratt V., Direct least-squares fitting of algebraic surfaces\", <i>Computer Graphics</i> (1987) <b>21</b> 145-152).";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String objectOutputMode = parameters.getValue(OBJECT_OUTPUT_MODE, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // If necessary, creating a new Objs and adding it to the Workspace
        ObjsI outputObjects = null;
        if (objectOutputMode.equals(OutputModes.CREATE_NEW_OBJECT)) {
            outputObjects = ObjsFactories.getDefaultFactory().createFromExample(outputObjectsName, inputObjects);
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
        ObjsI finalOutputObjects = outputObjects;

        for (ObjI inputObject : inputObjects.values()) {
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
            if (!objectOutputMode.equals(OutputModes.DO_NOT_STORE))
                outputObjects.convertToImageIDColours().showWithNormalisation(false);
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
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

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the circle fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("X-coordinate for the centre of the circle fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_PX);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the circle fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in pixels.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTRE_CAL);
        reference.setObjectsName(inputObjectsName);
        reference.setDescription("Y-coordinate for the centre of the circle fit to the 2D Z-projection of the "
                + "object, \"" + inputObjectsName + "\".  Measured in calibrated ("
                + SpatialUnit.getOMEUnit().getSymbol() + ") " + "units.");
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
