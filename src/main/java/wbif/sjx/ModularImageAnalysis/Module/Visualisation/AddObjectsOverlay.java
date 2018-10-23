// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import net.imagej.overlay.CompositeOverlay;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.net.URLDecoder;
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
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String TRACK_OBJECTS = "Track objects";
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";
    public static final String TRACK_HISTORY = "Track history (frames)";
    public static final String LINE_WIDTH = "Line width";


    public interface ColourModes extends ObjCollection.ColourModes {}

    public interface SingleColours extends ObjCollection.SingleColours {}

    public interface LabelModes extends ObjCollection.LabelModes {}

    public interface PositionModes {
        String ALL_POINTS = "All points";
        String CENTROID = "Centroid";
        String OUTLINE = "Outline";
        String POSITION_MEASUREMENTS = "Position measurements";
        String TRACKS = "Tracks";

        String[] ALL = new String[]{ALL_POINTS, CENTROID, OUTLINE, POSITION_MEASUREMENTS, TRACKS};

    }


    public static void addAllPointsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        addAllPointsOverlay(object,ipl,colour,lineWidth,"",0);
    }

    public static void addAllPointsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String label, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Still need to get mean coords for label
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true,false);

        // Adding each point
        double[] xx = object.getX(true);
        double[] yy = object.getY(true);
        double[] zz = object.getZ(true,false);

        int z = (int) Math.round(zMean+1);
        int t = object.getT()+1;

        for (int i=0;i<xx.length;i++) {
            PointRoi roi = new PointRoi(xx[i]+0.5,yy[i]+0.5);
            roi.setPointType(PointRoi.NORMAL);

            if (ipl.isHyperStack()) {
                roi.setPosition(1, (int) zz[i]+1, t);
            } else {
                int pos = Math.max(Math.max(1,(int) zz[i]+1),t);
                roi.setPosition(pos);
            }
            roi.setStrokeColor(colour);
            roi.setStrokeWidth(lineWidth);
            ovl.addElement(roi);

        }

        if (!label.equals("")) {
            double[] labelCoords = new double[]{xMean, yMean, z, t};
            addLabelsOverlay(ipl, label, labelCoords, colour, labelSize);
        }
    }

    public static void addCentroidOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        addCentroidOverlay(object,ipl,colour,lineWidth,"",0);
    }

    public static void addCentroidOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String label, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true,false);

        // Getting coordinates to plot
        int z = (int) Math.round(zMean+1);
        int t = object.getT()+1;

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

        if (!label.equals("")) {
            double[] labelCoords = new double[]{xMean, yMean, z, t};
            addLabelsOverlay(ipl, label, labelCoords, colour, labelSize);
        }
    }

    public static void addOutlineOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        addOutlineOverlay(object,ipl,colour,lineWidth,"",0);
    }

    public static void addOutlineOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String label, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Still need to get mean coords for label
        double xMean = object.getXMean(true);
        double yMean = object.getYMean(true);
        double zMean = object.getZMean(true, false);
        int t = object.getT() + 1;

        // Running through each slice of this object
        double[][] range = object.getExtents(true,false);
        for (int z= (int) range[2][0];z<= (int) range[2][1];z++) {
            Roi polyRoi = object.getRoi(z);

            //  If the object doesn't have any pixels in this plane, skip it
            if (polyRoi == null) continue;

            if (ipl.isHyperStack()) {
                ipl.setPosition(1,z+1,t);
                polyRoi.setPosition(1, z+1, t);
            } else {
                int pos = Math.max(Math.max(1, z+1), t);
                ipl.setPosition(pos);
                polyRoi.setPosition(pos);
            }

            polyRoi.setStrokeColor(colour);
            polyRoi.setStrokeWidth(lineWidth);
            ovl.addElement(polyRoi);

            if (!label.equals("")) {
                double[] labelCoords = new double[]{xMean, yMean, z+1, t};
                addLabelsOverlay(ipl, label, labelCoords, colour, labelSize);
            }
        }
    }

    public static void addPositionMeasurementsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String[] posMeasurements) {
        addPositionMeasurementsOverlay(object,ipl,colour,lineWidth,posMeasurements,"",0);
    }

    public static void addPositionMeasurementsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String[] posMeasurements, String label, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        double xMean = object.getMeasurement(posMeasurements[0]).getValue();
        double yMean = object.getMeasurement(posMeasurements[1]).getValue();
        double zMean = object.getMeasurement(posMeasurements[2]).getValue();

        // Getting coordinates to plot
        int z = (int) Math.round(zMean+1);
        int t = object.getT()+1;

        if (posMeasurements[3].equals("")) {
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

        if (!label.equals("")) {
            double[] labelCoords = new double[]{xMean, yMean, z, t};
            addLabelsOverlay(ipl, label, labelCoords, colour, labelSize);
        }
    }

    public static void addLabelsOverlay(ImagePlus ipl, String label, double[] labelCoords, Color colour,   int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Adding text label
        TextRoi text = new TextRoi(labelCoords[0]-labelSize/2, labelCoords[1]-labelSize/2+5, label);
        text.setCurrentFont(new Font(Font.SANS_SERIF,Font.PLAIN,labelSize));
        if (ipl.isHyperStack()) {
            text.setPosition(1, (int) labelCoords[2], (int) labelCoords[3]);
        } else {
            text.setPosition((int) Math.max(Math.max(1, labelCoords[2]), labelCoords[3]));
        }
        text.setStrokeColor(colour);
        ovl.addElement(text);

    }


    public HashMap<Integer,Color> getColours(ObjCollection inputObjects) {
        String colourMode = parameters.getValue(COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);

        // Generating colours for each object
        String sourceColour = "";
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
                sourceColour = singleColour;
                break;
            case ColourModes.MEASUREMENT_VALUE:
                sourceColour = measurementForColour;
                break;
            case ColourModes.PARENT_ID:
                sourceColour = parentObjectsForColourName;
                break;
        }
        return inputObjects.getColours(colourMode,sourceColour,true);

    }

    public HashMap<Integer,String> getLabels(ObjCollection inputObjects) {
        boolean showID = parameters.getValue(SHOW_LABEL);
        String labelMode = parameters.getValue(LABEL_MODE);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC);
        String parentObjectsForIDName = parameters.getValue(PARENT_OBJECT_FOR_ID);
        String measurementForID = parameters.getValue(MEASUREMENT_FOR_ID);

        String souceLabel = null;
        switch (labelMode) {
            case LabelModes.MEASUREMENT_VALUE:
                souceLabel = measurementForID;
                break;
            case LabelModes.PARENT_ID:
                souceLabel = parentObjectsForIDName;
                break;
        }

        if (showID) {
            return inputObjects.getIDs(labelMode,souceLabel,decimalPlaces,useScientific);
        } else {
            return null;
        }
    }

    public String[] getPositionMeasurements() {
        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT);
        boolean useRadius = parameters.getValue(USE_RADIUS);
        String measurementForRadius = parameters.getValue(MEASUREMENT_FOR_RADIUS);

        if (useRadius) {
            return new String[]{xPosMeas, yPosMeas, zPosMeas, measurementForRadius};
        } else {
            return new String[]{xPosMeas, yPosMeas, zPosMeas, ""};
        }
    }

    public void createOverlay(ImagePlus ipl, ObjCollection inputObjects, @Nonnull HashMap<Integer,Color> colours, @Nullable HashMap<Integer,String> labels) {
        String positionMode = parameters.getValue(POSITION_MODE);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        int labelSize = parameters.getValue(LABEL_SIZE);

        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        // Running through each object, adding it to the overlay along with an ID label
        int count = 0;
        for (Obj object:inputObjects.values()) {
            Color colour = colours.get(object.getID());
            String label = labels == null ? "" : labels.get(object.getID());

            double[] labelCoords = new double[0];
            switch (positionMode) {
                case PositionModes.ALL_POINTS:
                    addAllPointsOverlay(object,ipl,colour,lineWidth,label,labelSize);
                    break;

                case PositionModes.CENTROID:
                    addCentroidOverlay(object,ipl,colour,lineWidth,label,labelSize);
                    break;

                case PositionModes.OUTLINE:
                    addOutlineOverlay(object,ipl,colour,lineWidth,label,labelSize);
                    break;

                case PositionModes.POSITION_MEASUREMENTS:
                    String[] posMeasurements = getPositionMeasurements();
                    addPositionMeasurementsOverlay(object,ipl,colour,lineWidth,posMeasurements,label,labelSize);
                    break;
            }

            writeMessage("Rendered "+(++count)+" objects of "+inputObjects.size());

        }
    }

    public void createTrackOverlay(ImagePlus ipl, ObjCollection trackObjects, HashMap<Integer,Color> colours) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        double lineWidth = parameters.getValue(LINE_WIDTH);
        boolean limitHistory = parameters.getValue(LIMIT_TRACK_HISTORY);
        int history = parameters.getValue(TRACK_HISTORY);

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        int count = 0;
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
                Color color = colours.get(p2.getID());

                if (p1 != null) {
                    int x1 = (int) Math.round(p1.getXMean(true));
                    int y1 = (int) Math.round(p1.getYMean(true));
                    int x2 = (int) Math.round(p2.getXMean(true));
                    int y2 = (int) Math.round(p2.getYMean(true));

                    int maxFrame = nFrames;
                    if (limitHistory) maxFrame = p2.getT()+history;

                    for (int t = p2.getT();t<maxFrame-1;t++) {
                        PolygonRoi line = new PolygonRoi(new int[]{x1,x2},new int[]{y1,y2},2,PolygonRoi.POLYGON);

                        if (ipl.isHyperStack()) {
                            ipl.setPosition(1,1,t+1);
                            line.setPosition(1,1, t+1);
                        } else {
                            int pos = Math.max(Math.max(1, 1), t+1);
                            ipl.setPosition(pos);
                            line.setPosition(pos);
                        }

                        line.setStrokeWidth(lineWidth);
                        line.setStrokeColor(color);
                        ovl.addElement(line);
                    }
                }

                p1 = p2;

            }

            writeMessage("Rendered "+(count++)+" tracks of "+trackObjects.size());

        }
    }


    @Override
    public String getTitle() {
        return "Add overlay";
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
    public void run(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String positionMode = parameters.getValue(POSITION_MODE);
        String trackObjectsName = parameters.getValue(TRACK_OBJECTS);

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
        HashMap<Integer,Color> colours = getColours(inputObjects);

        // Generating labels for each object
        HashMap<Integer,String> labels = getLabels(inputObjects);

        switch (positionMode) {
            case PositionModes.ALL_POINTS:
            case PositionModes.CENTROID:
            case PositionModes.OUTLINE:
            case PositionModes.POSITION_MEASUREMENTS:
                createOverlay(ipl,inputObjects,colours,labels);
                break;

            case PositionModes.TRACKS:
                ObjCollection tracks = workspace.getObjectSet(trackObjectsName);
                createTrackOverlay(ipl,tracks,colours);
                break;
        }

        // If necessary, adding output image to workspace
        if (addOutputToWorkspace) {
            Image outputImage = new Image(outputImageName,ipl);
            workspace.addImage(outputImage);
        }

        // Duplicating the image, then displaying it.  Duplicating prevents the image being removed from the workspace
        // if it's closed
        if (showOutput) {
            ImagePlus showIpl = new Duplicator().run(ipl);
            showIpl.setTitle(outputImageName);
            showIpl.show();
        }
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
        parameters.add(new Parameter(SINGLE_COLOUR,Parameter.CHOICE_ARRAY,SingleColours.WHITE,SingleColours.ALL));
        parameters.add(new Parameter(MEASUREMENT_FOR_COLOUR, Parameter.OBJECT_MEASUREMENT,null,null));
        parameters.add(new Parameter(PARENT_OBJECT_FOR_COLOUR, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(TRACK_OBJECTS, Parameter.PARENT_OBJECTS,null,null));
        parameters.add(new Parameter(LIMIT_TRACK_HISTORY, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(TRACK_HISTORY, Parameter.INTEGER,10));
        parameters.add(new Parameter(LINE_WIDTH,Parameter.DOUBLE,1.0));

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
            returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

            if (parameters.getValue(LIMIT_TRACK_HISTORY)) {
                returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
            }

            String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
            parameters.updateValueSource(TRACK_OBJECTS,inputObjectsName);
        }

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE)) {
            case ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    parameters.updateValueSource(MEASUREMENT_FOR_COLOUR,parameters.getValue(INPUT_OBJECTS));
                }
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
                parameters.updateValueSource(PARENT_OBJECT_FOR_COLOUR,inputObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

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
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
