package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mpicbg.ij.clahe.Flat;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class ApplyCLAHE extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CLAHE_SEPARATOR = "CLAHE controls";
    public static final String BLOCK_SIZE = "Blocksize";
    public static final String CALIBRATED_UNITS = "Calibrated units";
    public static final String HISTOGRAM_BINS = "Histogram bins";
    public static final String MAXIMUM_SLOPE = "Maximum slope";
    public static final String USE_MASK = "Use mask";
    public static final String MASK_IMAGE = "Mask image";
    public static final String FAST_MODE = "Fast (less accurate)";

    public static final String EXECUTION_SEPARATOR = "Execution controls";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";

    public ApplyCLAHE(ModuleCollection modules) {
        super("Apply CLAHE", modules);
    }

    @Override
    public String getDescription() {
        return "Applies the MPICBG implementation of CLAHE (Contrast Limited Adaptive Histogram Equalization).  This module runs the Image \"<a href=\"https://imagej.net/Enhance_Local_Contrast_(CLAHE)\">CLAHE</a>\" plugin.";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    public void applyCLAHE(ImagePlus inputImagePlus, int blockRadius, int histogramBins, float maxSlope, @Nullable ByteProcessor mask, boolean fastMode, boolean multithread) {
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
            e.printStackTrace();
        }

        inputImagePlus.updateChannelAndDraw();

    }

    @Override
    protected Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        double blockSize = parameters.getValue(BLOCK_SIZE);
        boolean calibratedUnits = parameters.getValue(CALIBRATED_UNITS);
        int histogramBins = parameters.getValue(HISTOGRAM_BINS);
        float maxSlope = ((Double) parameters.getValue(MAXIMUM_SLOPE)).floatValue();
        boolean useMask = parameters.getValue(USE_MASK);
        String maskImageName = parameters.getValue(MASK_IMAGE);
        boolean fastMode = parameters.getValue(FAST_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

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
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply CLAHE to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true,
                "Select if CLAHE should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "",
                "Name of the output image created during the CLAHE process.  This image will be added to the workspace."));

        parameters.add(new ParamSeparatorP(CLAHE_SEPARATOR, this));
        parameters.add(new DoubleP(BLOCK_SIZE, this, 127));
        parameters.add(new BooleanP(CALIBRATED_UNITS, this, false,
                "Choose if block size is specified in pixel (set to \"false\") or calibrated (set to \"true\") units.  What units are used are controlled from \"Input control\"."));
        parameters.add(new IntegerP(HISTOGRAM_BINS, this, 256));
        parameters.add(new DoubleP(MAXIMUM_SLOPE, this, 3d));
        parameters.add(new BooleanP(USE_MASK, this, false));
        parameters.add(new InputImageP(MASK_IMAGE, this));
        parameters.add(new BooleanP(FAST_MODE, this, true));

        parameters.add(new ParamSeparatorP(EXECUTION_SEPARATOR, this));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING, this, true));

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

        returnedParameters.add(parameters.getParameter(CLAHE_SEPARATOR));
        returnedParameters.add(parameters.getParameter(BLOCK_SIZE));
        returnedParameters.add(parameters.getParameter(CALIBRATED_UNITS));
        returnedParameters.add(parameters.getParameter(HISTOGRAM_BINS));
        returnedParameters.add(parameters.getParameter(MAXIMUM_SLOPE));
        returnedParameters.add(parameters.getParameter(USE_MASK));
        if ((boolean) parameters.getValue(USE_MASK)) {
            returnedParameters.add(parameters.getParameter(MASK_IMAGE));
        }
        returnedParameters.add(parameters.getParameter(FAST_MODE));

        returnedParameters.add(parameters.getParameter(EXECUTION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(ENABLE_MULTITHREADING));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}