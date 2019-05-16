package wbif.sjx.MIA.Module.ImageMeasurements;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ImageMeasurementCalculator extends Module {
    public static final String INPUT_IMAGE = "Input image";
    public static final String MEASUREMENT_1 = "Measurement 1";
    public static final String MEASUREMENT_2 = "Measurement 2";
    public static final String OUTPUT_MEASUREMENT = "Output measurement";
    public static final String CALCULATION_MODE = "Calculation mode";

    public ImageMeasurementCalculator(ModuleCollection modules) {
        super(modules);
    }


    public interface CalculationModes {
        String ADD = "Add measurement 1 and measurement 2";
        String DIVIDE = "Divide measurement 1 by measurement 2";
        String MULTIPLY = "Multiply measurement 1 and measurement 2";
        String SUBTRACT = "Subtract measurement 2 from measurement 1";

        String[] ALL = new String[]{ADD,DIVIDE,MULTIPLY,SUBTRACT};

    }


    public static void doCalculation(Image image, String calculationMode, String measurementName1, String measurementName2, String outputMeasurement) {
        Measurement measurement1 = image.getMeasurement(measurementName1);
        Measurement measurement2 = image.getMeasurement(measurementName2);

        // If either measurement is missing, assign Double.NaN
        if (measurement1 == null || measurement2 == null) {
            image.addMeasurement(new Measurement(outputMeasurement,Double.NaN));
            return;
        }

        double measurementValue1 = measurement1.getValue();
        double measurementValue2 = measurement2.getValue();
        double outputMeasurementValue = Double.NaN;

        switch (calculationMode) {
            case CalculationModes.ADD:
                outputMeasurementValue = measurementValue1 + measurementValue2;
                break;

            case CalculationModes.DIVIDE:
                outputMeasurementValue = measurementValue1 / measurementValue2;
                break;

            case CalculationModes.MULTIPLY:
                outputMeasurementValue = measurementValue1 * measurementValue2;
                break;

            case CalculationModes.SUBTRACT:
                outputMeasurementValue = measurementValue1 - measurementValue2;
                break;
        }

        // Adding the new measurement
        image.addMeasurement(new Measurement(outputMeasurement,outputMeasurementValue));

    }

    public static String getFullName(String measurementName) {
        return "MEASUREMENT_CALCULATOR // " + measurementName;
    }

    @Override
    public String getTitle() {
        return "Image measurement calculator";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_MEASUREMENTS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        Image inputImage = workspace.getImage(inputImageName);
        String measurementName1 = parameters.getValue(MEASUREMENT_1);
        String measurementName2 = parameters.getValue(MEASUREMENT_2);
        String outputMeasurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        String calculationMode = parameters.getValue(CALCULATION_MODE);

        // Getting measurements
        doCalculation(inputImage,calculationMode,measurementName1,measurementName2,outputMeasurementName);

        // Showing results
        if (showOutput) inputImage.showMeasurements(this);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE, this));
        parameters.add(new ImageMeasurementP(MEASUREMENT_1, this));
        parameters.add(new ImageMeasurementP(MEASUREMENT_2, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        ImageMeasurementP measurement1 = parameters.getParameter(MEASUREMENT_1);
        measurement1.setImageName(inputImageName);

        ImageMeasurementP measurement2 = parameters.getParameter(MEASUREMENT_2);
        measurement2.setImageName(inputImageName);

        return parameters;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        imageMeasurementRefs.setAllAvailable(false);

        // Creating new MeasurementRef
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        String measurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));

        imageMeasurementRefs.getOrPut(measurementName).setImageName(inputImageName).setAvailable(true);

        return imageMeasurementRefs;

    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
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
