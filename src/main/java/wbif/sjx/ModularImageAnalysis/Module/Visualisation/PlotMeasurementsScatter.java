// TODO: Add NaN exclusion for 2D plot (will have to remove items from an array (may involve intermediate ArrayLists)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.gui.Plot;
import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by sc13967 on 19/05/2017.
 */
public class PlotMeasurementsScatter extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String EXCLUDE_NAN = "Exclude NaN measurements";
    public static final String MEASUREMENT1 = "First measurement (X)";
    public static final String MEASUREMENT2 = "Second measurement (Y)";
    public static final String INCLUDE_COLOUR = "Add third measurement as colour";
    public static final String MEASUREMENT3 = "Third measurement (Colour)";
    public static final String COLOURMAP = "Colourmap";

    public interface ColourMaps {
        String RED_TO_BLUE = "Red to blue";
        String RED_TO_GREEN = "Blue to green";

        String[] ALL = new String[]{"Red to blue", "Red to green"};

    }

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
    public String getPackageName() {
        return PackageNames.VISUALISATION;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

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

        writeMessage("Getting measurements to plot");
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
            writeMessage("Plotting "+measurement1+", " + measurement2+" and "+measurement3);

            String title = "Scatter plot of " + measurement1 + ", " + measurement2+" and "+measurement3;
            Plot plot = new Plot(title, measurement1, measurement2);

            Color[] colors = null;
            if (colourmap.equals(ColourMaps.RED_TO_BLUE)) { // Red to blue
                colors = createColourGradient(0,240d/255d,measurementValues3);

            } else if (colourmap.equals(ColourMaps.RED_TO_GREEN)) { // Red to green
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
            writeMessage("Plotting "+measurement1+" and "+measurement2);

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

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(EXCLUDE_NAN, this,true));
        parameters.add(new ObjectMeasurementP(MEASUREMENT1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT2, this));
        parameters.add(new BooleanP(INCLUDE_COLOUR, this,false));
        parameters.add(new ObjectMeasurementP(MEASUREMENT3, this));
        parameters.add(new ChoiceP(COLOURMAP, this,ColourMaps.RED_TO_BLUE,ColourMaps.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(EXCLUDE_NAN));
        returnedParameters.add(parameters.getParameter(MEASUREMENT1));
        returnedParameters.add(parameters.getParameter(MEASUREMENT2));

        // Updating measurements with measurement choices from currently-selected object
        String objectName = parameters.getValue(INPUT_OBJECTS);
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT1)).setObjectName(objectName);
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT2)).setObjectName(objectName);

        returnedParameters.add(parameters.getParameter(INCLUDE_COLOUR));
        if (parameters.getValue(INCLUDE_COLOUR)) {
            returnedParameters.add(parameters.getParameter(MEASUREMENT3));
            returnedParameters.add(parameters.getParameter(COLOURMAP));
            ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT3)).setObjectName(objectName);
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

    @Override
    public ArrayList<MacroOperation> getMacroOperations(MacroExtension handler) {
        return null;
    }
}
