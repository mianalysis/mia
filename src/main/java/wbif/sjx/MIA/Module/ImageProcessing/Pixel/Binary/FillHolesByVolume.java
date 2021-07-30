package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Binary;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.Duplicator;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import inra.ijpb.binary.conncomp.FloodFillComponentsLabeling3D;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Core.InputControl;
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
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;
import wbif.sjx.common.Exceptions.LongOverflowException;

public class FillHolesByVolume extends Module {
    public static final String INPUT_SEPARATOR = "Image input, object output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String HOLE_FILLING_SEPARATOR = "Hole filling controls";
    public static final String USE_MINIMUM_VOLUME = "Use minimum volume";
    public static final String MINIMUM_VOLUME = "Minimum size";
    public static final String USE_MAXIMUM_VOLUME = "Use maximum volume";
    public static final String MAXIMUM_VOLUME = "Maximum size";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String CONNECTIVITY = "Connectivity";
    public static final String BINARY_LOGIC = "Binary logic";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";
    public static final String MIN_STRIP_WIDTH = "Minimum strip width (px)";

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[] { SIX, TWENTYSIX };

    }
    
    public interface BinaryLogic extends BinaryLogicInterface {
    }


    public FillHolesByVolume(ModuleCollection modules) {
        super("Fill holes by volume", modules);
    }

    public static void process(ImagePlus ipl, double minVolume, double maxVolume, boolean blackBackground, int connectivity, boolean multithread,
            int minStripWidth) throws LongOverflowException {
        String name = new FillHolesByVolume(null).getName();

        // MorphoLibJ takes objects as being white
        if (blackBackground)
            InvertIntensity.process(ipl);

        int count = 0;
        int total = ipl.getNFrames() * ipl.getNChannels();
        int nSlices = ipl.getNSlices();
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            for (int t = 1; t <= ipl.getNFrames(); t++) {
                // Creating the current sub-stack
                ImagePlus currStack;
                if (ipl.getNFrames() == 1) {
                    currStack = ipl;
                } else {
                    currStack = SubHyperstackMaker.makeSubhyperstack(ipl, c + "-" + c, "1-" + nSlices, t + "-" + t);
                }
                currStack.updateChannelAndDraw();

                // Applying connected components labelling
                int nThreads = multithread ? Prefs.getThreads() : 1;
                if (multithread && nThreads > 1 && minStripWidth < ipl.getWidth()) {
                    currStack.setStack(
                            IdentifyObjects.connectedComponentsLabellingMT(currStack.getStack(), connectivity, minStripWidth));
                } else {
                    try {
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 16);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    } catch (RuntimeException e2) {
                        FloodFillComponentsLabeling3D ffcl3D = new FloodFillComponentsLabeling3D(connectivity, 32);
                        currStack.setStack(ffcl3D.computeLabels(currStack.getStack()));
                    }
                }

                // Counting the number of instances of each label
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
                for (int z = 1; z <= nSlices; z++) {
                    final int finalC = c;
                    final int finalZ = z;
                    final int finalT = t;

                    Runnable task = () -> {
                        int idx = ipl.getStackIndex(finalC, finalZ, finalT);
                        ImageProcessor ipr = ipl.getImageStack().getProcessor(idx);
                        ImageProcessor labelIpr = labelIst.getProcessor(finalZ);

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

                writeProgressStatus(++count, total, "stacks", name);
                
            }
        }

        // Ensuring the output is 8-bit
        if (ipl.getBitDepth() > 8) {
            ImageTypeConverter.process(ipl, 8, ImageTypeConverter.ScalingModes.CLIP);
        }

        // MorphoLibJ takes objects as being white
        if (blackBackground)
            InvertIntensity.process(ipl);

    }


    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
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
        boolean useMinVolume = parameters.getValue(USE_MINIMUM_VOLUME);
        boolean useMaxVolume = parameters.getValue(USE_MAXIMUM_VOLUME);
        double minVolume = parameters.getValue(MINIMUM_VOLUME);
        double maxVolume = parameters.getValue(MAXIMUM_VOLUME);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY));
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);
        int minStripWidth = parameters.getValue(MIN_STRIP_WIDTH);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);

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
            process(inputImagePlus, minVolume, maxVolume, blackBackground, connectivity, multithread, minStripWidth);
        } catch (LongOverflowException e) {
            return Status.FAIL;
        }

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
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(HOLE_FILLING_SEPARATOR, this));
        parameters.add(new BooleanP(USE_MINIMUM_VOLUME, this, true));
        parameters.add(new DoubleP(MINIMUM_VOLUME, this, 0d));
        parameters.add(new BooleanP(USE_MAXIMUM_VOLUME, this, true));
        parameters.add(new DoubleP(MAXIMUM_VOLUME, this, 1000d));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new ChoiceP(CONNECTIVITY, this,Connectivity.TWENTYSIX,Connectivity.ALL));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));
        parameters.add(new IntegerP(MIN_STRIP_WIDTH, this, 60));

        addParameterDescriptions();

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
        returnedParameters.add(parameters.getParameter(USE_MINIMUM_VOLUME));
        if ((boolean) parameters.getValue(USE_MINIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MINIMUM_VOLUME));
        }
        returnedParameters.add(parameters.getParameter(USE_MAXIMUM_VOLUME));
        if ((boolean) parameters.getValue(USE_MAXIMUM_VOLUME)) {
            returnedParameters.add(parameters.getParameter(MAXIMUM_VOLUME));
        }

        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(CONNECTIVITY));
        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

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

    void addParameterDescriptions() {
      parameters.get(INPUT_IMAGE).setDescription(
              "Image from workspace to apply fill holes operation to.  This must be an 8-bit binary image (255 = background, 0 = foreground).");

      parameters.get(APPLY_TO_INPUT).setDescription(
              "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \"" + OUTPUT_IMAGE + "\" parameter.");

      parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
              + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

      parameters.get(USE_MINIMUM_VOLUME).setDescription("");

      parameters.get(MINIMUM_VOLUME).setDescription("");

      parameters.get(USE_MAXIMUM_VOLUME).setDescription("");

      parameters.get(MAXIMUM_VOLUME).setDescription("");

      parameters.get(CALIBRATED_UNITS).setDescription("When selected, hole size limits are assumed to be specified in calibrated units (as defined by the \""+new InputControl(null).getName()+"\" parameter \""+InputControl.SPATIAL_UNIT+"\").  Otherwise, pixel units are assumed.");

      parameters.get(ENABLE_MULTITHREADING).setDescription("Break the image down into strips, each one processed on a separate CPU thread.  The overhead required to do this means it's best for large multi-core CPUs, but should be left disabled for small images or on CPUs with few cores.");

      parameters.get(MIN_STRIP_WIDTH).setDescription("Minimum width of each strip to be processed on a separate CPU thread.  Measured in pixel units.");

    }
}
