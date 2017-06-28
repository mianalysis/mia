package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ImageTypeConverter extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String OUTPUT_TYPE = "Output image type";
    public static final String SCALE_INTENSITIES = "Scale intensities to full range";

    private static final String INT8 = "8-bit integer";
    private static final String INT16 = "16-bit integer";
    private static final String FLOAT32 = "32-bit float";
    private static final String[] OUTPUT_TYPES = new String[]{INT8,INT16,FLOAT32};

    @Override
    public String getTitle() {
        return "Image type converter";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public void execute(HCWorkspace workspace, boolean verbose) {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        HCImage inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputType = parameters.getValue(OUTPUT_TYPE);
        boolean scaleIntensities = parameters.getValue(SCALE_INTENSITIES);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // If necessary, stretching input image intensities to full range
        if (scaleIntensities) IntensityMinMax.run(inputImagePlus,true);

        // Converting to requested type
        switch (outputType) {
            case INT8:
                IJ.run(inputImagePlus, "8-bit", null);
                break;
            case INT16:
                IJ.run(inputImagePlus, "16-bit", null);
                break;
            case FLOAT32:
                IJ.run(inputImagePlus, "32-bit", null);
                break;
        }

        // Adding output image to workspace if necessary
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            if (verbose) System.out.println("["+moduleName+"] Adding image ("+outputImageName+") to workspace");
            HCImage outputImage = new HCImage(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

        }

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.INPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(APPLY_TO_INPUT,HCParameter.BOOLEAN,true));
        parameters.addParameter(new HCParameter(OUTPUT_IMAGE,HCParameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new HCParameter(OUTPUT_TYPE,HCParameter.CHOICE_ARRAY,OUTPUT_TYPES[0],OUTPUT_TYPES));
        parameters.addParameter(new HCParameter(SCALE_INTENSITIES,HCParameter.BOOLEAN,false));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        HCParameterCollection returnedParameters = new HCParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(OUTPUT_TYPE));
        returnedParameters.addParameter(parameters.getParameter(SCALE_INTENSITIES));

        return returnedParameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
