package io.github.mianalysis.mia.module.images.process;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.drew.lang.annotations.Nullable;

import java.awt.image.BufferedImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.IntegerP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import mpicbg.ij.clahe.Flat;


/**
* Applies the MPICBG implementation of CLAHE (Contrast Limited Adaptive Histogram Equalization).  This module runs the Image "<a href="https://imagej.net/Enhance_Local_Contrast_(CLAHE)">CLAHE</a>" plugin.
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ApplyCLAHE extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image to apply CLAHE to.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* Select if CLAHE should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* Name of the output image created during the CLAHE process.  This image will be added to the workspace.
	*/
    public static final String OUTPUT_IMAGE = "Output image";


	/**
	* 
	*/
    public static final String CLAHE_SEPARATOR = "CLAHE controls";

	/**
	* "The size of the local region around a pixel for which the histogram is equalized. This size should be larger than the size of features to be preserved".  Description taken from <a href="https://imagej.net/plugins/clahe">https://imagej.net/plugins/clahe</a>
	*/
    public static final String BLOCK_SIZE = "Blocksize";

	/**
	* Choose if block size is specified in pixel (set to "false") or calibrated (set to "true") units.  What units are used are controlled from "Input control".
	*/
    public static final String CALIBRATED_UNITS = "Calibrated units";

	/**
	* "The number of histogram bins used for histogram equalization. The implementation internally works with byte resolution, so values larger than 256 are not meaningful. This value also limits the quantification of the output when processing 8bit gray or 24bit RGB images. The number of histogram bins should be smaller than the number of pixels in a block".  Description taken from <a href="https://imagej.net/plugins/clahe">https://imagej.net/plugins/clahe</a>
	*/
    public static final String HISTOGRAM_BINS = "Histogram bins";

	/**
	* "Limits the contrast stretch in the intensity transfer function. Very large values will let the histogram equalization do whatever it wants to do, that is result in maximal local contrast. The value 1 will result in the original image".  Description taken from <a href="https://imagej.net/plugins/clahe">https://imagej.net/plugins/clahe</a>
	*/
    public static final String MAXIMUM_SLOPE = "Maximum slope";

	/**
	* When selected, only pixels coincident with the white (255 intensity) part of the mask image (specified by the "Mask image" parameter) are processed.  All other pixels will retain their initial value
	*/
    public static final String USE_MASK = "Use mask";

	/**
	* Image to use for masking when "Use mask" is selected.  Only pixels in the input image coincident with white (255 intensity) pixels in this mask will be processed.
	*/
    public static final String MASK_IMAGE = "Mask image";
    public static final String FAST_MODE = "Fast (less accurate)";


	/**
	* 
	*/
    public static final String EXECUTION_SEPARATOR = "Execution controls";

	/**
	* Process multiple slices independently.  This can provide a speed improvement when working on a computer with a multi-core CPU.
	*/
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public ApplyCLAHE(Modules modules) {
        super("Apply CLAHE", modules);
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Applies the MPICBG implementation of CLAHE (Contrast Limited Adaptive Histogram Equalization).  This module runs the Image \"<a href=\"https://imagej.net/Enhance_Local_Contrast_(CLAHE)\">CLAHE</a>\" plugin.";
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGES_PROCESS;
    }

    public void applyCLAHE(ImagePlus inputImagePlus, int blockRadius, int histogramBins, float maxSlope,
            @Nullable ByteProcessor mask, boolean fastMode, boolean multithread) {
        // Setting up multithreading
        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());

        // Applying the macro
        ImageStack ist = inputImagePlus.getStack();
        for (int i = 0; i < ist.size(); i++) {
            ImageProcessor ipr = ist.getProcessor(i + 1);
            ImagePlus ipl = new ImagePlus("Temp", ipr);
            Runnable task = () -> {
                if (fastMode)
                    Flat.getFastInstance().run(ipl, blockRadius, histogramBins, maxSlope, mask, true);
                else
                    Flat.getInstance().run(ipl, blockRadius, histogramBins, maxSlope, mask, true);
            };
            pool.submit(task);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // Do nothing as the user has selected this
        }

        inputImagePlus.updateChannelAndDraw();

    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        double blockSize = parameters.getValue(BLOCK_SIZE, workspace);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS, workspace);
        int histogramBins = parameters.getValue(HISTOGRAM_BINS, workspace);
        float maxSlope = ((Double) parameters.getValue(MAXIMUM_SLOPE, workspace)).floatValue();
        boolean useMask = parameters.getValue(USE_MASK, workspace);
        String maskImageName = parameters.getValue(MASK_IMAGE, workspace);
        boolean fastMode = parameters.getValue(FAST_MODE, workspace);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING, workspace);

        if (calibratedUnits) {
            double dppXY = inputImagePlus.getCalibration().pixelWidth;
            blockSize = blockSize / dppXY;
        }

        int blockRadius = (((int) Math.round(blockSize)) - 1) / 2;
        histogramBins--;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImagePlus = inputImagePlus.duplicate();
        }

        // Getting mask image if necessary
        ByteProcessor mask = null;
        if (useMask) {
            Image maskImage = workspace.getImage(maskImageName);
            mask = (ByteProcessor) maskImage.getImagePlus().getProcessor().convertToByte(true);
        }

        // Running CLAHE
        applyCLAHE(inputImagePlus, blockRadius, histogramBins, maxSlope, mask, fastMode, multithread);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show();
        } else {
            if (showOutput)
                inputImage.show();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CLAHE_SEPARATOR, this));
        parameters.add(new DoubleP(BLOCK_SIZE, this, 127));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false));
        parameters.add(new IntegerP(HISTOGRAM_BINS, this, 256));
        parameters.add(new DoubleP(MAXIMUM_SLOPE, this, 3d));
        parameters.add(new BooleanP(USE_MASK, this, false));
        parameters.add(new InputImageP(MASK_IMAGE, this));
        parameters.add(new BooleanP(FAST_MODE, this, true));

        parameters.add(new SeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CLAHE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(BLOCK_SIZE));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(HISTOGRAM_BINS));
        returnedParameters.add(parameters.getParameter(MAXIMUM_SLOPE));
        returnedParameters.add(parameters.getParameter(USE_MASK));
        if ((boolean) parameters.getValue(USE_MASK, workspace)) {
            returnedParameters.add(parameters.getParameter(MASK_IMAGE));
        }
        returnedParameters.add(parameters.getParameter(FAST_MODE));

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

    void addParameterDescriptions() {
        String siteRef = "Description taken from <a href=\"https://imagej.net/plugins/clahe\">https://imagej.net/plugins/clahe</a>";

        parameters.get(INPUT_IMAGE).setDescription("Image to apply CLAHE to.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "Select if CLAHE should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace.");

        parameters.get(OUTPUT_IMAGE).setDescription(
                "Name of the output image created during the CLAHE process.  This image will be added to the workspace.");

        parameters.get(BLOCK_SIZE).setDescription(
                "\"The size of the local region around a pixel for which the histogram is equalized. This size should be larger than the size of features to be preserved\".  "
                        + siteRef);

        parameters.get(CALIBRATED_UNITS).setDescription(
                "Choose if block size is specified in pixel (set to \"false\") or calibrated (set to \"true\") units.  What units are used are controlled from \"Input control\".");

        parameters.get(HISTOGRAM_BINS).setDescription(
                "\"The number of histogram bins used for histogram equalization. The implementation internally works with byte resolution, so values larger than 256 are not meaningful. This value also limits the quantification of the output when processing 8bit gray or 24bit RGB images. The number of histogram bins should be smaller than the number of pixels in a block\".  "
                        + siteRef);

        parameters.get(MAXIMUM_SLOPE).setDescription(
                "\"Limits the contrast stretch in the intensity transfer function. Very large values will let the histogram equalization do whatever it wants to do, that is result in maximal local contrast. The value 1 will result in the original image\".  "
                        + siteRef);

        parameters.get(USE_MASK).setDescription(
                "When selected, only pixels coincident with the white (255 intensity) part of the mask image (specified by the \""
                        + MASK_IMAGE
                        + "\" parameter) are processed.  All other pixels will retain their initial value");

        parameters.get(MASK_IMAGE).setDescription("Image to use for masking when \"" + USE_MASK
                + "\" is selected.  Only pixels in the input image coincident with white (255 intensity) pixels in this mask will be processed.");

        parameters.get(FAST_MODE).setDescription(
                "\"Use the fast but less accurate version of the filter. The fast version does not evaluate the intensity transfer function for each pixel independently but for a grid of adjacent boxes of the given block size only and interpolates for locations in between\".  "
                        + siteRef);

        parameters.get(ENABLE_MULTITHREADING).setDescription(
                "Process multiple slices independently.  This can provide a speed improvement when working on a computer with a multi-core CPU.");

    }
}
