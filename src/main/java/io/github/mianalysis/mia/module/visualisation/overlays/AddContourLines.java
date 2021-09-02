package io.github.mianalysis.mia.module.visualisation.overlays;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.IndexColorModel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.sisu.Nullable;

import org.scijava.vecmath.Point2d;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.LabelFactory;
import io.github.sjcross.common.mathfunc.CumStat;
import io.github.sjcross.common.imagej.LUTs;

public class AddContourLines extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String COLOUR_SEPARATOR = "Contour colours";
    public static final String MINIMUM_INTENSITY = "Minimum intensity";
    public static final String MAXIMUM_INTENSITY = "Maximum intensity";
    public static final String NUMBER_OF_CONTOURS = "Number of contours";
    public static final String CONTOUR_COLOUR_MODE = "Contour colour mode";
    public static final String CONTOUR_COLOUR = "Contour colour";

    public static final String RENDERING_SEPARATOR = "Contour rendering";
    public static final String LINE_WIDTH = "Line width";
    public static final String DRAW_EVERY_N_POINTS = "Draw every N points";

    public static final String LABEL_SEPARATOR = "Label rendering";
    public static final String SHOW_LABELS = "Show labels";
    public static final String DECIMAL_PLACES = "Decimal places";
    public static final String USE_SCIENTIFIC = "Use scientific notation";
    public static final String LABEL_COLOUR_MODE = "Label colour mode";
    public static final String LABEL_COLOUR = "Label colour";
    public static final String LABEL_SIZE = "Label size";

    public interface ColourModes {
        String BLACK_FIRE = "Black fire";
        String ICE = "Ice";
        String JET = "Jet";
        String PHYSICS = "Physics";
        String RANDOM = "Random";
        String SINGLE_COLOUR_GRADIENT = "Single colour gradient";
        String SINGLE_COLOUR = "Single colour";
        String SPECTRUM = "Spectrum";
        String THERMAL = "Thermal";

        String[] ALL = new String[] { BLACK_FIRE, ICE, JET, PHYSICS, RANDOM, SINGLE_COLOUR_GRADIENT, SINGLE_COLOUR,
                SPECTRUM, THERMAL };

    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public AddContourLines(Modules modules) {
        super("Add contour lines", modules);
    }

    public static double[] getContourLevels(double minIntensity, double maxIntensity, int nContours) {
        double[] levels = new double[nContours];
        double intensityWidth = (maxIntensity - minIntensity) / (nContours - 1);

        for (int i = 0; i < nContours; i++)
            levels[i] = minIntensity + i * intensityWidth;

        return levels;

    }

    public static HashMap<Double, Color> getColours(double[] levels, String colourMode, @Nullable String singleColour) {
        HashMap<Double, Color> colours = new HashMap<>();

        double minIntensity = levels[0];
        double maxIntensity = levels[levels.length - 1];
        double range = maxIntensity - minIntensity;

        IndexColorModel cm = null;
        switch (colourMode) {
            case ColourModes.BLACK_FIRE:
                cm = LUTs.BlackFire().getColorModel();
                break;
            case ColourModes.ICE:
                cm = LUTs.Ice().getColorModel();
                break;
            case ColourModes.PHYSICS:
                cm = LUTs.Physics().getColorModel();
                break;
            case ColourModes.RANDOM:
                cm = LUTs.Random(false).getColorModel();
                break;
            case ColourModes.JET:
                cm = LUTs.Jet().getColorModel();
                break;
            case ColourModes.SPECTRUM:
                cm = LUTs.Spectrum().getColorModel();
                break;
            case ColourModes.THERMAL:
                cm = LUTs.Thermal().getColorModel();
                break;
        }

        for (double level : levels) {
            // Finding normalised position within range
            float norm = ((float) (level - minIntensity)) / ((float) range);
            int idx = (int) Math.round(norm * 255);

            switch (colourMode) {
                case ColourModes.BLACK_FIRE:
                case ColourModes.ICE:
                case ColourModes.PHYSICS:
                case ColourModes.RANDOM:
                case ColourModes.JET:
                case ColourModes.SPECTRUM:
                case ColourModes.THERMAL:
                    colours.put(level, new Color(cm.getRed(idx), cm.getGreen(idx), cm.getBlue(idx)));
                    break;
                case ColourModes.SINGLE_COLOUR:
                    colours.put(level, ColourFactory.getColour(singleColour));
                    break;
                case ColourModes.SINGLE_COLOUR_GRADIENT:
                    float hue = ColourFactory.getHue(singleColour);
                    colours.put(level, Color.getHSBColor(hue, 1 - norm, 1f));
                    break;
            }
        }

        return colours;

    }

    public static HashMap<Double, String> getLabels(double[] levels, int decimalPlaces, boolean useScientific) {
        HashMap<Double, String> labels = new HashMap<>();

        DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces, useScientific);

        for (double level : levels) {
            String label = df.format(level);
            labels.put(level, label);
        }

        return labels;

    }

    public static Roi getContour(ImageProcessor ipr, double level) {
        // We will be thresholding the ImageProcessor, so duplicating it first
        ipr = ipr.duplicate();

        // Binarising image at specified level
        ipr.setThreshold(level, Double.MAX_VALUE, ImageProcessor.NO_LUT_UPDATE);

        // Getting ROI corresponding to binarised region
        return new ThresholdToSelection().convert(ipr);

    }

    public static void addOverlay(ImagePlus ipl, double[] levels, HashMap<Double, Color> contourColours,
            double lineWidth, int drawEveryNPoints) {
        addOverlay(ipl, levels, contourColours, lineWidth, drawEveryNPoints, null, null, 0);

    }

    public static void addOverlay(ImagePlus ipl, double[] levels, HashMap<Double, Color> contourColours,
            double lineWidth, int drawEveryNPoints, @Nullable HashMap<Double, String> labels,
            @Nullable HashMap<Double, Color> labelColours, int labelSize) {
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

                    if (labelColours == null) {
                        addOverlay(ipl.getProcessor(), ipl.getOverlay(), pos, levels, contourColours, lineWidth,
                                drawEveryNPoints);
                    } else {
                        addOverlay(ipl.getProcessor(), ipl.getOverlay(), pos, levels, contourColours, lineWidth,
                                drawEveryNPoints, labels, labelColours, labelSize);
                    }
                }
            }
        }
    }

    public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay, int[] pos, double[] levels,
            HashMap<Double, Color> contourColours, double lineWidth, int drawEveryNPoints) {
        for (double level : levels) {
            Roi contour = getContour(ipr, level);
            Color contourColour = contourColours.get(level);

            if (contour == null)
                continue;

            if (contour.getType() == Roi.COMPOSITE) {
                ShapeRoi shapeRoi = new ShapeRoi(contour);
                for (Roi roi : shapeRoi.getRois()) {
                    addSingleLevelContour(ipr, roi, overlay, pos, level, contourColour, lineWidth, drawEveryNPoints,
                            new ArrayList<>());
                }
            } else {
                addSingleLevelContour(ipr, contour, overlay, pos, level, contourColour, lineWidth, drawEveryNPoints,
                        new ArrayList<>());
            }
        }
    }

    public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay, int[] pos, double[] levels,
            HashMap<Double, Color> contourColours, double lineWidth, int drawEveryNPoints,
            HashMap<Double, String> labels, HashMap<Double, Color> labelColours, int labelSize) {

        for (double level : levels) {
            Roi contour = getContour(ipr, level);
            Color contourColour = contourColours.get(level);
            String label = labels.get(level);
            Color labelColour = labelColours.get(level);

            if (contour == null)
                continue;

            if (contour.getType() == Roi.COMPOSITE) {
                ShapeRoi shapeRoi = new ShapeRoi(contour);
                for (Roi roi : shapeRoi.getRois()) {
                    ArrayList<Integer> labelRegion = getLabelPosition(ipr, roi, pos, label, labelSize);
                    addSingleLevelContour(ipr, roi, overlay, pos, level, contourColour, lineWidth, drawEveryNPoints,
                            labelRegion);
                    addSingleLevelLabel(ipr, roi, overlay, pos, label, labelColour, labelSize, labelRegion);
                }
            } else {
                ArrayList<Integer> labelRegion = getLabelPosition(ipr, contour, pos, label, labelSize);
                addSingleLevelContour(ipr, contour, overlay, pos, level, contourColour, lineWidth, drawEveryNPoints,
                        labelRegion);
                addSingleLevelLabel(ipr, contour, overlay, pos, label, labelColour, labelSize, labelRegion);
            }
        }
    }

    public static ArrayList<Integer> getLabelPosition(ImageProcessor ipr, Roi roi, int[] pos, String label,
            int labelSize) {

        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, labelSize);

        int imWidth = ipr.getWidth();
        int imHeight = ipr.getHeight();

        // Creating label, so we know the width
        TextRoi textRoi = new TextRoi(0, 0, label, font);
        double width = textRoi.getFloatWidth();

        // Ensuring there are enough points to draw the label
        FloatPolygon fp = roi.getInterpolatedPolygon();

        if (fp.npoints < width * 4)
            return new ArrayList<>();

        // Ensuring there are enough consecutive non-boundary points to draw the label
        ArrayList<Integer> longestPath = getLongestNonBoundaryPath(fp, imWidth, imHeight);
        if (longestPath.size() < width * 4)
            return new ArrayList<>();

        // Finding a location on the longest continuous part of the contour,
        // sufficiently far from one end
        ArrayList<Integer> labelRegion = new ArrayList<>();
        int startIdx = (int) Math.floor(0.25 * (longestPath.size() - 2 * width));
        // int startIdx = (int) Math.floor(Math.random() * (longestPath.size() - 2.5 *
        // width));
        int endIdx = startIdx + (int) Math.round(2 * width);

        for (int idx = startIdx; idx < endIdx; idx++)
            labelRegion.add(longestPath.get(idx));

        return labelRegion;

    }

    static ArrayList<Integer> getLongestNonBoundaryPath(FloatPolygon fp, int imWidth, int imHeight) {
        ArrayList<Integer> nbp = new ArrayList<>();
        ArrayList<Integer> nbpMax = new ArrayList<>();
        for (int i = 0; i < fp.npoints; i++) {
            float x = fp.xpoints[i];
            float y = fp.ypoints[i];

            if (x >= 1 && x < (imWidth - 1) && y >= 1 && y < (imHeight - 1)) {
                nbp.add(i);
            } else {
                if (nbp.size() > nbpMax.size())
                    nbpMax = new ArrayList<>(nbp);
                nbp = new ArrayList<>();
            }
        }
        if (nbp.size() > nbpMax.size()) {
            nbpMax = new ArrayList<>(nbp);
        }

        return nbpMax;

    }

    public static void addSingleLevelContour(ImageProcessor ipr, Roi roi, ij.gui.Overlay overlay, int[] pos,
            double level, Color colour, double lineWidth, int drawEveryNPoints, ArrayList<Integer> labelRegion) {

        int imWidth = ipr.getWidth();
        int imHeight = ipr.getHeight();

        FloatPolygon fp = roi.getInterpolatedPolygon();
        FloatPolygon fpPlot = new FloatPolygon();
        for (int i = 0; i < fp.npoints - drawEveryNPoints; i = i + drawEveryNPoints) {
            // Skip any points on the label region
            if (labelRegion.contains(i)) {
                addContourFragment(fpPlot, overlay, pos, colour, lineWidth);
                fpPlot = new FloatPolygon();
                continue;
            }

            float x1 = fp.xpoints[i];
            float y1 = fp.ypoints[i];
            float x2 = fp.xpoints[i + drawEveryNPoints];
            float y2 = fp.ypoints[i + drawEveryNPoints];
            if (i >= fp.npoints - 2 * drawEveryNPoints) {
                x2 = fp.xpoints[0];
                y2 = fp.ypoints[0];
            }

            if ((x1 >= 1 && x1 < (imWidth - 1) && y1 >= 1 && y1 < (imHeight - 1))
                    || (x2 >= 1 && x2 < (imWidth - 1) && y2 >= 1 && y2 < (imHeight - 1))) {

                fpPlot.addPoint(x1, y1);
                if (i >= fp.npoints - 2 * drawEveryNPoints)
                    fpPlot.addPoint(x2, y2);

            } else {
                addContourFragment(fpPlot, overlay, pos, colour, lineWidth);
                fpPlot = new FloatPolygon();
            }
        }

        addContourFragment(fpPlot, overlay, pos, colour, lineWidth);

    }
    // new array, x=no~rows of array list items, y = no columns of array list
    // items#generate good

    public static void addContourFragment(FloatPolygon fp, ij.gui.Overlay overlay, int[] pos, Color colour,
            double lineWidth) {

        if (fp.npoints == 0)
            return;

        PolygonRoi line = new PolygonRoi(fp, PolygonRoi.FREELINE);
        line.setStrokeWidth(lineWidth);
        line.setStrokeColor(colour);

        if (pos.length > 1) {
            line.setPosition(pos[0], pos[1], pos[2]);
        } else {
            line.setPosition(pos[0]);
        }
        overlay.addElement(line);

    }

    public static void addSingleLevelLabel(ImageProcessor ipr, Roi roi, ij.gui.Overlay overlay, int[] pos, String label,
            Color colour, int labelSize, ArrayList<Integer> labelRegion) {

        // If no label region was specified, don't add a label
        if (labelRegion.size() == 0)
            return;

        FloatPolygon fp = roi.getInterpolatedPolygon();
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, labelSize);

        // Getting label centre
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        for (int idx : labelRegion) {
            csX.addMeasure(fp.xpoints[idx]);
            csY.addMeasure(fp.ypoints[idx]);
        }
        double xCent = csX.getMean();
        double yCent = csY.getMean();

        // Adding text label
        double angle = getLabelOrientation(fp, labelRegion);
        TextRoi textRoi = createTextRoi(xCent, yCent, angle, label, font, overlay);
        textRoi.setStrokeColor(colour);

        if (pos.length > 1) {
            textRoi.setPosition(pos[0], pos[1], pos[2]);
        } else {
            textRoi.setPosition(pos[0]);
        }

        overlay.addElement(textRoi);

    }

    public static TextRoi createTextRoi(double xCent, double yCent, double angle, String text, Font font,
            ij.gui.Overlay overlay) {
        TextRoi textRoi = new TextRoi(xCent, yCent, text, font);

        double x0 = textRoi.getXBase();
        double y0 = textRoi.getYBase();
        double width = textRoi.getFloatWidth();
        double height = textRoi.getFloatHeight();

        Point2d p1 = new Point2d(x0, y0);
        Point2d pCent = new Point2d(x0 + width / 2 - 1, y0 + height / 2 - 1);
        Point2d pCentR = rotatePoint(pCent, p1, angle);

        double xBaseOffs = 2 * x0 - pCentR.x;
        double yBaseOffs = 2 * y0 - pCentR.y;

        textRoi.setLocation(xBaseOffs, yBaseOffs);
        textRoi.setAngle(angle);

        return textRoi;

    }

    public static Point2d rotatePoint(Point2d pIn, Point2d pRot, double angleD) {
        double angle = -Math.toRadians(angleD);

        double x1 = pIn.x - pRot.x;
        double y1 = pIn.y - pRot.y;

        double x2 = x1 * Math.cos(angle) - y1 * Math.sin(angle);
        double y2 = x1 * Math.sin(angle) + y1 * Math.cos(angle);

        pIn.x = x2 + pRot.x;
        pIn.y = y2 + pRot.y;

        return pIn;

    }

    public static double getLabelOrientation(FloatPolygon fp, ArrayList<Integer> labelRegion) {
        int idx1 = labelRegion.get(0);
        int idx2 = labelRegion.get(labelRegion.size() - 1);

        float x1 = fp.xpoints[idx1];
        float y1 = fp.ypoints[idx1];
        float x2 = fp.xpoints[idx2];
        float y2 = fp.ypoints[idx2];

        if (x1 > x2) {
            return -Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
        } else {
            return -Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        }
    }


    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "Adds overlay contour lines (and optional labels) to an image.  Lines correspond to contours of constant intensity in the input image.";
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

        double minIntensity = parameters.getValue(MINIMUM_INTENSITY);
        double maxIntensity = parameters.getValue(MAXIMUM_INTENSITY);
        int nContours = parameters.getValue(NUMBER_OF_CONTOURS);
        String contourColourMode = parameters.getValue(CONTOUR_COLOUR_MODE);
        String contourColour = parameters.getValue(CONTOUR_COLOUR);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        int drawEveryNPoints = parameters.getValue(DRAW_EVERY_N_POINTS);

        boolean showLabels = parameters.getValue(SHOW_LABELS);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC);
        String labelColourMode = parameters.getValue(LABEL_COLOUR_MODE);
        String labelColour = parameters.getValue(LABEL_COLOUR);
        int labelSize = parameters.getValue(LABEL_SIZE);

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
        HashMap<Double, Color> contourColours = getColours(levels, contourColourMode, contourColour);

        if (showLabels) {
            HashMap<Double, String> labels = getLabels(levels, decimalPlaces, useScientific);
            HashMap<Double, Color> labelColours = getColours(levels, labelColourMode, labelColour);
            addOverlay(ipl, levels, contourColours, lineWidth, drawEveryNPoints, labels, labelColours, labelSize);
        } else {
            addOverlay(ipl, levels, contourColours, lineWidth, drawEveryNPoints);
        }

        // If necessary, adding output image to workspace. This also allows us to show
        // it.
        Image outputImage = new Image(outputImageName, ipl);
        if (addOutputToWorkspace)
            workspace.addImage(outputImage);
        if (showOutput)
            outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(COLOUR_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_INTENSITY, this, 0));
        parameters.add(new DoubleP(MAXIMUM_INTENSITY, this, 255));
        parameters.add(new IntegerP(NUMBER_OF_CONTOURS, this, 9));
        parameters.add(new ChoiceP(CONTOUR_COLOUR_MODE, this, ColourModes.PHYSICS, ColourModes.ALL));
        parameters.add(new ChoiceP(CONTOUR_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));

        parameters.add(new SeparatorP(RENDERING_SEPARATOR, this));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new IntegerP(DRAW_EVERY_N_POINTS, this, 1));

        parameters.add(new SeparatorP(LABEL_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_LABELS, this, true));
        parameters.add(new IntegerP(DECIMAL_PLACES, this, 0));
        parameters.add(new BooleanP(USE_SCIENTIFIC, this, false));
        parameters.add(new ChoiceP(LABEL_COLOUR_MODE, this, ColourModes.PHYSICS, ColourModes.ALL));
        parameters.add(new ChoiceP(LABEL_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new IntegerP(LABEL_SIZE, this, 12));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(COLOUR_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMUM_INTENSITY));
        returnedParameters.add(parameters.getParameter(MAXIMUM_INTENSITY));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_CONTOURS));
        returnedParameters.add(parameters.getParameter(CONTOUR_COLOUR_MODE));
        switch ((String) parameters.getValue(CONTOUR_COLOUR_MODE)) {
            case ColourModes.SINGLE_COLOUR:
            case ColourModes.SINGLE_COLOUR_GRADIENT:
                returnedParameters.add(parameters.getParameter(CONTOUR_COLOUR));
                break;
        }

        returnedParameters.add(parameters.getParameter(RENDERING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        returnedParameters.add(parameters.getParameter(DRAW_EVERY_N_POINTS));

        returnedParameters.add(parameters.getParameter(LABEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_LABELS));
        if ((boolean) parameters.getValue(SHOW_LABELS)) {
            returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
            returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
            returnedParameters.add(parameters.getParameter(LABEL_COLOUR_MODE));
            switch ((String) parameters.getValue(LABEL_COLOUR_MODE)) {
                case ColourModes.SINGLE_COLOUR:
                case ColourModes.SINGLE_COLOUR_GRADIENT:
                    returnedParameters.add(parameters.getParameter(LABEL_COLOUR));
                    break;
            }
            returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        }

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE)
                .setDescription("Image onto which overlay will be rendered.  Input image will only be updated if \""
                        + APPLY_TO_INPUT
                        + "\" is enabled, otherwise the image containing the overlay will be stored as a new image with name specified by \""
                        + OUTPUT_IMAGE + "\".");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(MINIMUM_INTENSITY).setDescription("Minimum contour magnitude to display.");

        parameters.get(MAXIMUM_INTENSITY).setDescription("Maximum contour magnitude to display.");

        parameters.get(NUMBER_OF_CONTOURS).setDescription(
                "Number of different contour magnitudes to display.  Contour lines are equally spaced in magnitude between \""
                        + MINIMUM_INTENSITY + "\" and \"" + MAXIMUM_INTENSITY + "\".");

        parameters.get(CONTOUR_COLOUR_MODE).setDescription(
                "Determines the colour look-up table to use when rendering the contours.  Colour corresponds to the magnitude of that contour line:<br>"

                        + "<br>- \"" + ColourModes.BLACK_FIRE
                        + "\" Standard ImageJ \"Fire\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.ICE
                        + "\" Standard ImageJ \"Ice\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.JET
                        + "\" Look-up table with colour progression blue > cyan > green > yellow > orange > red.<br>"

                        + "<br>- \"" + ColourModes.PHYSICS
                        + "\" Standard Fiji \"Physics\" look-up table.  Values taken from \"https://github.com/fiji/fiji/tree/master/luts/physics.lut\".<br>"

                        + "<br>- \"" + ColourModes.RANDOM + "\" Random sequence of colours.<br>"

                        + "<br>- \"" + ColourModes.SINGLE_COLOUR
                        + "\" All contour lines have the same colour, determined by the \"" + CONTOUR_COLOUR
                        + "\" parameter.<br>"

                        + "<br>- \"" + ColourModes.SINGLE_COLOUR_GRADIENT
                        + "\" Single colour gradient from the colour determined by the \"" + CONTOUR_COLOUR
                        + "\" parameter (lowest magnitude contour line) and white (highest magnitude contour line).<br>"

                        + "<br>- \"" + ColourModes.SPECTRUM
                        + "\" Standard ImageJ \"Spectrum\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.THERMAL
                        + "\" Standard Fiji \"Thermal\" look-up table.  Values taken from \"https://github.com/fiji/fiji/tree/master/luts/Thermal.lut\".<br>");

        parameters.get(CONTOUR_COLOUR)
                .setDescription("Contour colour used when \"" + CONTOUR_COLOUR_MODE + "\" is set to either \""
                        + ColourModes.SINGLE_COLOUR + "\" or \"" + ColourModes.SINGLE_COLOUR_GRADIENT
                        + "\".  Choices are: " + String.join(", ", SingleColours.ALL) + ".");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(DRAW_EVERY_N_POINTS).setDescription(
                "Specifies the interval between plotted points on the contour line.  This is useful when there are a lot of contour lines in an image and where a reduction in line precision isn't problematic.  Plotting fewer points will reduce the memory required to store/display overlays.");

        parameters.get(SHOW_LABELS).setDescription(
                "Display magnitude values as text on each contour line.  Labels are placed randomly on each contour and are oriented in line with the local contour line.");

        parameters.get(DECIMAL_PLACES)
                .setDescription("Number of decimal places to use when displaying numeric values.");

        parameters.get(USE_SCIENTIFIC).setDescription(
                "When enabled, numeric values will be displayed in the format <i>1.23E-3</i>.  Otherwise, the same value would appear as <i>0.00123</i>.");

        parameters.get(LABEL_COLOUR_MODE).setDescription(
                "Determines the colour look-up table to use when rendering the contour labels.  Colour corresponds to the magnitude of that contour line:<br>"

                        + "<br>- \"" + ColourModes.BLACK_FIRE
                        + "\" Standard ImageJ \"Fire\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.ICE
                        + "\" Standard ImageJ \"Ice\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.JET
                        + "\" Look-up table with colour progression blue > cyan > green > yellow > orange > red.<br>"

                        + "<br>- \"" + ColourModes.PHYSICS
                        + "\" Standard Fiji \"Physics\" look-up table.  Values taken from \"https://github.com/fiji/fiji/tree/master/luts/physics.lut\".<br>"

                        + "<br>- \"" + ColourModes.RANDOM + "\" Random sequence of colours.<br>"

                        + "<br>- \"" + ColourModes.SINGLE_COLOUR
                        + "\" All contour line labels have the same colour, determined by the \"" + CONTOUR_COLOUR
                        + "\" parameter.<br>"

                        + "<br>- \"" + ColourModes.SINGLE_COLOUR_GRADIENT
                        + "\" Single colour gradient from the colour determined by the \"" + CONTOUR_COLOUR
                        + "\" parameter (lowest magnitude contour line label) and white (highest magnitude contour line label).<br>"

                        + "<br>- \"" + ColourModes.SPECTRUM
                        + "\" Standard ImageJ \"Spectrum\" look-up table.  Values taken from \"https://github.com/imagej/ImageJA/blob/master/src/main/java/ij/LookUpTable.java\".<br>"

                        + "<br>- \"" + ColourModes.THERMAL
                        + "\" Standard Fiji \"Thermal\" look-up table.  Values taken from \"https://github.com/fiji/fiji/tree/master/luts/Thermal.lut\".<br>");

        parameters.get(LABEL_COLOUR).setDescription("Contour line label colour used when \"" + CONTOUR_COLOUR_MODE
                + "\" is set to either \"" + ColourModes.SINGLE_COLOUR + "\" or \"" + ColourModes.SINGLE_COLOUR_GRADIENT
                + "\".  Choices are: " + String.join(", ", SingleColours.ALL) + ".");

        parameters.get(LABEL_SIZE).setDescription("Font size of the text label.");

    }
}