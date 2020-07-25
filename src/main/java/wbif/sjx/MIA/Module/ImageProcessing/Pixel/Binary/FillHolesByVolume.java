package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Module.ImageProcessing.Stack.ImageTypeConverter;
import wbif.sjx.MIA.Module.ObjectProcessing.Identification.IdentifyObjects;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.PartnerRefCollection;
import wbif.sjx.common.Exceptions.LongOverflowException;

public class FillHolesByVolume extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String HOLE_FILLING_SEPARATOR = "Hole filling controls";
    public static final String BLACK_WHITE_MODE = "Black-white mode";
    public static final String USE_MINIMUM_VOLUME = "Use minimum volume";
    public static final String MINIMUM_VOLUME = "Minimum size";
    public static final String USE_MAXIMUM_VOLUME = "Use maximum volume";
    public static final String MAXIMUM_VOLUME = "Maximum size";
    public static final String CALIBRATED_UNITS = "Calibrated units";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String MIN_STRIP_WIDTH = "Minimum strip width (px)";

    public interface BlackWhiteModes {
        String FILL_BLACK_HOLES = "Fill black holes";
        String FILL_WHITE_HOLES = "Fill white holes";

        String[] ALL = new String[] { FILL_BLACK_HOLES, FILL_WHITE_HOLES };

    }

    public FillHolesByVolume(ModuleCollection modules) {
        super("Fill holes by volume", modules);
    }

    public static void process(ImagePlus ipl, double minVolume, double maxVolume, boolean multithread,
            int minStripWidth) throws LongOverflowException {
        String name = new FillHolesByVolume(null).getName();

        int count = 0;
        int total = ipl.getNFrames() * ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            for (int t = 1; t <= ipl.getNFrames(); t++) {
                writeStatus("Processing stack " + (++count) + " of " + total, name);

                // Creating the current sub-stack
                // MIA.log.writeDebug("Getting substack");
                ImagePlus currStack;
                if (ipl.getNFrames() == 1) {
                    currStack = ipl;
                } else {
                    currStack = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                }
                currStack.updateChannelAndDraw();

                // Applying connected components labelling
                // MIA.log.writeDebug("Applying labelling");
                int nThreads = multithread ? Prefs.getThreads() : 1;
                // MIA.log.writeDebug("N threads = " + Prefs.getThreads());
                if (multithread && nThreads > 1 && minStripWidth < ipl.getWidth()) {
                    // MIA.log.writeDebug("Using MT connected components labeling");
                    currStack.setStack(IdentifyObjects.connectedComponentsLabellingMT(currStack.getStack(), 26, minStripWidth));
                } else {
                    // MIA.log.writeDebug("Using non-MT connected components labeling");
                    try {
                        // MIA.log.writeDebug("Using 16-bit labeling");
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26, 16);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    } catch (RuntimeException e2) {
                        // MIA.log.writeDebug("Using 32-bit labeling");
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(26, 32);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    }
                }

                // Counting the number of instances of each label
                // MIA.log.writeDebug("Counting label instances");
                ImageStack labelIst = currStack.getImageStack();

                ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>());

                // Storing on a slice-by-slice basis
                ConcurrentHashMap<Integer, Long> labels = new ConcurrentHashMap<>();
                for (int z = 0; z < labelIst.getSize(); z++) {
                    final int finalZ = z;
                    Runnable task = () -> {
                        HashMap<Integer, Long> currLabels = new HashMap<>();
                        final ImageProcessor labelIpr = labelIst.getProcessor(finalZ + 1);
                        for (int x = 0; x < labelIst.getWidth(); x++) {
                            for (int y = 0; y < labelIst.getHeight(); y++) {
                                int label = labelIpr.get(x, y);
                                currLabels.putIfAbsent(label, 0l);
                                currLabels.put(label, currLabels.get(label) + 1);

                            }
                        }

                        // Adding current labels to the global collection
                        for (int currLabel : currLabels.keySet()) {
                            labels.putIfAbsent(currLabel, 0l);
                            labels.put(currLabel, labels.get(currLabel) + currLabels.get(currLabel));
                        }
                    };
                    pool.submit(task);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }

                // Removing pixels with counts outside the limits
                // MIA.log.writeDebug("Identifying holes for removal");
                Iterator<Integer> iterator = labels.keySet().iterator();
                while (iterator.hasNext()) {
                    int label = iterator.next();
                    long nPixels = labels.get(label);
                    if (nPixels >= minVolume && nPixels <= maxVolume)
                        iterator.remove();

                }

                pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>());

                // Binarising the input image based on whether the label is still in the list
                // MIA.log.writeDebug("Removing holes");
                for (int z = 0; z < nSlices; z++) {
                    final int finalC = c;
                    final int finalZ = z;
                    final int finalT = t;
                    Runnable task = () -> {
                        ipl.setPosition(finalC, finalZ + 1, finalT);
                        final ImageProcessor ipr = ipl.getProcessor();
                        // final ImageProcessor ipr = ipl.getImageStack().getProcessor(finalZ + 1);
                        final ImageProcessor labelIpr = labelIst.getProcessor(finalZ + 1);
                        for (int x = 0; x < labelIst.getWidth(); x++) {
                            for (int y = 0; y < labelIst.getHeight(); y++) {
                                int label = labelIpr.get(x, y);
                                // int label = (int) labelIst.getVoxel(x, y, finalZ);
                                if (labels.containsKey(label))
                                    ipr.set(x, y, 0);
                                else if (label > 0)
                                    ipr.set(x, y, 255);
                            }
                        }
                    };
                    pool.submit(task);
                }

                pool.shutdown();
                try {
                    pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                // MIA.log.writeDebug("Hole removal complete");
            }
        }

        // Ensuring the output is 8-bit
        if (ipl.getBitDepth() > 8) {
            // MIA.log.writeDebug("Converting back to 8-bit");
            ImageTypeConverter.applyConversion(ipl, 8, ImageTypeConverter.ScalingModes.CLIP);
        }
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String blackWhiteMode = parameters.getValue(BLACK_WHITE_MODE);
        boolean useMinVolume = parameters.getValue(USE_MINIMUM_VOLUME);
        boolean useMaxVolume = parameters.getValue(USE_MAXIMUM_VOLUME);
        double minVolume = parameters.getValue(MINIMUM_VOLUME);
        double maxVolume = parameters.getValue(MAXIMUM_VOLUME);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        int minStripWidth = parameters.getValue(MIN_STRIP_WIDTH);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

        // If filling black holes, need to invert image
        if (blackWhiteMode.equals(BlackWhiteModes.FILL_BLACK_HOLES))
            InvertIntensity.process(inputImagePlus);

        if (!useMinVolume)
            minVolume = -Float.MAX_VALUE;
        if (!useMaxVolume)
            maxVolume = Float.MAX_VALUE;

        // If the units are calibrated, converting them to pixels
        if (calibratedUnits) {
            double dppXY = inputImagePlus.getCalibration().pixelWidth;
            double dppZ = inputImagePlus.getCalibration().pixelDepth;

            minVolume = minVolume / (dppXY * dppXY * dppZ);
            maxVolume = maxVolume / (dppXY * dppXY * dppZ);

        }

        try {
            process(inputImagePlus, minVolume, maxVolume, multithread, minStripWidth);

        } catch (LongOverflowException e) {
            return Status.FAIL;
        }

        // If filling black holes, need to invert image back to original
        if (blackWhiteMode.equals(BlackWhiteModes.FILL_BLACK_HOLES))
            InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            Image outputImage = new Image(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();

        } else {
            if (showOutput)
                inputImage.showImage();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new ParamSeparatorP(HOLE_FILLING_SEPARATOR, this));
        parameters.add(new ChoiceP(BLACK_WHITE_MODE, this, BlackWhiteModes.FILL_WHITE_HOLES, BlackWhiteModes.ALL));
        parameters.add(new BooleanP(USE_MINIMUM_VOLUME, this, true));
        parameters.add(new DoubleP(MINIMUM_VOLUME, this, 0d));
        parameters.add(new BooleanP(USE_MAXIMUM_VOLUME, this, true));
        parameters.add(new DoubleP(MAXIMUM_VOLUME, this, 1000d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true,
                "Break the image down into strips, each one processed on a separate CPU thread.  The overhead required to do this means it's best for large multi-core CPUs, but should be left disabled for small images or on CPUs with few cores."));
        parameters.add(new IntegerP(MIN_STRIP_WIDTH, this, 60,
                "Minimum width of each strip to be processed on a separate CPU thread.  Measured in pixel units."));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(HOLE_FILLING_SEPARATOR));
        returnedParameters.add(parameters.getParameter(BLACK_WHITE_MODE));
        returnedParameters.add(parameters.getParameter(USE_MINIMUM_VOLUME));
        if ((boolean) parameters.getValue(USE_MINIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MINIMUM_VOLUME));
        }
        returnedParameters.add(parameters.getParameter(USE_MAXIMUM_VOLUME));
        if ((boolean) parameters.getValue(USE_MAXIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_VOLUME));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));
        if ((boolean) parameters.getValue(ENABLE_MULTITHREADING)) {
            returnedParameters.add(parameters.get(MIN_STRIP_WIDTH));
        }

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
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
