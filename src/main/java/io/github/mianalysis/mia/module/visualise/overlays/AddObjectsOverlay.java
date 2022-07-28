// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package io.github.mianalysis.mia.module.visualise.overlays;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.drew.lang.annotations.NotNull;

import com.drew.lang.annotations.Nullable;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Arrow;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.mianalysis.mia.process.LabelFactory;

/**
 * Created by sc13967 on 17/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddObjectsOverlay extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String POSITION_MODE = "Position mode";
    public static final String ORIENTATION_MODE = "Arrow orientation mode";
    public static final String PARENT_OBJECT_FOR_ORIENTATION = "Parent object for orientation";
    public static final String MEASUREMENT_FOR_ORIENTATION = "Measurement for orientation";
    public static final String LENGTH_MODE = "Arrow length mode";
    public static final String LENGTH_VALUE = "Length value (px)";
    public static final String PARENT_OBJECT_FOR_LENGTH = "Parent object for length";
    public static final String MEASUREMENT_FOR_LENGTH = "Measurement for length";
    public static final String LENGTH_SCALE = "Arrow length scale";
    public static final String HEAD_SIZE = "Head size";
    public static final String LABEL_MODE = "Label mode";
    public static final String DECIMAL_PLACES = "Decimal places";
    public static final String USE_SCIENTIFIC = "Use scientific notation";
    public static final String LABEL_SIZE = "Label size";
    public static final String PARENT_OBJECT_FOR_LABEL = "Parent object for label";
    public static final String MEASUREMENT_FOR_LABEL = "Measurement for label";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String USE_RADIUS = "Use radius measurement";
    public static final String MEASUREMENT_FOR_RADIUS = "Measurement for radius";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String SPOT_OBJECTS = "Spot objects";
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";
    public static final String TRACK_HISTORY = "Track history (frames)";
    public static final String LINE_WIDTH = "Line width";
    public static final String RENDER_IN_ALL_FRAMES = "Render in all frames";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public AddObjectsOverlay(Modules modules) {
        super("Add overlay", modules);
        deprecated = true;
    }

    public interface OrientationModes {
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[] { PARENT_MEASUREMENT, MEASUREMENT };

    }

    public interface LengthModes {
        String FIXED_VALUE = "Fixed value";
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent measurement";

        String[] ALL = new String[] { FIXED_VALUE, PARENT_MEASUREMENT, MEASUREMENT };

    }

    public interface ColourModes {
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";
        String RANDOM_COLOUR = "Random colour";
        String SINGLE_COLOUR = "Single colour";

        String[] ALL = new String[] { ID, MEASUREMENT_VALUE, PARENT_ID, PARENT_MEASUREMENT_VALUE, RANDOM_COLOUR,
                SINGLE_COLOUR };

    }

    public interface SingleColours extends ColourFactory.SingleColours {
    }

    public interface LabelModes {
        String ID = "ID";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";
        String PARENT_MEASUREMENT_VALUE = "Parent measurement value";

        String[] ALL = new String[] { ID, MEASUREMENT_VALUE, PARENT_ID, PARENT_MEASUREMENT_VALUE };

    }

    public interface PositionModes {
        String ALL_POINTS = "All points";
        String ARROWS = "Arrows";
        String CENTROID = "Centroid";
        String LABEL_ONLY = "Label only";
        String OUTLINE = "Outline";
        String POSITION_MEASUREMENTS = "Position measurements";
        String TRACKS = "Tracks";

        String[] ALL = new String[] { ALL_POINTS, ARROWS, CENTROID, LABEL_ONLY, OUTLINE, POSITION_MEASUREMENTS,
                TRACKS };

    }

    public static void addAllPointsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth,
            boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        // Adding each point
        double[] xx = object.getX(true);
        double[] yy = object.getY(true);
        double[] zz = object.getZ(true, false);

        int t = object.getT() + 1;

        if (renderInAllFrames)
            t = 0;

        for (int i = 0; i < xx.length; i++) {
            PointRoi roi = new PointRoi(xx[i] + 0.5, yy[i] + 0.5);
            roi.setPointType(3);
            roi.setSize(0);
            roi.setStrokeColor(colour);
            roi.setStrokeWidth(lineWidth);

            if (ipl.isHyperStack()) {
                roi.setPosition(1, (int) zz[i] + 1, t);
            } else {
                int pos = Math.max(Math.max(1, (int) zz[i] + 1), t);
                roi.setPosition(pos);
            }
            ipl.getOverlay().addElement(roi);

        }
    }

    public static void addArrowsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, double orientation,
            double arrowLength, double headSize) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        double oriRads = Math.toRadians(orientation);

        // Adding each point
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);

        int z = (int) Math.round(zMean + 1);
        int t = object.getT() + 1;

        // Getting end point
        double x2 = arrowLength * Math.cos(oriRads);
        double y2 = arrowLength * Math.sin(oriRads);

        Arrow arrow = new Arrow(xMean, yMean, xMean + x2, yMean + y2);
        arrow.setHeadSize(headSize);
        arrow.setStrokeColor(colour);
        arrow.setStrokeWidth(lineWidth);

        if (ipl.isHyperStack()) {
            arrow.setPosition(1, (int) z, t);
        } else {
            int pos = Math.max(Math.max(1, (int) z), t);
            arrow.setPosition(pos);
        }
        ipl.getOverlay().addElement(arrow);

    }

    public static void addCentroidOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth,
            boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);

        // Getting coordinates to plot
        int z = (int) Math.round(zMean + 1);
        int t = object.getT() + 1;

        if (renderInAllFrames)
            t = 0;

        // Adding circles where the object centroids are
        PointRoi pointRoi = new PointRoi(xMean + 0.5, yMean + 0.5);
        pointRoi.setPointType(PointRoi.NORMAL);
        if (ipl.isHyperStack()) {
            pointRoi.setPosition(1, z, t);
        } else {
            int pos = Math.max(Math.max(1, z), t);
            pointRoi.setPosition(pos);
        }
        pointRoi.setStrokeColor(colour);
        pointRoi.setStrokeWidth(lineWidth);
        ipl.getOverlay().addElement(pointRoi);
    }

    public static void addOutlineOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth,
            boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        // Still need to get mean coords for label
        int t = object.getT() + 1;

        if (renderInAllFrames)
            t = 0;

        // Running through each slice of this object
        double[][] range = object.getExtents(true, false);
        for (int z = (int) range[2][0]; z <= (int) range[2][1]; z++) {
            Roi polyRoi = object.getRoi(z);

            // If the object doesn't have any pixels in this plane, skip it
            if (polyRoi == null)
                continue;

            if (ipl.isHyperStack()) {
                polyRoi.setPosition(1, z + 1, t);
            } else {
                int pos = Math.max(Math.max(1, z + 1), t);
                polyRoi.setPosition(pos);
            }

            polyRoi.setStrokeColor(colour);
            polyRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(polyRoi);
        }
    }

    public static void addPositionMeasurementsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth,
            String[] posMeasurements, boolean renderInAllFrames) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        double xMean = object.getMeasurement(posMeasurements[0]).getValue();
        double yMean = object.getMeasurement(posMeasurements[1]).getValue();
        double zMean = object.getMeasurement(posMeasurements[2]).getValue();

        // Getting coordinates to plot
        int z = (int) Math.round(zMean + 1);
        int t = object.getT() + 1;

        if (renderInAllFrames)
            t = 0;

        if (posMeasurements[3].equals("")) {
            PointRoi pointRoi = new PointRoi(xMean + 0.5, yMean + 0.5);
            pointRoi.setPointType(PointRoi.NORMAL);
            if (ipl.isHyperStack()) {
                pointRoi.setPosition(1, z, t);
            } else {
                int pos = Math.max(Math.max(1, z), t);
                pointRoi.setPosition(pos);
            }
            pointRoi.setStrokeColor(colour);
            pointRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(pointRoi);

        } else {
            double r = object.getMeasurement(posMeasurements[3]).getValue();
            OvalRoi ovalRoi = new OvalRoi(xMean + 0.5 - r, yMean + 0.5 - r, 2 * r, 2 * r);
            if (ipl.isHyperStack()) {
                ovalRoi.setPosition(1, z, t);
            } else {
                int pos = Math.max(Math.max(1, z), t);
                ovalRoi.setPosition(pos);
            }
            ovalRoi.setStrokeColor(colour);
            ovalRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(ovalRoi);
        }
    }

    public static void addTrackOverlay(Obj object, String spotObjectsName, ImagePlus ipl, Color colour,
            double lineWidth, int history) {
        Objs pointObjects = object.getChildren(spotObjectsName);

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

    public static void addLabelsOverlay(ImagePlus ipl, String label, double[] labelCoords, Color colour,
            int labelSize) {
        if (ipl.getOverlay() == null)
            ipl.setOverlay(new ij.gui.Overlay());

        // Adding text label
        TextRoi text = new TextRoi(labelCoords[0], labelCoords[1], label);
        text.setCurrentFont(new Font(Font.SANS_SERIF, Font.PLAIN, labelSize));
        text.setJustification(TextRoi.CENTER);
        text.setStrokeColor(colour);

        if (ipl.isHyperStack()) {
            text.setPosition(1, (int) labelCoords[2], (int) labelCoords[3]);
        } else {
            text.setPosition((int) Math.max(Math.max(1, labelCoords[2]), labelCoords[3]));
        }
        ipl.getOverlay().addElement(text);

    }

    public HashMap<Integer, Float> getHues(Objs inputObjects, String colourMode, String singleColour,
            String parentObjectsForColourName, String measurementForColour) {
        // Generating colours for each object
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
            default:
                return ColourFactory.getSingleColourValues(inputObjects, singleColour);
            case ColourModes.ID:
                return ColourFactory.getIDHues(inputObjects, true);
            case ColourModes.RANDOM_COLOUR:
                return ColourFactory.getRandomHues(inputObjects);
            case ColourModes.MEASUREMENT_VALUE:
                return ColourFactory.getMeasurementValueHues(inputObjects, measurementForColour, true, new double[]{Double.NaN,Double.NaN});
            case ColourModes.PARENT_ID:
                return ColourFactory.getParentIDHues(inputObjects, parentObjectsForColourName, true);
            case ColourModes.PARENT_MEASUREMENT_VALUE:
                return ColourFactory.getParentMeasurementValueHues(inputObjects, parentObjectsForColourName,
                        measurementForColour, true, new double[]{Double.NaN,Double.NaN});
        }
    }

    public HashMap<Integer, String> getLabels(Objs inputObjects, String labelMode, DecimalFormat df,
            String parentObjectsForLabelName, String measurementForLabel) {
        switch (labelMode) {
            case LabelModes.ID:
                return LabelFactory.getIDLabels(inputObjects, df);
            case LabelModes.MEASUREMENT_VALUE:
                return LabelFactory.getMeasurementLabels(inputObjects, measurementForLabel, df);
            case LabelModes.PARENT_ID:
                return LabelFactory.getParentIDLabels(inputObjects, parentObjectsForLabelName, df);
            case LabelModes.PARENT_MEASUREMENT_VALUE:
                return LabelFactory.getParentMeasurementLabels(inputObjects, parentObjectsForLabelName,
                        measurementForLabel, df);
        }

        return null;

    }

    public void createAllPointsOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            boolean multithread, double lineWidth, boolean renderInAllFrames) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);

                addAllPointsOverlay(object, finalIpl, colour, lineWidth, renderInAllFrames);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");

            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createArrowsOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            boolean multithread, double lineWidth, String oriMode, String oriMeasurementName,
            @Nullable String oriParentName, String lengthMode, String lengthMeasurementName,
            @Nullable String lengthParentName, double lengthValue, double lengthScale, int headSize)
            throws InterruptedException {

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);
                double orientation = 0;
                switch (oriMode) {
                    case OrientationModes.MEASUREMENT:
                        orientation = object.getMeasurement(oriMeasurementName).getValue();
                        break;
                    case OrientationModes.PARENT_MEASUREMENT:
                        orientation = object.getParent(oriParentName).getMeasurement(oriMeasurementName).getValue();
                        break;
                }

                double length = 0;
                switch (lengthMode) {
                    case LengthModes.FIXED_VALUE:
                        length = lengthValue;
                        break;
                    case LengthModes.MEASUREMENT:
                        length = object.getMeasurement(lengthMeasurementName).getValue();
                        break;
                    case LengthModes.PARENT_MEASUREMENT:
                        length = object.getParent(lengthParentName).getMeasurement(lengthMeasurementName).getValue();
                        break;
                }

                length = length * lengthScale;

                addArrowsOverlay(object, finalIpl, colour, lineWidth, orientation, length, headSize);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");

            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createCentroidOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            boolean multithread, double lineWidth, boolean renderInAllFrames) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);
                addCentroidOverlay(object, finalIpl, colour, lineWidth, renderInAllFrames);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createLabelOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            @Nullable HashMap<Integer, String> labels, boolean multithread, int labelSize, boolean renderInAllFrames)
            throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);
                String label = labels == null ? "" : labels.get(object.getID());

                double xMean = object.getXMean(true);
                double yMean = object.getYMean(true);
                double zMean = object.getZMean(true, false);
                int z = (int) Math.round(zMean + 1);
                int t = object.getT() + 1;

                if (renderInAllFrames)
                    t = 0;

                addLabelsOverlay(finalIpl, label, new double[] { xMean, yMean, z, t }, colour, labelSize);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createOutlineOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            boolean multithread, double lineWidth, boolean renderInAllFrames) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);

                addOutlineOverlay(object, finalIpl, colour, lineWidth, renderInAllFrames);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createPositionMeasurementsOverlay(ImagePlus ipl, Objs inputObjects,
            @NotNull HashMap<Integer, Float> values, String[] posMeasurements, boolean multithread, double lineWidth,
            boolean renderInAllFrames) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float value = values.get(object.getID());
                Color colour = ColourFactory.getColour(value, 100);

                addPositionMeasurementsOverlay(object, finalIpl, colour, lineWidth, posMeasurements, renderInAllFrames);

                writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
            };
            pool.submit(task);
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createTracksOverlay(ImagePlus ipl, Objs inputObjects, @NotNull HashMap<Integer, Float> values,
            String spotObjectsName, int history, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will
        // be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // int nThreads = multithread ? Prefs.getThreads() : 1;
        // ThreadPoolExecutor pool = new
        // ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new
        // LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object : inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            // Job task = () -> {
            float value = values.get(object.getID());
            Color colour = ColourFactory.getColour(value, 100);

            addTrackOverlay(object, spotObjectsName, finalIpl, colour, lineWidth, history);

            writeProgressStatus(count.incrementAndGet(), inputObjects.size(), "objects");
            // };
            // pool.submit(task);
        }

        // pool.shutdown();
        // pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never
        // terminate early

    }


    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_OVERLAYS;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED - Please use individual overlay modules (e.g. \"" + new AddLabels(null).getName() + "\", \""
                + new AddObjectOutline(null).getName() + "\", etc.).<br><br>"

                + "Adds an overlay to the specified input image which can represent each specified input object.  This module can render many different types of overlay; options include: "
                + String.join(", ", PositionModes.ALL);

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT,workspace);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String positionMode = parameters.getValue(POSITION_MODE,workspace);
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS,workspace);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE,workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String orientationMode = parameters.getValue(ORIENTATION_MODE,workspace);
        String parentForOrientation = parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION,workspace);
        String measurementForOrientation = parameters.getValue(MEASUREMENT_FOR_ORIENTATION,workspace);
        String lengthMode = parameters.getValue(LENGTH_MODE,workspace);
        double lengthValue = parameters.getValue(LENGTH_VALUE,workspace);
        String parentForLength = parameters.getValue(PARENT_OBJECT_FOR_LENGTH,workspace);
        String measurementForLength = parameters.getValue(MEASUREMENT_FOR_LENGTH,workspace);
        double lengthScale = parameters.getValue(LENGTH_SCALE,workspace);
        int headSize = parameters.getValue(HEAD_SIZE,workspace);
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT,workspace);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT,workspace);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT,workspace);
        boolean useRadius = parameters.getValue(USE_RADIUS,workspace);
        String measurementForRadius = parameters.getValue(MEASUREMENT_FOR_RADIUS,workspace);
        boolean limitHistory = parameters.getValue(LIMIT_TRACK_HISTORY,workspace);
        int history = parameters.getValue(TRACK_HISTORY,workspace);

        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE,workspace);
        String singleColour = parameters.getValue(SINGLE_COLOUR,workspace);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR,workspace);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR,workspace);

        // Getting label settings
        String labelMode = parameters.getValue(LABEL_MODE,workspace);
        int labelSize = parameters.getValue(LABEL_SIZE,workspace);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES,workspace);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC,workspace);
        String parentObjectsForLabelName = parameters.getValue(PARENT_OBJECT_FOR_LABEL,workspace);
        String measurementForLabel = parameters.getValue(MEASUREMENT_FOR_LABEL,workspace);

        double lineWidth = parameters.getValue(LINE_WIDTH,workspace);
        boolean renderInAllFrames = parameters.getValue(RENDER_IN_ALL_FRAMES,workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING,workspace);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput)
            ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer, Float> values = getHues(inputObjects, colourMode, singleColour, parentObjectsForColourName,
                measurementForColour);

        // Adding the overlay element
        try {
            switch (positionMode) {
                case PositionModes.ALL_POINTS:
                    createAllPointsOverlay(ipl, inputObjects, values, multithread, lineWidth, renderInAllFrames);
                    break;
                case PositionModes.ARROWS:
                    createArrowsOverlay(ipl, inputObjects, values, multithread, lineWidth, orientationMode,
                            measurementForOrientation, parentForOrientation, lengthMode, measurementForLength,
                            parentForLength, lengthValue, lengthScale, headSize);
                    break;
                case PositionModes.CENTROID:
                    createCentroidOverlay(ipl, inputObjects, values, multithread, lineWidth, renderInAllFrames);
                    break;
                case PositionModes.LABEL_ONLY:
                    DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces, useScientific);
                    HashMap<Integer, String> labels = getLabels(inputObjects, labelMode, df, parentObjectsForLabelName,
                            measurementForLabel);
                    createLabelOverlay(ipl, inputObjects, values, labels, multithread, labelSize, renderInAllFrames);
                    break;
                case PositionModes.OUTLINE:
                    createOutlineOverlay(ipl, inputObjects, values, multithread, lineWidth, renderInAllFrames);
                    break;
                case PositionModes.POSITION_MEASUREMENTS:
                    if (!useRadius)
                        measurementForRadius = null;
                    String[] posMeasurements = new String[] { xPosMeas, yPosMeas, zPosMeas, measurementForRadius };
                    createPositionMeasurementsOverlay(ipl, inputObjects, values, posMeasurements, multithread, lineWidth,
                            renderInAllFrames);
                    break;
                case PositionModes.TRACKS:
                    if (!limitHistory)
                        history = Integer.MAX_VALUE;
                    createTracksOverlay(ipl, inputObjects, values, spotObjectsName, history, multithread, lineWidth);
                    break;
            }
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
            return Status.FAIL;
        }

        Image outputImage = ImageFactory.createImage(outputImageName, ipl);

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
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this, false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(LABEL_MODE, this, LabelModes.ID, LabelModes.ALL));
        parameters.add(new IntegerP(DECIMAL_PLACES, this, 0));
        parameters.add(new BooleanP(USE_SCIENTIFIC, this, false));
        parameters.add(new IntegerP(LABEL_SIZE, this, 8));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LABEL, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LABEL, this));
        parameters.add(new ChoiceP(POSITION_MODE, this, PositionModes.CENTROID, PositionModes.ALL));
        parameters.add(new ChoiceP(ORIENTATION_MODE, this, OrientationModes.MEASUREMENT, OrientationModes.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_ORIENTATION, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_ORIENTATION, this));
        parameters.add(new ChoiceP(LENGTH_MODE, this, LengthModes.MEASUREMENT, LengthModes.ALL));
        parameters.add(new DoubleP(LENGTH_VALUE, this, 5d));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LENGTH, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LENGTH, this));
        parameters.add(new DoubleP(LENGTH_SCALE, this, 1d));
        parameters.add(new IntegerP(HEAD_SIZE, this, 3));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT, this));
        parameters.add(new BooleanP(USE_RADIUS, this, true));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_RADIUS, this));
        parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR, this, SingleColours.WHITE, SingleColours.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this));
        parameters.add(new ChildObjectsP(SPOT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_TRACK_HISTORY, this, false));
        parameters.add(new IntegerP(TRACK_HISTORY, this, 10));
        parameters.add(new DoubleP(LINE_WIDTH, this, 0.2));
        parameters.add(new BooleanP(RENDER_IN_ALL_FRAMES, this, false));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR,workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT,workspace)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if ((boolean) parameters.getValue(ADD_OUTPUT_TO_WORKSPACE,workspace)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(POSITION_MODE));
        switch ((String) parameters.getValue(POSITION_MODE,workspace)) {
            case PositionModes.ARROWS:
                returnedParameters.add(parameters.getParameter(ORIENTATION_MODE));
                switch ((String) parameters.getValue(ORIENTATION_MODE,workspace)) {
                    case OrientationModes.MEASUREMENT:
                        ObjectMeasurementP oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                        oriMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS,workspace));
                        returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                        break;

                    case OrientationModes.PARENT_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION));
                        ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_ORIENTATION);
                        parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS,workspace));

                        oriMeasurement = parameters.getParameter(MEASUREMENT_FOR_ORIENTATION);
                        oriMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_ORIENTATION,workspace));
                        returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ORIENTATION));
                        break;
                }

                returnedParameters.add(parameters.getParameter(LENGTH_MODE));
                switch ((String) parameters.getValue(LENGTH_MODE,workspace)) {
                    case LengthModes.FIXED_VALUE:
                        returnedParameters.add(parameters.getParameter(LENGTH_VALUE));
                        break;

                    case LengthModes.MEASUREMENT:
                        ObjectMeasurementP lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                        lengthMeasurement.setObjectName(parameters.getValue(INPUT_OBJECTS,workspace));
                        returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LENGTH));
                        break;

                    case LengthModes.PARENT_MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LENGTH));
                        ParentObjectsP parentObjects = parameters.getParameter(PARENT_OBJECT_FOR_LENGTH);
                        parentObjects.setChildObjectsName(parameters.getValue(INPUT_OBJECTS,workspace));

                        lengthMeasurement = parameters.getParameter(MEASUREMENT_FOR_LENGTH);
                        lengthMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_LENGTH,workspace));
                        returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LENGTH));
                        break;
                }

                returnedParameters.add(parameters.getParameter(LENGTH_SCALE));
                returnedParameters.add(parameters.getParameter(HEAD_SIZE));

                break;

            case PositionModes.POSITION_MEASUREMENTS:
                returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));

                ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(USE_RADIUS));
                if ((boolean) parameters.getValue(USE_RADIUS,workspace)) {
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_RADIUS));
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_RADIUS))
                            .setObjectName(inputObjectsName);
                }
                break;

            case PositionModes.TRACKS:
                returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));
                returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

                if ((boolean) parameters.getValue(LIMIT_TRACK_HISTORY,workspace))
                    returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
                ((ChildObjectsP) parameters.getParameter(SPOT_OBJECTS)).setParentObjectsName(inputObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE,workspace)) {
            case ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (parameters.getValue(INPUT_OBJECTS,workspace) != null) {
                    ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                    colourMeasurement.setObjectName(inputObjectsName);
                }
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);
                break;

            case ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR))
                        .setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ObjectMeasurementP colourMeasurement = parameters.getParameter(MEASUREMENT_FOR_COLOUR);
                colourMeasurement.setObjectName(parameters.getValue(PARENT_OBJECT_FOR_COLOUR,workspace));

                break;
        }

        if (!parameters.getValue(POSITION_MODE,workspace).equals(PositionModes.LABEL_ONLY)) {
            returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        }

        if (parameters.getValue(POSITION_MODE,workspace).equals(PositionModes.LABEL_ONLY)) {
            returnedParameters.add(parameters.getParameter(LABEL_MODE));

            switch ((String) parameters.getValue(LABEL_MODE,workspace)) {
                case LabelModes.MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL))
                            .setObjectName(inputObjectsName);
                    break;

                case LabelModes.PARENT_ID:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL))
                            .setChildObjectsName(inputObjectsName);
                    break;

                case LabelModes.PARENT_MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL))
                            .setChildObjectsName(inputObjectsName);

                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                    if (parentObjectsName != null) {
                        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL))
                                .setObjectName(inputObjectsName);
                    }
            }

            returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
            returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
            returnedParameters.add(parameters.getParameter(LABEL_SIZE));

        }

        if (!parameters.getValue(POSITION_MODE,workspace).equals(PositionModes.TRACKS)) {
            returnedParameters.add(parameters.getParameter(RENDER_IN_ALL_FRAMES));
        }

        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
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

        parameters.get(INPUT_OBJECTS).setDescription("Objects to represent as overlays.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Determines if the modifications made to the input image (added overlay elements) will be applied to that image or directed to a new image.  When selected, the input image will be updated.");

        parameters.get(ADD_OUTPUT_TO_WORKSPACE).setDescription(
                "If the modifications (overlay) aren't being applied directly to the input image, this control will determine if a separate image containing the overlay should be saved to the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "The name of the new image to be saved to the workspace (if not applying the changes directly to the input image).");

        parameters.get(LABEL_MODE).setDescription("Controls what information each label displays:<br><ul>"

                + "<li>\"" + LabelModes.ID + "\" The ID number of the object.</li>"

                + "<li>\"" + LabelModes.MEASUREMENT_VALUE
                + "\" A measurement associated with the object.  The measurement is selected using the \""
                + MEASUREMENT_FOR_LABEL + "\" parameter.</li>"

                + "<li>\"" + LabelModes.PARENT_ID
                + "\" The ID number of a parent of the object.  The parent object is selected using the \""
                + PARENT_OBJECT_FOR_LABEL + "\" parameter.</li>"

                + "<li>\"" + LabelModes.PARENT_MEASUREMENT_VALUE
                + "\" A measurement associated with a parent of the object.  The measurement is selected using the \""
                + MEASUREMENT_FOR_LABEL + "\" parameter and the parent object with the \"" + PARENT_OBJECT_FOR_LABEL
                + "\" parameter.</li></ul>");

        parameters.get(DECIMAL_PLACES)
                .setDescription("Number of decimal places to use when displaying numeric values.");

        parameters.get(USE_SCIENTIFIC).setDescription(
                "When enabled, numeric values will be displayed in the format <i>1.23E-3</i>.  Otherwise, the same value would appear as <i>0.00123</i>.");

        parameters.get(LABEL_SIZE).setDescription("Font size of the text label.");

        parameters.get(PARENT_OBJECT_FOR_LABEL).setDescription("If \"" + LABEL_MODE + "\" is set to either \""
                + LabelModes.PARENT_ID + "\" or \"" + LabelModes.PARENT_MEASUREMENT_VALUE
                + "\", these are the parent objects which will be used.  These objects will be parents of the input objects.");

        parameters.get(MEASUREMENT_FOR_LABEL)
                .setDescription("If \"" + LABEL_MODE + "\" is set to either \"" + LabelModes.MEASUREMENT_VALUE
                        + "\" or \"" + LabelModes.PARENT_MEASUREMENT_VALUE
                        + "\", these are the measurements which will be used.");

        parameters.get(POSITION_MODE)
                .setDescription("Controls the sort of overlay to be rendered:<br><ul>"
                
                        + "<li>\"" + PositionModes.ALL_POINTS + "\" All points in each object are rendered as small circles.</li>"

                        + "<li>\"" + PositionModes.ARROWS + "\" Each object is represented by an arrow.  The size, colour and orientation of each arrow can be fixed or based on a measurement value..</li>"

                        + "<li>\"" + PositionModes.CENTROID + "\" Each object is represented by a single small circle positioned at the centre of the object (mean XYZ location).</li>"

                        + "<li>\"" + PositionModes.LABEL_ONLY + "\" Displays a text string at the centroid location of each object.  Text can include object ID numbers, measurements or similar values for parent objects.</li>"

                        + "<li>\"" + PositionModes.OUTLINE + "\" Each object is represented by an outline.</li>"

                        + "<li>\"" + PositionModes.POSITION_MEASUREMENTS + "\" Each object is represented by a single circle positioned at the XYZ location specified by three position measurements.</li>"

                        + "<li>\"" + PositionModes.TRACKS + "\" The trajectory of a track (time-linked objects) is rendered for all track objects.  The line passes between the centre of each timepoint instance of that track and appears as the object moves )(i.e. it only shows the trajectory from previous frames).</li></ul>");

        parameters.get(ORIENTATION_MODE).setDescription("Source for arrow orientation values:<br><ul>"

                + "<li>\"" + OrientationModes.MEASUREMENT
                + "\" Orientation of arrows will be based on the measurement specified by the parameter \""
                + MEASUREMENT_FOR_ORIENTATION + "\" for each object.</li>"

                + "<li>\"" + OrientationModes.PARENT_MEASUREMENT
                + "\" Orientation of arrows will be based on the measurement specified by the parameter \""
                + MEASUREMENT_FOR_ORIENTATION
                + "\" taken from a parent of each object.  The parent object providing this measurement is specified by the parameter \""
                + PARENT_OBJECT_FOR_ORIENTATION + "\".</li></ul>");

        parameters.get(PARENT_OBJECT_FOR_ORIENTATION).setDescription(
                "Parent objects providing the measurements on which the orientation of the arrows are based.");

        parameters.get(MEASUREMENT_FOR_ORIENTATION).setDescription(
                "Measurement that defines the orientation of each arrow.  Measurements should be supplied in degree units.");

        parameters.get(LENGTH_MODE).setDescription("Method for determining the length of arrows:<br><ul>"

                + "<li>\"" + LengthModes.FIXED_VALUE
                + "\" All arrows are the same length.  Length is controlled by the \"" + LENGTH_VALUE
                + "\" parameter.</li>"

                + "<li>\"" + LengthModes.MEASUREMENT
                + "\" Arrow length is proportional to the measurement value specified by the \""
                + MEASUREMENT_FOR_LENGTH + "\" parameter.  Absolute arrow lengths are adjusted by the \"" + LENGTH_SCALE
                + "\" multiplication factor.</li>"

                + "<li>\"" + LengthModes.PARENT_MEASUREMENT
                + "\" Arrow length is proportional to a parent object measurement value.  The parent is specified by the \""
                + PARENT_OBJECT_FOR_LENGTH + "\" parameter and the measurement value by \"" + MEASUREMENT_FOR_LENGTH
                + "\".  Absolute arrow lengths are adjusted by the \"" + LENGTH_SCALE
                + "\" multiplication factor.</li></ul>");

        parameters.get(LENGTH_VALUE).setDescription("Fixed value specifying the length of all arrows in pixel units.");

        parameters.get(PARENT_OBJECT_FOR_LENGTH)
                .setDescription("Parent objects from which the arrow length measurements will be taken.");

        parameters.get(MEASUREMENT_FOR_LENGTH).setDescription(
                "Measurement value that will be used to control the arrow length.  This value is adjusted using the \""
                        + LENGTH_SCALE + "\" muliplication factor.");

        parameters.get(LENGTH_SCALE).setDescription(
                "Measurement values will be multiplied by this value prior to being used to control the arrow length.  Each arrow will be <i>MeasurementValue*LengthScale</i> pixels long.");

        parameters.get(HEAD_SIZE).setDescription(
                "Size of the arrow head.  This should be an integer between 0 and 30, where 0 is the smallest possible head and 30 is the largest.");

        parameters.get(X_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the X-position of the overlay marker.  Measurement value must be specified in pixel units.");

        parameters.get(Y_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the Y-position of the overlay marker.  Measurement value must be specified in pixel units.");

        parameters.get(Z_POSITION_MEASUREMENT).setDescription(
                "Object measurement specifying the Z-position (slice) of the overlay marker.  Measurement value must be specified in slice units.");

        parameters.get(USE_RADIUS).setDescription(
                "When selected, the radius of the overlay marker circle is controlled by the measurement specified by \""
                        + MEASUREMENT_FOR_RADIUS
                        + "\".  When not selected, point is represented by a single spot of fixed size.");

        parameters.get(MEASUREMENT_FOR_RADIUS).setDescription(
                "Object measurement use to specify the radius of the overlay marker circle.  Measurement value must be specified in pixel units.");

        parameters.get(COLOUR_MODE)
                .setDescription("Method for determining colour of each object's corresponding overlay:<br><ul>"

                        + "<li>\"" + ColourModes.ID
                        + "\" Overlay colour is quasi-randomly selected based on the ID number of the object.  The colour used for a specific "
                        + "ID number will always be the same and is calculated using the equation <i>value = (ID * 1048576 % 255) / 255</i>.</li>"

                        + "<li>\"" + ColourModes.MEASUREMENT_VALUE
                        + "\" Overlay colour is determined by a measurement value.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects missing the relevant measurement "
                        + " are always shown in red.  The measurement value is selected with the \""
                        + MEASUREMENT_FOR_COLOUR + "\" parameter.</li>"

                        + "<li>\"" + ColourModes.PARENT_ID
                        + "\" Overlay colour is quasi-randomly selected based on the ID number of a parent of this object.  "
                        + "The colour used for a specific ID number will always be the same and is calculated using the equation <i>value = (ID * 1048576 % 255) / 255</i>.  "
                        + "The parent object is selected with the \"" + PARENT_OBJECT_FOR_COLOUR + "\" parameter.</li>"

                        + "<li>\"" + ColourModes.PARENT_MEASUREMENT_VALUE
                        + "\" Overlay colour is determined by a measurement value of a parent of this object.  "
                        + "Colour range runs across the first half of the visible spectrum (i.e. red to cyan) and is maximised, so the object "
                        + "with the smallest measurement is shown in red and the object with the largest, in cyan.  Objects either missing the relevant measurement or without "
                        + "the relevant parent are always shown in red.  The parent object is selected with the \""
                        + PARENT_OBJECT_FOR_COLOUR + "\" parameter and the measurement "
                        + "value is selected with the \"" + MEASUREMENT_FOR_COLOUR + "\" parameter.</li>"

                        + "<li>\"" + ColourModes.RANDOM_COLOUR
                        + "\" Overlay colour is randomly selected for each object.  " + "Unlike the \"" + ColourModes.ID
                        + "\" option, the colours generated here will be different for each evaluation of the module.</li>"

                        + "<li>\"" + ColourModes.SINGLE_COLOUR
                        + "\" (default option) Overlay colour is fixed to one of a predetermined list of colours.  All objects "
                        + " will be assigned the same overlay colour.  The colour is chosen using the \""
                        + SINGLE_COLOUR + "\" parameter.</li></ul>");

        parameters.get(SINGLE_COLOUR)
                .setDescription("Colour for all object overlays to be rendered using.  This parameter is used if \""
                        + COLOUR_MODE + "\" is set to \"" + ColourModes.SINGLE_COLOUR + "\".  Choices are: "
                        + String.join(", ", SingleColours.ALL) + ".");

        parameters.get(MEASUREMENT_FOR_COLOUR)
                .setDescription("Measurement used to determine the colour of the overlay when \"" + COLOUR_MODE
                        + "\" is set to either \"" + ColourModes.MEASUREMENT_VALUE + "\" or \""
                        + ColourModes.PARENT_MEASUREMENT_VALUE + "\".");

        parameters.get(PARENT_OBJECT_FOR_COLOUR).setDescription(
                "Object collection used to determine the colour of the overlay based on either the ID or measurement value "
                        + " of a parent object when \"" + COLOUR_MODE + "\" is set to either  \""
                        + ColourModes.PARENT_ID + "\" or \"" + ColourModes.PARENT_MEASUREMENT_VALUE
                        + "\".  These objects will be parents of the input objects.");

        parameters.get(SPOT_OBJECTS)
                .setDescription("Objects present in each frame of this track.  These are children of the \""
                        + INPUT_OBJECTS + "\" and provide the coordinate information for each frame.");

        parameters.get(LIMIT_TRACK_HISTORY).setDescription(
                "When enabled, segments of a track will only be displayed for a finite number of frames after the timepoint they correspond to.  This gives the effect of a moving tail behind the object and can be use to prevent the overlay image becoming too cluttered for long/dense videos.  The duration of the track history is specified by the \""
                        + TRACK_HISTORY + "\" parameter.");

        parameters.get(TRACK_HISTORY).setDescription(
                "Number of frames a track segment will be displayed for after the timepoint to which it corresponds.");

        parameters.get(LINE_WIDTH).setDescription("Width of the rendered lines.  Specified in pixel units.");

        parameters.get(RENDER_IN_ALL_FRAMES).setDescription(
                "Display the overlay elements in all frames (time axis) of the input image stack, irrespective of whether the object was present in that frame.");

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple overlay elements simultaneously.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
