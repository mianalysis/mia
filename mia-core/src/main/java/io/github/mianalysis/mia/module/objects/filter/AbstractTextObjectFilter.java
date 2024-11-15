package io.github.mianalysis.mia.module.objects.filter;

import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;

public abstract class AbstractTextObjectFilter extends AbstractObjectFilter {
    public static final String FILTER_SEPARATOR = "Object filtering";
    public static final String FILTER_METHOD = "Method for filtering";
    public static final String REFERENCE_VALUE = "Reference value";

    public static final String MEASUREMENT_SEPARATOR = "Measurement output";
    public static final String STORE_SUMMARY_RESULTS = "Store summary filter results";
    public static final String STORE_INDIVIDUAL_RESULTS = "Store individual filter results";

    public interface FilterMethods {
        String CONTAINS = "Contains";
        String DOES_NOT_CONTAIN = "Does not contain";
        String EQUAL_TO = "Equal to";
        String NOT_EQUAL_TO = "Not equal to";

        String[] ALL = new String[] { CONTAINS, DOES_NOT_CONTAIN, EQUAL_TO, NOT_EQUAL_TO };

    }

    protected AbstractTextObjectFilter(String name, Modules modules) {
        super(name, modules);
    }

    public String getIndividualFullName(String filterMethod, String targetName, String referenceValue) {
        return "FILTER // " + targetName + " " + filterMethod.toLowerCase() + " " + referenceValue;
    }

    public String getSummaryFullName(String inputObjectsName, String filterMethod, String targetName,
            String referenceValue) {
        return "FILTER // NUM_" + inputObjectsName + " WHERE " + targetName + " " + filterMethod.toLowerCase() + " "
                + referenceValue;
    }

    public static boolean testFilter(String testValue, String referenceValue, String filterMethod) {
        switch (filterMethod) {
            case FilterMethods.CONTAINS:
                return testValue.contains(referenceValue);
            case FilterMethods.DOES_NOT_CONTAIN:
                return !testValue.contains(referenceValue);
            case FilterMethods.EQUAL_TO:
                return testValue.equals(referenceValue);
            case FilterMethods.NOT_EQUAL_TO:
                return !testValue.equals(referenceValue);
        }

        return false;

    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();

        parameters.add(new SeparatorP(FILTER_SEPARATOR, this));
        parameters.add(new ChoiceP(FILTER_METHOD, this, FilterMethods.EQUAL_TO, FilterMethods.ALL));
        parameters.add(new StringP(REFERENCE_VALUE, this));

        parameters.add(new SeparatorP(MEASUREMENT_SEPARATOR, this));
        parameters.add(new BooleanP(STORE_SUMMARY_RESULTS, this, false));
        parameters.add(new BooleanP(STORE_INDIVIDUAL_RESULTS, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        Parameters returnedParameters = new Parameters();
        returnedParameters.addAll(super.updateAndGetParameters());

        returnedParameters.add(parameters.getParameter(FILTER_SEPARATOR));
        returnedParameters.add(parameters.getParameter(FILTER_METHOD));
        returnedParameters.add(parameters.getParameter(REFERENCE_VALUE));
                
        return returnedParameters;

    }

    public Parameters updateAndGetMeasurementParameters() {
        Parameters returnedParameters = new Parameters();

        returnedParameters.add(parameters.getParameter(MEASUREMENT_SEPARATOR));
        returnedParameters.add(parameters.getParameter(STORE_SUMMARY_RESULTS));
        returnedParameters.add(parameters.getParameter(STORE_INDIVIDUAL_RESULTS));

        return returnedParameters;

    }

    @Override
    protected void addParameterDescriptions() {
    }
}