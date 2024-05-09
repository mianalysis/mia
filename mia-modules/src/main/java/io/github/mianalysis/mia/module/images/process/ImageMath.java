package io.github.mianalysis.mia.module.images.process;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
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
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 19/09/2017.
 */

/**
* Applies a mathematical operation to all pixels of the input image stack.  Operations that can be performed are: Absolute, Add, Divide, Multiply, Square, Squareroot, Subtract
*/
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ImageMath extends Module {

	/**
	* 
	*/
    public static final String INPUT_SEPARATOR = "Image input/output";

	/**
	* Image from workspace to apply calculation to.  This image can be of any bit depth.
	*/
    public static final String INPUT_IMAGE = "Input image";

	/**
	* When selected, the post-operation image will overwrite the input image in the workspace.  Otherwise, the image will be saved to the workspace with the name specified by the "Output image" parameter.
	*/
    public static final String APPLY_TO_INPUT = "Apply to input image";

	/**
	* If "Apply to input image" is not selected, the post-operation image will be saved to the workspace with this name.
	*/
    public static final String OUTPUT_IMAGE = "Output image";

	/**
	* When enabled, the calculation will be performed on 32-bit float values.  This is useful if the calculation is likely to create negative or decimal values.  The output image will also be stored in the workspace as a 32-bit float image.
	*/
    public static final String OUTPUT_32BIT = "Output 32-bit image";


	/**
	* 
	*/
    public static final String CALCULATION_SEPARATOR = "Image calculation";

	/**
	* Controls the mathematical operation being applied to all pixels of this image.  Choices are: Absolute, Add, Divide, Multiply, Square, Squareroot, Subtract
	*/
    public static final String CALCULATION_MODE = "Calculation";

	/**
	* For calculations that require a specific value (i.e. addition, subtraction, etc.) this parameter controls how the value is defined:<br><ul><li>"Fixed value" A fixed value is specified using the "Value" parameter.  This value is the same for all images processed by this module..</li><li>"Image measurement value" The value is taken from a measurement associated with the input image.  Values obtained in this way can be different from image to image.</li></ul>
	*/
    public static final String VALUE_SOURCE = "Value source";

	/**
	* If "Value source" is set to "Image measurement value", this is the image that the measurement will be taken from.  It can be any image in the workspace, not necessarily the image to which the math operation is being applied.
	*/
    public static final String IMAGE_FOR_MEASUREMENT = "Image for measurement";

	/**
	* If "Value source" is set to "Image measurement value", this is the measurement associated with the image specified by "Image for measurement" that will be used in the calculation.
	*/
    public static final String MEASUREMENT = "Measurement";

	/**
	* If "Value source" is set to "Fixed value", this is the value that will be used in the calculation.
	*/
    public static final String MATH_VALUE = "Value";

    public ImageMath(Modules modules) {
        super("Image math", modules);
    }

    public interface CalculationModes {
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
                case CalculationModes.ABSOLUTE:
                    ipr.abs();
                    break;

                case CalculationModes.ADD:
                    ipr.add(mathValue);
                    break;

                case CalculationModes.DIVIDE:
                    ipr.multiply(1 / mathValue);
                    break;

                case CalculationModes.MULTIPLY:
                    ipr.multiply(mathValue);
                    break;

                case CalculationModes.SQUARE:
                    ipr.sqr();
                    break;

                case CalculationModes.SQUAREROOT:
                    ipr.sqrt();
                    break;

                case CalculationModes.SUBTRACT:
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Applies a mathematical operation to all pixels of the input image stack.  Operations that can be performed are: "
                + String.join(", ", CalculationModes.ALL);
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE, workspace);
        Image inputImage = workspace.getImages().get(inputImageName);
        ImagePlus inputImagePlus = inputImage.getImagePlus();

        // Getting parameters
        boolean applyToInput = parameters.getValue(APPLY_TO_INPUT, workspace);
        String outputImageName = parameters.getValue(OUTPUT_IMAGE, workspace);
        boolean output32Bit = parameters.getValue(OUTPUT_32BIT, workspace);
        String calculationType = parameters.getValue(CALCULATION_MODE, workspace);
        String valueSource = parameters.getValue(VALUE_SOURCE, workspace);
        String imageForMeasurementName = parameters.getValue(IMAGE_FOR_MEASUREMENT, workspace);
        String measurement = parameters.getValue(MEASUREMENT, workspace);
        double mathValue = parameters.getValue(MATH_VALUE, workspace);

        // If applying to a new image, the input image is duplicated
        if (!applyToInput)
            inputImagePlus = new Duplicator().run(inputImagePlus);
        
        // If converting to 32-bit, do this now
        if (output32Bit && inputImagePlus.getBitDepth() != 32)
            ImageTypeConverter.process(inputImagePlus, 32, ImageTypeConverter.ScalingModes.CLIP);
            
        // Updating value if taken from a measurement
        switch (valueSource) {
            case ValueSources.MEASUREMENT:
                Image imageForMeasurement = workspace.getImage(imageForMeasurementName);
                mathValue = imageForMeasurement.getMeasurement(measurement).getValue();
                break;
        }

        process(inputImagePlus, calculationType, mathValue);

        // If the image is being saved as a new image, adding it to the workspace
        if (applyToInput) {
            // Reapplying the image in case it was an ImgLib2
            inputImage.setImagePlus(inputImagePlus);
            if (showOutput)
                inputImage.show();
        } else {
            Image outputImage = ImageFactory.createImage(outputImageName, inputImagePlus);
            workspace.addImage(outputImage);
            if (showOutput)
                outputImage.show();
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new BooleanP(APPLY_TO_INPUT, this, true));
        parameters.add(new OutputImageP(OUTPUT_IMAGE, this));
        parameters.add(new BooleanP(OUTPUT_32BIT, this, false));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));
        parameters.add(new ChoiceP(VALUE_SOURCE, this, ValueSources.FIXED, ValueSources.ALL));
        parameters.add(new InputImageP(IMAGE_FOR_MEASUREMENT, this));
        parameters.add(new ImageMeasurementP(MEASUREMENT, this));
        parameters.add(new DoubleP(MATH_VALUE, this, 1.0));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        Workspace workspace = null;
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(INPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(APPLY_TO_INPUT));

        if (!(boolean) parameters.getValue(APPLY_TO_INPUT, workspace))
            returnedParameters.add(parameters.getParameter(OUTPUT_IMAGE));
        returnedParameters.add(parameters.getParameter(OUTPUT_32BIT));

        returnedParameters.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParameters.add(parameters.getParameter(CALCULATION_MODE));
        switch ((String) parameters.getValue(CALCULATION_MODE, workspace)) {
            case CalculationModes.ADD:
            case CalculationModes.DIVIDE:
            case CalculationModes.MULTIPLY:
            case CalculationModes.SUBTRACT:
                returnedParameters.add(parameters.getParameter(VALUE_SOURCE));
                switch ((String) parameters.getValue(VALUE_SOURCE, workspace)) {
                    case ValueSources.FIXED:

                        returnedParameters.add(parameters.getParameter(MATH_VALUE));

                        break;

                    case ValueSources.MEASUREMENT:
                        returnedParameters.add(parameters.getParameter(IMAGE_FOR_MEASUREMENT));
                        returnedParameters.add(parameters.getParameter(MEASUREMENT));

                        if (parameters.getValue(INPUT_IMAGE, workspace) != null) {
                            ImageMeasurementP measurement = parameters.getParameter(MEASUREMENT);
                            measurement.setImageName(parameters.getValue(IMAGE_FOR_MEASUREMENT, workspace));
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
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

        parameters.get(OUTPUT_32BIT).setDescription(
                "When enabled, the calculation will be performed on 32-bit float values.  This is useful if the calculation is likely to create negative or decimal values.  The output image will also be stored in the workspace as a 32-bit float image.");

        parameters.get(CALCULATION_MODE).setDescription(
                "Controls the mathematical operation being applied to all pixels of this image.  Choices are: "
                        + String.join(", ", CalculationModes.ALL));

        parameters.get(VALUE_SOURCE).setDescription(
                "For calculations that require a specific value (i.e. addition, subtraction, etc.) this parameter controls how the value is defined:<br><ul>"

                        + "<li>\"" + ValueSources.FIXED + "\" A fixed value is specified using the \"" + MATH_VALUE
                        + "\" parameter.  This value is the same for all images processed by this module..</li>"

                        + "<li>\"" + ValueSources.MEASUREMENT
                        + "\" The value is taken from a measurement associated with the input image.  Values obtained in this way can be different from image to image.</li></ul>");

        parameters.get(IMAGE_FOR_MEASUREMENT).setDescription("If \"" + VALUE_SOURCE + "\" is set to \""
                + ValueSources.MEASUREMENT
                + "\", this is the image that the measurement will be taken from.  It can be any image in the workspace, not necessarily the image to which the math operation is being applied.");

        parameters.get(MEASUREMENT).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.MEASUREMENT
                + "\", this is the measurement associated with the image specified by \"" + IMAGE_FOR_MEASUREMENT
                + "\" that will be used in the calculation.");

        parameters.get(MATH_VALUE).setDescription("If \"" + VALUE_SOURCE + "\" is set to \"" + ValueSources.FIXED
                + "\", this is the value that will be used in the calculation.");

    }
}
