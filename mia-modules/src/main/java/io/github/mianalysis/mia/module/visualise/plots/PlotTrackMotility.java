package io.github.mianalysis.mia.module.visualise.plots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.google.common.primitives.Doubles;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.visualise.overlays.AbstractOverlay;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactories;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class PlotTrackMotility extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Object input/ImageI output";
    public static final String INPUT_TRACKS = "Input tracks";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String PLOT_SEPARATOR = "Plot controls";
    public static final String PLOT_MODE = "Plot mode";
    public static final String LINE_WIDTH = "Line width";
    public static final String ADD_OBJECT_ID = "Add object ID";
    public static final String OBJECT_ID_COLOUR = "Object ID colour";

    public static final String AXIS_SEPARATOR = "Axis controls";
    public static final String X_MIN_RANGE_MODE = "X-min range mode";
    public static final String X_RANGE_MIN = "X-range min";
    public static final String X_MAX_RANGE_MODE = "X-max range mode";
    public static final String X_RANGE_MAX = "X-range max";
    public static final String Y_MIN_RANGE_MODE = "Y-min range mode";
    public static final String Y_RANGE_MIN = "Y-range min";
    public static final String Y_MAX_RANGE_MODE = "Y-max range mode";
    public static final String Y_RANGE_MAX = "Y-range max";

    public interface PlotModes {
        public String ALL_TOGETHER = "All together";
        public String SEPARATE_PLOTS = "Separate plots";

        public String[] ALL = new String[] { ALL_TOGETHER, SEPARATE_PLOTS };

    }

    public interface Colours {
        String BLACK = "Black";
        String BLUE = "Blue";
        String CYAN = "Cyan";
        String GREEN = "Green";
        String MAGENTA = "Magenta";
        String RED = "Red";
        String YELLOW = "Yellow";

        String[] ALL = new String[] { BLACK, BLUE, CYAN, GREEN, MAGENTA, RED, YELLOW };

    }

    public PlotTrackMotility(ModulesI modules) {
        super("Plot track motility", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION_PLOTS;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";

    }

    public static ImageI createPlotsAllTogether(ObjsI trackObjects, String inputObjectsName, String outputImageName,
            double lineWidth, HashMap<Integer, Color> colours, String[] rangeModes, double[] ranges) {
        // Creating plot
        Plot plot = new Plot("Motility", "x-position (px)", "y-position (px)");

        // Iterating over each track, creating the plot
        int count = 0;
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();

        for (ObjI trackObject : trackObjects.values()) {
            // Getting values to plot
            double[][] posDouble = getPositionValues(trackObject, inputObjectsName);

            for (int i = 0; i < posDouble[0].length; i++) {
                csX.addMeasure(posDouble[0][i]);
                csY.addMeasure(posDouble[1][i]);
            }

            plot.setLineWidth((float) lineWidth);
            plot.setColor(colours.get(trackObject.getID()));
            plot.add("Line", posDouble[0], posDouble[1]);

            writeProgressStatus(count, trackObjects.size(), "tracks", "Plot track motility");

        }

        double[] limits = setPlotLimits(plot, csX, csY, rangeModes, ranges);
        setPlotSize(plot, limits);

        return ImageFactories.getDefaultFactory().create(outputImageName, plot.getImagePlus());

    }

    public static ImageI createPlotsSeparately(ObjsI trackObjects, String inputObjectsName, String outputImageName,
            double lineWidth, HashMap<Integer, Color> colours, double[] ranges, boolean addObjectID, String objectIDColour) {
        ImagePlus ipl = null;

        int count = 0;
        for (ObjI trackObject : trackObjects.values()) {
            // Initialising plot
            Plot plot = new Plot(trackObjects.getName() + " (ID " + trackObject.getID() + ")", "x-position (px)",
                    "y-position (px)");

            // Iterating over each track, creating the plot
            CumStat csX = new CumStat();
            CumStat csY = new CumStat();

            // Getting values to plot
            double[][] posDouble = getPositionValues(trackObject, inputObjectsName);

            for (int i = 0; i < posDouble[0].length; i++) {
                csX.addMeasure(posDouble[0][i]);
                csY.addMeasure(posDouble[1][i]);
            }

            plot.setLineWidth((float) lineWidth);
            plot.setColor(colours.get(trackObject.getID()));
            plot.add("Line", posDouble[0], posDouble[1]);

            plot.setLimits(ranges[0], ranges[1], ranges[2], ranges[3]);
            int[] sizes = setPlotSize(plot, ranges);

            if (addObjectID) {
                plot.setColor(objectIDColour);
                plot.addLabel(((double) 10)/((double) sizes[0]), ((double) 25)/((double) sizes[1]), trackObjects.getName() + " (ID " + trackObject.getID() + ") ");
            }

            ImagePlus currIpl = plot.getImagePlus();
            if (ipl == null)
                if (trackObjects.size() == 1)
                    ipl = currIpl;
                else
                    ipl = IJ.createImage("Plots", currIpl.getWidth(), currIpl.getHeight(), trackObjects.size(), 24);

            ipl.getStack().setProcessor(currIpl.getProcessor(), count++ + 1);

            writeProgressStatus(count, trackObjects.size(), "tracks", "Plot track motility");

        }

        // If no tracks exist, create a blank plot, so subsequent modules don't crash
        if (ipl == null)
            ipl = new Plot(trackObjects.getName() + " (ID 0)", "x-position (px)", "y-position (px)").getImagePlus();
        
        return ImageFactories.getDefaultFactory().create(outputImageName, ipl);

    }

    public static double[][] getPositionValues(ObjI trackObject, String objectsName) {
        ObjsI childObjects = trackObject.getChildren(objectsName);

        // Find first object in track
        int minT = Integer.MAX_VALUE;
        ObjI currObj = null;
        for (ObjI childObject : childObjects.values()) {
            if (childObject.getT() < minT) {
                minT = childObject.getT();
                currObj = childObject;
            }
        }

        // Creating stores for the coordinates
        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        x.add(0d);
        y.add(0d);

        // Iterating over each timepoint instance, adding a link to the previous object
        double x0 = currObj.getXMean(true);
        double y0 = currObj.getYMean(true);

        ObjsI nextObjects = currObj.getNextPartners(objectsName);
        while (nextObjects.size() > 0) {
            ObjI nextObject = nextObjects.getFirst();

            x.add(nextObject.getXMean(true) - x0);
            y.add(nextObject.getYMean(true) - y0);

            nextObjects = nextObject.getNextPartners(objectsName);

        }

        double[][] positions = new double[2][x.size()];
        positions[0] = Doubles.toArray(x);
        positions[1] = Doubles.toArray(y);

        return positions;

    }

    public static double[] setPlotLimits(Plot plot, CumStat csX, CumStat csY, String[] rangeModes, double[] ranges) {
        double xMin = rangeModes[0].equals(RangeModes.AUTOMATIC) ? csX.getMin() + csX.getMin() * 0.1 : ranges[0];
        double xMax = rangeModes[1].equals(RangeModes.AUTOMATIC) ? csX.getMax() + csX.getMax() * 0.1 : ranges[1];
        double yMin = rangeModes[2].equals(RangeModes.AUTOMATIC) ? csY.getMin() + csY.getMin() * 0.1 : ranges[2];
        double yMax = rangeModes[3].equals(RangeModes.AUTOMATIC) ? csY.getMax() + csY.getMax() * 0.1 : ranges[3];

        plot.setLimits(xMin, xMax, yMin, yMax);

        return new double[] { xMin, xMax, yMin, yMax };

    }

    public static int[] setPlotSize(Plot plot, double[] limits) {
        double xRange = limits[1] - limits[0];
        double yRange = limits[3] - limits[2];

        int xSize = 640;
        int ySize = 640;
        if (xRange > yRange)
            ySize = (int) Math.round(640 * yRange / xRange);
        else
            xSize = (int) Math.round(640 * xRange / yRange);

        plot.setSize(xSize,ySize);

        return new int[]{xSize,ySize};

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputTracksName = parameters.getValue(INPUT_TRACKS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        String plotMode = parameters.getValue(PLOT_MODE, workspace);
        double lineWidth = parameters.getValue(LINE_WIDTH, workspace);
        boolean addObjectID = parameters.getValue(ADD_OBJECT_ID, workspace);
        String objectIDColour = parameters.getValue(OBJECT_ID_COLOUR, workspace);

        String[] rangeModes = new String[4];
        rangeModes[0] = parameters.getValue(X_MIN_RANGE_MODE, workspace);
        rangeModes[1] = parameters.getValue(X_MAX_RANGE_MODE, workspace);
        rangeModes[2] = parameters.getValue(Y_MIN_RANGE_MODE, workspace);
        rangeModes[3] = parameters.getValue(Y_MAX_RANGE_MODE, workspace);

        double[] ranges = new double[4];
        ranges[0] = parameters.getValue(X_RANGE_MIN, workspace);
        ranges[1] = parameters.getValue(X_RANGE_MAX, workspace);
        ranges[2] = parameters.getValue(Y_RANGE_MIN, workspace);
        ranges[3] = parameters.getValue(Y_RANGE_MAX, workspace);

        ObjsI trackObjects = workspace.getObjects(inputTracksName);

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(trackObjects, workspace);

        ImageI outputImage;
        switch (plotMode) {
            case PlotModes.ALL_TOGETHER:
            default:
                outputImage = createPlotsAllTogether(trackObjects, inputObjectsName, outputImageName, lineWidth,
                        colours, rangeModes, ranges);
                break;
            case PlotModes.SEPARATE_PLOTS:
                outputImage = createPlotsSeparately(trackObjects, inputObjectsName, outputImageName, lineWidth,
                        colours, ranges, addObjectID, objectIDColour);
                break;
        }
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.showAsIs();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_TRACKS, this));
        parameters.add(new ChildObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(PLOT_SEPARATOR, this));
        parameters.add(new ChoiceP(PLOT_MODE, this, PlotModes.ALL_TOGETHER, PlotModes.ALL));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1));
        parameters.add(new BooleanP(ADD_OBJECT_ID, this, true));
        parameters.add(new ChoiceP(OBJECT_ID_COLOUR, this, Colours.BLACK, Colours.ALL));

        parameters.add(new SeparatorP(AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(X_MIN_RANGE_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(X_RANGE_MIN, this, -100));
        parameters.add(new ChoiceP(X_MAX_RANGE_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(X_RANGE_MAX, this, 100));
        parameters.add(new ChoiceP(Y_MIN_RANGE_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(Y_RANGE_MIN, this, -100));
        parameters.add(new ChoiceP(Y_MAX_RANGE_MODE, this, RangeModes.AUTOMATIC, RangeModes.ALL));
        parameters.add(new DoubleP(Y_RANGE_MAX, this, 100));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;

        Parameters returnedParameters = new Parameters();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(INPUT_TRACKS, workspace);

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACKS));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        ((ChildObjectsP) parameters.getParameter(INPUT_OBJECTS)).setParentObjectsName(trackObjectsName);
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(PLOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(PLOT_MODE));
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
        if (((String) parameters.getValue(PLOT_MODE, workspace)).equals(PlotModes.SEPARATE_PLOTS)) {
            returnedParameters.add(parameters.getParameter(ADD_OBJECT_ID));
            if ((boolean) parameters.getValue(ADD_OBJECT_ID, workspace))
                returnedParameters.add(parameters.getParameter(OBJECT_ID_COLOUR));
        }

        returnedParameters.addAll(super.updateAndGetParameters(trackObjectsName));

        returnedParameters.add(parameters.getParameter(AXIS_SEPARATOR));
        switch ((String) parameters.getValue(PLOT_MODE, workspace)) {
            case PlotModes.ALL_TOGETHER:
                returnedParameters.add(parameters.getParameter(X_MIN_RANGE_MODE));
                if (((String) parameters.getValue(X_MIN_RANGE_MODE, workspace)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(X_RANGE_MIN));

                returnedParameters.add(parameters.getParameter(X_MAX_RANGE_MODE));
                if (((String) parameters.getValue(X_MAX_RANGE_MODE, workspace)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(X_RANGE_MAX));

                returnedParameters.add(parameters.getParameter(Y_MIN_RANGE_MODE));
                if (((String) parameters.getValue(Y_MIN_RANGE_MODE, workspace)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(Y_RANGE_MIN));

                returnedParameters.add(parameters.getParameter(Y_MAX_RANGE_MODE));
                if (((String) parameters.getValue(Y_MAX_RANGE_MODE, workspace)).equals(RangeModes.MANUAL))
                    returnedParameters.add(parameters.getParameter(Y_RANGE_MAX));

                break;

            case PlotModes.SEPARATE_PLOTS:
                returnedParameters.add(parameters.getParameter(X_RANGE_MIN));
                returnedParameters.add(parameters.getParameter(X_RANGE_MAX));
                returnedParameters.add(parameters.getParameter(Y_RANGE_MIN));
                returnedParameters.add(parameters.getParameter(Y_RANGE_MAX));
                break;
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
}
