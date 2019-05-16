package wbif.sjx.MIA.Module.Visualisation.Overlays;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AddObjectOutline extends Module {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    private ColourServer colourServer;

    public AddObjectOutline(ModuleCollection modules) {
        super(modules);
    }


    public interface ColourModes extends ObjCollection.ColourModes {}

    public interface SingleColours extends ColourFactory.SingleColours {}

    public static void addOverlay(ImagePlus ipl, ObjCollection inputObjects, double lineWidth, HashMap<Integer,Float> hues, boolean renderInAllFrames, boolean multithread) {
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
                    Color colour = ColourFactory.getColour(hue);

                    addOverlay(object, finalIpl, colour, lineWidth, renderInAllFrames);

                };
                pool.submit(task);
            }

            pool.shutdown();
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return;
        }
    }

    public static void addOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

        int t = object.getT() + 1;
        if (renderInAllFrames) t = 0;

        // Running through each slice of this object
        double[][] range = object.getExtents(true,false);
        for (int z= (int) range[2][0];z<= (int) range[2][1];z++) {
            Roi polyRoi = object.getRoi(z);

            //  If the object doesn't have any pixels in this plane, skip it
            if (polyRoi == null) continue;

            if (ipl.isHyperStack()) {
                polyRoi.setPosition(1, z+1, t);
            } else {
                int pos = Math.max(Math.max(1, z+1), t);
                polyRoi.setPosition(pos);
            }

            polyRoi.setStrokeColor(colour);
            polyRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(polyRoi);
        }
    }

    @Override
    public String getTitle() {
        return "Add object outline";
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getHelp() {
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

        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues= colourServer.getHues(inputObjects);

        addOverlay(ipl,inputObjects,lineWidth,hues,renderInAllFrames,multithread);

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(OUTPUT_SEPARATOR,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR,this));
        parameters.add(new DoubleP(LINE_WIDTH,this,0.2));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES,this,false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR,this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        colourServer = new ColourServer(parameters.getParameter(INPUT_OBJECTS),this);
        parameters.addAll(colourServer.getParameters());

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
            if (parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.addAll(colourServer.updateAndGetParameters());
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
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
