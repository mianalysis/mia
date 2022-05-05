// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.TrackModel;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import fiji.plugin.trackmate.tracking.LAPUtils;
import fiji.plugin.trackmate.tracking.TrackerKeys;
import fiji.plugin.trackmate.tracking.kalman.KalmanTrackerFactory;
import fiji.plugin.trackmate.tracking.sparselap.SparseLAPTrackerFactory;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.GetLocalObjectRegion;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectCentroid;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.objects.OutputTrackObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.sjcommon.exceptions.IntegerOverflowException;
import io.github.sjcross.sjcommon.object.Point;
import io.github.sjcross.sjcommon.object.volume.PointOutOfRangeException;
import io.github.sjcross.sjcommon.object.volume.SpatCal;
import io.github.sjcross.sjcommon.object.volume.VolumeType;
import io.github.sjcross.sjcommon.process.IntensityMinMax;

/**
 * Created by sc13967 on 15/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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
    public static final String ESTIMATE_SIZE = "Estimate spot size";

    public static final String TRACK_SEPARATOR = "Spot tracking";
    public static final String DO_TRACKING = "Run tracking";
    public static final String TRACKING_METHOD = "Tracking method";
    public static final String OUTPUT_TRACK_OBJECTS = "Output track objects";
    public static final String LINKING_MAX_DISTANCE = "Max linking distance";
    public static final String INITIAL_SEARCH_RADIUS = "Initial search radius";
    public static final String SEARCH_RADIUS = "Search radius";
    public static final String GAP_CLOSING_MAX_DISTANCE = "Gap closing max distance";
    public static final String MAX_FRAME_GAP = "Max frame gap";

    public interface TrackingMethods {
        String KALMAN = "Linear motion (Kalman)";
        String SIMPLE = "Simple";

        String[] ALL = new String[] { KALMAN, SIMPLE };

    }

    public interface Measurements {
        String RADIUS_PX = "SPOT_DETECT_TRACK // RADIUS_(PX)";
        String RADIUS_CAL = "SPOT_DETECT_TRACK // RADIUS_(${SCAL})";
        String QUALITY = "SPOT_DETECT_TRACK // QUALITY";
        String X_CENTROID_PX = "SPOT_DETECT_TRACK // X_CENTROID_(PX)";
        String X_CENTROID_CAL = "SPOT_DETECT_TRACK // X_CENTROID_(${SCAL})";
        String Y_CENTROID_PX = "SPOT_DETECT_TRACK // Y_CENTROID_(PX)";
        String Y_CENTROID_CAL = "SPOT_DETECT_TRACK // Y_CENTROID_(${SCAL})";
        String Z_CENTROID_SLICE = "SPOT_DETECT_TRACK // Z_CENTROID_(SLICE)";
        String Z_CENTROID_CAL = "SPOT_DETECT_TRACK // Z_CENTROID_(${SCAL})";

    }

    public RunTrackMate(Modules modules) {
        super("Run TrackMate", modules);
        deprecated = true;
    }

    public Settings initialiseSettings(ImagePlus ipl, SpatCal calibration) {
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);
        String trackingMethod = parameters.getValue(TRACKING_METHOD);
        double maxLinkDist = parameters.getValue(LINKING_MAX_DISTANCE);
        double maxGapDist = parameters.getValue(GAP_CLOSING_MAX_DISTANCE);
        double initialSearchRadius = parameters.getValue(INITIAL_SEARCH_RADIUS);
        double searchRadius = parameters.getValue(SEARCH_RADIUS);
        int maxFrameGap = parameters.getValue(MAX_FRAME_GAP);

        // Applying conversion to parameters
        if (calibratedUnits) {
            radius = radius / calibration.getDppXY();
            maxLinkDist = maxLinkDist / calibration.getDppXY();
            maxGapDist = maxGapDist / calibration.getDppXY();
        }

        // Initialising settings for TrackMate
        Settings settings = new Settings(ipl);

        settings.detectorFactory = new LogDetectorFactory();
        settings.detectorSettings.put(DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalisation);
        settings.detectorSettings.put(DetectorKeys.KEY_DO_MEDIAN_FILTERING, medianFiltering);
        settings.detectorSettings.put(DetectorKeys.KEY_RADIUS, radius);
        settings.detectorSettings.put(DetectorKeys.KEY_THRESHOLD, threshold);
        settings.detectorSettings.put(DetectorKeys.KEY_TARGET_CHANNEL, 1);

        switch (trackingMethod) {
            case TrackingMethods.KALMAN:
                settings.trackerFactory = new KalmanTrackerFactory();
                settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
                settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, initialSearchRadius);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGap);
                settings.trackerSettings.put(KalmanTrackerFactory.KEY_KALMAN_SEARCH_RADIUS, searchRadius);
                break;
            case TrackingMethods.SIMPLE:
                settings.trackerFactory = new SparseLAPTrackerFactory();
                settings.trackerSettings = LAPUtils.getDefaultLAPSettingsMap();
                settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_SPLITTING, false);
                settings.trackerSettings.put(TrackerKeys.KEY_ALLOW_TRACK_MERGING, false);
                settings.trackerSettings.put(TrackerKeys.KEY_LINKING_MAX_DISTANCE, maxLinkDist);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE, maxGapDist);
                settings.trackerSettings.put(TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP, maxFrameGap);
                break;
        }

        return settings;

    }

    public Objs getSpots(Model model, SpatCal calibration, int nFrames, double frameInterval)
            throws IntegerOverflowException {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        boolean doSubpixel = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);

        // Getting trackObjects and adding them to the output trackObjects
        writeStatus("Processing detected objects");

        Objs spotObjects = new Objs(spotObjectsName, calibration, nFrames, frameInterval, TemporalUnit.getOMEUnit());
        SpotCollection spots = model.getSpots();
        for (Spot spot : spots.iterable(false)) {
            Obj spotObject = spotObjects.createAndAddNewObject(VolumeType.POINTLIST, spot.ID());
            try {
                spotObject.add((int) spot.getDoublePosition(0), (int) spot.getDoublePosition(1),
                        (int) spot.getDoublePosition(2));
            } catch (PointOutOfRangeException e) {
                MIA.log.writeError(e);
            }
            spotObject.setT((int) Math.round(spot.getFeature(Spot.FRAME)));

            addSpotMeasurements(spotObject, spot, doSubpixel);

        }

        // Adding spotObjects to the workspace
        writeStatus(spots.getNSpots(false) + " trackObjects detected");

        return spotObjects;

    }

    public Objs[] getSpotsAndTracks(Model model, SpatCal calibration, int nFrames, double frameInterval)
            throws IntegerOverflowException {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean doSubpixel = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);

        // Getting calibration
        Objs spotObjects = new Objs(spotObjectsName, calibration, nFrames, frameInterval, TemporalUnit.getOMEUnit());
        Objs trackObjects = new Objs(trackObjectsName, calibration, nFrames, frameInterval, TemporalUnit.getOMEUnit());

        // Converting tracks to local track model
        writeStatus("Converting tracks to local track model");
        TrackModel trackModel = model.getTrackModel();
        Set<Integer> trackIDs = trackModel.trackIDs(false);

        double dppXY = calibration.getDppXY();
        double dppZ = calibration.getDppZ();

        for (Integer trackID : trackIDs) {
            // If necessary, creating a new summary object for the track
            Obj trackObject = trackObjects.createAndAddNewObject(VolumeType.POINTLIST, trackID);
            ArrayList<Spot> spots = new ArrayList<>(trackModel.trackSpots(trackID));

            // Sorting spots based on frame number
            spots.sort((o1, o2) -> {
                double t1 = o1.getFeature(Spot.FRAME);
                double t2 = o2.getFeature(Spot.FRAME);
                return t1 > t2 ? 1 : t1 == t2 ? 0 : -1;
            });

            // Getting x,y,f and 2-channel spot intensities from TrackMate results
            for (Spot spot : spots) {
                // Initialising a new HCObject to store this track and assigning a unique ID and
                // group (track) ID.
                Obj spotObject = spotObjects.createAndAddNewObject(trackObject.getVolumeType());

                // Adding measurements
                addSpotMeasurements(spotObject, spot, doSubpixel);

                // Getting coordinates
                int x = (int) Math.round(spot.getDoublePosition(0));
                int y = (int) Math.round(spot.getDoublePosition(1));
                int z = (int) Math.round(spot.getDoublePosition(2) * dppZ / dppXY);
                int t = (int) Math.round(spot.getFeature(Spot.FRAME));

                // Adding coordinates to the instance objects
                try {
                    spotObject.add(x, y, z);
                } catch (PointOutOfRangeException e) {
                    continue;
                }
                spotObject.setT(t);

                // Adding the connection between instance and summary objects
                spotObject.addParent(trackObject);
                trackObject.addChild(spotObject);

            }
        }

        // Displaying the number of objects detected
        writeStatus(spotObjects.size() + " spots detected");
        writeStatus(trackObjects.size() + " tracks detected");

        return new Objs[] { spotObjects, trackObjects };

    }

    void addSpotMeasurements(Obj spotObject, Spot spot, boolean doSubpixel) {
        double dppXY = spotObject.getDppXY();
        double dppZ = spotObject.getDppZ();

        spotObject.addMeasurement(new Measurement(Measurements.RADIUS_PX, spot.getFeature(Spot.RADIUS)));
        spotObject.addMeasurement(new Measurement(Measurements.RADIUS_CAL, spot.getFeature(Spot.RADIUS) * dppXY));

        if (doSubpixel) {
            spotObject.addMeasurement(new Measurement(Measurements.X_CENTROID_PX, spot.getFeature(Spot.POSITION_X)));
            spotObject.addMeasurement(
                    new Measurement(Measurements.X_CENTROID_CAL, spot.getFeature(Spot.POSITION_X) * dppXY));

            spotObject.addMeasurement(new Measurement(Measurements.Y_CENTROID_PX, spot.getFeature(Spot.POSITION_Y)));
            spotObject.addMeasurement(
                    new Measurement(Measurements.Y_CENTROID_CAL, spot.getFeature(Spot.POSITION_Y) * dppXY));

            spotObject.addMeasurement(new Measurement(Measurements.Z_CENTROID_SLICE, spot.getFeature(Spot.POSITION_Z)));
            spotObject.addMeasurement(
                    new Measurement(Measurements.Z_CENTROID_CAL, spot.getFeature(Spot.POSITION_Z) * dppZ));
        }
    }

    public void estimateSpotSize(Objs spotObjects, ImagePlus ipl) throws IntegerOverflowException {
        Objs tempObjects = new Objs("SpotVolume", spotObjects);
        // Replacing spot volumes with explicit volume
        for (Obj spotObject : spotObjects.values()) {
            int radius = (int) Math.round(spotObject.getMeasurement(Measurements.RADIUS_PX).getValue());
            Point<Double> cent = spotObject.getMeanCentroid(true, false);
            int[] centroid = new int[] { (int) Math.round(cent.getX()), (int) Math.round(cent.getY()),
                    (int) Math.round(cent.getZ()) };
            Obj volumeObject = GetLocalObjectRegion.getLocalRegion(spotObject, tempObjects, centroid, radius, false);
            spotObject.getCoordinateSet().clear();
            spotObject.getCoordinateSet().addAll(volumeObject.getCoordinateSet());
            spotObject.clearSurface();
            spotObject.clearCentroid();
            spotObject.clearProjected();
            spotObject.clearROIs();

        }
    }

    public void showObjects(ImagePlus ipl, Objs spotObjects, boolean estimateSize) {
        String trackObjectsName = parameters.getValue(OUTPUT_TRACK_OBJECTS);
        boolean doTracking = parameters.getValue(DO_TRACKING);

        HashMap<Integer, Float> hues;
        // Colours will depend on the detection/tracking mode
        if (doTracking) {
            hues = ColourFactory.getParentIDHues(spotObjects, trackObjectsName, true);
        } else {
            hues = ColourFactory.getSingleColourValues(spotObjects, ColourFactory.SingleColours.ORANGE);
        }
        HashMap<Integer, Color> colours = ColourFactory.getColours(hues);

        String pointSize = AddObjectCentroid.PointSizes.SMALL;
        String pointType = AddObjectCentroid.PointTypes.CIRCLE;

        // Creating a duplicate of the input image
        ipl = new Duplicator().run(ipl);
        IntensityMinMax.run(ipl, true);

        // Adding the overlay
        if (estimateSize)
            AddObjectOutline.addOverlay(ipl, spotObjects, 1, 1, colours, false, true);
        else
            AddObjectCentroid.addOverlay(ipl, spotObjects, colours, pointSize, pointType, false, true);

        ipl.setPosition(1, 1, 1);
        ipl.updateChannelAndDraw();

        // Displaying the overlay
        ipl.show();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getDescription() {
        return "Uses the TrackMate plugin included with Fiji to detect and track spots in images.  For more information, see the <a href=\"https://imagej.net/TrackMate\">TrackMate</a> documentation.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Loading input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImagePlus ipl = inputImage.getImagePlus();

        // Storing, then removing calibration. This will be reapplied after the
        // detection.
        SpatCal calibration = SpatCal.getFromImage(ipl);
        Calibration cal = ipl.getCalibration();
        ipl.setCalibration(null);
        int nFrames = ipl.getNFrames();
        double frameInterval = cal.frameInterval;

        // Getting parameters
        boolean doTracking = parameters.getValue(DO_TRACKING);
        boolean estimateSize = parameters.getValue(ESTIMATE_SIZE);

        // Initialising TrackMate model to store data
        Model model = new Model();
        model.setLogger(Logger.VOID_LOGGER);
        Settings settings = initialiseSettings(ipl, calibration);
        TrackMate trackmate = new TrackMate(model, settings);

        // Resetting ipl to the input image
        ipl = inputImage.getImagePlus();

        Objs spotObjects;
        try {
            if (doTracking) {
                writeStatus("Running detection and tracking");
                trackmate.process();

                Objs[] spotsAndTracks = getSpotsAndTracks(model, calibration, nFrames, frameInterval);
                spotObjects = spotsAndTracks[0];
                Objs trackObjects = spotsAndTracks[1];

                if (estimateSize)
                    estimateSpotSize(spotObjects, ipl);

                // Adding objects to the workspace
                workspace.addObjects(spotObjects);
                workspace.addObjects(trackObjects);

            } else {
                writeStatus("Running detection only");
                if (!trackmate.checkInput())
                    MIA.log.writeError(trackmate.getErrorMessage());
                if (!trackmate.execDetection())
                    MIA.log.writeError(trackmate.getErrorMessage());
                if (!trackmate.computeSpotFeatures(false))
                    MIA.log.writeError(trackmate.getErrorMessage());

                spotObjects = getSpots(model, calibration, nFrames, frameInterval);

                if (estimateSize)
                    estimateSpotSize(spotObjects, ipl);

                workspace.addObjects(spotObjects);

            }
        } catch (IntegerOverflowException e) {
            return Status.FAIL;
        }

        // Displaying objects (if selected)
        if (showOutput)
            showObjects(ipl, spotObjects, estimateSize);

        // Reapplying calibration to input image
        inputImage.getImagePlus().setCalibration(cal);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image in which to detect spots."));
        parameters.add(new OutputObjectsP(OUTPUT_SPOT_OBJECTS, this, "",
                "Spot objects that will be added to the workspace.  If tracking is enabled, each spot will have a parent track object."));

        parameters.add(new SeparatorP(SPOT_SEPARATOR, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false, "Enable if spatial parameters (e.g. \"" + RADIUS
                + "\" or \"" + LINKING_MAX_DISTANCE
                + "\") are being specified in calibrated units.  If disabled, parameters are assumed to be specified in pixel units."));
        parameters.add(new BooleanP(DO_SUBPIXEL_LOCALIZATION, this, true,
                "Enable TrackMate's \"Subpixel localisation\" functionality.  When enabled, subpixel centroid coordinates will be stored as measurements associated with each detected object."));
        parameters.add(new BooleanP(DO_MEDIAN_FILTERING, this, false,
                "Enable TrackMate's \"Median filtering\" functionality."));
        parameters.add(new DoubleP(RADIUS, this, 2.0,
                "Expected radius of spots in the input image.  Specified in pixel units, unless \"" + CALIBRATED_UNITS
                        + "\" is selected."));
        parameters.add(new DoubleP(THRESHOLD, this, 10.0,
                "Threshold for spot detection.  Threshold is applied to filtered image (Laplacian of Gaussian), so will be affected by the specified \""
                        + RADIUS
                        + "\" value.  Increase this value to make detection more selective (i.e. detect fewer spots)."));
        parameters.add(new BooleanP(ESTIMATE_SIZE, this, false,
                "When enabled, output spot objects will have explicit size (rather than a single, centroid coordinate) determined by the TrackMate-calculated estimated diameter."));

        parameters.add(new SeparatorP(TRACK_SEPARATOR, this));
        parameters.add(new BooleanP(DO_TRACKING, this, true,
                "Track spot objects over time.  Spots in each frame will become children of a parent track object.  The track object itself won't contain any coordinate information."));
        parameters.add(new ChoiceP(TRACKING_METHOD, this, TrackingMethods.SIMPLE, TrackingMethods.ALL,
                "Method with which spots are tracked between frames:<br><ul>" + "<li>\"" + TrackingMethods.KALMAN
                        + "\" Uses the previous position of a spot and its current velocity to estimate where the spot will be in the next frame. These predicted spots are linked to the spots in the current frame.  When dealing with particles moving at roughly constant speeds, this method should be more accurate.</li>"
                        + "<li>\"" + TrackingMethods.SIMPLE
                        + "\" (default) Calculates links between spot positions in the previous and current frames.  This does not take motion into account.</li></ul>"));
        parameters.add(new DoubleP(LINKING_MAX_DISTANCE, this, 10.0,
                "Maximum distance a spot can travel between frames and still be linked to its starting spot.  Specified in pixel units, unless \""
                        + CALIBRATED_UNITS + "\" is selected."));
        parameters.add(new DoubleP(GAP_CLOSING_MAX_DISTANCE, this, 10.0, "Maximum distance a spot can travel between \""
                + MAX_FRAME_GAP
                + "\" frames and still be linked to its starting spot.  This accounts for the greater distance a spot can move between detections when it's allowed to go undetected in some timepoints.  Specified in pixel units, unless \""
                + CALIBRATED_UNITS + "\" is selected."));
        parameters.add(new DoubleP(INITIAL_SEARCH_RADIUS, this, 10.0,
                "Minimum spot separation required for creation of a new track."));
        parameters.add(new DoubleP(SEARCH_RADIUS, this, 10.0,
                "Maximum distance between predicted spot location and location of spot in current frame."));
        parameters.add(new IntegerP(MAX_FRAME_GAP, this, 3,
                "Maximum number of frames a spot can go undetected before it will be classed as a new track upon reappearance."));
        parameters.add(new OutputTrackObjectsP(OUTPUT_TRACK_OBJECTS, this, "",
                "Track objects that will be added to the workspace.  These are parent objects to the spots in that track.  Track objects are simply used for linking spots to a common track and storing track-specific measurements."));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_SPOT_OBJECTS));

        returnedParameters.add(parameters.getParameter(SPOT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(DO_SUBPIXEL_LOCALIZATION));
        returnedParameters.add(parameters.getParameter(DO_MEDIAN_FILTERING));
        returnedParameters.add(parameters.getParameter(RADIUS));
        returnedParameters.add(parameters.getParameter(THRESHOLD));
        returnedParameters.add(parameters.getParameter(ESTIMATE_SIZE));

        returnedParameters.add(parameters.getParameter(TRACK_SEPARATOR));
        returnedParameters.add(parameters.getParameter(DO_TRACKING));
        if ((boolean) parameters.getValue(DO_TRACKING)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_TRACK_OBJECTS));
            returnedParameters.add(parameters.getParameter(TRACKING_METHOD));
            switch ((String) parameters.getValue(TRACKING_METHOD)) {
                case TrackingMethods.KALMAN:
                    returnedParameters.add(parameters.get(INITIAL_SEARCH_RADIUS));
                    returnedParameters.add(parameters.get(SEARCH_RADIUS));
                    break;
                case TrackingMethods.SIMPLE:
                    returnedParameters.add(parameters.get(LINKING_MAX_DISTANCE));
                    returnedParameters.add(parameters.get(GAP_CLOSING_MAX_DISTANCE));
                    break;
            }
            returnedParameters.add(parameters.getParameter(MAX_FRAME_GAP));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String outputSpotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);

        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_PX);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Radius used as size estimate for spot detection.  Measured in pixel units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.RADIUS_CAL);
        reference.setObjectsName(outputSpotObjectsName);
        reference.setDescription("Radius used as size estimate for spot detection.  Measured in calibrated " + "("
                + SpatialUnit.getOMEUnit().getSymbol() + ") units.");
        returnedRefs.add(reference);

        reference = objectMeasurementRefs.getOrPut(Measurements.QUALITY);
        reference.setObjectsName(outputSpotObjectsName);
        returnedRefs.add(reference);

        if ((boolean) parameters.getValue(DO_SUBPIXEL_LOCALIZATION)) {
            reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTROID_PX);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.X_CENTROID_CAL);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTROID_PX);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Y_CENTROID_CAL);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Z_CENTROID_SLICE);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);

            reference = objectMeasurementRefs.getOrPut(Measurements.Z_CENTROID_CAL);
            reference.setObjectsName(outputSpotObjectsName);
            returnedRefs.add(reference);
        }

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        ParentChildRefs returnedRelationships = new ParentChildRefs();

        if ((boolean) parameters.getValue(DO_TRACKING)) {
            returnedRelationships.add(parentChildRefs.getOrPut(parameters.getValue(OUTPUT_TRACK_OBJECTS),
                    parameters.getValue(OUTPUT_SPOT_OBJECTS)));

        }

        return returnedRelationships;

    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}