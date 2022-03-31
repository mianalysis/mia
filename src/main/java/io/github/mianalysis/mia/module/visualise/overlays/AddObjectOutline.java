package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddObjectOutline extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String REDUCE_LINE_COMPLEXITY = "Reduce line complexity";
    public static final String LINE_INTERPOLATION = "Line interpolation";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddObjectOutline(Modules modules) {
        super("Add object outline", modules);
    }

    public interface ColourModes extends AbstractOverlay.ColourModes {
    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public static void addOverlay(ImagePlus ipl, Objs inputObjects, double lineInterpolation, double lineWidth,
            HashMap<Integer, Color> colours, boolean renderInAllFrames, boolean multithread) {
        String name = new AddObjectOutline(null).getName();

        // Adding the overlay element
        try {
            // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
            // be a standard ImagePlus)
            if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
                ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
            }

            // Counting number of overlays to add
            int tempTotal = 0;
            int nFrames = renderInAllFrames ? ipl.getNFrames() : 1;
            for (Obj object : inputObjects.values()) {
                double[][] extents = object.getExtents(true, false);
                tempTotal = tempTotal + ((int) extents[2][1] - (int) extents[2][0] + 1) * nFrames;
            }
            int total = tempTotal;

            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger(1);
            for (Obj object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    int t1 = object.getT() + 1;
                    int t2 = object.getT() + 1;
                    if (renderInAllFrames) {
                        t1 = 1;
                        t2 = finalIpl.getNFrames();
                    }

                    // Running through each slice of this object
                    double[][] range = object.getExtents(true, false);

                    // If this is a 2D object, add it to all slices
                    int minZ = (int) Math.floor(range[2][0]);
                    int maxZ = (int) Math.floor(range[2][1]);
                    if (object.is2D()) {
                        minZ = 0;
                        maxZ = finalIpl.getNSlices() - 1;
                    }

                    for (int t = t1; t <= t2; t++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            Color colour = colours.get(object.getID());
                            addOverlay(object, finalIpl, colour, lineInterpolation, lineWidth, t, z);
                            writeProgressStatus(count.getAndIncrement(), total, "objects", name);

                        }
                    }
                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return;
        }
    }

    static void addOverlay(Obj object, ImagePlus ipl, Color colour, double lineInterpolation, double lineWidth, int t,
            int z) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        Roi polyRoi = null;
        if (object.is2D()) {
            polyRoi = object.getRoi(0);
        } else {
            polyRoi = object.getRoi(z);
        }

        // If the object doesn't have any pixels in this plane, skip it
        if (polyRoi == null)
            return;

        drawOverlay(polyRoi, z, t, ipl, colour, lineInterpolation, lineWidth);

    }

    static void drawOverlay(Roi roi, int z, int t, ImagePlus ipl, Color colour, double lineInterpolation,
            double lineWidth) {

        if (roi.getType() == Roi.COMPOSITE) {
            ShapeRoi shapeRoi = new ShapeRoi(roi);
            for (Roi partRoi : shapeRoi.getRois()) {
                drawOverlay(partRoi, z, t, ipl, colour, lineInterpolation, lineWidth);
            }

        } else {
            // Applying interpolation to reduce complexity of line
            if (lineInterpolation != 1 && roi.getType() == Roi.TRACED_ROI
                    && roi.getFloatPolygon().npoints > lineInterpolation * 2) {
                roi = new PolygonRoi(roi.getInterpolatedPolygon(lineInterpolation, true), roi.getType());
            }

            if (ipl.isHyperStack()) {
                roi.setPosition(1, z + 1, t);
                ipl.setPosition(1, z + 1, t);
            } else {
                int pos = Math.max(Math.max(1, z + 1), t);
                roi.setPosition(pos);
                ipl.setPosition(pos);
            }

            roi.setStrokeColor(colour);
            roi.setStrokeWidth(lineWidth);

            ipl.getOverlay().addElement(roi);

        }
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image showing the outline of each specified input object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        boolean reduceLineComplexity = parameters.getValue(REDUCE_LINE_COMPLEXITY);
        double lineInterpolation = parameters.getValue(LINE_INTERPOLATION);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (!reduceLineComplexity)
            lineInterpolation = 1;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects);

        addOverlay(ipl, inputObjects, lineInterpolation, lineWidth, colours, renderInAllFrames, multithread);

        Image outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (!applyToInput && addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new BooleanP(REDUCE_LINE_COMPLEXITY, this, false));
        parameters.add(new DoubleP(LINE_INTERPOLATION, this, 1));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));
            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REDUCE_LINE_COMPLEXITY));
        if ((boolean) parameters.getValue(REDUCE_LINE_COMPLEXITY)) {
            returnedParameters.add(parameters.getParameter(LINE_INTERPOLATION));
        }
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.getParameter(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.getParameter(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");

        parameters.getParameter(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.getParameter(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.getParameter(REDUCE_LINE_COMPLEXITY).setDescription(
                "When enabled the contour can be plotted using a reduced number of points.  This is useful for simplifying outlines for large objects, where a reduction in line precision isn't problematic.  Higher interpolation values will reduce the memory required to store/display overlays.");

        parameters.getParameter(LINE_INTERPOLATION)
                .setDescription("Specifies the interval between plotted points on the contour line.");

        parameters.getParameter(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.getParameter(RENDER_IN_ALL_FRAMES).setDescription(
                "Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");

        parameters.getParameter(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
