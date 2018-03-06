// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.ImagePlus;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.*;

import java.awt.*;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class AddObjectsOverlay extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_LABEL = "Show label";
    public static final String LABEL_MODE = "Label mode";
    public static final String DECIMAL_PLACES = "Decimal places";
    public static final String USE_SCIENTIFIC = "Use scientific notation";
    public static final String LABEL_SIZE = "Label size";
    public static final String PARENT_OBJECT_FOR_ID = "Parent object for ID";
    public static final String MEASUREMENT_FOR_ID = "Measurement for ID";
    public static final String POSITION_MODE = "Position mode";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String USE_RADIUS = "Use radius measurement";
    public static final String MEASUREMENT_FOR_RADIUS = "Measuremen for radius";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String TRACK_OBJECTS = "Track objects";
    public static final String LINE_WIDTH = "Line width";
    public static final String SHOW_IMAGE = "Show image";

    public interface ColourModes extends ObjCollection.ColourModes {}

    public interface LabelModes extends ObjCollection.LabelModes {}

    public interface PositionModes {
        String ALL_POINTS = "All points";
        String CENTROID = "Centroid";
        String OUTLINE = "Outline";
        String POSITION_MEASUREMENTS = "Position measurements";
        String TRACKS = "Tracks";

        String[] ALL = new String[]{ALL_POINTS, CENTROID, OUTLINE, POSITION_MEASUREMENTS, TRACKS};

    }

    public static void createOverlay(ImagePlus ipl, ObjCollection inputObjects, String positionMode,
                                     String[] posMeasurements,HashMap<Integer,Float> colours, HashMap<Integer,String> IDs,
                                     int labelSize, double lineWidth) {

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        }

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Running through each object, adding it to the overlay along with an ID label
        for (Obj object:inputObjects.values()) {
            Color colour = Color.getHSBColor(colours.get(object.getID()),1,1);

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
                    double[] xx = object.getX(true);
                    double[] yy = object.getY(true);
                    double[] zz = object.getZ(true,false);

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
                        roi.setStrokeWidth(lineWidth);
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
                    PointRoi pointRoi = new PointRoi(xMean+0.5,yMean+0.5);
                    pointRoi.setPointType(PointRoi.NORMAL);
                    if (ipl.isHyperStack()) {
                        pointRoi.setPosition(1, z, t);
                    } else {
                        int pos = Math.max(Math.max(1,z),t);
                        pointRoi.setPosition(pos);
                    }
                    pointRoi.setStrokeColor(colour);
                    pointRoi.setStrokeWidth(lineWidth);
                    ovl.addElement(pointRoi);

                    break;

                case PositionModes.OUTLINE:
                    // Still need to get mean coords for label
                    xMean = object.getXMean(true);
                    yMean = object.getYMean(true);
                    zMean = object.getZMean(true,false);

                    // Getting coordinates to plot
                    z = (int) Math.round(zMean+1);
                    t = object.getT()+1;

                    Roi polyRoi = object.getRoi(ipl);
                    if (ipl.isHyperStack()) {
                        polyRoi.setPosition(1, z, t);
                    } else {
                        int pos = Math.max(Math.max(1,z),t);
                        polyRoi.setPosition(pos);
                    }
                    polyRoi.setStrokeColor(colour);
                    polyRoi.setStrokeWidth(lineWidth);
                    ovl.addElement(polyRoi);

                    break;

                case PositionModes.POSITION_MEASUREMENTS:
                    xMean = object.getMeasurement(posMeasurements[0]).getValue();
                    yMean = object.getMeasurement(posMeasurements[1]).getValue();
                    zMean = object.getMeasurement(posMeasurements[2]).getValue();

                    // Getting coordinates to plot
                    z = (int) Math.round(zMean+1);
                    t = object.getT()+1;

                    if (posMeasurements[3].equals("")) {
                        pointRoi = new PointRoi(xMean+0.5,yMean+0.5);
                        pointRoi.setPointType(PointRoi.NORMAL);
                        if (ipl.isHyperStack()) {
                            pointRoi.setPosition(1, z, t);
                        } else {
                            int pos = Math.max(Math.max(1,z),t);
                            pointRoi.setPosition(pos);
                        }
                        pointRoi.setStrokeColor(colour);
                        pointRoi.setStrokeWidth(lineWidth);
                        ovl.addElement(pointRoi);

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
                        ovl.addElement(ovalRoi);
                    }

                    break;

            }

            if (IDs != null) {
                // Adding text label
                TextRoi text = new TextRoi(xMean-labelSize/2, yMean-labelSize/2+5, IDs.get(object.getID()));
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

    public static void createTrackOverlay(ImagePlus ipl, String inputObjectsName, ObjCollection trackObjects,
                                          HashMap<Integer,Float> hues, double lineWidth, boolean verbose) {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        for (Obj trackObject:trackObjects.values()) {
            ObjCollection pointObjects = trackObject.getChildren(inputObjectsName);

            // Putting the current track points into a TreeMap stored by the frame
            TreeMap<Integer,Obj> points = new TreeMap<>();
            for (Obj pointObject:pointObjects.values()) {
                points.put(pointObject.getT(),pointObject);
            }

            //  Iterating over all points in the track, drawing lines between them
            int nFrames = ipl.getNFrames();
            Obj p1 = null;
            for (Obj p2:points.values()) {
                Color color = Color.getHSBColor(hues.get(p2.getID()),1,1);

                if (p1 != null) {
                    int x1 = (int) Math.round(p1.getXMean(true));
                    int y1 = (int) Math.round(p1.getYMean(true));
                    int x2 = (int) Math.round(p2.getXMean(true));
                    int y2 = (int) Math.round(p2.getYMean(true));

                    double r = 2;
                    for (int t = p2.getT();t<nFrames;t++) {
                        Line line = new Line(x1, y1, x2, y2);
                        line.setPosition(t+1);
                        line.setStrokeWidth(lineWidth);
                        line.setStrokeColor(color);
                        ovl.addElement(line);

                    }
                }

                p1 = p2;

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
        String labelMode = parameters.getValue(LABEL_MODE);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC);
        int labelSize = parameters.getValue(LABEL_SIZE);
        String parentObjectsForIDName = parameters.getValue(PARENT_OBJECT_FOR_ID);
        String measurementForID = parameters.getValue(MEASUREMENT_FOR_ID);
        String positionMode = parameters.getValue(POSITION_MODE);
        String colourMode = parameters.getValue(COLOUR_MODE);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT);
        boolean useRadius = parameters.getValue(USE_RADIUS);
        String measurementForRadius = parameters.getValue(MEASUREMENT_FOR_RADIUS);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);
        double lineWidth = parameters.getValue(LINE_WIDTH);
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

        // Generating colours for each object
        HashMap<Integer,Float> hues = inputObjects.getHue(colourMode,measurementForColour,parentObjectsForColourName,true);
        HashMap<Integer,String> IDs;
        if (showID) {
            IDs = inputObjects.getIDs(labelMode,measurementForID,parentObjectsForIDName,decimalPlaces,useScientific);
        } else {
            IDs = null;
        }

        switch (positionMode) {
            case PositionModes.ALL_POINTS:
            case PositionModes.CENTROID:
            case PositionModes.OUTLINE:
            case PositionModes.POSITION_MEASUREMENTS:
                String[] positionMeasurements;
                if (useRadius) {
                    positionMeasurements = new String[]{xPosMeas, yPosMeas, zPosMeas, measurementForRadius};
                } else {
                    positionMeasurements = new String[]{xPosMeas, yPosMeas, zPosMeas, ""};
                }
                createOverlay(ipl,inputObjects,positionMode,positionMeasurements,hues,IDs,labelSize,lineWidth);
                break;

            case PositionModes.TRACKS:
                ObjCollection tracks = workspace.getObjectSet(trackObjectsName);
                createTrackOverlay(ipl,inputObjectsName,tracks,hues,lineWidth,verbose);
                break;
        }

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
        parameters.add(new Parameter(LABEL_MODE, Parameter.CHOICE_ARRAY, LabelModes.ID, LabelModes.ALL));
        parameters.add(new Parameter(DECIMAL_PLACES, Parameter.INTEGER,2));
        parameters.add(new Parameter(USE_SCIENTIFIC,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(LABEL_SIZE, Parameter.INTEGER,8));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_ID, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(MEASUREMENT_FOR_ID, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(POSITION_MODE, Parameter.CHOICE_ARRAY, PositionModes.CENTROID, PositionModes.ALL));
        parameters.add(new Parameter(X_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(Y_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(Z_POSITION_MEASUREMENT, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(USE_RADIUS, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(MEASUREMENT_FOR_RADIUS, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(COLOUR_MODE, Parameter.CHOICE_ARRAY, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new Parameter(MEASUREMENT_FOR_COLOUR, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(TRACK_OBJECTS, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(LINE_WIDTH,Parameter.DOUBLE,1.0));
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
            returnedParameters.add(parameters.getParameter(LABEL_MODE));

            switch ((String) parameters.getValue(LABEL_MODE)) {
                case LabelModes.MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_ID));
                    String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                    parameters.updateValueSource(MEASUREMENT_FOR_ID,inputObjectsName);
                    break;

                case LabelModes.PARENT_ID:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_ID));
                    inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                    parameters.updateValueSource(PARENT_OBJECT_FOR_ID,inputObjectsName);
                    break;
            }

            returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
            returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
            returnedParameters.add(parameters.getParameter(LABEL_SIZE));

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

            returnedParameters.add(parameters.getParameter(USE_RADIUS));
            if (parameters.getValue(USE_RADIUS)) {
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_RADIUS));
                parameters.updateValueSource(MEASUREMENT_FOR_RADIUS,inputObjectsName);
            }
        }

        if (parameters.getValue(POSITION_MODE).equals(PositionModes.TRACKS)) {
            returnedParameters.add(parameters.getParameter(TRACK_OBJECTS));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(TRACK_OBJECTS,inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        if (parameters.getValue(COLOUR_MODE).equals(ObjCollection.ColourModes.MEASUREMENT_VALUE)) {
            // Use measurement
            returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));

            if (parameters.getValue(INPUT_OBJECTS) != null) {
                parameters.updateValueSource(MEASUREMENT_FOR_COLOUR,parameters.getValue(INPUT_OBJECTS));

            }

        } else if (parameters.getValue(COLOUR_MODE).equals(ObjCollection.ColourModes.PARENT_ID)) {
            // Use Parent ID
            returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);

        }

        returnedParameters.add(parameters.getParameter(LINE_WIDTH));
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
