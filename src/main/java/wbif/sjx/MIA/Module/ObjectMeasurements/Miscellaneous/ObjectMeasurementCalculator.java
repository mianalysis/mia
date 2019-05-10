package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.MeasurementRef;
import wbif.sjx.MIA.Object.References.MeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ObjectMeasurementCalculator extends Module {
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASUREMENT_1 = "Measurement 1";
    public static final String MEASUREMENT_2 = "Measurement 2";
    public static final String OUTPUT_MEASUREMENT = "Output measurement";
    public static final String CALCULATION_MODE = "Calculation mode";

    public ObjectMeasurementCalculator(ModuleCollection modules) {
        super(modules);
    }


    public interface CalculationModes {
        String ADD = "Add measurement 1 and measurement 2";
        String DIVIDE = "Divide measurement 1 by measurement 2";
        String MULTIPLY = "Multiply measurement 1 and measurement 2";
        String SUBTRACT = "Subtract measurement 2 from measurement 1";

        String[] ALL = new String[]{ADD,DIVIDE,MULTIPLY,SUBTRACT};

    }


    public static void doCalculation(Obj object, String calculationMode, String measurementName1, String measurementName2, String outputMeasurement) {
        Measurement measurement1 = object.getMeasurement(measurementName1);
        Measurement measurement2 = object.getMeasurement(measurementName2);

        // If either measurement is missing, assign Double.NaN
        if (measurement1 == null || measurement2 == null) {
            object.addMeasurement(new Measurement(outputMeasurement,Double.NaN));
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
        object.addMeasurement(new Measurement(outputMeasurement,outputMeasurementValue));

    }

    public static String getFullName(String measurementName) {
        return "MEASUREMENT_CALCULATOR // " + measurementName;
    }

    @Override
    public String getTitle() {
        return "Object measurement calculator";
    }

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        String measurementName1 = parameters.getValue(MEASUREMENT_1);
        String measurementName2 = parameters.getValue(MEASUREMENT_2);
        String outputMeasurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        String calculationMode = parameters.getValue(CALCULATION_MODE);

        // Getting measurements
        for (Obj inputObject:inputObjects.values()) {
            doCalculation(inputObject,calculationMode,measurementName1,measurementName2,outputMeasurementName);
        }

        // Showing results
        if (showOutput) inputObjects.showMeasurements(this,workspace.getAnalysis().getModules());

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_2, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ObjectMeasurementP measurement1 = parameters.getParameter(MEASUREMENT_1);
        measurement1.setObjectName(inputObjectsName);

        ObjectMeasurementP measurement2 = parameters.getParameter(MEASUREMENT_2);
        measurement2.setObjectName(inputObjectsName);

        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        objectMeasurementRefs.setAllAvailable(false);

        // Creating new MeasurementRef
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        MeasurementRef.Type type = MeasurementRef.Type.OBJECT;

        String measurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        objectMeasurementRefs.getOrPut(measurementName,type).setImageObjName(inputObjectsName).setAvailable(true);

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
