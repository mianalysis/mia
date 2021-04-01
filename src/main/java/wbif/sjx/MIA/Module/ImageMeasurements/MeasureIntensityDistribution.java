package wbif.sjx.MIA.Module.ImageMeasurements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import ij.ImagePlus;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.StackStatistics;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ObjectMeasurements.Intensity.MeasureRadialIntensityProfile;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.MIA.Process.ColourFactory;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Metadata;

/**
 * Created by Stephen on 17/11/2017.
 */
public class MeasureIntensityDistribution extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASUREMENT_TYPE = "Measurement type";
    public static final String DISTANCE_MAP_IMAGE = "Distance map image";
    public static final String NUMBER_OF_RADIAL_SAMPLES = "Number of radial samples";
    public static final String RANGE_MODE = "Range mode";
    public static final String MIN_DISTANCE = "Minimum distance";
    public static final String MAX_DISTANCE = "Maximum distance";
    public static final String SAVE_PROFILE_MODE = "Save profile mode";
    public static final String PROFILE_FILE_SUFFIX = "Profile file suffix";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String PROXIMAL_DISTANCE = "Proximal distance";
    public static final String SPATIAL_UNITS_MODE = "Spatial units mode";
    public static final String IGNORE_ON_OBJECTS = "Ignore values on objects";
    public static final String EDGE_DISTANCE_MODE = "Edge distance mode";

    public MeasureIntensityDistribution(ModuleCollection modules) {
        super("Measure intensity distribution",modules);
    }


    public interface MeasurementTypes {
        String DISTANCE_PROFILE = "Distance profile";
        String FRACTION_PROXIMAL_TO_OBJECTS = "Fraction proximal to objects";
        String INTENSITY_WEIGHTED_PROXIMITY = "Intensity-weighted proximity";

        String[] ALL = new String[]{DISTANCE_PROFILE,FRACTION_PROXIMAL_TO_OBJECTS,INTENSITY_WEIGHTED_PROXIMITY};

    }

    public interface RangeModes {
        String AUTOMATIC_RANGE = "Automatic range";
        String MANUAL_RANGE = "Manual range";

        String[] ALL = new String[]{AUTOMATIC_RANGE,MANUAL_RANGE};

    }

    public interface SpatialUnitModes {
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

    public interface SaveProfileModes {
        String DONT_SAVE = "Don't save";
        String INDIVIDUAL_FILES = "Individual files";

        String[] ALL = new String[]{DONT_SAVE,INDIVIDUAL_FILES};

    }

    private interface Measurements {
        String N_PX_INRANGE = "N_PX_INRANGE";
        String N_PX_OUTRANGE = "N_PX_OUTRANGE";
        String SUM_INT_INRANGE = "SUM_INT_INRANGE";
        String SUM_INT_OUTRANGE = "SUM_INT_OUTRANGE";
        String MEAN_INT_INRANGE = "MEAN_INT_INRANGE";
        String MEAN_INT_OUTRANGE = "MEAN_INT_OUTRANGE";
        String MEAN_PROXIMITY_PX = "MEAN_PROXIMITY_PX";
        String MEAN_PROXIMITY_CAL = "MEAN_PROXIMITY_${SCAL}";
        String STDEV_PROXIMITY_PX = "STDEV_PROXIMITY_PX";
        String STDEV_PROXIMITY_CAL = "STDEV_PROXIMITY_${SCAL}";

    }


    private String getFullName(String objectsName, String measurement) {
        return "INT_DISTR // "+objectsName+"_"+measurement;
    }

    /*
     * Calculates the bin centroids for the distance measurements from the maximum range in the distance map.
     */
    public static double[] getDistanceBins(Image distanceMap, int nRadialSamples) {
        // Getting the maximum distance measurement
        StackStatistics stackStatistics = new StackStatistics(distanceMap.getImagePlus());
        double minDistance = stackStatistics.min;
        double maxDistance = stackStatistics.max;

        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
        }

        return distanceBins;

    }

    /*
     * Calculates the bin centroids for the distance measurements from provided range values.
     */
    public static double[] getDistanceBins(int nRadialSamples, double minDistance, double maxDistance) {
        double[] distanceBins = new double[nRadialSamples];
        double binWidth = (maxDistance-minDistance)/(nRadialSamples-1);
        for (int i=0;i<nRadialSamples;i++) {
            distanceBins[i] = (i*binWidth)+minDistance;
        }

        return distanceBins;

    }

    public CumStat[] measureFractionProximal(ObjCollection inputObjects, Image inputImage, double proximalDistance, boolean ignoreOnObjects) {
        // Get binary image showing the objects
        HashMap<Integer,Float> hues = ColourFactory.getSingleColourHues(inputObjects,ColourFactory.SingleColours.WHITE);
        Image objectsImage = inputObjects.convertToImage("Objects", hues, 8,false);

        // Calculaing the distance map
        Image distanceImage = DistanceMap.process(objectsImage,"Distance",DistanceMap.WeightModes.WEIGHTS_3_4_5_7, true, false);
         
        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        return measureIntensityFractions(inputImage,distanceImage,ignoreOnObjects,proximalDistance);

    }

    public static CumStat[] measureIntensityFractions(Image inputImage, Image distanceImage, boolean ignoreOnObjects, double proximalDistance) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus distanceIpl = distanceImage.getImagePlus();

        CumStat[] cs = new CumStat[2];
        cs[0] = new CumStat();
        cs[1] = new CumStat();

        for (int z = 0; z < distanceIpl.getNSlices(); z++) {
            for (int c = 0; c < distanceIpl.getNChannels(); c++) {
                for (int t = 0; t < distanceIpl.getNFrames(); t++) {
                    distanceIpl.setPosition(c+1, z+1, t+1);
                    inputIpl.setPosition(c+1, z+1, t+1);

                    float[][] distVals = distanceIpl.getProcessor().getFloatArray();
                    float[][] inputVals = inputIpl.getProcessor().getFloatArray();

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

        distanceIpl.setPosition(1, 1, 1);
        inputIpl.setPosition(1, 1, 1);

        return cs;

    }

    public static CumStat measureIntensityWeightedProximity(ObjCollection inputObjects, Image inputImage, String edgeMode) {
        // Get binary image showing the objects
        HashMap<Integer,Float> hues = ColourFactory.getSingleColourHues(inputObjects,ColourFactory.SingleColours.WHITE);
        Image objectsImage = inputObjects.convertToImage("Objects", hues, 8, false);

        String weightMode = DistanceMap.WeightModes.WEIGHTS_3_4_5_7;
        ImagePlus distIpl = null;
        switch (edgeMode) {
            case EdgeDistanceModes.INSIDE_AND_OUTSIDE:
                ImagePlus dist1 = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = new Duplicator().run(objectsImage.getImagePlus());

                dist1 = DistanceMap.process(dist1,"Distance1", weightMode, true, false);
                InvertIntensity.process(distIpl);
                BinaryOperations2D.process(distIpl,BinaryOperations2D.OperationModes.ERODE,1,1);
                distIpl = DistanceMap.process(distIpl,"Distance2",weightMode, true, false);

                ImageCalculator.process(dist1,distIpl,ImageCalculator.CalculationMethods.ADD,ImageCalculator.OverwriteModes.OVERWRITE_IMAGE2,null,false,true);

                break;

            case EdgeDistanceModes.INSIDE_ONLY:
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                InvertIntensity.process(distIpl);
                BinaryOperations2D.process(distIpl,BinaryOperations2D.OperationModes.ERODE,1,1);
                distIpl = DistanceMap.process(distIpl,"Distance1",weightMode, true, false);
                break;

            case EdgeDistanceModes.OUTSIDE_ONLY:
                distIpl = new Duplicator().run(objectsImage.getImagePlus());
                distIpl = DistanceMap.process(distIpl,"Distance2",weightMode, true, false);
                break;
        }

        Image distanceImage = new Image("Distance map", distIpl);

        // Iterating over all pixels in the input image, adding intensity measurements to CumStat objects (one
        // for pixels in the proximity range, one for pixels outside it).
        return measureWeightedDistance(inputImage,distanceImage);

    }

    public static CumStat[] measureDistanceProfile(Image inputImage, Image distanceImage, double[] distanceBins) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus distanceIpl = distanceImage.getImagePlus().duplicate();

        CumStat[] cumStats = new CumStat[distanceBins.length];
        for (int i=0;i<cumStats.length;i++) cumStats[i] = new CumStat();

        double minDist = distanceBins[0];
        double maxDist = distanceBins[distanceBins.length-1];
        double binWidth = distanceBins[1]-distanceBins[0];

        for (int z = 0; z < distanceIpl.getNSlices(); z++) {
            for (int c = 0; c < distanceIpl.getNChannels(); c++) {
                for (int t = 0; t < distanceIpl.getNFrames(); t++) {
                    distanceIpl.setPosition(c+1, z+1, t+1);
                    inputIpl.setPosition(c+1, z+1, t+1);

                    for (int x=0;x<distanceIpl.getWidth();x++) {
                        for (int y=0;y<distanceIpl.getHeight();y++) {
                            double distance = distanceIpl.getProcessor().getPixelValue(x, y);
                            double intensity = inputIpl.getProcessor().getPixelValue(x, y);

                            if (distance== 0) continue;

                            double bin = Math.round((distance - minDist) / binWidth) * binWidth + minDist;

                            // Ensuring the bin is within the specified range
                            bin = Math.min(bin, maxDist);
                            bin = Math.max(bin, minDist);
                            for (int i=0;i<distanceBins.length;i++) {
                                if (Math.abs(bin-distanceBins[i]) < binWidth/2) cumStats[i].addMeasure(intensity);
                            }
                        }
                    }
                }
            }
        }

        return cumStats;
    }

    public static CumStat measureWeightedDistance(Image inputImage, Image distanceImage) {
        ImagePlus inputIpl = inputImage.getImagePlus();
        ImagePlus distanceIpl = distanceImage.getImagePlus();

        CumStat cumStat = new CumStat();

        for (int z = 0; z < distanceIpl.getNSlices(); z++) {
            for (int c = 0; c < distanceIpl.getNChannels(); c++) {
                for (int t = 0; t < distanceIpl.getNFrames(); t++) {
                    distanceIpl.setPosition(c+1, z+1, t+1);
                    inputIpl.setPosition(c+1, z+1, t+1);

                    for (int x=0;x<distanceIpl.getWidth();x++) {
                        for (int y=0;y<distanceIpl.getHeight();y++) {
                            double distance = distanceIpl.getProcessor().getPixelValue(x, y);
                            double intensity = inputIpl.getProcessor().getPixelValue(x, y);

                            if (distance== 0) continue;

                            cumStat.addMeasure(distance, intensity);

                        }
                    }
                }
            }
        }

        return cumStat;

    }

    public static void writeResultsFile(String path, double[] distanceBins, CumStat[] results) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // Adding header rows for the metadata sheet.
        int rowCount = 0;
        Sheet sheet = workbook.createSheet("Intensity profile");
        Row row = sheet.createRow(rowCount++);

        // Adding headers to the row
        Cell cell = row.createCell(0);
        cell.setCellValue("Distance");

        cell = row.createCell(1);
        cell.setCellValue("Mean");

        cell = row.createCell(2);
        cell.setCellValue("Standard deviation");

        cell = row.createCell(3);
        cell.setCellValue("N measurements");

        // Iterating over the results, adding to new rows
        for (int i=0;i<distanceBins.length;i++) {
            row = sheet.createRow(rowCount++);

            cell = row.createCell(0);
            cell.setCellValue(distanceBins[i]);

            cell = row.createCell(1);
            cell.setCellValue(results[i].getMean());

            cell = row.createCell(2);
            cell.setCellValue(results[i].getStd());

            cell = row.createCell(3);
            cell.setCellValue(results[i].getN());

        }

        // Writing the workbook to file
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch(FileNotFoundException e) {
            try {
                String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String rootPath = FilenameUtils.removeExtension(path);
                String newOutPath = rootPath + "_("+ dateTime + ").xlsx";
                FileOutputStream outputStream = null;

                outputStream = new FileOutputStream(newOutPath);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();

                MIA.log.writeWarning("Target file ("+new File(path).getName()+") inaccessible");
                MIA.log.writeWarning("Saved to alternative file ("+new File(newOutPath).getName()+")");

            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public Category getCategory() {
        return Categories.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "CURRENTLY ONLY WORKS IN 3D";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting parameters
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementType = parameters.getValue(MEASUREMENT_TYPE);
        String distanceMapImageName = parameters.getValue(DISTANCE_MAP_IMAGE);
        int nRadialSample = parameters.getValue(NUMBER_OF_RADIAL_SAMPLES);
        String rangeMode = parameters.getValue(RANGE_MODE);
        double minDistance = parameters.getValue(MIN_DISTANCE);
        double maxDistance = parameters.getValue(MAX_DISTANCE);
        String saveProfileMode = parameters.getValue(SAVE_PROFILE_MODE);
        String profileFileSuffix = parameters.getValue(PROFILE_FILE_SUFFIX);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        double proximalDistance = parameters.getValue(PROXIMAL_DISTANCE);
        String spatialUnits = parameters.getValue(SPATIAL_UNITS_MODE);
        boolean ignoreOnObjects = parameters.getValue(IGNORE_ON_OBJECTS);
        String edgeDistanceMode = parameters.getValue(EDGE_DISTANCE_MODE);

        Image inputImage = workspace.getImages().get(inputImageName);

        if (spatialUnits.equals(SpatialUnitModes.CALIBRATED)) {
            Calibration calibration = inputImage.getImagePlus().getCalibration();
            proximalDistance = calibration.getRawX(proximalDistance);
        }


        Metadata metadata = workspace.getMetadata();

        switch (measurementType) {
            case MeasurementTypes.DISTANCE_PROFILE:
                Image distanceMap = workspace.getImage(distanceMapImageName);

                // Getting the distance bin centroids
                double[] distanceBins = null;
                switch (rangeMode) {
                    case RangeModes.AUTOMATIC_RANGE:
                        distanceBins = getDistanceBins(distanceMap, nRadialSample);
                        break;
                    case RangeModes.MANUAL_RANGE:
                        distanceBins = getDistanceBins(nRadialSample, minDistance, maxDistance);
                        break;
                }

                // Processing each object
                CumStat[] cumStats = measureDistanceProfile(inputImage, distanceMap, distanceBins);
                double[] mean = Arrays.stream(cumStats).mapToDouble(CumStat::getMean).toArray();

                switch (saveProfileMode) {
                    case SaveProfileModes.INDIVIDUAL_FILES:
                        File rootFile =metadata.getFile();
                        String path = rootFile.getParent()+ File.separator +FilenameUtils.removeExtension(rootFile.getName());
                        path = path + "_S" + metadata.getSeriesNumber()  + profileFileSuffix+ ".xlsx";
                        writeResultsFile(path,distanceBins,cumStats);
                        break;
                }

                if (showOutput) {
                    String title = "File: "+metadata.getFilename()+"."+metadata.getExt()+" intensity profile";
                    Plot plot = new Plot(title,"Distance (px)","Mean intensity",distanceBins,mean);
                    plot.show();
                }

                break;

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
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE), Double.NaN));
                    inputImage.addMeasurement(
                            new Measurement(getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE), Double.NaN));

                    return Status.PASS;
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
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE), css[0].getSum()));
                inputImage.addMeasurement(
                        new Measurement(getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE), css[1].getSum()));

                writeStatus("Number of pixels inside range = " + css[0].getN());
                writeStatus("Number of pixels outside range = " + css[1].getN());
                writeStatus("Total intensity in range = " + css[0].getSum());
                writeStatus("Total intensity outside range = " + css[1].getSum());
                writeStatus("Mean intensity in range = " + css[0].getMean());
                writeStatus("Mean intensity outside range = " + css[1].getMean());

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                inputObjects = workspace.getObjects().get(inputObjectsName);

                // Checking if there are any objects to measure
                if (inputObjects.size() == 0) {
                    String name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL);
                    inputImage.addMeasurement(new Measurement(name, Double.NaN));
                    return Status.PASS;
                }

                CumStat cs = measureIntensityWeightedProximity(inputObjects, inputImage, edgeDistanceMode);
                double dppXY = inputImage.getImagePlus().getCalibration().pixelWidth;
                String name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                inputImage.addMeasurement(new Measurement(name, cs.getMean()));
                name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL);
                inputImage.addMeasurement(new Measurement(name, cs.getMean() * dppXY));
                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                inputImage.addMeasurement(new Measurement(name, cs.getStd()));
                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL);
                inputImage.addMeasurement(new Measurement(name, cs.getStd() * dppXY));

                writeStatus("Mean intensity proximity = " + cs.getMean() + " +/- " + cs.getStd());

                break;
        }

        if (showOutput) inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(MEASUREMENT_TYPE, this,MeasurementTypes.DISTANCE_PROFILE, MeasurementTypes.ALL));
        parameters.add(new InputImageP(DISTANCE_MAP_IMAGE,this));
        parameters.add(new IntegerP(NUMBER_OF_RADIAL_SAMPLES,this,10));
        parameters.add(new ChoiceP(RANGE_MODE,this,RangeModes.AUTOMATIC_RANGE,RangeModes.ALL));
        parameters.add(new DoubleP(MIN_DISTANCE,this,0d));
        parameters.add(new DoubleP(MAX_DISTANCE,this,1d));
        parameters.add(new ChoiceP(SAVE_PROFILE_MODE,this,SaveProfileModes.DONT_SAVE,SaveProfileModes.ALL));
        parameters.add(new StringP(PROFILE_FILE_SUFFIX,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new DoubleP(PROXIMAL_DISTANCE, this,2d));
        parameters.add(new ChoiceP(SPATIAL_UNITS_MODE, this, SpatialUnitModes.PIXELS, SpatialUnitModes.ALL));
        parameters.add(new BooleanP(IGNORE_ON_OBJECTS, this, false));
        parameters.add(new ChoiceP(EDGE_DISTANCE_MODE,this, EdgeDistanceModes.INSIDE_AND_OUTSIDE, EdgeDistanceModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_TYPE));

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.DISTANCE_PROFILE:
                returnedParameters.add(parameters.getParameter(DISTANCE_MAP_IMAGE));
                returnedParameters.add(parameters.getParameter(NUMBER_OF_RADIAL_SAMPLES));
                returnedParameters.add(parameters.getParameter(RANGE_MODE));
                switch ((String) parameters.getValue(RANGE_MODE)) {
                    case MeasureRadialIntensityProfile.RangeModes.MANUAL_RANGE:
                        returnedParameters.add(parameters.getParameter(MIN_DISTANCE));
                        returnedParameters.add(parameters.getParameter(MAX_DISTANCE));
                        break;
                }
                returnedParameters.add(parameters.getParameter(SAVE_PROFILE_MODE));
                switch ((String) parameters.getValue(SAVE_PROFILE_MODE)) {
                    case SaveProfileModes.INDIVIDUAL_FILES:
                        returnedParameters.add(parameters.getParameter(PROFILE_FILE_SUFFIX));
                        break;
                }
                break;

            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
                returnedParameters.add(parameters.getParameter(PROXIMAL_DISTANCE));
                returnedParameters.add(parameters.getParameter(SPATIAL_UNITS_MODE));
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
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        switch ((String) parameters.getValue(MEASUREMENT_TYPE)) {
            case MeasurementTypes.FRACTION_PROXIMAL_TO_OBJECTS:
                String name = getFullName(inputObjectsName, Measurements.N_PX_INRANGE);
                ImageMeasurementRef reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.N_PX_OUTRANGE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.MEAN_INT_INRANGE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.MEAN_INT_OUTRANGE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.SUM_INT_INRANGE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.SUM_INT_OUTRANGE);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                break;

            case MeasurementTypes.INTENSITY_WEIGHTED_PROXIMITY:
                name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_PX);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.MEAN_PROXIMITY_CAL);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_PX);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                name = getFullName(inputObjectsName, Measurements.STDEV_PROXIMITY_CAL);
                reference = imageMeasurementRefs.getOrPut(name);
                reference.setImageName(inputImageName);
                returnedRefs.add(reference);

                break;
        }

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
