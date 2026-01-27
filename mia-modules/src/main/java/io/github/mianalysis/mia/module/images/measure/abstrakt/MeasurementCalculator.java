package io.github.mianalysis.mia.module.images.measure.abstrakt;

import java.util.Collection;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.measurements.MeasurementI;
import io.github.mianalysis.mia.object.measurements.MeasurementFactories;
import io.github.mianalysis.mia.object.measurements.MeasurementProvider;
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
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.process.math.CumStat;

/**
 * Created by Stephen Cross on 14/11/2024.
 */

public abstract class MeasurementCalculator extends Module {
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

    protected enum ImageObjectMode {
        IMAGE, OBJECT;
    }

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
        String ADD = "Add value 1 and value 2";
        String DIFFERENCE = "Difference of value 1 and value 2";
        String DIVIDE = "Divide value 1 by value 2";
        String EQUAL_TO = "Value 1 == value 2";
        String GREATER_THAN = "Value 1 > value 2";
        String GREATER_THAN_OR_EQUAL_TO = "Value 1 >= value 2";
        String LESS_THAN = "Value 1 < value 2";
        String LESS_THAN_OR_EQUAL_TO = "Value 1 <= value 2";
        String MAX = "Maximum of value 1 and value 2";
        String MEAN = "Mean of value 1 and value 2";
        String MIN = "Minimum of value 1 and value 2";
        String NOT_EQUAL_TO = "Value 1 != value 2";
        String MULTIPLY = "Multiply value 1 and value 2";
        String SUBTRACT = "Subtract value 2 from value 1";

        String[] ALL = new String[] { ADD, DIFFERENCE, DIVIDE, EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
                LESS_THAN, LESS_THAN_OR_EQUAL_TO, MAX, MEAN, MIN, MULTIPLY, NOT_EQUAL_TO, SUBTRACT };

    }

    public MeasurementCalculator(String name, Modules modules) {
        super(name, modules);
    }

    public static double getObjectCollectionStatistic(ObjsI objects, String measurementName, String statistic) {
        // The first and last values are calculated slightly differently
        switch (statistic) {
            case StatisticModes.FIRST:
                return getFirstValue(objects, measurementName);
            case StatisticModes.LAST:
                return getLastValue(objects, measurementName);
        }

        CumStat cs = new CumStat();

        // Creating statistics calculator
        for (ObjI object : objects.values()) {
            MeasurementI measurement = object.getMeasurement(measurementName);
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

    static double getFirstValue(ObjsI objects, String measurementName) {
        CumStat cs = new CumStat();

        int minFrame = Integer.MAX_VALUE;

        // Creating statistics calculator
        for (ObjI object : objects.values()) {
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

    static double getLastValue(ObjsI objects, String measurementName) {
        CumStat cs = new CumStat();

        int maxFrame = -Integer.MAX_VALUE;

        // Creating statistics calculator
        for (ObjI object : objects.values()) {
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
            case CalculationModes.DIFFERENCE:
                return Math.abs(value1 - value2);
            case CalculationModes.DIVIDE:
                return value1 / value2;
            case CalculationModes.EQUAL_TO:
                return value1 == value2 ? 1 : 0;
            case CalculationModes.GREATER_THAN:
                return value1 > value2 ? 1 : 0;
            case CalculationModes.GREATER_THAN_OR_EQUAL_TO:
                return value1 >= value2 ? 1 : 0;
            case CalculationModes.LESS_THAN:
                return value1 < value2 ? 1 : 0;
            case CalculationModes.LESS_THAN_OR_EQUAL_TO:
                return value1 <= value2 ? 1 : 0;
            case CalculationModes.MAX:
                return Math.max(value1, value2);
            case CalculationModes.MEAN:
                return (value1 + value2) / 2;
            case CalculationModes.MIN:
                return Math.min(value1, value2);
            case CalculationModes.MULTIPLY:
                return value1 * value2;
            case CalculationModes.NOT_EQUAL_TO:
                return value1 != value2 ? 1 : 0;
            case CalculationModes.SUBTRACT:
                return value1 - value2;

        }
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    public Status process(WorkspaceI workspace, ImageObjectMode imageObjectMode,
            Collection<? extends MeasurementProvider> inputImageObjects) {

        String valueMode1 = parameters.getValue(VALUE_MODE_1, workspace);
        double fixedValue1 = parameters.getValue(FIXED_VALUE_1, workspace);
        String imageName1 = parameters.getValue(IMAGE_1, workspace);
        String imageMeasurement1 = parameters.getValue(IMAGE_MEASUREMENT_1, workspace);
        String measurementName1 = parameters.getValue(MEASUREMENT_1, workspace);
        String refObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1, workspace);
        String refMeasurementName1 = parameters.getValue(REFERENCE_MEASUREMENT_1, workspace);
        String statisticMode1 = parameters.getValue(STATISTIC_MODE_1, workspace);

        String valueMode2 = parameters.getValue(VALUE_MODE_2, workspace);
        double fixedValue2 = parameters.getValue(FIXED_VALUE_2, workspace);
        String imageName2 = parameters.getValue(IMAGE_2, workspace);
        String imageMeasurement2 = parameters.getValue(IMAGE_MEASUREMENT_2, workspace);
        String measurementName2 = parameters.getValue(MEASUREMENT_2, workspace);
        String refObjectsName2 = parameters.getValue(REFERENCE_OBJECTS_2, workspace);
        String refMeasurementName2 = parameters.getValue(REFERENCE_MEASUREMENT_2, workspace);
        String statisticMode2 = parameters.getValue(STATISTIC_MODE_2, workspace);

        String outputMeasurementName = parameters.getValue(OUTPUT_MEASUREMENT, workspace);
        String calculationMode = parameters.getValue(CALCULATION_MODE, workspace);

        // Getting reference object collections
        double refValue1 = Double.NaN;
        double refValue2 = Double.NaN;
        if (valueMode1.equals(ValueModes.OBJECT_COLLECTION_STATISTIC)) {
            ObjsI refObjects1 = workspace.getObjects(refObjectsName1);
            refValue1 = getObjectCollectionStatistic(refObjects1, refMeasurementName1, statisticMode1);
        }
        if (valueMode2.equals(ValueModes.OBJECT_COLLECTION_STATISTIC)) {
            ObjsI refObjects2 = workspace.getObjects(refObjectsName2);
            refValue2 = getObjectCollectionStatistic(refObjects2, refMeasurementName2, statisticMode2);
        }

        for (MeasurementProvider inputImageObject : inputImageObjects) {
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
                    value1 = inputImageObject.getMeasurement(measurementName1).getValue();
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
                    value2 = inputImageObject.getMeasurement(measurementName2).getValue();
                    break;
                case ValueModes.OBJECT_COLLECTION_STATISTIC:
                    value2 = refValue2;
                    break;
            }

            // Performing calculation
            double result = doCalculation(value1, value2, calculationMode);

            // Assigning measurement
            inputImageObject.addMeasurement(MeasurementFactories.getDefaultFactory().createMeasurement(outputMeasurementName, result));

        }

        return Status.PASS;

    }

    protected void initialiseParameters(ImageObjectMode imageObjectMode) {
        parameters.add(new SeparatorP(VALUE_SEPARATOR_1, this));
        parameters.add(new ChoiceP(VALUE_MODE_1, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_1, this, 0));
        parameters.add(new InputImageP(IMAGE_1, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_1, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_1, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_1, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_1, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(VALUE_SEPARATOR_2, this));
        parameters.add(new ChoiceP(VALUE_MODE_2, this, ValueModes.MEASUREMENT, ValueModes.ALL));
        parameters.add(new DoubleP(FIXED_VALUE_2, this, 0));
        parameters.add(new InputImageP(IMAGE_2, this));
        parameters.add(new ImageMeasurementP(IMAGE_MEASUREMENT_2, this));
        parameters.add(new InputObjectsP(REFERENCE_OBJECTS_2, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_MEASUREMENT_2, this));
        parameters.add(new ChoiceP(STATISTIC_MODE_2, this, StatisticModes.MIN, StatisticModes.ALL));

        parameters.add(new SeparatorP(CALCULATION_SEPARATOR, this));
        parameters.add(new StringP(OUTPUT_MEASUREMENT, this));
        parameters.add(new ChoiceP(CALCULATION_MODE, this, CalculationModes.ADD, CalculationModes.ALL));

        switch (imageObjectMode) {
            case IMAGE:
                parameters.add(new ImageMeasurementP(MEASUREMENT_1, this));
                parameters.add(new ImageMeasurementP(MEASUREMENT_2, this));
                break;
            case OBJECT:
                parameters.add(new ObjectMeasurementP(MEASUREMENT_1, this));
                parameters.add(new ObjectMeasurementP(MEASUREMENT_2, this));
                break;
        }

        addParameterDescriptions();

    }

    public Parameters updateAndGetParameters(ImageObjectMode imageObjectMode, String inputImageObjectName) {
        WorkspaceI workspace = null;
        Parameters returnedParams = new Parameters();

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_1));
        returnedParams.add(parameters.getParameter(VALUE_MODE_1));
        switch ((String) parameters.getValue(VALUE_MODE_1, workspace)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_1));
                break;

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_1));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_1));
                String imageName1 = parameters.getValue(IMAGE_1, workspace);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_1)).setImageName(imageName1);
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_1));
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_1));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_1));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_1));
                String referenceObjectsName1 = parameters.getValue(REFERENCE_OBJECTS_1, workspace);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_1))
                        .setObjectName(referenceObjectsName1);
                break;
        }

        returnedParams.add(parameters.getParameter(VALUE_SEPARATOR_2));
        returnedParams.add(parameters.getParameter(VALUE_MODE_2));
        switch ((String) parameters.getValue(VALUE_MODE_2, workspace)) {
            case ValueModes.FIXED:
                returnedParams.add(parameters.getParameter(FIXED_VALUE_2));
                break;

            case ValueModes.IMAGE_MEASUREMENT:
                returnedParams.add(parameters.getParameter(IMAGE_2));
                returnedParams.add(parameters.getParameter(IMAGE_MEASUREMENT_2));
                String imageName2 = parameters.getValue(IMAGE_2, workspace);
                ((ImageMeasurementP) parameters.get(IMAGE_MEASUREMENT_2)).setImageName(imageName2);
                break;

            case ValueModes.MEASUREMENT:
                returnedParams.add(parameters.getParameter(MEASUREMENT_2));
                break;

            case ValueModes.OBJECT_COLLECTION_STATISTIC:
                returnedParams.add(parameters.getParameter(REFERENCE_OBJECTS_2));
                returnedParams.add(parameters.getParameter(REFERENCE_MEASUREMENT_2));
                returnedParams.add(parameters.getParameter(STATISTIC_MODE_2));
                String referenceObjectsName2 = parameters.getValue(REFERENCE_OBJECTS_2, workspace);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_MEASUREMENT_2))
                        .setObjectName(referenceObjectsName2);
                break;
        }

        returnedParams.add(parameters.getParameter(CALCULATION_SEPARATOR));
        returnedParams.add(parameters.getParameter(OUTPUT_MEASUREMENT));
        returnedParams.add(parameters.getParameter(CALCULATION_MODE));

        switch (imageObjectMode) {
            case IMAGE:
                ((ImageMeasurementP) parameters.getParameter(MEASUREMENT_1)).setImageName(inputImageObjectName);
                ((ImageMeasurementP) parameters.getParameter(MEASUREMENT_2)).setImageName(inputImageObjectName);
                break;
            case OBJECT:
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_1)).setObjectName(inputImageObjectName);
                ((ObjectMeasurementP) parameters.getParameter(MEASUREMENT_2)).setObjectName(inputImageObjectName);
                break;
        }

        return returnedParams;

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
        parameters.get(VALUE_MODE_1)
                .setDescription("Controls how the first value in the calculation is defined:<br><ul>"

                        + "<li>\"" + ValueModes.FIXED + "\" A single, fixed value defined by \"" + FIXED_VALUE_1
                        + "\"is used.</li>"

                        + "<li>\"" + ValueModes.IMAGE_MEASUREMENT
                        + "\" A measurement associated with an image (specified by \"" + IMAGE_1
                        + "\") and defined by \"" + IMAGE_MEASUREMENT_1 + "\" is used.</li>"

                        + "<li>\"" + ValueModes.MEASUREMENT
                        + "\" A measurement associated with the input image/object and defined by \"" + MEASUREMENT_1
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
                .setDescription(
                        "Measurement, associated with the current image/object, to use in the calculation when \""
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
                        + "\" A measurement associated with the input image/object and defined by \"" + MEASUREMENT_2
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
                .setDescription(
                        "Measurement, associated with the current image/object, to use in the calculation when \""
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
                "The value resulting from the calculation will be stored as a new measurement with this name.  This output measurement will be associated with the corresponding image/object.");

        parameters.get(CALCULATION_MODE).setDescription(
                "Calculation to perform.  Choices are: " + String.join(", ", CalculationModes.ALL) + ".");

    }
}
