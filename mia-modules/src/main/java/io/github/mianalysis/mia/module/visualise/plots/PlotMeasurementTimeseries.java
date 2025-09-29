package io.github.mianalysis.mia.module.visualise.plots;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;
import com.google.common.primitives.Doubles;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChildObjectsP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class PlotMeasurementTimeseries extends Module {
    public static final String INPUT_SEPARATOR = "Object input/ImageI output";
    public static final String INPUT_TRACKS = "Input tracks";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String PLOT_SEPARATOR = "Plot controls";
    public static final String MEASUREMENT = "Measurement";
    public static final String PLOT_COLOUR = "Plot colour";
    public static final String PLOT_TYPE = "Plot type";
    public static final String LINE_WIDTH = "Line width";
    public static final String ADD_PLOT = "Add plot";

    public static final String AXIS_SEPARATOR = "Axis controls";
    public static final String X_RANGE_MODE = "X-range mode";
    public static final String X_RANGE_MIN = "X-range min";
    public static final String X_RANGE_MAX = "X-range max";
    public static final String Y_RANGE_MODE = "Y-range mode";
    public static final String Y_RANGE_MIN = "Y-range min";
    public static final String Y_RANGE_MAX = "Y-range max";
    public static final String Y_LABEL = "Y axis label";
    public static final String ADD_OBJECT_ID = "Add object ID";
    public static final String OBJECT_ID_COLOUR = "Object ID colour";

    public interface XRangeModes {
        String ALL_FRAMES = "All frames";
        String FIXED = "Fixed";
        String PER_OBJECT = "Per object";

        String[] ALL = new String[] { ALL_FRAMES, FIXED, PER_OBJECT };

    }

    public interface YRangeModes {
        String AUTO = "Automatic";
        String FIXED = "Fixed";

        String[] ALL = new String[] { AUTO, FIXED };

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

    public interface PlotTypes {
        String BAR = "Bar";
        String BOX = "Box";
        String CIRCLE = "Circle";
        String CONNECTED_CIRCLE = "Connected circle";
        String CROSS = "Cross";
        String DIAMOND = "Diamond";
        String DOT = "Dot";
        String FILLED = "Filled";
        String LINE = "Line";
        String SEPARATED_BAR = "Separated bar";
        String TRIANGLE = "Triangle";
        String X = "X";

        String[] ALL = new String[] { BAR, BOX, CIRCLE, CONNECTED_CIRCLE, CROSS, DIAMOND, DOT, FILLED, LINE,
                SEPARATED_BAR, TRIANGLE, X };

    }

    public PlotMeasurementTimeseries(Modules modules) {
        super("Plot measurement timeseries", modules);
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

    public static int[] getXValues(Objs trackObjects, Obj trackObject, String objectsName, String xRangeMode,
            @Nullable int xRangeMin, @Nullable int xRangeMax) {
        switch (xRangeMode) {
            case XRangeModes.ALL_FRAMES:
            default:
                return IntStream.rangeClosed(0, trackObjects.getNFrames() - 1).toArray();

            case XRangeModes.FIXED:
                return IntStream.rangeClosed(xRangeMin, xRangeMax).toArray();

            case XRangeModes.PER_OBJECT:
                if (trackObject.getChildren(objectsName) == null || trackObject.getChildren(objectsName).size() == 0)
                    return IntStream.rangeClosed(0, trackObjects.getNFrames() - 1).toArray();

                int[] temporalLimits = trackObject.getChildren(objectsName).getTemporalLimits();
                return IntStream.rangeClosed(temporalLimits[0], temporalLimits[1]).toArray();

        }
    }

    public static double[] getYValues(Obj trackObject, String objectsName, String measurementName, int[] xValues) {
        TreeMap<Integer, Double> yValues = new TreeMap<>();
        for (int xValue : xValues)
            yValues.put(xValue, Double.NaN);

        for (Obj inputObject : trackObject.getChildren(objectsName).values()) {
            Measurement measurement = inputObject.getMeasurement(measurementName);
            double value = measurement == null ? Double.NaN : measurement.getValue();

            yValues.put(inputObject.getT(), value);

        }

        return Doubles.toArray(yValues.values());

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting parameters
        String inputTracksName = parameters.getValue(INPUT_TRACKS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        LinkedHashMap<Integer, Parameters> measurementCollection = parameters.getValue(ADD_PLOT, workspace);
        boolean addObjectID = parameters.getValue(ADD_OBJECT_ID, workspace);
        String objectIDColour = parameters.getValue(OBJECT_ID_COLOUR, workspace);
        String xRangeMode = parameters.getValue(X_RANGE_MODE, workspace);
        int xRangeMin = parameters.getValue(X_RANGE_MIN, workspace);
        int xRangeMax = parameters.getValue(X_RANGE_MAX, workspace);
        String yRangeMode = parameters.getValue(Y_RANGE_MODE, workspace);
        double yRangeMin = parameters.getValue(Y_RANGE_MIN, workspace);
        double yRangeMax = parameters.getValue(Y_RANGE_MAX, workspace);
        String yLabel = parameters.getValue(Y_LABEL, workspace);

        ImagePlus ipl = null;
                
        // Iterating over each track, creating the plot
        int count = 0;
        Objs trackObjects = workspace.getObjects(inputTracksName);
        for (Obj trackObject : trackObjects.values()) {
            // Initialising plot
            Plot plot = new Plot(inputTracksName + " (ID " + trackObject.getID() + ")", "Frame", yLabel);

            // Getting values to plot
            int[] xValues = getXValues(trackObjects, trackObject, inputObjectsName, xRangeMode, xRangeMin, xRangeMax);

            double[] xValuesDouble = Arrays.stream(xValues).asDoubleStream().toArray();

            for (Parameters currParameters : measurementCollection.values()) {
                String measurementName = currParameters.getValue(MEASUREMENT, workspace);
                String plotColour = currParameters.getValue(PLOT_COLOUR, workspace);
                String plotType = currParameters.getValue(PLOT_TYPE, workspace);
                double lineWidth = currParameters.getValue(LINE_WIDTH, workspace);

                double[] yValues = getYValues(trackObject, inputObjectsName, measurementName, xValues);
                plot.setColor(plotColour);
                plot.setLineWidth((float) lineWidth);
                plot.add(plotType, xValuesDouble, yValues);

            }

            if (addObjectID) {
                Dimension dimension = plot.getSize();
                plot.setColor(objectIDColour);
                plot.addLabel(((double) 10)/((double) dimension.getWidth()), ((double) 25)/((double) dimension.getHeight()), inputTracksName + " (ID " + trackObject.getID() + ") ");
            }

            switch (yRangeMode) {
                case YRangeModes.AUTO:
                    plot.setLimitsToFit(true);
                    break;
                case YRangeModes.FIXED:
                    plot.setLimits(xValues[0], xValues[xValues.length - 1], yRangeMin, yRangeMax);
                    break;
            }

            ImagePlus currIpl = plot.getImagePlus();
            if (ipl == null)
                if (trackObjects.size() == 1)
                    ipl = currIpl;
                else
                    ipl = IJ.createImage("Plots", currIpl.getWidth(), currIpl.getHeight(), trackObjects.size(), 24);

            ipl.getStack().setProcessor(currIpl.getProcessor(), count++ + 1);

            writeProgressStatus(count, trackObjects.size(), "tracks");

        }

        // If no plot were created, create an empty image, so subsequent modules don't fail
        if (ipl == null)
            ipl = new Plot(inputTracksName + " (ID 0)", "Frame", yLabel).getImagePlus();
        
        ImageI outputImage = ImageFactory.createImage(outputImageName, ipl);
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_TRACKS, this));
        parameters.add(new ChildObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(PLOT_SEPARATOR, this));
        Parameters collection = new Parameters();
        collection.add(new ObjectMeasurementP(MEASUREMENT, this));
        collection.add(new ChoiceP(PLOT_COLOUR, this, Colours.BLUE, Colours.ALL));
        collection.add(new ChoiceP(PLOT_TYPE, this, PlotTypes.LINE, PlotTypes.ALL));
        collection.add(new DoubleP(LINE_WIDTH, this, 1.0));
        parameters.add(new ParameterGroup(ADD_PLOT, this, collection, 1));

        parameters.add(new SeparatorP(AXIS_SEPARATOR, this));
        parameters.add(new ChoiceP(X_RANGE_MODE, this, XRangeModes.ALL_FRAMES, XRangeModes.ALL));
        parameters.add(new IntegerP(X_RANGE_MIN, this, 0));
        parameters.add(new IntegerP(X_RANGE_MAX, this, 1));
        parameters.add(new ChoiceP(Y_RANGE_MODE, this, YRangeModes.AUTO, YRangeModes.ALL));
        parameters.add(new DoubleP(Y_RANGE_MIN, this, 0.0));
        parameters.add(new DoubleP(Y_RANGE_MAX, this, 1.0));
        parameters.add(new StringP(Y_LABEL, this));
        parameters.add(new BooleanP(ADD_OBJECT_ID, this, true));
        parameters.add(new ChoiceP(OBJECT_ID_COLOUR, this, Colours.BLACK, Colours.ALL));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        Parameters returnedParameters = new Parameters();
        String trackObjectsName = parameters.getValue(INPUT_TRACKS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACKS));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        ((ChildObjectsP) parameters.getParameter(INPUT_OBJECTS)).setParentObjectsName(trackObjectsName);
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(PLOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ADD_PLOT));
        ParameterGroup group = parameters.getParameter(ADD_PLOT);
        for (Parameters collection : group.getCollections(true).values())
            ((ObjectMeasurementP) collection.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);

        returnedParameters.add(parameters.getParameter(AXIS_SEPARATOR));
        returnedParameters.add(parameters.getParameter(X_RANGE_MODE));
        switch ((String) parameters.getValue(X_RANGE_MODE, workspace)) {
            case XRangeModes.FIXED:
                returnedParameters.add(parameters.getParameter(X_RANGE_MIN));
                returnedParameters.add(parameters.getParameter(X_RANGE_MAX));
                break;
        }

        returnedParameters.add(parameters.getParameter(Y_RANGE_MODE));
        switch ((String) parameters.getValue(Y_RANGE_MODE, workspace)) {
            case XRangeModes.FIXED:
                returnedParameters.add(parameters.getParameter(Y_RANGE_MIN));
                returnedParameters.add(parameters.getParameter(Y_RANGE_MAX));
                break;
        }

        returnedParameters.add(parameters.getParameter(Y_LABEL));
        returnedParameters.add(parameters.getParameter(ADD_OBJECT_ID));
        if ((boolean) parameters.getValue(ADD_OBJECT_ID, workspace))
            returnedParameters.add(parameters.getParameter(OBJECT_ID_COLOUR));

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
