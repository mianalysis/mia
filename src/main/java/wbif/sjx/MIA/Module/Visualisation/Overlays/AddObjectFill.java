package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

public class AddObjectFill extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddObjectFill(ModuleCollection modules) {
        super("Add object fill", modules);
    }

    public interface ColourModes extends AbstractOverlay.ColourModes {
    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public static void addOverlay(ImagePlus ipl, ObjCollection inputObjects, HashMap<Integer, Float> hues,
            double opacity, boolean renderInAllFrames, boolean multithread) {
        String name = new AddObjectFill(null).getName();

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
                    float hue = hues.get(object.getID());
                    addOverlay(object, finalIpl, ColourFactory.getColour(hue, opacity), renderInAllFrames);
                    writeStatus("Rendered " + count + " of " + total + " ("
                            + Math.floorDiv(100 * count.getAndIncrement(), total) + "%)", name);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t1 = object.getT() + 1;
        int t2 = object.getT() + 1;
        if (renderInAllFrames) {
            t1 = 1;
            t2 = ipl.getNFrames();
        }

        // Running through each slice of this object
        double[][] range = object.getExtents(true, false);

        // If this is a 2D object, add it to all slices
        int minZ = (int) Math.floor(range[2][0]);
        int maxZ = (int) Math.floor(range[2][1]);
        if (object.is2D()) {
            minZ = 0;
            maxZ = ipl.getNSlices() - 1;
        }

        for (int t = t1; t <= t2; t++) {
            for (int z = minZ; z <= maxZ; z++) {
                Roi polyRoi = null;
                if (object.is2D()) {
                    polyRoi = object.getRoi(0);
                } else {
                    polyRoi = object.getRoi(z);
                }

                // If the object doesn't have any pixels in this plane, skip it
                if (polyRoi == null)
                    continue;

                if (ipl.isHyperStack()) {
                    polyRoi.setPosition(1, z + 1, t);
                    ipl.setPosition(1, z + 1, t);
                } else {
                    int pos = Math.max(Math.max(1, z + 1), t);
                    polyRoi.setPosition(pos);
                    ipl.setPosition(pos);
                }

                polyRoi.setFillColor(colour);

                ipl.getOverlay().addElement(polyRoi);

            }
        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image showing the extent of each specified input object as a filled shape.  The opacity of the filled shape can be varied to see the image underneath.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        double opacity = parameters.getValue(OPACITY);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Float> hues = getHues(inputObjects);

        addOverlay(ipl, inputObjects, hues, opacity, renderInAllFrames, multithread);

        Image outputImage = new Image(outputImageName, ipl);

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

        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();
                
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

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

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));
        returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));

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
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
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
        parameters.getParameter(INPUT_IMAGE).setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \"" + APPLY_TO_INPUT
        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
        + OUTPUT_IMAGE + "\".");
        
        parameters.getParameter(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");
        
        parameters.getParameter(APPLY_TO_INPUT).setDescription("Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");
        
        parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE).setDescription("If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");
        
        parameters.getParameter(OUTPUT_IMAGE).setDescription("The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");
        
        parameters.getParameter(RENDER_IN_ALL_FRAMES).setDescription("Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");
        
        parameters.getParameter(ENABLE_MULTITHREADING).setDescription("Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");
        
    }
}
