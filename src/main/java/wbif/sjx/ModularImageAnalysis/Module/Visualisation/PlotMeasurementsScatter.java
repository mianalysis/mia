// TODO: Add NaN exclusion for 2D plot (will have to remove items from an array (may involve intermediate ArrayLists)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.gui.Plot;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class PlotMeasurementsScatter extends HCModule {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String EXCLUDE_NAN = "Exclude NaN measurements";
    public static final String MEASUREMENT1 = "First measurement (X)";
    public static final String MEASUREMENT2 = "Second measurement (Y)";
    public static final String INCLUDE_COLOUR = "Add third measurement as colour";
    public static final String MEASUREMENT3 = "Third measurement (Colour)";
    public static final String COLOURMAP = "Colourmap";

    private static final String[] COLOURMAPS = new String[]{"Red to blue","Red to green"};

    private Color[] createColourGradient(double startH, double endH, double[] values) {
        // Getting colour range
        CumStat cs = new CumStat();
        for (double value:values) cs.addMeasure(value);
        double min = cs.getMin();
        double max = cs.getMax();

        Color[] colours = new Color[values.length];
        for (int i=0;i<colours.length;i++) {
            double H = (values[i]-min)*(endH-startH)/(max-min) + startH;
            colours[i] = Color.getHSBColor((float) H,1,1);

        }

        return colours;

    }

    @Override
    public String getTitle() {
        return "Plot measurements as scatter";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting parameters
        boolean excludeNaN = parameters.getValue(EXCLUDE_NAN);
        boolean useColour = parameters.getValue(INCLUDE_COLOUR);
        String colourmap = null;
        if (useColour) colourmap = parameters.getValue(COLOURMAP);

        // Getting measurement names
        String measurement1 = parameters.getValue(MEASUREMENT1);
        String measurement2 = parameters.getValue(MEASUREMENT2);
        String measurement3 = null;
        if (useColour) measurement3 = parameters.getValue(MEASUREMENT3);

        // Getting measurement values
        double[] measurementValues1 = new double[inputObjects.size()];
        double[] measurementValues2 = new double[inputObjects.size()];
        double[] measurementValues3 = null;
        if (useColour) measurementValues3 = new double[inputObjects.size()];

        CumStat[] cs = new CumStat[2];
        cs[0] = new CumStat();
        cs[1] = new CumStat();

        if (verbose) System.out.println("["+moduleName+"] Getting measurements to plot");
        int iter = 0;
        for (Obj inputObject:inputObjects.values()) {
            measurementValues1[iter] = inputObject.getMeasurement(measurement1).getValue();
            measurementValues2[iter] = inputObject.getMeasurement(measurement2).getValue();
            if (useColour) measurementValues3[iter] = inputObject.getMeasurement(measurement3).getValue();

            // Adding the current measurements to MultiCumStat, so the min and max can be obtained
            cs[0].addMeasure(measurementValues1[iter]);
            cs[1].addMeasure(measurementValues2[iter]);

            iter++;

        }

        // Creating the scatter plot
        if (useColour) {
            if (verbose) System.out.println("["+moduleName+"] Plotting "+measurement1+", " + measurement2+" and "+measurement3);

            String title = "Scatter plot of " + measurement1 + ", " + measurement2+" and "+measurement3;
            Plot plot = new Plot(title, measurement1, measurement2);

            Color[] colors = null;
            if (colourmap.equals(COLOURMAPS[0])) { // Red to blue
                colors = createColourGradient(0,240d/255d,measurementValues3);

            } else if (colourmap.equals(COLOURMAPS[1])) { // Red to green
                colors = createColourGradient(0,120d/255d,measurementValues3);

            }

            for (int i=0;i<measurementValues1.length;i++) {
                // Adding the current point (with its assigned colour)
                plot.setColor(colors[i],colors[i]);

                if (excludeNaN) {
                    if (measurementValues1[i] != Double.NaN & measurementValues2[i] != Double.NaN & measurementValues3[i] != Double.NaN)
                    plot.addPoints(new double[]{measurementValues1[i]}, new double[]{measurementValues2[i]}, Plot.CIRCLE);

                } else {
                    plot.addPoints(new double[]{measurementValues1[i]}, new double[]{measurementValues2[i]}, Plot.CIRCLE);

                }
            }

            // Setting plot limits
            plot.setLimits(cs[0].getMin(), cs[0].getMax(), cs[1].getMin(), cs[1].getMax());

            // Displaying the plot
            plot.show();

        } else {
            if (verbose) System.out.println("["+moduleName+"] Plotting "+measurement1+" and "+measurement2);

            // Creating the plot
            String title = "Scatter plot of " + measurement1 + " and " + measurement2;
            Plot plot = new Plot(title, measurement1, measurement2);

            // Adding points to plot
            plot.addPoints(measurementValues1,measurementValues2,0);

            // Setting plot limits
            plot.setLimits(cs[0].getMin(), cs[0].getMax(), cs[1].getMin(), cs[1].getMax());

            // Displaying the plot
            plot.show();

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(EXCLUDE_NAN, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(MEASUREMENT1, Parameter.MEASUREMENT,null,null));
        parameters.addParameter(new Parameter(MEASUREMENT2, Parameter.MEASUREMENT,null,null));
        parameters.addParameter(new Parameter(INCLUDE_COLOUR, Parameter.BOOLEAN,false,null));
        parameters.addParameter(new Parameter(MEASUREMENT3, Parameter.MEASUREMENT,null,null));
        parameters.addParameter(new Parameter(COLOURMAP, Parameter.CHOICE_ARRAY,COLOURMAPS[0],COLOURMAPS));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(EXCLUDE_NAN));
        returnedParameters.addParameter(parameters.getParameter(MEASUREMENT1));
        returnedParameters.addParameter(parameters.getParameter(MEASUREMENT2));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(INPUT_OBJECTS);
        if (objectName != null) {
            parameters.updateValueSource(MEASUREMENT1, objectName);
            parameters.updateValueSource(MEASUREMENT2, objectName);

        } else {
            parameters.updateValueSource(MEASUREMENT1, null);
            parameters.updateValueSource(MEASUREMENT2, null);

        }

        returnedParameters.addParameter(parameters.getParameter(INCLUDE_COLOUR));
        if (parameters.getValue(INCLUDE_COLOUR)) {
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT3));
            returnedParameters.addParameter(parameters.getParameter(COLOURMAP));

            if (objectName != null) {
                parameters.updateValueSource(MEASUREMENT3, objectName);

            } else {
                parameters.updateValueSource(MEASUREMENT3, null);

            }

        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
