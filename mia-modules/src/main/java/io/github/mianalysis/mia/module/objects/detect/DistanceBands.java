// TODO: Distance limits

package io.github.mianalysis.mia.module.objects.detect;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageCalculator;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.ImageTypeConverter;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.module.objects.transform.MaskObjects;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.VolumeTypesInterface;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.BinaryLogicInterface;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.SpatialUnitsInterface;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import net.imagej.ImgPlus;
import net.imglib2.Point;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Created by sc13967 on 06/06/2017.
 */

/**
* 
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class DistanceBands<T extends RealType<T> & NativeType<T>> extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image controls";

    /**
    * 
    */
    public static final String INPUT_MODE = "Input mode";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String BINARY_LOGIC = "Binary logic";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String OUTPUT_SEPARATOR = "Object output";

    /**
    * 
    */
    public static final String OUTPUT_OBJECTS = "Output objects";

    /**
    * 
    */
    public static final String BAND_MODE = "Band mode";

    /**
    * 
    */
    public static final String VOLUME_TYPE = "Volume type";

    /**
    * 
    */
    public static final String PARENT_OBJECTS_MODE = "Parent objects mode";

    /**
    * 
    */
    public static final String MERGE_BANDS = "Merge bands";

    /**
    * 
    */
    public static final String PARENT_OBJECTS = "Parent objects";

    /**
    * 
    */
    public static final String BAND_SEPARATOR = "Band controls";

    /**
    * 
    */
    public static final String MATCH_Z_TO_X = "Match Z to XY";

    /**
    * 
    */
    public static final String WEIGHT_MODE = "Weight mode";

    /**
    * 
    */
    public static final String BAND_WIDTH = "Band width";

    /**
    * 
    */
    public static final String SPATIAL_UNITS_MODE = "Spatial units mode";

    /**
    * 
    */
    public static final String APPLY_MINIMUM_BAND_DISTANCE = "Apply minimum band distance";

    /**
    * 
    */
    public static final String MINIMUM_BAND_DISTANCE = "Minimum band distance";

    /**
    * 
    */
    public static final String APPLY_MAXIMUM_BAND_DISTANCE = "Apply maximum band distance";

    /**
    * 
    */
    public static final String MAXIMUM_BAND_DISTANCE = "Maximum band distance";

    public DistanceBands(Modules modules) {
        super("Distance bands", modules);
    }

    public interface InputModes {
        String IMAGE = "Image";
        String OBJECT_CENTROIDS = "Object centroids";
        String OBJECT_SURFACE = "Object surface";

        String[] ALL = new String[] { IMAGE, OBJECT_CENTROIDS, OBJECT_SURFACE };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public interface BandModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_OBJECTS = "Inside objects";
        String OUTSIDE_OBJECTS = "Outside objects";

        String[] ALL = new String[] { INSIDE_AND_OUTSIDE, INSIDE_OBJECTS, OUTSIDE_OBJECTS };

    }

    public interface ParentObjectsMode {
        String NONE = "None";
        String OVERLAP = "Overlap";
        // String PROXIMITY = "Proximity";

        // String[] ALL = new String[] { NONE, OVERLAP, PROXIMITY };
        String[] ALL = new String[] { NONE, OVERLAP };

    }

    public interface VolumeTypes extends VolumeTypesInterface {
    }

    public interface WeightModes extends DistanceMap.WeightModes {

    }

    public interface SpatialUnitsModes extends SpatialUnitsInterface {
    }

    public interface DetectionModes extends IdentifyObjects.DetectionModes {
    }

    public interface Measurements {
        String CENTRAL_DISTANCE_PX = "CENTRAL_DIST_(PX)";
        String CENTRAL_DISTANCE_CAL = "CENTRAL_DIST_(${SCAL})";
        String MIN_DISTANCE_PX = "MIN_DIST_(PX)";
        String MIN_DISTANCE_CAL = "MIN_DIST_(${SCAL})";
        String MAX_DISTANCE_PX = "MAX_DIST_(PX)";
        String MAX_DISTANCE_CAL = "MAX_DIST_(${SCAL})";

    }

    public static String getFullName(String measurement) {
        return "BANDS // " + measurement;
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_DETECT;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    public static <T extends RealType<T> & NativeType<T>> Objs getAllBands(Image<T> inputImage,
            String outputObjectsName, boolean blackBackground, String weightMode, boolean matchZToXY,
            double bandWidthPx, double minDistPx, double maxDistPx, String type, boolean mergeBands) {
        // Get distance map
        Objs internalBands = getInternalBandsOnly(inputImage, outputObjectsName, blackBackground, weightMode,
                matchZToXY, bandWidthPx, minDistPx, maxDistPx, type, mergeBands);
        Objs externalBands = getExternalBandsOnly(inputImage, outputObjectsName, blackBackground, weightMode,
                matchZToXY, bandWidthPx, minDistPx, maxDistPx, type, mergeBands);

        // Getting max ID for internal bands
        int ID = internalBands.getLargestID() + 1;
        for (Obj externalBand : externalBands.values()) {
            externalBand.setID(ID++);
            internalBands.add(externalBand);
        }

        return internalBands;

    }

    public static <T extends RealType<T> & NativeType<T>> Objs getInternalBandsOnly(Image<T> inputImage,
            String outputObjectsName, boolean blackBackground, String weightMode, boolean matchZToXY,
            double bandWidthPx, double minDistPx, double maxDistPx, String type, boolean mergeBands) {
        // Get distance map
        Image<T> distPx = getBandedDistanceMap(inputImage, blackBackground, weightMode, matchZToXY, bandWidthPx);

        // Applying distance limits
        applyDistanceLimits(distPx, minDistPx, maxDistPx, bandWidthPx);

        // Creating internal bands objects
        Objs bands;
        if (mergeBands)
            bands = distPx.convertImageToObjects(type, outputObjectsName, false);
        else
            bands = IdentifyObjects.process(distPx, outputObjectsName, blackBackground, false,
                    IdentifyObjects.DetectionModes.THREE_D, 26, type, false, 0, false);

        addMeasurements(bands, distPx, bandWidthPx, true);

        return bands;

    }

    public static <T extends RealType<T> & NativeType<T>> Objs getExternalBandsOnly(Image<T> inputImage,
            String outputObjectsName, boolean blackBackground, String weightMode, boolean matchZToXY,
            double bandWidthPx, double minDistPx, double maxDistPx, String type, boolean mergeBands) {
        // Get distance map
        Image<T> distPx = getBandedDistanceMap(inputImage, !blackBackground, weightMode, matchZToXY, bandWidthPx);

        // Applying distance limits
        applyDistanceLimits(distPx, minDistPx, maxDistPx, bandWidthPx);

        // Creating external bands objects
        Objs bands;
        if (mergeBands)
            bands = distPx.convertImageToObjects(type, outputObjectsName, false);
        else
            bands = IdentifyObjects.process(distPx, outputObjectsName, blackBackground, false,
                    IdentifyObjects.DetectionModes.THREE_D, 26, type, false, 0, false);

        addMeasurements(bands, distPx, bandWidthPx, false);

        return bands;

    }

    public static <T extends RealType<T> & NativeType<T>> Image<T> getBandedDistanceMap(Image<T> inputImage,
            boolean blackBackground, String weightMode, boolean matchZToXY, double bandWidthPx) {
        Image<T> distPx = DistanceMap.process(inputImage, "Distance", blackBackground, weightMode, matchZToXY, false);
        ImageMath.process(distPx, ImageMath.CalculationModes.DIVIDE, bandWidthPx);

        // Applying masking (can occur due to ZtoXY interpolation)
        String calculation = blackBackground ? ImageCalculator.CalculationMethods.AND
                : ImageCalculator.CalculationMethods.NOT;
        ImageCalculator.process(distPx, inputImage, calculation, ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1, null,
                true, false);

        ImgPlus<T> distPxImg = distPx.getImgPlus();
        LoopBuilder.setImages(distPxImg).forEachPixel((s) -> s.setReal(Math.ceil(s.getRealDouble())));
        distPx.setImgPlus(distPxImg);
        ImageTypeConverter.process(distPx, 8, ImageTypeConverter.ScalingModes.CLIP);

        return distPx;

    }

    public static <T extends RealType<T> & NativeType<T>> void applyDistanceLimits(Image<T> inputImage,
            double minDistPx, double maxDistPx, double bandWidthPx) {
        // Skipping this step if no limits were set
        if (minDistPx == 0 && maxDistPx == Double.MAX_VALUE)
            return;

        // Converting limits to scaled values
        double minDist = minDistPx / bandWidthPx;
        double maxDist = maxDistPx / bandWidthPx;

        ImgPlus<T> img = inputImage.getImgPlus();
        LoopBuilder.setImages(img).forEachPixel((s) -> s.setReal(
                s.getRealDouble() >= minDist && s.getRealDouble() <= maxDist ? s.getRealDouble() : Double.NaN));
        inputImage.setImgPlus(img);

    }

    public static Objs applyParentMasking(Objs tempBandObjects, Objs parentObjects) {
        Objs bandObjects = new Objs(tempBandObjects.getName(), tempBandObjects);

        for (Obj parentObject : parentObjects.values()) {
            Image maskImage = parentObject.getAsImage("Mask", false);
            for (Obj tempBandObject : tempBandObjects.values()) {
                Obj bandObject = MaskObjects.maskObject(tempBandObject, maskImage, tempBandObjects.getName());

                if (bandObject.size() == 0)
                    continue;

                bandObject.setID(bandObjects.getAndIncrementID());
                for (Measurement measurement : tempBandObject.getMeasurements().values())
                    bandObject.addMeasurement(new Measurement(measurement.getName(), measurement.getValue()));

                bandObject.addParent(parentObject);
                parentObject.addChild(bandObject);

                bandObjects.add(bandObject);

            }
        }

        return bandObjects;

    }

    static <T extends RealType<T> & NativeType<T>> void addMeasurements(Objs bands, Image<T> distPx, double bandWidthPx,
            boolean internalObjects) {
        double dppXY = bands.getDppXY();
        double sign = internalObjects ? -1 : 1;
        ImgPlus<T> distPxImg = distPx.getImgPlus();

        // Applying measurements
        for (Obj obj : bands.values()) {
            // Measure distance value of first pixel
            Point pt = obj.getImgPlusCoordinateIterator(distPxImg, 0).next();
            double val = distPxImg.getAt(pt).getRealDouble();

            // Converting value to distance
            double minDist = (val - 1) * bandWidthPx * sign;
            double maxDist = val * bandWidthPx * sign;
            double centDist = (minDist + maxDist) / 2;

            obj.addMeasurement(new Measurement(getFullName(Measurements.CENTRAL_DISTANCE_PX), centDist));
            obj.addMeasurement(new Measurement(getFullName(Measurements.CENTRAL_DISTANCE_CAL), centDist * dppXY));
            obj.addMeasurement(new Measurement(getFullName(Measurements.MIN_DISTANCE_PX), minDist));
            obj.addMeasurement(new Measurement(getFullName(Measurements.MIN_DISTANCE_CAL), minDist * dppXY));
            obj.addMeasurement(new Measurement(getFullName(Measurements.MAX_DISTANCE_PX), maxDist));
            obj.addMeasurement(new Measurement(getFullName(Measurements.MAX_DISTANCE_CAL), maxDist * dppXY));

        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputMode = parameters.getValue(INPUT_MODE, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String outputObjectsName = parameters.getValue(OUTPUT_OBJECTS, workspace);
        String binaryLogic = parameters.getValue(BINARY_LOGIC, workspace);
        String bandMode = parameters.getValue(BAND_MODE, workspace);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        String type = parameters.getValue(VOLUME_TYPE, workspace);
        String parentObjectsMode = parameters.getValue(PARENT_OBJECTS_MODE, workspace);
        boolean mergeBands = parameters.getValue(MERGE_BANDS, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS, workspace);
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X, workspace);
        String weightMode = parameters.getValue(WEIGHT_MODE, workspace);
        double bandWidth = parameters.getValue(BAND_WIDTH, workspace);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE, workspace);
        boolean applyMinDist = parameters.getValue(APPLY_MINIMUM_BAND_DISTANCE, workspace);
        double minDist = parameters.getValue(MINIMUM_BAND_DISTANCE, workspace);
        boolean applyMaxDist = parameters.getValue(APPLY_MAXIMUM_BAND_DISTANCE, workspace);
        double maxDist = parameters.getValue(MAXIMUM_BAND_DISTANCE, workspace);

        // Getting input image
        Image<T> inputImage = null;
        switch (inputMode) {
            case InputModes.IMAGE:
            default:
                inputImage = workspace.getImage(inputImageName);
                break;
            case InputModes.OBJECT_CENTROIDS:
                blackBackground = true;
                Objs inputObjects = workspace.getObjects(inputObjectsName);
                HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(inputObjects,
                        ColourFactory.SingleColours.WHITE);
                inputImage = inputObjects.convertCentroidsToImage("Objects", hues, 8, blackBackground);
                break;
            case InputModes.OBJECT_SURFACE:
                blackBackground = true;
                inputObjects = workspace.getObjects(inputObjectsName);
                hues = ColourFactory.getSingleColourValues(inputObjects, ColourFactory.SingleColours.WHITE);
                inputImage = inputObjects.convertToImage("Objects", hues, 8, blackBackground);
                break;
        }

        // Applying spatial calibration
        if (spatialUnits.equals(SpatialUnitsModes.CALIBRATED)) {
            double dppXY = inputImage.getImagePlus().getCalibration().pixelWidth;
            bandWidth = bandWidth / dppXY;
            minDist = minDist / dppXY;
            maxDist = maxDist / dppXY;
        }

        // Removing distance limits if not being used
        if (!applyMinDist)
            minDist = 0;
        if (!applyMaxDist)
            maxDist = Double.MAX_VALUE;

        // Getting internal bands
        Objs tempBandObjects;
        switch (bandMode) {
            case BandModes.INSIDE_AND_OUTSIDE:
            default:
                tempBandObjects = getAllBands(inputImage, outputObjectsName, blackBackground, weightMode,
                        matchZToXY, bandWidth, minDist, maxDist, type, mergeBands);
                break;
            case BandModes.INSIDE_OBJECTS:
                tempBandObjects = getInternalBandsOnly(inputImage, outputObjectsName, blackBackground, weightMode,
                        matchZToXY, bandWidth, minDist, maxDist, type, mergeBands);
                break;
            case BandModes.OUTSIDE_OBJECTS:
                tempBandObjects = getExternalBandsOnly(inputImage, outputObjectsName, blackBackground, weightMode,
                        matchZToXY, bandWidth, minDist, maxDist, type, mergeBands);
                break;
        }

        // Applying parent-based masking
        Objs bandObjects;
        switch (parentObjectsMode) {
            case ParentObjectsMode.NONE:
            default:
                bandObjects = tempBandObjects;
                break;
            case ParentObjectsMode.OVERLAP:
                Objs parentObjects = workspace.getObjects(parentObjectsName);
                bandObjects = applyParentMasking(tempBandObjects, parentObjects);
                break;
        }

        // Adding objects to workspace
        workspace.addObjects(bandObjects);

        // Showing objects
        if (showOutput)
            bandObjects.convertToImageIDColours().show();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new ChoiceP(INPUT_MODE, this, InputModes.IMAGE, InputModes.ALL));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(OUTPUT_SEPARATOR, this));
        parameters.add(new OutputObjectsP(OUTPUT_OBJECTS, this));
        parameters.add(new ChoiceP(BAND_MODE, this, BandModes.INSIDE_AND_OUTSIDE, BandModes.ALL));
        parameters.add(new ChoiceP(VOLUME_TYPE, this, VolumeTypes.QUADTREE, VolumeTypes.ALL));
        parameters.add(new ChoiceP(PARENT_OBJECTS_MODE, this, ParentObjectsMode.NONE, ParentObjectsMode.ALL));
        parameters.add(new BooleanP(MERGE_BANDS, this, false));
        parameters.add(new InputObjectsP(PARENT_OBJECTS, this));

        parameters.add(new SeparatorP(BAND_SEPARATOR, this));
        parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));
        parameters.add(new ChoiceP(WEIGHT_MODE, this, WeightModes.WEIGHTS_3_4_5_7, WeightModes.ALL));
        parameters.add(new DoubleP(BAND_WIDTH, this, 1));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitsModes.PIXELS, SpatialUnitsModes.ALL));
        parameters.add(new BooleanP(APPLY_MINIMUM_BAND_DISTANCE, this, false));
        parameters.add(new DoubleP(MINIMUM_BAND_DISTANCE, this, 0));
        parameters.add(new BooleanP(APPLY_MAXIMUM_BAND_DISTANCE, this, false));
        parameters.add(new DoubleP(MAXIMUM_BAND_DISTANCE, this, 1));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.get(INPUT_SEPARATOR));
        returnedParameters.add(parameters.get(INPUT_MODE));
        switch ((String) parameters.getValue(INPUT_MODE, null)) {
            case InputModes.IMAGE:
                returnedParameters.add(parameters.get(INPUT_IMAGE));
                returnedParameters.add(parameters.get(BINARY_LOGIC));
                break;
            case InputModes.OBJECT_CENTROIDS:
            case InputModes.OBJECT_SURFACE:
                returnedParameters.add(parameters.get(INPUT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.get(OUTPUT_SEPARATOR));
        returnedParameters.add(parameters.get(OUTPUT_OBJECTS));
        returnedParameters.add(parameters.get(BAND_MODE));
        returnedParameters.add(parameters.get(VOLUME_TYPE));
        returnedParameters.add(parameters.get(PARENT_OBJECTS_MODE));
        switch ((String) parameters.getValue(PARENT_OBJECTS_MODE, null)) {
            case ParentObjectsMode.NONE:
                returnedParameters.add(parameters.get(MERGE_BANDS));
                break;
            case ParentObjectsMode.OVERLAP:
                // case ParentObjectsMode.PROXIMITY:
                returnedParameters.add(parameters.get(PARENT_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.get(BAND_SEPARATOR));
        returnedParameters.add(parameters.get(MATCH_Z_TO_X));
        returnedParameters.add(parameters.get(WEIGHT_MODE));
        returnedParameters.add(parameters.get(BAND_WIDTH));
        returnedParameters.add(parameters.get(SPATIAL_UNITS_MODE));
        returnedParameters.add(parameters.get(APPLY_MINIMUM_BAND_DISTANCE));
        if ((boolean) parameters.getValue(APPLY_MINIMUM_BAND_DISTANCE, null))
            returnedParameters.add(parameters.get(MINIMUM_BAND_DISTANCE));
        returnedParameters.add(parameters.get(APPLY_MAXIMUM_BAND_DISTANCE));
        if ((boolean) parameters.getValue(APPLY_MAXIMUM_BAND_DISTANCE, null))
            returnedParameters.add(parameters.get(MAXIMUM_BAND_DISTANCE));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();
        String outputObjects = parameters.getValue(OUTPUT_OBJECTS, workspace);

        String name = getFullName(Measurements.CENTRAL_DISTANCE_PX);
        ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.CENTRAL_DISTANCE_CAL);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MIN_DISTANCE_PX);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MIN_DISTANCE_CAL);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MAX_DISTANCE_PX);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        name = getFullName(Measurements.MAX_DISTANCE_CAL);
        reference = objectMeasurementRefs.getOrPut(name);
        reference.setObjectsName(outputObjects);
        returnedRefs.add(reference);

        return returnedRefs;

    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        switch ((String) parameters.getValue(PARENT_OBJECTS_MODE, null)) {
            case ParentObjectsMode.NONE:
            default:
                return null;
            case ParentObjectsMode.OVERLAP:
                ParentChildRefs returnedRefs = new ParentChildRefs();

                String parentObjectsName = parameters.getValue(PARENT_OBJECTS, null);
                String childObjectsName = parameters.getValue(OUTPUT_OBJECTS, null);
                returnedRefs.add(parentChildRefs.getOrPut(parentObjectsName, childObjectsName));

                return returnedRefs;
        }
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {

    }
}
