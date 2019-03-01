package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Cursor;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;
import wbif.sjx.common.Process.ImgPlusTools;

public class BestFocusSubstack <T extends RealType<T> & NativeType<T>> extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String BEST_FOCUS_CALCULATION = "Best-focus calculation";
    //    public static final String RADIUS = "Variance calculation range";
//    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String RELATIVE_START_SLICE = "Relative start slice";
    public static final String RELATIVE_END_SLICE = "Relative end slice";
    public static final String CHANNEL_MODE = "Channel mode";
    public static final String CHANNEL = "Channel";

    public interface BestFocusCalculations {
        String MAX_STDEV = "Largest standard deviation";
//        String MAX_MEAN_VARIANCE = "Largest mean local variance";
//        String MAX_VARIANCE = "Largest local variance";

        String[] ALL = new String[]{MAX_STDEV};

    }

    public interface ChannelModes {
        String USE_ALL = "Use all channels";
        String USE_SINGLE = "Use single channel";

        String[] ALL = new String[]{USE_ALL, USE_SINGLE};

    }

//    public interface Measurements {
//        String MAX_MEAN_VARIANCE = "BEST_FOCUS // MAX_MEAN_VARIANCE";
//        String MAX_MEAN_VARIANCE_SLICE = "BEST_FOCUS // MAX_MEAN_VARIANCE_SLICE";
//        String MAX_VARIANCE = "BEST_FOCUS // MAX_VARIANCE";
//        String MAX_VARIANCE_SLICE = "BEST_FOCUS // MAX_VARIANCE_SLICE";
//
//    }


    public static String getFullName(String measurement, int channel) {
        return measurement + "_(CH" + channel + ")";

    }

    static int getMaxStandardDeviationSlice(Image image, int frame, int channel) {
        ImagePlus inputIpl = image.getImagePlus();

        // Setting the channels to measure over.  If channel is -1, use all channels
        int startChannel = 0;
        int endChannel = inputIpl.getNChannels();
        if (channel != -1) startChannel = endChannel = channel;

        // Measuring the statistics for each slice
        int bestSlice = 0;
        double bestStdev = 0;

        for (int c=startChannel;c<=endChannel;c++) {
            for (int z = 0; z < inputIpl.getNSlices(); z++) {
                inputIpl.setPosition(c+1,z+1,frame+1);
                double stdev = inputIpl.getProcessor().getStatistics().stdDev;

                if (stdev > bestStdev) {
                    bestSlice = z;
                    bestStdev = stdev;
                }
            }
        }

        return bestSlice;

    }

    static <T extends RealType<T> & NativeType<T>> ImgPlus<T> getEmptyImage(ImgPlus<T> inputImg, int relativeStart, int relativeEnd) {
        // Determining the number of slices
        int nSlices = Math.max(relativeStart,relativeEnd) - Math.min(relativeStart,relativeEnd) + 1;

        long[] dims = ImgPlusTools.getDimensionsXYCZT(inputImg);
        dims[3] = nSlices;

        // Creating the output image and copying over the pixel coordinates
        ArrayImgFactory<T> factory = new ArrayImgFactory<T>((T) inputImg.firstElement());
        ImgPlus<T> imgOut = new ImgPlus<T>(factory.create(dims));
        ImgPlusTools.applyCalibrationXYCZT(inputImg,imgOut);

        return imgOut;

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

        Cursor<T> cursorIn = Views.offsetInterval(inputImg, offsetIn, dimsIn).cursor();
        Cursor<T> cursorOut = Views.offsetInterval(outputImg, offsetOut, dimsOut).cursor();

        while (cursorIn.hasNext()) cursorOut.next().set(cursorIn.next());

    }


    @Override
    public String getTitle() {
        return "Best focus stack";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Extract a Z-substack from an input stack based on an automatically-calculated best-focus slice.  " +
                "Best focus is determined using the local 2D variance of pixels in each slice.  It is possible to " +
                "extract a fixed number of slices above and below the determined best-focus slice.";
    }

    @Override
    protected boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        ImgPlus<T> inputImg = inputImage.getImgPlus();

        // Getting other parameters
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String bestFocusCalculation = parameters.getValue(BEST_FOCUS_CALCULATION);
//        double radius = parameters.getValue(RADIUS);
//        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        int relativeStart = parameters.getValue(RELATIVE_START_SLICE);
        int relativeEnd = parameters.getValue(RELATIVE_END_SLICE);
        String channelMode = parameters.getValue(CHANNEL_MODE);
        int channel = parameters.getValue(CHANNEL);

        // Making sure the start and end are the right way round
        if (relativeStart > relativeEnd) {
            int a = relativeStart;
            relativeStart = relativeEnd;
            relativeEnd = a;
        }

        // Setting the channel number to zero-indexed or -1 if using all channels
        channel = channel-1;
        if (channelMode.equals(ChannelModes.USE_ALL)) channel = -1;

        // Creating the empty container image
        ImgPlus<T> imgOut = getEmptyImage(inputImg,relativeStart,relativeEnd);

        // Iterating over frame, extracting the relevant substack, then appending it to the output
        long nFrames = inputImg.dimension(inputImg.dimensionIndex(Axes.TIME));
        for (int f=0;f<nFrames;f++) {
            writeMessage("Processing frame "+(f+1)+" of "+nFrames);
            // Determining the best slice
            int bestSlice = 0;
            switch (bestFocusCalculation) {
                case BestFocusCalculations.MAX_STDEV:
                    bestSlice = getMaxStandardDeviationSlice(inputImage,f,channel);
                    break;
            }

            writeMessage("Best focus (frame "+(f+1)+") at "+(bestSlice+1));

            // Extracting the best-slice substack and adding it to the outputImage
            extractSubstack(inputImg,imgOut,bestSlice+relativeStart,bestSlice+relativeEnd,f);

        }

        // Applying input calibration and if a single slice, setting Z-calibration to 1
        ImgPlusTools.applyCalibrationXYCZT(inputImg,imgOut);
        if (imgOut.dimension(imgOut.dimensionIndex(Axes.Z)) == 1) {
            imgOut.setAxis(new DefaultLinearAxis(Axes.Z, 1.0D),imgOut.dimensionIndex(Axes.Z));
        }

        // Adding the new image to the Workspace
        Image outputImage = new Image<T>(outputImageName,imgOut);
        workspace.addImage(outputImage);

        if (showOutput) outputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this,"","Image to extract substack from."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,"","Substack image to be added to the current workspace."));
        parameters.add(new ChoiceP(BEST_FOCUS_CALCULATION,this,BestFocusCalculations.MAX_STDEV,BestFocusCalculations.ALL,"Method for determining the best-focus slice.  \""+BestFocusCalculations.MAX_STDEV+"\" calculates the standard deviation of each slice."));//"Method for determining the best-focus slice.  \""+BestFocusCalculations.MAX_MEAN_VARIANCE+"\" calculates the mean variance of each slice, then takes the slice with the largest mean.  \""+BestFocusCalculations.MAX_VARIANCE+"\" simply takes the slice with the largest variance."));
//        parameters.add(new DoubleP(RADIUS,this,1d,"Radius of filter for determining best focus stack.  If \"Calibrated units\" is false, this value is in pixel units, but if true this value is in calibrated units."));
//        parameters.add(new BooleanP(CALIBRATED_UNITS,this,false,"Controls if the radius is specified in pixel (false) or calibrated (true) units."));
        parameters.add(new IntegerP(RELATIVE_START_SLICE,this,0,"Index of start slice relative to determined best-focus slice (i.e. -5 is 5 slices below the best-focus)."));
        parameters.add(new IntegerP(RELATIVE_END_SLICE,this,0,"Index of end slice relative to determined best-focus slice (i.e. 5 is 5 slices above the best-focus)."));
        parameters.add(new ChoiceP(CHANNEL_MODE,this,ChannelModes.USE_SINGLE,ChannelModes.ALL,"How many channels to use when calculating the best-focus slice.  \""+ChannelModes.USE_ALL+"\" will use all channels, whereas \""+ChannelModes.USE_SINGLE+"\" will base the calculation on a single, user-defined channel."));
        parameters.add(new IntegerP(CHANNEL,this,1,"Channel to base the best-focus calculation on."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(BEST_FOCUS_CALCULATION));
//        returnedParameters.add(parameters.getParameter(RADIUS));
//        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(RELATIVE_START_SLICE));
        returnedParameters.add(parameters.getParameter(RELATIVE_END_SLICE));

        returnedParameters.add(parameters.getParameter(CHANNEL_MODE));
        switch ((String) parameters.getValue(CHANNEL_MODE)) {
            case ChannelModes.USE_SINGLE:
                returnedParameters.add(parameters.getParameter(CHANNEL));
                break;
        }

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
//        imageMeasurementRefs.setAllCalculated(false);
//
//        String inputImageName = parameters.getValue(INPUT_IMAGE);
//
//        MeasurementRef measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_MEAN_VARIANCE_SLICE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        measurementRef = new MeasurementRef(Measurements.MAX_VARIANCE_SLICE);
//        measurementRef.setCalculated(true);
//        measurementRef.setImageObjName(inputImageName);
//        imageMeasurementRefs.add(measurementRef);
//
//        return imageMeasurementRefs;

        return null;

    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
