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
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.MeasureObjectCentroid;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

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
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";
    public static final String DO_TRACKING = "Run tracking";
    public static final String CREATE_TRACK_OBJECTS = "Create track objects";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String SHOW_OBJECTS = "Show objects";
    public static final String SHOW_ID = "Show ID";
    public static final String ID_MODE = "ID source";

    private static final String USE_SPOT_ID = "Use spot ID";
    private static final String USE_TRACK_ID = "Use track ID";
    private static final String[] ID_MODES = new String[]{USE_SPOT_ID,USE_TRACK_ID};

    private static void createOverlay(ObjSet inputObjects, ImagePlus ipl, boolean showID, boolean useParentID, String parentObjectsName) {
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

            double xMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(Obj.X), MeasureObjectCentroid.MEAN);
            double yMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(Obj.Y), MeasureObjectCentroid.MEAN);
            double zMean = MeasureObjectCentroid.calculateCentroid(object.getCoordinates(Obj.Z), MeasureObjectCentroid.MEAN);

            // Getting coordinates to plot
            int c = ((int) object.getCoordinates(Obj.C)) + 1;
            int z = (int) Math.round(zMean+1);
            int t = ((int) object.getCoordinates(Obj.T)) + 1;

            // Adding circles where the object centroids are
            PointRoi roi = new PointRoi(xMean+1,yMean+1);
            roi.setPointType(3);
            if (ipl.isHyperStack()) {
                roi.setPosition(c, z, t);
            } else {
                int pos = Math.max(Math.max(c,z),t);
                roi.setPosition(pos);
            }

            if (showID) {
                int ID = useParentID ? object.getParent(parentObjectsName).getID() : object.getID();
                IJ.log("ID");
                TextRoi text = new TextRoi(xMean+1, yMean+1, String.valueOf(ID));
                text.setCurrentFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));

                if (ipl.isHyperStack()) {
                    text.setPosition(c, z, t);
                } else {
                    text.setPosition(Math.max(Math.max(c, z), t));
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
    public void execute(Workspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading input image
        String targetImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+targetImageName+") into workspace");
        ImagePlus ipl = workspace.getImages().get(targetImageName).getImagePlus();

        // Storing, then removing calibration.  This will be reapplied after the detection.
        Calibration calibration = ipl.getCalibration();
        ipl.setCalibration(null);

        // Getting parameters
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
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
        ObjSet spotObjects = new ObjSet(spotObjectsName);

        // Getting name of output summary objects (if required)
        boolean createTracks = parameters.getValue(CREATE_TRACK_OBJECTS);
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        ObjSet trackObjects = null;
        if (createTracks) trackObjects = new ObjSet(trackObjectsName);

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
        if (verbose) System.out.println("["+moduleName+"] Running TrackMate");
        if (!trackmate.checkInput()) IJ.log(trackmate.getErrorMessage());
        if (!trackmate.process()) IJ.log(trackmate.getErrorMessage());

        if (!(boolean) parameters.getValue(DO_TRACKING)) {
            // Getting trackObjects and adding them to the output trackObjects
            if (verbose) System.out.println("["+moduleName+"] Processing detected trackObjects");

            SpotCollection spots = model.getSpots();
            for (Spot spot:spots.iterable(false)) {
                Obj spotObject = new Obj(spotObjectsName,spot.ID());

                spotObject.addCoordinate(Obj.X,(int) spot.getDoublePosition(0));
                spotObject.addCoordinate(Obj.Y,(int) spot.getDoublePosition(1));
                spotObject.addCoordinate(Obj.Z,(int) spot.getDoublePosition(2));
                spotObject.addCoordinate(Obj.C,0);
                spotObject.addCoordinate(Obj.T,(int) Math.round(spot.getFeature(Spot.FRAME)));

                // Adding calibration values to the HCObject (physical distance per pixel)
                spotObject.addCalibration(Obj.X,calibration.getX(1));
                spotObject.addCalibration(Obj.Y,calibration.getY(1));
                spotObject.addCalibration(Obj.Z,calibration.getZ(1));
                spotObject.addCalibration(Obj.C,1);
                spotObject.addCalibration(Obj.T,1);
                spotObject.setCalibratedUnits(calibration.getUnits());

                spotObject.addMeasurement(new MIAMeasurement(MIAMeasurement.RADIUS,spot.getFeature(Spot.RADIUS),this));
                spotObject.addMeasurement(new MIAMeasurement(MIAMeasurement.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));

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

                // Getting parameters
                boolean showID = parameters.getValue(SHOW_ID);

                // Creating the overlay
                createOverlay(spotObjects,ipl,showID,false,"");

                // Displaying the overlay
                ipl.show();

            }

            if (verbose) System.out.println("["+moduleName+"] Complete");

            return;
        }

        // Converting tracks to local track model
        int ID = 1;
        if (verbose) System.out.println("["+moduleName+"] Converting tracks to local track model");

        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);
        for (Integer trackID:trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = null;
            if (createTracks) {
                trackObject = new Obj(trackObjectsName,trackID);

                // Adding calibration information
                trackObject.addCalibration(Obj.X,calibration.getX(1));
                trackObject.addCalibration(Obj.Y,calibration.getY(1));
                trackObject.addCalibration(Obj.Z,calibration.getZ(1));
                trackObject.addCalibration(Obj.C,1);
                trackObject.addCalibration(Obj.T,1);
                trackObject.setCalibratedUnits(calibration.getUnits());

            }

            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Creating an array to store the radius measurements for the summary object
            CumStat radiusAv = null;
            if (createTracks) radiusAv = new CumStat();
            CumStat estDiaAv = null;
            if (createTracks) estDiaAv = new CumStat();

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot:spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and group (track) ID.
                Obj spotObject = new Obj(spotObjectsName,ID++);

                // Getting coordinates
                int x = (int) spot.getDoublePosition(0);
                int y = (int) spot.getDoublePosition(1);
                int z = (int) (spot.getDoublePosition(2)*calibration.getZ(1)/calibration.getX(1));
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                spotObject.addCoordinate(Obj.X,x);
                spotObject.addCoordinate(Obj.Y,y);
                spotObject.addCoordinate(Obj.Z,z);
                spotObject.addCoordinate(Obj.C,0);
                spotObject.addCoordinate(Obj.T,t);

                // If necessary, adding coordinates to the summary objects
                if (createTracks) {
                    trackObject.addCoordinate(Obj.X,x);
                    trackObject.addCoordinate(Obj.Y,y);
                    trackObject.addCoordinate(Obj.Z,z);
                    trackObject.addCoordinate(Obj.C,0);
                    trackObject.addCoordinate(Obj.T,t);

                }

                // Adding radius measurement using the same coordinate system as HCObject (XYCZT)
                MIAMeasurement radiusMeasure = new MIAMeasurement(MIAMeasurement.RADIUS,spot.getFeature(Spot.RADIUS));
                radiusMeasure.setSource(this);
                spotObject.addMeasurement(radiusMeasure);
                if (createTracks) radiusAv.addMeasure(spot.getFeature(Spot.RADIUS));

                MIAMeasurement estDiaMeasure = new MIAMeasurement(MIAMeasurement.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER));
                estDiaMeasure.setSource(this);
                spotObject.addMeasurement(estDiaMeasure);
                if (createTracks) estDiaAv.addMeasure(spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER));

                // Adding calibration values to the HCObject (physical distance per pixel)
                spotObject.addCalibration(Obj.X,calibration.getX(1));
                spotObject.addCalibration(Obj.Y,calibration.getY(1));
                spotObject.addCalibration(Obj.Z,calibration.getZ(1));
                spotObject.addCalibration(Obj.C,1);
                spotObject.addCalibration(Obj.T,1);
                spotObject.setCalibratedUnits(calibration.getUnits());

                // Adding the connection between instance and summary objects
                if (createTracks) {
                    spotObject.addParent(trackObject);
                    trackObject.addChild(spotObject);

                }

                // Adding the instance object to the relevant collection
                spotObjects.put(spotObject.getID(),spotObject);

            }

            // Taking average measurements for the summary object
            if (createTracks) {
                MIAMeasurement radiusMeasure = new MIAMeasurement(MIAMeasurement.RADIUS,radiusAv.getMean());
                radiusMeasure.setSource(this);
                trackObject.addMeasurement(radiusMeasure);

                MIAMeasurement estDiaMeasure = new MIAMeasurement(MIAMeasurement.ESTIMATED_DIAMETER,estDiaAv.getMean());
                estDiaMeasure.setSource(this);
                trackObject.addMeasurement(estDiaMeasure);

                trackObjects.put(trackObject.getID(), trackObject);

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
                if (parameters.getValue(ID_MODE).equals(USE_TRACK_ID)) {
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

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(OUTPUT_SPOT_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Spots")));

        parameters.addParameter(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(DO_SUBPIXEL_LOCALIZATION, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(DO_MEDIAN_FILTERING, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(RADIUS, Parameter.DOUBLE,2.0));
        parameters.addParameter(new Parameter(THRESHOLD, Parameter.DOUBLE,5000.0));

        parameters.addParameter(new Parameter(DO_TRACKING, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(LINKING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.addParameter(new Parameter(GAP_CLOSING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.addParameter(new Parameter(MAX_FRAME_GAP, Parameter.INTEGER,3));

        parameters.addParameter(new Parameter(CREATE_TRACK_OBJECTS, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_TRACK_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Tracks")));

        parameters.addParameter(new Parameter(SHOW_OBJECTS, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(SHOW_ID, Parameter.BOOLEAN,false));
        parameters.addParameter(new Parameter(ID_MODE, Parameter.CHOICE_ARRAY,ID_MODES[0],ID_MODES));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.addParameter(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.addParameter(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.addParameter(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.addParameter(parameters.getParameter(RADIUS));
        returnedParameters.addParameter(parameters.getParameter(THRESHOLD));

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
    public void addMeasurements(MeasurementCollection measurements) {
        if (parameters.getValue(OUTPUT_SPOT_OBJECTS) != null) {
            measurements.addMeasurement(parameters.getValue(OUTPUT_SPOT_OBJECTS), MIAMeasurement.RADIUS);
            measurements.addMeasurement(parameters.getValue(OUTPUT_SPOT_OBJECTS), MIAMeasurement.ESTIMATED_DIAMETER);
        }

        if (parameters.getValue(CREATE_TRACK_OBJECTS)) {
            if (parameters.getValue(OUTPUT_TRACK_OBJECTS) != null) {
                measurements.addMeasurement(parameters.getValue(OUTPUT_TRACK_OBJECTS), MIAMeasurement.RADIUS);
                measurements.addMeasurement(parameters.getValue(OUTPUT_TRACK_OBJECTS), MIAMeasurement.ESTIMATED_DIAMETER);
            }
        }

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {
        if (parameters.getValue(DO_TRACKING)) {
            relationships.addRelationship(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS));

        }
    }
}
