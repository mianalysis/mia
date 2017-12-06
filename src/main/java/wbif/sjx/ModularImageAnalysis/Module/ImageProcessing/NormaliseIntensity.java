package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends HCModule {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    public static void normaliseIntenisty(ImagePlus ipl) {
        double minIntensity = ipl.getStatistics().min;
        double maxIntensity = ipl.getStatistics().max;

        int bitDepth = ipl.getProcessor().getBitDepth();

        for (int z = 1; z <= ipl.getNSlices(); z++) {
            for (int c = 1; c <= ipl.getNChannels(); c++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    ipl.setPosition(c, z, t);

                    if (bitDepth == 8 | bitDepth == 16) ipl.setProcessor(ipl.getProcessor().convertToFloat());

                    ipl.getProcessor().subtract(minIntensity);
                    ipl.getProcessor().multiply(1 / (maxIntensity - minIntensity));

                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "Normalise intensity";
    }

    @Override
    public String getHelp() {
        return "Sets the intensity to maximise the dynamic range of the image";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Running intensity normalisation
        normaliseIntenisty(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(outputImage.getImagePlus()).show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                new Duplicator().run(inputImagePlus).show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.addParameter(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.addParameter(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.addParameter(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,true));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.addParameter(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.addParameter(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.addParameter(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.addParameter(parameters.getParameter(SHOW_IMAGE));

        return returnedParameters;

    }

    @Override
    public void initialiseReferences() {

    }

    @Override
    public ReferenceCollection updateAndGetImageReferences() {
        return null;
    }

    @Override
    public ReferenceCollection updateAndGetObjectReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}