// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package wbif.sjx.MIA.Module.ObjectProcessing.Identification;

import fiji.plugin.trackmate.*;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.features.spot.SpotRadiusEstimatorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.Deprecated.AddObjectsOverlay;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Exceptions.IntegerOverflowException;
import wbif.sjx.common.Process.IntensityMinMax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sc13967 on 15/05/2017.
 */
public class RunTrackMate extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_SPOT_OBJECTS = "Output spot objects";

    public static final String SPOT_SEPARATOR = "Spot detection";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String DO_SUBPIXEL_LOCALIZATION = "Do sub-pixel localisation";
    public static final String DO_MEDIAN_FILTERING = "Median filtering";
    public static final String RADIUS = "Radius";
    public static final String THRESHOLD = "Threshold";
    public static final String NORMALISE_INTENSITY = "Normalise intensity";
    public static final String ESTIMATE_SIZE = "Estimate spot size";

    public static final String TRACK_SEPARATOR = "Spot tracking";
    public static final String DO_TRACKING = "Run tracking";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";


    public interface Measurements {
        String RADIUS_PX = "SPOT_DETECT_TRACK // RADIUS_(PX)";
        String RADIUS_CAL = "SPOT_DETECT_TRACK // RADIUS_(${CAL})";
        String ESTIMATED_DIAMETER_PX = "SPOT_DETECT_TRACK // EST_DIAMETER_(PX)";
        String ESTIMATED_DIAMETER_CAL = "SPOT_DETECT_TRACK // EST_DIAMETER_(${CAL})";

    }


    public Settings initialiseSettings(ImagePlus ipl, Calibration calibration) {
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);

        // Applying conversion to parameters
        if (calibratedUnits) {
            radius = calibration.getRawX(radius);
            maxLinkDist = calibration.getRawX(maxLinkDist);
            maxGapDist = calibration.getRawX(maxGapDist);
        }

        // Initialising settings for TrackMate
        Settings settings = new Settings();

        settings.setFrom(ipl);

        settings.detectorFactory = new LogDetectorFactory();
        settings.detectorSettings.put(DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalisation);
        settings.detectorSettings.put(DetectorKeys.KEY_DO_MEDIAN_FILTERING, medianFiltering);
        settings.detectorSettings.put(DetectorKeys.KEY_RADIUS, radius);
        settings.detectorSettings.put(DetectorKeys.KEY_THRESHOLD, threshold);
        settings.detectorSettings.put(DetectorKeys.KEY_TARGET_CHANNEL, 1);

        settings.addSpotAnalyzerFactory(new SpotRadiusEstimatorFactory<>());

        settings.trackerFactory  = new SparseLAPTrackerFactory();
        settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
        settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, false);
        settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, false);
        settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkDist);
        settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, maxGapDist);
        settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP,maxFrameGap);

        return settings;

    }

    public ObjCollection getSpots(Model model, Calibration calibration, boolean is2D) throws IntegerOverflowException {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        // Getting trackObjects and adding them to the output trackObjects
        writeMessage("Processing detected objects");

        // Getting calibration
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        ObjCollection spotObjects = new ObjCollection(spotObjectsName);
        SpotCollection spots = model.getSpots();
        for (Spot spot:spots.iterable(false)) {
            Obj spotObject = new Obj(spotObjectsName,spot.ID(),dppXY,dppZ,calibrationUnits,is2D);
            spotObject.addCoord((int) spot.getDoublePosition(0),(int) spot.getDoublePosition(1),(int) spot.getDoublePosition(2));
            spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

            spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS),this));
            spotObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_CAL),spot.getFeature(Spot.RADIUS)*dppXY,this));
            spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));
            spotObject.addMeasurement(new Measurement(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL),spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY,this));

            spotObjects.add(spotObject);

        }

        // Adding spotObjects to the workspace
        writeMessage(spots.getNSpots(false)+" trackObjects detected");

        return spotObjects;

    }

    public ObjCollection[] getSpotsAndTracks(Model model, Calibration calibration, boolean is2D) throws IntegerOverflowException {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);

        // Getting calibration
        double dppXY = calibration.getX(1);
        double dppZ = calibration.getZ(1);
        String calibrationUnits = calibration.getUnits();

        ObjCollection spotObjects = new ObjCollection(spotObjectsName);
        ObjCollection trackObjects = new ObjCollection(trackObjectsName);

        // Converting tracks to local track model
        writeMessage("Converting tracks to local track model");
        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        for (Integer trackID : trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = new Obj(trackObjectsName, trackID, dppXY, dppZ, calibrationUnits,is2D);
            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot : spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and group (track) ID.
                Obj spotObject = new Obj(spotObjectsName, spotObjects.getAndIncrementID(), dppXY, dppZ, calibrationUnits,is2D);

                spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX,spot.getFeature(Spot.RADIUS),this));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.RADIUS_CAL),spot.getFeature(Spot.RADIUS)*dppXY,this));
                spotObject.addMeasurement(new Measurement(Measurements.ESTIMATED_DIAMETER_PX,spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER),this));
                spotObject.addMeasurement(new Measurement(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL),spot.getFeature(SpotRadiusEstimatorFactory.ESTIMATED_DIAMETER)*dppXY,this));

                // Getting coordinates
                int x = (int) Math.round(spot.getDoublePosition(0));
                int y = (int) Math.round(spot.getDoublePosition(1));
                int z = (int) Math.round(spot.getDoublePosition(2) * dppZ / dppXY);
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                spotObject.addCoord(x, y, z);
                spotObject.setT(t);

                // If necessary, adding coordinates to the summary objects
                trackObject.addCoord(x, y, z);
                trackObject.setT(0);

                // Adding the connection between instance and summary objects
                spotObject.addParent(trackObject);
                trackObject.addChild(spotObject);

                // Adding the instance object to the relevant collection
                spotObjects.add(spotObject);
                trackObjects.add(trackObject);

            }
        }

        // Displaying the number of objects detected
        writeMessage(spotObjects.size() + " spots detected");
        writeMessage(trackObjects.size() + " tracks detected");

        return new ObjCollection[]{spotObjects,trackObjects};

    }

    public void estimateSpotSize(ObjCollection spotObjects, ImagePlus ipl) throws IntegerOverflowException {
        ObjCollection volumeObjects = new GetLocalObjectRegion().getLocalRegions(spotObjects, "SpotVolume",ipl,true,Measurements.RADIUS_PX,0,false);

        // Replacing spot volumes with explicit volume
        for (Obj spotObject:spotObjects.values()) {
            Obj spotVolumeObject = spotObject.getChildren("SpotVolume").values().iterator().next();

            spotObject.setPoints(spotVolumeObject.getPoints());

        }
    }

    public void showObjects(ImagePlus ipl, ObjCollection spotObjects) {
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean doTracking = parameters.getValue(DO_TRACKING);

        HashMap<Integer, Float> hues;
        // Colours will depend on the detection/tracking mode
        if (doTracking) {
            hues = ColourFactory.getParentIDHues(spotObjects,trackObjectsName,true);
        } else {
            hues = ColourFactory.getSingleColourHues(spotObjects,ColourFactory.SingleColours.ORANGE);
        }

        // Creating a duplicate of the input image
        ipl = new Duplicator().run(ipl);
        IntensityMinMax.run(ipl,true);

        // Adding the overlay
        try {
            new AddObjectsOverlay().createCentroidOverlay(ipl,spotObjects,hues,false,0.2,false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Displaying the overlay
        ipl.show();

    }


    @Override
    public String getTitle() {
        return "Run TrackMate";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_IDENTIFICATION;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Storing, then removing calibration.  This will be reapplied after the detection.
        Calibration calibration = ipl.getCalibration();
        ipl.setCalibration(null);

        // Getting parameters
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        boolean normaliseIntensity = parameters.getValue(NORMALISE_INTENSITY);
        boolean doTracking = parameters.getValue(DO_TRACKING);
        boolean estimateSize = parameters.getValue(ESTIMATE_SIZE);

        // If image should be normalised
        if (normaliseIntensity) {
            ipl = new Duplicator().run(ipl);
            IntensityMinMax.run(ipl,true);
        }

        // Initialising TrackMate model to store data
        Model model = new Model();
        model.setLogger(Logger.VOID_LOGGER);
        Settings settings = initialiseSettings(ipl,calibration);
        TrackMate trackmate = new TrackMate(model, settings);

        // Resetting ipl to the input image
        ipl = inputImage.getImagePlus();

        ObjCollection spotObjects;
        try {
            if (doTracking) {
                writeMessage("Running detection and tracking");
                if (!trackmate.process()) System.err.println(trackmate.getErrorMessage());

                ObjCollection[] spotsAndTracks = getSpotsAndTracks(model, calibration, ipl.getNSlices() == 1);
                spotObjects = spotsAndTracks[0];
                ObjCollection trackObjects = spotsAndTracks[1];

                if (estimateSize) estimateSpotSize(spotObjects, ipl);

                // Adding objects to the workspace
                workspace.addObjects(spotObjects);
                workspace.addObjects(trackObjects);

            } else {
                writeMessage("Running detection only");
                if (!trackmate.checkInput()) System.err.println(trackmate.getErrorMessage());
                if (!trackmate.execDetection()) System.err.println(trackmate.getErrorMessage());
                if (!trackmate.computeSpotFeatures(false)) System.err.println(trackmate.getErrorMessage());

                spotObjects = getSpots(model, calibration, ipl.getNSlices() == 1);

                if (estimateSize) estimateSpotSize(spotObjects, ipl);

                workspace.addObjects(spotObjects);

            }
        } catch (IntegerOverflowException e) {
            return false;
        }

        // Displaying objects (if selected)
        if (showOutput) showObjects(ipl,spotObjects);

        // Reapplying calibration to input image
        inputImage.getImagePlus().setCalibration(calibration);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new OutputObjectsP(OUTPUT_SPOT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(SPOT_SEPARATOR, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this,false));
        parameters.add(new BooleanP(DO_SUBPIXEL_LOCALIZATION, this,true));
        parameters.add(new BooleanP(DO_MEDIAN_FILTERING, this,false));
        parameters.add(new DoubleP(RADIUS, this,2.0));
        parameters.add(new DoubleP(THRESHOLD, this,5000.0));
        parameters.add(new BooleanP(NORMALISE_INTENSITY, this,false));
        parameters.add(new BooleanP(ESTIMATE_SIZE, this,false));

        parameters.add(new ParamSeparatorP(TRACK_SEPARATOR, this));
        parameters.add(new BooleanP(DO_TRACKING, this,true));
        parameters.add(new DoubleP(LINKING_MAX_DISTANCE, this,2.0));
        parameters.add(new DoubleP(GAP_CLOSING_MAX_DISTANCE, this,2.0));
        parameters.add(new IntegerP(MAX_FRAME_GAP, this,3));
        parameters.add(new OutputObjectsP(OUTPUT_TRACK_OBJECTS, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.add(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(THRESHOLD));
        returnedParameters.add(parameters.getParameter(NORMALISE_INTENSITY));
        returnedParameters.add(parameters.getParameter(ESTIMATE_SIZE));

        returnedParameters.add(parameters.getParameter(TRACK_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DO_TRACKING));
        if (parameters.getValue(DO_TRACKING)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.getParameter(LINKING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(GAP_CLOSING_MAX_DISTANCE));
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        objectMeasurementRefs.setAllCalculated(false);

        String outputSpotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        MeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_PX);
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.RADIUS_CAL));
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Measurements.ESTIMATED_DIAMETER_PX);
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        reference = objectMeasurementRefs.getOrPut(Units.replace(Measurements.ESTIMATED_DIAMETER_CAL));
        reference.setImageObjName(outputSpotObjectsName);
        reference.setCalculated(true);

        return objectMeasurementRefs;

    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        RelationshipCollection relationships = new RelationshipCollection();
        if (parameters.getValue(DO_TRACKING)) {
            relationships.addRelationship(parameters.getValue(OUTPUT_TRACK_OBJECTS), parameters.getValue(OUTPUT_SPOT_OBJECTS));

        }

        return relationships;

    }

}

