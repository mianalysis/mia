package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.process.LUT;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.Process.ImgPlusTools;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static wbif.sjx.MIA.Module.ImageProcessing.Stack.BestFocusSubstack.MinMaxMode.MAX;
import static wbif.sjx.MIA.Module.ImageProcessing.Stack.BestFocusSubstack.MinMaxMode.MIN;

public class BestFocusSubstack <T extends RealType<T> & NativeType<T>> extends Module implements ActionListener {
    private JFrame frame;
    private JTextField objectNumberField;
    private DefaultListModel<Ref> listModel = new DefaultListModel<>();
    private JList<Ref> list = new JList<>(listModel);
    private JScrollPane objectsScrollPane = new JScrollPane(list);

    private ImagePlus displayImagePlus;
    private TreeMap<Integer,Integer> refs;

    private static final String ADD = "Add";
    private static final String REMOVE = "Remove";
    private static final String FINISH = "Finish";

    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
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

    public BestFocusSubstack(ModuleCollection modules) {
        super("Best focus stack",modules);
    }


    public interface BestFocusCalculations {
        String MANUAL = "Manual";
        String MAX_MEAN = "Largest mean intensity";
        String MIN_STDEV = "Smallest standard deviation";
        String MAX_STDEV = "Largest standard deviation";

        String[] ALL = new String[]{MANUAL,MAX_MEAN,MIN_STDEV,MAX_STDEV};

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[]{INTERNAL,EXTERNAL};

    }

    public interface ChannelModes {
        String USE_ALL = "Use all channels";
        String USE_SINGLE = "Use single channel";

        String[] ALL = new String[]{USE_ALL, USE_SINGLE};

    }

    public interface MetadataNames {
        String SLICES =  "BEST_FOCUS // SLICES";
    }

    enum Stat {
        MEAN,STDEV;
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
        c.insets = new Insets(5,5,5,5);

        JLabel headerLabel = new JLabel("<html>Select a timepoint and slice for each reference." +
                "<br>As a minimum, the first and last timepoints must contain a reference.</html>");
        headerLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        frame.add(headerLabel,c);

        JButton newObjectButton = new JButton("Add reference");
        newObjectButton.addActionListener(this);
        newObjectButton.setActionCommand(ADD);
        c.gridy++;
        c.gridwidth = 1;
        frame.add(newObjectButton,c);

        JButton removeObjectButton = new JButton("Remove reference(s)");
        removeObjectButton.addActionListener(this);
        removeObjectButton.setActionCommand(REMOVE);
        c.gridx++;
        frame.add(removeObjectButton,c);

        JButton finishButton = new JButton("Finish");
        finishButton.addActionListener(this);
        finishButton.setActionCommand(FINISH);
        c.gridx++;
        frame.add(finishButton,c);

        objectsScrollPane.setPreferredSize(new Dimension(0,200));
        objectsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        objectsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        objectsScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.gridheight = 3;
        c.fill = GridBagConstraints.BOTH;
        frame.add(objectsScrollPane,c);

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

    int[] getBestFocusAuto(Image<T> inputImage, Image calculationImage, String bestFocusCalculation, MinMaxMode mode, int channel) {
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        // Iterating over frame, extracting the relevant substack, then appending it to the output
        long nFrames = inputImg.dimension(inputImg.dimensionIndex(Axes.TIME));
        int[] bestSlices = new int[(int) nFrames];
        for (int f=0;f<nFrames;f++) {
            // Determining the best slice
            switch (bestFocusCalculation) {
                case BestFocusCalculations.MAX_MEAN:
                    bestSlices[f] = getOptimalStatSlice(calculationImage,f,channel,Stat.MEAN,mode);
                    break;
                case BestFocusCalculations.MAX_STDEV:
                    bestSlices[f] = getOptimalStatSlice(calculationImage,f,channel,Stat.STDEV,mode);
                    break;
            }

            writeMessage("Best focus for frame "+(f+1)+" at "+(bestSlices[f]+1) +" (provisional)");

        }

        return bestSlices;

    }

    int[] getBestFocusManual(Image<T> refImage) {
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
                e.printStackTrace();
            }
        }

        // If an insufficient number of points were specified, perform polynomial fitting
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

        int i=0;
        for (int t:refs.keySet()) {
            x[i] = t;
            y[i++] = refs.get(t);
        }

        LinearInterpolator splineInterpolator = new LinearInterpolator();
        PolynomialSplineFunction splineFunction = splineInterpolator.interpolate(x,y);

        int[] fitValues = new int[nFrames];
        for (int j=0;j<nFrames;j++) {
            fitValues[j] = (int) Math.floor(splineFunction.value(j+1));
        }

        return fitValues;

    }

    int[] getRawValues() {
        int[] values = new int[refs.size()];
        for (int t:refs.keySet()) values[t-1] = refs.get(t);

        return values;

    }

    static int getOptimalStatSlice(Image image, int frame, int channel, Stat stat, MinMaxMode mode) {
        ImagePlus inputIpl = image.getImagePlus();

        // Setting the channels to measure over.  If channel is -1, use all channels
        int startChannel = 0;
        int endChannel = inputIpl.getNChannels();
        if (channel != -1) startChannel = endChannel = channel;

        // Measuring the statistics for each slice
        int bestSlice = 0;
        double bestVal = 0;

        for (int c=startChannel;c<=endChannel;c++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                inputIpl.setPosition(c+1,z+1,frame+1);
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

    Image<T> extract(Image<T> inputImage, int relativeStart, int relativeEnd, int[] bestSlices, String outputImageName) {
        // Creating the empty container image
        ImgPlus<T> inputImg = inputImage.getImgPlus();
        ImgPlus<T> outputImg = getEmptyImage(inputImg,relativeStart,relativeEnd);

        // Extracting the best-slice substack and adding it to the outputImage
        long nFrames = inputImg.dimension(inputImg.dimensionIndex(Axes.TIME));
        for (int f=0;f<nFrames;f++) {
            extractSubstack(inputImg, outputImg, bestSlices[f] + relativeStart, bestSlices[f] + relativeEnd, f);
        }

        ImagePlus outputImagePlus = ImageJFunctions.wrap(outputImg,outputImageName);
        outputImagePlus.setCalibration(inputImage.getImagePlus().getCalibration());
        if (outputImg.dimension(outputImg.dimensionIndex(Axes.Z))==1) outputImagePlus.getCalibration().pixelDepth = 1;
        ImgPlusTools.applyAxes(outputImg,outputImagePlus);

        // Adding the new image to the Workspace
        return new Image<T>(outputImageName,outputImagePlus);

    }

    static <T extends RealType<T> & NativeType<T>> ImgPlus<T> getEmptyImage(ImgPlus<T> inputImg, int relativeStart, int relativeEnd) {
        // Determining the number of slices
        int nSlices = Math.max(relativeStart,relativeEnd) - Math.min(relativeStart,relativeEnd) + 1;

        long[] dims = new long[inputImg.numDimensions()];
        for (int i=0;i<inputImg.numDimensions();i++) dims[i] = inputImg.dimension(i);
        dims[inputImg.dimensionIndex(Axes.Z)] = nSlices;

        // Creating the output image and copying over the pixel coordinates
        CellImgFactory<T> factory = new CellImgFactory<T>((T) inputImg.firstElement());
        ImgPlus<T> outputImg = new ImgPlus<T>(factory.create(dims));
        ImgPlusTools.copyAxes(inputImg,outputImg);

        return outputImg;

    }

    static <T extends RealType<T> & NativeType<T>> void extractSubstack(ImgPlus<T> inputImg, ImgPlus<T> outputImg, long startSlice, long endSlice, int frame) {
        // At this point, the start and end slices may be out of range of the input image
        long nActualSlices = inputImg.dimension(inputImg.dimensionIndex(Axes.Z));
        long actualOffset = Math.abs(Math.min(0,startSlice));
        startSlice = Math.max(startSlice,0);
        endSlice = Math.min(endSlice,nActualSlices-1);

        // Dimensions for the substack are the same in the input and output images
        int xIdxIn = inputImg.dimensionIndex(Axes.X);
        int yIdxIn = inputImg.dimensionIndex(Axes.Y);
        int zIdxIn = inputImg.dimensionIndex(Axes.Z);
        int cIdxIn = inputImg.dimensionIndex(Axes.CHANNEL);
        int tIdxIn = inputImg.dimensionIndex(Axes.TIME);

        long[] dimsIn = new long[inputImg.numDimensions()];
        if (xIdxIn != -1) dimsIn[xIdxIn] = inputImg.dimension(xIdxIn);
        if (yIdxIn != -1) dimsIn[yIdxIn] = inputImg.dimension(yIdxIn);
        if (cIdxIn != -1) dimsIn[cIdxIn] = inputImg.dimension(cIdxIn);
        if (zIdxIn != -1) dimsIn[zIdxIn] = endSlice-startSlice+1;
        if (tIdxIn != -1) dimsIn[tIdxIn] = 1;

        long[] offsetIn = new long[inputImg.numDimensions()];
        if (xIdxIn != -1) offsetIn[xIdxIn] = 0;
        if (yIdxIn != -1) offsetIn[yIdxIn] = 0;
        if (cIdxIn != -1) offsetIn[cIdxIn] = 0;
        if (zIdxIn != -1) offsetIn[zIdxIn] = startSlice;
        if (tIdxIn != -1) offsetIn[tIdxIn] = frame;

        int xIdxOut = outputImg.dimensionIndex(Axes.X);
        int yIdxOut = outputImg.dimensionIndex(Axes.Y);
        int zIdxOut = outputImg.dimensionIndex(Axes.Z);
        int cIdxOut = outputImg.dimensionIndex(Axes.CHANNEL);
        int tIdxOut = outputImg.dimensionIndex(Axes.TIME);

        long[] dimsOut = new long[outputImg.numDimensions()];
        if (xIdxOut != -1) dimsOut[xIdxOut] = outputImg.dimension(xIdxOut);
        if (yIdxOut != -1) dimsOut[yIdxOut] = outputImg.dimension(yIdxOut);
        if (cIdxOut != -1) dimsOut[cIdxOut] = outputImg.dimension(cIdxOut);
        if (zIdxOut != -1) dimsOut[zIdxOut] = endSlice-startSlice+1;
        if (tIdxOut != -1) dimsOut[tIdxOut] = 1;

        long[] offsetOut = new long[outputImg.numDimensions()];
        if (xIdxOut != -1) offsetOut[xIdxOut] = 0;
        if (yIdxOut != -1) offsetOut[yIdxOut] = 0;
        if (cIdxOut != -1) offsetOut[cIdxOut] = 0;
        if (zIdxOut != -1) offsetOut[zIdxOut] = actualOffset;
        if (tIdxOut != -1) offsetOut[tIdxOut] = frame;

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
        int halfW = (range - range%2)/2;

        int[] filtered = new int[vals.length];

        for (int i=0;i<vals.length;i++) {
            // Getting the min val
            int start = Math.max(0,i-halfW);
            int end = Math.min(vals.length-1,i+halfW);

            // Get median of values in this range
            int nVals = end-start+1;
            double[] currVals = new double[nVals];
            for (int j=0;j<nVals;j++) currVals[j] = vals[start+j];

            double median = new Median().evaluate(currVals);

            filtered[i] = (int) median;

        }

        return filtered;

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Extract a Z-substack from an input stack based on either manually-selected slices, " +
                "or an automatically-calculated best-focus slice.  " +
                "For automated methods, best focus is determined using the local 2D variance of pixels in each slice.  " +
                "It is possible to extract a fixed number of slices above and below the determined best-focus slice.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        // Remove any previously-entered references
        listModel.clear();

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        // Getting other parameters
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

        // Making sure the start and end are the right way round
        if (relativeStart > relativeEnd) {
            int a = relativeStart;
            relativeStart = relativeEnd;
            relativeEnd = a;
        }

        // The input image will be used for calculation unless an external image was specified
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
                String metadataString = Arrays.stream(bestSlices).mapToObj(String::valueOf).collect(Collectors.joining(","));
                workspace.getMetadata().put(MetadataNames.SLICES,metadataString);
                break;

            case BestFocusCalculations.MAX_MEAN:
            case BestFocusCalculations.MAX_STDEV:
                // Setting the channel number to zero-indexed or -1 if using all channels
                if (channelMode.equals(ChannelModes.USE_ALL)) channel = -1;
                bestSlices = getBestFocusAuto(inputImage,calculationImage,bestFocusCalculation,MAX,channel);
                break;

            case BestFocusCalculations.MIN_STDEV:
                // Setting the channel number to zero-indexed or -1 if using all channels
                if (channelMode.equals(ChannelModes.USE_ALL)) channel = -1;
                bestSlices = getBestFocusAuto(inputImage,calculationImage,bestFocusCalculation,MIN,channel);
                break;

            default:
                return false;
        }

        // Applying temporal smoothing of best focus slice index
        if (smoothTimeseries) bestSlices = rollingMedianFilter(bestSlices,smoothingRange);

        Image outputImage = extract(inputImage,relativeStart,relativeEnd,bestSlices,outputImageName);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Image to extract substack from."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,"","Substack image to be added to the current workspace."));

        parameters.add(new ParamSeparatorP(CALCULATION_SEPARATOR,this));
        parameters.add(new ChoiceP(BEST_FOCUS_CALCULATION,this,BestFocusCalculations.MAX_STDEV,BestFocusCalculations.ALL,"Method for determining the best-focus slice.  \""+BestFocusCalculations.MAX_STDEV+"\" calculates the standard deviation of each slice."));//"Method for determining the best-focus slice.  \""+BestFocusCalculations.MAX_MEAN_VARIANCE+"\" calculates the mean variance of each slice, then takes the slice with the largest mean.  \""+BestFocusCalculations.MAX_VARIANCE+"\" simply takes the slice with the largest variance."));
        parameters.add(new IntegerP(RELATIVE_START_SLICE,this,0,"Index of start slice relative to determined best-focus slice (i.e. -5 is 5 slices below the best-focus)."));
        parameters.add(new IntegerP(RELATIVE_END_SLICE,this,0,"Index of end slice relative to determined best-focus slice (i.e. 5 is 5 slices above the best-focus)."));
        parameters.add(new BooleanP(SMOOTH_TIMESERIES,this,false,"Apply median filter to best focus slice index over time.  This should smooth the transitions over time (prevent large jumps between frames)."));
        parameters.add(new IntegerP(SMOOTHING_RANGE,this,5,"Number of frames over which to calculate the median.  If the specified number is even it will be increased by 1."));

        parameters.add(new ParamSeparatorP(REFERENCE_SEPARATOR,this));
        parameters.add(new InputImageP(REFERENCE_IMAGE,this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE,this, UnwarpImages.CalculationSources.INTERNAL, UnwarpImages.CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE,this));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.USE_SINGLE,ChannelModes.ALL,"How many channels to use when calculating the best-focus slice.  \""+ChannelModes.USE_ALL+"\" will use all channels, whereas \""+ChannelModes.USE_SINGLE+"\" will base the calculation on a single, user-defined channel."));
        parameters.add(new IntegerP(CHANNEL,this,1,"Channel to base the best-focus calculation on."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
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

            case BestFocusCalculations.MAX_MEAN:
            case BestFocusCalculations.MAX_STDEV:
                returnedParameters.add(parameters.getParameter(SMOOTH_TIMESERIES));
                if ((boolean) parameters.getValue(SMOOTH_TIMESERIES)) {
                    returnedParameters.add(parameters.getParameter(SMOOTHING_RANGE));
                }

                returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
                switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
                    case UnwarpImages.CalculationSources.EXTERNAL:
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

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        imageMeasurementRefs.setAllAvailable(false);
//
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//
//        MeasurementRef measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE);
//        measurementRef.setAvailable(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.addRef(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE_SLICE);
//        measurementRef.setAvailable(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.addRef(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE);
//        measurementRef.setAvailable(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.addRef(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE_SLICE);
//        measurementRef.setAvailable(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.addRef(measurementRef);
//
//        return imageMeasurementRefs;

        return null;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        if (parameters.getValue(BEST_FOCUS_CALCULATION).equals(BestFocusCalculations.MANUAL)) {
            returnedRefs.add(metadataRefs.getOrPut(MetadataNames.SLICES));
        }

        return returnedRefs;

    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
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
        Ref ref = new Ref(t,z);
        listModel.addElement(ref);

        // Ensuring the scrollbar is visible if necessary and moving to the bottom
        JScrollBar scrollBar = objectsScrollPane.getVerticalScrollBar();
        scrollBar.setValue(scrollBar.getMaximum()-1);
        objectsScrollPane.revalidate();

    }

    private void removeReference() {
        // Get selected ROIs
        List<Ref> selected = list.getSelectedValuesList();
        list.setSelectedIndex(-1);

        for (Ref ref : selected) listModel.removeElement(ref);

    }

    private boolean checkTimepointExists(int t) {
        for (int i=0;i<listModel.size();i++) {
            if (listModel.get(i).getTimepoint() == t) return true;
        }

        return false;

    }

    private boolean checkEnds() {
        int first = 1;
        int last = displayImagePlus.getNFrames();

        boolean foundFirst = false;
        boolean foundLast = false;

        for (int i=0;i<listModel.size();i++) {
            Ref ref = listModel.get(i);
            if (ref == null) continue;

            if (ref.getTimepoint() == first) foundFirst = true;
            if (ref.getTimepoint() == last) foundLast = true;

        }

        return foundFirst && foundLast;

    }

    private void complete() {
        refs = new TreeMap<>();

        for (int i=0;i<listModel.size();i++) {
            Ref ref = listModel.get(i);
            if (ref != null) refs.put(ref.getTimepoint(),ref.getSlice());
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
            return "Timepoint "+timepoint+", slice "+slice;
        }
    }
}
