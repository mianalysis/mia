// TODO: Normalised distance from centre to edge.  Will need to calculate line between the two and assign points on that line

package io.github.mianalysis.MIA.Module.ObjectProcessing.Miscellaneous;

import java.util.HashMap;

import ij.IJ;
import ij.ImagePlus;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.ImageCalculator;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.ImageMath;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputObjectsP;
import io.github.mianalysis.MIA.Object.Parameters.OutputImageP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceInterfaces.SpatialUnitsInterface;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.MIA.Process.ColourFactory;
import io.github.sjcross.common.Object.Point;

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
        Image distanceMap = new Image(outputImageName, distanceMapIpl);
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
        return DistanceMap.process(distanceMap, outputImageName, false,
                DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false);

    }

    public static Image getEdgeDistanceMap(Objs inputObjects, String outputImageName, boolean invertInside) {
        // Creating an objects image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(inputObjects,
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
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(inputObjects,
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
        return Categories.OBJECT_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
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
        parameters.get(SPATIAL_UNITS_MODE).setDescription(SpatialUnitsInterface.getDescription());
    }
}