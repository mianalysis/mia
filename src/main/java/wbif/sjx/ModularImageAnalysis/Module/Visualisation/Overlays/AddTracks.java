// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation.Overlays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.Overlays.ColourServer;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;
import wbif.sjx.ModularImageAnalysis.Process.LabelFactory;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class AddTracks extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SPOT_OBJECTS = "Spot objects";
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";
    public static final String TRACK_HISTORY = "Track history (frames)";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    private ColourServer colourServer;


    public static void addTrackOverlay(Obj object, String spotObjectsName, ImagePlus ipl, Color colour, double lineWidth, int history) {
        ObjCollection pointObjects = object.getChildren(spotObjectsName);

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Putting the current track points into a TreeMap stored by the frame
        TreeMap<Integer,Obj> points = new TreeMap<>();
        for (Obj pointObject:pointObjects.values()) {
            points.put(pointObject.getT(),pointObject);
        }

        //  Iterating over all points in the track, drawing lines between them
        int nFrames = ipl.getNFrames();
        Obj p1 = null;
        for (Obj p2:points.values()) {
            if (p1 != null) {
                double x1 = p1.getXMean(true)+0.5;
                double y1 = p1.getYMean(true)+0.5;
                double x2 = p2.getXMean(true)+0.5;
                double y2 = p2.getYMean(true)+0.5;

                int maxFrame = history == Integer.MAX_VALUE ? nFrames : Math.min(nFrames,p2.getT()+history);
                for (int t = p2.getT();t<=maxFrame-1;t++) {
                    Line line = new Line(x1,y1,x2,y2);

                    if (ipl.isHyperStack()) {
                        ipl.setPosition(1,1,t+1);
                        line.setPosition(1,1, t+1);
                    } else {
                        int pos = Math.max(1,t+1);
                        ipl.setPosition(pos);
                        line.setPosition(pos);
                    }

                    line.setStrokeWidth(lineWidth);
                    line.setStrokeColor(colour);
                    ovl.addElement(line);

                }
            }

            p1 = p2;

        }
    }


    @Override
    public String getTitle() {
        return "Add tracks";
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
    public boolean process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        boolean limitHistory = parameters.getValue(LIMIT_TRACK_HISTORY);
        int history = parameters.getValue(TRACK_HISTORY);

        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues = colourServer.getHues(inputObjects);

        // Adding the overlay element
        if (!limitHistory) history = Integer.MAX_VALUE;

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            float hue = hues.get(object.getID());
            Color colour = ColourFactory.getColour(hue);

            addTrackOverlay(object, spotObjectsName, ipl, colour, lineWidth,  history);

            writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());

        }

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChildObjectsP(SPOT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_TRACK_HISTORY, this,false));
        parameters.add(new IntegerP(TRACK_HISTORY, this,10));
        parameters.add(new DoubleP(LINE_WIDTH,this,0.2));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        colourServer = new ColourServer(parameters.getParameter(INPUT_OBJECTS),this);
        parameters.addAll(colourServer.getParameters());

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if (parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));
        returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

        if (parameters.getValue(LIMIT_TRACK_HISTORY)) returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
        ((ChildObjectsP) parameters.getParameter(SPOT_OBJECTS)).setParentObjectsName(inputObjectsName);

        returnedParameters.addAll(colourServer.updateAndGetParameters());
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
