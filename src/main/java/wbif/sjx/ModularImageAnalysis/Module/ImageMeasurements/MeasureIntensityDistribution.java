package wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import inra.ijpb.binary.ChamferWeights3D;
import inra.ijpb.plugins.GeodesicDistanceMap3D;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.ObjectImageConverter;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.MathFunc.CumStat;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistribution extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASUREMENT_TYPE = "Measurement type";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROXIMAL_DISTANCE = "Proximal distance";
    public static final String SPATIAL_UNITS = "Spatial units";
    public static final String IGNORE_ON_OBJECTS = "Ignore values on objects";

    public interface MeasurementTypes {
        String FRACTION_PROXIMAL_TO_OBJECTS = "Fraction proximal to objects";
        String INTENSITY_WEIGHTED_PROXIMITY = "Intensity-weighted proximity";

        String[] ALL = new String[]{FRACTION_PROXIMAL_TO_OBJECTS,INTENSITY_WEIGHTED_PROXIMITY};

    }

    public interface SpatialUnits {
        String CALIBRATED = "Calibrated";
        String PIXELS = "Pixels";

        String[] ALL = new String[]{CALIBRATED,PIXELS};

    }


    public CumStat[] measureFractionProximal(ObjSet inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        Image objectsImage = ObjectImageConverter.convertObjectsToImage(inputObjects, "Objects image", inputImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, "", true);

        // Calculating a 3D distance map for the binary image
        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());

        // Inverting the mask intensity
        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
                    maskIpl.setPosition(c, z, t);
                    maskIpl.getProcessor().invert();
                }
            }
        }
        maskIpl.setPosition(1,1,1);

        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,false);

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
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

    public CumStat measureIntensityWeightedProximity(ObjSet inputObjects, Image inputImage, boolean ignoreOnObjects) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Get binary image showing the objects
        Image objectsImage = ObjectImageConverter.convertObjectsToImage(inputObjects, "Objects image", inputImage, ObjectImageConverter.ColourModes.SINGLE_COLOUR, "", true);

        // Calculating a 3D distance map for the binary image
        ImagePlus maskIpl = new Duplicator().run(objectsImage.getImagePlus());

        // Inverting the mask intensity
        for (int z = 1; z <= maskIpl.getNSlices(); z++) {
            for (int c = 1; c <= maskIpl.getNChannels(); c++) {
                for (int t = 1; t <= maskIpl.getNFrames(); t++) {
                    maskIpl.setPosition(c, z, t);
                    maskIpl.getProcessor().invert();
                }
            }
        }
        maskIpl.setPosition(1,1,1);

        maskIpl.show();
        objectsImage.getImagePlus().show();

        float[] weights = ChamferWeights3D.WEIGHTS_3_4_5_7.getFloatWeights();
        ImagePlus distIpl = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist",weights,false);
        distIpl.show();

        maskIpl.setCalibration(null);
        objectsImage.getImagePlus().setCalibration(null);

        ImagePlus distIpl2 = new GeodesicDistanceMap3D().process(objectsImage.getImagePlus(),maskIpl,"Dist2",weights,false);
        distIpl2.show();

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
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

                            if (ignoreOnObjects && dist == 0) continue;

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
    protected void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementType = parameters.getValue(MEASUREMENT_TYPE);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        double proximalDistance = parameters.getValue(PROXIMAL_DISTANCE);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS);
        boolean ignoreOnObjects = parameters.getValue(IGNORE_ON_OBJECTS);

        Image inputImage = workspace.getImages().get(inputImageName);

        if (spatialUnits.equals(SpatialUnits.CALIBRATED)) {
            proximalDistance = inputImage.getImagePlus().getCalibration().getRawX(proximalDistance);
        }

        switch (measurementType) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                ObjSet inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.N_PX_INRANGE, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.N_PX_OUTRANGE, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.SUM_I_INRANGE, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.SUM_I_OUTRANGE, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_INRANGE, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_OUTRANGE, Double.NaN));
                    return;
                }

                CumStat[] css = measureFractionProximal(inputObjects, inputImage, proximalDistance, ignoreOnObjects);

                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.N_PX_INRANGE, css[0].getN()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.N_PX_OUTRANGE, css[1].getN()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.SUM_I_INRANGE, css[0].getSum()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.SUM_I_OUTRANGE, css[1].getSum()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_INRANGE, css[0].getMean()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_OUTRANGE, css[1].getMean()));

                if (verbose) System.out.println("[" + moduleName + "] Number of pixels inside range = " + css[0].getN());
                if (verbose) System.out.println("[" + moduleName + "] Number of pixels outside range = " + css[1].getN());
                if (verbose) System.out.println("[" + moduleName + "] Total intensity in range = " + css[0].getSum());
                if (verbose) System.out.println("[" + moduleName + "] Total intensity outside range = " + css[1].getSum());
                if (verbose) System.out.println("[" + moduleName + "] Mean intensity in range = " + css[0].getMean());
                if (verbose) System.out.println("[" + moduleName + "] Mean intensity outside range = " + css[1].getMean());

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_PROXIMITY, Double.NaN));
                    inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.STD_I_PROXIMITY, Double.NaN));
                    return;
                }

                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, ignoreOnObjects);

                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.MEAN_I_PROXIMITY, cs.getMean()));
                inputImage.addMeasurement(new MIAMeasurement(MIAMeasurement.STD_I_PROXIMITY, cs.getStd()));

                if (verbose) System.out.println("[" + moduleName + "] Mean intensity proximity = " + cs.getMean() + " +/- "+cs.getStd());

                break;

        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(MEASUREMENT_TYPE, Parameter.CHOICE_ARRAY,MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS,MeasurementTypes.ALL));
        parameters.addParameter(new Parameter(INPUT_OBJECTS, Parameter.INPUT_OBJECTS,null));
        parameters.addParameter(new Parameter(PROXIMAL_DISTANCE, Parameter.DOUBLE,2d));
        parameters.addParameter(new Parameter(SPATIAL_UNITS, Parameter.CHOICE_ARRAY, SpatialUnits.PIXELS, SpatialUnits.ALL));
        parameters.addParameter(new Parameter(IGNORE_ON_OBJECTS, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(MEASUREMENT_TYPE));

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.addParameter(parameters.getParameter(PROXIMAL_DISTANCE));
                returnedParameters.addParameter(parameters.getParameter(SPATIAL_UNITS));

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                returnedParameters.addParameter(parameters.getParameter(INPUT_OBJECTS));

                break;

        }

        returnedParameters.addParameter(parameters.getParameter(IGNORE_ON_OBJECTS));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {
        String inputImageName = parameters.getParameter(INPUT_IMAGE).getName();

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                measurements.addMeasurement(inputImageName,MIAMeasurement.N_PX_INRANGE);
                measurements.addMeasurement(inputImageName,MIAMeasurement.N_PX_OUTRANGE);
                measurements.addMeasurement(inputImageName,MIAMeasurement.SUM_I_INRANGE);
                measurements.addMeasurement(inputImageName,MIAMeasurement.SUM_I_OUTRANGE);
                measurements.addMeasurement(inputImageName,MIAMeasurement.MEAN_I_INRANGE);
                measurements.addMeasurement(inputImageName,MIAMeasurement.MEAN_I_OUTRANGE);

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                measurements.addMeasurement(inputImageName,MIAMeasurement.MEAN_I_PROXIMITY);
                measurements.addMeasurement(inputImageName,MIAMeasurement.STD_I_PROXIMITY);

                break;

        }
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
