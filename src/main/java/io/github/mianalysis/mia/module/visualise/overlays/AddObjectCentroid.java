package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.PointRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;


/**
* Adds an overlay to the specified input image representing each object by a single marker placed at the centroid of that object.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddObjectCentroid extends AbstractOverlay {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image and object input";

	/**
	* Image onto which overlay will be rendered.  Input image will only be updated if "Apply to input image" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by "Output image".
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Objects to represent as overlays.
	*/
    public static final String INPUT_OBJECTS = "Input objects";


	/**
	* 
	*/
    public static final String OUTPUT_SEPARATOR = "Image output";

	/**
	* Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.
	*/
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

	/**
	* The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String RENDERING_SEPARATOR = "Overlay rendering";

	/**
	* Size of each overlay marker.  Choices are: Tiny, Small, Medium, Large, Extra large.
	*/
    public static final String POINT_SIZE = "Point size";

	/**
	* Type of overlay marker used to represent each object.  Choices are: Circle, Cross, Dot, Hybrid.
	*/
    public static final String POINT_TYPE = "Point type";

	/**
	* Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.
	*/
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";


	/**
	* 
	*/
    public static final String EXECUTION_SEPARATOR = "Execution controls";

	/**
	* Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.
	*/
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface PointSizes {
        String TINY = "Tiny";
        String SMALL = "Small";
        String MEDIUM = "Medium";
        String LARGE = "Large";
        String EXTRA_LARGE = "Extra large";

        String[] ALL = new String[] { TINY, SMALL, MEDIUM, LARGE, EXTRA_LARGE };

    }

    public interface PointTypes {
        String CIRCLE = "Circle";
        String CROSS = "Cross";
        String DOT = "Dot";
        String HYBRID = "Hybrid";

        String[] ALL = new String[] { CIRCLE, CROSS, DOT, HYBRID };

    }

    public AddObjectCentroid(Modules modules) {
        super("Add object centroid", modules);
    }

    public static void addOverlay(ImagePlus ipl, Objs inputObjects, HashMap<Integer, Color> colours, String size,
            String type, boolean renderInAllFrames, boolean multithread) {
        // Adding the overlay element
        try {
            // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
            // be a standard ImagePlus)
            if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
                ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
            }

            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            for (Obj object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(object.getID());

                    addOverlay(object, finalIpl, colour, size, type, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }
    }

    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, String size, String type,
            boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);

        int sizeVal = getSize(size);
        int typeVal = getType(type);

        // Getting coordinates to plot
        int z = (int) Math.round(zMean + 1);
        int t = object.getT() + 1;

        if (renderInAllFrames)
            t = 0;

        // Adding circles where the object centroids are
        PointRoi pointRoi = new PointRoi(xMean + 0.5, yMean + 0.5);
        pointRoi.setPointType(typeVal);
        pointRoi.setSize(sizeVal);

        if (ipl.isHyperStack()) {
            pointRoi.setPosition(1, z, t);
        } else {
            int pos = Math.max(Math.max(1, z), t);
            pointRoi.setPosition(pos);
        }
        pointRoi.setStrokeColor(colour);
        ipl.getOverlay().addElement(pointRoi);

    }

    static int getSize(String size) {
        switch (size) {
            case PointSizes.TINY:
                return 0;
            case PointSizes.SMALL:
            default:
                return 1;
            case PointSizes.MEDIUM:
                return 2;
            case PointSizes.LARGE:
                return 3;
            case PointSizes.EXTRA_LARGE:
                return 4;
        }
    }

    static int getType(String type) {
        switch (type) {
            case PointTypes.HYBRID:
                return 0;
            case PointTypes.CROSS:
                return 1;
            case PointTypes.DOT:
                return 2;
            case PointTypes.CIRCLE:
            default:
                return 3;
        }
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image representing each object by a single marker placed at the centroid of that object.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String pointSize = parameters.getValue(POINT_SIZE, workspace);
        String pointType = parameters.getValue(POINT_TYPE, workspace);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);

        addOverlay(ipl, inputObjects, colours, pointSize, pointType, renderInAllFrames, multithread);

        Image outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

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
        parameters.add(new ChoiceP(POINT_SIZE, this, PointSizes.SMALL, PointSizes.ALL));
        parameters.add(new ChoiceP(POINT_TYPE, this, PointTypes.CIRCLE, PointTypes.ALL));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(POINT_SIZE));
        returnedParameters.add(parameters.getParameter(POINT_TYPE));
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

        parameters.get(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(POINT_SIZE).setDescription(
                "Size of each overlay marker.  Choices are: " + String.join(", ", PointSizes.ALL) + ".");

        parameters.get(POINT_TYPE).setDescription("Type of overlay marker used to represent each object.  Choices are: "
                + String.join(", ", PointTypes.ALL) + ".");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription(
                "Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
