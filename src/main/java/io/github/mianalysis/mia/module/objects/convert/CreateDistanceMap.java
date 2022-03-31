// TODO: Normalised distance from centre to edge.  Will need to calculate line between the two and assign points on that line

package io.github.mianalysis.mia.module.objects.convert;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.process.ImageCalculator;
import io.github.mianalysis.mia.module.images.process.ImageMath;
import io.github.mianalysis.mia.module.images.process.InvertIntensity;
import io.github.mianalysis.mia.module.images.process.binary.BinaryOperations2D;
import io.github.mianalysis.mia.module.images.process.binary.DistanceMap;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.choiceinterfaces.SpatialUnitsInterface;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.ColourFactory;
import io.github.sjcross.common.object.Point;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class CreateDistanceMap extends Module {
    public static final String INPUT_SEPARATOR = "Objects input / image output";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String DISTANCE_MAP_SEPARATOR = "Distance map controls";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String INVERT_MAP_WITHIN_OBJECTS = "Invert map within objects";
    public static final String MASKING_MODE = "Masking mode";
    public static final String NORMALISE_MAP_PER_OBJECT = "Normalise map per object";
    public static final String SPATIAL_UNITS_MODE = "Spatial units mode";

    public CreateDistanceMap(Modules modules) {
        super("Create distance map", modules);
    }

    public interface ReferenceModes {
        String DISTANCE_FROM_CENTROID = "Distance from object centroid";
        String DISTANCE_FROM_EDGE = "Distance from object edge";

        String[] ALL = new String[] { DISTANCE_FROM_CENTROID, DISTANCE_FROM_EDGE };

    }

    public interface MaskingModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_ONLY = "Inside only";
        String OUTSIDE_ONLY = "Outside only";

        String[] ALL = new String[] { INSIDE_AND_OUTSIDE, INSIDE_ONLY, OUTSIDE_ONLY };

    }

    public interface SpatialUnitsModes extends SpatialUnitsInterface {
    }

    public static Image getCentroidDistanceMap(Objs inputObjects, String outputImageName) {
        // Getting image parameters
        int width = inputObjects.getWidth();
        int height = inputObjects.getHeight();
        int nZ = inputObjects.getNSlices();
        int nT = inputObjects.getNFrames();

        // Creating a blank image (8-bit, so binary operations work)
        ImagePlus distanceMapIpl = IJ.createHyperStack(outputImageName, width, height, 1, nZ, nT, 8);
        Image distanceMap = ImageFactory.createImage(outputImageName, distanceMapIpl);
        inputObjects.applyCalibration(distanceMap);

        // Adding a spot to the centre of each object
        for (Obj inputObj : inputObjects.values()) {
            int x = (int) Math.round(inputObj.getXMean(true));
            int y = (int) Math.round(inputObj.getYMean(true));
            int z = (int) Math.round(inputObj.getZMean(true, false));
            int t = inputObj.getT();

            distanceMapIpl.setPosition(1, z + 1, t + 1);
            distanceMapIpl.getProcessor().set(x, y, 255);
        }

        // Calculating the distance map
        return DistanceMap.process(distanceMap, outputImageName, false, DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true,
                false);

    }

    public static Image getEdgeDistanceMap(Objs inputObjects, String outputImageName, boolean invertInside) {
        // Creating an objects image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(inputObjects,
                ColourFactory.SingleColours.WHITE);
        Image objImage = inputObjects.convertToImage(outputImageName, hues, 8, false);

        // Calculating the distance maps. The inside map is set to negative
        String weightMode = DistanceMap.WeightModes.WEIGHTS_3_4_5_7;
        Image outsideDistImage = DistanceMap.process(objImage, "DistanceOutside", false, weightMode, true, false);
        InvertIntensity.process(objImage);
        BinaryOperations2D.process(objImage, BinaryOperations2D.OperationModes.ERODE, 1, 1, false);
        Image insideDistImage = DistanceMap.process(objImage, "DistanceInside", false, weightMode, true, false);

        // If selected, inverting the inside of the object, so values here are negative
        if (invertInside)
            ImageMath.process(insideDistImage, ImageMath.CalculationTypes.MULTIPLY, -1.0);

        // Compiling the distance map
        return ImageCalculator.process(insideDistImage, outsideDistImage, ImageCalculator.CalculationMethods.ADD,
                ImageCalculator.OverwriteModes.CREATE_NEW, outputImageName, true, true);

    }

    public static void applyMasking(Image inputImage, Objs inputObjects, String maskingMode) {
        // If the masking mode is set to INSIDE_AND_OUTSIDE skip this method
        if (maskingMode.equals(MaskingModes.INSIDE_AND_OUTSIDE))
            return;

        ImagePlus inputIpl = inputImage.getImagePlus();

        // Convert to image (and possibly invert), set to binary image (0 and 1) and
        // multiply as appropriate
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourValues(inputObjects,
                ColourFactory.SingleColours.WHITE);
        ImagePlus objIpl = inputObjects.convertToImage("Objects", hues, 8, false).getImagePlus();

        // For outside only masks invert the mask
        if (maskingMode.equals(MaskingModes.OUTSIDE_ONLY))
            InvertIntensity.process(objIpl);

        // Normalising the mask
        ImageMath.process(objIpl, ImageMath.CalculationTypes.DIVIDE, 255);

        // Applying the mask
        String calculationMode = ImageCalculator.CalculationMethods.MULTIPLY;
        String overwriteMode = ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1;
        ImageCalculator.process(inputIpl, objIpl, calculationMode, overwriteMode, null, false, true);

    }

    public static void applyNormalisation(Image inputImage, Objs inputObjects) {
        // Iterating over each object, calculating the largest distance, then dividing
        // all pixels within that object by
        // this value
        for (Obj inputObject : inputObjects.values()) {
            double maxDistance = getMaximumDistance(inputImage, inputObject);
            applyNormalisation(inputImage, inputObject, maxDistance);
        }
    }

    static double getMaximumDistance(Image inputImage, Obj inputObject) {
        // Iterating over each point in the object, getting the largest distance
        double maxDistance = Double.MIN_VALUE;

        int t = inputObject.getT();
        ImagePlus inputIpl = inputImage.getImagePlus();

        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            inputIpl.setPosition(1, z + 1, t + 1);
            double currentValue = inputIpl.getProcessor().getPixelValue(x, y);

            maxDistance = Math.max(Math.abs(currentValue), maxDistance);

        }

        return maxDistance;

    }

    static void applyNormalisation(Image inputImage, Obj inputObject, double maxDistance) {
        int t = inputObject.getT();
        ImagePlus inputIpl = inputImage.getImagePlus();

        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            inputIpl.setPosition(1, z + 1, t + 1);
            double currentValue = inputIpl.getProcessor().getPixelValue(x, y);
            inputIpl.getProcessor().setf(x, y, (float) (currentValue / maxDistance));

        }
    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_CONVERT;
    }

    @Override
    public String getDescription() {
        return "Creates a distance map for a selected object set.  Pixels in the output image are encoded with the distance to the nearest image edge or centroid (depending on setting).  A single distance map image is created for all objects in the specified set.  Uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting other parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        boolean invertInside = parameters.getValue(INVERT_MAP_WITHIN_OBJECTS);
        String maskingMode = parameters.getValue(MASKING_MODE);
        boolean normaliseMap = parameters.getValue(NORMALISE_MAP_PER_OBJECT);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE);

        // Initialising the distance map
        Image distanceMap = null;
        switch (referenceMode) {
            case ReferenceModes.DISTANCE_FROM_CENTROID:
                distanceMap = getCentroidDistanceMap(inputObjects, outputImageName);
                break;

            case ReferenceModes.DISTANCE_FROM_EDGE:
                distanceMap = getEdgeDistanceMap(inputObjects, outputImageName, invertInside);
                break;
        }

        if (distanceMap == null)
            return Status.PASS;

        // Applying masking
        applyMasking(distanceMap, inputObjects, maskingMode);

        // Performing normalisation (only when using inside-only masking)
        if (maskingMode.equals(MaskingModes.INSIDE_ONLY) && normaliseMap)
            applyNormalisation(distanceMap, inputObjects);

        // Applying spatial calibration (as long as we're not normalising the map)
        if (!(maskingMode.equals(MaskingModes.INSIDE_ONLY) && normaliseMap)
                && spatialUnits.equals(SpatialUnitsModes.CALIBRATED)) {
            double dppXY = inputObjects.getDppXY();
            DistanceMap.applyCalibratedUnits(distanceMap, dppXY);
        }

        // Adding distance map to output and showing
        workspace.addImage(distanceMap);
        if (showOutput)
            distanceMap.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(DISTANCE_MAP_SEPARATOR, this));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.DISTANCE_FROM_CENTROID, ReferenceModes.ALL));
        parameters.add(new BooleanP(INVERT_MAP_WITHIN_OBJECTS, this, true));
        parameters.add(new ChoiceP(MASKING_MODE, this, MaskingModes.INSIDE_AND_OUTSIDE, MaskingModes.ALL));
        parameters.add(new BooleanP(NORMALISE_MAP_PER_OBJECT, this, false));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitsModes.PIXELS, SpatialUnitsModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(DISTANCE_MAP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE)) {
            case ReferenceModes.DISTANCE_FROM_EDGE:
                returnedParameters.add(parameters.getParameter(INVERT_MAP_WITHIN_OBJECTS));
                break;
        }

        returnedParameters.add(parameters.getParameter(MASKING_MODE));
        switch ((String) parameters.getValue(MASKING_MODE)) {
            case MaskingModes.INSIDE_ONLY:
                returnedParameters.add(parameters.getParameter(NORMALISE_MAP_PER_OBJECT));
                break;
        }

        // If we're not using the inside-only masking with normalisation, allow the
        // units to be specified.
        if (!(((String) parameters.getValue(MASKING_MODE)).equals(MaskingModes.INSIDE_ONLY)
                && (boolean) parameters.getValue(NORMALISE_MAP_PER_OBJECT))) {
            returnedParameters.add(parameters.getParameter(SPATIAL_UNITS_MODE));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
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
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription(
                "Objects from workspace for which distance map will be created.  A single distance map will be created for all objects.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Output distance map image which will be added to the workspace.  This will contain the distance map for each object.");

        parameters.get(REFERENCE_MODE).setDescription("Controls where the distances are calculated from:<br><ul>"

                + "<li>\"" + ReferenceModes.DISTANCE_FROM_CENTROID
                + "\" Each pixel is encoded with the distance from the centre of the respective object.</li>"

                + "<li>\"" + ReferenceModes.DISTANCE_FROM_EDGE
                + "\" Each pixel is encoded with the distance from the edge of the respective object.</li></ul>");

        parameters.get(INVERT_MAP_WITHIN_OBJECTS).setDescription("When selected (and \"" + REFERENCE_MODE
                + "\" is set to \"" + ReferenceModes.DISTANCE_FROM_EDGE
                + "\"), the distance map will be inverted, such that the distances inside objects are also positive.  If not selected, the distances inside objects will be negative.  Distance values outside objects are always positive.");

        parameters.get(MASKING_MODE).setDescription("Controls which regions of the image are displayed:<br><ul>"

                + "<li>\"" + MaskingModes.INSIDE_AND_OUTSIDE
                + "\" Distances both inside and outside the objects are non-zero.</li>"

                + "<li>\"" + MaskingModes.INSIDE_ONLY
                + "\" Distances are shown inside each object, but are set to zero for all pixels outside an object.</li>"

                + "<li>\"" + MaskingModes.OUTSIDE_ONLY
                + "\" Distances are shown outside each object, but are set to zero for all pixels inside an object.</li></ul>");

        parameters.get(NORMALISE_MAP_PER_OBJECT).setDescription(
                "When selected, the distance values inside each object are normalised to the range 0-1.  Normalisation is performed on an object-by-object basis, so the absolute distance values cannot be directly compared between objects.");

        parameters.get(SPATIAL_UNITS_MODE).setDescription(SpatialUnitsInterface.getDescription());

    }
}
