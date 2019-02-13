package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import com.drew.lang.annotations.Nullable;
import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.SubHyperstackMaker;
import ij.process.ImageProcessor;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UnwarpImages extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String ROLLING_CORRECTION = "Rolling correction";
    public static final String CORRECTION_INTERVAL = "Correction interval";
    public static final String REFERENCE_IMAGE = "Reference image";
    public static final String CALCULATION_SOURCE = "Calculation source";
    public static final String EXTERNAL_SOURCE = "External source";
    public static final String CALCULATION_CHANNEL = "Calculation channel";
    public static final String REGISTRATION_MODE = "Registration mode";
    public static final String SUBSAMPLE_FACTOR = "Subsample factor";
    public static final String INITIAL_DEFORMATION_MODE = "Initial deformation mode";
    public static final String FINAL_DEFORMATION_MODE = "Final deformation mode";
    public static final String DIVERGENCE_WEIGHT = "Divergence weight";
    public static final String CURL_WEIGHT = "Curl weight";
    public static final String LANDMARK_WEIGHT = "Landmark weight";
    public static final String IMAGE_WEIGHT = "Image weight";
    public static final String CONSISTENCY_WEIGHT = "Consistency weight";
    public static final String STOP_THRESHOLD = "Stop threshold";
    public static final String ENABLE_MULTITHREADING = "Enable multithreading";


    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[]{FIRST_FRAME,PREVIOUS_FRAME,SPECIFIC_IMAGE};

    }

    public interface RollingCorrectionModes {
        final String NONE = "None";
        final String EVERY_NTH_FRAME = "Every nth frame";

        final String[] ALL = new String[]{NONE,EVERY_NTH_FRAME};

    }

    public interface CalculationSources {
        String INTERNAL = "Internal";
        String EXTERNAL = "External";

        String[] ALL = new String[]{INTERNAL,EXTERNAL};

    }

    public interface RegistrationModes {
        final String FAST = "Fast";
        final String ACCURATE = "Accurate";
        final String MONO = "Mono";

        final String[] ALL = new String[]{FAST,ACCURATE,MONO};

    }

    public interface InitialDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";

        final String[] ALL = new String[]{VERY_COARSE,COARSE,FINE,VERY_FINE};

    }

    public interface FinalDeformationModes {
        final String VERY_COARSE = "Very Coarse";
        final String COARSE = "Coarse";
        final String FINE = "Fine";
        final String VERY_FINE = "Very Fine";
        final String SUPER_FINE = "Super Fine";

        final String[] ALL = new String[]{VERY_COARSE,COARSE,FINE,VERY_FINE,SUPER_FINE};

    }


    private int getRegistrationMode(String registrationMode) {
        switch (registrationMode) {
            case RegistrationModes.FAST:
            default:
                return 0;
            case RegistrationModes.ACCURATE:
                return 1;
            case RegistrationModes.MONO:
                return 2;
        }
    }

    private int getInitialDeformationMode(String initialDeformationMode) {
        switch (initialDeformationMode) {
            case InitialDeformationModes.VERY_COARSE:
            default:
                return 0;
            case InitialDeformationModes.COARSE:
                return 1;
            case InitialDeformationModes.FINE:
                return 2;
            case InitialDeformationModes.VERY_FINE:
                return 3;
        }
    }

    private int getFinalDeformationMode(String finalDeformationMode) {
        switch (finalDeformationMode) {
            case FinalDeformationModes.VERY_COARSE:
            default:
                return 0;
            case FinalDeformationModes.COARSE:
                return 1;
            case FinalDeformationModes.FINE:
                return 2;
            case FinalDeformationModes.VERY_FINE:
                return 3;
            case FinalDeformationModes.SUPER_FINE:
                return 4;
        }
    }

    public static Transformation getTransformation(Image referenceImage, Image warpedImage, Param param) {
        ImagePlus referenceIpl = referenceImage.getImagePlus();
        ImagePlus warpedIpl = warpedImage.getImagePlus();

        return bUnwarpJ_.computeTransformationBatch(referenceIpl, warpedIpl, null, null, param);

    }

    public void applyTransformation(Image inputImage, Transformation transformation, boolean multithread) throws InterruptedException {
        final String tempPath;
        try {
            File tempFile = File.createTempFile("unwarp",".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        int nChannels = inputIpl.getNChannels();
        int nSlices = inputIpl.getNSlices();
        int nFrames = inputIpl.getNFrames();

        int nThreads = multithread ? Prefs.getThreads() : 1;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(nThreads,nThreads,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());

        for (int c=1;c<=nChannels;c++) {
            for (int z=1;z<=nSlices;z++) {
                for (int t=1;t<=nFrames;t++) {
                    int finalC = c;
                    int finalZ = z;
                    int finalT = t;

                    Runnable task = () -> {
                        ImagePlus slice = getSetStack(inputIpl, finalT, finalC, finalZ, null);
                        bUnwarpJ_.applyTransformToSource(tempPath, slice, slice);
                        ImageTypeConverter.applyConversion(slice, 8, ImageTypeConverter.ScalingModes.CLIP);

                        getSetStack(inputIpl, finalT, finalC, finalZ, slice.getProcessor());

                    };
                    pool.submit(task);
                }
            }
        }

        pool.shutdown();
        pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS); // i.e. never terminate early

    }

    synchronized private static ImagePlus getSetStack(ImagePlus inputImagePlus, int timepoint, int channel, int slice, @Nullable ImageProcessor toPut) {
        if (toPut == null) {
            // Get mode
            return SubHyperstackMaker.makeSubhyperstack(inputImagePlus, channel + "-" + channel, slice + "-" + slice, timepoint + "-" + timepoint);
        } else {
            inputImagePlus.setPosition(channel,slice,timepoint);
            inputImagePlus.setProcessor(toPut);
            return null;
        }
    }

    public static void replaceStack(Image inputImage, Image newStack, int channel, int timepoint) {
        ImagePlus inputImagePlus = inputImage.getImagePlus();
        ImagePlus newStackImagePlus = newStack.getImagePlus();

        for (int z=1;z<=newStackImagePlus.getNSlices();z++) {
            inputImagePlus.setPosition(channel,z,timepoint);
            newStackImagePlus.setPosition(1,z,1);

            inputImagePlus.setProcessor(newStackImagePlus.getProcessor());

        }
    }

    public void process(Image inputImage, int calculationChannel, String relativeMode, Param param, int correctionInterval, boolean multithread, @Nullable Image reference, @Nullable Image externalSource) throws InterruptedException {
        // Creating a reference image
        Image projectedReference = null;

        // Assigning source image
        Image source = externalSource == null ? inputImage : externalSource;

        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", "1");
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null) return;
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int total = source.getImagePlus().getNFrames();
        for (int t = 1; t <= source.getImagePlus().getNFrames(); t++) {
            writeMessage("Processing timepoint "+(++count)+" of "+total);

            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't processAutomatic if this is the first frame
                if (t == 1) continue;

                reference = ExtractSubstack.extractSubstack(source, "Reference", String.valueOf(calculationChannel), "1-end", String.valueOf(t - 1));
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);

            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(calculationChannel), "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped", ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (projectedReference == null) return;
            Transformation transformation = getTransformation(projectedReference, projectedWarped, param);

            // Setting the time range for the correction.  This is only the case if a correction interval for "previous-
            // frame" correction is used and the current frame number is an integer multiple of the interval.
            int t2 = t;
            switch (relativeMode) {
                case RelativeModes.PREVIOUS_FRAME:
                    if (correctionInterval != -1 && t%correctionInterval == 0) {
                        t2 = source.getImagePlus().getNFrames();
                    }
                    break;
            }

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same transformation.
            for (int tt = t; tt <= t2; tt++) {
                for (int c = 1; c <= inputImage.getImagePlus().getNChannels(); c++) {
                    warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                    applyTransformation(warped, transformation, multithread);
                    replaceStack(inputImage, warped, c, tt);
                }
            }

            // Need to apply the warp to an external image
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME) && externalSource != null) {
                for (int tt = t; tt <= t2; tt++) {
                    for (int c = 1; c <= source.getImagePlus().getNChannels(); c++) {
                        warped = ExtractSubstack.extractSubstack(source, "Warped", String.valueOf(c), "1-end", String.valueOf(tt));
                        applyTransformation(warped, transformation, multithread);
                        replaceStack(source, warped, c, tt);
                    }
                }
            }
        }
    }


    @Override
    public String getTitle() {
        return "Unwarp images";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        String rollingCorrectionMode = parameters.getValue(ROLLING_CORRECTION);
        int correctionInterval = parameters.getValue(CORRECTION_INTERVAL);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        String calculationSource = parameters.getValue(CALCULATION_SOURCE);
        String externalSourceName = parameters.getValue(EXTERNAL_SOURCE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        String registrationMode = parameters.getValue(REGISTRATION_MODE);
        boolean multithread = parameters.getValue(ENABLE_MULTITHREADING);

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        // If the rolling correction mode is off, set the interval to -1
        if (rollingCorrectionMode.equals(RollingCorrectionModes.NONE)) correctionInterval = -1;

        // Setting up the parameters
        Param param = new Param();
        param.mode = getRegistrationMode(registrationMode);
        param.img_subsamp_fact = parameters.getValue(SUBSAMPLE_FACTOR);
        param.min_scale_deformation = getInitialDeformationMode(parameters.getValue(INITIAL_DEFORMATION_MODE));
        param.max_scale_deformation = getFinalDeformationMode(parameters.getValue(FINAL_DEFORMATION_MODE));
        param.divWeight = parameters.getValue(DIVERGENCE_WEIGHT);
        param.curlWeight = parameters.getValue(CURL_WEIGHT);
        param.landmarkWeight = parameters.getValue(LANDMARK_WEIGHT);
        param.imageWeight = parameters.getValue(IMAGE_WEIGHT);
        if (registrationMode.equals(RegistrationModes.MONO)) {
            param.consistencyWeight = 10.0;
        } else {
            param.consistencyWeight = parameters.getValue(CONSISTENCY_WEIGHT);
        }
        param.stopThreshold = parameters.getValue(STOP_THRESHOLD);

        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName) : null;
        Image externalSource = calculationSource.equals(CalculationSources.EXTERNAL) ? workspace.getImage(externalSourceName) : null;

        try {
            process(inputImage, calculationChannel, relativeMode, param, correctionInterval, multithread, reference, externalSource);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        // Dealing with module outputs
        if (!applyToInput) workspace.addImage(inputImage);
        if (showOutput) inputImage.showImage();

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this));
        parameters.add(new BooleanP(APPLY_TO_INPUT,this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this));
        parameters.add(new ChoiceP(RELATIVE_MODE,this,RelativeModes.FIRST_FRAME,RelativeModes.ALL));
        parameters.add(new ChoiceP(ROLLING_CORRECTION,this,RollingCorrectionModes.NONE,RollingCorrectionModes.ALL));
        parameters.add(new IntegerP(CORRECTION_INTERVAL,this,1));
        parameters.add(new InputImageP(REFERENCE_IMAGE,this));
        parameters.add(new ChoiceP(CALCULATION_SOURCE,this,CalculationSources.INTERNAL,CalculationSources.ALL));
        parameters.add(new InputImageP(EXTERNAL_SOURCE,this));
        parameters.add(new IntegerP(CALCULATION_CHANNEL,this,1));
        parameters.add(new ChoiceP(REGISTRATION_MODE,this,RegistrationModes.FAST,RegistrationModes.ALL));
        parameters.add(new IntegerP(SUBSAMPLE_FACTOR,this,0));
        parameters.add(new ChoiceP(INITIAL_DEFORMATION_MODE,this,InitialDeformationModes.VERY_COARSE,InitialDeformationModes.ALL));
        parameters.add(new ChoiceP(FINAL_DEFORMATION_MODE,this,FinalDeformationModes.FINE,FinalDeformationModes.ALL));
        parameters.add(new DoubleP(DIVERGENCE_WEIGHT,this,0d));
        parameters.add(new DoubleP(CURL_WEIGHT,this,0d));
        parameters.add(new DoubleP(LANDMARK_WEIGHT,this,0d));
        parameters.add(new DoubleP(IMAGE_WEIGHT,this,1d));
        parameters.add(new DoubleP(CONSISTENCY_WEIGHT,this,10d));
        parameters.add(new DoubleP(STOP_THRESHOLD,this,0.01));
        parameters.add(new BooleanP(ENABLE_MULTITHREADING,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));
        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(RELATIVE_MODE));
        switch ((String) parameters.getValue(RELATIVE_MODE)) {
            case RelativeModes.PREVIOUS_FRAME:
                returnedParameters.add(parameters.getParameter(ROLLING_CORRECTION));
                switch ((String) parameters.getValue(ROLLING_CORRECTION)) {
                    case RollingCorrectionModes.EVERY_NTH_FRAME:
                        returnedParameters.add(parameters.getParameter(CORRECTION_INTERVAL));
                        break;
                }
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_SOURCE));
        switch ((String) parameters.getValue(CALCULATION_SOURCE)) {
            case CalculationSources.EXTERNAL:
                returnedParameters.add(parameters.getParameter(EXTERNAL_SOURCE));
                break;
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_CHANNEL));
        returnedParameters.add(parameters.getParameter(REGISTRATION_MODE));
        returnedParameters.add(parameters.getParameter(SUBSAMPLE_FACTOR));
        returnedParameters.add(parameters.getParameter(INITIAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(FINAL_DEFORMATION_MODE));
        returnedParameters.add(parameters.getParameter(DIVERGENCE_WEIGHT));
        returnedParameters.add(parameters.getParameter(CURL_WEIGHT));
        returnedParameters.add(parameters.getParameter(LANDMARK_WEIGHT));
        returnedParameters.add(parameters.getParameter(IMAGE_WEIGHT));

        switch ((String) parameters.getValue(REGISTRATION_MODE)) {
            case RegistrationModes.ACCURATE:
            case RegistrationModes.FAST:
                returnedParameters.add(parameters.getParameter(CONSISTENCY_WEIGHT));
                break;
        }

        returnedParameters.add(parameters.getParameter(STOP_THRESHOLD));
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
