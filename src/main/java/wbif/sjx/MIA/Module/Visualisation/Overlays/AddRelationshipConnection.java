package wbif.sjx.MIA.Module.Visualisation.Overlays;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddRelationshipConnection extends Overlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String LINE_MODE = "Line mode";
    public static final String PARENT_OBJECTS = "Parent objects";
    public static final String CHILD_OBJECTS_1 = "Child objects 1";
    public static final String CHILD_OBJECTS_2 = "Child objects 2";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface LineModes {
        String BETWEEN_CHILDREN = "Between children";
        String PARENT_TO_CHILD = "Parent to child";

        String[] ALL = new String[]{BETWEEN_CHILDREN,PARENT_TO_CHILD};

    }

    public AddRelationshipConnection(ModuleCollection modules) {
        super("Add relationship connection",modules);
    }

    public static void addParentChildOverlay(ImagePlus ipl, ObjCollection inputObjects, String childObjectsName, double lineWidth, HashMap<Integer,Float> hues, double opacity, boolean renderInAllFrames, boolean multithread) {
        // Adding the overlay element
        try {
            // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
            if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
                ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
            }

            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (Obj object:inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue,opacity);

                    addParentChildOverlay(object, childObjectsName, finalIpl, colour, lineWidth, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addParentChildOverlay(Obj object, String childObjectsName, ImagePlus ipl, Color colour, double lineWidth, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames? 0 : object.getT() + 1;

        // Running through each slice of this object
        for (Obj childObj:object.getChildren(childObjectsName).values()) {
            drawLine(ipl,object,childObj,t,colour,lineWidth);
        }
    }

    public static void addSiblingOverlay(ImagePlus ipl, ObjCollection inputObjects, String childObjects1Name, String childObjects2Name, double lineWidth, HashMap<Integer,Float> hues, double opacity, boolean renderInAllFrames, boolean multithread) {
        // Adding the overlay element
        try {
            // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
            if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
                ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
            }

            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (Obj object:inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue,opacity);

                    addSiblingOverlay(object, childObjects1Name, childObjects2Name, finalIpl, colour, lineWidth, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addSiblingOverlay(Obj object, String childObjects1Name, String childObjects2Name, ImagePlus ipl, Color colour, double lineWidth, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new ij.gui.Overlay());

        int t = renderInAllFrames? 0 : object.getT() + 1;

        // Running through each slice of this object
        for (Obj childObj1:object.getChildren(childObjects1Name).values()) {
            for (Obj childObj2:object.getChildren(childObjects2Name).values()) {
                drawLine(ipl, childObj1, childObj2, t, colour, lineWidth);
            }
        }
    }

    public static void drawLine(ImagePlus ipl, Obj object1, Obj object2, int t, Color colour, double lineWidth) {
        int nSlices = ipl.getNSlices();

        double x1 = object1.getXMean(true)+0.5;
        double y1 = object1.getYMean(true)+0.5;
        double x2 = object2.getXMean(true)+0.5;
        double y2 = object2.getYMean(true)+0.5;

        for (int z = 0; z < nSlices; z++) {
            Line line = new Line(x1,y1,x2,y2);
            if (ipl.isHyperStack()) {
                ipl.setPosition(1, z+1, t);
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


    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input objects
        String lineMode = parameters.getValue(LINE_MODE);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS);
        ObjCollection parentObjects = workspace.getObjects().get(parentObjectsName);
        String childObjects1Name = parameters.getValue(CHILD_OBJECTS_1);
        String childObjects2Name = parameters.getValue(CHILD_OBJECTS_2);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        double opacity = parameters.getValue(OPACITY);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Only add output to workspace if not applying to input
        if (applyToInput) addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues = getHues(parentObjects);

        switch (lineMode) {
            case LineModes.BETWEEN_CHILDREN:
                addSiblingOverlay(ipl,parentObjects,childObjects1Name,childObjects2Name,lineWidth,hues,opacity,renderInAllFrames,multithread);
                break;

            case LineModes.PARENT_TO_CHILD:
                addParentChildOverlay(ipl,parentObjects,childObjects1Name,lineWidth,hues,opacity,renderInAllFrames,multithread);
                break;
        }

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(LINE_MODE,this,LineModes.PARENT_TO_CHILD,LineModes.ALL));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_1, this));
        parameters.add(new ChildObjectsP(CHILD_OBJECTS_2, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new DoubleP(LINE_WIDTH,this,1));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES,this,false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(PARENT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(LINE_MODE));
        returnedParameters.add(parameters.getParameter(PARENT_OBJECTS));
        ChildObjectsP childObjectsP = parameters.getParameter(CHILD_OBJECTS_1);
        childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));
        returnedParameters.add(childObjectsP);
        if ((boolean) parameters.getValue(LINE_MODE).equals(LineModes.BETWEEN_CHILDREN)) {
            childObjectsP = parameters.getParameter(CHILD_OBJECTS_2);
            childObjectsP.setParentObjectsName(parameters.getValue(PARENT_OBJECTS));
            returnedParameters.add(childObjectsP);
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
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
