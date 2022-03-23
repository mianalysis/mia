// TODO: Add NaN exclusion for 2D plot (will have to remove items from an array (may involve intermediate ArrayLists)

package io.github.mianalysis.mia.module.visualise;

import java.awt.Color;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.gui.Plot;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.sjcross.common.mathfunc.CumStat;

/**
 * Created by sc13967 on 19/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class PlotMeasurementsScatter extends Module {
    public static final String INPUT_SEPARATOR = "Object input/image output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String PLOTTING_SEPARATOR = "Plotting controls";
    public static final String MEASUREMENT1 = "First measurement (X)";
    public static final String MEASUREMENT2 = "Second measurement (Y)";
    public static final String INCLUDE_COLOUR = "Add third measurement as colour";
    public static final String MEASUREMENT3 = "Third measurement (Colour)";
    public static final String COLOURMAP = "Colourmap";

    public static final String MISC_SEPARATOR = "Miscellaneous controls";
    public static final String SHOW_AS_INTERACTIVE_PLOT = "Show as interactive plot";

    public PlotMeasurementsScatter(Modules modules) {
        super("Plot measurements as scatter", modules);
    }

    public interface ColourMaps {
        String RED_TO_BLUE = "Red to blue";
        String RED_TO_GREEN = "Blue to green";

        String[] ALL = new String[] { "Red to blue", "Red to green" };

    }

    private Color[] createColourGradient(double startH, double endH, double[] values) {
        // Getting colour range
        CumStat cs = new CumStat();
        for (double value : values)
            cs.addMeasure(value);
        double min = cs.getMin();
        double max = cs.getMax();

        Color[] colours = new Color[values.length];
        for (int i = 0; i < colours.length; i++) {
            double H = (values[i] - min) * (endH - startH) / (max - min) + startH;
            colours[i] = Color.getHSBColor((float) H, 1, 1);

        }

        return colours;

    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public String getDescription() {
        return "Creates an ImageJ scatter plot of two measurements associated with specified objects.  A third measurement can be encoded as point colour.  The output plot can either be displayed immediately in an interactive ImageJ plotting window or stored as an image to the MIA workspace (allowing it to subsequently be saved to file).";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useColour = parameters.getValue(INCLUDE_COLOUR);
        String measurement1 = parameters.getValue(MEASUREMENT1);
        String measurement2 = parameters.getValue(MEASUREMENT2);
        String measurement3 = parameters.getValue(MEASUREMENT3);
        String colourmap = parameters.getValue(COLOURMAP);
        boolean showInteractive = parameters.getValue(SHOW_AS_INTERACTIVE_PLOT);

        // Getting measurement values
        double[] measurementValues1 = new double[inputObjects.size()];
        double[] measurementValues2 = new double[inputObjects.size()];
        double[] measurementValues3 = null;
        if (useColour)
            measurementValues3 = new double[inputObjects.size()];

        CumStat[] cs = new CumStat[2];
        cs[0] = new CumStat();
        cs[1] = new CumStat();

        int iter = 0;
        for (Obj inputObject : inputObjects.values()) {
            measurementValues1[iter] = inputObject.getMeasurement(measurement1).getValue();
            measurementValues2[iter] = inputObject.getMeasurement(measurement2).getValue();
            if (useColour)
                measurementValues3[iter] = inputObject.getMeasurement(measurement3).getValue();

            // Adding the current measurements to MultiCumStat, so the min and max can be
            // obtained
            cs[0].addMeasure(measurementValues1[iter]);
            cs[1].addMeasure(measurementValues2[iter++]);

        }

        // Creating the scatter plot
        Plot plot;
        if (useColour) {
            String title = "Scatter plot of " + measurement1 + ", " + measurement2 + " and " + measurement3;
            plot = new Plot(title, measurement1, measurement2);

            Color[] colors = null;
            if (colourmap.equals(ColourMaps.RED_TO_BLUE)) // Red to blue
                colors = createColourGradient(0, 240d / 255d, measurementValues3);
            else if (colourmap.equals(ColourMaps.RED_TO_GREEN)) // Red to green
                colors = createColourGradient(0, 120d / 255d, measurementValues3);

            for (int i = 0; i < measurementValues1.length; i++) {
                // Adding the current point (with its assigned colour)
                plot.setColor(colors[i], colors[i]);

                if (measurementValues1[i] != Double.NaN & measurementValues2[i] != Double.NaN
                        & measurementValues3[i] != Double.NaN)
                    plot.addPoints(new double[] { measurementValues1[i] }, new double[] { measurementValues2[i] },
                            Plot.CIRCLE);

            }
        } else {
            String title = "Scatter plot of " + measurement1 + " and " + measurement2;
            plot = new Plot(title, measurement1, measurement2);
            plot.addPoints(measurementValues1, measurementValues2, 0);
        }

        plot.setLimits(cs[0].getMin(), cs[0].getMax(), cs[1].getMin(), cs[1].getMax());

        Image outputImage = new Image(outputImageName, plot.getImagePlus());
        workspace.addImage(outputImage);

        if (showOutput)
            if (showInteractive)
                plot.show();
            else
                outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(PLOTTING_SEPARATOR, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT2, this));
        parameters.add(new BooleanP(INCLUDE_COLOUR, this, false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT3, this));
        parameters.add(new ChoiceP(COLOURMAP, this, ColourMaps.RED_TO_BLUE, ColourMaps.ALL));

        parameters.add(new SeparatorP(MISC_SEPARATOR, this));
        parameters.add(new BooleanP(SHOW_AS_INTERACTIVE_PLOT, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(PLOTTING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASUREMENT1));
        returnedParameters.add(parameters.getParameter(MEASUREMENT2));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT1)).setObjectName(objectName);
        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT2)).setObjectName(objectName);

        returnedParameters.add(parameters.getParameter(INCLUDE_COLOUR));
        if ((boolean) parameters.getValue(INCLUDE_COLOUR)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT3));
            returnedParameters.add(parameters.getParameter(COLOURMAP));
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT3)).setObjectName(objectName);
        }

        returnedParameters.add(parameters.getParameter(MISC_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SHOW_AS_INTERACTIVE_PLOT));

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
        parameters.get(INPUT_OBJECTS)
                .setDescription("Input object collection for which object-associated measurements will be plotted.");

        parameters.get(OUTPUT_IMAGE)
                .setDescription("Output plot image which will be saved to the workspace with this name.");

        parameters.get(MEASUREMENT1).setDescription(
                "Measurement associated with the input objects which will be plotted along the x-axis.");

        parameters.get(MEASUREMENT2).setDescription(
                "Measurement associated with the input objects which will be plotted along the y-axis.");

        parameters.get(INCLUDE_COLOUR).setDescription(
                "When selected, a third measurement can be represented as the plot marker colour.  This colour will vary according to the colourmap set with the \""
                        + COLOURMAP + "\" parameter");

        parameters.get(MEASUREMENT3).setDescription("If \"" + INCLUDE_COLOUR
                + "\" is selected, this measurement associated with the input objects will determine the plot marker colour.");

        parameters.get(COLOURMAP).setDescription("If \"" + INCLUDE_COLOUR
                + "\" is selected, this is the colourmap that will control how marker colours vary in response to the magnitude of their values.");

        parameters.get(SHOW_AS_INTERACTIVE_PLOT).setDescription(
                "When selected, and if displaying module output in realtime (\"Show output\" button selected), the plot will be displayed as an interactive ImageJ plot (editable rendering).  Otherwise, the standard image output will be displayed (i.e. the same image added to the workspace).");

    }
}
