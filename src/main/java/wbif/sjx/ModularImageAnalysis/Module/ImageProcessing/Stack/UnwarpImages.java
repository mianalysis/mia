package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.HyperStackMaker;
import ij.plugin.SubHyperstackMaker;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;

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
        param.setAnisotropyCorrection(1);
        param.setScaleCorrection(1);
        param.setShearCorrection(1);

        return bUnwarpJ_.computeTransformationBatch(warpedIpl, referenceIpl, null, null, param);

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
        inputIpl.duplicate().show();

        for (int c=1;c<=inputIpl.getNChannels();c++) {
            for (int z=1;z<=inputIpl.getNSlices();z++) {
                for (int t=1;t<=inputIpl.getNFrames();t++) {
                    ImagePlus slice = SubHyperstackMaker.makeSubhyperstack(inputIpl,c+"-"+c,z+"-"+z,t+"-"+t);
                    bUnwarpJ_.applyTransformToSource(tempPath,slice,slice);

                    inputIpl.setPosition(c,z,t);
                    inputIpl.setProcessor(slice.getProcessor());
                }
            }
        }

        inputIpl.duplicate().show();

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

        Image referenceImage = workspace.getImage(referenceImageName);

        if (!applyToInput) inputImage = new Image(outputImageName,inputImage.getImagePlus().duplicate());

        Transformation transformation = getTransformation(inputImage,referenceImage);
        applyTransformation(inputImage,transformation);

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE,Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(RELATIVE_MODE,Parameter.CHOICE_ARRAY,RelativeModes.FIRST_FRAME,RelativeModes.ALL));
        parameters.add(new Parameter(REFERENCE_IMAGE,Parameter.INPUT_IMAGE,null));

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
