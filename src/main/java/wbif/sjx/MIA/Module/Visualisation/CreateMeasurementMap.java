package wbif.sjx.MIA.Module.Visualisation;

import javax.annotation.Nullable;import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import org.apache.commons.math3.analysis.function.Gaussian;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.common.MathFunc.Indexer;
import wbif.sjx.common.MathFunc.MidpointCircle;
import wbif.sjx.common.Object.Point;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CreateMeasurementMap extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String TEMPLATE_IMAGE = "Template image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String MEASUREMENT_MODE = "Measurement mode";
    public static final String PARENT_OBJECT = "Parent object";
    public static final String MEASUREMENT = "Measurement";
    public static final String STATISTIC = "Statistic";
    public static final String RANGE = "Range";
    public static final String AVERAGE_SLICES = "Average slices";
    public static final String AVERAGE_TIME = "Average time";

    public interface MeasurementModes {
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[]{MEASUREMENT,PARENT_MEASUREMENT};

    }

    public interface Statistics {
        String COUNT = "Count";
        String MEAN = "Mean";
        String MIN = "Minimum";
        String MAX = "Maximum";
        String STD = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[]{COUNT,MEAN,MIN,MAX,STD,SUM};

    }

    public static Indexer initialiseIndexer(Image image, boolean averageZ, boolean averageT) {
        ImagePlus ipl = image.getImagePlus();

        // Get final CumStat[] dimensions
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nSlices = averageZ ? 1 : ipl.getNSlices();
        int nFrames = averageT ? 1 : ipl.getNFrames();

        // Create Indexer
        return new Indexer(new int[]{width,height,nSlices,nFrames});

    }

    public static CumStat[] initialiseCumStats(Image image, boolean averageZ, boolean averageT) {
        ImagePlus ipl = image.getImagePlus();

        // Get final CumStat[] dimensions
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nSlices = averageZ ? 1 : ipl.getNSlices();
        int nFrames = averageT ? 1 : ipl.getNFrames();

        // Create CumStat[]
        CumStat[] cumStats =  new CumStat[width*height*nSlices*nFrames];

        // EnableExtensions CumStats
        for (int i=0;i<cumStats.length;i++) cumStats[i] = new CumStat();

        return cumStats;

    }

    public static void processObjectMeasurement(CumStat[] cumStats, Indexer indexer, ObjCollection objects, String measurementName, @Nullable String message) {
        // Adding objects
        int count = 0;
        int nTotal = objects.size();
        for (Obj object:objects.values()) {
            if (message != null) writeMessage("Processing object "+(++count)+" of "+nTotal,message);
            // Getting measurement value.  Skip if null or NaN.
            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null) continue;
            double measurementValue = measurement.getValue();
            if (Double.isNaN(measurementValue)) continue;

            // Getting all object points
            for (Point<Integer> point:object.getPoints()) {
                // Getting index for this point
                int z = indexer.getDim()[2] == 1 ? 0 : point.getZ();
                int t = indexer.getDim()[3] == 1 ? 0 : object.getT();
                int idx = indexer.getIndex(new int[]{point.getX(),point.getY(),z,t});

                // Adding measurement
                cumStats[idx].addMeasure(measurementValue);

            }
        }
    }

    public static void processParentMeasurements(CumStat[] cumStats, Indexer indexer, ObjCollection objects, String parentObjectsName, String measurementName, @Nullable String message) {
        // Adding objects
        int count = 0;
        int nTotal = objects.size();
        for (Obj object:objects.values()) {
            if (message != null) writeMessage("Processing object "+(++count)+" of "+nTotal,message);

            // Getting parent object
            Obj parentObject = object.getParent(parentObjectsName);
            if (parentObject == null) continue;

            // Getting measurement value.  Skip if null or NaN.
            Measurement measurement = parentObject.getMeasurement(measurementName);
            if (measurement == null) continue;

            double measurementValue = measurement.getValue();
            if (Double.isNaN(measurementValue)) continue;

            // Getting all object points
            for (Point<Integer> point:object.getPoints()) {
                // Getting index for this point
                int z = indexer.getDim()[2] == 1 ? 0 : point.getZ();
                int t = indexer.getDim()[3] == 1 ? 0 : object.getT();
                int idx = indexer.getIndex(new int[]{point.getX(),point.getY(),z,t});

                // Adding measurement
                cumStats[idx].addMeasure(measurementValue);

            }
        }
    }

    public static CumStat[] applyBlur(CumStat[] inputCumstats, Indexer indexer, int range, String statistic) {
        // Create CumStat array to calculate scores for neighbouring objects
        CumStat[] outputCumStats =  new CumStat[inputCumstats.length];
        for (int i=0;i<outputCumStats.length;i++) outputCumStats[i] = new CumStat();

        // Initialising the Gaussian calculator for distance weights
        Gaussian gaussian = new Gaussian(0,range);

        // Getting coordinates of reference points
        MidpointCircle midpointCircle = new MidpointCircle(3*range);
        int[] xSamp = midpointCircle.getXCircleFill();
        int[] ySamp = midpointCircle.getYCircleFill();

        // Setting up the ExecutorService, which will manage the threads
        int nThreads = Prefs.getThreads();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        // Iterating over each pixel in the CumStat array
        int[] dims = indexer.getDim();

        for (int z=0;z<dims[2];z++) {
            for (int t = 0; t < dims[3]; t++) {
                for (int x = 0; x < dims[0]; x++) {
                    for (int y = 0; y < dims[1]; y++) {
                        int finalX = x;
                        int finalY = y;
                        int finalZ = z;
                        int finalT = t;

                        Runnable task = () -> {
                            int idx = indexer.getIndex(new int[]{finalX, finalY, finalZ, finalT});
                            // Getting neighbour measurements
                            for (int i = 0; i < xSamp.length; i++) {
                                int xx = finalX + xSamp[i];
                                int yy = finalY + ySamp[i];

                                int idx2 = indexer.getIndex(new int[]{xx, yy, finalZ, finalT});
                                if (idx2 == -1) continue;

                                double dist = Math.sqrt((xx - finalX) * (xx - finalX) + (yy - finalY) * (yy - finalY));
                                double measurementValue = 0;
                                switch (statistic) {
                                    case Statistics.COUNT:
                                        measurementValue = inputCumstats[idx2].getN();
                                        break;
                                    case Statistics.MEAN:
                                        measurementValue = inputCumstats[idx2].getMean();
                                        break;
                                    case Statistics.MIN:
                                        measurementValue = inputCumstats[idx2].getMin();
                                        break;
                                    case Statistics.MAX:
                                        measurementValue = inputCumstats[idx2].getMax();
                                        break;
                                    case Statistics.STD:
                                        measurementValue = inputCumstats[idx2].getStd();
                                        break;
                                    case Statistics.SUM:
                                        measurementValue = inputCumstats[idx2].getSum();
                                        break;
                                }

                                double weight = gaussian.value(dist);
                                outputCumStats[idx].addMeasure(measurementValue, weight);

                            }
                        };
                        pool.submit(task);
                    }
                }
            }
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            return null;
        }

        return outputCumStats;

    }

    public static Image convertToImage(CumStat[] cumStats, Indexer indexer, String outputImageName, Calibration calibration) {
        int[] dim = indexer.getDim();
        int width = dim[0];
        int height = dim[1];
        int nSlices = dim[2];
        int nFrames = dim[3];

        // Creating ImagePlus
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName,width,height,1,nSlices,nFrames,32);
        outputIpl.setCalibration(calibration);

        // Iterating over all points in the image
        for (int z=0;z<nSlices;z++) {
            for (int t=0;t<nFrames;t++) {
                outputIpl.setPosition(1,z+1,t+1);
                ImageProcessor ipr = outputIpl.getProcessor();

                for (int x=0;x<width;x++) {
                    for (int y=0;y<height;y++) {
                        // Getting relevant index
                        int idx = indexer.getIndex(new int[]{x,y,z,t});
                        CumStat cumStat = cumStats[idx];
                        ipr.setf(x,y,(float) cumStat.getMean());
                    }
                }
            }
        }

        return new Image(outputImageName,outputIpl);

    }

    @Override
    public String getTitle() {
        return "Create measurement map";
    }

    @Override
    public String getPackageName() {
        return PackageNames.VISUALISATION;
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

        // Getting template image
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT);
        String measurementName = parameters.getValue(MEASUREMENT);
        String statistic = parameters.getValue(STATISTIC);
        int range = parameters.getValue(RANGE);
        boolean averageZ = parameters.getValue(AVERAGE_SLICES);
        boolean averageT = parameters.getValue(AVERAGE_TIME);

        // Initialising stores
        CumStat[] cumStats = initialiseCumStats(templateImage,averageZ,averageT);
        Indexer indexer = initialiseIndexer(templateImage,averageZ,averageT);

        // Compressing relevant measures
        switch (measurementMode) {
            case MeasurementModes.MEASUREMENT:
                processObjectMeasurement(cumStats,indexer,inputObjects,measurementName,getTitle());
                break;
            case MeasurementModes.PARENT_MEASUREMENT:
                processParentMeasurements(cumStats,indexer,inputObjects,parentObjectsName,measurementName,getTitle());
                break;
        }

        // Blurring image
        writeMessage("Blurring image");
        CumStat[] blurCumStats = applyBlur(cumStats,indexer,range,statistic);

        // Converting statistic array to Image
        Calibration calibration = templateImage.getImagePlus().getCalibration();
        Image outputImage = convertToImage(blurCumStats,indexer,outputImageName,calibration);

        workspace.addImage(outputImage);
        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this));
        parameters.add(new InputImageP(TEMPLATE_IMAGE,this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(MEASUREMENT_MODE,this,MeasurementModes.MEASUREMENT,MeasurementModes.ALL));
        parameters.add(new ChoiceP(STATISTIC,this,Statistics.MEAN,Statistics.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT,this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT,this));
        parameters.add(new IntegerP(RANGE,this,3));
        parameters.add(new BooleanP(AVERAGE_SLICES,this,true));
        parameters.add(new BooleanP(AVERAGE_TIME,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(TEMPLATE_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_MODE));
        switch ((String) parameters.getValue(MEASUREMENT_MODE)) {
            case MeasurementModes.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                returnedParameters.add(parameters.getParameter(STATISTIC));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(inputObjectsName);
                break;

            case MeasurementModes.PARENT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(PARENT_OBJECT));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                returnedParameters.add(parameters.getParameter(STATISTIC));

                ((ParentObjectsP) parameters.getParameter(PARENT_OBJECT)).setChildObjectsName(inputObjectsName);
                String parentObjectsName = parameters.getValue(PARENT_OBJECT);
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(parentObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(RANGE));
        returnedParameters.add(parameters.getParameter(AVERAGE_SLICES));
        returnedParameters.add(parameters.getParameter(AVERAGE_TIME));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
