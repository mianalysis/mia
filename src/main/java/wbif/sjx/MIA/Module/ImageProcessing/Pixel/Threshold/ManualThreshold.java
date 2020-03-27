// TODO: Add true 3D local thresholds (local auto thresholding works slice-by-slice)

package wbif.sjx.MIA.Module.ImageProcessing.Pixel.Threshold;

import ij.ImagePlus;
import ij.Prefs;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.InvertIntensity;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.IntegerP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class ManualThreshold extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String THRESHOLD_SEPARATOR = "Threshold controls";
    public static final String THRESHOLD_SOURCE = "Threshold source";
    public static final String THRESHOLD_VALUE = "Threshold value";
    public static final String MEASUREMENT = "Measurement";
    public static final String WHITE_BACKGROUND = "Black objects/white background";


    public interface ThresholdSources {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";

        String[] ALL = new String[]{FIXED_VALUE, IMAGE_MEASUREMENT};

    }


    public ManualThreshold(ModuleCollection modules) {
        super("Manual threshold",modules);
    }


    public static void applyThreshold(ImagePlus inputImagePlus, int threshold) {
        // Applying threshold
        for (int z = 1; z <= inputImagePlus.getNSlices(); z++) {
            for (int c = 1; c <= inputImagePlus.getNChannels(); c++) {
                for (int t = 1; t <= inputImagePlus.getNFrames(); t++) {
                    inputImagePlus.setPosition(c, z, t);
                    inputImagePlus.getProcessor().threshold(threshold);
                }
            }
        }

        inputImagePlus.setPosition(1,1,1);

    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL_THRESHOLD;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        boolean whiteBackground = parameters.getValue(WHITE_BACKGROUND);
        String thresholdSource = parameters.getValue(THRESHOLD_SOURCE);
        int thresholdValue = parameters.getValue(THRESHOLD_VALUE);
        String measurementName = parameters.getValue(MEASUREMENT);

        if (thresholdSource.equals(ThresholdSources.IMAGE_MEASUREMENT)) {
            thresholdValue = (int) Math.round(inputImage.getMeasurement(measurementName).getValue());
        }

        Prefs.blackBackground = !whiteBackground;

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Calculating the threshold based on the selected algorithm
        applyThreshold(inputImagePlus,thresholdValue);

        if (whiteBackground) InvertIntensity.process(inputImagePlus);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            if (showOutput) inputImage.showImage();

        } else {
            String outputImageName = parameters.getValue(OUTPUT_IMAGE);
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();
        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputImageP(INPUT_IMAGE, this, "", "Image to apply threshold to."));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true, "Select if the threshold should be applied directly to the input image, or if it should be applied to a duplicate, then stored as a different image in the workspace."));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this, "", "Name of the output image created during the thresholding process.  This image will be added to the workspace."));

        parameters.add(new ParamSeparatorP(THRESHOLD_SEPARATOR,this));
        parameters.add(new ChoiceP(THRESHOLD_SOURCE, this, ThresholdSources.FIXED_VALUE, ThresholdSources.ALL));
        parameters.add(new IntegerP(THRESHOLD_VALUE, this, 1, "Absolute manual threshold value that will be applied to all pixels."));
        parameters.add(new ImageMeasurementP(MEASUREMENT, this));
        parameters.add(new BooleanP(WHITE_BACKGROUND, this,true, "Controls the logic of the output image in terms of what is considered foreground and background."));

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

        returnedParameters.add(parameters.getParameter(THRESHOLD_SEPARATOR));
        returnedParameters.add(parameters.getParameter(THRESHOLD_SOURCE));

        switch ((String) parameters.getValue(THRESHOLD_SOURCE)) {
            case ThresholdSources.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(THRESHOLD_VALUE));
                break;
            case ThresholdSources.IMAGE_MEASUREMENT:
                ImageMeasurementP parameter = parameters.getParameter(MEASUREMENT);
                parameter.setImageName(parameters.getValue(INPUT_IMAGE));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));
                break;
        }

        returnedParameters.add(parameters.getParameter(WHITE_BACKGROUND));

        return returnedParameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
