package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.CumStat;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistribution extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASUREMENT_TYPE = "Measurement type";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROXIMAL_DISTANCE = "Proximal distance";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String IGNORE_ON_OBJECTS = "Ignore values on objects";
    public static final String EDGE_DISTANCE_MODE = "Edge distance mode";

    public interface MeasurementTypes {
        String FRACTION_PROXIMAL_TO_OBJECTS = "Fraction proximal to objects";
        String INTENSITY_WEIGHTED_PROXIMITY = "Intensity-weighted proximity";

        String[] ALL = new String[]{FRACTION_PROXIMAL_TO_OBJECTS,INTENSITY_WEIGHTED_PROXIMITY};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixel";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }

    public interface EdgeDistanceModes {
        String INSIDE_AND_OUTSIDE = "Inside and outside";
        String INSIDE_ONLY = "Inside only";
        String OUTSIDE_ONLY = "Outside only";

        String[] ALL = new String[]{INSIDE_AND_OUTSIDE,INSIDE_ONLY,OUTSIDE_ONLY};

    }

    private interface Measurements {
        String N_PX_INRANGE = "N_PX_INRANGE";
        String N_PX_OUTRANGE = "N_PX_OUTRANGE";
        String SUM_INT_INRANGE = "SUM_INT_INRANGE";
        String SUM_INT_OUTRANGE = "SUM_INT_OUTRANGE";
        String MEAN_INT_INRANGE = "MEAN_INT_INRANGE";
        String MEAN_INT_OUTRANGE = "MEAN_INT_OUTRANGE";
        String MEAN_PROXIMITY_PX = "MEAN_PROXIMITY_PX";
        String MEAN_PROXIMITY_CAL = "MEAN_PROXIMITY_${CAL}";
        String STDEV_PROXIMITY_PX = "STDEV_PROXIMITY_PX";
        String STDEV_PROXIMITY_CAL = "STDEV_PROXIMITY_${CAL}";

    }


    private String getFullName(String objectsName, String measurement) {
        return "INT_DISTR // "+objectsName+"_"+measurement;
    }

    public CumStat[] measureFractionProximal(ObjCollection inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        HashMap<Integer,Float> hues = inputObjects.getHues(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR, hues);
        
        // Calculaing the distance map
        ImagePlus distIpl = BinaryOperations.getDistanceMap3D(objectsImage.getImagePlus(),true);

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        return measureIntensityFractions(inputImagePlus,distIpl,ignoreOnObjects,proximalDistance);

    }

    private static CumStat[] measureIntensityFractions(ImagePlus inputImagePlus, ImagePlus distIpl, boolean ignoreOnObjects, double proximalDistance) {
        CumStat[] cs = new CumStat[2];
        cs[0] = new CumStat();
        cs[1] = new CumStat();

        for (int z = 0; z < distIpl.getNSlices(); z++) {
            for (int c = 0; c < distIpl.getNChannels(); c++) {
                for (int t = 0; t < distIpl.getNFrames(); t++) {
                    distIpl.setPosition(c+1, z+1, t+1);
                    inputImagePlus.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distIpl.getProcessor().getFloatArray();
                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();

                    for (int x=0;x<distVals.length;x++) {
                        for (int y=0;y<distVals[0].length;y++) {
                            float dist = distVals[x][y];
                            float val = inputVals[x][y];

                            if (ignoreOnObjects && dist == 0) continue;

                            if (dist <= proximalDistance) {
                                cs[0].addMeasure(val);
                            } else {
                                cs[1].addMeasure(val);
                            }
                        }
                    }
                }
            }
        }

        distIpl.setPosition(1, 1, 1);
        inputImagePlus.setPosition(1, 1, 1);

        return cs;

    }

    public static CumStat measureIntensityWeightedProximity(ObjCollection inputObjects, Image inputImage, String edgeMode) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        HashMap<Integer,Float> hues = inputObjects.getHues(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR, hues);

        ImagePlus distIpl = null;
        switch (edgeMode) {
            case EdgeDistanceModes.INSIDE_AND_OUTSIDE:
                ImagePlus dist1 = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = new Duplicator().run(objectsImage.getImagePlus());

                dist1 = BinaryOperations.getDistanceMap3D(dist1,true);
                InvertIntensity.process(distIpl);
                BinaryOperations.applyStockBinaryTransform(distIpl,BinaryOperations.OperationModes.ERODE_2D,1);
                distIpl = BinaryOperations.getDistanceMap3D(distIpl,true);

                new ImageCalculator().process(dist1,distIpl,ImageCalculator.CalculationMethods.ADD,ImageCalculator.OverwriteModes.OVERWRITE_IMAGE2,false,true);

                break;

            case EdgeDistanceModes.INSIDE_ONLY:
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                InvertIntensity.process(distIpl);
                BinaryOperations.applyStockBinaryTransform(distIpl,BinaryOperations.OperationModes.ERODE_2D,1);
                distIpl = BinaryOperations.getDistanceMap3D(distIpl,true);
                break;

            case EdgeDistanceModes.OUTSIDE_ONLY:
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = BinaryOperations.getDistanceMap3D(distIpl,true);
                break;
        }

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        return measureWeightedDistance(inputImagePlus,distIpl);

    }

    private static CumStat measureWeightedDistance(ImagePlus inputImagePlus, ImagePlus distIpl) {
        CumStat cs = new CumStat();

        for (int z = 0; z < distIpl.getNSlices(); z++) {
            for (int c = 0; c < distIpl.getNChannels(); c++) {
                for (int t = 0; t < distIpl.getNFrames(); t++) {
                    distIpl.setPosition(c+1, z+1, t+1);
                    inputImagePlus.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distIpl.getProcessor().getFloatArray();
                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();

                    for (int x=0;x<distVals.length;x++) {
                        for (int y=0;y<distVals[0].length;y++) {
                            float dist = distVals[x][y];
                            float val = inputVals[x][y];

                            if (dist == 0) continue;

                            cs.addMeasure(dist,val);

                        }
                    }
                }
            }
        }

        distIpl.setPosition(1, 1, 1);
        inputImagePlus.setPosition(1, 1, 1);

        return cs;

    }

    @Override
    public String getTitle() {
        return "Measure intensity distribution";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return "CURRENTLY ONLY WORKS IN 3D";
    }

    @Override
    protected void run(Workspace workspace) throws GenericMIAException {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementType = parameters.getValue(MEASUREMENT_TYPE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        double proximalDistance = parameters.getValue(PROXIMAL_DISTANCE);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
        boolean ignoreOnObjects = parameters.getValue(IGNORE_ON_OBJECTS);
        String edgeDistanceMode = parameters.getValue(EDGE_DISTANCE_MODE);

        Image inputImage = workspace.getImages().get(inputImageName);

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            proximalDistance = inputImage.getImagePlus().getCalibration().getRawX(proximalDistance);
        }

        switch (measurementType) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), Double.NaN));

                    return;
                }

                CumStat[] css = measureFractionProximal(inputObjects, inputImage, proximalDistance, ignoreOnObjects);

                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), css[0].getN()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), css[1].getN()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), css[0].getMean()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), css[1].getMean()));

                writeMessage("Number of pixels inside range = " + css[0].getN());
                writeMessage("Number of pixels outside range = " + css[1].getN());
                writeMessage("Total intensity in range = " + css[0].getSum());
                writeMessage("Total intensity outside range = " + css[1].getSum());
                writeMessage("Mean intensity in range = " + css[0].getMean());
                writeMessage("Mean intensity outside range = " + css[1].getMean());

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    String name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = Units.replace(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL));
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = Units.replace(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL));
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    return;
                }

                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, edgeDistanceMode);
                double dppXY = inputImage.getImagePlus().getCalibration().pixelWidth;
                String name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                inputImage.addMeasurement(new Measurement(name, cs.getMean()));
                name = Units.replace(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL));
                inputImage.addMeasurement(new Measurement(name, cs.getMean()*dppXY));
                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                inputImage.addMeasurement(new Measurement(name, cs.getStd()));
                name = Units.replace(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL));
                inputImage.addMeasurement(new Measurement(name, cs.getStd()*dppXY));

                writeMessage("Mean intensity proximity = " + cs.getMean() + " +/- "+cs.getStd());

                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT_TYPE, Parameter.CHOICE_ARRAY,
                MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS, MeasurementTypes.ALL));
        parameters.add(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(PROXIMAL_DISTANCE, Parameter.DOUBLE,2d));
        parameters.add(new Parameter(SPATIAL_UNITS, Parameter.CHOICE_ARRAY, SpatialUnits.PIXELS, SpatialUnits.ALL));
        parameters.add(new Parameter(EDGE_DISTANCE_MODE,Parameter.CHOICE_ARRAY, EdgeDistanceModes.INSIDE_AND_OUTSIDE, EdgeDistanceModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_TYPE));

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(PROXIMAL_DISTANCE));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS));
                returnedParameters.add(parameters.getParameter(IGNORE_ON_OBJECTS));

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(EDGE_DISTANCE_MODE));

                break;

        }

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        imageMeasurementReferences.setAllCalculated(false);

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                String name = getFullName(inputObjectsName, Measurements.N_PX_INRANGE);
                MeasurementReference reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = Units.replace(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL));
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                name = Units.replace(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL));
                reference = imageMeasurementReferences.getOrPut(name);
                reference.setCalculated(true);

                break;
        }

        return imageMeasurementReferences;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
