//package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;
//
//import ij.ImagePlus;
//import ij.plugin.Duplicator;
//import inra.ijpb.binary.ChamferWeights3D;
//import inra.ijpb.plugins.GeodesicDistanceMap3D;
//import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
//import wbif.sjx.ModularImageAnalysis.Module.HCModule;
//import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
//import wbif.sjx.ModularImageAnalysis.Object.*;
//import wbif.sjx.common.MathFunc.CumStat;
//
///**
// * Created by Stephen on 17/11/2017.
// */
//public class MeasureIntensityDistribution extends HCModule {
//    public static final String INPUT_IMAGE = "Input image";
//    public static final String MEASUREMENT_TYPE = "Measurement type";
//    public static final String INPUT_OBJECTS = "Input objects";
//    public static final String PROXIMAL_DISTANCE = "Proximal distance";
//    public static final String SPATIAL_UNITS = "Spatial units";
//    public static final String IGNORE_ON_OBJECTS = "Ignore values on objects";
//
//    private ImageObjReference inputImage;
//    private MeasurementReference nPxInrange;
//    private MeasurementReference nPxOutrange;
//    private MeasurementReference meanIntInrange;
//    private MeasurementReference meanIntOutrange;
//    private MeasurementReference sumIntInrange;
//    private MeasurementReference sumIntOutrange;
//    private MeasurementReference meanProximity;
//    private MeasurementReference stdevProximity;
//
//    String inputImageName = parameters.getParameter(INPUT_IMAGE).getName();
//    String inputObjectsName = parameters.getParameter(INPUT_OBJECTS).getName();
//
//
//    public interface MeasurementTypes {
//        String FRACTION_PROXIMAL_TO_OBJECTS = "Fraction proximal to objects";
//        String INTENSITY_WEIGHTED_PROXIMITY = "Intensity-weighted proximity";
//
//        String[] ALL = new String[]{FRACTION_PROXIMAL_TO_OBJECTS,INTENSITY_WEIGHTED_PROXIMITY};
//
//    }
//
//    public interface SpatialUnits {
//        String CALIBRATED = "Calibrated";
//        String PIXELS = "Pixels";
//
//        String[] ALL = new String[]{CALIBRATED,PIXELS};
//
//    }
//
//    private interface Measurements {
//        String N_PX_INRANGE = "N_PX_INRANGE";
//        String N_PX_OUTRANGE = "N_PX_OUTRANGE";
//        String SUM_INT_INRANGE = "SUM_INT_INRANGE";
//        String SUM_INT_OUTRANGE = "SUM_INT_OUTRANGE";
//        String MEAN_INT_INRANGE = "MEAN_INT_INRANGE";
//        String MEAN_INT_OUTRANGE = "MEAN_INT_OUTRANGE";
//        String MEAN_PROXIMITY = "MEAN_PROXIMITY";
//        String STDEV_PROXIMITY = "STDEV_PROXIMITY";
//
//    }
//
//
//    private String getFullName(String objectsName, String measurement) {
//        return "INT_DISTR//"+objectsName+"_"+measurement;
//    }
//
//    public CumStat[] measureFractionProximal(ObjCollection inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        // Get binary image showing the objects
//        Image objectsImage = ObjectImageConverter.convertObjectsToImage(inputObjects, "Objects image", inputImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, "", true);
//
//        // Calculating a 3D distance map for the binary image
//        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());
//
//        // Inverting the mask intensity
//        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
//            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
//                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
//                    maskIpl.setPosition(c, z, t);
//                    maskIpl.getProcessor().invert();
//                }
//            }
//        }
//        maskIpl.setPosition(1,1,1);
//
//        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
//        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,true);
//
//        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
//        // for pixels in the proximity range, one for pixels outside it).
//        CumStat[] cs = new CumStat[2];
//        cs[0] = new CumStat();
//        cs[1] = new CumStat();
//
//        for (int z = 0; z < distIpl.getNSlices(); z++) {
//            for (int c = 0; c < distIpl.getNChannels(); c++) {
//                for (int t = 0; t < distIpl.getNFrames(); t++) {
//                    distIpl.setPosition(c+1, z+1, t+1);
//                    inputImagePlus.setPosition(c+1, z+1, t+1);
//
//                    float[][] distVals = distIpl.getProcessor().getFloatArray();
//                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();
//
//                    for (int x=0;x<distVals.length;x++) {
//                        for (int y=0;y<distVals[0].length;y++) {
//                            float dist = distVals[x][y];
//                            float val = inputVals[x][y];
//
//                            if (ignoreOnObjects && dist == 0) continue;
//
//                            if (dist <= proximalDistance) {
//                                cs[0].addMeasure(val);
//                            } else {
//                                cs[1].addMeasure(val);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        distIpl.setPosition(1, 1, 1);
//        inputImagePlus.setPosition(1, 1, 1);
//
//        return cs;
//
//    }
//
//    public CumStat measureIntensityWeightedProximity(ObjCollection inputObjects, Image inputImage, boolean ignoreOnObjects) {
//        ImagePlus inputImagePlus = inputImage.getImagePlus();
//
//        // Get binary image showing the objects
//        Image objectsImage = ObjectImageConverter.convertObjectsToImage(inputObjects, "Objects image", inputImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, "", true);
//
//        // Calculating a 3D distance map for the binary image
//        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());
//
//        // Inverting the mask intensity
//        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
//            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
//                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
//                    maskIpl.setPosition(c, z, t);
//                    maskIpl.getProcessor().invert();
//                }
//            }
//        }
//        maskIpl.setPosition(1,1,1);
//
//        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
//        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,true);
//
//        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
//        // for pixels in the proximity range, one for pixels outside it).
//        CumStat cs = new CumStat();
//
//        for (int z = 0; z < distIpl.getNSlices(); z++) {
//            for (int c = 0; c < distIpl.getNChannels(); c++) {
//                for (int t = 0; t < distIpl.getNFrames(); t++) {
//                    distIpl.setPosition(c+1, z+1, t+1);
//                    inputImagePlus.setPosition(c+1, z+1, t+1);
//
//                    float[][] distVals = distIpl.getProcessor().getFloatArray();
//                    float[][] inputVals = inputImagePlus.getProcessor().getFloatArray();
//
//                    for (int x=0;x<distVals.length;x++) {
//                        for (int y=0;y<distVals[0].length;y++) {
//                            float dist = distVals[x][y];
//                            float val = inputVals[x][y];
//
//                            if (ignoreOnObjects && dist == 0) continue;
//
//                            cs.addMeasure(dist,val);
//
//                        }
//                    }
//                }
//            }
//        }
//
//        distIpl.setPosition(1, 1, 1);
//        inputImagePlus.setPosition(1, 1, 1);
//
//        return cs;
//    }
//
//    @Override
//    public String getTitle() {
//        return "Measure intensity distribution";
//    }
//
//    @Override
//    public String getHelp() {
//        return "CURRENTLY ONLY WORKS IN 3D";
//    }
//
//    @Override
//    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
//        // Getting parameters
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//        String measurementType = parameters.getValue(MEASUREMENT_TYPE);
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//        double proximalDistance = parameters.getValue(PROXIMAL_DISTANCE);
//        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
//        boolean ignoreOnObjects = parameters.getValue(IGNORE_ON_OBJECTS);
//
//        Image inputImage = workspace.getImages().get(inputImageName);
//
//        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
//            proximalDistance = inputImage.getImagePlus().getCalibration().getRawX(proximalDistance);
//        }
//
//        switch (measurementType) {
//            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
//                ObjCollection inputObjects = workspace.getObjects().get(inputObjectsName);
//
//                // Checking if there are any objects to measure
//                if (inputObjects.size() == 0) {
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), Double.NaN));
//                    return;
//                }
//
//                CumStat[] css = measureFractionProximal(inputObjects, inputImage, proximalDistance, ignoreOnObjects);
//
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_INRANGE), css[0].getN()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE), css[1].getN()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE), css[0].getMean()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE), css[1].getMean()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), css[0].getSum()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), css[1].getSum()));
//
//                if (verbose) System.out.println("[" + moduleName + "] Number of pixels inside range = " + css[0].getN());
//                if (verbose) System.out.println("[" + moduleName + "] Number of pixels outside range = " + css[1].getN());
//                if (verbose) System.out.println("[" + moduleName + "] Total intensity in range = " + css[0].getSum());
//                if (verbose) System.out.println("[" + moduleName + "] Total intensity outside range = " + css[1].getSum());
//                if (verbose) System.out.println("[" + moduleName + "] Mean intensity in range = " + css[0].getMean());
//                if (verbose) System.out.println("[" + moduleName + "] Mean intensity outside range = " + css[1].getMean());
//
//                break;
//
//            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
//                inputObjects = workspace.getObjects().get(inputObjectsName);
//
//                // Checking if there are any objects to measure
//                if (inputObjects.size() == 0) {
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), Double.NaN));
//                    inputImage.addMeasurement(
//                            new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), Double.NaN));
//                    return;
//                }
//
//                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, ignoreOnObjects);
//
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY), cs.getMean()));
//                inputImage.addMeasurement(
//                        new Measurement(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY), cs.getStd()));
//
//                if (verbose)
//                    System.out.println("[" + moduleName + "] Mean intensity proximity = " + cs.getMean() + " +/- "+cs.getStd());
//
//                break;
//
//        }
//    }
//
//    @Override
//    public void initialiseParameters() {
//        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
//        parameters.addParameter(new Parameter(MEASUREMENT_TYPE, Parameter.CHOICE_ARRAY,
//                MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS, MeasurementTypes.ALL));
//        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
//        parameters.addParameter(new Parameter(PROXIMAL_DISTANCE, Parameter.DOUBLE,2d));
//        parameters.addParameter(new Parameter(SPATIAL_UNITS, Parameter.CHOICE_ARRAY, SpatialUnits.PIXELS, SpatialUnits.ALL));
//        parameters.addParameter(new Parameter(IGNORE_ON_OBJECTS, Parameter.BOOLEAN,true));
//
//    }
//
//    @Override
//    public ParameterCollection updateAndGetParameters() {
//        ParameterCollection returnedParameters = new ParameterCollection();
//        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
//        returnedParameters.addParameter(parameters.getParameter(MEASUREMENT_TYPE));
//
//        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
//            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
//                returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
//                returnedParameters.addParameter(parameters.getParameter(PROXIMAL_DISTANCE));
//                returnedParameters.addParameter(parameters.getParameter(SPATIAL_UNITS));
//
//                break;
//
//            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
//                returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
//
//                break;
//
//        }
//
//        returnedParameters.addParameter(parameters.getParameter(IGNORE_ON_OBJECTS));
//
//        return returnedParameters;
//
//    }
//
//    @Override
//    public void initialiseImageReferences() {
//        inputImage = new ImageObjReference();
//        imageReferences.add(inputImage);
//
//        nPxInrange = new MeasurementReference(Measurements.N_PX_INRANGE);
//        nPxOutrange = new MeasurementReference(Measurements.N_PX_OUTRANGE);
//        meanIntInrange = new MeasurementReference(Measurements.MEAN_INT_INRANGE);
//        meanIntOutrange = new MeasurementReference(Measurements.MEAN_INT_OUTRANGE);
//        sumIntInrange = new MeasurementReference(Measurements.SUM_INT_INRANGE);
//        sumIntOutrange = new MeasurementReference(Measurements.SUM_INT_OUTRANGE);
//        meanProximity = new MeasurementReference(Measurements.MEAN_PROXIMITY);
//        stdevProximity = new MeasurementReference(Measurements.STDEV_PROXIMITY);
//
//        inputImage.addMeasurementReference(nPxInrange);
//        inputImage.addMeasurementReference(nPxOutrange);
//        inputImage.addMeasurementReference(meanIntInrange);
//        inputImage.addMeasurementReference(meanIntOutrange);
//        inputImage.addMeasurementReference(sumIntInrange);
//        inputImage.addMeasurementReference(sumIntOutrange);
//        inputImage.addMeasurementReference(meanProximity);
//        inputImage.addMeasurementReference(stdevProximity);
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetImageReferences() {
//        inputImage.setName(parameters.getValue(INPUT_IMAGE));
//
//        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
//
//        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
//            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
//                nPxInrange.setCalculated(true);
//                nPxOutrange.setCalculated(true);
//                meanIntInrange.setCalculated(true);
//                meanIntOutrange.setCalculated(true);
//                sumIntInrange.setCalculated(true);
//                sumIntOutrange.setCalculated(true);
//                meanProximity.setCalculated(false);
//                stdevProximity.setCalculated(false);
//
//                nPxInrange.setName(getFullName(inputObjectsName, Measurements.N_PX_INRANGE));
//                nPxOutrange.setName(getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE));
//                meanIntInrange.setName(getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE));
//                meanIntOutrange.setName( getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE));
//                sumIntInrange.setName(getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE));
//                sumIntOutrange.setName(getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE));
//
//                break;
//
//            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
//                nPxInrange.setCalculated(false);
//                nPxOutrange.setCalculated(false);
//                meanIntInrange.setCalculated(false);
//                meanIntOutrange.setCalculated(false);
//                sumIntInrange.setCalculated(false);
//                sumIntOutrange.setCalculated(false);
//                meanProximity.setCalculated(true);
//                stdevProximity.setCalculated(true);
//
//                meanProximity.setName(getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY));
//                stdevProximity.setName(getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY));
//
//                break;
//        }
//
//        return imageReferences;
//
//    }
//
//    @Override
//    public ReferenceCollection updateAndGetObjectReferences() {
//        return null;
//    }
//
//    @Override
//    public void addRelationships(RelationshipCollection relationships) {
//
//    }
//}
