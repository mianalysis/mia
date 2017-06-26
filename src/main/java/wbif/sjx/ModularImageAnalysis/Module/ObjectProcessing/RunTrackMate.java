// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

import java.util.ArrayList;
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

    @Override
    public String getTitle() {
        return "Run TrackMate";

    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Loading input image
        HCName targetImageName = parameters.getValue(INPUT_IMAGE);
        if (verbose) System.out.println("["+moduleName+"] Loading image ("+targetImageName.getName()+") into workspace");
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
        HCName outputObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        HCObjectSet outputObjects = new HCObjectSet(outputObjectsName);

        // Getting name of output summary objects (if required)
        boolean createSummary = parameters.getValue(CREATE_TRACK_OBJECTS);
        HCName outputSummaryObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        HCObjectSet summaryObjects = null;
        if (createSummary) summaryObjects = new HCObjectSet(outputSummaryObjectsName);

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
            // Getting objects and adding them to the output objects
            if (verbose) System.out.println("["+moduleName+"] Processing detected objects");
            HCObjectSet objects = new HCObjectSet(outputObjectsName);

            SpotCollection spots = model.getSpots();
            for (Spot spot:spots.iterable(false)) {
                HCObject object = new HCObject(outputObjectsName,spot.ID());

                object.addCoordinate(HCObject.X,(int) spot.getDoublePosition(0));
                object.addCoordinate(HCObject.Y,(int) spot.getDoublePosition(1));
                object.addCoordinate(HCObject.Z,(int) spot.getDoublePosition(2));
                object.addCoordinate(HCObject.C,0);
                object.addCoordinate(HCObject.T,(int) Math.round(spot.getFeature(Spot.FRAME)));

                // Adding calibration values to the HCObject (physical distance per pixel)
                object.addCalibration(HCObject.X,calibration.getX(1));
                object.addCalibration(HCObject.Y,calibration.getY(1));
                object.addCalibration(HCObject.Z,calibration.getZ(1));
                object.addCalibration(HCObject.C,1);
                object.addCalibration(HCObject.T,1);
                object.setCalibratedUnits(calibration.getUnits());

                object.addMeasurement(new HCMeasurement(HCMeasurement.RADIUS,spot.getFeature(Spot.RADIUS),this));
                object.addMeasurement(new HCMeasurement(HCMeasurement.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));

                objects.put(object.getID(),object);

            }

            if (verbose) System.out.println("["+moduleName+"] "+spots.getNSpots(false)+" objects detected");

            // Adding objects to the workspace
            if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName.getName()+") to workspace");
            workspace.addObjects(objects);

            // Displaying objects (if selected)
            if (parameters.getValue(SHOW_OBJECTS)) {
                // Creating a duplicate of the input image
                ipl = new Duplicator().run(ipl);

                // Getting parameters
                boolean showID = parameters.getValue(SHOW_ID);
                boolean useGroupID = false;
                if (parameters.getValue(DO_TRACKING)) {
                    if (parameters.getValue(ID_MODE).equals(USE_TRACK_ID)) {
                        useGroupID = true;
                    }
                }

//                // Creating the overlay
//                ShowObjectsOverlay.createOverlay(ipl,outputObjects,showID,useGroupID);

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
            HCObject summaryObject = null;
            if (createSummary) {
                summaryObject = new HCObject(outputSummaryObjectsName,trackID);

                // Adding calibration information
                summaryObject.addCalibration(HCObject.X,calibration.getX(1));
                summaryObject.addCalibration(HCObject.Y,calibration.getY(1));
                summaryObject.addCalibration(HCObject.Z,calibration.getZ(1));
                summaryObject.addCalibration(HCObject.C,1);
                summaryObject.addCalibration(HCObject.T,1);
                summaryObject.setCalibratedUnits(calibration.getUnits());

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
            if (createSummary) radiusAv = new CumStat();
            CumStat estDiaAv = null;
            if (createSummary) estDiaAv = new CumStat();

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot:spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and group (track) ID.
                HCObject object = new HCObject(outputObjectsName,ID++);

                // Getting coordinates
                int x = (int) spot.getDoublePosition(0);
                int y = (int) spot.getDoublePosition(1);
                int z = (int) (spot.getDoublePosition(2)*calibration.getZ(1)/calibration.getX(1));
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                object.addCoordinate(HCObject.X,x);
                object.addCoordinate(HCObject.Y,y);
                object.addCoordinate(HCObject.Z,z);
                object.addCoordinate(HCObject.C,0);
                object.addCoordinate(HCObject.T,t);

                // If necessary, adding coordinates to the summary objects
                if (createSummary) {
                    summaryObject.addCoordinate(HCObject.X,x);
                    summaryObject.addCoordinate(HCObject.Y,y);
                    summaryObject.addCoordinate(HCObject.Z,z);
                    summaryObject.addCoordinate(HCObject.C,0);
                    summaryObject.addCoordinate(HCObject.T,t);

                }

                // Adding radius measurement using the same coordinate system as HCObject (XYCZT)
                HCMeasurement radiusMeasure = new HCMeasurement(HCMeasurement.RADIUS,spot.getFeature(Spot.RADIUS));
                radiusMeasure.setSource(this);
                object.addMeasurement(radiusMeasure);
                if (createSummary) radiusAv.addMeasure(spot.getFeature(Spot.RADIUS));

                HCMeasurement estDiaMeasure = new HCMeasurement(HCMeasurement.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER));
                estDiaMeasure.setSource(this);
                object.addMeasurement(estDiaMeasure);
                if (createSummary) estDiaAv.addMeasure(spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER));

                // Adding calibration values to the HCObject (physical distance per pixel)
                object.addCalibration(HCObject.X,calibration.getX(1));
                object.addCalibration(HCObject.Y,calibration.getY(1));
                object.addCalibration(HCObject.Z,calibration.getZ(1));
                object.addCalibration(HCObject.C,1);
                object.addCalibration(HCObject.T,1);
                object.setCalibratedUnits(calibration.getUnits());

                // Adding the connection between instance and summary objects
                if (createSummary) {
                    object.addParent(summaryObject);
                    summaryObject.addChild(object);

                }

                // Adding the instance object to the relevant collection
                outputObjects.put(object.getID(),object);

            }

            // Taking average measurements for the summary object
            if (createSummary) {
                HCMeasurement radiusMeasure = new HCMeasurement(HCMeasurement.RADIUS,radiusAv.getMean());
                radiusMeasure.setSource(this);
                summaryObject.addMeasurement(radiusMeasure);

                HCMeasurement estDiaMeasure = new HCMeasurement(HCMeasurement.ESTIMATED_DIAMETER,estDiaAv.getMean());
                estDiaMeasure.setSource(this);
                summaryObject.addMeasurement(estDiaMeasure);

                summaryObjects.put(summaryObject.getID(), summaryObject);

            }
        }

        // Displaying the number of objects detected
        if (verbose) {
            System.out.println("["+moduleName+"] "+outputObjects.size()+" spots detected");

            if (createSummary) {
                System.out.println("["+moduleName+"] "+summaryObjects.size()+" tracks detected");

            }
        }

        // Adding objects to the workspace
        if (verbose) System.out.println("["+moduleName+"] Adding objects ("+outputObjectsName.getName()+") to workspace");
        workspace.addObjects(outputObjects);

        if (createSummary) workspace.addObjects(summaryObjects);

        // Displaying objects (if selected)
        if (parameters.getValue(SHOW_OBJECTS)) {
            // Creating a duplicate of the input image
            ipl = new Duplicator().run(ipl);

            // Getting parameters
            boolean showID = parameters.getValue(SHOW_ID);
            boolean useGroupID = false;
            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(ID_MODE).equals(USE_TRACK_ID)) {
                    useGroupID = true;
                }
            }

//            // Creating the overlay
//            ShowObjectsOverlay.createOverlay(ipl,outputObjects,showID,useGroupID);

            // Displaying the overlay
            ipl.show();

        }

        // Reapplying calibration to input image
        ipl.setCalibration(calibration);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter( OUTPUT_SPOT_OBJECTS,HCParameter.OUTPUT_OBJECTS,new HCName("Spots")));

        parameters.addParameter(new HCParameter(CALIBRATED_UNITS,HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(DO_SUBPIXEL_LOCALIZATION,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(DO_MEDIAN_FILTERING,HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(RADIUS,HCParameter.DOUBLE,2.0));
        parameters.addParameter(new HCParameter(THRESHOLD,HCParameter.DOUBLE,5000.0));

        parameters.addParameter(new HCParameter(DO_TRACKING,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(LINKING_MAX_DISTANCE,HCParameter.DOUBLE,2.0));
        parameters.addParameter(new HCParameter(GAP_CLOSING_MAX_DISTANCE,HCParameter.DOUBLE,2.0));
        parameters.addParameter(new HCParameter(MAX_FRAME_GAP,HCParameter.INTEGER,3));

        parameters.addParameter(new HCParameter(CREATE_TRACK_OBJECTS,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(OUTPUT_TRACK_OBJECTS,HCParameter.OUTPUT_OBJECTS,new HCName("Tracks")));

        parameters.addParameter(new HCParameter(SHOW_OBJECTS,HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(SHOW_ID,HCParameter.BOOLEAN,false));
        parameters.addParameter(new HCParameter(ID_MODE,HCParameter.CHOICE_ARRAY,ID_MODES[0],ID_MODES));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
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
    public void addMeasurements(HCMeasurementCollection measurements) {
        if (parameters.getValue(OUTPUT_SPOT_OBJECTS) != null) {
            measurements.addMeasurement(parameters.getValue(OUTPUT_SPOT_OBJECTS),HCMeasurement.RADIUS);
            measurements.addMeasurement(parameters.getValue(OUTPUT_SPOT_OBJECTS),HCMeasurement.ESTIMATED_DIAMETER);
        }

        if (parameters.getValue(CREATE_TRACK_OBJECTS)) {
            if (parameters.getValue(OUTPUT_TRACK_OBJECTS) != null) {
                measurements.addMeasurement(parameters.getValue(OUTPUT_TRACK_OBJECTS),HCMeasurement.RADIUS);
                measurements.addMeasurement(parameters.getValue(OUTPUT_TRACK_OBJECTS),HCMeasurement.ESTIMATED_DIAMETER);
            }
        }

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {
        if (parameters.getValue(CREATE_TRACK_OBJECTS)) {
            relationships.addRelationship(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS));

        }
    }
}
