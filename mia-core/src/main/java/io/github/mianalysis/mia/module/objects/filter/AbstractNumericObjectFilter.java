package io.github.mianalysis.mia.module.objects.filter;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ImageMeasurementP;
import io.github.mianalysis.mia.object.parameters.InputImageP;
import io.github.mianalysis.mia.object.parameters.ObjectMeasurementP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.ParentObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.DoubleP;
import io.github.mianalysis.mia.object.parameters.text.StringP;

public abstract class AbstractNumericObjectFilter extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_MODE = "Reference mode";
    public static final String REFERENCE_VALUE = "Reference value";
    public static final String REFERENCE_VAL_IMAGE = "Reference value image";
    public static final String REFERENCE_IMAGE_MEASUREMENT = "Reference image measurement";
    public static final String REFERENCE_VAL_PARENT_OBJECT = "Reference value parent object";
    public static final String REFERENCE_OBJECT_MEASUREMENT = "Reference object measurement";
    public static final String REFERENCE_MULTIPLIER = "Reference value multiplier";

    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String USE_FIXED_SUMMARY_NAME = "Use fixed summary name";
    public static final String SUMMARY_NAME = "Summary name";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";
    public static final String USE_FIXED_INDIVIDUAL_NAME = "Use fixed individual name";
    public static final String INDIVIDUAL_NAME = "Individual name";

    public interface FilterMethods {
        String LESS_THAN = "Less than";
        String LESS_THAN_OR_EQUAL_TO = "Less than or equal to";
        String EQUAL_TO = "Equal to";
        String GREATER_THAN_OR_EQUAL_TO = "Greater than or equal to";
        String GREATER_THAN = "Greater than";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[] { LESS_THAN, LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO,
                GREATER_THAN, NOT_EQUAL_TO };

    }

    public interface ReferenceModes {
        String FIXED_VALUE = "Fixed value";
        String IMAGE_MEASUREMENT = "Image measurement";
        String MEASUREMENT = "Measurement (this object)";
        String PARENT_OBJECT_MEASUREMENT = "Parent object measurement";

        String[] ALL = new String[] { FIXED_VALUE, IMAGE_MEASUREMENT, MEASUREMENT, PARENT_OBJECT_MEASUREMENT };

    }

    protected AbstractNumericObjectFilter(String name, Modules modules) {
        super(name, modules);
    }

    public String getIndividualFixedValueFullName(String filterMethod, String targetName, String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + targetName + " " + filterMethodSymbol + " " + referenceValue;
    }

    public String getSummaryFixedValueFullName(String inputObjectsName, String filterMethod, String targetName,
            String referenceValue) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + targetName + " " + filterMethodSymbol + " "
                + referenceValue;
    }

    public String getIndividualImageMeasFullName(String filterMethod, String targetName, String imageName,
            String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + imageName + " " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getSummaryImageMeasFullName(String inputObjectsName, String filterMethod, String targetName,
            String imageName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + imageName + " " + targetName + " " + filterMethodSymbol
                + " " + refName;
    }

    public String getIndividualMeasFullName(String filterMethod, String targetName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getSummaryMeasFullName(String inputObjectsName, String filterMethod, String targetName,
            String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getIndividualParentMeasFullName(String filterMethod, String targetName, String parentName,
            String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // " + parentName + " " + targetName + " " + filterMethodSymbol + " " + refName;
    }

    public String getSummaryParentMeasFullName(String inputObjectsName, String filterMethod, String targetName,
            String parentName, String refName) {
        String filterMethodSymbol = getFilterMethodSymbol(filterMethod);
        return "FILTER // NUM_" + inputObjectsName + " WITH " + parentName + " " + targetName + " " + filterMethodSymbol
                + " " + refName;
    }

    public static boolean testFilter(double testValue, double referenceValue, String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return testValue < referenceValue;
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return testValue <= referenceValue;
            case FilterMethods.EQUAL_TO:
                return testValue == referenceValue;
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return testValue >= referenceValue;
            case FilterMethods.GREATER_THAN:
                return testValue > referenceValue;
            case FilterMethods.NOT_EQUAL_TO:
                return testValue != referenceValue;
        }

        return false;

    }

    public static String getFilterMethodSymbol(String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.LESS_THAN:
                return "<";
            case FilterMethods.LESS_THAN_OR_EQUAL_TO:
                return "<=";
            case FilterMethods.EQUAL_TO:
                return "==";
            case FilterMethods.GREATER_THAN_OR_EQUAL_TO:
                return ">=";
            case FilterMethods.GREATER_THAN:
                return ">";
            case FilterMethods.NOT_EQUAL_TO:
                return "!=";
        }

        return "";

    }

    public String getIndividualMeasurementName(String targetName, WorkspaceI workspace) {
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        double fixedValue = parameters.getValue(REFERENCE_VALUE, workspace);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE, workspace);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT, workspace);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT, workspace);
        String refParentMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT, workspace);

        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                return getIndividualFixedValueFullName(filterMethod, targetName, String.valueOf(fixedValue));
            case ReferenceModes.IMAGE_MEASUREMENT:
                return getIndividualImageMeasFullName(filterMethod, targetName, refImage, refImageMeas);
            case ReferenceModes.MEASUREMENT:
                return getIndividualMeasFullName(filterMethod, targetName, refParentMeas);
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                return getIndividualParentMeasFullName(filterMethod, targetName, refParent, refParentMeas);
            default:
                return "";
        }
    }

    public String getSummaryMeasurementName(String targetName, WorkspaceI workspace) {
        if ((Boolean) parameters.getValue(USE_FIXED_SUMMARY_NAME, workspace))
            return parameters.getValue(SUMMARY_NAME, workspace);        
        
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        String filterMethod = parameters.getValue(FILTER_METHOD, workspace);
        double fixedValue = parameters.getValue(REFERENCE_VALUE, workspace);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE, workspace);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT, workspace);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT, workspace);
        String refMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT, workspace);

        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                return getSummaryFixedValueFullName(inputObjectsName, filterMethod, targetName,
                        String.valueOf(fixedValue));
            case ReferenceModes.IMAGE_MEASUREMENT:
                return getSummaryImageMeasFullName(inputObjectsName, filterMethod, targetName, refImage, refImageMeas);
            case ReferenceModes.MEASUREMENT:
                return getSummaryMeasFullName(inputObjectsName, filterMethod, targetName, refMeas);
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                return getSummaryParentMeasFullName(inputObjectsName, filterMethod, targetName, refParent, refMeas);
            default:
                return "";
        }
    }

    public double getReferenceValue(WorkspaceI workspace, ObjI inputObject) {
        String referenceMode = parameters.getValue(REFERENCE_MODE, workspace);
        double fixedValue = parameters.getValue(REFERENCE_VALUE, workspace);
        String refImage = parameters.getValue(REFERENCE_VAL_IMAGE, workspace);
        String refParent = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT, workspace);
        String refImageMeas = parameters.getValue(REFERENCE_IMAGE_MEASUREMENT, workspace);
        String refMeas = parameters.getValue(REFERENCE_OBJECT_MEASUREMENT, workspace);
        double refMultiplier = parameters.getValue(REFERENCE_MULTIPLIER, workspace);

        // Getting the values to filter on
        double refValue;
        switch (referenceMode) {
            case ReferenceModes.FIXED_VALUE:
                refValue = fixedValue;
                refMultiplier = 1;
                break;
            case ReferenceModes.IMAGE_MEASUREMENT:
                refValue = workspace.getImage(refImage).getMeasurement(refImageMeas).getValue();
                break;
            case ReferenceModes.MEASUREMENT:
                refValue = inputObject.getMeasurement(refMeas).getValue();
                break;
            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                ObjI parentObject = inputObject.getParent(refParent);
                if (parentObject == null)
                    return Double.NaN;
                refValue = parentObject.getMeasurement(refMeas).getValue();
                break;
            default:
                return Double.NaN;
        }

        return refValue * refMultiplier;

    }

    @Override
    public void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new ChoiceP(REFERENCE_MODE, this, ReferenceModes.FIXED_VALUE, ReferenceModes.ALL));
        parameters.add(new DoubleP(REFERENCE_VALUE, this, 1d));
        parameters.add(new InputImageP(REFERENCE_VAL_IMAGE, this));
        parameters.add(new ImageMeasurementP(REFERENCE_IMAGE_MEASUREMENT, this));
        parameters.add(new ParentObjectsP(REFERENCE_VAL_PARENT_OBJECT, this));
        parameters.add(new ObjectMeasurementP(REFERENCE_OBJECT_MEASUREMENT, this));
        parameters.add(new DoubleP(REFERENCE_MULTIPLIER, this, 1d));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(USE_FIXED_SUMMARY_NAME, this, false));
        parameters.add(new StringP(SUMMARY_NAME, this));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));
        parameters.add(new BooleanP(USE_FIXED_INDIVIDUAL_NAME, this, false));
        parameters.add(new StringP(INDIVIDUAL_NAME, this));

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        String inputObjectsName = parameters.getValue(INPUT_OBJECTS, workspace);

        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(REFERENCE_MODE));
        switch ((String) parameters.getValue(REFERENCE_MODE, workspace)) {
            case ReferenceModes.FIXED_VALUE:
                returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                break;

            case ReferenceModes.IMAGE_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_VAL_IMAGE));
                returnedParameters.add(parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                String referenceValueImageName = parameters.getValue(REFERENCE_VAL_IMAGE, workspace);
                ((ImageMeasurementP) parameters.getParameter(REFERENCE_IMAGE_MEASUREMENT))
                        .setImageName(referenceValueImageName);
                break;

            case ReferenceModes.MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT))
                        .setObjectName(inputObjectsName);
                break;

            case ReferenceModes.PARENT_OBJECT_MEASUREMENT:
                returnedParameters.add(parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT));
                returnedParameters.add(parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT));
                returnedParameters.add(parameters.getParameter(REFERENCE_MULTIPLIER));
                String referenceValueParentObjectsName = parameters.getValue(REFERENCE_VAL_PARENT_OBJECT, workspace);
                ((ParentObjectsP) parameters.getParameter(REFERENCE_VAL_PARENT_OBJECT))
                        .setChildObjectsName(inputObjectsName);
                ((ObjectMeasurementP) parameters.getParameter(REFERENCE_OBJECT_MEASUREMENT))
                        .setObjectName(referenceValueParentObjectsName);
                break;
        }

        return returnedParameters;

    }

    public Parameters updateAndGetMeasurementParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));

        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        if ((Boolean) parameters.getValue(STORE_SUMMARY_RESULTS, null)) {
            returnedParameters.add(parameters.getParameter(USE_FIXED_SUMMARY_NAME));
            if ((Boolean) parameters.getValue(USE_FIXED_SUMMARY_NAME, null))
                returnedParameters.add(parameters.getParameter(SUMMARY_NAME));
        }

        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));
        if ((Boolean) parameters.getValue(STORE_INDIVIDUAL_RESULTS, null)) {
            returnedParameters.add(parameters.getParameter(USE_FIXED_INDIVIDUAL_NAME));
            if ((Boolean) parameters.getValue(USE_FIXED_INDIVIDUAL_NAME, null))
                returnedParameters.add(parameters.getParameter(INDIVIDUAL_NAME));
        }

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
        super.addParameterDescriptions();

        parameters.get(FILTER_METHOD).setDescription(
                "Numeric comparison used to determine which objects should be removed.  Objects with specified property (e.g. a measurement value) that passes this filter will be removed.  For example, an object with a measurement value of 7 would be removed if \""
                        + FILTER_METHOD + "\" is set to \"" + FilterMethods.LESS_THAN
                        + "\" with a reference value of 10.  Choices are: " + String.join(", ", FilterMethods.ALL)
                        + ".");

        parameters.get(REFERENCE_MODE).setDescription("Type of reference value used to compare objects to:<br><ul>"
                + "<li>\"" + ReferenceModes.FIXED_VALUE
                + "\" Objects will be compared to a single, fixed value specified using the \"" + REFERENCE_VALUE
                + "\" parameter.</li>"

                + "<li>\"" + ReferenceModes.IMAGE_MEASUREMENT
                + "\" Objects will be compared to a measurement associated with an image in the workspace.  The image and associated measurement are specified by the \""
                + REFERENCE_VAL_IMAGE + "\" and \"" + REFERENCE_IMAGE_MEASUREMENT
                + "\" parameters.  In this mode, all objects will still be compared to the same value; however, this value can vary between input files.</li>"

                + "<li>\"" + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\" Objects will be compared to a measurement associated with a parent object.  The parent object and measurement are specified by the \""
                + REFERENCE_VAL_PARENT_OBJECT + "\" and \"" + REFERENCE_OBJECT_MEASUREMENT
                + "\" parameters.  In this mode all objects can (but won't necessarily) be compared to different values.</li></ul>");

        parameters.get(REFERENCE_VALUE).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.FIXED_VALUE + "\", all objects will be compared to this fixed value.");

        parameters.get(REFERENCE_VAL_IMAGE)
                .setDescription("When \"" + REFERENCE_MODE + "\" is set to \"" + ReferenceModes.IMAGE_MEASUREMENT
                        + "\", all objects will be compared to a measurement associated with this image.");

        parameters.get(REFERENCE_IMAGE_MEASUREMENT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.IMAGE_MEASUREMENT
                + "\", all objects will be compared to this measurement, which itself is associated with the image specified by \""
                + REFERENCE_VAL_IMAGE + "\".");

        parameters.get(REFERENCE_VAL_PARENT_OBJECT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\", all objects will be compared to a measurement associated with their parent object from this collection.");

        parameters.get(REFERENCE_OBJECT_MEASUREMENT).setDescription("When \"" + REFERENCE_MODE + "\" is set to \""
                + ReferenceModes.PARENT_OBJECT_MEASUREMENT
                + "\", all objects will be compared to this measurement, which itself is associated with the parent object specified by \""
                + REFERENCE_VAL_PARENT_OBJECT + "\".");

        parameters.get(REFERENCE_MULTIPLIER).setDescription(
                "Irrespective of how the reference value was determined, it can be systematically adjusted prior to use for comparison using this multiplier.  For example, an image measurement reference value of 32 with a \""
                        + REFERENCE_MULTIPLIER
                        + "\" of \"0.5\" will see all objects compared against a value of 16.  This is useful when comparing to dynamic values coming from image and parent object measurements.");

        parameters.get(STORE_INDIVIDUAL_RESULTS).setDescription(
                "When selected, each input object will be assigned a measurement reporting if that object passed or failed the filter.  The measurement value is \"1\" for objects that failed the filter (i.e. would be removed if the relevant removal setting was enabled) and \"0\" for objects that passed (i.e. wouldn't be removed).");

        parameters.get(STORE_SUMMARY_RESULTS).setDescription(
                "When selected, a metadata value is stored in the workspace, which records the number of objects which failed the filter and were removed or moved to another object class (depending on the \""
                        + FILTER_METHOD + "\" parameter).");

    }
}