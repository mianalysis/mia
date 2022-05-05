// TODO: Check "frame" from RidgeDetection is 0-indexed
// TODO: Add junction linking (could be just for objects with a single shared junction)
// TODO: Add multitimepoint analysis (LineDetector only works on a single image in 2D)

package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.drew.lang.annotations.Nullable;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import ij.ImagePlus;
import ij.measure.Calibration;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectFill;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.mathfunc.CumStat;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import io.github.sjcross.sjcommon.process.IntensityMinMax;
import io.github.sjcross.sjcommon.process.skeletontools.BreakFixer;

/**
 * Created by sc13967 on 30/05/2017.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RidgeDetection extends Module {
    public static final String INPUT_SEPARATOR = "Image input/object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_OBJECTS = "Output objects";

    public static final String DETECTION_SEPARATOR = "Detection settings";
    public static final String CONTOUR_CONTRAST = "Contour contrast";
    public static final String LOWER_THRESHOLD = "Lower threshold";
    public static final String UPPER_THRESHOLD = "Upper threshold";
    public static final String SIGMA = "Sigma";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String EXTEND_LINE = "Extend line";
    public static final String ESTIMATE_WIDTH = "Estimate width";
    public static final String OVERLAP_MODE = "Overlap mode";

    public static final String REFINEMENT_SEPARATOR = "Refinement settings";
    public static final String MIN_LENGTH = "Minimum length";
    public static final String MAX_LENGTH = "Maximum length";
    public static final String LINK_ENDS = "Link ends";
    public static final String ALIGNMENT_RANGE = "Alignment range (px)";
    public static final String MAXIMUM_END_SEPARATION = "Maximum end separation (px)";
    public static final String MAXIMUM_END_MISALIGNMENT = "Maximum end misalignment (degs)";

    public interface ContourContrast {
        String DARK_LINE = "Dark line";
        String LIGHT_LINE = "Light line";

        String[] ALL = new String[] { DARK_LINE, LIGHT_LINE };

    }

    public interface OverlapModes {
        String NONE = "None";
        String SLOPE = "Slope";

        String[] ALL = new String[] { NONE, SLOPE };

    }

    public RidgeDetection(Modules modules) {
        super("Ridge detection", modules);
    }

    private interface Measurements {
        String LENGTH_PX = "RIDGE_DETECT // LENGTH_(PX)";
        String LENGTH_CAL = "RIDGE_DETECT // LENGTH_(${SCAL})";
        String MEAN_HALFWIDTH_PX = "RIDGE_DETECT // MEAN_HALFWIDTH_(PX)";
        String STDEV_HALFWIDTH_PX = "RIDGE_DETECT // STDEV_HALFWIDTH_(PX)";
        String MEAN_HALFWIDTH_CAL = "RIDGE_DETECT // MEAN_HALFWIDTH_(${SCAL})";
        String STDEV_HALFWIDTH_CAL = "RIDGE_DETECT // STDEV_HALFWIDTH_(${SCAL})";

    }

    public static void linkEnds(HashMap<Line, HashSet<Line>> groups, int endRange, double maxEndSeparation, double maxMisalignment) {
        ArrayList<Line> lines = new ArrayList<>(groups.keySet());

        for (Line line1 : lines) {
            for (Line line2 : lines) {
                // Don't compare to the same line
                if (line1 == line2)
                    continue;

                // Testing end alignment
                double misalignment = testEndAlignment(line1, line2, endRange, maxEndSeparation);
                if (misalignment > maxMisalignment)
                    continue;

                HashSet<Line> group1 = groups.get(line1);
                HashSet<Line> group2 = groups.get(line2);

                // Adding all entries from the second LineGroup into the first
                group1.addAll(group2);

                // Removing the second Line from the HashMap, then re-adding it with the first
                // LineGroup
                groups.remove(line2);
                groups.put(line2, group1);

            }
        }
    }

    public static double testEndAlignment(Line line1, Line line2, int endRange, double maxEndSeparation) {
        // Calculating alignment of 4 end combinations and returning smallest alignment
        // within range. If none are within range, return Double.MAX_VALUE

        // Getting end points
        float[] xx1 = line1.getXCoordinates();
        float[] yy1 = line1.getYCoordinates();
        float[] xx2 = line2.getXCoordinates();
        float[] yy2 = line2.getYCoordinates();

        float x1_1 = xx1[0];
        float y1_1 = yy1[0];
        float x1_2 = xx1[xx1.length - 1];
        float y1_2 = yy1[yy1.length - 1];
        float x2_1 = xx2[0];
        float y2_1 = yy2[0];
        float x2_2 = xx2[xx2.length - 1];
        float y2_2 = yy2[yy2.length - 1];

        double minMisalignment = Double.MAX_VALUE;
        if (calculatePointPointSeparation(x1_1, y1_1, x2_1, y2_1) <= maxEndSeparation)
            minMisalignment = Math.min(minMisalignment,
                    calculateEndMisalignment(line1, line2, x1_1, y1_1, x2_1, y2_1, endRange));

        if (calculatePointPointSeparation(x1_1, y1_1, x2_2, y2_2) <= maxEndSeparation)
            minMisalignment = Math.min(minMisalignment,
                    calculateEndMisalignment(line1, line2, x1_1, y1_1, x2_2, y2_2, endRange));

        if (calculatePointPointSeparation(x1_2, y1_2, x2_1, y2_1) <= maxEndSeparation)
            minMisalignment = Math.min(minMisalignment,
                    calculateEndMisalignment(line1, line2, x1_2, y1_2, x2_1, y2_1, endRange));

        if (calculatePointPointSeparation(x1_2, y1_2, x2_2, y2_2) <= maxEndSeparation)
            minMisalignment = Math.min(minMisalignment,
                    calculateEndMisalignment(line1, line2, x1_2, y1_2, x2_2, y2_2, endRange));
                        
        return minMisalignment;

    }

    public static double calculatePointPointSeparation(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);

    }

    public static double calculateEndMisalignment(Line line1, Line line2, float x1, float y1, float x2, float y2,
            int endRange) {
        ArrayList<int[]> end1 = getLineAtJunction(line1, x1, y1, endRange);
        ArrayList<int[]> end2 = getLineAtJunction(line2, x2, y2, endRange);

        double angle1 = Math.toDegrees(BreakFixer.getEndAngleRads(end1));
        double angle2 = Math.toDegrees(BreakFixer.getEndAngleRads(end2)) - 180;

        double misAlignment = angle1 - angle2;
        misAlignment = Math.abs((misAlignment + 180) % 360 - 180);

        return misAlignment;

    }

    public static ArrayList<int[]> getLineAtJunction(Line line, float xEnd, float yEnd, int endRange) {
        // Get line coordinates
        float[] x = line.getXCoordinates();
        float[] y = line.getYCoordinates();

        // Finding which end is to be linked
        float dx1 = x[0] - xEnd;
        float dy1 = y[0] - yEnd;
        double dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);

        float dx2 = x[x.length - 1] - xEnd;
        float dy2 = y[y.length - 1] - yEnd;
        double dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        ArrayList<int[]> c = new ArrayList<>();
        if (dist1 < dist2)
            for (int i = 0; i < Math.min(x.length - 1, endRange); i++)
                c.add(new int[] { Math.round(x[i]), Math.round(y[i]) });
        else
            for (int i = x.length - 1; i >= Math.max(0, x.length - endRange); i--)
                c.add(new int[] { Math.round(x[i]), Math.round(y[i]) });

        return c;

    }

    public static Obj initialiseObject(Objs outputObjects, int t) {
        Obj outputObject = outputObjects.createAndAddNewObject(VolumeType.POINTLIST);
        outputObject.setT(t);

        return outputObject;

    }

    public static void addLine(Obj outputObject, int z, Line line, @Nullable CumStat width) {
        // Adding coordinates for the current line
        float[] x = line.getXCoordinates();
        float[] y = line.getYCoordinates();
        float[] widthL = line.getLineWidthL();
        float[] widthR = line.getLineWidthR();

        for (int i = 0; i < x.length; i++) {
            try {
                outputObject.add(Math.round(x[i]), Math.round(y[i]), z);
            } catch (PointOutOfRangeException e) {
            }

            // If necessary, calculating width
            if (width != null) {
                float halfWidth = (widthL[i] + widthR[i]) / 2;
                addPointWidth(outputObject, x[i], y[i], z, halfWidth);
                width.addMeasure(halfWidth);
            }
        }
    }

    public static void addPointWidth(Obj object, float x, float y, int z, float halfWidth) {
        int xMin = Math.round(x - halfWidth);
        int xMax = Math.round(x + halfWidth);
        int yMin = Math.round(y - halfWidth);
        int yMax = Math.round(y + halfWidth);

        for (int xx = xMin; xx <= xMax; xx++) {
            for (int yy = yMin; yy <= yMax; yy++) {
                if (Math.sqrt((xx - x) * (xx - x) + (yy - y) * (yy - y)) > halfWidth)
                    continue;
                if (xx < 0 || xx >= object.getWidth() || yy < 0 || yy >= object.getHeight())
                    continue;
                try {
                    object.add(xx, yy, z);
                } catch (PointOutOfRangeException e) {
                }
            }
        }
    }

    public static void addMeasurements(Obj object, double estimatedLength, @Nullable CumStat width) {
        double dppXY = object.getDppXY();

        // Setting single values for the current contour
        object.addMeasurement(new Measurement(Measurements.LENGTH_PX, estimatedLength));
        object.addMeasurement(new Measurement(Measurements.LENGTH_CAL, estimatedLength * dppXY));

        if (width == null) {
            object.addMeasurement(new Measurement(Measurements.MEAN_HALFWIDTH_PX, Double.NaN));
            object.addMeasurement(new Measurement(Measurements.STDEV_HALFWIDTH_PX, Double.NaN));
            object.addMeasurement(new Measurement(Measurements.MEAN_HALFWIDTH_CAL, Double.NaN));
            object.addMeasurement(new Measurement(Measurements.STDEV_HALFWIDTH_CAL, Double.NaN));
        } else {
            object.addMeasurement(new Measurement(Measurements.MEAN_HALFWIDTH_PX, width.getMean()));
            object.addMeasurement(new Measurement(Measurements.STDEV_HALFWIDTH_PX, width.getStd()));
            object.addMeasurement(new Measurement(Measurements.MEAN_HALFWIDTH_CAL, width.getMean() * dppXY));
            object.addMeasurement(new Measurement(Measurements.STDEV_HALFWIDTH_CAL, width.getStd() * dppXY));
        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getDescription() {
        return "Detects ridge objects in an image from the workspace.  A ridge is considered as a line of higher (or lower) intensity pixels in an image.  Ridges are output as objects to the workspace with relevant measurements associated with each object (e.g. ridge length).  This module uses the \"<a href=\"https://imagej.net/Ridge_Detection\">Ridge Detection</a>\" plugin, which itself is based on the paper \"An Unbiased Detector of Curvilinear Structures\" (Steger, C., <i>IEEE Transactions on Pattern Analysis and Machine Intelligence</i> (1998) <b>20</b> 113–125)."

                + "<br><br>Note: This module detects ridges in 2D, but can be run on multi-dimensional images.  For higher dimensionality images than 2D, ridge detection is performed slice-by-slice, with output objects confined to a single slice.";

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters (RidgeDetection plugin wants to use pixel units only)
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);
        String contourContrast = parameters.getValue(CONTOUR_CONTRAST);
        boolean darkLine = contourContrast.equals(ContourContrast.DARK_LINE);
        double lowerThreshold = parameters.getValue(LOWER_THRESHOLD);
        double upperThreshold = parameters.getValue(UPPER_THRESHOLD);
        double sigma = parameters.getValue(SIGMA);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean extendLine = parameters.getValue(EXTEND_LINE);
        boolean estimateWidth = parameters.getValue(ESTIMATE_WIDTH);
        String overlapMode = parameters.getValue(OVERLAP_MODE);
        double minLength = parameters.getValue(MIN_LENGTH);
        double maxLength = parameters.getValue(MAX_LENGTH);
        boolean linkEnds = parameters.getValue(LINK_ENDS);
        int alignmentRange = parameters.getValue(ALIGNMENT_RANGE);
        double maxEndSeparation = parameters.getValue(MAXIMUM_END_SEPARATION);
        double maxEndMisalignment = parameters.getValue(MAXIMUM_END_MISALIGNMENT);

        // Converting distances to calibrated units if necessary
        if (calibratedUnits) {
            Calibration calibration = inputImage.getImagePlus().getCalibration();
            sigma = calibration.getRawX(sigma);
            minLength = calibration.getRawX(minLength);
            maxLength = calibration.getRawX(maxLength);
        }

        // Getting overlap mode
        OverlapOption overlapOption = null;
        switch (overlapMode) {
            case OverlapModes.NONE:
            default:
                overlapOption = OverlapOption.NONE;
                break;
            case OverlapModes.SLOPE:
                overlapOption = OverlapOption.SLOPE;
                break;
        }

        ImagePlus inputIpl = inputImage.getImagePlus();
        LineDetector lineDetector = new LineDetector();
        SpatCal calibration = SpatCal.getFromImage(inputIpl);
        int nFrames = inputIpl.getNFrames();
        double frameInterval = inputIpl.getCalibration().frameInterval;
        Objs outputObjects = new Objs(outputObjectsName, calibration, nFrames, frameInterval,
                TemporalUnit.getOMEUnit());
        workspace.addObjects(outputObjects);

        // Iterating over each image in the stack
        int count = 1;
        int total = inputIpl.getNChannels() * inputIpl.getNSlices() * inputIpl.getNFrames();
        CumStat width = null;

        for (int c = 0; c < inputIpl.getNChannels(); c++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                for (int t = 0; t < inputIpl.getNFrames(); t++) {
                    inputIpl.setPosition(c + 1, z + 1, t + 1);

                    // Running the ridge detection
                    Lines lines;
                    try {
                        lines = lineDetector.detectLines(inputIpl.getProcessor(), sigma, upperThreshold, lowerThreshold,
                                minLength, maxLength, darkLine, true, estimateWidth, extendLine, overlapOption);
                    } catch (NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
                        continue;
                    }

                    // If linking contours, adding all to a HashSet. This prevents the same contours
                    // being added to the same set
                    HashMap<Line, HashSet<Line>> groups = new HashMap<>(); // Stored as <Line,LineGroup>
                    for (Line line : lines) {
                        HashSet<Line> lineGroup = new HashSet<>();
                        lineGroup.add(line);
                        groups.put(line, lineGroup);
                    }

                    // Iterating over each object, adding it to the nascent Objs
                    if (linkEnds)
                        linkEnds(groups, alignmentRange, maxEndSeparation, maxEndMisalignment);

                    // Getting the unique LineGroups and converting them to Obj
                    Set<HashSet<Line>> uniqueLineGroup = new HashSet<>(groups.values());
                    for (HashSet<Line> lineGroup : uniqueLineGroup) {
                        Obj outputObject = initialiseObject(outputObjects, t);

                        double estimatedLength = 0;
                        if (estimateWidth)
                            width = new CumStat();

                        for (Line line : lineGroup) {
                            addLine(outputObject, z, line, width);
                            estimatedLength += line.estimateLength();
                        }

                        addMeasurements(outputObject, estimatedLength, width);

                    }

                    writeProgressStatus(count++, total, "images");

                }
            }
        }

        inputIpl.setPosition(1, 1, 1);

        if (showOutput) {
            // Adding image to workspace
            writeStatus("Adding objects (" + outputObjectsName + ") to workspace");

            // Creating a duplicate of the input image
            ImagePlus dispIpl = inputImage.getImagePlus().duplicate();
            IntensityMinMax.run(dispIpl, true);

            // Creating the overlay
            HashMap<Integer, Float> hues = ColourFactory.getIDHues(outputObjects, true);
            HashMap<Integer, Color> colours = ColourFactory.getColours(hues);
            AddObjectFill.addOverlay(dispIpl, outputObjects, colours, false, true);

            dispIpl.show();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));

        parameters.add(new SeparatorP(DETECTION_SEPARATOR, this));
        parameters.add(new ChoiceP(CONTOUR_CONTRAST, this, ContourContrast.DARK_LINE, ContourContrast.ALL));
        parameters.add(new DoubleP(LOWER_THRESHOLD, this, 0.5));
        parameters.add(new DoubleP(UPPER_THRESHOLD, this, 0.85));
        parameters.add(new DoubleP(SIGMA, this, 3d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new BooleanP(EXTEND_LINE, this, false));
        parameters.add(new BooleanP(ESTIMATE_WIDTH, this, false));
        parameters.add(new ChoiceP(OVERLAP_MODE, this, OverlapModes.NONE, OverlapModes.ALL));

        parameters.add(new SeparatorP(REFINEMENT_SEPARATOR, this));
        parameters.add(new DoubleP(MIN_LENGTH, this, 0d));
        parameters.add(new DoubleP(MAX_LENGTH, this, 0d));
        parameters.add(new BooleanP(LINK_ENDS, this, false));
        parameters.add(new IntegerP(ALIGNMENT_RANGE, this, 3));
        parameters.add(new DoubleP(MAXIMUM_END_SEPARATION, this, 5));
        parameters.add(new DoubleP(MAXIMUM_END_MISALIGNMENT, this, 10));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_OBJECTS));

        returnedParameters.add(parameters.getParameter(DETECTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CONTOUR_CONTRAST));
        returnedParameters.add(parameters.getParameter(LOWER_THRESHOLD));
        returnedParameters.add(parameters.getParameter(UPPER_THRESHOLD));
        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SIGMA));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(EXTEND_LINE));
        returnedParameters.add(parameters.getParameter(ESTIMATE_WIDTH));
        returnedParameters.add(parameters.getParameter(OVERLAP_MODE));

        returnedParameters.add(parameters.getParameter(REFINEMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MIN_LENGTH));
        returnedParameters.add(parameters.getParameter(MAX_LENGTH));
        returnedParameters.add(parameters.getParameter(LINK_ENDS));
        if ((boolean) parameters.getValue(LINK_ENDS)) {
            returnedParameters.add(parameters.getParameter(ALIGNMENT_RANGE));
            returnedParameters.add(parameters.getParameter(MAXIMUM_END_SEPARATION));
            returnedParameters.add(parameters.getParameter(MAXIMUM_END_MISALIGNMENT));

        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_PX);
        reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        reference.setDescription("Length of detected, \"" + outputObjectsName + "\" ridge object.  Measured in pixel " +
                "units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.LENGTH_CAL);
        reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
        reference.setDescription(
                "Length of detected, \"" + outputObjectsName + "\" ridge object.  Measured in calibrated " +
                        "(" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
        returnedRefs.add(reference);

        if ((boolean) parameters.getValue(ESTIMATE_WIDTH)) {
            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_HALFWIDTH_PX);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription(
                    "Mean half width of detected, \"" + outputObjectsName + "\" ridge object.  Half width" +
                            "is from the central (backbone) of the ridge to the edge.  Measured in pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.STDEV_HALFWIDTH_PX);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Standard deviation of the half width of detected, \"" + outputObjectsName + "\" "
                    +
                    "ridge object.  Half width is from the central (backbone) of the ridge to the edge.  Measured in " +
                    "pixel units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.MEAN_HALFWIDTH_CAL);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription(
                    "Mean half width of detected, \"" + outputObjectsName + "\" ridge object.  Half width" +
                            "is from the central (backbone) of the ridge to the edge.  Measured in calibrated " +
                            "(" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.STDEV_HALFWIDTH_CAL);
            reference.setObjectsName(parameters.getValue(OUTPUT_OBJECTS));
            reference.setDescription("Standard deviation of the half width of detected, \"" + outputObjectsName + "\" "
                    +
                    "ridge object.  Half width is from the central (backbone) of the ridge to the edge.  Measured in " +
                    "calibrated (" + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
            returnedRefs.add(reference);

        }

        return returnedRefs;

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
        parameters.get(INPUT_IMAGE).setDescription(
                "Image from the workspace on which ridges will be identified.  This can be a multi-dimensional image, although ridges are currently only detected in 2D.  In the case of higher-dimensionality images, ridges are detected on a slice-by-slice basis.");

        parameters.get(OUTPUT_OBJECTS).setDescription(
                "Output ridge objects, which will be added to the workspace.  These objects will have measurements associated with them.");

        parameters.get(CONTOUR_CONTRAST).setDescription(
                "Controls if we are detecting dark lines on a lighter background or light lines on a darker background.");

        parameters.get(LOWER_THRESHOLD).setDescription(
                "Lower response threshold for points on a line to be accepted.  This threshold is based on the value of points after the ridge enhancement filtering and is not a reflection of their absolute pixel intensities.");

        parameters.get(UPPER_THRESHOLD).setDescription(
                "Upper response threshold for points on a line to be accepted.  This threshold is based on the value of points after the ridge enhancement filtering and is not a reflection of their absolute pixel intensities.");

        parameters.get(SIGMA).setDescription(
                "Sigma of the derivatives to be applied to the input image when detecting ridges.  This is related to the width of the lines to be detected and is greater than or equal to \"width/(2*sqrt(3))\".");

        parameters.get(CALIBRATED_UNITS).setDescription(
                "When selected, spatial values are assumed to be specified in calibrated units (as defined by the \""
                        + new InputControl(null).getName() + "\" parameter \"" + InputControl.SPATIAL_UNIT
                        + "\").  Otherwise, pixel units are assumed.");

        parameters.get(EXTEND_LINE).setDescription("Lines are extended in an attempt to locate more junction points.");

        parameters.get(ESTIMATE_WIDTH).setDescription(
                "When this is selected, the width of each line is estimated.  All points within the width extents of each line are included in the output objects (i.e. output objects are broad).  When not selected, the output objects have single pixel width.  If estimating width, width-based measurements will be associated with each output object.");

        parameters.get(OVERLAP_MODE).setDescription(
                "Controls how intersecting lines should be handled.  For more information see <a href=\"https://imagej.net/Ridge_Detection.html#Overlap_resolution\"/>.<br><ul>"

                        + "<li>\"" + OverlapModes.NONE
                        + "\" Ridges are terminated at line intersections, so two overlapping ridges will likely be identified as at least four separate ridge objects.</li>"

                        + "<li>\"" + OverlapModes.SLOPE
                        + "\" When selected, this will attempt to resolve ridge overlaps such that two overlapping ridges would be identified as two separate objects.  Ridges output in this manner can have common, shared paths (i.e. during the overlap region).</li></ul>");

        parameters.get(MIN_LENGTH).setDescription(
                "Minimum length of a detected line for it to be retained and output.  Specified in pixel units, unless \""
                        + CALIBRATED_UNITS + "\" is selected.");

        parameters.get(MAX_LENGTH).setDescription(
                "Maximum length of a detected line for it to be retained and output.  Specified in pixel units, unless \""
                        + CALIBRATED_UNITS + "\" is selected.");

        parameters.get(LINK_ENDS).setDescription(
                "When selected, ridges with ends in close proximity will be linked into a single object.");

        // parameters.get(LIMIT_END_MISALIGNMENT).setDescription("When selected (and \"" + LINK_ENDS
        //         + "\" is also enabled), this limits the permitted difference in the orientation of any linked ends.  This filter helps allow ridge linking where the ends are pointing in the same direction, whilst excluding those that are oriented very differently (e.g. at a junction).  The maximum allowed orientation difference is specified by \""
        //         + MAXIMUM_END_MISALIGNMENT + "\".");

        parameters.get(ALIGNMENT_RANGE).setDescription(
                "If linking contours, but limiting the end misalignment, this is the number of points from each contour end for which the orientation of that end is calculated.");

        parameters.get(MAXIMUM_END_MISALIGNMENT).setDescription(
                "If linking contours, but limiting the end misalignment, this is the maximum permitted misalignment of each contour end.");

    }
}
