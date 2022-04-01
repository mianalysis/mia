package io.github.mianalysis.mia.module.objects.measure.miscellaneous;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.measurements.Measurement;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.common.mathfunc.CumStat;

/**
 * Created by Stephen Cross on 19/03/2019.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ObjectMeasurementCalculator extends Module {
    public static final String INPUT_SEPARATOR = "Object input";
    public static final String INPUT_OBJECTS = "Input objects";

    public static final String VALUE_SEPARATOR_1 = "Value 1 selection";
    public static final String VALUE_MODE_1 = "Value mode 1";
    public static final String FIXED_VALUE_1 = "Fixed value 1";
    public static final String IMAGE_1 = "Image 1";
    public static final String IMAGE_MEASUREMENT_1 = "Image measurement 1";
    public static final String MEASUREMENT_1 = "Measurement 1";
    public static final String REFERENCE_OBJECTS_1 = "Reference objects 1";
    public static final String REFERENCE_MEASUREMENT_1 = "Reference measurement 1";
    public static final String STATISTIC_MODE_1 = "Statistic mode 1";

    public static final String VALUE_SEPARATOR_2 = "Value 2 selection";
    public static final String VALUE_MODE_2 = "Value mode 2";
    public static final String FIXED_VALUE_2 = "Fixed value 2";
    public static final String IMAGE_2 = "Image 2";
    public static final String IMAGE_MEASUREMENT_2 = "Image measurement 2";
    public static final String MEASUREMENT_2 = "Measurement 2";
    public static final String REFERENCE_OBJECTS_2 = "Reference objects 2";
    public static final String REFERENCE_MEASUREMENT_2 = "Reference measurement 2";
    public static final String STATISTIC_MODE_2 = "Statistic mode 2";

    public static final String CALCULATION_SEPARATOR = "Measurement calculation";
    public static final String OUTPUT_MEASUREMENT = "Output measurement";
    public static final String CALCULATION_MODE = "Calculation mode";

    public interface ValueModes {
        String FIXED = "Fixed";
        String IMAGE_MEASUREMENT = "Image measurement";
        String MEASUREMENT = "Measurement";
        String OBJECT_COLLECTION_STATISTIC = "Object collection statistic";

        String[] ALL = new String[] { FIXED, IMAGE_MEASUREMENT, MEASUREMENT, OBJECT_COLLECTION_STATISTIC };

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

    public ObjectMeasurementCalculator(Modules modules) {
        super("Object measurement calculator", modules);
    }

    public static String getFullName(String measurementName) {
        return "MEASUREMENT_CALCULATOR // " + measurementName;
    }

    public static double getObjectCollectionStatistic(Objs objects, String measurementName, String statistic) {
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

    static double getFirstValue(Objs objects, String measurementName) {
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

    static double getLastValue(Objs objects, String measurementName) {
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
    public Category getCategory() {
        return Categories.OBJECTS_MEASURE_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Perform a mathematical operation on measurements associated with each object of an object collection in the workspace.  The calculation can replace either or both values with fixed values, measurements associated with an image or a statistic of all measurements associated with another object collection (e.g. the mean volume of all objects).  The resulting measurements are associated with the corresponding input objects as new measurements.";
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);

        String valueMode1 = parameters.getValue(VALUE_MODE_1);
        double fixedValue1 = parameters.getValue(FIXED_VALUE_1);
        String imageName1 = parameters.getValue(IMAGE_1);
        String imageMeasurement1 = parameters.getValue(IMAGE_MEASUREMENT_1);
        String measurementName1 = parameters.getValue(MEASUREMENT_1);
        String refObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1);
        String refMeasurementName1 = parameters.getValue(REFERENCE_MEASUREMENT_1);
        String statisticMode1 = parameters.getValue(STATISTIC_MODE_1);

        String valueMode2 = parameters.getValue(VALUE_MODE_2);
        double fixedValue2 = parameters.getValue(FIXED_VALUE_2);
        String imageName2 = parameters.getValue(IMAGE_2);
        String imageMeasurement2 = parameters.getValue(IMAGE_MEASUREMENT_2);
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
            Objs refObjects1 = workspace.getObjectSet(refObjectsName1);
            refValue1 = getObjectCollectionStatistic(refObjects1, refMeasurementName1, statisticMode1);
        }
        if (valueMode2.equals(ValueModes.OBJECT_COLLECTION_STATISTIC)) {
            Objs refObjects2 = workspace.getObjectSet(refObjectsName2);
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
                case ValueModes.IMAGE_MEASUREMENT:
                    value1 = workspace.getImage(imageName1).getMeasurement(imageMeasurement1).getValue();
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
                case ValueModes.IMAGE_MEASUREMENT:
                    value2 = workspace.getImage(imageName2).getMeasurement(imageMeasurement2).getValue();
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

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(VALUE_MODE_1, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_1, this, 0));
        parameters.add(new InputImageP(IMAGE_1, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_1, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_1, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_1, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_1, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_1, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(VALUE_MODE_2, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_2, this, 0));
        parameters.add(new InputImageP(IMAGE_2, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_2, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT_2, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_2, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_2, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_2, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        Parameters returnedParams = new Parameters();

        returnedParams.add(parameters.getParameter(INPUT_SEPARATOR));
        returnedParams.add(parameters.getParameter(INPUT_OBJECTS));

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_1));
        returnedParams.add(parameters.getParameter(VALUE_MODE_1));
        switch ((String) parameters.getValue(VALUE_MODE_1)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_1));
                break;

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_1));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_1));
                String imageName1 = parameters.getValue(IMAGE_1);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_1)).setImageName(imageName1);
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

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_2));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_2));
                String imageName2 = parameters.getValue(IMAGE_2);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_2)).setImageName(imageName2);
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
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        ObjMeasurementRefs returnedRefs = new ObjMeasurementRefs();

        // Creating new MeasurementRef
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        String measurementName = getFullName(parameters.getValue(OUTPUT_MEASUREMENT));
        returnedRefs.add(objectMeasurementRefs.getOrPut(measurementName).setObjectsName(inputObjectsName));

        return returnedRefs;

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
        parameters.get(INPUT_OBJECTS).setDescription(
                "Object collection from the workspace to perform the measurement calculation for.  The specified calculation will be performed once per object in the collection.");

        parameters.get(VALUE_MODE_1)
                .setDescription("Controls how the first value in the calculation is defined:<br><ul>"

                        + "<li>\"" + ValueModes.FIXED + "\" A single, fixed value defined by \"" + FIXED_VALUE_1
                        + "\"is used.</li>"

                        + "<li>\"" + ValueModes.IMAGE_MEASUREMENT
                        + "\" A measurement associated with an image (specified by \"" + IMAGE_1
                        + "\") and defined by \"" + IMAGE_MEASUREMENT_1 + "\" is used.</li>"

                        + "<li>\"" + ValueModes.MEASUREMENT
                        + "\" A measurement associated with the input object and defined by \"" + MEASUREMENT_1
                        + "\" is used.</li>"

                        + "<li>\"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" A statistic (specified by \""
                        + STATISTIC_MODE_1 + "\") of a measurement (specified by \"" + REFERENCE_MEASUREMENT_1
                        + "\") associated with all objects in an object collection (specified by \""
                        + REFERENCE_OBJECTS_1
                        + "\") is used.  For example, the mean object volume.  Note: The object collection used to calculate the statistic doesn't have to be the same as the input object collection.</li></ul>");

        parameters.get(FIXED_VALUE_1).setDescription("Fixed value to use in the calculation when \"" + VALUE_MODE_1
                + "\" is in \"" + ValueModes.FIXED + "\" mode.");

        parameters.get(IMAGE_1)
                .setDescription("A measurement associated with this image will be used in the calculated when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(IMAGE_MEASUREMENT_1)
                .setDescription("Measurement associated with an image (specified by \"" + IMAGE_1
                        + "\") to use in the calculation when \"" + VALUE_MODE_1 + "\" is in \""
                        + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(MEASUREMENT_1)
                .setDescription("Measurement, associated with the current object, to use in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.MEASUREMENT + "\" mode.");

        parameters.get(REFERENCE_OBJECTS_1).setDescription(
                "Object collection for which a statistic of an associated measurement will be used in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(REFERENCE_MEASUREMENT_1)
                .setDescription("Measurement associated with the objects in the collection specified by \""
                        + REFERENCE_OBJECTS_1
                        + "\".  A statistic of all object measurements will be used in the calculation when \""
                        + VALUE_MODE_1 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(STATISTIC_MODE_1)
                .setDescription("Statistic to apply to all measurements (specified by \"" + REFERENCE_MEASUREMENT_1
                        + "\") of an object collection (specified by \"" + REFERENCE_OBJECTS_1
                        + "\").  The resulting value will be used in the calculation when \"" + VALUE_MODE_1
                        + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.  Choices are: "
                        + String.join(", ", StatisticModes.ALL) + ".");

        parameters.get(VALUE_MODE_2)
                .setDescription("Controls how the second value in the calculation is defined:<br><ul>"

                        + "<li>\"" + ValueModes.FIXED + "\" A single, fixed value defined by \"" + FIXED_VALUE_2
                        + "\"is used.</li>"

                        + "<li>\"" + ValueModes.IMAGE_MEASUREMENT
                        + "\" A measurement associated with an image (specified by \"" + IMAGE_2
                        + "\") and defined by \"" + IMAGE_MEASUREMENT_2 + "\" is used.</li>"

                        + "<li>\"" + ValueModes.MEASUREMENT
                        + "\" A measurement associated with the input object and defined by \"" + MEASUREMENT_2
                        + "\" is used.</li>"

                        + "<li>\"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" A statistic (specified by \""
                        + STATISTIC_MODE_2 + "\") of a measurement (specified by \"" + REFERENCE_MEASUREMENT_2
                        + "\") associated with all objects in an object collection (specified by \""
                        + REFERENCE_OBJECTS_2
                        + "\") is used.  For example, the mean object volume.  Note: The object collection used to calculate the statistic doesn't have to be the same as the input object collection.</li></ul>");

        parameters.get(FIXED_VALUE_2).setDescription("Fixed value to use in the calculation when \"" + VALUE_MODE_2
                + "\" is in \"" + ValueModes.FIXED + "\" mode.");

        parameters.get(IMAGE_2)
                .setDescription("A measurement associated with this image will be used in the calculated when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(IMAGE_MEASUREMENT_2)
                .setDescription("Measurement associated with an image (specified by \"" + IMAGE_2
                        + "\") to use in the calculation when \"" + VALUE_MODE_2 + "\" is in \""
                        + ValueModes.IMAGE_MEASUREMENT + "\" mode.");

        parameters.get(MEASUREMENT_2)
                .setDescription("Measurement, associated with the current object, to use in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.MEASUREMENT + "\" mode.");

        parameters.get(REFERENCE_OBJECTS_2).setDescription(
                "Object collection for which a statistic of an associated measurement will be used in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(REFERENCE_MEASUREMENT_2)
                .setDescription("Measurement associated with the objects in the collection specified by \""
                        + REFERENCE_OBJECTS_2
                        + "\".  A statistic of all object measurements will be used in the calculation when \""
                        + VALUE_MODE_2 + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.");

        parameters.get(STATISTIC_MODE_2)
                .setDescription("Statistic to apply to all measurements (specified by \"" + REFERENCE_MEASUREMENT_2
                        + "\") of an object collection (specified by \"" + REFERENCE_OBJECTS_2
                        + "\").  The resulting value will be used in the calculation when \"" + VALUE_MODE_2
                        + "\" is in \"" + ValueModes.OBJECT_COLLECTION_STATISTIC + "\" mode.  Choices are: "
                        + String.join(", ", StatisticModes.ALL) + ".");

        parameters.get(OUTPUT_MEASUREMENT).setDescription(
                "The value resulting from the calculation will be stored as a new measurement with this name.  This output measurement will be associated with the corresponding object from the input object collection.");

        parameters.get(CALCULATION_MODE).setDescription(
                "Calculation to perform.  Choices are: " + String.join(", ", CalculationModes.ALL) + ".");

    }
}
