// TODO: Add option to leave overlay as objects (i.e. don't flatten)
// TODO: Add option to plot tracks (will need to import track and spot objects as parent/child relationship)

package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.*;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.ModularImageAnalysis.Process.ColourFactory;
import wbif.sjx.ModularImageAnalysis.Process.LabelFactory;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sc13967 on 17/05/2017.
 */
public class AddObjectsOverlay extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String ADD_OUTPUT_TO_WORKSPACE = "Add output image to workspace";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String POSITION_MODE = "Position mode";
    public static final String LABEL_MODE = "Label mode";
    public static final String DECIMAL_PLACES = "Decimal places";
    public static final String USE_SCIENTIFIC = "Use scientific notation";
    public static final String LABEL_SIZE = "Label size";
    public static final String PARENT_OBJECT_FOR_LABEL = "Parent object for label";
    public static final String MEASUREMENT_FOR_LABEL = "Measurement for label";
    public static final String X_POSITION_MEASUREMENT = "X-position measurement";
    public static final String Y_POSITION_MEASUREMENT = "Y-position measurement";
    public static final String Z_POSITION_MEASUREMENT = "Z-position measurement";
    public static final String USE_RADIUS = "Use radius measurement";
    public static final String MEASUREMENT_FOR_RADIUS = "Measuremen for radius";
    public static final String COLOUR_MODE = "Colour mode";
    public static final String SINGLE_COLOUR = "Single colour";
    public static final String MEASUREMENT_FOR_COLOUR = "Measurement for colour";
    public static final String PARENT_OBJECT_FOR_COLOUR = "Parent object for colour";
    public static final String SPOT_OBJECTS = "Spot objects";
    public static final String LIMIT_TRACK_HISTORY = "Limit track history";
    public static final String TRACK_HISTORY = "Track history (frames)";
    public static final String LINE_WIDTH = "Line width";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


    public interface ColourModes extends ObjCollection.ColourModes {}

    public interface SingleColours extends ColourFactory.SingleColours {}

    public interface LabelModes extends LabelFactory.LabelModes {}

    public interface PositionModes {
        String ALL_POINTS = "All points";
        String CENTROID = "Centroid";
        String LABEL_ONLY = "Label only";
        String OUTLINE = "Outline";
        String POSITION_MEASUREMENTS = "Position measurements";
        String TRACKS = "Tracks";

        String[] ALL = new String[]{ALL_POINTS, CENTROID, LABEL_ONLY, OUTLINE, POSITION_MEASUREMENTS, TRACKS};

    }


    public static void addAllPointsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

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
            roi.setPointType(3);
            roi.setSize(0);
            roi.setStrokeColor(colour);
            roi.setStrokeWidth(lineWidth);

            if (ipl.isHyperStack()) {
                roi.setPosition(1, (int) zz[i]+1, t);
            } else {
                int pos = Math.max(Math.max(1,(int) zz[i]+1),t);
                roi.setPosition(pos);
            }
            ipl.getOverlay().addElement(roi);

        }
    }

    public static void addCentroidOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

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
        ipl.getOverlay().addElement(pointRoi);
    }

    public static void addOutlineOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

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
                polyRoi.setPosition(1, z+1, t);
            } else {
                int pos = Math.max(Math.max(1, z+1), t);
                polyRoi.setPosition(pos);
            }

            polyRoi.setStrokeColor(colour);
            polyRoi.setStrokeWidth(lineWidth);
            ipl.getOverlay().addElement(polyRoi);
        }
    }

    public static void addPositionMeasurementsOverlay(Obj object, ImagePlus ipl, Color colour, double lineWidth, String[] posMeasurements) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

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
            ipl.getOverlay().addElement(pointRoi);

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
            ipl.getOverlay().addElement(ovalRoi);
        }
    }

    public static void addTrackOverlay(Obj object, String spotObjectsName, ImagePlus ipl, Color colour, double lineWidth, int history) {
        ObjCollection pointObjects = object.getChildren(spotObjectsName);

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Putting the current track points into a TreeMap stored by the frame
        TreeMap<Integer,Obj> points = new TreeMap<>();
        for (Obj pointObject:pointObjects.values()) {
            points.put(pointObject.getT(),pointObject);
        }

        //  Iterating over all points in the track, drawing lines between them
        int nFrames = ipl.getNFrames();
        Obj p1 = null;
        for (Obj p2:points.values()) {
            if (p1 != null) {
                int x1 = (int) Math.round(p1.getXMean(true));
                int y1 = (int) Math.round(p1.getYMean(true));
                int x2 = (int) Math.round(p2.getXMean(true));
                int y2 = (int) Math.round(p2.getYMean(true));

                int maxFrame = Math.min(nFrames, Math.min(nFrames,p2.getT()+history));

                for (int t = p2.getT();t<=maxFrame-1;t++) {
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
                    line.setStrokeColor(colour);
                    ovl.addElement(line);
                }
            }

            p1 = p2;

        }
    }

    public static void addLabelsOverlay(ImagePlus ipl, String label, double[] labelCoords, Color colour, int labelSize) {
        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());

        // Adding text label
        TextRoi text = new TextRoi(labelCoords[0], labelCoords[1], label);
        text.setCurrentFont(new Font(Font.SANS_SERIF,Font.PLAIN,labelSize));
        text.setJustification(TextRoi.CENTER);
        text.setStrokeColor(colour);
        text.setLocation(labelCoords[0] - text.getFloatWidth()/2, labelCoords[1]-text.getFloatHeight()/2);

        if (ipl.isHyperStack()) {
            text.setPosition(1, (int) labelCoords[2], (int) labelCoords[3]);
        } else {
            text.setPosition((int) Math.max(Math.max(1, labelCoords[2]), labelCoords[3]));
        }
        ipl.getOverlay().addElement(text);

    }

    public HashMap<Integer,Float> getHues(ObjCollection inputObjects, String colourMode, String singleColour, String parentObjectsForColourName, String measurementForColour) {
        // Generating colours for each object
        switch (colourMode) {
            case ColourModes.SINGLE_COLOUR:
            default:
                return ColourFactory.getSingleColourHues(inputObjects,singleColour);
            case ColourModes.ID:
                return ColourFactory.getIDHues(inputObjects,true);
            case ColourModes.RANDOM_COLOUR:
                return ColourFactory.getRandomHues(inputObjects);
            case ColourModes.MEASUREMENT_VALUE:
                return ColourFactory.getMeasurementValueHues(inputObjects,measurementForColour,true);
            case ColourModes.PARENT_ID:
                return ColourFactory.getParentIDHues(inputObjects,parentObjectsForColourName,true);
            case ColourModes.PARENT_MEASUREMENT_VALUE:
                return ColourFactory.getParentMeasurementValueHues(inputObjects,parentObjectsForColourName,measurementForColour,true);
        }
    }

    public HashMap<Integer,String> getLabels(ObjCollection inputObjects, String labelMode, DecimalFormat df, String parentObjectsForLabelName, String measurementForLabel) {
        switch (labelMode) {
            case LabelModes.ID:
                return LabelFactory.getIDLabels(inputObjects,df);
            case LabelModes.MEASUREMENT_VALUE:
                return LabelFactory.getMeasurementLabels(inputObjects,measurementForLabel,df);
            case LabelModes.PARENT_ID:
                return LabelFactory.getParentIDLabels(inputObjects,parentObjectsForLabelName,df);
            case LabelModes.PARENT_MEASUREMENT_VALUE:
                return LabelFactory.getParentMeasurementLabels(inputObjects,parentObjectsForLabelName,measurementForLabel,df);
        }

        return null;

    }

    public void createAllPointsOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);

                addAllPointsOverlay(object, finalIpl, colour, lineWidth);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());

            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createCentroidOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);
                addCentroidOverlay(object, finalIpl, colour, lineWidth);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createLabelOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, @Nullable HashMap<Integer,String> labels, boolean multithread, int labelSize) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);
                String label = labels == null ? "" : labels.get(object.getID());

                double xMean = object.getXMean(true);
                double yMean = object.getYMean(true);
                double zMean = object.getZMean(true, false);
                int z = (int) Math.round(zMean + 1);
                int t = object.getT() + 1;

                addLabelsOverlay(finalIpl, label, new double[]{xMean, yMean, z, t}, colour, labelSize);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createOutlineOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);

                addOutlineOverlay(object, finalIpl, colour, lineWidth);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createPositionMeasurementsOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, String[] posMeasurements, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);

                addPositionMeasurementsOverlay(object, finalIpl, colour, lineWidth, posMeasurements);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    public void createTracksOverlay(ImagePlus ipl, ObjCollection inputObjects, @NotNull HashMap<Integer,Float> hues, String spotObjectsName, int history, boolean multithread, double lineWidth) throws InterruptedException {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (!ipl.isComposite() & (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1)) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());
        }

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        // Running through each object, adding it to the overlay along with an ID label
        AtomicInteger count = new AtomicInteger();
        for (Obj object:inputObjects.values()) {
            ImagePlus finalIpl = ipl;

            Runnable task = () -> {
                float hue = hues.get(object.getID());
                Color colour = ColourFactory.getColour(hue);

                addTrackOverlay(object, spotObjectsName, finalIpl, colour, lineWidth,  history);

                writeMessage("Rendered " + (count.incrementAndGet()) + " objects of " + inputObjects.size());
            };
            pool.submit(task);
        }

        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

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
    public boolean run(Workspace workspace) {
        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean addOutputToWorkspace = parameters.getValue(ADD_OUTPUT_TO_WORKSPACE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String positionMode = parameters.getValue(POSITION_MODE);
        String spotObjectsName = parameters.getValue(SPOT_OBJECTS);

        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        String xPosMeas = parameters.getValue(X_POSITION_MEASUREMENT);
        String yPosMeas = parameters.getValue(Y_POSITION_MEASUREMENT);
        String zPosMeas = parameters.getValue(Z_POSITION_MEASUREMENT);
        boolean useRadius = parameters.getValue(USE_RADIUS);
        String measurementForRadius = parameters.getValue(MEASUREMENT_FOR_RADIUS);
        boolean limitHistory = parameters.getValue(LIMIT_TRACK_HISTORY);
        int history = parameters.getValue(TRACK_HISTORY);

        // Getting colour settings
        String colourMode = parameters.getValue(COLOUR_MODE);
        String singleColour = parameters.getValue(SINGLE_COLOUR);
        String parentObjectsForColourName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);
        String measurementForColour = parameters.getValue(MEASUREMENT_FOR_COLOUR);

        // Getting label settings
        String labelMode = parameters.getValue(LABEL_MODE);
        int labelSize = parameters.getValue(LABEL_SIZE);
        int decimalPlaces = parameters.getValue(DECIMAL_PLACES);
        boolean useScientific = parameters.getValue(USE_SCIENTIFIC);
        String parentObjectsForLabelName = parameters.getValue(PARENT_OBJECT_FOR_LABEL);
        String measurementForLabel = parameters.getValue(MEASUREMENT_FOR_LABEL);

        int lineWidth = parameters.getValue(LINE_WIDTH);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Duplicating the image, so the original isn't altered
        if (!applyToInput) ipl = new Duplicator().run(ipl);

        // Generating colours for each object
        HashMap<Integer,Float> hues= getHues(inputObjects,colourMode,singleColour,parentObjectsForColourName,measurementForColour);

        // Adding the overlay element
        try {
            switch (positionMode) {
                case PositionModes.ALL_POINTS:
                    createAllPointsOverlay(ipl,inputObjects,hues,multithread,lineWidth);
                    break;
                case PositionModes.CENTROID:
                    createCentroidOverlay(ipl,inputObjects,hues,multithread,lineWidth);
                    break;
                case PositionModes.LABEL_ONLY:
                    DecimalFormat df = LabelFactory.getDecimalFormat(decimalPlaces,useScientific);
                    HashMap<Integer,String> labels = getLabels(inputObjects,labelMode,df,parentObjectsForLabelName,measurementForLabel);
                    createLabelOverlay(ipl,inputObjects,hues,labels,multithread,labelSize);
                    break;
                case PositionModes.OUTLINE:
                    createOutlineOverlay(ipl,inputObjects,hues,multithread,lineWidth);
                    break;
                case PositionModes.POSITION_MEASUREMENTS:
                    if (!useRadius) measurementForRadius = null;
                    String[] posMeasurements = new String[]{xPosMeas, yPosMeas, zPosMeas, measurementForRadius};
                    createPositionMeasurementsOverlay(ipl,inputObjects,hues,posMeasurements,multithread,lineWidth);
                    break;
                case PositionModes.TRACKS:
                    if (!limitHistory) history = Integer.MAX_VALUE;
                    createTracksOverlay(ipl,inputObjects,hues,spotObjectsName,history,multithread,lineWidth);
                    break;
            }
        } catch (InterruptedException e) {
            return false;
        }

        Image outputImage = new Image(outputImageName,ipl);

        // If necessary, adding output image to workspace.  This also allows us to show it.
        if (addOutputToWorkspace) workspace.addImage(outputImage);
        if (showOutput) showImage(outputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,false));
        parameters.add(new BooleanP(ADD_OUTPUT_TO_WORKSPACE, this,false));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(LABEL_MODE, this, LabelModes.ID, LabelModes.ALL));
        parameters.add(new IntegerP(DECIMAL_PLACES, this,0));
        parameters.add(new BooleanP(USE_SCIENTIFIC,this,false));
        parameters.add(new IntegerP(LABEL_SIZE, this,8));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_LABEL, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_LABEL, this));
        parameters.add(new ChoiceP(POSITION_MODE, this, PositionModes.CENTROID, PositionModes.ALL));
        parameters.add(new ObjectMeasurementP(X_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Y_POSITION_MEASUREMENT, this));
        parameters.add(new ObjectMeasurementP(Z_POSITION_MEASUREMENT, this));
        parameters.add(new BooleanP(USE_RADIUS, this,true));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_RADIUS, this));
        parameters.add(new ChoiceP(COLOUR_MODE, this, ColourModes.SINGLE_COLOUR, ColourModes.ALL));
        parameters.add(new ChoiceP(SINGLE_COLOUR,this,SingleColours.WHITE,SingleColours.ALL));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_FOR_COLOUR, this));
        parameters.add(new ParentObjectsP(PARENT_OBJECT_FOR_COLOUR, this));
        parameters.add(new ChildObjectsP(SPOT_OBJECTS, this));
        parameters.add(new BooleanP(LIMIT_TRACK_HISTORY, this,false));
        parameters.add(new IntegerP(TRACK_HISTORY, this,10));
        parameters.add(new DoubleP(LINE_WIDTH,this,0.2));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT_FOR_COLOUR);

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

        returnedParameters.add(parameters.getParameter(POSITION_MODE));
        if (parameters.getValue(POSITION_MODE).equals(PositionModes.POSITION_MEASUREMENTS)) {
            returnedParameters.add(parameters.getParameter(X_POSITION_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Y_POSITION_MEASUREMENT));
            returnedParameters.add(parameters.getParameter(Z_POSITION_MEASUREMENT));

            ((ObjectMeasurementP) parameters.getParameter(X_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
            ((ObjectMeasurementP) parameters.getParameter(Y_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);
            ((ObjectMeasurementP) parameters.getParameter(Z_POSITION_MEASUREMENT)).setObjectName(inputObjectsName);

            returnedParameters.add(parameters.getParameter(USE_RADIUS));
            if (parameters.getValue(USE_RADIUS)) {
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_RADIUS));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_RADIUS)).setObjectName(inputObjectsName);
            }
        }

        if (parameters.getValue(POSITION_MODE).equals(PositionModes.TRACKS)) {
            returnedParameters.add(parameters.getParameter(SPOT_OBJECTS));
            returnedParameters.add(parameters.getParameter(LIMIT_TRACK_HISTORY));

            if (parameters.getValue(LIMIT_TRACK_HISTORY)) returnedParameters.add(parameters.getParameter(TRACK_HISTORY));
            ((ChildObjectsP) parameters.getParameter(SPOT_OBJECTS)).setParentObjectsName(inputObjectsName);

        }

        returnedParameters.add(parameters.getParameter(COLOUR_MODE));
        switch ((String) parameters.getValue(COLOUR_MODE)) {
            case ColourModes.SINGLE_COLOUR:
                returnedParameters.add(parameters.getParameter(SINGLE_COLOUR));
                break;

            case ColourModes.MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                if (parameters.getValue(INPUT_OBJECTS) != null) {
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_COLOUR)).setObjectName(inputObjectsName);
                }
                break;

            case ColourModes.PARENT_ID:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);
                break;

            case ColourModes.PARENT_MEASUREMENT_VALUE:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_COLOUR));
                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_COLOUR)).setChildObjectsName(inputObjectsName);

                returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_COLOUR));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_COLOUR)).setObjectName(inputObjectsName);

                break;
        }

        returnedParameters.add(parameters.getParameter(LINE_WIDTH));

        if (parameters.getValue(POSITION_MODE).equals(PositionModes.LABEL_ONLY)) {
            returnedParameters.add(parameters.getParameter(LABEL_MODE));

            switch ((String) parameters.getValue(LABEL_MODE)) {
                case LabelModes.MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                    ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL)).setObjectName(inputObjectsName);
                    break;

                case LabelModes.PARENT_ID:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL)).setChildObjectsName(inputObjectsName);
                    break;

                case LabelModes.PARENT_MEASUREMENT_VALUE:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECT_FOR_LABEL));
                    ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT_FOR_LABEL)).setChildObjectsName(inputObjectsName);

                    returnedParameters.add(parameters.getParameter(MEASUREMENT_FOR_LABEL));
                    if (parentObjectsName != null) {
                        ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_FOR_LABEL)).setObjectName(inputObjectsName);
                    }
            }

            returnedParameters.add(parameters.getParameter(DECIMAL_PLACES));
            returnedParameters.add(parameters.getParameter(USE_SCIENTIFIC));
            returnedParameters.add(parameters.getParameter(LABEL_SIZE));
        }

        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

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
}
