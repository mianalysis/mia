package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.awt.Color;

import javax.annotation.Nullable;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay.ColourModes;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;

public class AddContourLines extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String RENDERING_SEPARATOR = "Overlay rendering";
    public static final String MINIMUM_INTENSITY = "Minimum intensity";
    public static final String MAXIMUM_INTENSITY = "Maximum intensity";
    public static final String NUMBER_OF_CONTOURS = "Number of contours";
    public static final String CONTOUR_COLOUR_MODE = "Contour colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String LINE_WIDTH = "Line width";

    public interface ContourColourModes {
        String SINGLE_COLOUR_GRADIENT = "Single colour gradient";
        String SINGLE_COLOUR = "Single colour";
        String SPECTRUM = "Spectrum";

        String[] ALL = new String[] { SINGLE_COLOUR_GRADIENT, SINGLE_COLOUR, SPECTRUM };

    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public AddContourLines(ModuleCollection modules) {
        super("Add contour lines", modules);
    }

    public static double[] getContourLevels(double minIntensity, double maxIntensity, int nContours) {
        double[] levels = new double[nContours];
        double intensityWidth = (maxIntensity - minIntensity + 1) / (nContours - 1);

        for (int i = 0; i < nContours; i++)
            levels[i] = minIntensity + i * intensityWidth;

        return levels;

    }

    public static HashMap<Double, Color> getContourColours(double[] levels, String colourMode,
            @Nullable String singleColour) {
        HashMap<Double, Color> colours = new HashMap<>();

        double minIntensity = levels[0];
        double maxIntensity = levels[levels.length - 1];
        double range = maxIntensity - minIntensity;

        for (double level : levels) {
            // Finding normalised position within range
            float norm = ((float) (level - minIntensity)) / ((float) range);

            switch (colourMode) {
                case ContourColourModes.SINGLE_COLOUR:
                    colours.put(level, ColourFactory.getColour(singleColour));
                    break;
                case ContourColourModes.SINGLE_COLOUR_GRADIENT:
                    float hue = ColourFactory.getHue(singleColour);
                    colours.put(level, Color.getHSBColor(hue, 1-norm, 1f));
                    break;
                case ContourColourModes.SPECTRUM:
                    colours.put(level, Color.getHSBColor(norm, 1f, 1f));
                    break;
            }
        }

        return colours;

    }

    public static Roi getContour(ImageProcessor ipr, double level) {
        // We will be thresholding the ImageProcessor, so duplicating it first
        ipr = ipr.duplicate();

        // Binarising image at specified level
        ipr.setThreshold(level, Double.MAX_VALUE, ImageProcessor.NO_LUT_UPDATE);

        // Getting ROI corresponding to binarised region
        return new ThresholdToSelection().convert(ipr);

    }

    public static void addOverlay(ImagePlus ipl, double[] levels, HashMap<Double, Color> colours, double lineWidth) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        for (int c = 0; c < ipl.getNChannels(); c++) {
            for (int z = 0; z < ipl.getNSlices(); z++) {
                for (int t = 0; t < ipl.getNFrames(); t++) {
                    int[] pos = null;
                    if (ipl.isHyperStack()) {
                        pos = new int[] { c + 1, z + 1, t + 1 };
                        ipl.setPosition(pos[0], pos[1], pos[2]);
                    } else {
                        pos = new int[] { Math.max(Math.max(c, z), t) + 1 };
                        ipl.setPosition(pos[0]);
                    }

                    addOverlay(ipl.getProcessor(), ipl.getOverlay(), pos, levels, colours, lineWidth);
                }
            }
        }
    }

    public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay, int[] pos, double[] levels,
            HashMap<Double, Color> colours, double lineWidth) {

        for (double level : levels) {
            Roi contour = getContour(ipr, level);
            if (contour == null)
                continue;

            contour.setStrokeWidth(lineWidth);
            contour.setStrokeColor(colours.get(level));

            if (pos.length > 1) {
                contour.setPosition(pos[0], pos[1], pos[2]);
            } else {
                contour.setPosition(pos[0]);
            }

            overlay.addElement(contour);

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
    protected Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        double lineWidth = parameters.getValue(LINE_WIDTH);
        double minIntensity = parameters.getValue(MINIMUM_INTENSITY);
        double maxIntensity = parameters.getValue(MAXIMUM_INTENSITY);
        int nContours = parameters.getValue(NUMBER_OF_CONTOURS);
        String colourMode = parameters.getValue(CONTOUR_COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);

        // Only add output to workspace if not applying to input
        if (applyToInput)
            addOutputToWorkspace = false;

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Getting intensity levels for contours
        double[] levels = getContourLevels(minIntensity, maxIntensity, nContours);
        HashMap<Double, Color> colours = getContourColours(levels, colourMode, singleColour);
        addOverlay(ipl, levels, colours, lineWidth);

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
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_INTENSITY, this, 0));
        parameters.add(new DoubleP(MAXIMUM_INTENSITY, this, 255));
        parameters.add(new IntegerP(NUMBER_OF_CONTOURS, this, 0));
        parameters.add(new ChoiceP(CONTOUR_COLOUR_MODE, this, ContourColourModes.SPECTRUM, ContourColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_INTENSITY));
        returnedParameters.add(parameters.getParameter(MAXIMUM_INTENSITY));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_CONTOURS));
        returnedParameters.add(parameters.getParameter(CONTOUR_COLOUR_MODE));
        switch ((String) parameters.getValue(CONTOUR_COLOUR_MODE)) {
            case ContourColourModes.SINGLE_COLOUR:
            case ContourColourModes.SINGLE_COLOUR_GRADIENT:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;
        }
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

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
}