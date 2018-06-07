package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageStatistics;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String SHOW_IMAGE = "Show image";

    public static void normaliseIntensity(ImagePlus ipl) {
        int bitDepth = ipl.getProcessor().getBitDepth();
        if (bitDepth == 8 | bitDepth == 16) IJ.run(ipl, "32-bit", null);

        // Get min max values for whole stack
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    ipl.setPosition(c, z, t);
                    ImageStatistics imageStatistics = ipl.getStatistics();
                    min = Math.min(min, imageStatistics.min);
                    max = Math.max(max, imageStatistics.max);
                }
            }
        }

        // Applying normalisation
        double mult = bitDepth == 32 ? 1 : Math.pow(2,bitDepth)-1;
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    ipl.setPosition(c, z, t);
                    ipl.getProcessor().subtract(min);
                    ipl.getProcessor().multiply(mult / (max - min));
                }
            }
        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

        ipl.setDisplayRange(0,mult);
        switch (bitDepth) {
            case 8:
                IJ.run(ipl, "8-bit", null);
                break;

            case 16:
                IJ.run(ipl, "16-bit", null);
                break;

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
    public void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Running intensity normalisation
        normaliseIntensity(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                ImagePlus showIpl = new Duplicator().run(outputImage.getImagePlus());
                showIpl.setTitle(outputImageName);
                showIpl.show();
            }

        } else {
            // If selected, displaying the image
            if (parameters.getValue(SHOW_IMAGE)) {
                ImagePlus showIpl = new Duplicator().run(inputImagePlus);
                showIpl.setTitle(inputImageName);
                showIpl.show();
            }
        }
    }

    @Override
    public void initialiseParameters() {
        parameters.add(new Parameter(INPUT_IMAGE, Parameter.INPUT_IMAGE,null));
        parameters.add(new Parameter(APPLY_TO_INPUT, Parameter.BOOLEAN,true));
        parameters.add(new Parameter(OUTPUT_IMAGE, Parameter.OUTPUT_IMAGE,null));
        parameters.add(new Parameter(SHOW_IMAGE, Parameter.BOOLEAN,false));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(SHOW_IMAGE));

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}