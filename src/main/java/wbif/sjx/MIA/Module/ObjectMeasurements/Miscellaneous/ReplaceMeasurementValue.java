package wbif.sjx.MIA.Module.ObjectMeasurements.Miscellaneous;

import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.FilterObjects.AbstractNumericObjectFilter;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;
import wbif.sjx.MIA.Object.Parameters.ObjectMeasurementP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.SeparatorP;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class ReplaceMeasurementValue extends Module {
    public static final String INPUT_SEPARATOR = "Object/measurement input";
    public static final String INPUT_OBJECTS = "Input objects";
    public static final String MEASUREMENT = "Measurement";

    public static final String REPLACEMENT_SEPARATOR = "Measurement replacement";
    public static final String REPLACEMENT_CONDITION = "Replacement condition";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REPLACEMENT_VALUE_TYPE = "Replacement value type";
    public static final String REPLACEMENT_VALUE = "Replacement value";

    public interface ReplacementConditions {
        String IS_INFINITE = "Is infinite";
        String IS_NOT_INFINITE = "Is not infinite";
        String IS_NAN = "Is NaN";
        String IS_NOT_NAN = "Is not NaN";
        String LESS_THAN = "Less than";
        String LESS_THAN_OR_EQUAL_TO = "Less than or equal to";
        String EQUAL_TO = "Equal to";
        String GREATER_THAN_OR_EQUAL_TO = "Greater than or equal to";
        String GREATER_THAN = "Greater than";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[] { IS_NAN, IS_NOT_NAN, LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO,
                GREATER_THAN_OR_EQUAL_TO, GREATER_THAN, NOT_EQUAL_TO };

    }

    public interface ReplacementValueTypes {
        String NUMBER = "Number";
        String NAN = "NaN (not a number)";
        String NEGATIVE_INFINITY = "Negative infinity";
        String POSITIVE_INFINITY = "Positive infinity";

        String[] ALL = new String[] { NUMBER, NAN, NEGATIVE_INFINITY, POSITIVE_INFINITY };

    }

    public ReplaceMeasurementValue(ModuleCollection modules) {
        super("Replace measurement value", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.OBJECT_MEASUREMENTS_MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "Replaces measurement values matching specific criteria with different values.  This can be used to replace instances of NaN (not a number) values with numeric values or to replace all measurement values failing a numeric test (e.g. less than) with, for example, NaN.<br><br>Note: \"NaN\" stands for \"Not a Number\" and can arise from certain calculations (e.g. division of 0 by 0) or if a measurement couldn't be made (e.g. fitting an ellipse to an object with too few coordinates).";
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);

        String measurementName = parameters.getValue(MEASUREMENT);
        String replacementCondition = parameters.getValue(REPLACEMENT_CONDITION);
        double referenceValue = parameters.getValue(REFERENCE_VALUE);
        String replacementValueType = parameters.getValue(REPLACEMENT_VALUE_TYPE);
        double replacementValue = parameters.getValue(REPLACEMENT_VALUE);

        for (Obj inputObject : inputObjects.values()) {
            Measurement measurement = inputObject.getMeasurement(measurementName);
            if (measurement == null)
                continue;

            double currentValue = measurement.getValue();

            boolean replace = false;
            switch (replacementCondition) {
                case ReplacementConditions.IS_INFINITE:
                    replace = Double.isInfinite(currentValue);
                    break;
                case ReplacementConditions.IS_NOT_INFINITE:
                    replace = !Double.isInfinite(currentValue);
                    break;
                case ReplacementConditions.IS_NAN:
                    replace = Double.isNaN(currentValue);
                    break;
                case ReplacementConditions.IS_NOT_NAN:
                    replace = !Double.isNaN(currentValue);
                    break;
                case ReplacementConditions.EQUAL_TO:
                case ReplacementConditions.GREATER_THAN:
                case ReplacementConditions.GREATER_THAN_OR_EQUAL_TO:
                case ReplacementConditions.LESS_THAN:
                case ReplacementConditions.LESS_THAN_OR_EQUAL_TO:
                case ReplacementConditions.NOT_EQUAL_TO:
                    replace = AbstractNumericObjectFilter.testFilter(currentValue, referenceValue,
                            replacementCondition);
                    break;
            }

            if (replace) {
                switch (replacementValueType) {
                    case ReplacementValueTypes.NUMBER:
                        measurement.setValue(replacementValue);
                        break;
                    case ReplacementValueTypes.NAN:
                        measurement.setValue(Double.NaN);
                        break;
                    case ReplacementValueTypes.NEGATIVE_INFINITY:
                        measurement.setValue(Double.NEGATIVE_INFINITY);
                        break;
                    case ReplacementValueTypes.POSITIVE_INFINITY:
                        measurement.setValue(Double.POSITIVE_INFINITY);
                        break;
                }
            }
        }

        if (showOutput) inputObjects.showMeasurements(this, modules);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this));
        parameters.add(new ObjectMeasurementP(MEASUREMENT, this));

        parameters.add(new SeparatorP(REPLACEMENT_SEPARATOR, this));
        parameters
                .add(new ChoiceP(REPLACEMENT_CONDITION, this, ReplacementConditions.IS_NAN, ReplacementConditions.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 0d));
        parameters.add(
                new ChoiceP(REPLACEMENT_VALUE_TYPE, this, ReplacementValueTypes.NUMBER, ReplacementValueTypes.ALL));
        parameters.add(new DoubleP(REPLACEMENT_VALUE, this, 0));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS);

        ParameterCollection returnedParams = new ParameterCollection();

        returnedParams.add(parameters.get(INPUT_SEPARATOR));
        returnedParams.add(parameters.get(INPUT_OBJECTS));
        returnedParams.add(parameters.get(MEASUREMENT));

        returnedParams.add(parameters.get(REPLACEMENT_SEPARATOR));
        returnedParams.add(parameters.get(REPLACEMENT_CONDITION));
        switch ((String) parameters.getValue(REPLACEMENT_CONDITION)) {
            case ReplacementConditions.EQUAL_TO:
            case ReplacementConditions.GREATER_THAN:
            case ReplacementConditions.GREATER_THAN_OR_EQUAL_TO:
            case ReplacementConditions.LESS_THAN:
            case ReplacementConditions.LESS_THAN_OR_EQUAL_TO:
            case ReplacementConditions.NOT_EQUAL_TO:
                returnedParams.add(parameters.get(REFERENCE_VALUE));
                break;
        }

        returnedParams.add(parameters.get(REPLACEMENT_VALUE_TYPE));
        switch ((String) parameters.getValue(REPLACEMENT_VALUE_TYPE)) {
            case ReplacementValueTypes.NUMBER:
                returnedParams.add(parameters.get(REPLACEMENT_VALUE));
                break;
        }

        ObjectMeasurementP measurementParameter = parameters.getParameter(MEASUREMENT);
        measurementParameter.setObjectName(inputObjectsName);

        return returnedParams;

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
      parameters.get(INPUT_OBJECTS).setDescription("Objects from the workspace for which specific measurement values will be replaced.  Any object measurements with values matching the specified criteria will be replaced by another specified value.");

      parameters.get(MEASUREMENT).setDescription("Measurement associated with the input objects.  Any object measurements with values matching the specified criteria will be replaced by another specified value.");

      parameters.get(REPLACEMENT_CONDITION).setDescription("Controls under what condition the input object measurement (specified by \""+MEASUREMENT+"\") will be replaced by the specified value.  Note: \"NaN\" stands for \"Not a Number\" and can arise from certain calculations (e.g. division of 0 by 0) or if a measurement couldn't be made (e.g. fitting an ellipse to an object with too few coordinates).  Choices are: "+ String.join(", ", ReplacementConditions.ALL)+".");

      parameters.get(REFERENCE_VALUE).setDescription("If \""+REPLACEMENT_CONDITION+"\" is set to a numeric condition (e.g. less than), this value will be used as the threshold against which the measurement value will be tested.");

      parameters.get(REPLACEMENT_VALUE_TYPE).setDescription("Controls what type of value any measurements identified for replacement will be replaced by:<br><ul>"

      +"<li>\""+ReplacementValueTypes.NAN+"\" Measurement values will be replaced by NaN (not a number).</li>"

      +"<li>\""+ReplacementValueTypes.NUMBER+"\" Measurement values will be replaced by the numeric value specified by \""+REPLACEMENT_VALUE+"\".</li>"

      +"<li>\""+ReplacementValueTypes.NEGATIVE_INFINITY+"\" Measurement values will be replaced by the specific \"negative infinity\" value.</li>"

      +"<li>\""+ReplacementValueTypes.POSITIVE_INFINITY+"\" Measurement values will be replaced by the specific \"positive infinity\" value.</li></ul>");

      parameters.get(REPLACEMENT_VALUE).setDescription("Value to replace identified measurements with if \""+REPLACEMENT_VALUE_TYPE+"\" is set to \""+ReplacementValueTypes.NUMBER+"\" mode.");

    }
}
