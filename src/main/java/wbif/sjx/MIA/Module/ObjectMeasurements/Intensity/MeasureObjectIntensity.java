// TODO: Could addRef an optional parameter to select the channel of the input image to use for measurement

package wbif.sjx.MIA.Module.ObjectMeasurements.Intensity;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureIntensityDistribution;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.BinaryOperations2D;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary.DistanceMap;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageCalculator;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.ImageMath;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.Object.Point;

import com.drew.lang.annotations.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by sc13967 on 05/05/2017.
 */
public class MeasureObjectIntensity extends Module {
    public static final String INPUT_SEPARATOR = "Object and image input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String INPUT_IMAGE = "Input image";

    public static final String WEIGHTED_CENTRE_SEPARATOR = "Weighted centre";
    public static final String MEASURE_WEIGHTED_CENTRE = "Measure weighted centre";

    public static final String WEIGHTED_DISTANCE_TO_EDGE_SEPARATOR = "Weighted distance to edge";
    public static final String MEASURE_WEIGHTED_EDGE_DISTANCE = "Measure weighted distance to edge";
    public static final String EDGE_DISTANCE_MODE = "Edge distance mode";

    public static final String INTENSITY_PROFILE_SEPARATOR = "Intensity profile from edge";
    public static final String MEASURE_EDGE_INTENSITY_PROFILE = "Measure intensity profile from edge";
    public static final String MINIMUM_DISTANCE = "Minimum distance";
    public static final String MAXIMUM_DISTANCE = "Maximum distance";
    public static final String CALIBRATED_DISTANCES = "Calibrated distances";
    public static final String NUMBER_OF_MEASUREMENTS = "Number of measurements";
    public static final String ONLY_MEASURE_ON_MASK = "Only measure on masked regions";
    public static final String MASK_IMAGE = "Mask image";


    public MeasureObjectIntensity(ModuleCollection modules) {
        super("Measure object intensity",modules);
    }

    public interface Measurements {
        String MEAN = "MEAN";
        String MIN = "MIN";
        String MAX = "MAX";
        String SUM = "SUM";
        String STDEV = "STDEV";

        String X_CENT_MEAN = "X_CENTRE_MEAN (PX)";
        String X_CENT_STDEV = "X_CENTRE_STDEV (PX)";
        String Y_CENT_MEAN = "Y_CENTRE_MEAN (PX)";
        String Y_CENT_STDEV = "Y_CENTRE_STDEV (PX)";
        String Z_CENT_MEAN = "Z_CENTRE_MEAN (SLICE)";
        String Z_CENT_STDEV = "Z_CENTRE_STDEV (SLICE)";

        String MEAN_EDGE_DISTANCE_PX = "MEAN_EDGE_DISTANCE (PX)";
        String MEAN_EDGE_DISTANCE_CAL = "MEAN_EDGE_DISTANCE (${CAL})";
        String STD_EDGE_DISTANCE_PX = "STD_EDGE_DISTANCE (PX)";
        String STD_EDGE_DISTANCE_CAL = "STD_EDGE_DISTANCE (${CAL})";

        String EDGE_PROFILE = "EDGE_PROFILE";

    }

    public interface EdgeDistanceModes extends MeasureIntensityDistribution.EdgeDistanceModes {}


    public static String getFullName(String imageName, String measurement) {
        return "INTENSITY // "+imageName+"_"+measurement;
    }

    private double[] getProfileBins(double minDist, double maxDist, int nMeasurements) {
        double[] binNames = new double[nMeasurements];

        double binWidth = (maxDist-minDist)/(nMeasurements-1);
        for (int i=0;i<nMeasurements;i++) binNames[i] = (i*binWidth)+minDist;

        return binNames;

    }

    private String getBinNameFormat(boolean calibratedDistances) {
        if (calibratedDistances) {
            return "0.00E0";
        } else {
            return "#.00";
        }
    }

    private void measureIntensity(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        int t = object.getT();
        CumStat cs = new CumStat();

        for (Point<Integer> point:object.getCoordinateSet()) {
            ipl.setPosition(1,point.getZ()+1,t+1);
            float value = ipl.getProcessor().getf(point.getX(),point.getY());
            cs.addMeasure(value);
        }

        // Calculating mean, std, min and max intensity
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MEAN), cs.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MIN), cs.getMin()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.MAX), cs.getMax()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.STDEV), cs.getStd(CumStat.SAMPLE)));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.SUM), cs.getSum()));

    }

    private void measureWeightedCentre(Obj object, ImagePlus ipl) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);

        // Initialising the cumulative statistics objects to store pixel intensities in each direction.
        CumStat csX = new CumStat();
        CumStat csY = new CumStat();
        CumStat csZ = new CumStat();

        // Getting pixel coordinates
        ArrayList<Integer> x = object.getXCoords();
        ArrayList<Integer> y = object.getYCoords();
        ArrayList<Integer> z = object.getZCoords();
        int tPos = object.getT();

        // Running through all pixels in this object and adding the intensity to the MultiCumStat object
        for (int i=0;i<x.size();i++) {
            ipl.setPosition(1,z.get(i)+1,tPos+1);
            csX.addMeasure(x.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csY.addMeasure(y.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));
            csZ.addMeasure(z.get(i),ipl.getProcessor().getPixelValue(x.get(i),y.get(i)));

        }

        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_MEAN), csX.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.X_CENT_STDEV), csX.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_MEAN), csY.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Y_CENT_STDEV), csY.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_MEAN), csZ.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName,Measurements.Z_CENT_STDEV), csZ.getStd()));

    }

    private void measureWeightedEdgeDistance(Obj object, Image image) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);
        String edgeDistanceMode = parameters.getValue(EDGE_DISTANCE_MODE);

        ObjCollection collection = new ObjCollection(object.getName(),object.getSpatialCalibration(),object.getNFrames());
        collection.add(object);
        CumStat cs = MeasureIntensityDistribution.measureIntensityWeightedProximity(collection,image,edgeDistanceMode);

        double distPerPxXY = object.getDppXY();

        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN_EDGE_DISTANCE_PX), cs.getMean()));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.MEAN_EDGE_DISTANCE_CAL), cs.getMean()*distPerPxXY));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.STD_EDGE_DISTANCE_PX), cs.getStd()));
        object.addMeasurement(new Measurement(getFullName(imageName, Measurements.STD_EDGE_DISTANCE_CAL), cs.getStd()*distPerPxXY));

    }

    private void measureEdgeIntensityProfile(Obj object, ImagePlus intensityIpl, @Nullable Image maskImage) {
        // Getting parameters
        String imageName = parameters.getValue(INPUT_IMAGE);
        double minDist = parameters.getValue(MINIMUM_DISTANCE);
        double maxDist = parameters.getValue(MAXIMUM_DISTANCE);
        boolean calibratedDistances = parameters.getValue(CALIBRATED_DISTANCES);
        int nMeasurements = parameters.getValue(NUMBER_OF_MEASUREMENTS);
        double distPerPxXY = object.getDppXY();

        // Setting up CumStats to hold results
        LinkedHashMap<Double,CumStat> cumStats = new LinkedHashMap<>();
        double binWidth = (maxDist-minDist)/(nMeasurements-1);
        double[] bins = getProfileBins(minDist,maxDist,nMeasurements);
        for (int i=0;i<nMeasurements;i++) cumStats.put(bins[i],new CumStat());

        // Creating an object image
        ImagePlus objIpl = object.convertObjToImage("Inside dist").getImagePlus();

        // Calculating the distance maps.  The inside map is set to negative
        ImagePlus outsideDistIpl = DistanceMap.getDistanceMap(objIpl,true);
        InvertIntensity.process(objIpl);
        BinaryOperations2D.process(objIpl,BinaryOperations2D.OperationModes.ERODE,1,1);
        ImagePlus insideDistIpl = DistanceMap.getDistanceMap(objIpl,true);
        ImageMath.process(insideDistIpl,ImageMath.CalculationTypes.MULTIPLY,-1.0);
        ImagePlus distIpl = ImageCalculator.process(insideDistIpl,outsideDistIpl,ImageCalculator.CalculationMethods.ADD,
                ImageCalculator.OverwriteModes.CREATE_NEW,true,true);

        // Iterating over each pixel in the image, adding that intensity value to the corresponding bin
        int nChannels = distIpl.getNChannels();
        int nSlices = distIpl.getNSlices();
        int nFrames = distIpl.getNFrames();

        // Checking the number of dimensions.  If a dimension of image2 is 1 this dimension is used for all images.
        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    distIpl.setPosition(c,z,t);
                    intensityIpl.setPosition(c,z,t);

                    ImageProcessor distIpr = distIpl.getProcessor();
                    ImageProcessor intensityIpr = intensityIpl.getProcessor();

                    for (int x=0;x<distIpl.getWidth();x++) {
                        for (int y=0;y<distIpl.getHeight();y++) {
                            // If only considering points on mask objects
                            if (maskImage != null) {
                                maskImage.getImagePlus().setPosition(c,z,t);
                                if (maskImage.getImagePlus().getProcessor().getf(x,y) == 255) continue;
                            }

                            // Determining which bin to use
                            double dist = distIpr.getf(x,y);

                            // If using calibrated distances, this must be converted back to calibrated units from px
                            if (calibratedDistances) dist = dist*distPerPxXY;
                            double bin = Math.round((dist-minDist)/binWidth)*binWidth+minDist;

                            // Ensuring the bin is within the specified range
                            bin = Math.min(bin,maxDist);
                            bin = Math.max(bin,minDist);

                            // Adding the measurement to the relevant bin
                            double intensity = intensityIpr.getf(x,y);
                            cumStats.get(bin).addMeasure(intensity);

                        }
                    }
                }
            }
        }

        int nDigits = (int) Math.log10(bins.length)+1;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<nDigits;i++) stringBuilder.append("0");
        DecimalFormat intFormat = new DecimalFormat(stringBuilder.toString());
        String units = (boolean) parameters.getValue(CALIBRATED_DISTANCES) ? Units.getOMEUnits().getSymbol() : "PX";

        DecimalFormat decFormat = new DecimalFormat(getBinNameFormat(calibratedDistances));

        int count = 0;
        for (CumStat cumStat:cumStats.values()) {
            String profileMeasName = Measurements.EDGE_PROFILE + "_BIN" + intFormat.format(count+1)
                    +"_("+decFormat.format(bins[count++])+units+")";
            object.addMeasurement(new Measurement(getFullName(imageName,profileMeasName), cumStat.getMean()));
        }
    }


    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_INTENSITY;
    }

    @Override
    public String getDescription() {
        return "Measure intensity of each object in a specified image.  Measurements of intensity are taken at all pixel coordinates corresponding to each object.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input objects
        String objectName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection objects = workspace.getObjects().get(objectName);

        // Getting input image
        String imageName = parameters.getValue(INPUT_IMAGE);
        Image image = workspace.getImages().get(imageName);
        ImagePlus ipl = image.getImagePlus();

        // Measuring intensity for each object and adding the measurement to that object
        for (Obj object:objects.values()) measureIntensity(object,ipl);

        // If specified, measuring weighted centre for intensity
        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            for (Obj object:objects.values()) measureWeightedCentre(object,ipl);
        }

        // If specified, measuring weighted distance to the object edge
        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            for (Obj object:objects.values()) measureWeightedEdgeDistance(object,image);
        }

        // If specified, measuring intensity profiles relative to the object edge
        if ((boolean) parameters.getValue(MEASURE_EDGE_INTENSITY_PROFILE)) {
            Image maskImage = null;
            if ((boolean) parameters.getValue(ONLY_MEASURE_ON_MASK)) {
                maskImage = workspace.getImage(parameters.getValue(MASK_IMAGE));
            }
            for (Obj object:objects.values()) measureEdgeIntensityProfile(object,ipl,maskImage);
        }

        if (showOutput) objects.showMeasurements(this,modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(WEIGHTED_CENTRE_SEPARATOR,this));
        parameters.add(new BooleanP(MEASURE_WEIGHTED_CENTRE, this, false));

        parameters.add(new ParamSeparatorP(WEIGHTED_DISTANCE_TO_EDGE_SEPARATOR,this));
        parameters.add(new BooleanP(MEASURE_WEIGHTED_EDGE_DISTANCE, this, false));
        parameters.add(new ChoiceP(EDGE_DISTANCE_MODE,this,EdgeDistanceModes.INSIDE_AND_OUTSIDE,EdgeDistanceModes.ALL));

        parameters.add(new ParamSeparatorP(INTENSITY_PROFILE_SEPARATOR,this));
        parameters.add(new BooleanP(MEASURE_EDGE_INTENSITY_PROFILE,this,false));
        parameters.add(new DoubleP(MINIMUM_DISTANCE,this,0d));
        parameters.add(new DoubleP(MAXIMUM_DISTANCE,this,1d));
        parameters.add(new BooleanP(CALIBRATED_DISTANCES, this,false));
        parameters.add(new IntegerP(NUMBER_OF_MEASUREMENTS,this,10));
        parameters.add(new BooleanP(ONLY_MEASURE_ON_MASK,this,false));
        parameters.add(new InputImageP(MASK_IMAGE,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(WEIGHTED_CENTRE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_CENTRE));

        returnedParameters.add(parameters.getParameter(WEIGHTED_DISTANCE_TO_EDGE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_WEIGHTED_EDGE_DISTANCE));
        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            returnedParameters.add(parameters.getParameter(EDGE_DISTANCE_MODE));
        }

        returnedParameters.add(parameters.getParameter(INTENSITY_PROFILE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASURE_EDGE_INTENSITY_PROFILE));
        if ((boolean) parameters.getValue(MEASURE_EDGE_INTENSITY_PROFILE)) {
            returnedParameters.add(parameters.getParameter(MINIMUM_DISTANCE));
            returnedParameters.add(parameters.getParameter(MAXIMUM_DISTANCE));
            returnedParameters.add(parameters.getParameter(CALIBRATED_DISTANCES));
            returnedParameters.add(parameters.getParameter(NUMBER_OF_MEASUREMENTS));
            returnedParameters.add(parameters.getParameter(ONLY_MEASURE_ON_MASK));
            if ((boolean) parameters.getValue(ONLY_MEASURE_ON_MASK)) {
                returnedParameters.add(parameters.getParameter(MASK_IMAGE));
            }
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        String name = getFullName(inputImageName,Measurements.MEAN);
        ObjMeasurementRef mean = objectMeasurementRefs.getOrPut(name);
        mean.setObjectsName(inputObjectsName);
        mean.setDescription("Mean intensity of pixels from the image \""+inputImageName+"\" contained within each" +
                " \""+inputObjectsName+"\" object");
        returnedRefs.add(mean);

        name = getFullName(inputImageName,Measurements.MIN);
        ObjMeasurementRef min = objectMeasurementRefs.getOrPut(name);
        min.setObjectsName(inputObjectsName);
        min.setDescription("Minimum intensity of pixels from the image \""+inputImageName+"\" contained within each" +
                " \""+inputObjectsName+"\" object");
        returnedRefs.add(min);

        name = getFullName(inputImageName,Measurements.MAX);
        ObjMeasurementRef max = objectMeasurementRefs.getOrPut(name);
        max.setObjectsName(inputObjectsName);
        max.setDescription("Maximum intensity of pixels from the image \""+inputImageName+"\" contained within each" +
                " \""+inputObjectsName+"\" object");
        returnedRefs.add(max);

        name = getFullName(inputImageName,Measurements.STDEV);
        ObjMeasurementRef stdev = objectMeasurementRefs.getOrPut(name);
        stdev.setObjectsName(inputObjectsName);
        stdev.setDescription("Standard deviation of intensity of pixels from the image \""+inputImageName+"\" " +
                "contained within each \""+inputObjectsName+"\" object");
        returnedRefs.add(stdev);

        name = getFullName(inputImageName,Measurements.SUM);
        ObjMeasurementRef sum = objectMeasurementRefs.getOrPut(name);
        sum.setObjectsName(inputObjectsName);
        sum.setDescription("Sum intensity of pixels from the image \""+inputImageName+"\" contained within each" +
                " \""+inputObjectsName+"\" object");
        returnedRefs.add(sum);

        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_CENTRE)) {
            name = getFullName(inputImageName, Measurements.X_CENT_MEAN);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted x-position for each \""+inputObjectsName+"\" object, " +
                    "with weighting coming from the image \""+inputImageName+"\".  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.X_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted x-position for each \""
                    +inputObjectsName+"\" object, with weighting coming from the image \""+inputImageName+"\".  " +
                    "Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Y_CENT_MEAN);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted y-position for each \""+inputObjectsName+"\" object, " +
                    "with weighting coming from the image \""+inputImageName+"\".  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Y_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted y-position for each \""
                    +inputObjectsName+"\" object, with weighting coming from the image \""+inputImageName+"\".  " +
                    "Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Z_CENT_MEAN);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity weighted z-position for each \""+inputObjectsName+"\" object, " +
                    "with weighting coming from the image \""+inputImageName+"\".  Measured in slice units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.Z_CENT_STDEV);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation of intensity weighted z-position for each \""
                    +inputObjectsName+"\" object, with weighting coming from the image \""+inputImageName+"\".  " +
                    "Measured in slice units.");
            returnedRefs.add(reference);

        }

        if ((boolean) parameters.getValue(MEASURE_WEIGHTED_EDGE_DISTANCE)) {
            name = getFullName(inputImageName, Measurements.MEAN_EDGE_DISTANCE_PX);
            ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity-weighted distance of all signal in the image, \""+inputImageName
                    +"\", to each object, \""+inputObjectsName+"\".  This value will get smaller as the brightest " +
                    "regions of the image get closer to the input objects.  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.MEAN_EDGE_DISTANCE_CAL);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Mean intensity-weighted distance of all signal in the image, \""+inputImageName
                    +"\", to each object, \""+inputObjectsName+"\".  This value will get smaller as the brightest " +
                    "regions of the image get closer to the input objects.  Measured in calibrated ("
                    +Units.getOMEUnits().getSymbol()+") units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.STD_EDGE_DISTANCE_PX);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation intensity-weighted distance of all signal in the image, \""
                    +inputImageName+"\", to each object, \""+inputObjectsName+"\".  This value will get smaller as " +
                    "the brightest regions of the image get closer to the input objects.  Measured in pixel units.");
            returnedRefs.add(reference);

            name = getFullName(inputImageName, Measurements.STD_EDGE_DISTANCE_CAL);
            reference = objectMeasurementRefs.getOrPut(name);
            reference.setObjectsName(inputObjectsName);
            reference.setDescription("Standard deviation intensity-weighted distance of all signal in the image, \""
                    +inputImageName+"\", to each object, \""+inputObjectsName+"\".  This value will get smaller as " +
                    "the brightest regions of the image get closer to the input objects.  Measured in calibrated ("
                    +Units.getOMEUnits().getSymbol()+") units.");
            returnedRefs.add(reference);

        }

        if ((boolean) parameters.getValue(MEASURE_EDGE_INTENSITY_PROFILE)) {
            double minDist = parameters.getValue(MINIMUM_DISTANCE);
            double maxDist = parameters.getValue(MAXIMUM_DISTANCE);
            int nMeasurements = parameters.getValue(NUMBER_OF_MEASUREMENTS);
            double[] bins = getProfileBins(minDist,maxDist,nMeasurements);
            String units = (boolean) parameters.getValue(CALIBRATED_DISTANCES) ? Units.getOMEUnits().getSymbol() : "PX";

            // Bin names must be in alphabetical order (for the ObjMeasurementRefCollection TreeMap)
            int nDigits = (int) Math.log10(bins.length)+1;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0;i<nDigits;i++) stringBuilder.append("0");
            DecimalFormat intFormat = new DecimalFormat(stringBuilder.toString());

            String nameFormat = getBinNameFormat(parameters.getValue(CALIBRATED_DISTANCES));
            DecimalFormat decFormat = new DecimalFormat(nameFormat);

            for (int i=0;i<nMeasurements;i++) {
                String profileMeasName = Measurements.EDGE_PROFILE+"_BIN"+intFormat.format(i+1)
                        +"_("+decFormat.format(bins[i])+units+")";

                name = getFullName(inputImageName,profileMeasName);
                ObjMeasurementRef reference = objectMeasurementRefs.getOrPut(name);
                reference.setObjectsName(inputObjectsName);

                String minBin = i==0 ? "0" :decFormat.format(bins[i-1]);
                String maxBin = i== nMeasurements-1 ? "Inf." : decFormat.format(bins[i]+1);

                reference.setDescription("Mean intensity of the image, \""+inputImageName+"\" between "+minBin+" "
                        +units+" and "+maxBin+" "+units+" from each \""+inputObjectsName+"\" object.  This is the " +
                        intFormat.format(i+1)+" bin.");
                returnedRefs.add(reference);
            }
        }

        return returnedRefs;

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
