// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.gui.Line;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.interfaces.MeasurementPositionProvider;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
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
 * Created by sc13967 on 17/05/2017.
 */

/**
 * Adds an overlay to the specified input image showing the path of each track
 * object. The line is drawn between object centroids.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class AddTracks extends AbstractOverlay implements MeasurementPositionProvider {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image and object input";

    /**
     * Image onto which overlay will be rendered. Input image will only be updated
     * if "Apply to input image" is enabled, otherwise the image containing the
     * overlay will be stored as a new image with name specified by "Output image".
     */
    public static final String INPUT_IMAGE = "Input image";

    /**
     * Track objects to render in the overlay. Track objects themselves don't
     * contain any coordinate information, they simply act as links between the
     * different "Spot objects" children in each frame of the track.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
     * Objects present in each frame of this track. These are children of the "Input
     * objects" and provide the coordinate information for each frame.
     */
    public static final String SPOT_OBJECTS = "Spot objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "ImageI output";

    /**
     * Determines if the modifications made to the input image (added overlay
     * elements) will be applied to that image or directed to a new image. When
     * selected, the input image will be updated.
     */
    public static final String APPLY_TO_INPUT = "Apply to input image";

    /**
     * If the modifications (overlay) aren't being applied directly to the input
     * image, this control will determine if a separate image containing the overlay
     * should be saved to the workspace.
     */
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";

    /**
     * The name of the new image to be saved to the workspace (if not applying the
     * changes directly to the input image).
     */
    public static final String OUTPUT_IMAGE = "Output image";

    /**
    * 
    */
    public static final String POSITION_SEPARATOR = "Position controls";

    /**
    * 
    */
    public static final String RENDERING_SEPARATOR = "Overlay rendering";

    /**
     * When enabled, segments of a track will only be displayed for a finite number
     * of frames after the timepoint they correspond to. This gives the effect of a
     * moving tail behind the object and can be use to prevent the overlay image
     * becoming too cluttered for long/dense videos. The duration of the track
     * history is specified by the "Track history (frames)" parameter.
     */
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";

    /**
     * 
     */
    public static final String TRACK_HISTORY = "Track history (frames)";

    /**
     * 
     */
    public static final String FADE_OUT_TRACKS = "Fade out tracks";

    /**
     * Width of the rendered lines. Specified in pixel units.
     */
    public static final String LINE_WIDTH = "Line width";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple overlay elements simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddTracks(Modules modules) {
        super("Add tracks", modules);
    }

    public interface ColourModes extends AbstractOverlay.ColourModes {
        String INSTANTANEOUS_MEASUREMENT_VALUE = "Instantaneous measurement value";

        String[] ALL = new String[] { CHILD_COUNT, ID, INSTANTANEOUS_MEASUREMENT_VALUE, MEASUREMENT_VALUE, PARENT_ID,
                PARENT_MEASUREMENT_VALUE, PARTNER_COUNT, RANDOM_COLOUR, SINGLE_COLOUR };

    }

    public void addOverlay(Obj trackObject, String spotObjectsName, ImagePlus ipl, HashMap<Integer, Color> colours,
            double lineWidth, int history, boolean fadeOutTracks, boolean instantaneousColour) {
        Objs pointObjects = trackObject.getChildren(spotObjectsName);

        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());
        ij.gui.Overlay ovl = ipl.getOverlay();

        int nFrames = ipl.getNFrames();
        for (int t = 0; t < nFrames; t++) {
            for (Obj pointObject : pointObjects.values()) {
                if (pointObject.getT() > t || pointObject.getT() < t - history)
                    continue;

                // Showing previous object connections
                Objs previousPartners = pointObject.getPreviousPartners(pointObject.getName());

                for (Obj previousPartner : previousPartners.values()) {
                    if (previousPartner.getT() < t - history)
                        continue;

                    double[] position1 = getObjectPosition(pointObject, parameters, true, false);
                    double[] position2 = getObjectPosition(previousPartner, parameters, true, false);

                    double x1 = position1[0] + 0.5;
                    double y1 = position1[1] + 0.5;
                    double x2 = position2[0] + 0.5;
                    double y2 = position2[1] + 0.5;

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

                    Color colour = null;
                    if (instantaneousColour)
                        colour = colours.get(previousPartner.getID());
                    else if (pointObject.getPreviousPartners(pointObject.getName()).size() == 1)
                        colour = colours.get(trackObject.getID());
                    else {
                        String parentName = trackObject.getName();

                        if (spotObjectsName.contains("//")) {
                            String[] elements = spotObjectsName.split(" // ");
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i = elements.length - 2; i >= 0; i--)
                                stringBuilder.append(elements[i]).append(" // ");
                            stringBuilder.append(trackObject.getName());
                            parentName = stringBuilder.toString();
                        }

                        colour = colours.get(previousPartner.getParent(parentName).getID());
                    }

                    if (fadeOutTracks) {
                        // Calculate how far from end of track this is
                        double opacity = 1 - (((double) t - (double) pointObject.getT()) / history);

                        int alpha = (int) Math.round(colour.getAlpha() * opacity);

                        colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);

                    }

                    line.setStrokeColor(colour);
                    
                    ovl.addElement(line);

                }
            }
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
        return "Adds an overlay to the specified input image showing the path of each track object.  The line is drawn between object centroids.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS, workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        ImageI inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String colourMode = parameters.getValue(COLOUR_MODE, workspace);
        String colourMap = parameters.getValue(COLOUR_MAP, workspace);
        String rangeMinMode = parameters.getValue(RANGE_MINIMUM_MODE, workspace);
        double minValue = parameters.getValue(MINIMUM_VALUE, workspace);
        String rangeMaxMode = parameters.getValue(RANGE_MAXIMUM_MODE, workspace);
        double maxValue = parameters.getValue(MAXIMUM_VALUE, workspace);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR, workspace);
        boolean limitHistory = parameters.getValue(LIMIT_TRACK_HISTORY, workspace);
        int history = parameters.getValue(TRACK_HISTORY, workspace);
        boolean fadeOutTracks = parameters.getValue(FADE_OUT_TRACKS, workspace);

        double opacity = parameters.getValue(OPACITY, workspace);
        double lineWidth = parameters.getValue(LINE_WIDTH, workspace);

        boolean instantaneousColour = colourMode.equals(ColourModes.INSTANTANEOUS_MEASUREMENT_VALUE);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(inputObjects, workspace);

        if (instantaneousColour) {
            String[] elements = spotObjectsName.split(" // ");
            Objs spotObjects = workspace.getObjects(elements[elements.length - 1]);
            double[] range = new double[] { Double.NaN, Double.NaN };
            if (rangeMinMode.equals(RangeModes.MANUAL))
                range[0] = minValue;
            if (rangeMaxMode.equals(RangeModes.MANUAL))
                range[1] = maxValue;
            colours = ColourFactory
                    .getColours(ColourFactory.getMeasurementValueHues(spotObjects, measurementForColour, true, range),
                            colourMap, opacity);
        }

        // Adding the overlay element
        if (!limitHistory)
            history = Integer.MAX_VALUE;

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1))
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            addOverlay(object, spotObjectsName, ipl, colours, lineWidth, history, fadeOutTracks, instantaneousColour);
            writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
        }

        ImageI outputImage = ImageFactory.createImage(outputImageName, ipl);

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        // Updating colour modes to the custom version, which includes the option to
        // have a different colour at each timepoint
        ((ChoiceP) parameters.getParameter(COLOUR_MODE)).setChoices(ColourModes.ALL);

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ChildObjectsP(SPOT_OBJECTS, this));

        parameters.add(new SeparatorP(POSITION_SEPARATOR, this));
        parameters.addAll(initialisePositionParameters(this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new BooleanP(LIMIT_TRACK_HISTORY, this, false));
        parameters.add(new IntegerP(TRACK_HISTORY, this, 10));
        parameters.add(new BooleanP(FADE_OUT_TRACKS, this, true));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));
        ((ChildObjectsP) parameters.getParameter(SPOT_OBJECTS)).setParentObjectsName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE, workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
            }
        }

        returnedParameters.add(parameters.getParameter(POSITION_SEPARATOR));
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS, workspace);
        returnedParameters.addAll(updateAndGetPositionParameters(spotObjectsName, parameters));

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));
        if (((String) parameters.getValue(COLOUR_MODE, workspace))
                .equals(ColourModes.INSTANTANEOUS_MEASUREMENT_VALUE)) {
            returnedParameters.add(parameters.getParameter(LINE_WIDTH));
            returnedParameters.add(parameters.getParameter(COLOUR_MAP));
            returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_COLOUR))
                    .setObjectName(parameters.getValue(SPOT_OBJECTS, workspace));
            returnedParameters.add(parameters.getParameter(RANGE_MINIMUM_MODE));
            if (((String) parameters.getValue(RANGE_MINIMUM_MODE, workspace)).equals(RangeModes.MANUAL))
                returnedParameters.add(parameters.getParameter(MINIMUM_VALUE));
            returnedParameters.add(parameters.getParameter(RANGE_MAXIMUM_MODE));
            if (((String) parameters.getValue(RANGE_MAXIMUM_MODE, workspace)).equals(RangeModes.MANUAL))
                returnedParameters.add(parameters.getParameter(MAXIMUM_VALUE));
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

        if ((boolean) parameters.getValue(LIMIT_TRACK_HISTORY, workspace)) {
            returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
            returnedParameters.add(parameters.getParameter(FADE_OUT_TRACKS));
        }

        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

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

        parameters.getParameter(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.getParameter(INPUT_OBJECTS).setDescription(
                "Track objects to render in the overlay.  Track objects themselves don't contain any coordinate information, they simply act as links between the different \""
                        + SPOT_OBJECTS + "\" children in each frame of the track.");

        parameters.getParameter(SPOT_OBJECTS)
                .setDescription("Objects present in each frame of this track.  These are children of the \""
                        + INPUT_OBJECTS + "\" and provide the coordinate information for each frame.");

        parameters.getParameter(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.getParameter(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.getParameter(LIMIT_TRACK_HISTORY).setDescription(
                "When enabled, segments of a track will only be displayed for a finite number of frames after the timepoint they correspond to.  This gives the effect of a moving tail behind the object and can be use to prevent the overlay image becoming too cluttered for long/dense videos.  The duration of the track history is specified by the \""
                        + TRACK_HISTORY + "\" parameter.");

        parameters.getParameter(TRACK_HISTORY).setDescription(
                "Number of frames a track segment will be displayed for after the timepoint to which it corresponds.");

        parameters.getParameter(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.getParameter(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
