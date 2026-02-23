package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Arrow;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;


/**
* Adds an overlay to the specified input image with each object represented by an arrow.  The size, colour and orientation of each arrow can be fixed or based on a measurement value.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddArrows extends AbstractOverlay {

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
    public static final String OUTPUT_SEPARATOR = "ImageI output";

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
	* Source for arrow orientation values:<br><ul><li>"Measurement" Orientation of arrows will be based on the measurement specified by the parameter "Measurement for orientation" for each object.</li><li>"Parent measurement" Orientation of arrows will be based on the measurement specified by the parameter "Measurement for orientation" taken from a parent of each object.  The parent object providing this measurement is specified by the parameter "Parent object for orientation".</li></ul>
	*/
    public static final String ORIENTATION_MODE = "Arrow orientation mode";

	/**
	* Parent objects providing the measurements on which the orientation of the arrows are based.
	*/
    public static final String PARENT_OBJECT_FOR_ORIENTATION = "Parent object for orientation";

	/**
	* Measurement that defines the orientation of each arrow.  Measurements should be supplied in degree units.
	*/
    public static final String MEASUREMENT_FOR_ORIENTATION = "Measurement for orientation";

	/**
	* Method for determining the length of arrows:<br><ul><li>"Fixed value" All arrows are the same length.  Length is controlled by the "Length value (px)" parameter.</li><li>"Measurement" Arrow length is proportional to the measurement value specified by the "Measurement for length" parameter.  Absolute arrow lengths are adjusted by the "Arrow length scale" multiplication factor.</li><li>"Parent measurement" Arrow length is proportional to a parent object measurement value.  The parent is specified by the "Parent object for length" parameter and the measurement value by "Measurement for length".  Absolute arrow lengths are adjusted by the "Arrow length scale" multiplication factor.</li></ul>
	*/
    public static final String LENGTH_MODE = "Arrow length mode";
    public static final String LENGTH_VALUE = "Length value (px)";

	/**
	* Parent objects from which the arrow length measurements will be taken.
	*/
    public static final String PARENT_OBJECT_FOR_LENGTH = "Parent object for length";

	/**
	* Measurement value that will be used to control the arrow length.  This value is adjusted using the "Arrow length scale" muliplication factor.
	*/
    public static final String MEASUREMENT_FOR_LENGTH = "Measurement for length";

	/**
	* Measurement values will be multiplied by this value prior to being used to control the arrow length.  Each arrow will be <i>MeasurementValue*LengthScale</i> pixels long.
	*/
    public static final String LENGTH_SCALE = "Arrow length scale";

	/**
	* Size of the arrow head.  This should be an integer between 0 and 30, where 0 is the smallest possible head and 30 is the largest.
	*/
    public static final String HEAD_SIZE = "Head size";

	/**
	* Width of the rendered lines.  Specified in pixel units.
	*/
    public static final String LINE_WIDTH = "Line width";

	/**
	* Display overlay elements in all frames, irrespective of whether each object is present in that frame.
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

    public AddArrows(ModulesI modules) {
        super("Add arrows", modules);
    }

    public interface OrientationModes {
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[] { PARENT_MEASUREMENT, MEASUREMENT };

    }

    public interface LengthModes {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[] { FIXED_VALUE, PARENT_MEASUREMENT, MEASUREMENT };

    }

    public static void addOverlay(ObjI object, ImagePlus ipl, Color colour, double lineWidth, double orientation,
            double arrowLength, double headSize) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        double oriRads = Math.toRadians(orientation);

        // Adding each point
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);

        int z = (int) Math.round(zMean + 1);
        int t = object.getT() + 1;

        // Getting end point
        double x2 = arrowLength * Math.cos(oriRads);
        double y2 = arrowLength * Math.sin(oriRads);

        Arrow arrow = new Arrow(xMean, yMean, xMean - x2, yMean - y2);
        arrow.setHeadSize(headSize);
        arrow.setStrokeColor(colour);
        arrow.setStrokeWidth(lineWidth);

        if (ipl.isHyperStack()) {
            arrow.setPosition(1, (int) z, t);
        } else {
            int pos = Math.max(Math.max(1, (int) z), t);
            arrow.setPosition(pos);
        }
        ipl.getOverlay().addElement(arrow);

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
        return "Adds an overlay to the specified input image with each object represented by an arrow.  The size, colour and orientation of each arrow can be fixed or based on a measurement value.";

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String orientationMode = parameters.getValue(ORIENTATION_MODE,workspace);
        String parentForOri = parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION,workspace);
        String measForOri = parameters.getValue(MEASUREMENT_FOR_ORIENTATION,workspace);
        String lengthMode = parameters.getValue(LENGTH_MODE,workspace);
        double lengthValue = parameters.getValue(LENGTH_VALUE,workspace);
        String parentForLength = parameters.getValue(PARENT_OBJECT_FOR_LENGTH,workspace);
        String measurementForLength = parameters.getValue(MEASUREMENT_FOR_LENGTH,workspace);
        double lengthScale = parameters.getValue(LENGTH_SCALE,workspace);
        int headSize = parameters.getValue(HEAD_SIZE,workspace);

        double lineWidth = parameters.getValue(LINE_WIDTH,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Adding the overlay element
        try {
            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (ObjI object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(object.getID());
                    double orientation = 0;
                    switch (orientationMode) {
                        case OrientationModes.MEASUREMENT:
                            orientation = object.getMeasurement(measForOri).getValue();
                            break;
                        case OrientationModes.PARENT_MEASUREMENT:
                            orientation = object.getParent(parentForOri).getMeasurement(measForOri).getValue();
                            break;
                    }

                    double length = 0;
                    switch (lengthMode) {
                        case LengthModes.FIXED_VALUE:
                            length = lengthValue;
                            break;
                        case LengthModes.MEASUREMENT:
                            length = object.getMeasurement(measurementForLength).getValue();
                            break;
                        case LengthModes.PARENT_MEASUREMENT:
                            length = object.getParent(parentForLength).getMeasurement(measurementForLength).getValue();
                            break;
                    }

                    length = length * lengthScale;

                    addOverlay(object, finalIpl, colour, lineWidth, orientation, length, headSize);

                    writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return Status.FAIL;
        }

        ImageI outputImage = ImageFactories.getDefaultFactory().create(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.showAsIs();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new ChoiceP(ORIENTATION_MODE, this, OrientationModes.MEASUREMENT, OrientationModes.ALL));

        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_ORIENTATION, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_ORIENTATION, this));
        parameters.add(new ChoiceP(LENGTH_MODE, this, LengthModes.MEASUREMENT, LengthModes.ALL));

        parameters.add(new DoubleP(LENGTH_VALUE, this, 5d));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LENGTH, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LENGTH, this));
        parameters.add(new DoubleP(LENGTH_SCALE, this, 1d));
        parameters.add(new IntegerP(HEAD_SIZE, this, 3));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));
        
        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
        switch ((String) parameters.getValue(ORIENTATION_MODE,workspace)) {
            case OrientationModes.MEASUREMENT:
                ObjectMeasurementP oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                oriMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS,workspace));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                break;

            case OrientationModes.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION));
                ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION);
                parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS,workspace));

                oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                oriMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION,workspace));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                break;
        }

        returnedParameters.add(parameters.getParameter(LENGTH_MODE));
        switch ((String) parameters.getValue(LENGTH_MODE,workspace)) {
            case LengthModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(LENGTH_VALUE));
                break;

            case LengthModes.MEASUREMENT:
                ObjectMeasurementP lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                lengthMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS,workspace));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LENGTH));
                break;

            case LengthModes.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LENGTH));
                ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_LENGTH);
                parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS,workspace));

                lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                lengthMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_LENGTH,workspace));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LENGTH));
                break;
        }

        returnedParameters.add(parameters.getParameter(LENGTH_SCALE));
        returnedParameters.add(parameters.getParameter(HEAD_SIZE));
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

        parameters.get(ORIENTATION_MODE).setDescription("Source for arrow orientation values:<br><ul>"

                + "<li>\"" + OrientationModes.MEASUREMENT
                + "\" Orientation of arrows will be based on the measurement specified by the parameter \""
                + MEASUREMENT_FOR_ORIENTATION + "\" for each object.</li>"

                + "<li>\"" + OrientationModes.PARENT_MEASUREMENT
                + "\" Orientation of arrows will be based on the measurement specified by the parameter \""
                + MEASUREMENT_FOR_ORIENTATION
                + "\" taken from a parent of each object.  The parent object providing this measurement is specified by the parameter \""
                + PARENT_OBJECT_FOR_ORIENTATION + "\".</li></ul>");

        parameters.get(PARENT_OBJECT_FOR_ORIENTATION).setDescription(
                "Parent objects providing the measurements on which the orientation of the arrows are based.");

        parameters.get(MEASUREMENT_FOR_ORIENTATION).setDescription(
                "Measurement that defines the orientation of each arrow.  Measurements should be supplied in degree units.");

        parameters.get(LENGTH_MODE).setDescription("Method for determining the length of arrows:<br><ul>"

                + "<li>\"" + LengthModes.FIXED_VALUE
                + "\" All arrows are the same length.  Length is controlled by the \"" + LENGTH_VALUE
                + "\" parameter.</li>"

                + "<li>\"" + LengthModes.MEASUREMENT
                + "\" Arrow length is proportional to the measurement value specified by the \""
                + MEASUREMENT_FOR_LENGTH + "\" parameter.  Absolute arrow lengths are adjusted by the \"" + LENGTH_SCALE
                + "\" multiplication factor.</li>"

                + "<li>\"" + LengthModes.PARENT_MEASUREMENT
                + "\" Arrow length is proportional to a parent object measurement value.  The parent is specified by the \""
                + PARENT_OBJECT_FOR_LENGTH + "\" parameter and the measurement value by \"" + MEASUREMENT_FOR_LENGTH
                + "\".  Absolute arrow lengths are adjusted by the \"" + LENGTH_SCALE
                + "\" multiplication factor.</li></ul>");

        parameters.get(LENGTH_VALUE).setDescription("Fixed value specifying the length of all arrows in pixel units.");

        parameters.get(PARENT_OBJECT_FOR_LENGTH)
                .setDescription("Parent objects from which the arrow length measurements will be taken.");

        parameters.get(MEASUREMENT_FOR_LENGTH).setDescription(
                "Measurement value that will be used to control the arrow length.  This value is adjusted using the \""
                        + LENGTH_SCALE + "\" muliplication factor.");

        parameters.get(LENGTH_SCALE).setDescription(
                "Measurement values will be multiplied by this value prior to being used to control the arrow length.  Each arrow will be <i>MeasurementValue*LengthScale</i> pixels long.");

        parameters.get(HEAD_SIZE).setDescription(
                "Size of the arrow head.  This should be an integer between 0 and 30, where 0 is the smallest possible head and 30 is the largest.");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription(
                "Display overlay elements in all frames, irrespective of whether each object is present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
