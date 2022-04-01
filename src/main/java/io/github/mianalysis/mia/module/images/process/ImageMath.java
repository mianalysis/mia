package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.OutputImageP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 19/09/2017.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
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

    public ImageMath(Modules modules) {
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
        return Categories.IMAGES_PROCESS;
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
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
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
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();

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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        String storageMode = MIA.preferences.getDataStorageMode();
        if (storageMode.equals(Preferences.DataStorageModes.STREAM_FROM_DRIVE) & il2Support.equals(IL2Support.NONE))
            return false;

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

        parameters.get(IMAGE_FOR_MEASUREMENT).setDescription("If \"" + VALUE_SOURCE + "\" is set to \""
                + ValueSources.MEASUREMENT + "\", this is the image that the measurement will be taken from.  It can be any image in the workspace, not necessarily the image to which the math operation is being applied.");

        parameters.get(MEASUREMENT).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.MEASUREMENT
                + "\", this is the measurement associated with the image specified by \""+IMAGE_FOR_MEASUREMENT+"\" that will be used in the calculation.");

        parameters.get(MATH_VALUE).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.FIXED
                + "\", this is the value that will be used in the calculation.");

    }
}
