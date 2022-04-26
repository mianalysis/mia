package io.github.mianalysis.mia.module.images.transform;

import static io.github.mianalysis.mia.module.images.transform.FocusStackGlobal.MinMaxMode.MAX;
import static io.github.mianalysis.mia.module.images.transform.FocusStackGlobal.MinMaxMode.MIN;
import static io.github.mianalysis.mia.module.images.transform.FocusStackGlobal.Stat.MEAN;
import static io.github.mianalysis.mia.module.images.transform.FocusStackGlobal.Stat.STDEV;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Image;
import io.github.mianalysis.mia.object.Measurement;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.ImageMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.process.ImgPlusTools;
import io.github.sjcross.common.mathfunc.CumStat;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class FocusStackGlobal <T extends RealType<T> & NativeType<T>> extends Module implements ActionListener {
    private JFrame frame;
    private DefaultListModel<Ref> listModel = new DefaultListModel<>();
    private JList<Ref> list = new JList<>(listModel);
    private JScrollPane objectsScrollPane = new JScrollPane(list);

    private ImagePlus displayImagePlus;
    private TreeMap<Integer, Integer> refs;

    private static final String ADD = "Add";
    private static final String REMOVE = "Remove";
    private static final String FINISH = "Finish";

    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_MODE = "Output mode";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CALCULATION_SEPARATOR = "Best focus calculation";
    public static final String BEST_FOCUS_CALCULATION = "Best-focus calculation";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String RELATIVE_START_SLICE = "Relative start slice";
    public static final String RELATIVE_END_SLICE = "Relative end slice";
    public static final String SMOOTH_TIMESERIES = "Smooth timeseries";
    public static final String SMOOTHING_RANGE = "Smoothing range (odd numbers)";

    public static final String REFERENCE_SEPARATOR = "Reference controls";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String CHANNEL = "Channel";

    public FocusStackGlobal(Modules modules) {
        super("Focus stack (global)", modules);
    }

    public interface OutputModes {
        String CALCULATE_ONLY = "Calculate only";
        String CALCULATE_AND_APPLY = "Calculate and apply";

        String[] ALL = new String[] { CALCULATE_ONLY, CALCULATE_AND_APPLY };

    }

    public interface BestFocusCalculations {
        String MANUAL = "Manual";
        String MIN_MEAN = "Smallest mean intensity";
        String MAX_MEAN = "Largest mean intensity";
        String MIN_STDEV = "Smallest standard deviation";
        String MAX_STDEV = "Largest standard deviation";

        String[] ALL = new String[] { MANUAL, MIN_MEAN, MAX_MEAN, MIN_STDEV, MAX_STDEV };

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[] { INTERNAL, EXTERNAL };

    }

    public interface ChannelModes {
        String USE_ALL = "Use all channels";
        String USE_SINGLE = "Use single channel";

        String[] ALL = new String[] { USE_ALL, USE_SINGLE };

    }

    public interface Measurements {
        String MEAN_SLICE = "BEST_FOCUS // MEAN_SLICE";
        String MEDIAN_SLICE = "BEST_FOCUS // MEDIAN_SLICE";
        String MIN_SLICE = "BEST_FOCUS // MIN_SLICE";
        String MAX_SLICE = "BEST_FOCUS // MAX_SLICE";
        String STDEV_SLICE = "BEST_FOCUS // STDEV_SLICE";

    }

    public interface MetadataNames {
        String SLICES = "BEST_FOCUS // SLICES";
    }

    enum Stat {
        MEAN, STDEV;
    }

    enum MinMaxMode {
        MIN, MAX;
    }

    public static String getFullName(String measurement, int channel) {
        return measurement + "_(CH" + channel + ")";

    }

    private void showOptionsPanel() {
        frame = new JFrame();
        frame.setAlwaysOnTop(true);

        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        JLabel headerLabel = new JLabel("<html>Select a timepoint and slice for each reference."
                + "<br>As a minimum, the first and last timepoints must contain a reference.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel, c);

        JButton newObjectButton = new JButton("Add reference");
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(newObjectButton, c);

        JButton removeObjectButton = new JButton("Remove reference(s)");
        removeObjectButton.addActionListener(this);
        removeObjectButton.setActionCommand(REMOVE);
        c.gridx++;
        frame.add(removeObjectButton, c);

        JButton finishButton = new JButton("Finish");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton, c);

        objectsScrollPane.setPreferredSize(new Dimension(0, 200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        objectsScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane, c);

        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Ref selected = list.getSelectedValue();
                if (selected != null) {
                    displayImagePlus.setT(selected.getTimepoint());
                    displayImagePlus.setZ(selected.getSlice());
                }
            }
        });
    }

    boolean isSingleSlice(Image image) {
        return image.getImagePlus().getNSlices() == 1;

    }

    int[] getBestFocusAuto(Image inputImage, Image calculationImage, Stat stat, MinMaxMode minMax, int channel) {
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        // Iterating over frame, extracting the relevant substack, then appending it to
        // the output
        long nFrames = inputImg.dimension(inputImg.dimensionIndex(Axes.TIME));
        int[] bestSlices = new int[(int) nFrames];
        for (int f = 0; f < nFrames; f++) {
            bestSlices[f] = getOptimalStatSlice(calculationImage, f, channel, stat, minMax);
            writeProgressStatus(f+1, (int) nFrames, "frames");
        }

        return bestSlices;

    }

    int[] getBestFocusManual(Image refImage) {
        if (refImage != null) {
            displayImagePlus = refImage.getImagePlus().duplicate();
            displayImagePlus.setLut(LUT.createLutFromColor(Color.WHITE));
            displayImagePlus.show();
        }

        showOptionsPanel();

        // All the while the control is open, do nothing
        while (frame != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Do nothing as the user has selected this
            }
        }

        // If an insufficient number of points were specified, perform polynomial
        // fitting
        int nFrames = refImage.getImagePlus().getNFrames();
        if (refs.size() < nFrames) {
            return getFitValues(nFrames);
        } else {
            return getRawValues();
        }
    }

    int[] getFitValues(int nFrames) {
        double[] x = new double[refs.size()];
        double[] y = new double[refs.size()];

        int i = 0;
        for (int t : refs.keySet()) {
            x[i] = t;
            y[i++] = refs.get(t);
        }

        LinearInterpolator splineInterpolator = new LinearInterpolator();
        PolynomialSplineFunction splineFunction = splineInterpolator.interpolate(x, y);

        int[] fitValues = new int[nFrames];
        for (int j = 0; j < nFrames; j++) {
            fitValues[j] = (int) Math.floor(splineFunction.value(j + 1));
        }

        return fitValues;

    }

    int[] getRawValues() {
        int[] values = new int[refs.size()];
        for (int t : refs.keySet())
            values[t - 1] = refs.get(t);

        return values;

    }

    static int getOptimalStatSlice(Image image, int frame, int channel, Stat stat, MinMaxMode mode) {
        ImagePlus inputIpl = image.getImagePlus();

        // Setting the channels to measure over. If channel is -1, use all channels
        int startChannel = 0;
        int endChannel = inputIpl.getNChannels();
        if (channel != -1)
            startChannel = endChannel = channel;

        // Measuring the statistics for each slice
        int bestSlice = 0;
        double bestVal = 0;
        if (mode == MIN)
            bestVal = Double.MAX_VALUE;

        for (int c = startChannel; c <= endChannel; c++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                inputIpl.setPosition(c + 1, z + 1, frame + 1);
                double val = 0;
                switch (stat) {
                case MEAN:
                    val = inputIpl.getProcessor().getStatistics().mean;
                    break;
                case STDEV:
                    val = inputIpl.getProcessor().getStatistics().stdDev;
                    break;
                }

                switch (mode) {
                case MIN:
                    if (val < bestVal) {
                        bestSlice = z;
                        bestVal = val;
                    }
                    break;
                case MAX:
                    if (val > bestVal) {
                        bestSlice = z;
                        bestVal = val;
                    }
                    break;
                }
            }
        }

        return bestSlice;

    }

    Image extract(Image inputImage, int relativeStart, int relativeEnd, int[] bestSlices,
            String outputImageName) {
        // Creating the empty container image
        ImgPlus<T> inputImg = inputImage.getImgPlus();
        ImgPlus<T> outputImg = getEmptyImage(inputImg, relativeStart, relativeEnd);

        // Extracting the best-slice substack and adding it to the outputImage
        long nFrames = inputImg.dimension(inputImg.dimensionIndex(Axes.TIME));
        for (int f = 0; f < nFrames; f++) {
            extractSubstack(inputImg, outputImg, bestSlices[f] + relativeStart, bestSlices[f] + relativeEnd, f);
            writeProgressStatus(f+1, (int) nFrames, "frames");
        }

        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg, outputImageName);
        outputImagePlus.setCalibration(inputImage.getImagePlus().getCalibration());
        if (outputImg.dimension(outputImg.dimensionIndex(Axes.Z)) == 1)
            outputImagePlus.getCalibration().pixelDepth = 1;
        ImgPlusTools.applyAxes(outputImg, outputImagePlus);

        // Adding the new image to the Workspace
        return new Image(outputImageName, outputImagePlus);

    }

    static <T extends RealType<T> & NativeType<T>> ImgPlus<T> getEmptyImage(ImgPlus<T> inputImg, int relativeStart,
            int relativeEnd) {
        // Determining the number of slices
        int nSlices = Math.max(relativeStart, relativeEnd) - Math.min(relativeStart, relativeEnd) + 1;

        long[] dims = new long[inputImg.numDimensions()];
        for (int i = 0; i < inputImg.numDimensions(); i++)
            dims[i] = inputImg.dimension(i);
        dims[inputImg.dimensionIndex(Axes.Z)] = nSlices;

        // Creating the output image and copying over the pixel coordinates
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg.firstElement());
        ImgPlus<T> outputImg = new ImgPlus<T>(factory.create(dims));
        ImgPlusTools.copyAxes(inputImg, outputImg);

        return outputImg;

    }

    static <T extends RealType<T> & NativeType<T>> void extractSubstack(ImgPlus<T> inputImg, ImgPlus<T> outputImg,
            long startSlice, long endSlice, int frame) {
        // At this point, the start and end slices may be out of range of the input
        // image
        long nActualSlices = inputImg.dimension(inputImg.dimensionIndex(Axes.Z));
        long actualOffset = Math.abs(Math.min(0, startSlice));
        startSlice = Math.max(startSlice, 0);
        endSlice = Math.min(endSlice, nActualSlices - 1);

        // Dimensions for the substack are the same in the input and output images
        int xIdxIn = inputImg.dimensionIndex(Axes.X);
        int yIdxIn = inputImg.dimensionIndex(Axes.Y);
        int zIdxIn = inputImg.dimensionIndex(Axes.Z);
        int cIdxIn = inputImg.dimensionIndex(Axes.CHANNEL);
        int tIdxIn = inputImg.dimensionIndex(Axes.TIME);

        long[] dimsIn = new long[inputImg.numDimensions()];
        if (xIdxIn != -1)
            dimsIn[xIdxIn] = inputImg.dimension(xIdxIn);
        if (yIdxIn != -1)
            dimsIn[yIdxIn] = inputImg.dimension(yIdxIn);
        if (cIdxIn != -1)
            dimsIn[cIdxIn] = inputImg.dimension(cIdxIn);
        if (zIdxIn != -1)
            dimsIn[zIdxIn] = endSlice - startSlice + 1;
        if (tIdxIn != -1)
            dimsIn[tIdxIn] = 1;

        long[] offsetIn = new long[inputImg.numDimensions()];
        if (xIdxIn != -1)
            offsetIn[xIdxIn] = 0;
        if (yIdxIn != -1)
            offsetIn[yIdxIn] = 0;
        if (cIdxIn != -1)
            offsetIn[cIdxIn] = 0;
        if (zIdxIn != -1)
            offsetIn[zIdxIn] = startSlice;
        if (tIdxIn != -1)
            offsetIn[tIdxIn] = frame;

        int xIdxOut = outputImg.dimensionIndex(Axes.X);
        int yIdxOut = outputImg.dimensionIndex(Axes.Y);
        int zIdxOut = outputImg.dimensionIndex(Axes.Z);
        int cIdxOut = outputImg.dimensionIndex(Axes.CHANNEL);
        int tIdxOut = outputImg.dimensionIndex(Axes.TIME);

        long[] dimsOut = new long[outputImg.numDimensions()];
        if (xIdxOut != -1)
            dimsOut[xIdxOut] = outputImg.dimension(xIdxOut);
        if (yIdxOut != -1)
            dimsOut[yIdxOut] = outputImg.dimension(yIdxOut);
        if (cIdxOut != -1)
            dimsOut[cIdxOut] = outputImg.dimension(cIdxOut);
        if (zIdxOut != -1)
            dimsOut[zIdxOut] = endSlice - startSlice + 1;
        if (tIdxOut != -1)
            dimsOut[tIdxOut] = 1;

        long[] offsetOut = new long[outputImg.numDimensions()];
        if (xIdxOut != -1)
            offsetOut[xIdxOut] = 0;
        if (yIdxOut != -1)
            offsetOut[yIdxOut] = 0;
        if (cIdxOut != -1)
            offsetOut[cIdxOut] = 0;
        if (zIdxOut != -1)
            offsetOut[zIdxOut] = actualOffset;
        if (tIdxOut != -1)
            offsetOut[tIdxOut] = frame;

        Cursor<T> cursorIn = Views.offsetInterval(inputImg, offsetIn, dimsIn).localizingCursor();
        RandomAccess<T> randomAccessOut = Views.offsetInterval(outputImg, offsetOut, dimsOut).randomAccess();

        while (cursorIn.hasNext()) {
            cursorIn.fwd();
            randomAccessOut.setPosition(cursorIn);
            randomAccessOut.get().set(cursorIn.get());
        }
    }

    private int[] rollingMedianFilter(int[] vals, int range) {
        // Getting the half width (odd numbers need to subtract 1)
        int halfW = (range - range % 2) / 2;

        int[] filtered = new int[vals.length];

        for (int i = 0; i < vals.length; i++) {
            // Getting the min val
            int start = Math.max(0, i - halfW);
            int end = Math.min(vals.length - 1, i + halfW);

            // Get median of values in this range
            int nVals = end - start + 1;
            double[] currVals = new double[nVals];
            for (int j = 0; j < nVals; j++)
                currVals[j] = vals[start + j];

            double median = new Median().evaluate(currVals);

            filtered[i] = (int) median;

        }

        return filtered;

    }

    static <T extends RealType<T> & NativeType<T>> void addMeasurements(Image image, int[] slices) {
        CumStat cs = new CumStat();
        for (int slice : slices)
            cs.addMeasure(slice);

        // CumStat can't calculate median, so doing that separately
        double[] doubleSlices = Arrays.stream(slices).asDoubleStream().toArray();
        double median = new Median().evaluate(doubleSlices);

        image.addMeasurement(new Measurement(Measurements.MEAN_SLICE, cs.getMean()));
        image.addMeasurement(new Measurement(Measurements.MEDIAN_SLICE, median));
        image.addMeasurement(new Measurement(Measurements.MIN_SLICE, cs.getMin()));
        image.addMeasurement(new Measurement(Measurements.MAX_SLICE, cs.getMax()));
        image.addMeasurement(new Measurement(Measurements.STDEV_SLICE, cs.getStd()));

    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_TRANSFORM;
    }

    @Override
    public String getDescription() {
        return "Extract a Z-substack from an input stack based on either manually-selected slices, or an automatically-calculated best-focus slice.  For automated methods, best focus is determined using intensity statistics (e.g. largest variance) of all pixels in each slice.  When in manual mode, only the best focus slice for the first and last timepoints need be specified (all others will be estimated using polynomial spline interpolation); however, more frames can be specified if preferred.<br><br>Irrespective of the calculation method (manual or automatic), it's possible to extract a fixed number of slices above and below the determined best-focus slice.";
    }

    @Override
    protected Status process(Workspace workspace) {
        // Remove any previously-entered references
        listModel.clear();

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting other parameters
        String outputMode = parameters.getValue(OUTPUT_MODE);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String bestFocusCalculation = parameters.getValue(BEST_FOCUS_CALCULATION);
        int relativeStart = parameters.getValue(RELATIVE_START_SLICE);
        int relativeEnd = parameters.getValue(RELATIVE_END_SLICE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        int channel = ((int) parameters.getValue(CHANNEL)) - 1;
        boolean smoothTimeseries = parameters.getValue(SMOOTH_TIMESERIES);
        int smoothingRange = parameters.getValue(SMOOTHING_RANGE);

        // Checking if there is only a single slice to start with
        if (isSingleSlice(inputImage)) {
            // Adding blank measurements
            updateAndGetImageMeasurementRefs().addBlankMeasurements(inputImage);
            
            if (outputMode.equals(OutputModes.CALCULATE_AND_APPLY)) {
                Image outputImage = new Image(outputImageName, inputImage.getImagePlus().duplicate());
                workspace.addImage(outputImage);

                if (showOutput)
                    outputImage.showImage();
            }

            return Status.PASS;

        }

        // Making sure the start and end are the right way round
        if (relativeStart > relativeEnd) {
            int a = relativeStart;
            relativeStart = relativeEnd;
            relativeEnd = a;
        }

        // The input image will be used for calculation unless an external image was
        // specified
        Image calculationImage = inputImage;
        switch (calculationSource) {
        case CalculationSources.EXTERNAL:
            calculationImage = workspace.getImage(externalSourceName);
            break;
        }

        // Getting best focus slice indices
        int[] bestSlices;
        switch (bestFocusCalculation) {
        case BestFocusCalculations.MANUAL:
            Image refImage = workspace.getImage(referenceImageName);
            bestSlices = getBestFocusManual(refImage);
            String metadataString = Arrays.stream(bestSlices).mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
            workspace.getMetadata().put(MetadataNames.SLICES, metadataString);
            break;

        case BestFocusCalculations.MIN_MEAN:
            // Setting the channel number to zero-indexed or -1 if using all channels
            if (channelMode.equals(ChannelModes.USE_ALL))
                channel = -1;
            bestSlices = getBestFocusAuto(inputImage, calculationImage, MEAN, MIN, channel);
            break;

        case BestFocusCalculations.MAX_MEAN:
            // Setting the channel number to zero-indexed or -1 if using all channels
            if (channelMode.equals(ChannelModes.USE_ALL))
                channel = -1;
            bestSlices = getBestFocusAuto(inputImage, calculationImage, MEAN, MAX, channel);
            break;

        case BestFocusCalculations.MIN_STDEV:
            // Setting the channel number to zero-indexed or -1 if using all channels
            if (channelMode.equals(ChannelModes.USE_ALL))
                channel = -1;
            bestSlices = getBestFocusAuto(inputImage, calculationImage, STDEV, MIN, channel);
            break;

        case BestFocusCalculations.MAX_STDEV:
            // Setting the channel number to zero-indexed or -1 if using all channels
            if (channelMode.equals(ChannelModes.USE_ALL))
                channel = -1;
            bestSlices = getBestFocusAuto(inputImage, calculationImage, STDEV, MAX, channel);
            break;

        default:
            return Status.FAIL;
        }

        // Applying temporal smoothing of best focus slice index
        if (smoothTimeseries)
            bestSlices = rollingMedianFilter(bestSlices, smoothingRange);

        // Adding measurements
        addMeasurements(inputImage, bestSlices);
        if (showOutput)
            inputImage.showMeasurements(this);

        if (outputMode.equals(OutputModes.CALCULATE_AND_APPLY)) {
            Image outputImage = extract(inputImage, relativeStart, relativeEnd, bestSlices, outputImageName);
            workspace.addImage(outputImage);

            if (showOutput)
                outputImage.showImage();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ChoiceP(OUTPUT_MODE, this, OutputModes.CALCULATE_AND_APPLY, OutputModes.ALL));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(
                new ChoiceP(BEST_FOCUS_CALCULATION, this, BestFocusCalculations.MAX_STDEV, BestFocusCalculations.ALL));
        parameters.add(new IntegerP(RELATIVE_START_SLICE, this, 0));
        parameters.add(new IntegerP(RELATIVE_END_SLICE, this, 0));

        parameters.add(new SeparatorP(REFERENCE_SEPARATOR, this));
        parameters.add(new InputImageP(REFERENCE_IMAGE, this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE, this, CalculationSources.INTERNAL, CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE, this));
        parameters.add(new ChoiceP(CHANNEL_MODE, this, ChannelModes.USE_SINGLE, ChannelModes.ALL));
        parameters.add(new IntegerP(CHANNEL, this, 1));
        parameters.add(new BooleanP(SMOOTH_TIMESERIES, this, false));
        parameters.add(new IntegerP(SMOOTHING_RANGE, this, 5));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_MODE));

        if (parameters.getValue(OUTPUT_MODE).equals(OutputModes.CALCULATE_AND_APPLY))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(BEST_FOCUS_CALCULATION));
        returnedParameters.add(parameters.getParameter(RELATIVE_START_SLICE));
        returnedParameters.add(parameters.getParameter(RELATIVE_END_SLICE));

        returnedParameters.add(parameters.getParameter(REFERENCE_SEPARATOR));
        switch ((String) parameters.getValue(BEST_FOCUS_CALCULATION)) {
        case BestFocusCalculations.MANUAL:
            returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
            break;

        case BestFocusCalculations.MIN_MEAN:
        case BestFocusCalculations.MAX_MEAN:
        case BestFocusCalculations.MIN_STDEV:
        case BestFocusCalculations.MAX_STDEV:
            returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
            switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
            }

            returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
            switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.USE_SINGLE:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                break;
            }
            break;
        }

        returnedParameters.add(parameters.getParameter(SMOOTH_TIMESERIES));
        if ((boolean) parameters.getValue(SMOOTH_TIMESERIES)) {
            returnedParameters.add(parameters.getParameter(SMOOTHING_RANGE));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementRefs returnedRefs = new ImageMeasurementRefs();

        ImageMeasurementRef measurementRef = imageMeasurementRefs.getOrPut(Measurements.MEAN_SLICE);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        measurementRef = imageMeasurementRefs.getOrPut(Measurements.MEDIAN_SLICE);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        measurementRef = imageMeasurementRefs.getOrPut(Measurements.MIN_SLICE);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        measurementRef = imageMeasurementRefs.getOrPut(Measurements.MAX_SLICE);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        measurementRef = imageMeasurementRefs.getOrPut(Measurements.STDEV_SLICE);
        measurementRef.setImageName(inputImageName);
        returnedRefs.add(measurementRef);

        return returnedRefs;

    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        MetadataRefs returnedRefs = new MetadataRefs();

        if (parameters.getValue(BEST_FOCUS_CALCULATION).equals(BestFocusCalculations.MANUAL)) {
            returnedRefs.add(metadataRefs.getOrPut(MetadataNames.SLICES));
        }

        return returnedRefs;

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image to extract substack from.");

        parameters.get(OUTPUT_MODE).setDescription("Controls whether the best focus positions are calculated and applied (creating a new image) or simply calculated.  In both cases, statistics for the best focus position (mean, median, minimum, maximum and standard deviation of slices) are stored as measurements associated with the input image.");

        parameters.get(OUTPUT_IMAGE).setDescription("Substack image to be added to the current workspace.");

        parameters.get(BEST_FOCUS_CALCULATION).setDescription("Method for determining the best-focus slice.<br><ul>"

                + "<li>\"" + BestFocusCalculations.MANUAL
                + "\" Displays a control window, allowing the user to specify reference slices.  These slices should be at the same true z-plane.  Once complete, a substack will be extracted a specific number of slices above and below the reference plane (defined by \""
                + RELATIVE_START_SLICE + "\" and \"" + RELATIVE_END_SLICE
                + "\".  If references aren't specific for all timepoints, the missing frames will be estimated using polynomial spline interpolation.</li>"

                + "<li>\"" + BestFocusCalculations.MIN_MEAN
                + "\" The reference slice is taken as the slice with the minimum mean intensity.</li>"

                + "<li>\"" + BestFocusCalculations.MAX_MEAN
                + "\" The reference slice is taken as the slice with the maximum mean intensity.</li>"

                + "<li>\"" + BestFocusCalculations.MIN_STDEV
                + "\" The reference slice is taken as the slice with the minimum intensity standard deviation.</li>"

                + "<li>\"" + BestFocusCalculations.MAX_STDEV
                + "\" The reference slice is taken as the slice with the maximum intensity standard deviation.</li></ul>");

        parameters.get(RELATIVE_START_SLICE).setDescription(
                "Index of start slice relative to determined best-focus slice (i.e. -5 is 5 slices below the best-focus).");

        parameters.get(RELATIVE_END_SLICE).setDescription(
                "Index of end slice relative to determined best-focus slice (i.e. 5 is 5 slices above the best-focus).");

        parameters.get(REFERENCE_IMAGE).setDescription(
                "If using manual selection of best focus slices, this is the image that will be shown to the user.  While it doesn't need to be the input image (the one the output substack will be generated from), it must have the same number of slices and timepoints as the input.");

        parameters.get(CALCULATION_SOURCE).setDescription(
                "When using automatic best focus slice determination this controls the image source:<br><ul>"

                        + "<li>\"" + CalculationSources.EXTERNAL
                        + "\" The image for which intensity statistics are calculated is different to the image that the final substack will be created from.  For example, this could be an filtered version of the input image to enhance structures when in focus.</li>"

                        + "<li>\"" + CalculationSources.INTERNAL
                        + "\" The same image will be used for determination of the best slice and generation of the output substack.</li></ul>");

        parameters.get(EXTERNAL_SOURCE).setDescription("If using a separate image to determine the best focus slice (\""
                + CALCULATION_SOURCE + "\" set to \"" + CalculationSources.EXTERNAL + "\").");

        parameters.get(CHANNEL_MODE)
                .setDescription("How many channels to use when calculating the best-focus slice.  \""
                        + ChannelModes.USE_ALL + "\" will use all channels, whereas \"" + ChannelModes.USE_SINGLE
                        + "\" will base the calculation on a single, user-defined channel.");

        parameters.get(CHANNEL).setDescription("Channel to base the best-focus calculation on.");

        parameters.get(SMOOTH_TIMESERIES).setDescription(
                "Apply median filter to best focus slice index over time.  This should smooth the transitions over time (prevent large jumps between frames).");

        parameters.get(SMOOTHING_RANGE).setDescription(
                "Number of frames over which to calculate the median.  If the specified number is even it will be increased by 1.");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case (ADD):
            addReference();
            break;

        case (REMOVE):
            removeReference();
            break;

        case (FINISH):
            if (checkEnds()) {
                complete();
                frame.dispose();
                frame = null;
                displayImagePlus.close();
            } else {
                IJ.error("References must be provided for the first and last timepoints");
            }

            break;
        }
    }

    private void addReference() {
        // Getting currently-selected frame and slice
        int t = displayImagePlus.getT();
        int z = displayImagePlus.getZ();

        // Check if this timepoint has been pre-specified
        if (checkTimepointExists(t)) {
            IJ.error("Timepoint already specified.  Please remove existing reference first.");
            return;
        }

        // Creating a reference and adding to the list
        Ref ref = new Ref(t, z);
        listModel.addElement(ref);

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum() - 1);
        objectsScrollPane.revalidate();

    }

    private void removeReference() {
        // Get selected ROIs
        List<Ref> selected = list.getSelectedValuesList();
        list.setSelectedIndex(-1);

        for (Ref ref : selected)
            listModel.removeElement(ref);

    }

    private boolean checkTimepointExists(int t) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getTimepoint() == t)
                return true;
        }

        return false;

    }

    private boolean checkEnds() {
        int first = 1;
        int last = displayImagePlus.getNFrames();

        boolean foundFirst = false;
        boolean foundLast = false;

        for (int i = 0; i < listModel.size(); i++) {
            Ref ref = listModel.get(i);
            if (ref == null)
                continue;

            if (ref.getTimepoint() == first)
                foundFirst = true;
            if (ref.getTimepoint() == last)
                foundLast = true;

        }

        return foundFirst && foundLast;

    }

    private void complete() {
        refs = new TreeMap<>();

        for (int i = 0; i < listModel.size(); i++) {
            Ref ref = listModel.get(i);
            if (ref != null)
                refs.put(ref.getTimepoint(), ref.getSlice());
        }
    }

    class Ref {
        private int timepoint;
        private int slice;

        Ref(int timepoint, int slice) {
            this.timepoint = timepoint;
            this.slice = slice;

        }

        public int getTimepoint() {
            return timepoint;
        }

        public void setTimepoint(int timepoint) {
            this.timepoint = timepoint;
        }

        public int getSlice() {
            return slice;
        }

        public void setSlice(int slice) {
            this.slice = slice;
        }

        @Override
        public String toString() {
            return "Timepoint " + timepoint + ", slice " + slice;
        }
    }
}
