package io.github.mianalysis.MIA.Module.ImageProcessing.Pixel.Binary;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sisu.Nullable;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.StackProcessor;
import inra.ijpb.morphology.MinimaAndMaxima3D;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceP;
import io.github.mianalysis.MIA.Object.Parameters.InputImageP;
import io.github.mianalysis.MIA.Object.Parameters.OutputImageP;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ChoiceInterfaces.BinaryLogicInterface;
import io.github.mianalysis.MIA.Object.Parameters.Text.IntegerP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

/**
 * Created by sc13967 on 07/03/2018.
 */
public class ExtendedMinima extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String EXTENDED_MINIMA_SEPARATOR = "Extended minima controls";
    public static final String MINIMA_MAXIMA_MODE = "Minima/maxima";
    public static final String DYNAMIC = "Dynamic";
    public static final String CONNECTIVITY_3D = "Connectivity (3D)";
    public static final String BINARY_LOGIC = "Binary logic";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public ExtendedMinima(Modules modules) {
        super("Extended minima/maxima", modules);
    }

    public interface MinimaMaximaModes {
        String MAXIMA = "Maxima";
        String MINIMA = "Minima";

        String[] ALL = new String[] { MAXIMA, MINIMA };

    }

    public interface Connectivity {
        String SIX = "6";
        String TWENTYSIX = "26";

        String[] ALL = new String[] { SIX, TWENTYSIX };

    }

    public interface BinaryLogic extends BinaryLogicInterface {
    }

    public static Image process(Image inputImage, String outputImageName, String minimaMaxima, boolean blackBackground,
            int dynamic, int connectivity, boolean multithread) throws InterruptedException {
        ImagePlus inputIpl = inputImage.getImagePlus();
        int width = inputIpl.getWidth();
        int height = inputIpl.getHeight();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        String moduleName = new ExtendedMinima(null).getName();

        // Creating output image
        ImagePlus outputIpl = IJ.createHyperStack(outputImageName, width, height, nChannels, nSlices, nFrames, 8);

        // Setting the calibration from the input image
        outputIpl.setCalibration(inputImage.getImagePlus().getCalibration());

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        int nTotal = nChannels * nFrames;
        AtomicInteger count = new AtomicInteger();

        for (int c = 1; c <= nChannels; c++) {
            for (int t = 1; t <= nFrames; t++) {
                int finalT = t;
                int finalC = c;

                Runnable task = () -> {
                    // Getting maskIpl for this timepoint
                    ImageStack timepoint = getSetStack(inputIpl, finalT, finalC, null);

                    if (timepoint == null)
                        return;

                    switch (minimaMaxima) {
                        case MinimaMaximaModes.MAXIMA:
                            timepoint = MinimaAndMaxima3D.extendedMaxima(timepoint, dynamic, connectivity);
                            break;
                        case MinimaMaximaModes.MINIMA:
                            timepoint = MinimaAndMaxima3D.extendedMinima(timepoint, dynamic, connectivity);
                            break;
                    }

                    // Setting the binary logic of the output extended minima
                    if (blackBackground)
                        new StackProcessor(timepoint).invert();

                    // Replacing the maskIpl intensity
                    getSetStack(outputIpl, finalT, finalC, timepoint);

                    writeProgressStatus(count.incrementAndGet(), nTotal, "stacks", moduleName);

                };
                pool.submit(task);
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

        // MorphoLibJ gives white objects on a black background. Inverting this to match
        // the logic of ImageJ
        IJ.run(outputIpl, "Invert", "stack");

        return new Image(outputImageName, outputIpl);

    }

    synchronized private static ImageStack getSetStack(ImagePlus inputImagePlus, int timepoint, int channel,
            @Nullable ImageStack toPut) {
        int nSlices = inputImagePlus.getNSlices();
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, "1-" + nSlices,
                    timepoint + "-" + timepoint).getStack();
        } else {
            for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
                inputImagePlus.setPosition(channel, z, timepoint);
                inputImagePlus.setProcessor(toPut.getProcessor(z));
            }

            inputImagePlus.updateAndDraw();

            return null;
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL_BINARY;
    }

    @Override
    public String getDescription() {
        return "Detects extended minima or maxima in a specified input image.<br><br>As described in the <a href=\"https://imagej.net/imagej-wiki-static/MorphoLibJ\">MorphoLibJ documentation</a>: \"Extended maxima are defined as a connected region containing elements such that the difference of the value of each element within the region with the maximal value within the region is lower than the tolerance, and such that the neighbors of the regions all have values smaller than the maximum within the region minus the tolerance. This definition allows the identification of larger extrema, that better takes into account the noise within the image. The extended minima are defined in a similar way, and are efficiently used as pre-processing step for watershed segmentation.\".<br><br>This module uses the plugin \"<a href=\"https://github.com/ijpb/MorphoLibJ\">MorphoLibJ</a>\".";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String minimaMaximaMode = parameters.getValue(MINIMA_MAXIMA_MODE);
        int dynamic = parameters.getValue(DYNAMIC);
        int connectivity = Integer.parseInt(parameters.getValue(CONNECTIVITY_3D));
        String binaryLogic = parameters.getValue(BINARY_LOGIC);
        boolean blackBackground = binaryLogic.equals(BinaryLogic.BLACK_BACKGROUND);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        // Getting region minima
        Image outputImage;
        try {
            outputImage = process(inputImage, outputImageName, minimaMaximaMode, blackBackground, dynamic, connectivity,
                    multithread);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Status.FAIL;
        }

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            workspace.addImage(outputImage);
        } else {
            inputImage.setImagePlus(outputImage.getImagePlus());
        }

        if (showOutput)
            outputImage.showImage();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(EXTENDED_MINIMA_SEPARATOR, this));
        parameters.add(new ChoiceP(MINIMA_MAXIMA_MODE, this, MinimaMaximaModes.MINIMA, MinimaMaximaModes.ALL));
        parameters.add(new IntegerP(DYNAMIC, this, 1));
        parameters.add(new ChoiceP(CONNECTIVITY_3D, this, Connectivity.TWENTYSIX, Connectivity.ALL));
        parameters.add(new ChoiceP(BINARY_LOGIC, this, BinaryLogic.BLACK_BACKGROUND, BinaryLogic.ALL));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(EXTENDED_MINIMA_SEPARATOR));
        returnedParameters.add(parameters.getParameter(MINIMA_MAXIMA_MODE));
        returnedParameters.add(parameters.getParameter(DYNAMIC));
        returnedParameters.add(parameters.getParameter(CONNECTIVITY_3D));
        returnedParameters.add(parameters.getParameter(BINARY_LOGIC));

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

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE).setDescription("Image from workspace to apply extended minima operation to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.  This image will be 8-bit with binary logic determined by the \"" + BINARY_LOGIC + "\" parameter.");

        parameters.get(MINIMA_MAXIMA_MODE).setDescription(
                "Controls whether the module will detect minima or maxima in the input intensity image");

        parameters.get(DYNAMIC).setDescription(
                "This parameter specifies the maximum permitted pixel intensity difference for a single minima.  Local intensity differences greater than this will result in creation of more minima.  The smaller the dynamic value is, the more minima will be created.  As the dynamic value increases, minima will increase in size.");

        parameters.get(CONNECTIVITY_3D).setDescription("Controls which adjacent pixels are considered:<br><ul>"

                + "<li>\"" + Connectivity.SIX
                + "\" Only pixels immediately next to the active pixel are considered.  These are the pixels on the four \"cardinal\" directions plus the pixels immediately above and below the current pixel.  If working in 2D, 4-way connectivity is used.</li>"

                + "<li>\"" + Connectivity.TWENTYSIX
                + "\" In addition to the core 6-pixels, all immediately diagonal pixels are used.  If working in 2D, 8-way connectivity is used.</li>");

        parameters.get(BINARY_LOGIC).setDescription(BinaryLogicInterface.getDescription());

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple 3D stacks simultaneously.  Since the extended minima operation is applied on a single 3D stack at a time, multithreading only works for images with multiple channels or timepoints (other stacks will still work, but won't see a speed improvement).  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}