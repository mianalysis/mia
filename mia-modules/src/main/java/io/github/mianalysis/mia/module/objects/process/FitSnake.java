package io.github.mianalysis.mia.module.objects.process;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ABSnake.SnakeConfig;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.thirdparty.ABSnakeMod;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class FitSnake extends Module {
    /**
     * 
    */
    public static final String OBJECTS_SEPARATOR = "Objects input/output";

    /**
     * Objects from the workspace to which active contours will be fit. Active
     * contours are fit in 2D to the object points from the first slice. As such,
     * input objects can be stored in 3D space, but only a single slice will be fit.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * When selected, the input objects will have their coordinates replaced with
     * the coordinates from the fit contour. Applied coordinates will be solid
     * within the boundary of the associated contour.
     */
    public static final String UPDATE_INPUT_OBJECTS = "Update input objects";

    /**
    * 
    */
    public static final String IMAGE_SEPARATOR = "Image input";

    /**
     * Image from the workspace to which the contours will be fit. The intensity of
     * this image will contribute to the external forces applied to the contour. For
     * example, the contour will attempt to minimise the intensity along the path of
     * the contour.
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * If "Update input objects" is not selected, this is the name with which the
     * output contour objects will be stored in the workspace.
     */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String SNAKE_SEPARATOR = "Snake controls";

    /**
    * 
    */
    public static final String MINIMUM_REGULARISATION = "Minimum regularisation";

    /**
    * 
    */
    public static final String MAXIMUM_REGULARISATION = "Maximum regularisation";

    /**
    * 
    */
    public static final String ALPHA_DERICHE = "Alpha Deriche";

    /**
    * 
    */
    public static final String MAXIMUM_DISPLACEMENT = "Maximum displacement";

    /**
    * 
    */
    public static final String STEP_MULTIPLIER = "Step multiplier";

    /**
    * 
    */
    public static final String GRADIENT_THRESHOLD = "Gradient threshold";

    /**
    * 
    */
    public static final String NUM_ITERATIONS = "Number of iterations";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple input objects simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public FitSnake(Modules modules) {
        super("Fit snake (active contour)", modules);
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
        return "Uses the ABSnake implementation of active contours to fit contours round each slice of specified objects.  The 2D perimeter of each input object is converted to a closed contour, the position of which is optimised in order to minimise various internal (contour) and external (image) energies.  This uses the \"ABSnake\" plugin from <a href=\"https://github.com/mcib3d/ABSnake/tree/master\">https://github.com/mcib3d/ABSnake/tree/master</ a>.";
    }

    public void process(Obj inputObj, Image inputImage, @Nullable Obj outputObj, double regMin,
            double regMax, double alphaD, double displMax, double step, double gradThresh, int nIterations) {
        // The following are hardcoded in the original plugin
        double force = 5;
        double maxSearch = 100;

        ImagePlus inputIpl = inputImage.getImagePlus();

        HashMap<Integer, Roi> newRois = new HashMap<>();

        double[][] extents = inputObj.getExtents(true, false);
        int zStart = (int) Math.round(extents[2][0]);
        int zEnd = (int) Math.round(extents[2][1]);
        int t = inputObj.getT();

        for (int z = zStart; z <= zEnd; z++) {
            int sliceIdx = inputIpl.getStackIndex(1, z + 1, t + 1);
            ImageProcessor ipr = inputIpl.getStack().getProcessor(sliceIdx);

            SnakeConfig config = new SnakeConfig(gradThresh, displMax, maxSearch, regMin, regMax, alphaD);
            ABSnakeMod snake = new ABSnakeMod();
            snake.Init(inputObj.getRoi(z));
            snake.setOriginalImage(ipr);
            snake.setConfig(config);

            snake.computeGrad(ipr);

            double dist0 = 0;
            for (int i = 0; i < nIterations; ++i) {
                double dist = snake.process();
                if (dist >= dist0 && dist < force) {
                    snake.computeGrad(ipr);
                    config.update(step);
                }

                dist0 = dist;

            }

            snake.setOriginalImage((ImageProcessor) null);

            newRois.put(z, snake.createRoi());

        }

        if (outputObj == null) {
            inputObj.clearAllCoordinates();

            for (int z : newRois.keySet())
                inputObj.addPointsFromRoi(newRois.get(z), z);

        } else {
            outputObj.setT(t);
            outputObj.addParent(inputObj);
            inputObj.addChild(outputObj);

            for (int z : newRois.keySet())
                outputObj.addPointsFromRoi(newRois.get(z), z);

        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        boolean updateInputObjects = parameters.getValue(UPDATE_INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        double regMin = parameters.getValue(MINIMUM_REGULARISATION, workspace);
        double regMax = parameters.getValue(MAXIMUM_REGULARISATION, workspace);
        double alphaD = parameters.getValue(ALPHA_DERICHE, workspace);
        double displMax = parameters.getValue(MAXIMUM_DISPLACEMENT, workspace);
        double step = parameters.getValue(STEP_MULTIPLIER, workspace);
        double gradThresh = parameters.getValue(GRADIENT_THRESHOLD, workspace);
        int nIterations = parameters.getValue(NUM_ITERATIONS, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // Getting input image and objects
        Image inputImage = workspace.getImage(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        Objs outputObjects = new Objs(outputObjectsName, inputObjects);

        // If there are no input objects, creating an empty collection
        if (inputObjects.getFirst() == null) {
            workspace.addObjects(outputObjects);
            return Status.PASS;
        }

        // Setting up multithreading options
        int nThreads = multithread ? Prefs.getThreads() : 1;
        try (ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>())) {
            AtomicInteger count = new AtomicInteger(1);
            int total = inputObjects.size();

            for (Obj inputObject : inputObjects.values()) {
                Runnable task = () -> {
                    Obj outputObj = null;
                    if (!updateInputObjects)
                        outputObj = outputObjects.createAndAddNewObject(inputObject.getVolumeType());

                    process(inputObject, inputImage, outputObj, regMin, regMax, alphaD, displMax, step, gradThresh,
                            nIterations);

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
            pool.close();
        }

        if (showOutput) {
            if (updateInputObjects)
                inputObjects.convertToImageIDColours().show(false);
            else
                outputObjects.convertToImageIDColours().show(false);
        }

        // If selected, adding new Objs to the Workspace
        if (!updateInputObjects)
            workspace.addObjects(outputObjects);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(OBJECTS_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(UPDATE_INPUT_OBJECTS, this, true));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(IMAGE_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(SNAKE_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_REGULARISATION, this, 5));
        parameters.add(new DoubleP(MAXIMUM_REGULARISATION, this, 20));
        parameters.add(new DoubleP(ALPHA_DERICHE, this, 2));
        parameters.add(new DoubleP(MAXIMUM_DISPLACEMENT, this, 2));
        parameters.add(new DoubleP(STEP_MULTIPLIER, this, 0.99));
        parameters.add(new DoubleP(GRADIENT_THRESHOLD, this, 20));
        parameters.add(new IntegerP(NUM_ITERATIONS, this, 50));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(OBJECTS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(UPDATE_INPUT_OBJECTS));

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(IMAGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(SNAKE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_REGULARISATION));
        returnedParameters.add(parameters.getParameter(MAXIMUM_REGULARISATION));
        returnedParameters.add(parameters.getParameter(ALPHA_DERICHE));
        returnedParameters.add(parameters.getParameter(MAXIMUM_DISPLACEMENT));
        returnedParameters.add(parameters.getParameter(STEP_MULTIPLIER));
        returnedParameters.add(parameters.getParameter(GRADIENT_THRESHOLD));
        returnedParameters.add(parameters.getParameter(NUM_ITERATIONS));

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
        return null;
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

        if (!(boolean) parameters.getValue(UPDATE_INPUT_OBJECTS, workspace)) {
            String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
            String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
            returnedRelationships.add(parentChildRefs.getOrPut(inputObjectsName, outputObjectsName));

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
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace to which the contours will be fit.  The intensity of this image will contribute to the external forces applied to the contour.  For example, the contour will attempt to minimise the intensity along the path of the contour.");

        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from the workspace to which active contours will be fit.  Active contours are fit in 2D to the object points from the first slice.  As such, input objects can be stored in 3D space, but only a single slice will be fit.");

        parameters.get(UPDATE_INPUT_OBJECTS).setDescription(
                "When selected, the input objects will have their coordinates replaced with the coordinates from the fit contour.  Applied coordinates will be solid within the boundary of the associated contour.");

        parameters.get(OUTPUT_OBJECTS).setDescription("If \"" + UPDATE_INPUT_OBJECTS
                + "\" is not selected, this is the name with which the output contour objects will be stored in the workspace.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple input objects simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}