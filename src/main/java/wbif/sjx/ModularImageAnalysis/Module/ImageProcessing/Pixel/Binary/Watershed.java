package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.Binary;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

import javax.annotation.Nullable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Watershed extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String USE_MARKERS = "Use markers";
    public static final String MARKER_IMAGE = "Input marker image";
    public static final String INTENSITY_MODE = "Intensity mode";
    public static final String INTENSITY_IMAGE = "Intensity image";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String MATCH_Z_TO_X= "Match Z to XY";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public interface IntensityModes {
        String DISTANCE = "Distance";
        String INPUT_IMAGE = "Input image intensity";

        String[] ALL = new String[]{DISTANCE,INPUT_IMAGE};

    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[]{SIX,TWENTYSIX};

    }

    public void process(ImagePlus intensityIpl, ImagePlus markerIpl, ImagePlus maskIpl, int dynamic, int connectivity, boolean multithread) throws InterruptedException {
        // Expected inputs for binary images (marker and mask) are black objects on a white background.  These need to
        // be inverted before using as MorphoLibJ uses the opposite convention.
        IJ.run(maskIpl,"Invert","stack");
        if (markerIpl != null) IJ.run(markerIpl, "Invert", "stack");

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        int nChannels = maskIpl.getNChannels();
        int nFrames = maskIpl.getNFrames();
        int nTotal = nChannels*nFrames;
        AtomicInteger count = new AtomicInteger();

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                int finalT = t;
                int finalC = c;

                Runnable task = () -> {
                    // Getting maskIpl for this timepoint
                    ImageStack timepointMask = getSetStack(maskIpl, finalT, finalC, null);
                    ImageStack timepointIntensity = getSetStack(intensityIpl, finalT, finalC, null);

                    if (markerIpl == null) {
                        timepointMask = ExtendedMinimaWatershed.extendedMinimaWatershed(timepointIntensity, timepointMask, dynamic, connectivity, false);
                    } else {
                        ImageStack timepointMarker = getSetStack(markerIpl, finalT, finalC, null);
                        timepointMarker = BinaryImages.componentsLabeling(timepointMarker, connectivity, 32);
                        timepointMask = inra.ijpb.watershed.Watershed.computeWatershed(timepointIntensity, timepointMarker, timepointMask, connectivity, true, false);
                    }

                    // The image produced by MorphoLibJ's watershed function is labelled.  Converting to binary and back to 8-bit.
                    ImagePlus timepointMaskIpl = new ImagePlus("Timepoint mask",timepointMask);
                    IJ.setRawThreshold(timepointMaskIpl, 0, 0, null);
                    IJ.run(timepointMaskIpl, "Convert to Mask", "method=Default background=Light");
                    IJ.run(timepointMaskIpl, "Invert LUT", "");
                    IJ.run(timepointMaskIpl, "8-bit", null);

                    //  Replacing the maskIpl intensity
                    getSetStack(maskIpl, finalT, finalC, timepointMaskIpl.getStack());
                    writeMessage("Processed " + (count.incrementAndGet()) + " of " + nTotal + " stacks");

                };
                pool.submit(task);
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    synchronized private static ImageStack getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, @Nullable ImageStack toPut) {
        int nSlices = inputImagePlus.getNSlices();
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, "1-" + nSlices, timepoint + "-" + timepoint).getStack();
        } else {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                inputImagePlus.setPosition(channel,z,timepoint);
                inputImagePlus.setProcessor(toPut.getProcessor(z));
            }
            return null;
        }
    }


    @Override
    public String getTitle() {
        return "Watershed transform";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getHelp() {
        return "Uses MorphoLibJ implementation of watershed transform.";
    }

    @Override
    protected boolean run(Workspace workspace) {
// Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        boolean useMarkers = parameters.getValue(USE_MARKERS);
        String markerImageName = parameters.getValue(MARKER_IMAGE);
        String intensityMode = parameters.getValue(INTENSITY_MODE);
        String intensityImageName = parameters.getValue(INTENSITY_IMAGE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY));
        boolean matchZToXY = parameters.getValue(MATCH_Z_TO_X);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        ImagePlus markerIpl = null;
        if (useMarkers) markerIpl = workspace.getImage(markerImageName).getImagePlus();

        ImagePlus intensityIpl = null;
        switch (intensityMode) {
            case IntensityModes.DISTANCE:
                intensityIpl = new Duplicator().run(inputImagePlus);
                intensityIpl = DistanceMap.getDistanceMap(intensityIpl,matchZToXY);
                IJ.run(intensityIpl,"Invert","stack");
                break;

            case IntensityModes.INPUT_IMAGE:
                intensityIpl = workspace.getImage(intensityImageName).getImagePlus();
                break;

        }

        try {
            process(intensityIpl,markerIpl,inputImagePlus,dynamic,connectivity,multithread);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) showImage(outputImage);

        } else {
            if (showOutput) showImage(inputImage);

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new BooleanP(USE_MARKERS, this,false));
        parameters.add(new InputImageP(MARKER_IMAGE, this));
        parameters.add(new ChoiceP(INTENSITY_MODE, this,IntensityModes.DISTANCE,IntensityModes.ALL));
        parameters.add(new InputImageP(INTENSITY_IMAGE, this));
        parameters.add(new IntegerP(DYNAMIC, this,1));
        parameters.add(new ChoiceP(CONNECTIVITY, this,Connectivity.TWENTYSIX,Connectivity.ALL));
        parameters.add(new BooleanP(MATCH_Z_TO_X, this, true));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(USE_MARKERS));
        if (parameters.getValue(USE_MARKERS)) {
            returnedParameters.add(parameters.getParameter(MARKER_IMAGE));
        } else {
            returnedParameters.add(parameters.getParameter(DYNAMIC));
        }

        returnedParameters.add(parameters.getParameter(INTENSITY_MODE));
        switch ((String) parameters.getValue(INTENSITY_MODE)) {
            case IntensityModes.DISTANCE:
                returnedParameters.add(parameters.getParameter(MATCH_Z_TO_X));
                break;

            case IntensityModes.INPUT_IMAGE:
                returnedParameters.add(parameters.getParameter(INTENSITY_IMAGE));
                break;
        }
        returnedParameters.add(parameters.getParameter(CONNECTIVITY));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
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
