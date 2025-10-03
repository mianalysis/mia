package io.github.mianalysis.mia.module.images.measure;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import org.apache.commons.math3.complex.Complex;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.analyze.directionality.Directionality_;
import fiji.analyze.directionality.Directionality_.AnalysisMethod;
import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.StackStatistics;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.inputoutput.ImageSaver;
import io.github.mianalysis.mia.module.inputoutput.abstrakt.AbstractSaver;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Calculates the orientation of structures in an image. This module uses the
 * <a href="https://imagej.net/plugins/directionality">Directionality_</a>
 * plugin to calculate core measures. Additional measurements, such as the
 * Alignment Index [1] are also calculated. All measurements are made for the
 * entire image stack; that is, the individual slice histograms are merged and
 * normalised prior to calculation of all measurements. For multi-slice stacks,
 * images can first be decomposed into whole-slice objects using the
 * CreateWholeSliceObjects module, then processed on a per-object basis using
 * the MeasureObjectIntensityOrientation module.<br>
 * <br>
 * References:<br>
 * <ol>
 * <li>Sun, M., et al. "Rapid Quantification of 3D Collagen Fiber Alignment and
 * Fiber Intersection Correlations with High Sensitivity" <i>PLOS ONE</i>
 * (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li>
 * </ol>
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class MeasureImageIntensityOrientation extends AbstractSaver {
    private static final int histWidth = 600;
    private static final int histHeight = 400;

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Image input/output";

    /**
    * 
    */
    public static final String INPUT_IMAGE = "Input image";

    /**
    * 
    */
    public static final String OUTPUT_ORIENTATION_MAP = "Output orientation map";

    /**
    * 
    */
    public static final String ORIENTATION_MAP_NAME = "Orientation map name";

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

    public MeasureImageIntensityOrientation(Modules modules) {
        super("Measure image intensity orientation", modules);
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

    public static String getFullName(double binStart, double binEnd, int nBins, String measurement,
            boolean includeBinRange, boolean includeBinNumber) {
        DecimalFormat df = new DecimalFormat("#0.0");
        StringBuilder sb = new StringBuilder("INT_ORI // ");

        if (includeBinRange)
            sb.append(df.format(binStart) + "° TO " + df.format(binEnd) + "° // ");

        if (includeBinNumber)
            sb.append(nBins + " BINS // ");

        sb.append(measurement);

        return sb.toString();

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_MEASURE;
    }

    @Override
    public String getVersionNumber() {
        return "1.1.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the orientation of structures in an image.  This module uses the <a href=\"https://imagej.net/plugins/directionality\">Directionality_</a> plugin to calculate core measures.  Additional measurements, such as the Alignment Index [1] are also calculated.  All measurements are made for the entire image stack; that is, the individual slice histograms are merged and normalised prior to calculation of all measurements.  For multi-slice stacks, images can first be decomposed into whole-slice objects using the CreateWholeSliceObjects module, then processed on a per-object basis using the MeasureObjectIntensityOrientation module."
                +
                "<br><br>References:<br>" +
                "<ol><li>Sun, M., et al. \"Rapid Quantification of 3D Collagen Fiber Alignment and Fiber Intersection Correlations with High Sensitivity\" <i>PLOS ONE</i> (2015), doi: https://doi.org/10.1371/journal.pone.0131814</li></ol>";
    }

    /**
     * Default Directionality_ function is to create a histogram for each slice of
     * an image. This method combines all histograms into a single, normalised
     * histogram, thus allowing a single fit to be created for the whole stack. The
     * input Directionality_ object will be updated such that it only contains the
     * merged histogram.
     * 
     * @param directionality Input Directionality_ object which has already
     *                       processed images
     */
    public static void mergeHistograms(Directionality_ directionality) {
        // Combining histograms into one
        ArrayList<double[]> histograms = directionality.getHistograms();

        int nHist = histograms.size();
        if (nHist > 1) {
            double[] rootHistogram = histograms.get(0);
            for (int i = 1; i < histograms.size(); i++) {
                double[] currHistogram = histograms.get(i);
                for (int j = 0; j < rootHistogram.length; j++)
                    rootHistogram[j] = rootHistogram[j] + currHistogram[j];
            }

            // Normalising directionality
            for (int j = 0; j < rootHistogram.length; j++)
                rootHistogram[j] = rootHistogram[j] / nHist;

            // Updating histograms with merged histogram and removing all others
            histograms.set(0, rootHistogram);
            for (int j = nHist - 1; j > 0; j--)
                histograms.remove(j);

        }
    }

    public static double[] calculateSpreadMeasures(double[] dir, double[] binsRads) {
        double n_meas = 0;
        double n_meas_sub = 0;
        double real = 0;
        double imag = 0;
        double[] meas = new double[3];

        // For calculation of the circular mean for this distribution, we need to be in
        // the range -pi to +pi. Since we're only going to calculate the alignment
        // index, the absolute values of orientation don't matter. As such, a new
        // normalised set of bins (of the same number as the input, binsRads) is created
        // in this range.
        double binInterval = 2 * Math.PI / binsRads.length;
        double[] binsNorm = new double[binsRads.length];
        for (int i = 0; i < binsRads.length; i++)
            binsNorm[i] = -Math.PI + i * binInterval;

        // Calculating the circular mean from the normalised bin range
        for (int i = 0; i < dir.length; i++) {
            real += dir[i] * Math.cos(binsNorm[i]);
            imag += dir[i] * Math.sin(binsNorm[i]);
            n_meas += dir[i];
        }

        Complex z = new Complex(real, imag);
        z.divide(new Complex(n_meas, 0));
        double th_mean = z.getArgument();

        // Alignment index (full data) from "Rapid Quantification of 3D Collagen Fiber
        // Alignment and Fiber Intersection Correlations with High Sensitivity" by Sun,
        // M. et al. (2015) PlosONE (https://doi.org/10.1371/journal.pone.0131814)
        // This calculation is done in the range -pi/2 to +pi/2. As such, the binsNorm
        // value and th_mean values are divided by 2.
        double AI = 0;
        for (int i = 0; i < dir.length; i++)
            AI += dir[i] * (2 * Math.pow(Math.cos(binsNorm[i] / 2 - th_mean / 2), 2) - 1);
        AI /= n_meas;
        meas[0] = AI;

        // Alignment index (BG subtracted)
        double[] dir_sub = BGSubDir(dir);
        for (int i = 0; i < dir.length; i++)
            n_meas_sub += dir_sub[i];

        double AI_sub = 0;
        for (int i = 0; i < dir_sub.length; i++)
            AI_sub += dir_sub[i] * (2 * Math.pow(Math.cos(binsNorm[i] / 2 - th_mean / 2), 2) - 1);

        AI_sub /= n_meas_sub;
        meas[1] = AI_sub;
        meas[2] = dir[0] - dir_sub[0];

        return meas;

    }

    public static ImageI getHistogramIJ(double[] bins, double[] data) {
        int n = 1000;
        double sum = 0;
        for (double val : data)
            sum += val;

        float[] counts = new float[n];
        int k = 0;

        for (int i = 0; i < bins.length; i++) {
            double v = data[i];
            int count = (int) Math.round(v * n / sum);
            System.out.println(sum + "_" + bins[i] + "_" + v + "_" + count);
            for (int j = 0; j < count; j++)
                counts[k++] = (float) bins[i];
        }

        ImageProcessor ip = new FloatProcessor(n, 1, counts, null);
        ImagePlus imp = new ImagePlus("", ip);
        imp.show();
        ImageStatistics stats = new StackStatistics(imp, bins.length, bins[0], bins[bins.length - 1]);
        ImagePlus histIpl = new HistogramWindow("Histogram", imp, stats).getImagePlus();

        return ImageFactory.createImage("Histogram",histIpl);

    }

    public static ImageI getHistogramRGB(Directionality_ directionality) {
        JFrame frame = directionality.plotResults();
        frame.setUndecorated(true);
        frame.setPreferredSize(new Dimension(histWidth, histHeight));
        frame.pack();        
        frame.setLocation(Integer.MAX_VALUE, Integer.MAX_VALUE);
        frame.setVisible(true);        

        BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);        
        try {
            // This pause helps ensure the frame has rendered
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        frame.paint(img.getGraphics());
        
        ImagePlus histIpl = new ImagePlus("Histogram", img);

        frame.dispose();
        
        return ImageFactory.createImage("Histogram", histIpl);

    }

    public static double[] BGSubDir(double[] dir) {
        double min = Double.POSITIVE_INFINITY;

        for (int i = 0; i < dir.length; i++)
            if (dir[i] < min)
                min = dir[i];

        double[] dir_sub = new double[dir.length];
        for (int i = 0; i < dir_sub.length; i++)
            dir_sub[i] = dir[i] - min;

        return dir_sub;

    }

    @Override
    public Status process(WorkspaceI workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        boolean outputOrientationMap = parameters.getValue(OUTPUT_ORIENTATION_MAP, workspace);
        String orientationMapName = parameters.getValue(ORIENTATION_MAP_NAME, workspace);
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
        
        ImageI inputImage = workspace.getImage(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

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

        Directionality_ directionality = new Directionality_();

        // Configuring Directionality plugin
        directionality.setImagePlus(inputImagePlus);
        directionality.setBinNumber(nBins);
        directionality.setBinRange(binStart, binEnd);
        directionality.setMethod(method);
        directionality.setBuildOrientationMapFlag(outputOrientationMap);

        // Process image
        directionality.computeHistograms();
        mergeHistograms(directionality);
        directionality.fitHistograms();

        ArrayList<double[]> fitParameters = directionality.getFitAnalysis();
        double[] results = fitParameters.iterator().next();

        String name = getFullName(binStart, binEnd, nBins, Measurements.DIRECTION, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, Math.toDegrees(results[0])));

        name = getFullName(binStart, binEnd, nBins, Measurements.DISPERSION, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, Math.toDegrees(results[1])));

        name = getFullName(binStart, binEnd, nBins, Measurements.AMOUNT, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, results[2]));

        name = getFullName(binStart, binEnd, nBins, Measurements.GOODNESS, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, results[3]));

        ArrayList<double[]> histograms = directionality.getHistograms();
        double[] hist = histograms.iterator().next();
        double[] binsDegs = directionality.getBins();
        double[] binsRads = Arrays.stream(binsDegs).map((v) -> Math.toRadians(v)).toArray();
        double[] extra_results = calculateSpreadMeasures(hist, binsRads);

        name = getFullName(binStart, binEnd, nBins, Measurements.ALIGNMENT_INDEX, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, extra_results[0]));

        name = getFullName(binStart, binEnd, nBins, Measurements.ALIGNMENT_INDEX_BG_SUB, includeBinRange,
                includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, extra_results[1]));

        name = getFullName(binStart, binEnd, nBins, Measurements.BACKGROUND, includeBinRange, includeBinNumber);
        inputImage.addMeasurement(new Measurement(name, extra_results[2]));

        if (outputOrientationMap) {
            ImagePlus oriIpl = new ImagePlus(orientationMapName, directionality.getOrientationMap());
            ImageI oriImage = ImageFactory.createImage(orientationMapName, oriIpl);
            workspace.addImage(oriImage);

            if (showOutput)
                oriImage.showAsIs();
        }

        if (saveHistogram) {
            // Image histogramImage = getHistogramIJ(binsDegs, hist);
            ImageI histogramImage = getHistogramRGB(directionality);

            String outputPath = getOutputPath(modules, workspace);
            String outputName = getOutputName(modules, workspace);

            // Adding last bits to name
            outputPath = outputPath + outputName;
            outputPath = appendSeries(outputPath, workspace, appendSeriesMode);
            outputPath = appendDateTime(outputPath, appendDateTimeMode);
            outputPath = outputPath + suffix + ".tif";

            ImageSaver.saveImage(histogramImage, ImageSaver.FileFormats.TIF, outputPath);

        }

        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(OUTPUT_ORIENTATION_MAP, this, false));
        parameters.add(new OutputImageP(ORIENTATION_MAP_NAME, this));

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

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_ORIENTATION_MAP));
        if ((boolean) parameters.getValue(OUTPUT_ORIENTATION_MAP, null))
            returnedParameters.add(parameters.getParameter(ORIENTATION_MAP_NAME));

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
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        WorkspaceI workspace = null;
        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        int nBins = parameters.getValue(NUMBER_OF_BINS, workspace);
        double binStart = parameters.getValue(HISTOGRAM_START, workspace);
        double binEnd = parameters.getValue(HISTOGRAM_END, workspace);
        boolean includeBinRange = parameters.getValue(INCLUDE_BIN_RANGE_IN_NAME, workspace);
        boolean includeBinNumber = parameters.getValue(INCLUDE_BIN_NUMBER_IN_NAME, workspace);

        String name = getFullName(binStart, binEnd, nBins, Measurements.DIRECTION, includeBinRange, includeBinNumber);
        ImageMeasurementRef ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.DISPERSION, includeBinRange, includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.AMOUNT, includeBinRange, includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.GOODNESS, includeBinRange, includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.ALIGNMENT_INDEX, includeBinRange, includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.ALIGNMENT_INDEX_BG_SUB, includeBinRange,
                includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        name = getFullName(binStart, binEnd, nBins, Measurements.BACKGROUND, includeBinRange, includeBinNumber);
        ref = imageMeasurementRefs.getOrPut(name);
        ref.setImageName(inputImageName);
        returnedRefs.add(ref);

        return returnedRefs;

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

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(INPUT_IMAGE).setDescription("");

    }
}
