package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.BinaryOperations;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Miscellaneous.ConvertObjectsToImage;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Image;
import wbif.sjx.common.MathFunc.CumStat;

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
        String MEAN_PROXIMITY = "MEAN_PROXIMITY";
        String STDEV_PROXIMITY = "STDEV_PROXIMITY";

    }


    private String getFullName(String objectsName, String measurement) {
        return "INT_DISTR//"+objectsName+"_"+measurement;
    }

    public CumStat[] measureFractionProximal(ObjCollection inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        HashMap<Integer,Float> hues = inputObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR, hues, true);
        
        // Calculaing the distance map
        ImagePlus distIpl = BinaryOperations.applyDistanceMap3D(objectsImage.getImagePlus(),true,true);

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
        HashMap<Integer,Float> hues = inputObjects.getHue(ObjCollection.ColourModes.SINGLE_COLOUR,"",false);
        Image objectsImage = inputObjects.convertObjectsToImage("Objects", inputImagePlus, ConvertObjectsToImage.ColourModes.SINGLE_COLOUR, hues, true);

        ImagePlus distIpl = null;
        switch (edgeMode) {
            case EdgeDistanceModes.INSIDE_AND_OUTSIDE:
                ImagePlus dist1 = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = new Duplicator().run(objectsImage.getImagePlus());

                dist1 = BinaryOperations.applyDistanceMap3D(dist1,true,true);
                InvertIntensity.process(distIpl);
                distIpl = BinaryOperations.applyDistanceMap3D(distIpl,true,true);

                new ImageCalculator().process(dist1,distIpl,ImageCalculator.CalculationMethods.ADD,ImageCalculator.OverwriteModes.OVERWRITE_IMAGE2,false,true);

                break;

            case EdgeDistanceModes.INSIDE_ONLY:
                InvertIntensity.process(objectsImage.getImagePlus());
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = BinaryOperations.applyDistanceMap3D(distIpl,true,true);
                break;

            case EdgeDistanceModes.OUTSIDE_ONLY:
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = BinaryOperations.applyDistanceMap3D(distIpl,true,true);
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
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), Double.NaN));
                    return;
                }

                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, edgeDistanceMode);

                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), cs.getMean()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), cs.getStd()));

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
    protected void initialiseMeasurementReferences() {
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_OUTRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_INT_INRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_INT_OUTRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.SUM_INT_INRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.SUM_INT_OUTRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.MEAN_PROXIMITY));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.N_PX_INRANGE));
//        imageMeasurementReferences.add(new MeasurementReference(Measurements.STDEV_PROXIMITY));

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
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        MeasurementReference nPxInrange = imageMeasurementReferences.get(Measurements.N_PX_INRANGE);
        MeasurementReference nPxOutrange = imageMeasurementReferences.get(Measurements.N_PX_OUTRANGE);
        MeasurementReference meanIntInrange = imageMeasurementReferences.get(Measurements.MEAN_INT_INRANGE);
        MeasurementReference meanIntOutrange = imageMeasurementReferences.get(Measurements.MEAN_INT_OUTRANGE);
        MeasurementReference sumIntInrange = imageMeasurementReferences.get(Measurements.SUM_INT_INRANGE);
        MeasurementReference sumIntOutrange = imageMeasurementReferences.get(Measurements.SUM_INT_OUTRANGE);
        MeasurementReference meanProximity = imageMeasurementReferences.get(Measurements.MEAN_PROXIMITY);
        MeasurementReference stdevProximity = imageMeasurementReferences.get(Measurements.STDEV_PROXIMITY);

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                nPxInrange.setCalculated(true);
                nPxOutrange.setCalculated(true);
                meanIntInrange.setCalculated(true);
                meanIntOutrange.setCalculated(true);
                sumIntInrange.setCalculated(true);
                sumIntOutrange.setCalculated(true);
                meanProximity.setCalculated(false);
                stdevProximity.setCalculated(false);

//                nPxInrange.setNickName(getFullName(inputObjectsName, Measurements.N_PX_INRANGE));
//                nPxOutrange.setNickName(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE));
//                meanIntInrange.setNickName(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE));
//                meanIntOutrange.setNickName( getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE));
//                sumIntInrange.setNickName(getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE));
//                sumIntOutrange.setNickName(getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE));

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                nPxInrange.setCalculated(false);
                nPxOutrange.setCalculated(false);
                meanIntInrange.setCalculated(false);
                meanIntOutrange.setCalculated(false);
                sumIntInrange.setCalculated(false);
                sumIntOutrange.setCalculated(false);
                meanProximity.setCalculated(true);
                stdevProximity.setCalculated(true);

//                meanProximity.setNickName(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY));
//                stdevProximity.setNickName(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY));

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
