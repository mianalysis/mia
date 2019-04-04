// TODO: Normalised distance from centre to edge.  Will need to calculate line between the two and assign points on that line

package wbif.sjx.MIA.Module.ObjectProcessing.Miscellaneous;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageMath;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.Object.Point;

import java.util.HashMap;

public class CreateDistanceMap extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String INVERT_MAP_WITHIN_OBJECTS = "Invert map within objects";
    public static final String MASKING_MODE = "Masking mode";
    public static final String NORMALISE_MAP_PER_OBJECT = "Normalise map per object";
    public static final String SPATIAL_UNITS = "Spatial units";


    public interface ReferenceModes {
        String DISTANCE_FROM_CENTROID = "Distance from object centroid";
        String DISTANCE_FROM_EDGE = "Distance from object edge";

        String[] ALL = new String[]{DISTANCE_FROM_CENTROID, DISTANCE_FROM_EDGE};

    }

    public interface MaskingModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_ONLY = "Inside only";
        String OUTSIDE_ONLY = "Outside only";

        String[] ALL = new String[]{INSIDE_AND_OUTSIDE,INSIDE_ONLY,OUTSIDE_ONLY};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }


    public static Image getCentroidDistanceMap(Image inputImage, ObjCollection inputObjects, String outputImageName) {
        // Getting image parameters
        int width = inputImage.getImagePlus().getWidth();
        int height = inputImage.getImagePlus().getHeight();
        int nZ = inputImage.getImagePlus().getNSlices();
        int nT = inputImage.getImagePlus().getNFrames();

        // Creating a blank image (8-bit, so binary operations work)
        ImagePlus distanceMap = IJ.createHyperStack(outputImageName, width, height,1,nZ,nT,8);
        distanceMap.setCalibration(inputImage.getImagePlus().getCalibration());

        // Adding a spot to the centre of each object
        for (Obj inputObj:inputObjects.values()) {
            int x = (int) Math.round(inputObj.getXMean(true));
            int y = (int) Math.round(inputObj.getYMean(true));
            int z = (int) Math.round(inputObj.getZMean(true,false));
            int t = inputObj.getT();

            distanceMap.setPosition(1,z+1,t+1);
            distanceMap.getProcessor().set(x,y,255);
        }

        // Calculating the distance map
        distanceMap = DistanceMap.getDistanceMap(distanceMap,true);

        return new Image(outputImageName,distanceMap);

    }

    public static Image getEdgeDistanceMap(Image inputImage, ObjCollection inputObjects, String outputImageName, boolean invertInside) {
        // Creating an objects image
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(inputObjects,ColourFactory.SingleColours.WHITE);
        ImagePlus objIpl = inputObjects.convertObjectsToImage(outputImageName,inputImage,hues,8,false).getImagePlus();

        // Calculating the distance maps.  The inside map is set to negative
        ImagePlus outsideDistIpl = DistanceMap.getDistanceMap(objIpl,true);
        InvertIntensity.process(objIpl);
        BinaryOperations2D.process(objIpl,BinaryOperations2D.OperationModes.ERODE,1);
        ImagePlus insideDistIpl = DistanceMap.getDistanceMap(objIpl,true);

        // If selected, inverting the inside of the object, so values here are negative
        if (invertInside) ImageMath.process(insideDistIpl,ImageMath.CalculationTypes.MULTIPLY,-1.0);

        // Compiling the distance map
        ImagePlus distanceMap = new ImageCalculator().process(insideDistIpl,outsideDistIpl,
                ImageCalculator.CalculationMethods.ADD,ImageCalculator.OverwriteModes.CREATE_NEW,true,true);

        return new Image(outputImageName,distanceMap);

    }

    public static void applyMasking(Image inputImage, ObjCollection inputObjects, String maskingMode) {
        // If the masking mode is set to INSIDE_AND_OUTSIDE skip this method
        if (maskingMode.equals(MaskingModes.INSIDE_AND_OUTSIDE)) return;

        ImagePlus inputIpl = inputImage.getImagePlus();

        // Convert to image (and possibly invert), set to binary image (0 and 1) and multiply as appropriate
        HashMap<Integer, Float> hues = ColourFactory.getSingleColourHues(inputObjects,ColourFactory.SingleColours.WHITE);
        ImagePlus objIpl = inputObjects.convertObjectsToImage("Objects",inputImage,hues,8,false).getImagePlus();

        // For outside only masks invert the mask
        if (maskingMode.equals(MaskingModes.OUTSIDE_ONLY)) InvertIntensity.process(objIpl);

        // Normalising the mask
        ImageMath.process(objIpl,ImageMath.CalculationTypes.DIVIDE,255);

        // Applying the mask
        String calculationMode = ImageCalculator.CalculationMethods.MULTIPLY;
        String overwriteMode = ImageCalculator.OverwriteModes.OVERWRITE_IMAGE1;
        new ImageCalculator().process(inputIpl,objIpl,calculationMode,overwriteMode,false,true);

    }

    public static void applyNormalisation(Image inputImage, ObjCollection inputObjects) {
        // Iterating over each object, calculating the largest distance, then dividing all pixels within that object by
        // this value
        for (Obj inputObject:inputObjects.values()) {
            double maxDistance = getMaximumDistance(inputImage, inputObject);
            applyNormalisation(inputImage,inputObject,maxDistance);
        }
    }

    static double getMaximumDistance(Image inputImage, Obj inputObject) {
        // Iterating over each point in the object, getting the largest distance
        double maxDistance = Double.MIN_VALUE;

        int t = inputObject.getT();
        ImagePlus inputIpl = inputImage.getImagePlus();

        for (Point<Integer> point:inputObject.getPoints()) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            inputIpl.setPosition(1,z+1,t+1);
            double currentValue = inputIpl.getProcessor().getPixelValue(x,y);

            maxDistance = Math.max(Math.abs(currentValue),maxDistance);

        }

        return maxDistance;

    }

    static void applyNormalisation(Image inputImage, Obj inputObject, double maxDistance) {
        int t = inputObject.getT();
        ImagePlus inputIpl = inputImage.getImagePlus();

        for (Point<Integer> point:inputObject.getPoints()) {
            int x = point.getX();
            int y = point.getY();
            int z = point.getZ();

            inputIpl.setPosition(1,z+1,t+1);
            double currentValue = inputIpl.getProcessor().getPixelValue(x,y);
            inputIpl.getProcessor().setf(x,y,(float) (currentValue/maxDistance));
            
        }
    }

    static void applyCalibratedUnits(Image inputImage, double dppXY) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImageTypeConverter.applyConversion(inputIpl,32,ImageTypeConverter.ScalingModes.CLIP);

        int width = inputIpl.getWidth();
        int height = inputIpl.getHeight();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        for (int t=0;t<nFrames;t++) {
            for (int z=0;z<nSlices;z++) {
                inputIpl.setPosition(1,z+1,t+1);
                ImageProcessor inputIpr = inputIpl.getProcessor();

                for (int x=0;x<width;x++) {
                    for (int y=0;y<height;y++) {
                        inputIpr.setf(x,y,(float) (inputIpr.getf(x,y)*dppXY));
                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "Create distance map";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_PROCESSING_MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting other parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String referenceMode = parameters.getValue(REFERENCE_MODE);
        boolean invertInside = parameters.getValue(INVERT_MAP_WITHIN_OBJECTS);
        String maskingMode = parameters.getValue(MASKING_MODE);
        boolean normaliseMap = parameters.getValue(NORMALISE_MAP_PER_OBJECT);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);

        // Initialising the distance map
        Image distanceMap = null;
        switch (referenceMode) {
            case ReferenceModes.DISTANCE_FROM_CENTROID:
                distanceMap = getCentroidDistanceMap(inputImage,inputObjects,outputImageName);
                break;

            case ReferenceModes.DISTANCE_FROM_EDGE:
                distanceMap = getEdgeDistanceMap(inputImage,inputObjects,outputImageName,invertInside);
                break;
        }

        if (distanceMap == null) return true;

        // Applying masking
        applyMasking(distanceMap,inputObjects,maskingMode);

        // Performing normalisation (only when using inside-only masking)
        if (maskingMode.equals(MaskingModes.INSIDE_ONLY) && normaliseMap) applyNormalisation(distanceMap,inputObjects);

        // Applying spatial calibration (as long as we're not normalising the map)
        if (!maskingMode.equals(MaskingModes.INSIDE_ONLY) && !normaliseMap) {
            double dppXY = inputImage.getImagePlus().getCalibration().pixelWidth;
            if (spatialUnits.equals(SpatialUnits.CALIBRATED)) applyCalibratedUnits(distanceMap, dppXY);
        }

        // Adding distance map to output and showing
        workspace.addImage(distanceMap);
        if (showOutput) distanceMap.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(REFERENCE_MODE,this,ReferenceModes.DISTANCE_FROM_CENTROID,ReferenceModes.ALL));
        parameters.add(new BooleanP(INVERT_MAP_WITHIN_OBJECTS,this,true));
        parameters.add(new ChoiceP(MASKING_MODE,this,MaskingModes.INSIDE_AND_OUTSIDE,MaskingModes.ALL));
        parameters.add(new BooleanP(NORMALISE_MAP_PER_OBJECT,this,false));
        parameters.add(new ChoiceP(SPATIAL_UNITS, this, SpatialUnits.PIXELS, SpatialUnits.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

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

        // If we're not using the inside-only masking with normalisation, allow the units to be specified.
        if (!parameters.getValue(MASKING_MODE).equals(MaskingModes.INSIDE_ONLY)
                && !(boolean) parameters.getValue(NORMALISE_MAP_PER_OBJECT)) {
            returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetImageMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
