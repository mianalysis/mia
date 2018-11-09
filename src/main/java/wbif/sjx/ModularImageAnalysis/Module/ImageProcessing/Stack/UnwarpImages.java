package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.plugin.HyperStackMaker;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.NormaliseIntensity;
import wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel.ProjectImage;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UnwarpImages extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String RELATIVE_MODE = "Relative mode";
    public static final String REFERENCE_IMAGE = "Reference image";
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


    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[]{FIRST_FRAME,PREVIOUS_FRAME,SPECIFIC_IMAGE};

    }

    public interface RegistrationModes {
        String FAST = "Fast";
        String ACCURATE = "Accurate";
        String MONO = "Mono";

        String[] ALL = new String[]{FAST,ACCURATE,MONO};

    }

    public interface InitialDeformationModes {
        String VERY_COARSE = "Very Coarse";
        String COARSE = "Coarse";
        String FINE = "Fine";
        String VERY_FINE = "Very Fine";

        String[] ALL = new String[]{VERY_COARSE,COARSE,FINE,VERY_FINE};

    }

    public interface FinalDeformationModes {
        String VERY_COARSE = "Very Coarse";
        String COARSE = "Coarse";
        String FINE = "Fine";
        String VERY_FINE = "Very Fine";
        String SUPER_FINE = "Super Fine";

        String[] ALL = new String[]{VERY_COARSE,COARSE,FINE,VERY_FINE,SUPER_FINE};

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

    public static void applyTransformation(Image inputImage, Transformation transformation) {
        String tempPath = null;
        try {
            File tempFile = File.createTempFile("unwarp",".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));
            bufferedWriter.close();

            tempPath = tempFile.getAbsolutePath();
            transformation.saveDirectTransformation(tempPath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tempPath == null) return;

        // Iterate over all images in the stack
        ImagePlus inputIpl = inputImage.getImagePlus();
        for (int c=1;c<=inputIpl.getNChannels();c++) {
            for (int z=1;z<=inputIpl.getNSlices();z++) {
                for (int t=1;t<=inputIpl.getNFrames();t++) {
                    inputIpl.setPosition(c,z,t);
                    ImagePlus slice = new ImagePlus("Slice",inputIpl.getProcessor().duplicate());

                    bUnwarpJ_.applyTransformToSource(tempPath,slice,slice);
                    ImageTypeConverter.applyConversion(slice,8,ImageTypeConverter.ScalingModes.CLIP);

                    inputIpl.setProcessor(slice.getProcessor());

                }
            }
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

    public void process(Image inputImage, int calculationChannel, String relativeMode, Param param, @Nullable Image reference) {
        // Creating a reference image
        Image projectedReference = null;

        // Assigning fixed reference images
        switch (relativeMode) {
            case RelativeModes.FIRST_FRAME:
                reference = ExtractSubstack.extractSubstack(inputImage, "Reference", String.valueOf(calculationChannel), "1-end", "1");
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;

            case RelativeModes.SPECIFIC_IMAGE:
                if (reference == null) return;
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);
                break;
        }

        // Iterate over each time-step
        int count = 0;
        int total = inputImage.getImagePlus().getNFrames();
        for (int t = 1; t <= inputImage.getImagePlus().getNFrames(); t++) {
            writeMessage("Processing timepoint "+(++count)+" of "+total);

            // If the reference image is the previous frame, calculate this now
            if (relativeMode.equals(RelativeModes.PREVIOUS_FRAME)) {
                // Can't process if this is the first frame
                if (t == 1) continue;

                reference = ExtractSubstack.extractSubstack(inputImage, "Reference", String.valueOf(calculationChannel), "1-end", String.valueOf(t - 1));
                projectedReference = ProjectImage.projectImageInZ(reference, "ProjectedReference", ProjectImage.ProjectionModes.MAX);

            }

            // Getting the projected image at this time-point
            Image warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(calculationChannel), "1-end", String.valueOf(t));
            Image projectedWarped = ProjectImage.projectImageInZ(warped, "ProjectedWarped", ProjectImage.ProjectionModes.MAX);

            // Calculating the transformation for this image pair
            if (projectedReference == null) return;
            Transformation transformation = getTransformation(projectedReference, projectedWarped, param);

            // Applying the transformation to the whole stack.
            // All channels should move in the same way, so are processed with the same transformation.
            for (int c=1;c<=inputImage.getImagePlus().getNChannels();c++) {
                warped = ExtractSubstack.extractSubstack(inputImage, "Warped", String.valueOf(c), "1-end", String.valueOf(t));
                applyTransformation(warped, transformation);

                // Replacing the original stack with the warped one
                replaceStack(inputImage, warped, c, t);

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
    protected void run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String relativeMode = parameters.getValue(RELATIVE_MODE);
        String referenceImageName = parameters.getValue(REFERENCE_IMAGE);
        int calculationChannel = parameters.getValue(CALCULATION_CHANNEL);
        String registrationMode = parameters.getValue(REGISTRATION_MODE);
        int subsampleFactor = parameters.getValue(SUBSAMPLE_FACTOR);
        String initialDeformationMode = parameters.getValue(INITIAL_DEFORMATION_MODE);
        String finalDeformationMode = parameters.getValue(FINAL_DEFORMATION_MODE);
        double divergenceWeight = parameters.getValue(DIVERGENCE_WEIGHT);
        double curlWeight = parameters.getValue(CURL_WEIGHT);
        double landmarkWeight = parameters.getValue(LANDMARK_WEIGHT);
        double imageWeight = parameters.getValue(IMAGE_WEIGHT);
        double consistencyWeight = parameters.getValue(CONSISTENCY_WEIGHT);
        double stopThreshold = parameters.getValue(STOP_THRESHOLD);

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        // Setting up the parameters
        Param param = new Param();
        param.mode = getRegistrationMode(registrationMode);
        param.img_subsamp_fact = subsampleFactor;
        param.min_scale_deformation = getInitialDeformationMode(initialDeformationMode); // Very coarse
        param.max_scale_deformation = getFinalDeformationMode(finalDeformationMode); // Fine
        param.divWeight = divergenceWeight;
        param.curlWeight = curlWeight;
        param.landmarkWeight = landmarkWeight;
        param.imageWeight = imageWeight;
        if (registrationMode.equals(RegistrationModes.MONO)) {
            param.consistencyWeight = 10.0;
        } else {
            param.consistencyWeight = consistencyWeight;
        }
        param.stopThreshold = stopThreshold;

        Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE) ? workspace.getImage(referenceImageName) : null;
        process(inputImage,calculationChannel,relativeMode,param,reference);

        // Dealing with module outputs
        if (!applyToInput) workspace.addImage(inputImage);
        if (showOutput) showImage(inputImage);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(RELATIVE_MODE,Parameter.CHOICE_ARRAY,RelativeModes.FIRST_FRAME,RelativeModes.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(CALCULATION_CHANNEL, Parameter.INTEGER,1));
        parameters.add(new Parameter(REGISTRATION_MODE,Parameter.CHOICE_ARRAY,RegistrationModes.FAST,RegistrationModes.ALL));
        parameters.add(new Parameter(SUBSAMPLE_FACTOR,Parameter.INTEGER,0));
        parameters.add(new Parameter(INITIAL_DEFORMATION_MODE,Parameter.CHOICE_ARRAY,InitialDeformationModes.VERY_COARSE,InitialDeformationModes.ALL));
        parameters.add(new Parameter(FINAL_DEFORMATION_MODE,Parameter.CHOICE_ARRAY,FinalDeformationModes.FINE,FinalDeformationModes.ALL));
        parameters.add(new Parameter(DIVERGENCE_WEIGHT, Parameter.DOUBLE,0d));
        parameters.add(new Parameter(CURL_WEIGHT, Parameter.DOUBLE,0d));
        parameters.add(new Parameter(LANDMARK_WEIGHT, Parameter.DOUBLE,0d));
        parameters.add(new Parameter(IMAGE_WEIGHT, Parameter.DOUBLE,1d));
        parameters.add(new Parameter(CONSISTENCY_WEIGHT, Parameter.DOUBLE,10d));
        parameters.add(new Parameter(STOP_THRESHOLD, Parameter.DOUBLE,0.01));

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
            case RelativeModes.SPECIFIC_IMAGE:
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE));
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

        return returnedParameters;

    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
