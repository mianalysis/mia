package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.common.MathFunc.CumStat;
import wbif.sjx.MIA.Object.Workspace;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
public class ObjectMeasurementCalculator extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String VALUE_SEPARATOR_1 = "Value 1 selection";
    public static final String VALUE_MODE_1 = "Value mode 1";
    public static final String FIXED_VALUE_1 = "Fixed value 1";
    public static final String MEASUREMENT_1 = "Measurement 1";
    public static final String REFERENCE_OBJECTS_1 = "Reference objects 1";
    public static final String REFERENCE_MEASUREMENT_1 = "Reference measurement 1";
    public static final String STATISTIC_MODE_1 = "Statistic mode 1";

    public static final String VALUE_SEPARATOR_2 = "Value 2 selection";
    public static final String VALUE_MODE_2 = "Value mode 2";
    public static final String FIXED_VALUE_2 = "Fixed value 2";
    public static final String MEASUREMENT_2 = "Measurement 2";
    public static final String REFERENCE_OBJECTS_2 = "Reference objects 2";
    public static final String REFERENCE_MEASUREMENT_2 = "Reference measurement 2";
    public static final String STATISTIC_MODE_2 = "Statistic mode 2";

    public static final String CALCULATION_SEPARATOR = "Measurement calculation";
    public static final String OUTPUT_MEASUREMENT = "Output measurement";
    public static final String CALCULATION_MODE = "Calculation mode";

    public interface ValueModes {
        String FIXED = "Fixed";
        String MEASUREMENT = "Measurement";
        String OBJECT_COLLECTION_STATISTIC = "Object collection statistic";

        String[] ALL = new String[] { FIXED, MEASUREMENT, OBJECT_COLLECTION_STATISTIC };

    }

    public interface StatisticModes {
        String FIRST = "First value";
        String LAST = "Last value";
        String MIN = "Minimum";
        String MEAN = "Mean";
        String MAX = "Maximum";
        String RANGE = "Range";
        String STDEV = "Standard deviation";
        String SUM = "Sum";

        String[] ALL = new String[] { FIRST, LAST, MIN, MEAN, MAX, RANGE, STDEV, SUM };

    }

    public interface CalculationModes {
        String ADD = "Add measurement 1 and measurement 2";
        String DIVIDE = "Divide measurement 1 by measurement 2";
        String MULTIPLY = "Multiply measurement 1 and measurement 2";
        String SUBTRACT = "Subtract measurement 2 from measurement 1";

        String[] ALL = new String[] { ADD, DIVIDE, MULTIPLY, SUBTRACT };

    }

    public ObjectMeasurementCalculator(ModuleCollection modules) {
        super("Object measurement calculator", modules);
    }

    public static String getFullName(String measurementName) {
        return "MEASUREMENT_CALCULATOR // " + measurementName;
    }

    public static double getObjectCollectionStatistic(ObjCollection objects, String measurementName, String statistic) {
        // The first and last values are calculated slightly differently
        switch (statistic) {
            case StatisticModes.FIRST:
                return getFirstValue(objects, measurementName);
            case StatisticModes.LAST:
                return getLastValue(objects, measurementName);
        }

        CumStat cs = new CumStat();

        // Creating statistics calculator
        for (Obj object : objects.values()) {
            Measurement measurement = object.getMeasurement(measurementName);
            if (measurement == null)
                continue;

            cs.addMeasure(measurement.getValue());

        }

        switch (statistic) {
            default:
                return Double.NaN;
            case StatisticModes.MAX:
                return cs.getMax();
            case StatisticModes.MEAN:
                return cs.getMean();
            case StatisticModes.MIN:
                return cs.getMin();
            case StatisticModes.RANGE:
                return cs.getMax() - cs.getMin();
            case StatisticModes.STDEV:
                return cs.getStd();
            case StatisticModes.SUM:
                return cs.getSum();
        }
    }

    static double getFirstValue(ObjCollection objects, String measurementName) {
        CumStat cs = new CumStat();

        int minFrame = Integer.MAX_VALUE;

        // Creating statistics calculator
        for (Obj object : objects.values()) {
            int t = object.getT();

            if (t < minFrame) {
                // Create a new CumStat for this timepoint
                minFrame = t;
                cs = new CumStat();
                cs.addMeasure(object.getMeasurement(measurementName).getValue());
            } else if (t == minFrame) {
                cs.addMeasure(object.getMeasurement(measurementName).getValue());
            }
        }

        return cs.getMean();

    }

    static double getLastValue(ObjCollection objects, String measurementName) {
        CumStat cs = new CumStat();

        int maxFrame = -Integer.MAX_VALUE;

        // Creating statistics calculator
        for (Obj object : objects.values()) {
            int t = object.getT();

            if (t > maxFrame) {
                // Create a new CumStat for this timepoint
                maxFrame = t;
                cs = new CumStat();
                cs.addMeasure(object.getMeasurement(measurementName).getValue());
            } else if (t == maxFrame) {
                cs.addMeasure(object.getMeasurement(measurementName).getValue());
            }
        }

        return cs.getMean();

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

    @Override
    public String getPackageName() {
        return PackageNames.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String valueMode1 = parameters.getValue(VALUE_MODE_1);
        double fixedValue1 = parameters.getValue(FIXED_VALUE_1);
        String measurementName1 = parameters.getValue(MEASUREMENT_1);
        String refObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1);
        String refMeasurementName1 = parameters.getValue(REFERENCE_MEASUREMENT_1);
        String statisticMode1 = parameters.getValue(STATISTIC_MODE_1);

        String valueMode2 = parameters.getValue(VALUE_MODE_2);
        double fixedValue2 = parameters.getValue(FIXED_VALUE_2);
        String measurementName2 = parameters.getValue(MEASUREMENT_2);
        String refObjectsName2 = parameters.getValue(REFERENCE_OBJECTS_2);
        String refMeasurementName2 = parameters.getValue(REFERENCE_MEASUREMENT_2);
        String statisticMode2 = parameters.getValue(STATISTIC_MODE_2);

        String outputMeasurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        String calculationMode = parameters.getValue(CALCULATION_MODE);

        // Getting reference object collections
        double refValue1 = Double.NaN;
        double refValue2 = Double.NaN;
        if (valueMode1.equals(ValueModes.OBJECT_COLLECTION_STATISTIC)) {
            ObjCollection refObjects1 = workspace.getObjectSet(refObjectsName1);
            refValue1 = getObjectCollectionStatistic(refObjects1, refMeasurementName1, statisticMode1);
        }
        if (valueMode2.equals(ValueModes.OBJECT_COLLECTION_STATISTIC)) {
            ObjCollection refObjects2 = workspace.getObjectSet(refObjectsName2);
            refValue2 = getObjectCollectionStatistic(refObjects2, refMeasurementName2, statisticMode2);
        }

        // Getting measurements
        for (Obj inputObject : inputObjects.values()) {
            // Getting value 1
            double value1 = 0;
            switch (valueMode1) {
                case ValueModes.FIXED:
                    value1 = fixedValue1;
                    break;
                case ValueModes.MEASUREMENT:
                    value1 = inputObject.getMeasurement(measurementName1).getValue();
                    break;
                case ValueModes.OBJECT_COLLECTION_STATISTIC:
                    value1 = refValue1;
                    break;
            }

            // Getting value 2
            double value2 = 0;
            switch (valueMode2) {
                case ValueModes.FIXED:
                    value2 = fixedValue2;
                    break;
                case ValueModes.MEASUREMENT:
                    value2 = inputObject.getMeasurement(measurementName2).getValue();
                    break;
                case ValueModes.OBJECT_COLLECTION_STATISTIC:
                    value2 = refValue2;
                    break;
            }

            // Performing calculation
            double result = doCalculation(value1, value2, calculationMode);

            // Assigning measurement
            inputObject.addMeasurement(new Measurement(outputMeasurementName, result));

        }

        // Showing results
        if (showOutput)
            inputObjects.showMeasurements(this, modules);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new ParamSeparatorP(VALUE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(VALUE_MODE_1, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_1, this, 0));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_1, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_1, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_1, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_1, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new ParamSeparatorP(VALUE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(VALUE_MODE_2, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_2, this, 0));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_2, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_2, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_2, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_2, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new ParamSeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParams = new ParameterCollection();

        returnedParams.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParams.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_1));
        returnedParams.add(parameters.getParameter(VALUE_MODE_1));
        switch ((String) parameters.getValue(VALUE_MODE_1)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_1));
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_1));
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_1)).setObjectName(inputObjectsName);
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_1));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_1));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_1));
                String referenceObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_1))
                        .setObjectName(referenceObjectsName1);
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
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_2)).setObjectName(inputObjectsName);
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_2));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_2));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_2));
                String referenceObjectsName2 = parameters.getValue(REFERENCE_OBJECTS_2);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_2))
                        .setObjectName(referenceObjectsName2);
                break;
        }

        returnedParams.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParams.add(parameters.getParameter(OUTPUT_MEASUREMENT));
        returnedParams.add(parameters.getParameter(CALCULATION_MODE));

        return returnedParams;

    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefCollection returnedRefs = new ObjMeasurementRefCollection();

        // Creating new MeasurementRef
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String measurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        returnedRefs.add(objectMeasurementRefs.getOrPut(measurementName).setObjectsName(inputObjectsName));

        return returnedRefs;

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
