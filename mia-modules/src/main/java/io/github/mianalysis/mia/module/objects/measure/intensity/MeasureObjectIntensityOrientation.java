package io.github.mianalysis.mia.module.objects.measure.intensity;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.analyze.directionality.Directionality_;
import fiji.analyze.directionality.Directionality_.AnalysisMethod;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.images.measure.MeasureImageIntensityOrientation;
import io.github.mianalysis.mia.module.images.transform.ExtractSubstack;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.module.visualise.overlays.AddText;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Calculates the orientation of structures in each object for a specific image.
 * This module uses the
 * <a href="https://imagej.net/plugins/directionality">Directionality_</a>
 * plugin to calculate core measures. Additional measurements, such as the
 * Alignment Index [1] are also calculated. All measurements are made for all
 * slices within an object; that is, the individual slice histograms are merged
 * and normalised prior to calculation of all measurements.<br>
 * <br>
 * References:<br>
 * <ol>
 * <li>Sun, M., et al. "Rapid Quantification of 3D Collagen Fiber Alignment and
 * Fiber Intersection Correlations with High Sensitivity" <i>PLOS ONE</i>
 * (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li>
 * </ol>
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureObjectIntensityOrientation extends AbstractSaver {
    private static final int histWidth = 600;
    private static final int histHeight = 400;

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image/object input/output";

    /**
    * 
    */
    public static final String INPUT_OBJECTS = "Input objects";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String ORIENTATION_SEPARATOR = "Orientation controls";

    /**
    * 
    */
    public static final String METHOD = "Method";

    /**
    * 
    */
    public static final String NUMBER_OF_BINS = "Number of bins";

    /**
    * 
    */
    public static final String HISTOGRAM_START = "Histogram start (°)";

    /**
    * 
    */
    public static final String HISTOGRAM_END = "Histogram end (°)";

    /**
    * 
    */
    public static final String MEASUREMENT_SEPARATOR = "Measurement controls";

    /**
    * 
    */
    public static final String INCLUDE_BIN_RANGE_IN_NAME = "Include bin range in name";

    /**
    * 
    */
    public static final String INCLUDE_BIN_NUMBER_IN_NAME = "Include number of bins in name";

    /**
    * 
    */
    public static final String HISTOGRAM_SEPARATOR = "Histogram saving controls";

    /**
    * 
    */
    public static final String SAVE_HISTOGRAM = "Save histogram";

    /**
     * 
     */
    public static final String HISTOGRAM_GROUPING_MODE = "Histogram grouping";

    /**
     * 
     */
    public static final String PARENT_OBJECTS_NAME = "Parent objects name";

    /**
    * 
    */
    public static final String EXECUTION_SEPARATOR = "Execution controls";

    /**
     * Process multiple input objects simultaneously. This can provide a speed
     * improvement when working on a computer with a multi-core CPU.
     */
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface HistogramGroupingModes {
        String ALL_TOGETHER = "All together";
        String GROUP_BY_PARENT = "Group by parent";
        String INDIVIDUAL_FILES = "Individual files";

        String[] ALL = new String[] { ALL_TOGETHER, GROUP_BY_PARENT, INDIVIDUAL_FILES };

    }

    public interface Methods {
        String FOURIER = "Fourier components";
        String LOCAL_GRADIENT = "Local gradient orientation";

        String[] ALL = new String[] { FOURIER, LOCAL_GRADIENT };

    }

    public interface Measurements {
        String DIRECTION = "DIRECTION_(°)";
        String DISPERSION = "DISPERSION_(°)";
        String AMOUNT = "AMOUNT";
        String GOODNESS = "GOODNESS";
        String ALIGNMENT_INDEX = "ALIGNMENT_INDEX";
        String ALIGNMENT_INDEX_BG_SUB = "ALIGNMENT_INDEX_(BG_SUB)";
        String BACKGROUND = "BACKGROUND_VALUE";

        String[] ALL = new String[] { DIRECTION, DISPERSION, AMOUNT, GOODNESS, ALIGNMENT_INDEX, ALIGNMENT_INDEX_BG_SUB,
                BACKGROUND };

    }

    public MeasureObjectIntensityOrientation(Modules modules) {
        super("Measure object intensity orientation", modules);
    }

    public static String getFullName(double binStart, double binEnd, int nBins, String imageName, String measurement,
            boolean includeBinRange, boolean includeBinNumber) {
        DecimalFormat df = new DecimalFormat("#0.0");
        StringBuilder sb = new StringBuilder("INT_ORI // ");

        sb.append(imageName + " // ");

        if (includeBinRange)
            sb.append(df.format(binStart) + "° TO " + df.format(binEnd) + "° // ");

        if (includeBinNumber)
            sb.append(nBins + " BINS // ");

        sb.append(measurement);

        return sb.toString();

    }

    @Override
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_INTENSITY;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the orientation of structures in each object for a specific image.  This module uses the <a href=\"https://imagej.net/plugins/directionality\">Directionality_</a> plugin to calculate core measures.  Additional measurements, such as the Alignment Index [1] are also calculated.  All measurements are made for all slices within an object; that is, the individual slice histograms are merged and normalised prior to calculation of all measurements."
                +
                "<br><br>References:<br>" +
                "<ol><li>Sun, M., et al. \"Rapid Quantification of 3D Collagen Fiber Alignment and Fiber Intersection Correlations with High Sensitivity\" <i>PLOS ONE</i> (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li></ol>";
    }

    public static Directionality_ processObject(Obj obj, Image inputImage, int nBins, double binStart, double binEnd,
            AnalysisMethod method, boolean includeBinRange, boolean includeBinNumber) {
        ArrayList<double[]> tempHistograms = new ArrayList<>();
        HashMap<Integer, Roi> rois = obj.getRois();
        Directionality_ directionality = new Directionality_();

        for (int z : rois.keySet()) {
            // Getting current image slice
            Image sliceImage = ExtractSubstack.extractSubstack(inputImage, "Slice", "1", String.valueOf(z + 1),
                    String.valueOf(obj.getT() + 1));
            ImagePlus sliceIpl = sliceImage.getImagePlus();
            sliceIpl.setRoi(rois.get(z));
            ImagePlus cropIpl = sliceIpl.crop();
            cropIpl.setTitle("Directionality");

            // Configuring Directionality plugin
            directionality = new Directionality_();
            directionality.setImagePlus(cropIpl);
            directionality.setBinNumber(nBins);
            directionality.setBinRange(binStart, binEnd);
            directionality.setMethod(method);

            // Running analysis on this slice for this object
            directionality.computeHistograms();
            ArrayList<double[]> currHistograms = directionality.getHistograms();
            tempHistograms.add(currHistograms.get(0));

        }

        // Removing existing histograms (from the most recent run) and replacing with
        // all histograms from each object slice
        ArrayList<double[]> histograms = directionality.getHistograms();
        histograms.clear();
        for (double[] histogram : tempHistograms)
            histograms.add(histogram);

        MeasureImageIntensityOrientation.mergeHistograms(directionality);
        directionality.fitHistograms();

        // Adding measurements
        ArrayList<double[]> fitParameters = directionality.getFitAnalysis();
        double[] results = fitParameters.iterator().next();

        String inputImageName = inputImage.getName();
        String name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DIRECTION, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, Math.toDegrees(results[0])));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DISPERSION, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, Math.toDegrees(results[1])));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.AMOUNT, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, results[2]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.GOODNESS, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, results[3]));

        histograms = directionality.getHistograms();
        double[] hist = histograms.iterator().next();
        double[] binsDegs = directionality.getBins();
        double[] binsRads = Arrays.stream(binsDegs).map((v) -> Math.toRadians(v)).toArray();
        double[] extra_results = MeasureImageIntensityOrientation.calculateSpreadMeasures(hist, binsRads);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[0]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX_BG_SUB,
                includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[1]));

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.BACKGROUND, includeBinRange,
                includeBinNumber);
        obj.addMeasurement(new Measurement(name, extra_results[2]));

        return directionality;

    }

    void saveHistogramsAllTogether(Objs inputObjects, TreeMap<Integer, ImagePlus> histogramIpls, String outputPath) {
        ImagePlus histIpl = IJ.createImage("Histograms", histWidth, histHeight, inputObjects.size(), 24);

        int count = 1;
        for (Obj obj : inputObjects.values()) {
            ImagePlus currHistIpl = histogramIpls.get(obj.getID());
            AddText.addOverlay(currHistIpl, "ID=" + obj.getID(), Color.RED, 14, 1, 20, 5, new int[] { 1 },
                    new int[] { 1 }, false);
            histIpl.setPosition(histIpl.getStackIndex(1, count++, 1));
            histIpl.setProcessor(currHistIpl.flatten().getProcessor());
        }

        outputPath = outputPath + ".tif";
        ImageSaver.saveImage(histIpl, ImageSaver.FileFormats.TIF, outputPath);

    }

    void saveHistogramsByParent(Objs parentObjects, String inputObjectsName, TreeMap<Integer, ImagePlus> histogramIpls,
            String outputPath) {
        for (Obj parent : parentObjects.values()) {
            Objs children = parent.getChildren(inputObjectsName);
            ImagePlus histIpl = IJ.createImage("Histograms", histWidth, histHeight, children.size(), 24);

            int count = 1;
            for (Obj obj : children.values()) {
                ImagePlus currHistIpl = histogramIpls.get(obj.getID());
                AddText.addOverlay(currHistIpl, "ID=" + obj.getID() + "_T=" + (obj.getT() + 1), Color.RED,
                        14, 1, 20, 5,
                        new int[] { 1 },
                        new int[] { 1 }, false);
                histIpl.setPosition(histIpl.getStackIndex(1, count++, 1));
                histIpl.setProcessor(currHistIpl.flatten().getProcessor());
            }

            String finalOutputPath = outputPath + "_ID" + parent.getID() + ".tif";
            ImageSaver.saveImage(histIpl, ImageSaver.FileFormats.TIF, finalOutputPath);
        }
    }

    void saveHistogramsIndividually(Objs inputObjects, TreeMap<Integer, ImagePlus> histogramIpls, String outputPath) {
        for (Obj obj : inputObjects.values()) {
            ImagePlus currHistIpl = histogramIpls.get(obj.getID());
            AddText.addOverlay(currHistIpl, "ID=" + obj.getID(), Color.RED, 14, 1, 20, 5, new int[] { 1 },
                    new int[] { 1 }, false);
            String finalOutputPath = outputPath + "_ID" + obj.getID() + ".tif";
            ImageSaver.saveImage(currHistIpl.flatten(), ImageSaver.FileFormats.TIF, finalOutputPath);
        }
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String methodString = parameters.getValue(METHOD, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);
        boolean includeBinRange = parameters.getValue(INCLUDE_BIN_RANGE_IN_NAME, workspace);
        boolean includeBinNumber = parameters.getValue(INCLUDE_BIN_NUMBER_IN_NAME, workspace);
        boolean saveHistogram = parameters.getValue(SAVE_HISTOGRAM, workspace);
        String appendSeriesMode = parameters.getValue(APPEND_SERIES_MODE, workspace);
        String appendDateTimeMode = parameters.getValue(APPEND_DATETIME_MODE, workspace);
        String suffix = parameters.getValue(SAVE_SUFFIX, workspace);
        String histGroupMode = parameters.getValue(HISTOGRAM_GROUPING_MODE, workspace);
        String parentObjectsName = parameters.getValue(PARENT_OBJECTS_NAME, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        Image inputImage = workspace.getImage(inputImageName);
        Objs inputObjects = workspace.getObjects(inputObjectsName);

        AnalysisMethod method;
        switch (methodString) {
            case Methods.FOURIER:
            default:
                method = AnalysisMethod.FOURIER_COMPONENTS;
                break;
            case Methods.LOCAL_GRADIENT:
                method = AnalysisMethod.LOCAL_GRADIENT_ORIENTATION;
                break;
        }

        // Setting up multithreading options
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Running through each object, taking measurements and adding new object to the
        // workspace where necessary
        AtomicInteger count = new AtomicInteger(1);
        int total = inputObjects.size();
        TreeMap<Integer, ImagePlus> histogramIpls = new TreeMap<>();

        for (Obj inputObject : inputObjects.values()) {
            Runnable task = () -> {
                Directionality_ directionality = processObject(inputObject, inputImage, nBins, binStart, binEnd, method,
                        includeBinRange, includeBinNumber);

                if (saveHistogram)
                    histogramIpls.put(inputObject.getID(),
                            MeasureImageIntensityOrientation.getHistogramRGB(directionality).getImagePlus());

                writeProgressStatus(count.getAndIncrement(), total, "objects");
            };
            pool.submit(task);

        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        if (saveHistogram) {
            // Constructing core filename
            String outputPath = getOutputPath(modules, workspace);
            String outputName = getOutputName(modules, workspace);
            outputPath = outputPath + outputName;
            outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
            outputPath = appendDateTime(outputPath, appendDateTimeMode);
            outputPath = outputPath + suffix;

            switch (histGroupMode) {
                case HistogramGroupingModes.ALL_TOGETHER:
                    saveHistogramsAllTogether(inputObjects, histogramIpls, outputPath);
                    break;
                case HistogramGroupingModes.GROUP_BY_PARENT:
                    Objs parentObjects = workspace.getObjects(parentObjectsName);
                    saveHistogramsByParent(parentObjects, inputObjectsName, histogramIpls, outputPath);
                    break;
                case HistogramGroupingModes.INDIVIDUAL_FILES:
                    saveHistogramsIndividually(inputObjects, histogramIpls, outputPath);
                    break;
            }
        }

        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(ORIENTATION_SEPARATOR, this));
        parameters.add(new ChoiceP(METHOD, this, Methods.FOURIER, Methods.ALL));
        parameters.add(new IntegerP(NUMBER_OF_BINS, this, 90));
        parameters.add(new DoubleP(HISTOGRAM_START, this, -90));
        parameters.add(new DoubleP(HISTOGRAM_END, this, 90));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(INCLUDE_BIN_RANGE_IN_NAME, this, true));
        parameters.add(new BooleanP(INCLUDE_BIN_NUMBER_IN_NAME, this, true));

        parameters.add(new SeparatorP(HISTOGRAM_SEPARATOR, this));
        parameters.add(new BooleanP(SAVE_HISTOGRAM, this, false));
        parameters.add(new ChoiceP(HISTOGRAM_GROUPING_MODE, this, HistogramGroupingModes.ALL_TOGETHER,
                HistogramGroupingModes.ALL));
        parameters.add(new ParentObjectsP(PARENT_OBJECTS_NAME, this));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_OBJECTS));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(ORIENTATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(METHOD));
        returnedParameters.add(parameters.getParameter(NUMBER_OF_BINS));
        returnedParameters.add(parameters.getParameter(HISTOGRAM_START));
        returnedParameters.add(parameters.getParameter(HISTOGRAM_END));

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INCLUDE_BIN_RANGE_IN_NAME));
        returnedParameters.add(parameters.getParameter(INCLUDE_BIN_NUMBER_IN_NAME));

        returnedParameters.add(parameters.getParameter(HISTOGRAM_SEPARATOR));
        returnedParameters.add(parameters.getParameter(SAVE_HISTOGRAM));
        if ((boolean) parameters.getValue(SAVE_HISTOGRAM, null)) {
            returnedParameters.addAll(super.updateAndGetParameters());
            returnedParameters.remove(FILE_SAVING_SEPARATOR);
            returnedParameters.add(parameters.getParameter(HISTOGRAM_GROUPING_MODE));
            switch ((String) parameters.getValue(HISTOGRAM_GROUPING_MODE, null)) {
                case HistogramGroupingModes.GROUP_BY_PARENT:
                    returnedParameters.add(parameters.getParameter(PARENT_OBJECTS_NAME));
                    ParentObjectsP parameter = parameters.getParameter(PARENT_OBJECTS_NAME);
                    parameter.setChildObjectsName(parameters.getValue(INPUT_OBJECTS, null));
                    break;
            }
        }

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        Workspace workspace = null;
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);
        boolean includeBinRange = parameters.getValue(INCLUDE_BIN_RANGE_IN_NAME, workspace);
        boolean includeBinNumber = parameters.getValue(INCLUDE_BIN_NUMBER_IN_NAME, workspace);

        String name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DIRECTION, includeBinRange,
                includeBinNumber);
        ObjMeasurementRef ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.DISPERSION, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.AMOUNT, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.GOODNESS, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.ALIGNMENT_INDEX_BG_SUB,
                includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, inputImageName, Measurements.BACKGROUND, includeBinRange,
                includeBinNumber);
        ref = objectMeasurementRefs.getOrPut(name);
        ref.setObjectsName(inputObjectsName);
        returnedRefs.add(ref);

        return returnedRefs;

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE).setDescription("");

    }
}
