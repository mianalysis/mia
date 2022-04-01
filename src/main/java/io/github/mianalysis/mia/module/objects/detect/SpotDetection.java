// TODO: See how TrackMate behaves with different channels.  Currently always assigns channel to "0".

package io.github.mianalysis.mia.module.objects.detect;

import java.awt.Color;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.TrackMate;
import fiji.plugin.trackmate.detection.DetectorKeys;
import fiji.plugin.trackmate.detection.LogDetectorFactory;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.GetLocalObjectRegion;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectCentroid;
import io.github.mianalysis.mia.module.visualise.overlays.AddObjectOutline;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.units.SpatialUnit;
import io.github.mianalysis.mia.object.units.TemporalUnit;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.common.exceptions.IntegerOverflowException;
import io.github.sjcross.common.object.Point;
import io.github.sjcross.common.object.volume.PointOutOfRangeException;
import io.github.sjcross.common.object.volume.SpatCal;
import io.github.sjcross.common.object.volume.VolumeType;
import io.github.sjcross.common.process.IntensityMinMax;

/**
 * Created by sc13967 on 15/05/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class SpotDetection extends Module {
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

    public interface Measurements {
        String RADIUS_PX = "SPOT_DETECT // RADIUS_(PX)";
        String RADIUS_CAL = "SPOT_DETECT // RADIUS_(${SCAL})";
        String QUALITY = "SPOT_DETECT // QUALITY";
        String X_CENTROID_PX = "SPOT_DETECT // X_CENTROID_(PX)";
        String X_CENTROID_CAL = "SPOT_DETECT // X_CENTROID_(${SCAL})";
        String Y_CENTROID_PX = "SPOT_DETECT // Y_CENTROID_(PX)";
        String Y_CENTROID_CAL = "SPOT_DETECT // Y_CENTROID_(${SCAL})";
        String Z_CENTROID_SLICE = "SPOT_DETECT // Z_CENTROID_(SLICE)";
        String Z_CENTROID_CAL = "SPOT_DETECT // Z_CENTROID_(${SCAL})";

    }

    public SpotDetection(Modules modules) {
        super("Spot detection", modules);
    }

    public Settings initialiseSettings(ImagePlus ipl, SpatCal calibration) {
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean subpixelLocalisation = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);
        boolean medianFiltering = parameters.getValue(DO_MEDIAN_FILTERING);
        double radius = parameters.getValue(RADIUS);
        double threshold = parameters.getValue(THRESHOLD);

        // Applying conversion to parameters
        if (calibratedUnits)
            radius = radius / calibration.getDppXY();

        // Initialising settings for TrackMate
        Settings settings = new Settings(ipl);

        settings.detectorFactory = new LogDetectorFactory();
        settings.detectorSettings.put(DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION, subpixelLocalisation);
        settings.detectorSettings.put(DetectorKeys.KEY_DO_MEDIAN_FILTERING, medianFiltering);
        settings.detectorSettings.put(DetectorKeys.KEY_RADIUS, radius);
        settings.detectorSettings.put(DetectorKeys.KEY_THRESHOLD, threshold);
        settings.detectorSettings.put(DetectorKeys.KEY_TARGET_CHANNEL, 1);

        return settings;

    }

    public Objs getSpots(Model model, SpatCal calibration, int nFrames, double frameInterval)
            throws IntegerOverflowException {
        String spotObjectsName = parameters.getValue(OUTPUT_SPOT_OBJECTS);
        boolean doSubpixel = parameters.getValue(DO_SUBPIXEL_LOCALIZATION);

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

        return spotObjects;

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
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(spotObjects,
                ColourFactory.SingleColours.ORANGE);
        HashMap<Integer, Color> colours = ColourFactory.getColours(hues,100);

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
        return "Detects spot-like features in 2D and 3D using TrackMate's LogDetector.  By default, detected spots are stored as individual, single pixel, objects centred on the detected feature.  Optionally, spots can be given area or volume based on the estimated size of the spot.  Adds measurements to each output spot for estimated radius and quality.  If sub-pixel localisation is specified, the sub-pixel centroid location in X,Y and Z is also stored as a measurement."

                + "<br><br>For more information, see the <a href=\"https://imagej.net/TrackMate\">TrackMate</a> documentation.";
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
            if (!trackmate.execDetection())
                MIA.log.writeError(trackmate.getErrorMessage());
            if (!trackmate.computeSpotFeatures(false))
                MIA.log.writeError(trackmate.getErrorMessage());

            spotObjects = getSpots(model, calibration, nFrames, frameInterval);

            if (estimateSize)
                estimateSpotSize(spotObjects, ipl);

            workspace.addObjects(spotObjects);

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
        parameters.add(
                new OutputObjectsP(OUTPUT_SPOT_OBJECTS, this, "", "Spot objects that will be added to the workspace."));

        parameters.add(new SeparatorP(SPOT_SEPARATOR, this));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false, "Enable if spatial parameters (e.g. \"" + RADIUS
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
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

        return true;
    }
}