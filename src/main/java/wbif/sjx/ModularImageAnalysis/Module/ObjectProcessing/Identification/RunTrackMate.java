// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Identification;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.NormaliseIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.AddObjectsOverlay;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class RunTrackMate extends Module {
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
    public static final String ESTIMATE_SIZE = "Estimate spot size";
    public static final String DO_TRACKING = "Run tracking";
    public static final String CREATE_TRACK_OBJECTS = "Create track objects";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String SHOW_OBJECTS = "Show objects";
    public static final String SHOW_ID = "Show ID";
    public static final String ID_MODE = "ID source";

    public interface IDModes {
        String USE_SPOT_ID = "Use spot ID";
        String USE_TRACK_ID = "Use track ID";

        String[] ALL = new String[]{USE_SPOT_ID, USE_TRACK_ID};

    }

    private interface Measurements {
        String RADIUS = "SPOT_DETECT_TRACK//RADIUS";
        String ESTIMATED_DIAMETER = "SPOT_DETECT_TRACK//EST_DIAMETER";

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
    public void run(Workspace workspace) {
        // Loading input image
        String targetImageName = parameters.getValue(INPUT_IMAGE);
        writeMessage("Loading image ("+targetImageName+") into workspace");
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
        boolean estimateSize = parameters.getValue(ESTIMATE_SIZE);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);
        boolean showID = parameters.getValue(SHOW_ID);

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
        writeMessage("Running TrackMate detection");
        if (!trackmate.checkInput()) IJ.log(trackmate.getErrorMessage());
        if (!trackmate.execDetection()) IJ.log(trackmate.getErrorMessage());
        if (!trackmate.computeSpotFeatures(false)) IJ.log(trackmate.getErrorMessage());

        // Re-applying the spatial calibration
        ipl.setCalibration(calibration);

        if (normaliseIntensity) ipl = targetImage.getImagePlus();

        if (!(boolean) parameters.getValue(DO_TRACKING)) {
            // Getting trackObjects and adding them to the output trackObjects
            writeMessage("Processing detected objects");

            SpotCollection spots = model.getSpots();
            for (Spot spot:spots.iterable(false)) {
                Obj spotObject = new Obj(spotObjectsName,spot.ID(),dppXY,dppZ,calibrationUnits);
                spotObject.addCoord((int) spot.getDoublePosition(0),(int) spot.getDoublePosition(1),(int) spot.getDoublePosition(2));
                spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

                spotObject.addMeasurement(new Measurement(Measurements.RADIUS,spot.getFeature(Spot.RADIUS),this));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));

                spotObjects.put(spotObject.getID(),spotObject);

            }

            // Adding explicit volume to spots
            if (estimateSize) {
                GetLocalObjectRegion.getLocalRegions(spotObjects,"SpotVolume",0,false,true,Measurements.RADIUS);

                // Replacing spot volumes with explicit volume
                for (Obj spotObject:spotObjects.values()) {
                    Obj spotVolumeObject = spotObject.getChildren("SpotVolume").values().iterator().next();

                    spotObject.setPoints(spotVolumeObject.getPoints());
                }
            }

            // Adding spotObjects to the workspace
            writeMessage(spots.getNSpots(false)+" trackObjects detected");
            writeMessage("Adding spotObjects ("+spotObjectsName+") to workspace");
            workspace.addObjects(spotObjects);

            // Displaying trackObjects (if selected)
            if (parameters.getValue(SHOW_OBJECTS)) {
                ipl = new Duplicator().run(ipl);
                IntensityMinMax.run(ipl,true);
                String colourMode = ObjCollection.ColourModes.RANDOM_COLOUR;
                HashMap<Integer,Float> hues = spotObjects.getHue(colourMode,"",true);
                String labelMode = ObjCollection.LabelModes.ID;
                HashMap<Integer,String> IDs = showID ? spotObjects.getIDs(labelMode,"",0,false) : null;
                new AddObjectsOverlay().createOverlay(
                        ipl,spotObjects, AddObjectsOverlay.PositionModes.CENTROID,null,hues,IDs,8,1);

                // Displaying the overlay
                ipl.show();

            }

            return;
        }

        writeMessage("Running TrackMate tracking");
        if (!trackmate.execTracking()) IJ.log(trackmate.getErrorMessage());

        // Converting tracks to local track model
        writeMessage("Converting tracks to local track model");

        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);
        for (Integer trackID:trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = createTracks ? new Obj(trackObjectsName,trackID,dppXY,dppZ,calibrationUnits) : null;
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
                Obj spotObject = new Obj(spotObjectsName,spotObjects.getNextID(),dppXY,dppZ,calibrationUnits);

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

                    // Adding the connection between instance and summary objects
                    spotObject.addParent(trackObject);
                    trackObject.addChild(spotObject);
                }

                // Adding the instance object to the relevant collection
                spotObjects.put(spotObject.getID(),spotObject);

            }
        }

        // Adding explicit volume to spots
        if (estimateSize) {
            ObjCollection spotVolumeObjects = GetLocalObjectRegion.getLocalRegions(spotObjects,"SpotVolume",0,false,true,Measurements.ESTIMATED_DIAMETER);

            // Replacing spot volumes with explicit volume
            for (Obj spotObject:spotObjects.values()) {
                Obj spotVolumeObject = spotObject.getParent("SpotVolume");
                spotObject.setPoints(spotVolumeObject.getPoints());
            }
        }

        // Displaying the number of objects detected
        writeMessage(spotObjects.size()+" spots detected");
            if (createTracks) writeMessage(trackObjects.size()+" tracks detected");

        // Adding objects to the workspace
        writeMessage("Adding objects ("+spotObjectsName+") to workspace");
        workspace.addObjects(spotObjects);
        if (createTracks) workspace.addObjects(trackObjects);

        // Displaying objects (if selected)
        if (parameters.getValue(SHOW_OBJECTS)) {
            // Creating a duplicate of the input image
            ipl = new Duplicator().run(ipl);

            // Getting parameters
            boolean useTrackID = false;
            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(ID_MODE).equals(IDModes.USE_TRACK_ID)) {
                    useTrackID = true;
                }
            }

            // Creating the overlay
            String colourMode = ObjCollection.ColourModes.PARENT_ID;
            HashMap<Integer,Float> hues = spotObjects.getHue(colourMode,trackObjectsName,true);
            String labelMode = ObjCollection.LabelModes.PARENT_ID;
            HashMap<Integer,String> IDs = showID ? spotObjects.getIDs(labelMode,trackObjectsName,0,false) : null;
            new AddObjectsOverlay().createOverlay(
                    ipl,spotObjects, AddObjectsOverlay.PositionModes.CENTROID,null,hues,IDs,8,1);

            // Displaying the overlay
            ipl.show();

        }

        // Reapplying calibration to input image
        targetImage.getImagePlus().setCalibration(calibration);

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_SPOT_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Spots")));

        parameters.add(new Parameter(CALIBRATED_UNITS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(DO_SUBPIXEL_LOCALIZATION, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(DO_MEDIAN_FILTERING, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(RADIUS, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(THRESHOLD, Parameter.DOUBLE,5000.0));
        parameters.add(new Parameter(NORMALISE_INTENSITY, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ESTIMATE_SIZE, Parameter.BOOLEAN,false));

        parameters.add(new Parameter(DO_TRACKING, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(LINKING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(GAP_CLOSING_MAX_DISTANCE, Parameter.DOUBLE,2.0));
        parameters.add(new Parameter(MAX_FRAME_GAP, Parameter.INTEGER,3));

        parameters.add(new Parameter(CREATE_TRACK_OBJECTS, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_TRACK_OBJECTS, Parameter.OUTPUT_OBJECTS,new String("Tracks")));

        parameters.add(new Parameter(SHOW_OBJECTS, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(SHOW_ID, Parameter.BOOLEAN,false));
        parameters.add(new Parameter(ID_MODE, Parameter.CHOICE_ARRAY,IDModes.USE_SPOT_ID,IDModes.ALL));

    }

    @Override
    protected void initialiseMeasurementReferences() {
        objectMeasurementReferences.add(new MeasurementReference(Measurements.RADIUS));
        objectMeasurementReferences.add(new MeasurementReference(Measurements.ESTIMATED_DIAMETER));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.add(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(THRESHOLD));
        returnedParameters.add(parameters.getParameter(NORMALISE_INTENSITY));
        returnedParameters.add(parameters.getParameter(ESTIMATE_SIZE));

        returnedParameters.add(parameters.getParameter(DO_TRACKING));
        if (parameters.getValue(DO_TRACKING)) {
            returnedParameters.add(parameters.getParameter(LINKING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(GAP_CLOSING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));

            returnedParameters.add(parameters.getParameter(CREATE_TRACK_OBJECTS));
            if (parameters.getValue(CREATE_TRACK_OBJECTS)) {
                returnedParameters.add(parameters.getParameter(OUTPUT_TRACK_OBJECTS));

            }
        }

        returnedParameters.add(parameters.getParameter(SHOW_OBJECTS));
        if (parameters.getValue(SHOW_OBJECTS)) {
            returnedParameters.add(parameters.getParameter(SHOW_ID));

            if (parameters.getValue(DO_TRACKING)) {
                if (parameters.getValue(SHOW_ID)) {
                    returnedParameters.add(parameters.getParameter(ID_MODE));

                }
            }
        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
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

