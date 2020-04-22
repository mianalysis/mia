package wbif.sjx.MIA.Module.Visualisation.Overlays;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoiMod;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
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
import wbif.sjx.common.MathFunc.CumStat;

public class AddContourLines extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CONTOUR_SEPARATOR = "Contour rendering";
    public static final String MINIMUM_INTENSITY = "Minimum intensity";
    public static final String MAXIMUM_INTENSITY = "Maximum intensity";
    public static final String NUMBER_OF_CONTOURS = "Number of contours";
    public static final String CONTOUR_COLOUR_MODE = "Contour colour mode";
    public static final String CONTOUR_COLOUR = "Contour colour";
    public static final String LINE_WIDTH = "Line width";

    public static final String LABEL_SEPARATOR = "Label rendering";
    public static final String SHOW_LABELS = "Show labels";
    public static final String LABEL_COLOUR_MODE = "Label colour mode";
    public static final String LABEL_COLOUR = "Label colour";
    public static final String LABEL_SIZE = "Label size";

    public interface ColourModes {
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

    public static HashMap<Double, Color> getColours(double[] levels, String colourMode, @Nullable String singleColour) {
        HashMap<Double, Color> colours = new HashMap<>();

        double minIntensity = levels[0];
        double maxIntensity = levels[levels.length - 1];
        double range = maxIntensity - minIntensity;

        for (double level : levels) {
            // Finding normalised position within range
            float norm = ((float) (level - minIntensity)) / ((float) range);

            switch (colourMode) {
                case ColourModes.SINGLE_COLOUR:
                    colours.put(level, ColourFactory.getColour(singleColour));
                    break;
                case ColourModes.SINGLE_COLOUR_GRADIENT:
                    float hue = ColourFactory.getHue(singleColour);
                    colours.put(level, Color.getHSBColor(hue, 1 - norm, 1f));
                    break;
                case ColourModes.SPECTRUM:
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

    public static void addOverlay(ImagePlus ipl, double[] levels, HashMap<Double, Color> contourColours,
            double lineWidth) {
        addOverlay(ipl, levels, contourColours, lineWidth, null, 0);

    }

    public static void addOverlay(ImagePlus ipl, double[] levels, HashMap<Double, Color> contourColours,
            double lineWidth, @Nullable HashMap<Double, Color> labelColours, int labelSize) {
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
                        addOverlay(ipl.getProcessor(), ipl.getOverlay(), pos, levels, contourColours, lineWidth);
                    } else {
                        addOverlay(ipl.getProcessor(), ipl.getOverlay(), pos, levels, contourColours, lineWidth,
                                labelColours, labelSize);
                    }
                }
            }
        }
    }

    public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay, int[] pos, double[] levels,
            HashMap<Double, Color> contourColours, double lineWidth) {
        for (double level : levels) {
            Roi contour = getContour(ipr, level);
            Color contourColour = contourColours.get(level);

            if (contour == null)
                continue;

            if (contour.getType() == Roi.COMPOSITE) {
                ShapeRoi shapeRoi = new ShapeRoi(contour);
                for (Roi roi : shapeRoi.getRois()) {
                    float[] offset = new float[] { (float) shapeRoi.getXBase(), (float) shapeRoi.getXBase() };
                    addSingleLevelContour(ipr, roi, overlay, offset, pos, level, contourColour, lineWidth, new ArrayList<>());
                }
            } else {
                float[] offset = new float[] { (float) contour.getXBase(), (float) contour.getXBase() };
                addSingleLevelContour(ipr, contour, overlay, offset, pos, level, contourColour, lineWidth, new ArrayList<>());
            }
        }
    }

    public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay, int[] pos, double[] levels,
            HashMap<Double, Color> contourColours, double lineWidth, HashMap<Double, Color> labelColours,
            int labelSize) {

        for (double level : levels) {
            Roi contour = getContour(ipr, level);
            Color contourColour = contourColours.get(level);
            Color labelColour = labelColours.get(level);

            if (contour == null)
                continue;

            if (contour.getType() == Roi.COMPOSITE) {
                ShapeRoi shapeRoi = new ShapeRoi(contour);
                for (Roi roi : shapeRoi.getRois()) {
                    float[] offset = new float[] { (float) roi.getXBase(), (float) roi.getXBase() };
                    offset = new float[] { 0, 0 };
                    ArrayList<Integer> labelRegion = getLabelPosition(ipr, roi, offset, pos, level, labelSize);
                    addSingleLevelContour(ipr, roi, overlay, offset, pos, level, contourColour, lineWidth, labelRegion);
                    addSingleLevelLabel(ipr, roi, overlay, offset, pos, level, labelColour, labelSize, labelRegion);
                }
            } else {
                float[] offset = new float[] { (float) contour.getXBase(), (float) contour.getXBase() };
                offset = new float[] { 0, 0 };
                ArrayList<Integer> labelRegion = getLabelPosition(ipr, contour, offset, pos, level, labelSize);
                addSingleLevelContour(ipr, contour, overlay, offset, pos, level, contourColour, lineWidth, labelRegion);
                addSingleLevelLabel(ipr, contour, overlay, offset, pos, level, labelColour, labelSize, labelRegion);
            }
        }
    }

    public static ArrayList<Integer> getLabelPosition(ImageProcessor ipr, Roi roi, float[] offset, int[] pos,
            double level, int labelSize) {

        DecimalFormat df = new DecimalFormat("0");
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, labelSize);

        int imWidth = ipr.getWidth();
        int imHeight = ipr.getHeight();

        // Creating label, so we know the width
        TextRoiMod textRoi = new TextRoiMod(0, 0, df.format(level), font);
        double width = textRoi.getFloatWidth();

        // Ensuring there are enough points to draw the label
        FloatPolygon fp = roi.getInterpolatedPolygon();

        if (fp.npoints < width * 3)
            return new ArrayList<>();

        // Ensuring there are enough consecutive non-boundary points to draw the label
        ArrayList<Integer> longestPath = getLongestNonBoundaryPath(fp, imWidth, imHeight, offset);
        if (longestPath.size() < width * 3)
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

    static ArrayList<Integer> getLongestNonBoundaryPath(FloatPolygon fp, int imWidth, int imHeight, float[] offset) {
        ArrayList<Integer> nbp = new ArrayList<>();
        ArrayList<Integer> nbpMax = new ArrayList<>();
        for (int i = 0; i < fp.npoints; i++) {
            float x = fp.xpoints[i] + offset[0];
            float y = fp.ypoints[i] + offset[1];

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

    public static void addSingleLevelContour(ImageProcessor ipr, Roi roi, ij.gui.Overlay overlay, float[] offset,
            int[] pos, double level, Color colour, double lineWidth, ArrayList<Integer> labelRegion) {

        int imWidth = ipr.getWidth();
        int imHeight = ipr.getHeight();

        FloatPolygon fp = roi.getInterpolatedPolygon();
        for (int i = 0; i < fp.npoints - 1; i++) {
            // Skip any points on the label region
            if (labelRegion.contains(i))
                continue;

            float x1 = fp.xpoints[i] + offset[0];
            float y1 = fp.ypoints[i] + offset[1];
            float x2 = fp.xpoints[i + 1] + offset[0];
            float y2 = fp.ypoints[i + 1] + offset[1];

            if ((x1 >= 1 && x1 < (imWidth - 1) && y1 >= 1 && y1 < (imHeight - 1))
                    || (x2 >= 1 && x2 < (imWidth - 1) && y2 >= 1 && y2 < (imHeight - 1))) {

                Line line = new Line(x1, y1, x2, y2);
                line.setStrokeWidth(lineWidth);
                line.setStrokeColor(colour);

                if (pos.length > 1) {
                    line.setPosition(pos[0], pos[1], pos[2]);
                } else {
                    line.setPosition(pos[0]);
                }
                overlay.addElement(line);
            }
        }
    }

    public static void addSingleLevelLabel(ImageProcessor ipr, Roi roi, ij.gui.Overlay overlay, float[] offset,
            int[] pos, double level, Color colour, int labelSize, ArrayList<Integer> labelRegion) {

        // If no label region was specified, don't add a label
        if (labelRegion.size() == 0)
            return;

        FloatPolygon fp = roi.getInterpolatedPolygon();
        DecimalFormat df = new DecimalFormat("0");
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
        TextRoiMod textRoi = new TextRoiMod(xCent, yCent, df.format(level), font);
        double angle = getLabelOrientation(fp, labelRegion, offset);
        textRoi.setAngle(angle);
        textRoi.setLocation(offset[0] + textRoi.getXBase() - textRoi.getFloatWidth() / 2 + 1,
                offset[1] + textRoi.getYBase() - textRoi.getFloatHeight() / 2 + 1);
        textRoi.setStrokeColor(colour);

        if (pos.length > 1) {
            textRoi.setPosition(pos[0], pos[1], pos[2]);
        } else {
            textRoi.setPosition(pos[0]);
        }

        overlay.addElement(textRoi);

    }

    public static double getLabelOrientation(FloatPolygon fp, ArrayList<Integer> labelRegion, float[] offset) {
        int idx1 = labelRegion.get(0);
        int idx2 = labelRegion.get(labelRegion.size() - 1);

        float x1 = fp.xpoints[idx1] + offset[0];
        float y1 = fp.ypoints[idx1] + offset[1];
        float x2 = fp.xpoints[idx2] + offset[0];
        float y2 = fp.ypoints[idx2] + offset[1];

        if (x1 > x2) {
            return -Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
        } else {
            return -Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
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

        double minIntensity = parameters.getValue(MINIMUM_INTENSITY);
        double maxIntensity = parameters.getValue(MAXIMUM_INTENSITY);
        int nContours = parameters.getValue(NUMBER_OF_CONTOURS);
        String contourColourMode = parameters.getValue(CONTOUR_COLOUR_MODE);
        String contourColour = parameters.getValue(CONTOUR_COLOUR);
        double lineWidth = parameters.getValue(LINE_WIDTH);

        boolean showLabels = parameters.getValue(SHOW_LABELS);
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
            HashMap<Double, Color> labelColours = getColours(levels, labelColourMode, labelColour);
            addOverlay(ipl, levels, contourColours, lineWidth, labelColours, labelSize);
        } else {
            addOverlay(ipl, levels, contourColours, lineWidth);
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
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(CONTOUR_SEPARATOR, this));
        parameters.add(new DoubleP(MINIMUM_INTENSITY, this, 0));
        parameters.add(new DoubleP(MAXIMUM_INTENSITY, this, 255));
        parameters.add(new IntegerP(NUMBER_OF_CONTOURS, this, 0));
        parameters.add(new ChoiceP(CONTOUR_COLOUR_MODE, this, ColourModes.SPECTRUM, ColourModes.ALL));
        parameters.add(new ChoiceP(CONTOUR_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));

        parameters.add(new ParamSeparatorP(LABEL_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_LABELS, this, true));
        parameters.add(new ChoiceP(LABEL_COLOUR_MODE, this, ColourModes.SPECTRUM, ColourModes.ALL));
        parameters.add(new ChoiceP(LABEL_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new IntegerP(LABEL_SIZE, this, 12));

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

        returnedParameters.add(parameters.getParameter(CONTOUR_SEPARATOR));
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
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

        returnedParameters.add(parameters.getParameter(LABEL_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_LABELS));
        if ((boolean) parameters.getValue(SHOW_LABELS)) {
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

///// THE FOLLOWING IS A START ON ORIENTING THE TEXTROI USING THE STOCK
///// TEXTROI CLASS.
// public static void addOverlay(ImageProcessor ipr, ij.gui.Overlay overlay,
// int[] pos, double[] levels,
// HashMap<Double, Color> colours, double lineWidth) {

// int labelSize = 12;
// Font font = new Font(Font.SANS_SERIF, Font.PLAIN, labelSize);

// for (double level : levels) {
// Roi contour = getContour(ipr, level);
// if (contour == null)
// continue;

// ShapeRoi shapeRoi = new ShapeRoi(contour);
// for (Roi roi : shapeRoi.getRois()) {
// FloatPolygon fp = roi.getInterpolatedPolygon();

// // Adding text label
// int idx = (int) Math.round(labelSize * 1.5);
// double angle = 45;

// // TextRoiMod textRoi = new TextRoiMod(fp.xpoints[idx], fp.ypoints[idx],
// // String.valueOf(level), font);
// TextRoi textRoi = new TextRoi(fp.xpoints[idx], fp.ypoints[idx],
// String.valueOf(level), font);
// // textRoi.setDrawOffset(true);
// // textRoi.setDrawStringMode(true);
// // textRoi.setLocation(textRoi.getXBase() - textRoi.getFloatWidth() / 2 + 1,
// // textRoi.getYBase() - textRoi.getFloatHeight() / 2 + 1);
// // textRoi.setRotationCenter(textRoi.getXBase() + textRoi.getFloatWidth() / 2
// ,
// // textRoi.getYBase() + textRoi.getFloatHeight() / 2);

// // if (pos.length > 1) {
// // textRoi.setPosition(pos[0], pos[1], pos[2]);
// // } else {
// // textRoi.setPosition(pos[0]);
// // }

// // textRoi.setLocation(textRoi.getXBase(), textRoi.getYBase() -
// // textRoi.getFloatHeight() / 2 + 1);

// // double[] cent = textRoi.getContourCentroid();
// // overlay.add(new PointRoi(cent[0], cent[1]));

// // FloatPolygon fl = textRoi.getInterpolatedPolygon();
// // overlay.add(new PolygonRoi(fl, PolygonRoi.FREELINE));

// // PointRoi p = new PointRoi(textRoi.getXBase(), textRoi.getYBase());
// // p.setStrokeColor(Color.CYAN);
// // overlay.add(p);

// // p = new PointRoi(textRoi.getXBase() + textRoi.getFloatWidth(),
// textRoi.getYBase());
// // p.setStrokeColor(Color.CYAN);
// // overlay.add(p);

// // p = new PointRoi(textRoi.getXBase(), textRoi.getYBase() +
// textRoi.getFloatHeight());
// // p.setStrokeColor(Color.CYAN);
// // overlay.add(p);

// // p = new PointRoi(textRoi.getXBase() + textRoi.getFloatWidth(),
// // textRoi.getYBase() + textRoi.getFloatHeight());
// // p.setStrokeColor(Color.CYAN);
// // overlay.add(p);

// double x0 = textRoi.getXBase();
// double y0 = textRoi.getYBase();
// double width = textRoi.getFloatWidth();
// double height = textRoi.getFloatHeight();

// Point2d p1 = new Point2d(x0,y0);
// Point2d p2 = new Point2d(x0+width+1,y0);
// Point2d p3 = new Point2d(x0,y0+height+1);
// Point2d p4 = new Point2d(x0 + width+1, y0 + height+1);

// Point2d p1R = rotatePoint(p1, p1, 45);
// Point2d p2R = rotatePoint(p2, p1, 45);
// Point2d p3R = rotatePoint(p3, p1, 45);
// Point2d p4R = rotatePoint(p4, p1, 45);

// PointRoi p = new PointRoi(p1R.getX(), p1R.getY());
// p.setStrokeColor(Color.ORANGE);
// overlay.add(p);

// p = new PointRoi(p2R.getX(), p2R.getY());
// p.setStrokeColor(Color.ORANGE);
// overlay.add(p);

// p = new PointRoi(p3R.getX(), p3R.getY());
// p.setStrokeColor(Color.ORANGE);
// overlay.add(p);

// p = new PointRoi(p4R.getX(), p4R.getY());
// p.setStrokeColor(Color.ORANGE);
// overlay.add(p);

// // overlay.add(
// // new PointRoi(textRoi.getRotationCenter().xpoints[0],
// // textRoi.getRotationCenter().ypoints[0]));

// // Rectangle r = textRoi.getBounds();
// // overlay.add(new Roi(r.x, r.y, r.width, r.height));

// textRoi.setAngle(45);
// overlay.addElement(textRoi);

// for (int i = labelSize * 3; i < fp.npoints - 1; i++) {
// float x1 = fp.xpoints[i];
// float y1 = fp.ypoints[i];
// float x2 = fp.xpoints[i + 1];
// float y2 = fp.ypoints[i + 1];

// Line line = new Line(x1, y1, x2, y2);
// line.setStrokeWidth(lineWidth);
// line.setStrokeColor(colours.get(level));

// if (pos.length > 1) {
// line.setPosition(pos[0], pos[1], pos[2]);
// } else {
// line.setPosition(pos[0]);
// }
// overlay.addElement(line);

// }
// }
// }
// }