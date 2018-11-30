package wbif.sjx.ModularImageAnalysis.Module.Visualisation;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
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
    public static final String MEASUREMENT = "Measurement";
    public static final String STATISTIC = "Statistic";
    public static final String DOWNSAMPLE_RANGE = "Downsample range";
    public static final String AVERAGE_SLICES = "Average slices";
    public static final String AVERAGE_TIME = "Average time";

    public interface MeasurementModes {
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[]{MEASUREMENT};

    }

    public interface Statistics {
        String MEAN = "Mean";
        String MIN = "Minimum";
        String MAX = "Maximum";
        String STD = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[]{MEAN,MIN,MAX,STD,SUM};

    }


    public Object[] createStatisticImage(ObjCollection objects, Image image, String measurementName, boolean averageZ, boolean averageT) {
        ImagePlus ipl = image.getImagePlus();

        // Get final CumStat[] dimensions
        int width = ipl.getWidth();
        int height = ipl.getHeight();
        int nSlices = averageZ ? 1 : ipl.getNSlices();
        int nFrames = averageT ? 1 : ipl.getNFrames();

        // Create CumStat[] and Indexer
        CumStat[] cumStats =  new CumStat[width*height*nSlices*nFrames];
        Indexer indexer = new Indexer(new int[]{width,height,nSlices,nFrames});

        // Initialise CumStats
        for (int i=0;i<cumStats.length;i++) cumStats[i] = new CumStat();

        // Adding objects
        for (Obj object:objects.values()) {
            // Getting measurement value.  Skip if null or NaN.
            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null) continue;
            double measurementValue = measurement.getValue();
            if (Double.isNaN(measurementValue)) continue;

            // Getting all object points
            for (Point<Integer> point:object.getPoints()) {
                // Getting index for this point
                int z = averageZ ? 0 : point.getZ();
                int t = averageT ? 0 : object.getT();
                int idx = indexer.getIndex(new int[]{point.getX(),point.getY(),z,t});

                // Adding measurement
                cumStats[idx].addMeasure(measurementValue);

            }
        }

        return new Object[]{cumStats,indexer};

    }

    public Image convertToImage(CumStat[] cumStats, Indexer indexer, String statistic, String outputImageName, Calibration calibration) {
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
                        if (cumStat.getMean() < 0) System.err.println(cumStat.getMean()+"_"+x+"_"+y);

                        // Getting statistic
                        switch (statistic) {
                            case Statistics.MEAN:
                                ipr.setf(x,y,(float) cumStat.getMean());
                                break;
                            case Statistics.MIN:
                                ipr.setf(x,y,(float) cumStat.getMin());
                                break;
                            case Statistics.MAX:
                                ipr.setf(x,y,(float) cumStat.getMax());
                                break;
                            case Statistics.STD:
                                ipr.setf(x,y,(float) cumStat.getStd());
                                break;
                            case Statistics.SUM:
                                ipr.setf(x,y,(float) cumStat.getSum());
                                break;
                        }
                    }
                }
            }
        }

        return new Image(outputImageName,outputIpl);

    }

    public CumStat[] applyBlur(CumStat[] inputCumstats, Indexer indexer, int downsampleRange) {
        // Create CumStat array to calculate scores for neighbouring objects
        CumStat[] outputCumStats =  new CumStat[inputCumstats.length];
        for (int i=0;i<outputCumStats.length;i++) outputCumStats[i] = new CumStat();

        // Getting coordinates of reference points
        MidpointCircle midpointCircle = new MidpointCircle(downsampleRange);
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
                                double measurementValue = inputCumstats[idx2].getMean();
                                outputCumStats[idx].addMeasure(measurementValue, Math.max(0,downsampleRange - dist));
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
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        // Getting template image
        String templateImageName = parameters.getValue(TEMPLATE_IMAGE);
        Image templateImage = workspace.getImage(templateImageName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE);
        String measurementName = parameters.getValue(MEASUREMENT);
        String statistic = parameters.getValue(STATISTIC);
        int downsampleRange = parameters.getValue(DOWNSAMPLE_RANGE);
        boolean averageZ = parameters.getValue(AVERAGE_SLICES);
        boolean averageT = parameters.getValue(AVERAGE_TIME);

        // Create statistic image
        Object[] statsOutput = createStatisticImage(inputObjects,templateImage,measurementName,averageZ,averageT);
        CumStat[] cumStats = (CumStat[]) statsOutput[0];
        Indexer indexer = (Indexer) statsOutput[1];

        // Blurring image
        CumStat[] blurCumStats = applyBlur(cumStats,indexer,downsampleRange);

        // If cancelled, blurring will return null
        if (blurCumStats == null) return false;

        // Converting statistic array to Image
        Calibration calibration = templateImage.getImagePlus().getCalibration();
        Image outputImage = convertToImage(blurCumStats,indexer,statistic,outputImageName,calibration);

        workspace.addImage(outputImage);
        if (showOutput) showImage(outputImage);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_OBJECTS,Parameter.INPUT_OBJECTS,null));
        parameters.add(new Parameter(TEMPLATE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(OUTPUT_IMAGE,Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(MEASUREMENT_MODE,Parameter.CHOICE_ARRAY,MeasurementModes.MEASUREMENT,MeasurementModes.ALL));
        parameters.add(new Parameter(STATISTIC,Parameter.CHOICE_ARRAY,Statistics.MEAN,Statistics.ALL));
        parameters.add(new Parameter(MEASUREMENT,Parameter.OBJECT_MEASUREMENT,null));
        parameters.add(new Parameter(DOWNSAMPLE_RANGE,Parameter.INTEGER,3));
        parameters.add(new Parameter(AVERAGE_SLICES,Parameter.BOOLEAN,true));
        parameters.add(new Parameter(AVERAGE_TIME,Parameter.BOOLEAN,true));

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

                parameters.updateValueSource(MEASUREMENT,inputObjectsName);

                break;
        }

        returnedParameters.add(parameters.getParameter(DOWNSAMPLE_RANGE));
        returnedParameters.add(parameters.getParameter(AVERAGE_SLICES));
        returnedParameters.add(parameters.getParameter(AVERAGE_TIME));

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
