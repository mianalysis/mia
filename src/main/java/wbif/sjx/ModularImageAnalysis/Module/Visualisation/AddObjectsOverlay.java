// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.TextRoi;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.util.Random;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class AddObjectsOverlay extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_LABEL = "Show label";
    public static final String LABEL_SIZE = "Label size";
    public static final String USE_PARENT_ID = "Use parent ID";
    public static final String PARENT_OBJECT_FOR_ID = "Parent object for ID";
    public static final String POSITION_MODE = "Position mode";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String MEASUREMENT = "Measurement";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String SHOW_IMAGE = "Show image";

    public interface PositionModes {
        String ALL_POINTS = "All points";
        String CENTROID = "Centroid";
        String OUTLINE = "Outline";
        String POSITION_MEASUREMENTS = "Position measurements";

        String[] ALL = new String[]{ALL_POINTS, CENTROID, OUTLINE, POSITION_MEASUREMENTS};

    }

    public interface ColourModes {
        String SINGLE_COLOUR = "Single colour";
        String RANDOM_COLOUR = "Random colour";
        String MEASUREMENT_VALUE = "Measurement value";
        String PARENT_ID = "Parent ID";

        String[] ALL = new String[]{SINGLE_COLOUR, RANDOM_COLOUR, MEASUREMENT_VALUE, PARENT_ID};

    }

    public static void createOverlay(ImagePlus ipl, ObjCollection inputObjects, String measurement, String colourMode,
                                     String parentObjectsForColourName, String positionMode, String xPosMeas,
                                     String yPosMeas, String zPosMeas, boolean useParentID, boolean showID,
                                     int labelSize, String parentObjectsForIDName) {

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        }

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Getting minimum and maximum values from measurement (if required)
        CumStat cs = new CumStat();
        if (colourMode.equals(ColourModes.MEASUREMENT_VALUE)) {
            inputObjects.values().forEach(e -> cs.addMeasure(e.getMeasurement(measurement).getValue()));

        }

        // Running through each object, adding it to the overlay along with an ID label
        for (Obj object:inputObjects.values()) {
            // Default hue value in case none is assigned
            float H = 0.2f;

            switch (colourMode) {
                case ColourModes.RANDOM_COLOUR:
                    // Random colours
                    H = new Random().nextFloat();
                    break;

                case ColourModes.MEASUREMENT_VALUE:
                    double value = object.getMeasurement(measurement).getValue();
                    double startH = 0;
                    double endH = 120d / 255d;
                    H = (float) ((value - cs.getMin()) * (endH - startH) / (cs.getMax() - cs.getMin()) + startH);
                    break;

                case ColourModes.PARENT_ID:
                    if (object.getParent(parentObjectsForColourName)==null) {
                        H = 0.2f;
                    } else {
                        H = ((float) object.getParent(parentObjectsForColourName).getID() * 1048576 % 255) / 255;
                    }

                    break;

            }

            Color colour = Color.getHSBColor(H, 1, 1);

            double xMean = 0;
            double yMean = 0;
            double zMean;
            int z = 0;
            int t = 0;

            switch (positionMode) {
                case PositionModes.ALL_POINTS:
                    // Still need to get mean coords for label
                    xMean = object.getXMean(true);
                    yMean = object.getYMean(true);

                    // Adding each point
                    double[] xx = object.getX(false);
                    double[] yy = object.getY(false);
                    double[] zz = object.getZ(false,false);

                    t = object.getT()+1;

                    for (int i=0;i<xx.length;i++) {
                        PointRoi roi = new PointRoi(xx[i]+0.5,yy[i]+0.5);
                        roi.setPointType(PointRoi.NORMAL);

                        if (ipl.isHyperStack()) {
                            roi.setPosition(1, (int) zz[i], t);
                        } else {
                            int pos = Math.max(Math.max(1,(int) zz[i]),t);
                            roi.setPosition(pos);
                        }
                        roi.setStrokeColor(colour);
                        ovl.addElement(roi);
                    }

                    break;

                case PositionModes.CENTROID:
                    xMean = object.getXMean(true);
                    yMean = object.getYMean(true);
                    zMean = object.getZMean(true,false);

                    // Getting coordinates to plot
                    z = (int) Math.round(zMean+1);
                    t = object.getT()+1;

                    // Adding circles where the object centroids are
                    PointRoi roi = new PointRoi(xMean+0.5,yMean+0.5);
                    roi.setPointType(PointRoi.NORMAL);
                    if (ipl.isHyperStack()) {
                        roi.setPosition(1, z, t);
                    } else {
                        int pos = Math.max(Math.max(1,z),t);
                        roi.setPosition(pos);
                    }
                    roi.setStrokeColor(colour);
                    ovl.addElement(roi);

                    break;

                case PositionModes.OUTLINE:
                    // Still need to get mean coords for label
                    xMean = object.getXMean(true);
                    yMean = object.getYMean(true);

                    // Adding each point.  This requires the surface to be calculated in 2D first
                    object.calculateSurface2D();
                    double[] xSurf = object.getSurfaceX(true);
                    double[] ySurf = object.getSurfaceY(true);
                    double[] zSurf = object.getZ(false,false);

                    t = object.getT()+1;

                    for (int i=0;i<xSurf.length;i++) {
                        roi = new PointRoi(xSurf[i]+0.5,ySurf[i]+0.5);
                        roi.setPointType(PointRoi.NORMAL);

                        if (ipl.isHyperStack()) {
                            roi.setPosition(1, (int) zSurf[i], t);
                        } else {
                            int pos = Math.max(Math.max(1,(int) zSurf[i]),t);
                            roi.setPosition(pos);
                        }
                        roi.setStrokeColor(colour);
                        ovl.addElement(roi);
                    }

                    break;

                case PositionModes.POSITION_MEASUREMENTS:
                    xMean = object.getMeasurement(xPosMeas).getValue();
                    yMean = object.getMeasurement(yPosMeas).getValue();
                    zMean = object.getMeasurement(zPosMeas).getValue();

                    // Getting coordinates to plot
                    z = (int) Math.round(zMean+1);
                    t = object.getT()+1;

                    // Adding circles where the object centroids are
                    roi = new PointRoi(xMean+0.5,yMean+0.5);
                    roi.setPointType(PointRoi.NORMAL);
                    if (ipl.isHyperStack()) {
                        roi.setPosition(1, z, t);
                    } else {
                        int pos = Math.max(Math.max(1,z),t);
                        roi.setPosition(pos);
                    }
                    roi.setStrokeColor(colour);
                    ovl.addElement(roi);

                    break;

            }

            if (showID) {
                // Adding text label
                TextRoi text;
                if (useParentID) {
                    text = new TextRoi(xMean, yMean, String.valueOf(object.getParent(parentObjectsForIDName).getID()));
                } else {
                    text = new TextRoi(xMean, yMean, String.valueOf(object.getID()));
                }
                text.setCurrentFont(new Font(Font.SANS_SERIF,Font.PLAIN,labelSize));
                if (ipl.isHyperStack()) {
                    text.setPosition(1, z, t);
                } else {
                    text.setPosition(Math.max(Math.max(1, z), t));
                }
                text.setStrokeColor(colour);
                ovl.addElement(text);

            }
        }
    }

    @Override
    public String getTitle() {
        return "Add objects as overlay";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean showID = parameters.getValue(SHOW_LABEL);
        int labelSize = parameters.getValue(LABEL_SIZE);
        boolean useParentID = parameters.getValue(USE_PARENT_ID);
        String parentObjectsForIDName = parameters.getValue(PARENT_OBJECT_FOR_ID);
        String positionMode = parameters.getValue(POSITION_MODE);
        String colourMode = parameters.getValue(COLOUR_MODE);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurement = parameters.getValue(MEASUREMENT);
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT);
        boolean showImage = parameters.getValue(SHOW_IMAGE);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        createOverlay(ipl, inputObjects, measurement, colourMode,  parentObjectsForColourName, positionMode, xPosMeas,
                yPosMeas, zPosMeas, useParentID, showID, labelSize, parentObjectsForIDName);

        // If necessary, adding output image to workspace
        if (addOutputToWorkspace) {
            Image outputImage = new Image(outputImageName,ipl);
            workspace.addImage(outputImage);
        }

        // Duplicating the image, then displaying it.  Duplicating prevents the image being removed from the workspace
        // if it's closed
        if (showImage) new Duplicator().run(ipl).show();

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ADD_OUTPUT_TO_WORKSPACE, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(SHOW_LABEL, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(LABEL_SIZE, Parameter.INTEGER,8));
        parameters.add(new Parameter(USE_PARENT_ID, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_ID, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(POSITION_MODE, Parameter.CHOICE_ARRAY,PositionModes.CENTROID,PositionModes.ALL));
        parameters.add(new Parameter(X_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(Y_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(Z_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(COLOUR_MODE, Parameter.CHOICE_ARRAY,ColourModes.SINGLE_COLOUR,ColourModes.ALL));
        parameters.add(new Parameter(MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,true));

    }

    @Override
    protected void initialiseMeasurementReferences() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(ADD_OUTPUT_TO_WORKSPACE));

            if (parameters.getValue(ADD_OUTPUT_TO_WORKSPACE)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

            }
        }

        returnedParameters.add(parameters.getParameter(SHOW_LABEL));

        if (parameters.getValue(SHOW_LABEL)) {
            returnedParameters.add(parameters.getParameter(LABEL_SIZE));
            returnedParameters.add(parameters.getParameter(USE_PARENT_ID));

            if (parameters.getValue(USE_PARENT_ID)) {
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_ID));

                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT_FOR_ID,inputObjectsName);

            }
        }

        returnedParameters.add(parameters.getParameter(POSITION_MODE));
        if (parameters.getValue(POSITION_MODE).equals(PositionModes.POSITION_MEASUREMENTS)) {
            returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(X_POSITION_MEASUREMENT,inputObjectsName);
            parameters.updateValueSource(Y_POSITION_MEASUREMENT,inputObjectsName);
            parameters.updateValueSource(Z_POSITION_MEASUREMENT,inputObjectsName);

        }

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        if (parameters.getValue(COLOUR_MODE).equals(ColourModes.MEASUREMENT_VALUE)) {
            // Use measurement
            returnedParameters.add(parameters.getParameter(MEASUREMENT));

            if (parameters.getValue(INPUT_OBJECTS) != null) {
                parameters.updateValueSource(MEASUREMENT,parameters.getValue(INPUT_OBJECTS));

            }

        } else if (parameters.getValue(COLOUR_MODE).equals(ColourModes.PARENT_ID)) {
            // Use Parent ID
            returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);

        }

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
