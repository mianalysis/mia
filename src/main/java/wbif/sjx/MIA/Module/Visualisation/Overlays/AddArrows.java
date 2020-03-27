package wbif.sjx.MIA.Module.Visualisation.Overlays;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Arrow;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddArrows extends Overlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String ORIENTATION_MODE = "Arrow orientation mode";
    public static final String PARENT_OBJECT_FOR_ORIENTATION = "Parent object for orientation";
    public static final String MEASUREMENT_FOR_ORIENTATION = "Measurement for orientation";
    public static final String LENGTH_MODE = "Arrow length mode";
    public static final String LENGTH_VALUE = "Length value (px)";
    public static final String PARENT_OBJECT_FOR_LENGTH = "Parent object for length";
    public static final String MEASUREMENT_FOR_LENGTH = "Measurement for length";
    public static final String LENGTH_SCALE = "Arrow length scale";
    public static final String HEAD_SIZE = "Head size";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


    public AddArrows(ModuleCollection modules) {
        super("Add arrows",modules);
    }


    public interface OrientationModes {
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[]{PARENT_MEASUREMENT, MEASUREMENT};

    }

    public interface LengthModes {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[]{FIXED_VALUE, PARENT_MEASUREMENT, MEASUREMENT};

    }


    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, double orientation, double arrowLength, double headSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new ij.gui.Overlay());

        double oriRads = Math.toRadians(orientation);

        // Adding each point
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true,false);

        int z = (int) Math.round(zMean+1);
        int t = object.getT()+1;

        // Getting end point
        double x2 = arrowLength*Math.cos(oriRads);
        double y2 = arrowLength*Math.sin(oriRads);

        Arrow arrow = new Arrow(xMean,yMean,xMean+x2,yMean+y2);
        arrow.setHeadSize(headSize);
        arrow.setStrokeColor(colour);
        arrow.setStrokeWidth(lineWidth);

        if (ipl.isHyperStack()) {
            arrow.setPosition(1, (int) z, t);
        } else {
            int pos = Math.max(Math.max(1,(int) z),t);
            arrow.setPosition(pos);
        }
        ipl.getOverlay().addElement(arrow);

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
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String orientationMode = parameters.getValue(ORIENTATION_MODE);
        String parentForOri = parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION);
        String measForOri = parameters.getValue(MEASUREMENT_FOR_ORIENTATION);
        String lengthMode = parameters.getValue(LENGTH_MODE);
        double lengthValue = parameters.getValue(LENGTH_VALUE);
        String parentForLength = parameters.getValue(PARENT_OBJECT_FOR_LENGTH);
        String measurementForLength = parameters.getValue(MEASUREMENT_FOR_LENGTH);
        double lengthScale = parameters.getValue(LENGTH_SCALE);
        int headSize = parameters.getValue(HEAD_SIZE);

        double opacity = parameters.getValue(OPACITY);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Only add output to workspace if not applying to input
        if (applyToInput) addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues = getHues(inputObjects);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Adding the overlay element
        try {
            int nThreads = multithread ? Prefs.getThreads() : 1;
            ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

            // Running through each object, adding it to the overlay along with an ID label
            AtomicInteger count = new AtomicInteger();
            for (Obj object:inputObjects.values()) {
                ImagePlus finalIpl = ipl;

                Runnable task = () -> {
                    float hue = hues.get(object.getID());
                    Color colour = ColourFactory.getColour(hue,opacity);
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
                            length= object.getMeasurement(measurementForLength).getValue();
                            break;
                        case LengthModes.PARENT_MEASUREMENT:
                            length = object.getParent(parentForLength).getMeasurement(measurementForLength).getValue();
                            break;
                    }

                    length = length*lengthScale;

                    addOverlay(object, finalIpl, colour, lineWidth, orientation, length, headSize);

                    writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        } catch (InterruptedException e) {
            return false;
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
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new ChoiceP(ORIENTATION_MODE, this, AddObjectsOverlay.OrientationModes.MEASUREMENT, AddObjectsOverlay.OrientationModes.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_ORIENTATION, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_ORIENTATION, this));
        parameters.add(new ChoiceP(LENGTH_MODE, this, AddObjectsOverlay.LengthModes.MEASUREMENT, AddObjectsOverlay.LengthModes.ALL));
        parameters.add(new DoubleP(LENGTH_VALUE,this,5d));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LENGTH, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LENGTH, this));
        parameters.add(new DoubleP(LENGTH_SCALE,this,1d));
        parameters.add(new IntegerP(HEAD_SIZE,this,3));
        parameters.add(new DoubleP(LINE_WIDTH,this,1));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES,this,false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

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
        returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
        switch ((String) parameters.getValue(ORIENTATION_MODE)) {
            case AddObjectsOverlay.OrientationModes.MEASUREMENT:
                ObjectMeasurementP oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                oriMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                break;

            case AddObjectsOverlay.OrientationModes.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION));
                ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION);
                parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS));

                oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                oriMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                break;
        }

        returnedParameters.add(parameters.getParameter(LENGTH_MODE));
        switch ((String) parameters.getValue(LENGTH_MODE)) {
            case AddObjectsOverlay.LengthModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(LENGTH_VALUE));
                break;

            case AddObjectsOverlay.LengthModes.MEASUREMENT:
                ObjectMeasurementP lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                lengthMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LENGTH));
                break;

            case AddObjectsOverlay.LengthModes.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LENGTH));
                ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_LENGTH);
                parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS));

                lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                lengthMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_LENGTH));
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
    public ParentChildRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
