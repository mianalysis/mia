package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.TextRoi;
import ij.plugin.HyperStackConverter;
import ij.process.StackConverter;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.util.Random;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class ShowObjectsOverlay extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String USE_GROUP_ID = "Use group ID";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String MEASUREMENT = "Measurement";

    private static final String SINGLE_COLOUR = "Single colour";
    private static final String RANDOM_COLOUR = "Random colour";
    private static final String MEASUREMENT_VALUE = "Measurement value";
    private static final String PARENT_ID = "Parent ID";
    private static final String[] COLOUR_MODES = new String[]{SINGLE_COLOUR,RANDOM_COLOUR,MEASUREMENT_VALUE,PARENT_ID};

    @Override
    public String getTitle() {
        return "Show objects as overlay";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting parameters
        boolean useGroupID = parameters.getValue(USE_GROUP_ID);
        String colourMode = parameters.getValue(COLOUR_MODE);

        // Getting input objects
        HCName inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        HCObjectSet inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        HCName inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        ImagePlus hyperstack = HyperStackConverter.toHyperStack(ipl,ipl.getNChannels(),ipl.getNSlices(),ipl.getNFrames());
        if (!hyperstack.isHyperStack()) new StackConverter(hyperstack).convertToRGB();

        hyperstack.setOverlay(new Overlay());

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat(1);
        if (colourMode.equals(COLOUR_MODES[2])) {
            String measurement = parameters.getValue(MEASUREMENT);
            inputObjects.values().forEach(e -> cs.addSingleMeasure(0,e.getMeasurement(measurement).getValue()));

        }

        // Running through each object, adding it to the overlay along with an ID label
        for (HCObject object:inputObjects.values()) {

            float H = 0.2f;
            Color colour = Color.ORANGE;
            switch (colourMode) {
                case RANDOM_COLOUR:
                    // Random colours
                    H = new Random(object.getGroupID()*object.getGroupID()*object.getGroupID()).nextFloat();
                    break;

                case MEASUREMENT_VALUE:
                    String measurement = parameters.getValue(MEASUREMENT);

                    double value = object.getMeasurement(measurement).getValue();
                    double startH = 0;
                    double endH = 120d / 255d;
                    H = (float) ((value - cs.getMin()[0]) * (endH - startH) / (cs.getMax()[0] - cs.getMin()[0]) + startH);
                    break;

                case PARENT_ID:
                    if (object.getParent() != null) {
                        H = ((float) object.getParent().getID() * 1048576 % 255) / 255;
                    } else {
                        H = 0.2f;
                    }
                    break;

            }

            colour = Color.getHSBColor(H, 1, 1);

            double xMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(HCObject.X),MeasureObjectCentroid.MEAN);
            double yMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(HCObject.Y),MeasureObjectCentroid.MEAN);
            double zMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(HCObject.Z),MeasureObjectCentroid.MEAN);

            // Getting coordinates to plot
            int c = ((int) object.getCoordinates(HCObject.C)) + 1;
            int z = (int) Math.round(zMean+1);
            int t = ((int) object.getCoordinates(HCObject.T)) + 1;

            // Adding circles where the object centroids are
            OvalRoi roi = new OvalRoi(xMean-2,yMean-2,4,4);
            if (hyperstack.isHyperStack()) {
                roi.setPosition(c, z, t);
            } else {
                roi.setPosition(Math.max(Math.max(c,z),t));
            }
            roi.setStrokeColor(colour);
            hyperstack.getOverlay().add(roi);

            // Adding text label
            TextRoi text;
            if (useGroupID) {
                text = new TextRoi(xMean, yMean, String.valueOf(object.getGroupID()));
            } else {
                text = new TextRoi(xMean, yMean, String.valueOf(object.getID()));
            }
            if (hyperstack.isHyperStack()) {
                text.setPosition(c, z, t);
            } else {
                text.setPosition(Math.max(Math.max(c,z),t));
            }
            text.setStrokeColor(colour);
            hyperstack.getOverlay().add(text);

        }

        hyperstack.show();

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(INPUT_OBJECTS,HCParameter.INPUT_OBJECTS,null));
        parameters.addParameter(new HCParameter(USE_GROUP_ID,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(COLOUR_MODE,HCParameter.CHOICE_ARRAY,COLOUR_MODES[0],COLOUR_MODES));
        parameters.addParameter(new HCParameter(MEASUREMENT,HCParameter.MEASUREMENT,null,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.addParameter(parameters.getParameter(USE_GROUP_ID));
        returnedParameters.addParameter(parameters.getParameter(COLOUR_MODE));

        if (parameters.getValue(COLOUR_MODE).equals(COLOUR_MODES[2])) {
            // Use measurement
            returnedParameters.addParameter(parameters.getParameter(MEASUREMENT));

            if (parameters.getValue(INPUT_OBJECTS) != null) {
                parameters.updateValueRange(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

            }
        }

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
