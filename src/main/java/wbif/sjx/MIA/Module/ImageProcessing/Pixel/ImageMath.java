package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.plugin.Duplicator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 19/09/2017.
 */
public class ImageMath extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";
    public static final String CALCULATION_TYPE = "Calculation";
    public static final String VALUE_SOURCE = "Value source";
    public static final String MEASUREMENT = "Measurement";
    public static final String MATH_VALUE = "Value";

    public ImageMath(ModuleCollection modules) {
        super("Image math",modules);
    }

    public interface CalculationTypes {
        String ADD = "Add";
        String DIVIDE = "Divide";
        String MULTIPLY = "Multiply";
        String SUBTRACT = "Subtract";

        String[] ALL = new String[]{ADD,DIVIDE,MULTIPLY,SUBTRACT};

    }

    public interface ValueSources {
        String FIXED = "Fixed value";
        String MEASUREMENT = "Measurement value";

        String[] ALL = new String[]{FIXED,MEASUREMENT};

    }

    public static void process(ImagePlus inputImagePlus, String calculationType, double mathValue) {
        int nChannels = inputImagePlus.getNChannels();
        int nSlices = inputImagePlus.getNSlices();
        int nFrames = inputImagePlus.getNFrames();

        // Checking the number of dimensions.  If a dimension of image2 is 1 this dimension is used for all images.
        for (int z = 1; z <= nSlices; z++) {
            for (int c = 1; c <= nChannels; c++) {
                for (int t = 1; t <= nFrames; t++) {
                    inputImagePlus.setPosition(c, z, t);

                    switch (calculationType) {
                        case CalculationTypes.ADD:
                            inputImagePlus.getProcessor().add(mathValue);
                            break;

                        case CalculationTypes.DIVIDE:
                            inputImagePlus.getProcessor().multiply(1 / mathValue);
                            break;

                        case CalculationTypes.MULTIPLY:
                            inputImagePlus.getProcessor().multiply(mathValue);
                            break;

                        case CalculationTypes.SUBTRACT:
                            inputImagePlus.getProcessor().subtract(mathValue);
                            break;

                    }
                }
            }
        }

        inputImagePlus.setPosition(1, 1, 1);
    }


    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String calculationType = parameters.getValue(CALCULATION_TYPE);
        String valueSource = parameters.getValue(VALUE_SOURCE);
        String measurement = parameters.getValue(MEASUREMENT);
        double mathValue = parameters.getValue(MATH_VALUE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {inputImagePlus = new Duplicator().run(inputImagePlus);}

        // Updating value if taken from a measurement
        switch (valueSource) {
            case ValueSources.MEASUREMENT:
                mathValue = inputImage.getMeasurement(measurement).getValue();
                break;
        }

        process(inputImagePlus,calculationType,mathValue);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeMessage("Adding image ("+outputImageName+") to workspace");
            Image outputImage = new Image(outputImageName,inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput) outputImage.showImage();

        } else {
            if (showOutput) inputImage.showImage();

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this,true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new ChoiceP(CALCULATION_TYPE,this,CalculationTypes.ADD,CalculationTypes.ALL));
        parameters.add(new ChoiceP(VALUE_SOURCE,this, ValueSources.FIXED, ValueSources.ALL));
        parameters.add(new ImageMeasurementP(MEASUREMENT,this));
        parameters.add(new DoubleP(MATH_VALUE,this,1.0));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT)) {
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        }

        returnedParameters.add(parameters.getParameter(CALCULATION_TYPE));
        returnedParameters.add(parameters.getParameter(VALUE_SOURCE));

        switch ((String) parameters.getValue(VALUE_SOURCE)) {
            case ValueSources.FIXED:
                returnedParameters.add(parameters.getParameter(MATH_VALUE));
                break;

            case ValueSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_IMAGE) != null) {
                    ImageMeasurementP measurement = parameters.getParameter(MEASUREMENT);
                    measurement.setImageName(parameters.getValue(INPUT_IMAGE));
                }
                break;
        }

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

}
