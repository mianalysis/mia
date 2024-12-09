package io.github.mianalysis.mia.module.visualise;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Point;
import io.github.mianalysis.mia.object.coordinates.volume.SpatCal;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;
import io.github.mianalysis.mia.process.math.Indexer;
import io.github.mianalysis.mia.process.voxel.MidpointCircle;


/**
* Creates a map of object measurements.  Each pixel of the output map is a combination of all object measurements at that location.  Measurements can be taken from the input objects themselves or from associated parent objects.  Multiple Z-slices and/or timepoints can be combined into a single slice.  The outmap is blurred with a Gaussian function to show smooth transitions between regions.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class CreateMeasurementMap extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input, object output";

	/**
	* Objects from workspace for which the measurement map will be created.
	*/
    public static final String INPUT_OBJECTS = "Input objects";

	/**
	* Output image showing the measurement map which will be output to the workspace.  The value at each location will be based on the measurements of nearby objects.  Any pixels too far from an object (this distance will be controlled by the blur range, "Range") will be assigned NaN (not a number) values.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String MAP_SEPARATOR = "Map controls";

	/**
	* Controls whether the measurements being rendered for each object are measurements associated with that object ("Measurement") or are measurements associated with a parent object ("Parent object measurement").
	*/
    public static final String MEASUREMENT_MODE = "Measurement mode";

	/**
	* If "Measurement mode" is set to "Parent object measurement", this is the parent object collection from which the measurement (specified by "Measurement") will be taken.
	*/
    public static final String PARENT_OBJECT = "Parent object";

	/**
	* Controls the measurement for each object that will be rendered on the measurement map.  Depending on the setting for "Measurement mode", this can either be a measurement associated with the input object or with a parent of that object.
	*/
    public static final String MEASUREMENT = "Measurement";

	/**
	* Controls the statistic that will be used to combine multiple object measurements at each location.  For example, if two objects overlap at a specific location (occurs more frequently when "Merge slices" or "Merge time" are selected), the value at that location could be an average (or any other listed statistic) of the two.
	*/
    public static final String STATISTIC = "Statistic";

	/**
	* The measurement map can be blurred using a Gaussian distribution.  This is the sigma value for that blurring function.
	*/
    public static final String RANGE = "Range";

	/**
	* When selected, all measurements from different slices are combined into a single slice.
	*/
    public static final String MERGE_SLICES = "Merge slices";

	/**
	* When selected, all measurements from different timepoints are combined into a single timepoint.
	*/
    public static final String MERGE_TIME = "Merge time";

    public CreateMeasurementMap(Modules modules) {
        super("Create measurement map", modules);
    }

    public interface MeasurementModes {
        String MEASUREMENT = "Measurement";
        String PARENT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[] { MEASUREMENT, PARENT_MEASUREMENT };

    }

    public interface Statistics {
        String COUNT = "Count";
        String MEAN = "Mean";
        String MIN = "Minimum";
        String MAX = "Maximum";
        String STD = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[] { COUNT, MEAN, MIN, MAX, STD, SUM };

    }

    public static Indexer initialiseIndexer(SpatCal calibration, int nFrames, boolean mergeZ, boolean mergeT) {
        // Get final CumStat[] dimensions
        int width = calibration.getWidth();
        int height = calibration.getHeight();
        int nSlices = mergeZ ? 1 : calibration.getNSlices();
        nFrames = mergeT ? 1 : nFrames;

        // Create Indexer
        return new Indexer(new int[] { width, height, nSlices, nFrames });

    }

    public static CumStat[] initialiseCumStats(SpatCal calibration, int nFrames, boolean mergeZ, boolean mergeT) {
        // Get final CumStat[] dimensions
        int width = calibration.getWidth();
        int height = calibration.getHeight();
        int nSlices = mergeZ ? 1 : calibration.getNSlices();
        nFrames = mergeT ? 1 : nFrames;

        // Create CumStat[]
        CumStat[] cumStats = new CumStat[width * height * nSlices * nFrames];

        // EnableExtensions CumStats
        for (int i = 0; i < cumStats.length; i++)
            cumStats[i] = new CumStat();

        return cumStats;

    }

    public static void processObjectMeasurement(CumStat[] cumStats, Indexer indexer, Objs objects,
            String measurementName, @Nullable String message) {
        // Adding objects
        int count = 0;
        int nTotal = objects.size();
        for (Obj object : objects.values()) {
            // Getting measurement value. Skip if null or NaN.
            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null)
                continue;
            double measurementValue = measurement.getValue();
            if (Double.isNaN(measurementValue))
                continue;

            // Getting all object points
            for (Point<Integer> point : object.getCoordinateSet()) {
                // Getting index for this point
                int z = indexer.getDim()[2] == 1 ? 0 : point.getZ();
                int t = indexer.getDim()[3] == 1 ? 0 : object.getT();
                int idx = indexer.getIndex(new int[] { point.getX(), point.getY(), z, t });

                // Adding measurement
                cumStats[idx].addMeasure(measurementValue);

            }

            if (message != null)
                writeProgressStatus(++count, nTotal, "objects", message);

        }
    }

    public static void processParentMeasurements(CumStat[] cumStats, Indexer indexer, Objs objects,
            String parentObjectsName, String measurementName, @Nullable String message) {
        // Adding objects
        int count = 0;
        int nTotal = objects.size();
        for (Obj object : objects.values()) {
            // Getting parent object
            Obj parentObject = object.getParent(parentObjectsName);
            if (parentObject == null)
                continue;

            // Getting measurement value. Skip if null or NaN.
            Measurement measurement = parentObject.getMeasurement(measurementName);
            if (measurement == null)
                continue;

            double measurementValue = measurement.getValue();
            if (Double.isNaN(measurementValue))
                continue;

            // Getting all object points
            for (Point<Integer> point : object.getCoordinateSet()) {
                // Getting index for this point
                int z = indexer.getDim()[2] == 1 ? 0 : point.getZ();
                int t = indexer.getDim()[3] == 1 ? 0 : object.getT();
                int idx = indexer.getIndex(new int[] { point.getX(), point.getY(), z, t });

                // Adding measurement
                cumStats[idx].addMeasure(measurementValue);

            }

            if (message != null)
                writeProgressStatus(++count, nTotal, "objects", message);

        }
    }

    public static CumStat[] applyBlur(CumStat[] inputCumstats, Indexer indexer, int range, String statistic) {
        // Create CumStat array to calculate scores for neighbouring objects
        CumStat[] outputCumStats = new CumStat[inputCumstats.length];
        for (int i = 0; i < outputCumStats.length; i++)
            outputCumStats[i] = new CumStat();

        // Initialising the Gaussian calculator for distance weights
        Gaussian gaussian = new Gaussian(0, range);

        // Getting coordinates of reference points
        MidpointCircle midpointCircle = new MidpointCircle(3 * range);
        int[] xSamp = midpointCircle.getXCircleFill();
        int[] ySamp = midpointCircle.getYCircleFill();

        // Setting up the ExecutorService, which will manage the threads
        int nThreads = Prefs.getThreads();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Iterating over each pixel in the CumStat array
        int[] dims = indexer.getDim();

        for (int z = 0; z < dims[2]; z++) {
            for (int t = 0; t < dims[3]; t++) {
                for (int x = 0; x < dims[0]; x++) {
                    for (int y = 0; y < dims[1]; y++) {
                        int finalX = x;
                        int finalY = y;
                        int finalZ = z;
                        int finalT = t;

                        Runnable task = () -> {
                            int idx = indexer.getIndex(new int[] { finalX, finalY, finalZ, finalT });
                            // Getting neighbour measurements
                            for (int i = 0; i < xSamp.length; i++) {
                                int xx = finalX + xSamp[i];
                                int yy = finalY + ySamp[i];

                                int idx2 = indexer.getIndex(new int[] { xx, yy, finalZ, finalT });
                                if (idx2 == -1)
                                    continue;

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
            // Do nothing as the user has selected this
            return null;
        }

        return outputCumStats;

    }

    public static Image convertToImage(CumStat[] cumStats, Indexer indexer, String outputImageName,
            Calibration calibration) {
        int[] dim = indexer.getDim();
        int width = dim[0];
        int height = dim[1];
        int nSlices = dim[2];
        int nFrames = dim[3];

        // Creating ImagePlus
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName, width, height, 1, nSlices, nFrames, 32);
        outputIpl.setCalibration(calibration);

        // Iterating over all points in the image
        for (int z = 0; z < nSlices; z++) {
            for (int t = 0; t < nFrames; t++) {
                outputIpl.setPosition(1, z + 1, t + 1);
                ImageProcessor ipr = outputIpl.getProcessor();

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        // Getting relevant index
                        int idx = indexer.getIndex(new int[] { x, y, z, t });
                        CumStat cumStat = cumStats[idx];
                        ipr.setf(x, y, (float) cumStat.getMean());
                    }
                }
            }
        }

        return ImageFactory.createImage(outputImageName, outputIpl);

    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Creates a map of object measurements.  Each pixel of the output map is a combination of all object measurements at that location.  Measurements can be taken from the input objects themselves or from associated parent objects.  Multiple Z-slices and/or timepoints can be combined into a single slice.  The outmap is blurred with a Gaussian function to show smooth transitions between regions.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input objects
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        // Getting parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE,workspace);
        String measurementMode = parameters.getValue(MEASUREMENT_MODE,workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECT,workspace);
        String measurementName = parameters.getValue(MEASUREMENT,workspace);
        String statistic = parameters.getValue(STATISTIC,workspace);
        int range = parameters.getValue(RANGE,workspace);
        boolean mergeZ = parameters.getValue(MERGE_SLICES,workspace);
        boolean mergeT = parameters.getValue(MERGE_TIME,workspace);

        // Initialising stores
        SpatCal calibration = inputObjects.getSpatialCalibration();
        int nFrames = inputObjects.getNFrames();
        CumStat[] cumStats = initialiseCumStats(calibration, nFrames, mergeZ, mergeT);
        Indexer indexer = initialiseIndexer(calibration, nFrames, mergeZ, mergeT);

        // Compressing relevant measures
        switch (measurementMode) {
            case MeasurementModes.MEASUREMENT:
                processObjectMeasurement(cumStats, indexer, inputObjects, measurementName, getName());
                break;
            case MeasurementModes.PARENT_MEASUREMENT:
                processParentMeasurements(cumStats, indexer, inputObjects, parentObjectsName, measurementName,
                        getName());
                break;
        }

        // Blurring image
        CumStat[] blurCumStats = applyBlur(cumStats, indexer, range, statistic);

        // Converting statistic array to Image
        Calibration imagecalibration = inputObjects.getSpatialCalibration().createImageCalibration();
        Image outputImage = convertToImage(blurCumStats, indexer, outputImageName, imagecalibration);

        workspace.addImage(outputImage);
        if (showOutput)
            outputImage.show();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(MAP_SEPARATOR, this));
        parameters.add(new ChoiceP(MEASUREMENT_MODE, this, MeasurementModes.MEASUREMENT, MeasurementModes.ALL));
        parameters.add(new ChoiceP(STATISTIC, this, Statistics.MEAN, Statistics.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));
        parameters.add(new IntegerP(RANGE, this, 3));
        parameters.add(new BooleanP(MERGE_SLICES, this, true));
        parameters.add(new BooleanP(MERGE_TIME, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS,workspace);

        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(MAP_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MEASUREMENT_MODE));
        switch ((String) parameters.getValue(MEASUREMENT_MODE,workspace)) {
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
                String parentObjectsName = parameters.getValue(PARENT_OBJECT,workspace);
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT)).setObjectName(parentObjectsName);
                break;
        }

        returnedParameters.add(parameters.getParameter(RANGE));
        returnedParameters.add(parameters.getParameter(MERGE_SLICES));
        returnedParameters.add(parameters.getParameter(MERGE_TIME));

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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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

    public void addParameterDescriptions() {
        parameters.get(INPUT_OBJECTS).setDescription("Objects from workspace for which the measurement map will be created.");

        parameters.get(OUTPUT_IMAGE).setDescription("Output image showing the measurement map which will be output to the workspace.  The value at each location will be based on the measurements of nearby objects.  Any pixels too far from an object (this distance will be controlled by the blur range, \""+RANGE+"\") will be assigned NaN (not a number) values.");

        parameters.get(MEASUREMENT_MODE).setDescription("Controls whether the measurements being rendered for each object are measurements associated with that object (\""+MeasurementModes.MEASUREMENT+"\") or are measurements associated with a parent object (\""+MeasurementModes.PARENT_MEASUREMENT+"\").");

        parameters.get(PARENT_OBJECT).setDescription("If \""+MEASUREMENT_MODE+"\" is set to \""+MeasurementModes.PARENT_MEASUREMENT+"\", this is the parent object collection from which the measurement (specified by \""+MEASUREMENT+"\") will be taken.");

        parameters.get(MEASUREMENT).setDescription("Controls the measurement for each object that will be rendered on the measurement map.  Depending on the setting for \""+MEASUREMENT_MODE+"\", this can either be a measurement associated with the input object or with a parent of that object.");

        parameters.get(STATISTIC).setDescription("Controls the statistic that will be used to combine multiple object measurements at each location.  For example, if two objects overlap at a specific location (occurs more frequently when \""+MERGE_SLICES+"\" or \""+MERGE_TIME+"\" are selected), the value at that location could be an average (or any other listed statistic) of the two.");

        parameters.get(RANGE).setDescription("The measurement map can be blurred using a Gaussian distribution.  This is the sigma value for that blurring function.");

        parameters.get(MERGE_SLICES).setDescription("When selected, all measurements from different slices are combined into a single slice.");

        parameters.get(MERGE_TIME).setDescription("When selected, all measurements from different timepoints are combined into a single timepoint.");

    }
}
