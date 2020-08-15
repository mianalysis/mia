package wbif.sjx.MIA.Module.Visualisation.Overlays;

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
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.PartnerObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

public class AddRelationshipConnection extends Overlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String LINE_MODE = "Line mode";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS_1 = "Child objects 1";
    public static final String CHILD_OBJECTS_2 = "Child objects 2";
    public static final String PARTNER_OBJECTS_1 = "Partner objects 1";
    public static final String PARTNER_OBJECTS_2 = "Partner objects 2";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String RENDER_MODE = "Render mode";
    public static final String LINE_WIDTH = "Line width";
    public static final String POINT_SIZE = "Point size";
    public static final String POINT_TYPE = "Point type";
    public static final String OFFSET_BY_MEASUREMENT = "Offset by measurement";
    public static final String MEASUREMENT_NAME_1 = "Measurement name 1";
    public static final String MEASUREMENT_NAME_2 = "Measurement name 2";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
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

    public AddRelationshipConnection(ModuleCollection modules) {
        super("Add relationship connection", modules);
    }

    public static void addParentChildOverlay(ImagePlus ipl, ObjCollection inputObjects, String childObjectsName,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, HashMap<Integer, Float> hues, double opacity, boolean renderInAllFrames,
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
            for (Obj object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue, opacity);

                    addParentChildOverlay(object, childObjectsName, finalIpl, colour, renderMode, lineWidth, pointSize,
                            pointType, offset, measName1, measName2, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addParentChildOverlay(Obj object, String childObjectsName, ImagePlus ipl, Color colour,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames ? 0 : object.getT() + 1;

        // Running through each slice of this object
        for (Obj childObj : object.getChildren(childObjectsName).values()) {
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

    public static void addSiblingOverlay(ImagePlus ipl, ObjCollection inputObjects, String childObjects1Name,
            String childObjects2Name, String renderMode, double lineWidth, String pointSize, String pointType,
            boolean offset, String measName1, String measName2, HashMap<Integer, Float> hues, double opacity,
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
            for (Obj object : inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue, opacity);

                    addSiblingOverlay(object, childObjects1Name, childObjects2Name, finalIpl, colour, renderMode,
                            lineWidth, pointSize, pointType, offset, measName1, measName2, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addSiblingOverlay(Obj object, String childObjects1Name, String childObjects2Name, ImagePlus ipl,
            Color colour, String renderMode, double lineWidth, String pointSize, String pointType, boolean offset,
            String measName1, String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames ? 0 : object.getT() + 1;

        // Running through each slice of this object
        for (Obj childObj1 : object.getChildren(childObjects1Name).values()) {
            for (Obj childObj2 : object.getChildren(childObjects2Name).values()) {
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

    public static void addPartnerOverlay(ImagePlus ipl, ObjCollection partnerObjects1, String partnerObjects2Name,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, HashMap<Integer, Float> hues, double opacity, boolean renderInAllFrames,
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
            for (Obj partnerObject1 : partnerObjects1.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(partnerObject1.getID());
                    Color colour = ColourFactory.getColour(hue, opacity);

                    addPartnerOverlay(partnerObject1, partnerObjects2Name, finalIpl, colour, renderMode, lineWidth,
                            pointSize, pointType, offset, measName1, measName2, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addPartnerOverlay(Obj partnerObject1, String partnerObjects2Name, ImagePlus ipl, Color colour,
            String renderMode, double lineWidth, String pointSize, String pointType, boolean offset, String measName1,
            String measName2, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames ? 0 : partnerObject1.getT() + 1;

        // Running through each slice of this object
        for (Obj partnerObject2 : partnerObject1.getPartners(partnerObjects2Name).values()) {
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

    public static void drawFullLine(ImagePlus ipl, Obj object1, Obj object2, int t, Color colour, double lineWidth) {
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

    public static void drawHalfLine(ImagePlus ipl, Obj object1, Obj object2, int t, Color colour, double lineWidth,
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

    public static void drawMidpointDot(ImagePlus ipl, Obj object1, Obj object2, int t, Color colour, String pointSize,
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
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "Adds a series of line overlays, representing various relationship scenarios.  Lines extend between centroids of two objects.  Depicted relationships can be between mutual child of the same parent object, between partner objects of between children and parents.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input objects
        String lineMode = parameters.getValue(LINE_MODE);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        String childObjects1Name = parameters.getValue(CHILD_OBJECTS_1);
        String childObjects2Name = parameters.getValue(CHILD_OBJECTS_2);
        String partnerObjects1Name = parameters.getValue(PARTNER_OBJECTS_1);
        String partnerObjects2Name = parameters.getValue(PARTNER_OBJECTS_2);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        double opacity = parameters.getValue(OPACITY);
        String renderMode = parameters.getValue(RENDER_MODE);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        String pointSize = parameters.getValue(POINT_SIZE);
        String pointType = parameters.getValue(POINT_TYPE);
        boolean offset = parameters.getValue(OFFSET_BY_MEASUREMENT);
        String measName1 = parameters.getValue(MEASUREMENT_NAME_1);
        String measName2 = parameters.getValue(MEASUREMENT_NAME_2);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        switch (lineMode) {
            case LineModes.BETWEEN_CHILDREN:
                ObjCollection parentObjects = workspace.getObjectSet(parentObjectsName);
                HashMap<Integer, Float> hues = getHues(parentObjects);
                addSiblingOverlay(ipl, parentObjects, childObjects1Name, childObjects2Name, renderMode, lineWidth,
                        pointSize, pointType, offset, measName1, measName2, hues, opacity, renderInAllFrames,
                        multithread);
                break;

            case LineModes.BETWEEN_PARTNERS:
                ObjCollection partnerObjects1 = workspace.getObjectSet(partnerObjects1Name);
                hues = getHues(partnerObjects1);
                addPartnerOverlay(ipl, partnerObjects1, partnerObjects2Name, renderMode, lineWidth, pointSize,
                        pointType, offset, measName1, measName2, hues, opacity, renderInAllFrames, multithread);
                break;

            case LineModes.PARENT_TO_CHILD:
                parentObjects = workspace.getObjectSet(parentObjectsName);
                hues = getHues(parentObjects);
                addParentChildOverlay(ipl, parentObjects, childObjects1Name, renderMode, lineWidth, pointSize,
                        pointType, offset, measName1, measName2, hues, opacity, renderInAllFrames, multithread);
                break;
        }

        Image outputImage = new Image(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
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
        parameters.add(new ChoiceP(LINE_MODE, this, LineModes.PARENT_TO_CHILD, LineModes.ALL));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_1, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_2, this));
        parameters.add(new InputObjectsP(PARTNER_OBJECTS_1, this));
        parameters.add(new PartnerObjectsP(PARTNER_OBJECTS_2, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new ChoiceP(RENDER_MODE, this, RenderModes.FULL_LINE, RenderModes.ALL));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new ChoiceP(POINT_SIZE, this, PointSizes.SMALL, PointSizes.ALL));
        parameters.add(new ChoiceP(POINT_TYPE, this, PointTypes.CIRCLE, PointTypes.ALL));
        parameters.add(new BooleanP(OFFSET_BY_MEASUREMENT, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME_1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_NAME_2, this));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(PARENT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(LINE_MODE));

        switch ((String) parameters.getValue(LINE_MODE)) {
            case LineModes.BETWEEN_CHILDREN:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

                ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS_1);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));
                returnedParameters.add(childObjectsP);

                childObjectsP = parameters.getParameter(CHILD_OBJECTS_2);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));
                returnedParameters.add(childObjectsP);
                break;

            case LineModes.BETWEEN_PARTNERS:
                returnedParameters.add(parameters.getParameter(PARTNER_OBJECTS_1));

                PartnerObjectsP partnerObjectsP = parameters.getParameter(PARTNER_OBJECTS_2);
                partnerObjectsP.setPartnerObjectsName(parameters.getValue(PARTNER_OBJECTS_1));
                returnedParameters.add(partnerObjectsP);
                break;

            case LineModes.PARENT_TO_CHILD:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));

                childObjectsP = parameters.getParameter(CHILD_OBJECTS_1);
                childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));
                returnedParameters.add(childObjectsP);
                break;
        }

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

        returnedParameters.add(parameters.getParameter(RENDER_MODE));
        switch ((String) parameters.getValue(RENDER_MODE)) {
            case RenderModes.FULL_LINE:
            case RenderModes.HALF_LINE:
                returnedParameters.add(parameters.getParameter(LINE_WIDTH));
                break;
            case RenderModes.MIDPOINT_DOT:
                returnedParameters.add(parameters.getParameter(POINT_SIZE));
                returnedParameters.add(parameters.getParameter(POINT_TYPE));
                break;
        }
        switch ((String) parameters.getValue(RENDER_MODE)) {
            case RenderModes.HALF_LINE:
            case RenderModes.MIDPOINT_DOT:
                returnedParameters.add(parameters.getParameter(OFFSET_BY_MEASUREMENT));
                if ((boolean) parameters.getValue(OFFSET_BY_MEASUREMENT)) {
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME_1));
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_NAME_2));

                    String name1 = "";
                    String name2 = "";
                    switch ((String) parameters.getValue(LINE_MODE)) {
                        case LineModes.BETWEEN_CHILDREN:
                            name1 = parameters.getValue(CHILD_OBJECTS_1);
                            name2 = parameters.getValue(CHILD_OBJECTS_2);
                            break;
                        case LineModes.BETWEEN_PARTNERS:
                            name1 = parameters.getValue(PARTNER_OBJECTS_1);
                            name2 = parameters.getValue(PARTNER_OBJECTS_2);
                            break;
                        case LineModes.PARENT_TO_CHILD:
                            name1 = parameters.getValue(PARENT_OBJECTS);
                            name2 = parameters.getValue(CHILD_OBJECTS_1);
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
                "Size of each overlay marker.  Options are: " + String.join(", ", PointSizes.ALL) + ".");

        parameters.get(POINT_TYPE).setDescription("Type of overlay marker used to represent each object.  Options are: "
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
