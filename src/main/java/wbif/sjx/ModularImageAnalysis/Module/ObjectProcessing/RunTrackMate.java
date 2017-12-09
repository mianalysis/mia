// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.NormaliseIntensity;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Process.IntensityMinMax;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class RunTrackMate extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_SPOT_OBJECTS = "Output spot objects";
    public static final String CALIBRATED_UNITS = "Calibrated radius";
    public static final String DO_SUBPIXEL_LOCALIZATION = "Do sub-pixel localisation";
    public static final String DO_MEDIAN_FILTERING = "Median filtering";
    public static final String RADIUS = "Radius";
    public static final String THRESHOLD = "Threshold";
    public static final String NORMALISE_INTENSITY = "Normalise intensity";
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";
    public static final String DO_TRACKING = "Run tracking";
    public static final String CREATE_TRACK_OBJECTS = "Create track objects";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String SHOW_OBJECTS = "Show objects";
    public static final String SHOW_ID = "Show ID";
    public static final String ID_MODE = "ID source";

    private ImageObjReference spotObjects;

    public interface IDModes {
        String USE_SPOT_ID = "Use spot ID";
        String USE_TRACK_ID = "Use track ID";

        String[] ALL = new String[]{USE_SPOT_ID, USE_TRACK_ID};

    }

    private interface Measurements {
        String RADIUS = "SPOT_DETECT_TRACK//RADIUS";
        String ESTIMATED_DIAMETER = "SPOT_DETECT_TRACK//EST_DIAMETER";

    }

    private static void createOverlay(ObjCollection inputObjects, ImagePlus ipl, boolean showID, boolean useParentID, String parentObjectsName) {
        // If necessary, turning the image into a HyperStack (if 2 dimensions=1 it will be a standard ImagePlus)
        if (ipl.getNSlices() > 1 | ipl.getNFrames() > 1 | ipl.getNChannels() > 1) {
            ipl = HyperStackConverter.toHyperStack(ipl, ipl.getNChannels(), ipl.getNSlices(), ipl.getNFrames());

        }

        if (ipl.getOverlay() == null) ipl.setOverlay(new Overlay());
        Overlay ovl = ipl.getOverlay();

        // Running through each object, adding it to the overlay along with an ID label
        for (Obj object:inputObjects.values()) {
            float H = new Random().nextFloat();
            Color colour = Color.getHSBColor(H, 1, 1);

            double xMean = object.getXMean(true);
            double yMean = object.getYMean(true);
            double zMean = object.getZMean(true,false);

            // Getting coordinates to plot
            int z = (int) Math.round(zMean+1);
            int t = object.getT();

            // Adding circles where the object centroids are
            PointRoi roi = new PointRoi(xMean+1,yMean+1);
            roi.setPointType(3);
            if (ipl.isHyperStack()) {
                roi.setPosition(1, z, t);
            } else {
                int pos = Math.max(Math.max(1,z),t);
                roi.setPosition(pos);
            }

            if (showID) {
                int ID = useParentID ? object.getParent(parentObjectsName).getID() : object.getID();
                IJ.log("ID");
                TextRoi text = new TextRoi(xMean+1, yMean+1, String.valueOf(ID));
                text.setCurrentFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));

                if (ipl.isHyperStack()) {
                    text.setPosition(1, z, t);
                } else {
                    text.setPosition(Math.max(Math.max(1, z), t));
                }
                text.setStrokeColor(colour);
                ovl.addElement(text);

            }

            roi.setStrokeColor(colour);
            ovl.addElement(roi);

        }
    }

    @Override
    public String getTitle() {
        return "Run TrackMate";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void run(Workspace workspace, boolean verbose) {
        // Loading input image
        String targetImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+targetImageName+") into workspace");
        Image targetImage = workspace.getImage(targetImageName);
        ImagePlus ipl = targetImage.getImagePlus();

        // Storing, then removing calibration.  This will be reapplied after the detection.
        Calibration calibration = ipl.getCalibration();
        ipl.setCalibration(null);
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        // Getting parameters
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
        boolean normaliseIntensity = parameters.getValue(NORMALISE_INTENSITY);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);

        // Applying conversion to parameters
        if (calibratedUnits) {
            radius = calibration.getRawX(radius);
            maxLinkDist = calibration.getRawX(maxLinkDist);
            maxGapDist = calibration.getRawX(maxGapDist);

        }

        // Getting name of output objects
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        ObjCollection spotObjects = new ObjCollection(spotObjectsName);

        // Getting name of output summary objects (if required)
        boolean createTracks = parameters.getValue(CREATE_TRACK_OBJECTS);
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        ObjCollection trackObjects = null;
        if (createTracks) trackObjects = new ObjCollection(trackObjectsName);

        // If image should be normalised
        if (normaliseIntensity) {
            ipl = new Duplicator().run(ipl);
            NormaliseIntensity.normaliseIntenisty(ipl);

        }

        // Initialising TrackMate model to store data
        Model model = new Model();
        model.setLogger(Logger.VOID_LOGGER);

        // Initialising settings for TrackMate
        Settings settings = new Settings();

        settings.setFrom(ipl);
        settings.detectorFactory = new LogDetectorFactory();
        settings.detectorSettings.put("DO_SUBPIXEL_LOCALIZATION", subpixelLocalisation);
        settings.detectorSettings.put("DO_MEDIAN_FILTERING", medianFiltering);
        settings.detectorSettings.put("RADIUS", radius);
        settings.detectorSettings.put("THRESHOLD", threshold);
        settings.detectorSettings.put("TARGET_CHANNEL", 1);

        settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<>());

        settings.trackerFactory  = new SparseLAPTrackerFactory();
        settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
        settings.trackerSettings.put("ALLOW_TRACK_SPLITTING", false);
        settings.trackerSettings.put("ALLOW_TRACK_MERGING", false);
        settings.trackerSettings.put("LINKING_MAX_DISTANCE", maxLinkDist);
        settings.trackerSettings.put("GAP_CLOSING_MAX_DISTANCE", maxGapDist);
        settings.trackerSettings.put("MAX_FRAME_GAP",maxFrameGap);

        TrackMate trackmate = new TrackMate(model, settings);

        // Running TrackMate
        if (verbose) System.out.println("["+moduleName+"] Running TrackMate detection");
        if (!trackmate.checkInput()) IJ.log(trackmate.getErrorMessage());
        if (!trackmate.execDetection()) IJ.log(trackmate.getErrorMessage());
        if (!trackmate.computeSpotFeatures(false)) IJ.log(trackmate.getErrorMessage());

        if (normaliseIntensity) ipl = targetImage.getImagePlus();

        if (!(boolean) parameters.getValue(DO_TRACKING)) {
            // Getting trackObjects and adding them to the output trackObjects
            if (verbose) System.out.println("["+moduleName+"] Processing detected objects");

            SpotCollection spots = model.getSpots();
            for (Spot spot:spots.iterable(false)) {
                Obj spotObject = new Obj(spotObjectsName,spot.ID(),dppXY,dppZ,calibrationUnits);
                spotObject.addCoord((int) spot.getDoublePosition(0),(int) spot.getDoublePosition(1),(int) spot.getDoublePosition(2));
                spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

                spotObject.addMeasurement(new Measurement(Measurements.RADIUS,spot.getFeature(Spot.RADIUS),this));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));

                spotObjects.put(spotObject.getID(),spotObject);

            }

            if (verbose) System.out.println("["+moduleName+"] "+spots.getNSpots(false)+" trackObjects detected");

            // Adding spotObjects to the workspace
            if (verbose) System.out.println("["+moduleName+"] Adding spotObjects ("+spotObjectsName+") to workspace");
            workspace.addObjects(spotObjects);

            // Displaying trackObjects (if selected)
            if (parameters.getValue(SHOW_OBJECTS)) {
                System.out.println("Showing trackObjects");

                // Creating a duplicate of the input image
                ipl = new Duplicator().run(ipl);
                IntensityMinMax.run(ipl,true);

                // Getting parameters
                boolean showID = parameters.getValue(SHOW_ID);

                // Creating the overlay
                createOverlay(spotObjects,ipl,showID,false,"");

                // Displaying the overlay
                ipl.show();

            }

            return;
        }

        if (verbose) System.out.println("["+moduleName+"] Running TrackMate tracking");
        if (!trackmate.execTracking()) IJ.log(trackmate.getErrorMessage());

        // Converting tracks to local track model
        int ID = 1;
        if (verbose) System.out.println("["+moduleName+"] Converting tracks to local track model");

        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);
        for (Integer trackID:trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = null;
            if (createTracks) {
                trackObject = new Obj(trackObjectsName,trackID,dppXY,dppZ,calibrationUnits);

            }

            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot:spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and group (track) ID.
                Obj spotObject = new Obj(spotObjectsName,ID++,dppXY,dppZ,calibrationUnits);

                // Getting coordinates
                int x = (int) spot.getDoublePosition(0);
                int y = (int) spot.getDoublePosition(1);
                int z = (int) (spot.getDoublePosition(2)*dppZ/dppXY);
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                spotObject.addCoord(x,y,z);
                spotObject.setT(t);

                // If necessary, adding coordinates to the summary objects
                if (createTracks) {
                    trackObject.addCoord(x,y,z);
                    trackObject.setT(0);

                }

                // Adding the connection between instance and summary objects
                if (createTracks) {
                    spotObject.addParent(trackObject);
                    trackObject.addChild(spotObject);

                }

                // Adding the instance object to the relevant collection
                spotObjects.put(spotObject.getID(),spotObject);

            }
        }

        // Displaying the number of objects detected
        if (verbose) {
            System.out.println("["+moduleName+"] "+spotObjects.size()+" spots detected");

            if (createTracks) System.out.println("["+moduleName+"] "+trackObjects.size()+" tracks detected");

        }

        // Adding objects to the workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+spotObjectsName+") to workspace");
        workspace.addObjects(spotObjects);

        if (createTracks) workspace.addObjects(trackObjects);

        // Displaying objects (if selected)
        if (parameters.getValue(SHOW_OBJECTS)) {
            // Creating a duplicate of the input image
            ipl = new Duplicator().run(ipl);

            // Getting parameters
            boolean showID = parameters.getValue(SHOW_ID);
            boolean useTrackID = false;
            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(ID_MODE).equals(IDModes.USE_TRACK_ID)) {
                    useTrackID = true;
                }
            }

            // Creating the overlay
            createOverlay(spotObjects,ipl,showID,useTrackID,trackObjectsName);

            // Displaying the overlay
            ipl.show();

        }

        // Reapplying calibration to input image
        ipl.setCalibration(calibration);

    }

    @Override
    public ParameterCollection initialiseParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        returnedParameters.addParameter(new Parameter(OUTPUT_SPOT_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Spots")));

        returnedParameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        returnedParameters.addParameter(new Parameter(DO_SUBPIXEL_LOCALIZATION, Parameter.BOOLEAN,true));
        returnedParameters.addParameter(new Parameter(DO_MEDIAN_FILTERING, Parameter.BOOLEAN,false));
        returnedParameters.addParameter(new Parameter(RADIUS, Parameter.DOUBLE,2.0));
        returnedParameters.addParameter(new Parameter(THRESHOLD, Parameter.DOUBLE,5000.0));
        returnedParameters.addParameter(new Parameter(NORMALISE_INTENSITY, Parameter.BOOLEAN,false));

        returnedParameters.addParameter(new Parameter(DO_TRACKING, Parameter.BOOLEAN,true));
        returnedParameters.addParameter(new Parameter(LINKING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        returnedParameters.addParameter(new Parameter(GAP_CLOSING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        returnedParameters.addParameter(new Parameter(MAX_FRAME_GAP, Parameter.INTEGER,3));

        returnedParameters.addParameter(new Parameter(CREATE_TRACK_OBJECTS, Parameter.BOOLEAN,true));
        returnedParameters.addParameter(new Parameter(OUTPUT_TRACK_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Tracks")));

        returnedParameters.addParameter(new Parameter(SHOW_OBJECTS, Parameter.BOOLEAN,false));
        returnedParameters.addParameter(new Parameter(SHOW_ID, Parameter.BOOLEAN,false));
        returnedParameters.addParameter(new Parameter(ID_MODE, Parameter.CHOICE_ARRAY,IDModes.USE_SPOT_ID,IDModes.ALL));

        return returnedParameters;

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.addParameter(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.addParameter(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.addParameter(parameters.getParameter(RADIUS));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD));
        returnedParameters.addParameter(parameters.getParameter(NORMALISE_INTENSITY));

        returnedParameters.addParameter(parameters.getParameter(DO_TRACKING));
        if (parameters.getValue(DO_TRACKING)) {
            returnedParameters.addParameter(parameters.getParameter(LINKING_MAX_DISTANCE));
            returnedParameters.addParameter(parameters.getParameter(GAP_CLOSING_MAX_DISTANCE));
            returnedParameters.addParameter(parameters.getParameter(MAX_FRAME_GAP));

            returnedParameters.addParameter(parameters.getParameter(CREATE_TRACK_OBJECTS));
            if (parameters.getValue(CREATE_TRACK_OBJECTS)) {
                returnedParameters.addParameter(parameters.getParameter(OUTPUT_TRACK_OBJECTS));

            }
        }

        returnedParameters.addParameter(parameters.getParameter(SHOW_OBJECTS));
        if (parameters.getValue(SHOW_OBJECTS)) {
            returnedParameters.addParameter(parameters.getParameter(SHOW_ID));

            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(SHOW_ID)) {
                    returnedParameters.addParameter(parameters.getParameter(ID_MODE));

                }
            }
        }

        return returnedParameters;

    }

    @Override
    protected MeasurementReferenceCollection initialiseImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    protected MeasurementReferenceCollection initialiseObjectMeasurementReferences() {
        MeasurementReferenceCollection references = new MeasurementReferenceCollection();

        references.add(new MeasurementReference(Measurements.RADIUS));
        references.add(new MeasurementReference(Measurements.ESTIMATED_DIAMETER));

        return references;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        String outputSpotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        MeasurementReference radius = objectMeasurementReferences.get(Measurements.RADIUS);
        MeasurementReference estimatedDiameter = objectMeasurementReferences.get(Measurements.ESTIMATED_DIAMETER);

        radius.setImageObjName(outputSpotObjectsName);
        estimatedDiameter.setImageObjName(outputSpotObjectsName);

        return objectMeasurementReferences;

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        if (parameters.getValue(DO_TRACKING)) {
            relationships.addRelationship(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS));

        }
    }
}

