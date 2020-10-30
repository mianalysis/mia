package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ImageMeasurementP;
import wbif.sjx.MIA.Object.Parameters.InputImageP;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ImageMeasurementCalculator extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";

    public static final String VALUE_SEPARATOR_1 = "Value 1 selection";
    public static final String VALUE_MODE_1 = "Value mode 1";
    public static final String FIXED_VALUE_1 = "Fixed value 1";
    public static final String MEASUREMENT_1 = "Measurement 1";

    public static final String VALUE_SEPARATOR_2 = "Value 2 selection";
    public static final String VALUE_MODE_2 = "Value mode 2";
    public static final String FIXED_VALUE_2 = "Fixed value 2";
    public static final String MEASUREMENT_2 = "Measurement 2";

    public static final String CALCULATION_SEPARATOR = "Measurement calculation";
    public static final String OUTPUT_MEASUREMENT = "Output measurement";
    public static final String CALCULATION_MODE = "Calculation mode";

    public interface ValueModes {
        String FIXED = "Fixed";
        String MEASUREMENT = "Measurement";

        String[] ALL = new String[] { FIXED, MEASUREMENT };

    }

    public interface CalculationModes {
        String ADD = "Add value 1 and value 2";
        String DIVIDE = "Divide value 1 by value 2";
        String MULTIPLY = "Multiply value 1 and value 2";
        String SUBTRACT = "Subtract value 2 from value 1";

        String[] ALL = new String[] { ADD, DIVIDE, MULTIPLY, SUBTRACT };

    }

    public ImageMeasurementCalculator(ModuleCollection modules) {
        super("Image measurement calculator", modules);
    }

    public static double doCalculation(double value1, double value2, String calculationMode) {
        switch (calculationMode) {
            default:
                return Double.NaN;
            case CalculationModes.ADD:
                return value1 + value2;
            case CalculationModes.DIVIDE:
                return value1 / value2;
            case CalculationModes.MULTIPLY:
                return value1 * value2;
            case CalculationModes.SUBTRACT:
                return value1 - value2;
        }
    }

    public static String getFullName(String measurementName) {
        return "MEASUREMENT_CALCULATOR // " + measurementName;
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getDescription() {
        return "Perform a mathematical operation on measurements associated with an image.  The calculation can replace either or both input image measurements with fixed values.  The resulting measurement is associated with the input image as a new measurement.";

    }

    @Override
    protected Status process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);

        String valueMode1 = parameters.getValue(VALUE_MODE_1);
        double fixedValue1 = parameters.getValue(FIXED_VALUE_1);
        String measurementName1 = parameters.getValue(MEASUREMENT_1);

        String valueMode2 = parameters.getValue(VALUE_MODE_2);
        double fixedValue2 = parameters.getValue(FIXED_VALUE_2);
        String measurementName2 = parameters.getValue(MEASUREMENT_2);

        String outputMeasurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        String calculationMode = parameters.getValue(CALCULATION_MODE);

        // Getting value 1
        double value1 = 0;
        switch (valueMode1) {
            case ValueModes.FIXED:
                value1 = fixedValue1;
                break;
            case ValueModes.MEASUREMENT:
                value1 = inputImage.getMeasurement(measurementName1).getValue();
                break;
        }

        // Getting value 2
        double value2 = 0;
        switch (valueMode2) {
            case ValueModes.FIXED:
                value2 = fixedValue2;
                break;
            case ValueModes.MEASUREMENT:
                value2 = inputImage.getMeasurement(measurementName2).getValue();
                break;
        }

        // Performing calculation
        double result = doCalculation(value1, value2, calculationMode);

        // Adding the new measurement
        inputImage.addMeasurement(new Measurement(outputMeasurementName, result));

        // Showing results
        if (showOutput)
            inputImage.showMeasurements(this);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputImageP(INPUT_IMAGE, this));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(VALUE_MODE_1, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_1, this, 0));
        parameters.add(new ImageMeasurementP(MEASUREMENT_1, this));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(VALUE_MODE_2, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_2, this, 0));
        parameters.add(new ImageMeasurementP(MEASUREMENT_2, this));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParams = new ParameterCollection();

        String inputImageName = parameters.getValue(INPUT_IMAGE);

        returnedParams.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParams.add(parameters.getParameter(INPUT_IMAGE));

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_1));
        returnedParams.add(parameters.getParameter(VALUE_MODE_1));
        switch ((String) parameters.getValue(VALUE_MODE_1)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_1));
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_1));
                ((ImageMeasurementP) parameters.getParameter(MEASUREMENT_1)).setImageName(inputImageName);
                break;
        }

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_2));
        returnedParams.add(parameters.getParameter(VALUE_MODE_2));
        switch ((String) parameters.getValue(VALUE_MODE_2)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_2));
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_2));
                ((ImageMeasurementP) parameters.getParameter(MEASUREMENT_2)).setImageName(inputImageName);
                break;
        }

        returnedParams.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParams.add(parameters.getParameter(OUTPUT_MEASUREMENT));
        returnedParams.add(parameters.getParameter(CALCULATION_MODE));

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        ImageMeasurementRefCollection returnedRefs = new ImageMeasurementRefCollection();

        // Creating new MeasurementRef
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));

        returnedRefs.add(imageMeasurementRefs.getOrPut(measurementName).setImageName(inputImageName));

        return returnedRefs;

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
      parameters.get(INPUT_IMAGE).setDescription("Image from the workspace to perform the measurement calculation for.");

      parameters.get(VALUE_MODE_1).setDescription("Controls how the first value in the calculation is defined:<br><ul>"

      +"<li>\""+ValueModes.FIXED+"\" A single, fixed value defined by \""+FIXED_VALUE_1+"\"is used.</li>"

      +"<li>\""+ValueModes.MEASUREMENT+"\" A measurement associated with the input image and defined by \""+MEASUREMENT_1+"\" is used.</li></ul>");

      parameters.get(FIXED_VALUE_1).setDescription("Fixed value to use in the calculation when \""+VALUE_MODE_1+"\" is in \""+ValueModes.FIXED+"\" mode.");

      parameters.get(MEASUREMENT_1).setDescription("Measurement associated with the input image to use in the calculation when \""+VALUE_MODE_1+"\" is in \""+ValueModes.MEASUREMENT+"\" mode.");

      parameters.get(VALUE_MODE_2).setDescription("Controls how the second value in the calculation is defined:<br><ul>"

      +"<li>\""+ValueModes.FIXED+"\" A single, fixed value defined by \""+FIXED_VALUE_2+"\"is used.</li>"

      +"<li>\""+ValueModes.MEASUREMENT+"\" A measurement associated with the input image and defined by \""+MEASUREMENT_2+"\" is used.</li></ul>");

      parameters.get(FIXED_VALUE_2).setDescription("Fixed value to use in the calculation when \""+VALUE_MODE_2+"\" is in \""+ValueModes.FIXED+"\" mode.");

      parameters.get(MEASUREMENT_2).setDescription("Measurement associated with the input image to use in the calculation when \""+VALUE_MODE_2+"\" is in \""+ValueModes.MEASUREMENT+"\" mode.");

      parameters.get(OUTPUT_MEASUREMENT).setDescription("The value resulting from the calculation will be stored as a new measurement with this name.  This output measurement will be associated with the input image");

      parameters.get(CALCULATION_MODE).setDescription("Calculation to perform.  Choices are: "+ String.join(", ",CalculationModes.ALL) + ".");

    }
}
