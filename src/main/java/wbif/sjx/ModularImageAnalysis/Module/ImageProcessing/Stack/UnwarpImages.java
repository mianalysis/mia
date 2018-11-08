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


    public interface RelativeModes {
        final String FIRST_FRAME = "First frame";
        final String PREVIOUS_FRAME = "Previous frame";
        final String SPECIFIC_IMAGE = "Specific image";

        final String[] ALL = new String[]{FIRST_FRAME,PREVIOUS_FRAME,SPECIFIC_IMAGE};

    }

    public static Transformation getTransformation(Image referenceImage, Image warpedImage) {
        ImagePlus referenceIpl = referenceImage.getImagePlus();
        ImagePlus warpedIpl = warpedImage.getImagePlus();

        Param param = new Param();
        param.mode = 2; // Accurate
        param.img_subsamp_fact = 0;
        param.min_scale_deformation = 0; // Very coarse
        param.max_scale_deformation = 2; // Fine
        param.divWeight = 0;
        param.curlWeight = 0;
        param.landmarkWeight = 0;
        param.imageWeight = 1;
        param.consistencyWeight = 10;
        param.stopThreshold = 0.01;

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

    public void process(Image inputImage, int calculationChannel, String relativeMode, @Nullable Image reference) {
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
            writeMessage("Processing image "+(++count)+" of "+total);

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
            Transformation transformation = getTransformation(projectedReference, projectedWarped);

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

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        // Loops over each channel
           Image reference = relativeMode.equals(RelativeModes.SPECIFIC_IMAGE)
                    ? workspace.getImage(referenceImageName) : null;
            process(inputImage,calculationChannel,relativeMode,reference);

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
