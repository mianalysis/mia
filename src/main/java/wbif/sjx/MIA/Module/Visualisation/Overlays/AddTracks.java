// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import ij.ImagePlus;
import ij.gui.Line;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChildObjectsP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Objects.InputTrackObjectsP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class AddTracks extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Image and object input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String SPOT_OBJECTS = "Spot objects";

    public static final String OUTPUT_SEPARATOR = "Image output";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";
    public static final String TRACK_HISTORY = "Track history (frames)";
    public static final String LINE_WIDTH = "Line width";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddTracks(ModuleCollection modules) {
        super("Add tracks", modules);
    }

    public static void addOverlay(Obj object, String spotObjectsName, ImagePlus ipl, Color colour, double lineWidth,
            int history) {
        ObjCollection pointObjects = object.getChildren(spotObjectsName);

        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());
        ij.gui.Overlay ovl = ipl.getOverlay();

        // Putting the current track points into a TreeMap stored by the frame
        TreeMap<Integer, Obj> points = new TreeMap<>();
        for (Obj pointObject : pointObjects.values()) {
            points.put(pointObject.getT(), pointObject);
        }

        // Iterating over all points in the track, drawing lines between them
        int nFrames = ipl.getNFrames();
        Obj p1 = null;
        for (Obj p2 : points.values()) {
            if (p1 != null) {
                double x1 = p1.getXMean(true) + 0.5;
                double y1 = p1.getYMean(true) + 0.5;
                double x2 = p2.getXMean(true) + 0.5;
                double y2 = p2.getYMean(true) + 0.5;

                int maxFrame = history == Integer.MAX_VALUE ? nFrames : Math.min(nFrames, p2.getT() + history);
                for (int t = p2.getT(); t <= maxFrame - 1; t++) {
                    Line line = new Line(x1, y1, x2, y2);

                    if (ipl.isHyperStack()) {
                        ipl.setPosition(1, 1, t + 1);
                        line.setPosition(1, 1, t + 1);
                    } else {
                        int pos = Math.max(1, t + 1);
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
    public String getPackageName() {
        return PackageNames.VISUALISATION_OVERLAYS;
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "Adds an overlay to the specified input image showing the path of each track object.  The line is drawn between object centroids.";
    }

    @Override
    public Status process(Workspace workspace) {
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

        double opacity = parameters.getValue(OPACITY);
        double lineWidth = parameters.getValue(LINE_WIDTH);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Float> hues = getHues(inputObjects);

        // Adding the overlay element
        if (!limitHistory)
            history = Integer.MAX_VALUE;

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            float hue = hues.get(object.getID());
            Color colour = ColourFactory.getColour(hue, opacity);

            addOverlay(object, spotObjectsName, ipl, colour, lineWidth, history);

            writeStatus("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());

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

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputTrackObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChildObjectsP(SPOT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new BooleanP(LIMIT_TRACK_HISTORY, this, false));
        parameters.add(new IntegerP(TRACK_HISTORY, this, 10));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
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
        returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

        if ((boolean) parameters.getValue(LIMIT_TRACK_HISTORY))
            returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
        ((ChildObjectsP) parameters.getParameter(SPOT_OBJECTS)).setParentObjectsName(inputObjectsName);

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();
        
        parameters.getParameter(INPUT_IMAGE).setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""+APPLY_TO_INPUT+"\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""+OUTPUT_IMAGE+"\".");
        
        parameters.getParameter(INPUT_OBJECTS).setDescription("Track objects to render in the overlay.  Track objects themselves don't contain any coordinate information, they simply act as links between the different \""+SPOT_OBJECTS+"\" children in each frame of the track.");
        
        parameters.getParameter(SPOT_OBJECTS).setDescription("Objects present in each frame of this track.  These are children of the \""+INPUT_OBJECTS+"\" and provide the coordinate information for each frame.");
        
        parameters.getParameter(APPLY_TO_INPUT).setDescription("Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");
        
        parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE).setDescription("If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");
        
        parameters.getParameter(OUTPUT_IMAGE).setDescription("The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");
        
        parameters.getParameter(LIMIT_TRACK_HISTORY).setDescription("When enabled, segments of a track will only be displayed for a finite number of frames after the timepoint they correspond to.  This gives the effect of a moving tail behind the object and can be use to prevent the overlay image becoming too cluttered for long/dense videos.  The duration of the track history is specified by the \""+TRACK_HISTORY+"\" parameter.");
        
        parameters.getParameter(TRACK_HISTORY).setDescription("Number of frames a track segment will be displayed for after the timepoint to which it corresponds.");
        
        parameters.getParameter(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.getParameter(ENABLE_MULTITHREADING).setDescription("Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");
        
    }
}
