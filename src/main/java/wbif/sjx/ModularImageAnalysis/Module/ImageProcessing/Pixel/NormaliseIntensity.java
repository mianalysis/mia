package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Pixel;

import bunwarpj.bUnwarpJ_;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageStatistics;
import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.common.Process.IntensityMinMax;

/**
 * Created by sc13967 on 10/08/2017.
 */
public class NormaliseIntensity extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CLIP_FRACTION = "Clipping fraction";

    public static void normaliseIntensity(ImagePlus ipl, double clipFraction) {
        int bitDepth = ipl.getProcessor().getBitDepth();

        // Get min max values for whole stack
        for (int c = 1; c <= ipl.getNChannels(); c++) {
            double[] range = IntensityMinMax.getWeightedChannelRange(ipl,c-1,clipFraction);
            double min = range[0];
            double max = range[1];

            // Applying normalisation
            double factor = bitDepth == 32 ? 1 : Math.pow(2,bitDepth)-1;
            double mult = factor / (max - min);

            for (int z = 1; z <= ipl.getNSlices(); z++) {
                for (int t = 1; t <= ipl.getNFrames(); t++) {
                    ipl.setPosition(c, z, t);
                    ipl.getProcessor().subtract(min);
                    ipl.getProcessor().multiply(mult);
                }
            }
        }

        // Resetting location of the image
        ipl.setPosition(1,1,1);

        // Set brightness/contrast
        IntensityMinMax.run(ipl,true,0);

    }

    @Override
    public String getTitle() {
        return "Normalise intensity";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "Sets the intensity to maximise the dynamic range of the image.\n" +
                "\"Clipping fraction\" is the fraction of pixels at either end of the range that gets clipped.";
    }

    @Override
    public void run(Workspace workspace) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        double clipFraction = parameters.getValue(CLIP_FRACTION);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) inputImagePlus = new Duplicator().run(inputImagePlus);

        // Running intensity normalisation
        normaliseIntensity(inputImagePlus,clipFraction);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);

            // If selected, displaying the image
            if (showOutput) {
                ImagePlus showIpl = new Duplicator().run(outputImage.getImagePlus());
                showIpl.setTitle(outputImageName);
                showIpl.show();
            }

        } else {
            // If selected, displaying the image
            if (showOutput) {
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
        parameters.add(new Parameter(CLIP_FRACTION,Parameter.DOUBLE,0d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CLIP_FRACTION));

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