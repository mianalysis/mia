package io.github.mianalysis.mia.module.visualise.plots;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.google.common.primitives.Doubles;

import ij.gui.Plot;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualise.overlays.AbstractOverlay;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.ObjectMetadataP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.PartnerObjectsP;
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
import io.github.mianalysis.mia.process.ColourFactory;

@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class PlotTrackMotility extends AbstractOverlay {
    public static final String INPUT_SEPARATOR = "Object input/image output";
    public static final String INPUT_TRACKS = "Input tracks";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String PLOT_SEPARATOR = "Plot controls";
    public static final String LINE_WIDTH = "Line width";

    // public static final String AXIS_SEPARATOR = "Axis controls";
    // public static final String X_RANGE_MODE = "X-range mode";
    // public static final String X_RANGE_MIN = "X-range min";
    // public static final String X_RANGE_MAX = "X-range max";
    // public static final String Y_RANGE_MODE = "Y-range mode";
    // public static final String Y_RANGE_MIN = "Y-range min";
    // public static final String Y_RANGE_MAX = "Y-range max";


    public PlotTrackMotility(Modules modules) {
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

    public static double[][] getPositionValues(Obj trackObject, String objectsName) {
        Objs childObjects = trackObject.getChildren(objectsName);
        
        // Find first object in track
        int minT = Integer.MAX_VALUE;
        Obj currObj = null;
        for (Obj childObject:childObjects.values()) {
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

        Objs nextObjects = currObj.getNextPartners(objectsName);
        while (nextObjects.size() > 0) {
            Obj nextObject = nextObjects.getFirst();
            
            x.add(nextObject.getXMean(true)-x0);            
            y.add(nextObject.getYMean(true)-y0);
            
            nextObjects = nextObject.getNextPartners(objectsName);

        }

        double[][] positions = new double[2][x.size()];
        positions[0] = Doubles.toArray(x);
        positions[1] = Doubles.toArray(y);

        return positions;

    }


    @Override
    protected Status process(Workspace workspace) {
        // Getting parameters
        String inputTracksName = parameters.getValue(INPUT_TRACKS, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        double lineWidth = parameters.getValue(LINE_WIDTH, workspace);

        Objs trackObjects = workspace.getObjects(inputTracksName);

        // Creating plot
        Plot plot = new Plot("Motility", "x-position (px)", "y-position (px)");

        // Generating colours for each object
        HashMap<Integer, Color> colours = getColours(trackObjects, workspace);

        // Iterating over each track, creating the plot
        int count = 0;        
        double extreme = 0;

        for (Obj trackObject : trackObjects.values()) {
            // Getting values to plot
            double[][] posDouble = getPositionValues(trackObject, inputObjectsName);

            for (int i=0;i<posDouble[0].length;i++) {
                if (Math.abs(posDouble[0][i]) > extreme)
                    extreme = Math.abs(posDouble[0][i]);
                if (Math.abs(posDouble[1][i]) > extreme)
                    extreme = Math.abs(posDouble[1][i]);
            }

            plot.setLineWidth((float) lineWidth);
            plot.setColor(colours.get(trackObject.getID()));
            plot.add("Line", posDouble[0], posDouble[1]);

            writeProgressStatus(count, trackObjects.size(), "tracks");

        }

        plot.setLimits(-extreme,extreme,-extreme,extreme);
        plot.setSize(640,640);

        Image outputImage = ImageFactory.createImage(outputImageName, plot.getImagePlus());
        workspace.addImage(outputImage);

        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_TRACKS, this));
        parameters.add(new ChildObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(PLOT_SEPARATOR, this));
        parameters.add(new DoubleP(LINE_WIDTH, this, 1.0));

        // parameters.add(new SeparatorP(AXIS_SEPARATOR, this));
        // parameters.add(new IntegerP(X_RANGE_MIN, this, 0));
        // parameters.add(new IntegerP(X_RANGE_MAX, this, 1));
        // parameters.add(new DoubleP(Y_RANGE_MIN, this, 0.0));
        // parameters.add(new DoubleP(Y_RANGE_MAX, this, 1.0));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        
        Parameters returnedParameters = new Parameters();
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String trackObjectsName = parameters.getValue(INPUT_TRACKS, workspace);

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_TRACKS));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        ((ChildObjectsP) parameters.getParameter(INPUT_OBJECTS)).setParentObjectsName(trackObjectsName);
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(PLOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

        returnedParameters.addAll(super.updateAndGetParameters(inputObjectsName));

        // returnedParameters.add(parameters.getParameter(AXIS_SEPARATOR));
        // returnedParameters.add(parameters.getParameter(X_RANGE_MODE));
        // switch ((String) parameters.getValue(X_RANGE_MODE, workspace)) {
        // case AxisRangeModes.FIXED:
        // returnedParameters.add(parameters.getParameter(X_RANGE_MIN));
        // returnedParameters.add(parameters.getParameter(X_RANGE_MAX));
        // break;
        // }

        // returnedParameters.add(parameters.getParameter(Y_RANGE_MODE));
        // switch ((String) parameters.getValue(Y_RANGE_MODE, workspace)) {
        // case AxisRangeModes.FIXED:
        // returnedParameters.add(parameters.getParameter(Y_RANGE_MIN));
        // returnedParameters.add(parameters.getParameter(Y_RANGE_MAX));
        // break;
        // }

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
