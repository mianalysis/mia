package wbif.sjx.MIA.Module.ImageProcessing.Pixel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 19/09/2017.
 */
public class ImageMath extends Module {
    public static final String INPUT_SEPARATOR = "Image input/output";
    public static final String INPUT_IMAGE = "Input image";
    public static final String APPLY_TO_INPUT = "Apply to input image";
    public static final String OUTPUT_IMAGE = "Output image";

    public static final String CALCULATION_SEPARATOR = "Image calculation";
    public static final String CALCULATION_TYPE = "Calculation";
    public static final String VALUE_SOURCE = "Value source";
    public static final String IMAGE_FOR_MEASUREMENT = "Image for measurement";
    public static final String MEASUREMENT = "Measurement";
    public static final String MATH_VALUE = "Value";

    public ImageMath(ModuleCollection modules) {
        super("Image math", modules);
    }

    public interface CalculationTypes {
        String ABSOLUTE = "Absolute";
        String ADD = "Add";
        String DIVIDE = "Divide";
        String MULTIPLY = "Multiply";
        String SQUARE = "Square";
        String SQUAREROOT = "Squareroot";
        String SUBTRACT = "Subtract";

        String[] ALL = new String[] { ABSOLUTE, ADD, DIVIDE, MULTIPLY, SQUARE, SQUAREROOT, SUBTRACT };

    }

    public interface ValueSources {
        String FIXED = "Fixed value";
        String MEASUREMENT = "Image measurement value";

        String[] ALL = new String[] { FIXED, MEASUREMENT };

    }

    public static void process(Image inputImage, String calculationType, double mathValue) {
        process(inputImage.getImagePlus(), calculationType, mathValue);

    }

    public static void process(ImagePlus inputImagePlus, String calculationType, double mathValue) {
        ImageStack ist = inputImagePlus.getStack();

        for (int i = 0; i < ist.size(); i++) {
            ImageProcessor ipr = ist.getProcessor(i + 1);
            switch (calculationType) {
            case CalculationTypes.ABSOLUTE:
                ipr.abs();
                break;

            case CalculationTypes.ADD:
                ipr.add(mathValue);
                break;

            case CalculationTypes.DIVIDE:
                ipr.multiply(1 / mathValue);
                break;

            case CalculationTypes.MULTIPLY:
                ipr.multiply(mathValue);
                break;

            case CalculationTypes.SQUARE:
                ipr.sqr();
                break;

            case CalculationTypes.SQUAREROOT:
                ipr.sqrt();
                break;

            case CalculationTypes.SUBTRACT:
                ipr.subtract(mathValue);
                break;
            }
        }
    }

    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_PIXEL;
    }

    @Override
    public String getDescription() {
        return "Applies a mathematical operation to all pixels of the input image stack.  Operations that can be performed are: "
                + String.join(", ", CalculationTypes.ALL);
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE);
        String calculationType = parameters.getValue(CALCULATION_TYPE);
        String valueSource = parameters.getValue(VALUE_SOURCE);
        String imageForMeasurementName = parameters.getValue(IMAGE_FOR_MEASUREMENT);
        String measurement = parameters.getValue(MEASUREMENT);
        double mathValue = parameters.getValue(MATH_VALUE);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput) {
            inputImagePlus = new Duplicator().run(inputImagePlus);
        }

        // Updating value if taken from a measurement
        switch (valueSource) {
        case ValueSources.MEASUREMENT:
            Image imageForMeasurement = workspace.getImage(imageForMeasurementName);
            mathValue = imageForMeasurement.getMeasurement(measurement).getValue();
            break;
        }

        process(inputImagePlus, calculationType, mathValue);

        // If the image is being saved as a new image, adding it to the workspace
        if (!applyToInput) {
            writeStatus("Adding image (" + outputImageName + ") to workspace");
            Image outputImage = new Image(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.showImage();

        } else {
            if (showOutput)
                inputImage.showImage();

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_TYPE, this, CalculationTypes.ADD, CalculationTypes.ALL));
        parameters.add(new ChoiceP(VALUE_SOURCE, this, ValueSources.FIXED, ValueSources.ALL));
        parameters.add(new InputImageP(IMAGE_FOR_MEASUREMENT, this));
        parameters.add(new ImageMeasurementP(MEASUREMENT, this));
        parameters.add(new DoubleP(MATH_VALUE, this, 1.0));

        addParameterDescriptions();

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

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_TYPE));
        switch ((String) parameters.getValue(CALCULATION_TYPE)) {
        case CalculationTypes.ADD:
        case CalculationTypes.DIVIDE:
        case CalculationTypes.MULTIPLY:
        case CalculationTypes.SUBTRACT:
            returnedParameters.add(parameters.getParameter(VALUE_SOURCE));
            switch ((String) parameters.getValue(VALUE_SOURCE)) {
            case ValueSources.FIXED:

                returnedParameters.add(parameters.getParameter(MATH_VALUE));

                break;

            case ValueSources.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(IMAGE_FOR_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(MEASUREMENT));

                if (parameters.getValue(INPUT_IMAGE) != null) {
                    ImageMeasurementP measurement = parameters.getParameter(MEASUREMENT);
                    measurement.setImageName(parameters.getValue(IMAGE_FOR_MEASUREMENT));
                }
                break;
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
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }

    void addParameterDescriptions() {
        parameters.get(INPUT_IMAGE)
                .setDescription("Image from workspace to apply calculation to.  This image can be of any bit depth.");

        parameters.get(APPLY_TO_INPUT).setDescription(
                "When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the \""
                        + OUTPUT_IMAGE + "\" parameter.");

        parameters.get(OUTPUT_IMAGE).setDescription("If \"" + APPLY_TO_INPUT
                + "\" is not selected, the post-operation image will be saved to the workspace with this name.");

        parameters.get(CALCULATION_TYPE).setDescription(
                "Controls the mathematical operation being applied to all pixels of this image.  Choices are: "
                        + String.join(", ", CalculationTypes.ALL));

        parameters.get(VALUE_SOURCE).setDescription(
                "For calculations that require a specific value (i.e. addition, subtraction, etc.) this parameter controls how the value is defined:<br><ul>"

                        + "<li>\"" + ValueSources.FIXED + "\" A fixed value is specified using the \"" + MATH_VALUE
                        + "\" parameter.  This value is the same for all images processed by this module..</li>"

                        + "<li>\"" + ValueSources.MEASUREMENT
                        + "\" The value is taken from a measurement associated with the input image.  Values obtained in this way can be different from image to image.</li></ul>");

        parameters.get(MEASUREMENT).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.MEASUREMENT
                + "\", this is the measurement associated with the input image that will be used in the calculation.");

        parameters.get(MATH_VALUE).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.FIXED
                + "\", this is the value that will be used in the calculation.");

    }
}
