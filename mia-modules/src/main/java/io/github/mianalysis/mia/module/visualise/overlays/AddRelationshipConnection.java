package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.PointRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;


/**
* Adds a series of line overlays, representing various relationship scenarios.  Lines extend between centroids of two objects.  Depicted relationships can be between mutual child of the same parent object, between partner objects of between children and parents.
*/
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddRelationshipConnection extends AbstractOverlay {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image and object input";

	/**
	* Image onto which overlay will be rendered.  Input image will only be updated if "Apply to input image" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by "Output image".
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Controls what object-object relationships will be represented by the lines.  In all cases, lines are drawn between object centroids, although the line start and end points can be offset along the line when the "Offset by measurement" setting is selected:<br><br>- "Between children" Draw lines between all objects in two child object sets of a common parent object ("Parent objects").  This will result in all permutations of lines between one child object set and the other  The child object sets are selected with the "Child objects 1" and "Child objects 2" parameters.<br><br>- "Between partners" Draw lines between all partner objects.  The two partner object sets are selected using the "Partner objects 1" and "Partner objects 2" parameters.<br><br>- "Parent to child" Draw lines between parent objects and all their children.  Parent objects are selected using the "Parent objects" parameter and children using the "Child objects 1" parameter..<br>
	*/
    public static final String LINE_MODE = "Line mode";

	/**
	* Used to select the parent object of the two child object sets when in "Between children" mode, or to select the parent objects in "Parent to child" mode.
	*/
    public static final String PARENT_OBJECTS = "Parent objects";

	/**
	* Selects the first child objects set when in "Between children" mode, or the (only) child set when in "Parent to child" mode.
	*/
    public static final String CHILD_OBJECTS_1 = "Child objects 1";

	/**
	* Selects the second child objects set when in "Between children" mode.
	*/
    public static final String CHILD_OBJECTS_2 = "Child objects 2";

	/**
	* Selects the first partner objects set when in "Between partners" mode.
	*/
    public static final String PARTNER_OBJECTS_1 = "Partner objects 1";

	/**
	* Selects the second partner objects set when in "Between partners" mode.
	*/
    public static final String PARTNER_OBJECTS_2 = "Partner objects 2";


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
	* Controls how the line is displayed between the centroids of the relevant two objects:<br><br>- "Full line" Draws a complete line between the two centroids (unless "Offset by measurement" is selected, in which case the line won't necessarily begin at the centroid).<br><br>- "Half line" Draws a line between the first object (either "Child objects 1", "Partner objects 1" or "Parent objects" when in "Between children", "Between partners" or "Parent to child" modes, respectively) and the mid-point between the two relevant objects.  Any offsets applied when "Offset by measurement" is selected still apply.<br><br>- "Midpoint dot" Draws a dot half way between the two relevant objects (no line is drawn).<br>
	*/
    public static final String RENDER_MODE = "Render mode";

	/**
	* Width of the rendered lines.  Specified in pixel units.
	*/
    public static final String LINE_WIDTH = "Line width";

	/**
	* Size of each overlay marker.  Choices are: Tiny, Small, Medium, Large, Extra large.
	*/
    public static final String POINT_SIZE = "Point size";

	/**
	* Type of overlay marker used to represent each object.  Choices are: Circle, Cross, Dot, Hybrid.
	*/
    public static final String POINT_TYPE = "Point type";

	/**
	* When selected, the lines at either end can start a fraction of the way between the two relevant object centroids.  Separate offsets are applied at each end, with measurements providing the offset values selected using the "Measurement name 1" and "Measurement name 2" parameters.  For example.  This is useful when it is preferable to not have the line extend all the way between centroids.
	*/
    public static final String OFFSET_BY_MEASUREMENT = "Offset by measurement";

	/**
	* Object measurement specifying offset to be applied to the line start point.  Offsets are fractional values, specifying the proportion of the line to ignore.  For example, dual offsets of 0.25 will result in a line half the usual length.
	*/
    public static final String MEASUREMENT_NAME_1 = "Measurement name 1";

	/**
	* Object measurement specifying offset to be applied to the line end point.  Offsets are fractional values, specifying the proportion of the line to ignore.  For example, dual offsets of 0.25 will result in a line half the usual length.
	*/
    public static final String MEASUREMENT_NAME_2 = "Measurement name 2";

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

    public interface LineModes {
        String BETWEEN_CHILDREN = "Between children";
        String BETWEEN_PARTNERS = "Between partners";
        String PARENT_TO_CHILD = "Parent to child";

        String[] ALL = new String[] { BETWEEN_CHILDREN, BETWEEN_PARTNERS, PARENT_TO_CHILD };

    }

    public interface RenderModes {
        String FULL_LINE = "Full line";
        String HALF_LINE = "Half line";
        String MIDPOINT_DOT = "Midpoint dot";

        String[] ALL = new String[] { FULL_LINE, HALF_LINE, MIDPOINT_DOT };

    }

    public interface PointSizes extends AddObjectCentroid.PointSizes {
    }

    public interface PointTypes extends AddObjectCentroid.PointTypes {
    }

    public AddRelationshipConnection(Modules modules) {
        super("Add relationship connection", modules);
    }

    public static void addParentChildOverlay(ImagePlus ipl, ObjsI inputObjects, String childObjectsName,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, HashMap<Integer, Color> colours, boolean renderInAllFrames,
            boolean multithread) {
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
            for (ObjI object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(object.getID());

                    addParentChildOverlay(object, childObjectsName, finalIpl, colour, renderMode, lineWidth, pointSize,
                            pointType, offset, measName1, measName2, renderInAllFrames);

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

    public static void addParentChildOverlay(ObjI object, String childObjectsName, ImagePlus ipl, Color colour,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames ? 0 : object.getT() + 1;

        // Running through each slice of this object
        for (ObjI childObj : object.getChildren(childObjectsName).values()) {
            switch (renderMode) {
                case RenderModes.FULL_LINE:
                    drawFullLine(ipl, object, childObj, t, colour, lineWidth);
                    break;
                case RenderModes.HALF_LINE:
                    drawHalfLine(ipl, object, childObj, t, colour, lineWidth, offset, measName1, measName2);
                    break;
                case RenderModes.MIDPOINT_DOT:
                    drawMidpointDot(ipl, object, childObj, t, colour, pointSize, pointType, offset, measName1,
                            measName2);
                    break;
            }
        }
    }

    public static void addSiblingOverlay(ImagePlus ipl, ObjsI inputObjects, String childObjects1Name,
            String childObjects2Name, String renderMode, double lineWidth, String pointSize, String pointType,
            boolean offset, String measName1, String measName2, HashMap<Integer, Color> colours,
            boolean renderInAllFrames, boolean multithread) {
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
            for (ObjI object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(object.getID());

                    addSiblingOverlay(object, childObjects1Name, childObjects2Name, finalIpl, colour, renderMode,
                            lineWidth, pointSize, pointType, offset, measName1, measName2, renderInAllFrames);

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

    public static void addSiblingOverlay(ObjI object, String childObjects1Name, String childObjects2Name, ImagePlus ipl,
            Color colour, String renderMode, double lineWidth, String pointSize, String pointType, boolean offset,
            String measName1, String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        // Running through each slice of this object
        for (ObjI childObj1 : object.getChildren(childObjects1Name).values()) {
            int t = renderInAllFrames ? 0 : childObj1.getT() + 1;
            for (ObjI childObj2 : object.getChildren(childObjects2Name).values()) {
                if (childObj1.getT() == childObj2.getT())
                switch (renderMode) {
                    case RenderModes.FULL_LINE:
                        drawFullLine(ipl, childObj1, childObj2, t, colour, lineWidth);
                        break;
                    case RenderModes.HALF_LINE:
                        drawHalfLine(ipl, childObj1, childObj2, t, colour, lineWidth, offset, measName1, measName2);
                        break;
                    case RenderModes.MIDPOINT_DOT:
                        drawMidpointDot(ipl, childObj1, childObj2, t, colour, pointSize, pointType, offset, measName1,
                                measName2);
                        break;
                }
            }
        }
    }

    public static void addPartnerOverlay(ImagePlus ipl, ObjsI partnerObjects1, String partnerObjects2Name,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, HashMap<Integer, Color> colours, boolean renderInAllFrames,
            boolean multithread) {
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
            for (ObjI partnerObject1 : partnerObjects1.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    Color colour = colours.get(partnerObject1.getID());

                    addPartnerOverlay(partnerObject1, partnerObjects2Name, finalIpl, colour, renderMode, lineWidth,
                            pointSize, pointType, offset, measName1, measName2, renderInAllFrames);

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

    public static void addPartnerOverlay(ObjI partnerObject1, String partnerObjects2Name, ImagePlus ipl, Color colour,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames ? 0 : partnerObject1.getT() + 1;

        // Running through each slice of this object
        for (ObjI partnerObject2 : partnerObject1.getPartners(partnerObjects2Name).values()) {
            switch (renderMode) {
                case RenderModes.FULL_LINE:
                    drawFullLine(ipl, partnerObject1, partnerObject2, t, colour, lineWidth);
                    break;
                case RenderModes.HALF_LINE:
                    drawHalfLine(ipl, partnerObject1, partnerObject2, t, colour, lineWidth, offset, measName1,
                            measName2);
                    break;
                case RenderModes.MIDPOINT_DOT:
                    drawMidpointDot(ipl, partnerObject1, partnerObject2, t, colour, pointSize, pointType, offset,
                            measName1, measName2);
                    break;
            }
        }
    }

    public static void drawFullLine(ImagePlus ipl, ObjI object1, ObjI object2, int t, Color colour, double lineWidth) {
        int nSlices = ipl.getNSlices();

        double x1 = object1.getXMean(true) + 0.5;
        double y1 = object1.getYMean(true) + 0.5;
        double x2 = object2.getXMean(true) + 0.5;
        double y2 = object2.getYMean(true) + 0.5;

        for (int z = 0; z < nSlices; z++) {
            Line line = new Line(x1, y1, x2, y2);
            if (ipl.isHyperStack()) {
                ipl.setPosition(1, z + 1, t);
                line.setPosition(1, z + 1, t);
            } else {
                int pos = Math.max(Math.max(1, z + 1), t);
                ipl.setPosition(pos);
                line.setPosition(pos);
            }

            line.setStrokeColor(colour);
            line.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(line);

        }
    }

    public static void drawHalfLine(ImagePlus ipl, ObjI object1, ObjI object2, int t, Color colour, double lineWidth,
            boolean offset, String measName1, String measName2) {
        int nSlices = ipl.getNSlices();

        double offsetValue = 0.5;
        if (offset) {
            double val1 = object1.getMeasurement(measName1).getValue();
            double val2 = object2.getMeasurement(measName2).getValue();
            offsetValue = val1 / (val1 + val2);
        }

        double x1 = object1.getXMean(true) + 0.5;
        double y1 = object1.getYMean(true) + 0.5;
        double x2 = object2.getXMean(true) + 0.5;
        double y2 = object2.getYMean(true) + 0.5;
        double x1p5 = (x2 - x1) * offsetValue + x1;
        double y1p5 = (y2 - y1) * offsetValue + y1;

        for (int z = 0; z < nSlices; z++) {
            Line line = new Line(x1, y1, x1p5, y1p5);
            if (ipl.isHyperStack()) {
                ipl.setPosition(1, z + 1, t);
                line.setPosition(1, z + 1, t);
            } else {
                int pos = Math.max(Math.max(1, z + 1), t);
                ipl.setPosition(pos);
                line.setPosition(pos);
            }

            line.setStrokeColor(colour);
            line.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(line);

        }
    }

    public static void drawMidpointDot(ImagePlus ipl, ObjI object1, ObjI object2, int t, Color colour, String pointSize,
            String pointType, boolean offset, String measName1, String measName2) {
        int sizeVal = AddObjectCentroid.getSize(pointSize);
        int typeVal = AddObjectCentroid.getType(pointType);

        int nSlices = ipl.getNSlices();

        double offsetValue = 0.5;
        if (offset) {
            double val1 = object1.getMeasurement(measName1).getValue();
            double val2 = object2.getMeasurement(measName2).getValue();
            offsetValue = val1 / (val1 + val2);
        }

        double x1 = object1.getXMean(true) + 0.5;
        double y1 = object1.getYMean(true) + 0.5;
        double x2 = object2.getXMean(true) + 0.5;
        double y2 = object2.getYMean(true) + 0.5;
        double x1p5 = (x2 - x1) * offsetValue + x1;
        double y1p5 = (y2 - y1) * offsetValue + y1;

        for (int z = 0; z < nSlices; z++) {
            PointRoi pointRoi = new PointRoi(x1p5 + 0.5, y1p5 + 0.5);
            pointRoi.setPointType(typeVal);
            pointRoi.setSize(sizeVal);

            if (ipl.isHyperStack()) {
                ipl.setPosition(1, z + 1, t);
                pointRoi.setPosition(1, z + 1, t);
            } else {
                int pos = Math.max(Math.max(1, z + 1), t);
                ipl.setPosition(pos);
                pointRoi.setPosition(pos);
            }

            pointRoi.setStrokeColor(colour);
            ipl.getOverlay().addElement(pointRoi);

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
        return "Adds a series of line overlays, representing various relationship scenarios.  Lines extend between centroids of two objects.  Depicted relationships can be between mutual child of the same parent object, between partner objects of between children and parents.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);

        // Getting input objects
        String lineMode = parameters.getValue(LINE_MODE,workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS,workspace);
        String childObjects1Name = parameters.getValue(CHILD_OBJECTS_1,workspace);
        String childObjects2Name = parameters.getValue(CHILD_OBJECTS_2,workspace);
        String partnerObjects1Name = parameters.getValue(PARTNER_OBJECTS_1,workspace);
        String partnerObjects2Name = parameters.getValue(PARTNER_OBJECTS_2,workspace);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        double opacity = parameters.getValue(OPACITY,workspace);
        String renderMode = parameters.getValue(RENDER_MODE,workspace);
        double lineWidth = parameters.getValue(LINE_WIDTH,workspace);
        String pointSize = parameters.getValue(POINT_SIZE,workspace);
        String pointType = parameters.getValue(POINT_TYPE,workspace);
        boolean offset = parameters.getValue(OFFSET_BY_MEASUREMENT,workspace);
        String measName1 = parameters.getValue(MEASUREMENT_NAME_1,workspace);
        String measName2 = parameters.getValue(MEASUREMENT_NAME_2,workspace);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        switch (lineMode) {
            case LineModes.BETWEEN_CHILDREN:
                ObjsI parentObjects = workspace.getObjects(parentObjectsName);
                HashMap<Integer, Color> colours = getColours(parentObjects, workspace);
                addSiblingOverlay(ipl, parentObjects, childObjects1Name, childObjects2Name, renderMode, lineWidth,
                        pointSize, pointType, offset, measName1, measName2, colours, renderInAllFrames,
                        multithread);
                break;

            case LineModes.BETWEEN_PARTNERS:
                ObjsI partnerObjects1 = workspace.getObjects(partnerObjects1Name);
                colours = getColours(partnerObjects1, workspace);
                addPartnerOverlay(ipl, partnerObjects1, partnerObjects2Name, renderMode, lineWidth, pointSize,
                        pointType, offset, measName1, measName2, colours, renderInAllFrames, multithread);
                break;

            case LineModes.PARENT_TO_CHILD:
                parentObjects = workspace.getObjects(parentObjectsName);
                colours = getColours(parentObjects, workspace);
                addParentChildOverlay(ipl, parentObjects, childObjects1Name, renderMode, lineWidth, pointSize,
                        pointType, offset, measName1, measName2, colours, renderInAllFrames, multithread);
                break;
        }

        ImageI outputImage = ImageFactory.createImage(outputImageName, ipl);

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
        parameters.add(new ChoiceP(LINE_MODE, this, LineModes.PARENT_TO_CHILD, LineModes.ALL));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_1, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_2, this));
        parameters.add(new InputObjectsP(PARTNER_OBJECTS_1, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_2, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new ChoiceP(RENDER_MODE, this, RenderModes.FULL_LINE, RenderModes.ALL));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new ChoiceP(POINT_SIZE, this, PointSizes.SMALL, PointSizes.ALL));
        parameters.add(new ChoiceP(POINT_TYPE, this, PointTypes.CIRCLE, PointTypes.ALL));
        parameters.add(new BooleanP(OFFSET_BY_MEASUREMENT, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME_1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME_2, this));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        String refObjectsName = "";

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(LINE_MODE));

        switch ((String) parameters.getValue(LINE_MODE,workspace)) {
            case LineModes.BETWEEN_CHILDREN:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

                ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS_1);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS,workspace));
                returnedParameters.add(childObjectsP);

                childObjectsP = parameters.getParameter(CHILD_OBJECTS_2);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS,workspace));
                returnedParameters.add(childObjectsP);

                refObjectsName = parameters.getValue(CHILD_OBJECTS_1,workspace);

                break;

            case LineModes.BETWEEN_PARTNERS:
                returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_1));

                PartnerObjectsP partnerObjectsP = parameters.getParameter(PARTNER_OBJECTS_2);
                partnerObjectsP.setPartnerObjectsName(parameters.getValue(PARTNER_OBJECTS_1,workspace));
                returnedParameters.add(partnerObjectsP);

                refObjectsName = parameters.getValue(PARTNER_OBJECTS_1,workspace);

                break;

            case LineModes.PARENT_TO_CHILD:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

                childObjectsP = parameters.getParameter(CHILD_OBJECTS_1);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS,workspace));
                returnedParameters.add(childObjectsP);

                refObjectsName = parameters.getValue(PARENT_OBJECTS,workspace);

                break;
        }

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));
            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.addAll(super.updateAndGetParameters(refObjectsName));

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(RENDER_MODE));
        switch ((String) parameters.getValue(RENDER_MODE,workspace)) {
            case RenderModes.FULL_LINE:
            case RenderModes.HALF_LINE:
                returnedParameters.add(parameters.getParameter(LINE_WIDTH));
                break;
            case RenderModes.MIDPOINT_DOT:
                returnedParameters.add(parameters.getParameter(POINT_SIZE));
                returnedParameters.add(parameters.getParameter(POINT_TYPE));
                break;
        }
        switch ((String) parameters.getValue(RENDER_MODE,workspace)) {
            case RenderModes.HALF_LINE:
            case RenderModes.MIDPOINT_DOT:
                returnedParameters.add(parameters.getParameter(OFFSET_BY_MEASUREMENT));
                if ((boolean) parameters.getValue(OFFSET_BY_MEASUREMENT,workspace)) {
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME_1));
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME_2));

                    String name1 = "";
                    String name2 = "";
                    switch ((String) parameters.getValue(LINE_MODE,workspace)) {
                        case LineModes.BETWEEN_CHILDREN:
                            name1 = parameters.getValue(CHILD_OBJECTS_1,workspace);
                            name2 = parameters.getValue(CHILD_OBJECTS_2,workspace);
                            break;
                        case LineModes.BETWEEN_PARTNERS:
                            name1 = parameters.getValue(PARTNER_OBJECTS_1,workspace);
                            name2 = parameters.getValue(PARTNER_OBJECTS_2,workspace);
                            break;
                        case LineModes.PARENT_TO_CHILD:
                            name1 = parameters.getValue(PARENT_OBJECTS,workspace);
                            name2 = parameters.getValue(CHILD_OBJECTS_1,workspace);
                            break;
                    }

                    ((ObjectMeasurementP) parameters.get(MEASUREMENT_NAME_1)).setObjectName(name1);
                    ((ObjectMeasurementP) parameters.get(MEASUREMENT_NAME_2)).setObjectName(name2);

                }
                break;
        }

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

        parameters.get(LINE_MODE).setDescription(
                "Controls what object-object relationships will be represented by the lines.  In all cases, lines are drawn between object centroids, although the line start and end points can be offset along the line when the \""
                        + OFFSET_BY_MEASUREMENT + "\" setting is selected:<br>"

                        + "<br>- \"" + LineModes.BETWEEN_CHILDREN
                        + "\" Draw lines between all objects in two child object sets of a common parent object (\""
                        + PARENT_OBJECTS
                        + "\").  This will result in all permutations of lines between one child object set and the other  The child object sets are selected with the \""
                        + CHILD_OBJECTS_1 + "\" and \"" + CHILD_OBJECTS_2 + "\" parameters.<br>"

                        + "<br>- \"" + LineModes.BETWEEN_PARTNERS
                        + "\" Draw lines between all partner objects.  The two partner object sets are selected using the \""
                        + PARTNER_OBJECTS_1 + "\" and \"" + PARTNER_OBJECTS_2 + "\" parameters.<br>"

                        + "<br>- \"" + LineModes.PARENT_TO_CHILD
                        + "\" Draw lines between parent objects and all their children.  Parent objects are selected using the \""
                        + PARENT_OBJECTS + "\" parameter and children using the \"" + CHILD_OBJECTS_1
                        + "\" parameter..<br>"

        );

        parameters.get(PARENT_OBJECTS)
                .setDescription("Used to select the parent object of the two child object sets when in \""
                        + LineModes.BETWEEN_CHILDREN + "\" mode, or to select the parent objects in \""
                        + LineModes.PARENT_TO_CHILD + "\" mode.");

        parameters.get(CHILD_OBJECTS_1)
                .setDescription("Selects the first child objects set when in \"" + LineModes.BETWEEN_CHILDREN
                        + "\" mode, or the (only) child set when in \"" + LineModes.PARENT_TO_CHILD + "\" mode.");

        parameters.get(CHILD_OBJECTS_2).setDescription(
                "Selects the second child objects set when in \"" + LineModes.BETWEEN_CHILDREN + "\" mode.");

        parameters.get(PARTNER_OBJECTS_1).setDescription(
                "Selects the first partner objects set when in \"" + LineModes.BETWEEN_PARTNERS + "\" mode.");

        parameters.get(PARTNER_OBJECTS_2).setDescription(
                "Selects the second partner objects set when in \"" + LineModes.BETWEEN_PARTNERS + "\" mode.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(RENDER_MODE).setDescription(
                "Controls how the line is displayed between the centroids of the relevant two objects:<br>"

                        + "<br>- \"" + RenderModes.FULL_LINE
                        + "\" Draws a complete line between the two centroids (unless \"" + OFFSET_BY_MEASUREMENT
                        + "\" is selected, in which case the line won't necessarily begin at the centroid).<br>"

                        + "<br>- \"" + RenderModes.HALF_LINE + "\" Draws a line between the first object (either \""
                        + CHILD_OBJECTS_1 + "\", \"" + PARTNER_OBJECTS_1 + "\" or \"" + PARENT_OBJECTS + "\" when in \""
                        + LineModes.BETWEEN_CHILDREN + "\", \"" + LineModes.BETWEEN_PARTNERS + "\" or \""
                        + LineModes.PARENT_TO_CHILD
                        + "\" modes, respectively) and the mid-point between the two relevant objects.  Any offsets applied when \""
                        + OFFSET_BY_MEASUREMENT + "\" is selected still apply.<br>"

                        + "<br>- \"" + RenderModes.MIDPOINT_DOT
                        + "\" Draws a dot half way between the two relevant objects (no line is drawn).<br>");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(POINT_SIZE).setDescription(
                "Size of each overlay marker.  Choices are: " + String.join(", ", PointSizes.ALL) + ".");

        parameters.get(POINT_TYPE).setDescription("Type of overlay marker used to represent each object.  Choices are: "
                + String.join(", ", PointTypes.ALL) + ".");

        parameters.get(OFFSET_BY_MEASUREMENT).setDescription("When selected, the lines at either end can start a fraction of the way between the two relevant object centroids.  Separate offsets are applied at each end, with measurements providing the offset values selected using the \""+MEASUREMENT_NAME_1+"\" and \""+MEASUREMENT_NAME_2+"\" parameters.  For example.  This is useful when it is preferable to not have the line extend all the way between centroids.");

        parameters.get(MEASUREMENT_NAME_1).setDescription("Object measurement specifying offset to be applied to the line start point.  Offsets are fractional values, specifying the proportion of the line to ignore.  For example, dual offsets of 0.25 will result in a line half the usual length.");

        parameters.get(MEASUREMENT_NAME_2).setDescription("Object measurement specifying offset to be applied to the line end point.  Offsets are fractional values, specifying the proportion of the line to ignore.  For example, dual offsets of 0.25 will result in a line half the usual length.");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription(
                "Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
