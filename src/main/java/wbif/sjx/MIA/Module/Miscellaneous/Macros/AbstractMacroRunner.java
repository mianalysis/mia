package wbif.sjx.MIA.Module.Miscellaneous.Macros;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import ij.measure.ResultsTable;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.Text.DoubleP;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;

public abstract class AbstractMacroRunner extends Module {    
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_TYPE = "Variable type";
    public static final String VARIABLE_CHECKBOX = "Variable checkbox";
    public static final String VARIABLE_NUMERIC_VALUE = "Variable numeric value";
    public static final String VARIABLE_TEXT_VALUE = "Variable text value";

    public static final String ADD_VARIABLE = "Add variable";

    public interface VariableTypes {
        String BOOLEAN = "Boolean";
        String NUMBER = "Number";
        String TEXT = "Text";

        String[] ALL = new String[] { BOOLEAN, NUMBER, TEXT };

    }

    protected AbstractMacroRunner(String name, ModuleCollection modules) {
        super(name, modules);
    }

    public static String getFullName(String assignedName) {
        return "MACRO // " + assignedName;
    }

    public static String addVariables(String macroString, ParameterGroup group) {
        StringBuilder sb = new StringBuilder();

        LinkedHashMap<Integer, ParameterCollection> collections = group.getCollections(false);
        for (ParameterCollection collection : collections.values()) {
            String name = collection.getValue(VARIABLE_NAME);
            String type = collection.getValue(VARIABLE_TYPE);
            String value = "";

            switch (type) {
                case VariableTypes.BOOLEAN:
                    value = (boolean) collection.getValue(VARIABLE_CHECKBOX) ? "true" : "false";
                    break;
                case VariableTypes.NUMBER:
                    value = collection.getValue(VARIABLE_NUMERIC_VALUE).toString();
                    break;
                case VariableTypes.TEXT:
                    value = "\"" + collection.getValue(VARIABLE_TEXT_VALUE) + "\"";
                    break;
            }

            // Adding this variable into the code
            sb.append(name);
            sb.append("=");
            sb.append(value);
            sb.append(";\n");

        }

    sb.append(macroString);

    return sb.toString();

    }

    public static LinkedHashSet<String> expectedMeasurements(ParameterGroup group, String measurementHeading) {
        LinkedHashSet<String> addedMeasurements = new LinkedHashSet<>();

        LinkedHashMap<Integer, ParameterCollection> collections = group.getCollections(false);
        for (ParameterCollection collection : collections.values()) {
            String heading = collection.getValue(measurementHeading);
            addedMeasurements.add(heading);
        }

        return addedMeasurements;

    }

    public static Measurement interceptMeasurement(ResultsTable table, String heading) {
        if (table == null || table.getColumn(0) == null)
            return new Measurement(getFullName(heading), Double.NaN);

        int nRows = table.getColumn(0).length;
        double value = table.getValue(heading, nRows - 1);

        return new Measurement(getFullName(heading), value);

    }

    @Override
    protected void initialiseParameters() {        
        ParameterCollection variableCollection = new ParameterCollection();
        variableCollection.add(new ChoiceP(VARIABLE_TYPE, this, VariableTypes.TEXT, VariableTypes.ALL));
        variableCollection.add(new StringP(VARIABLE_NAME, this));
        variableCollection.add(new BooleanP(VARIABLE_CHECKBOX, this, true));
        variableCollection.add(new DoubleP(VARIABLE_NUMERIC_VALUE, this, 0d));
        variableCollection.add(new StringP(VARIABLE_TEXT_VALUE, this));

        parameters.add(new ParameterGroup(ADD_VARIABLE, this, variableCollection, getUpdaterAndGetter()));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterCollection returnedParameters = new ParameterCollection();

        returnedParameters.add(parameters.getParameter(ADD_VARIABLE));

        return returnedParameters;

    }

    @Override
    public boolean verify() {
        return true;
    }

    protected void addParameterDescriptions() {
        ParameterGroup group = (ParameterGroup) parameters.get(ADD_VARIABLE);
        ParameterCollection collection = group.getTemplateParameters();
        collection.get(VARIABLE_NAME).setDescription(
                "The variable value can be accessed from within the macro by using this variable name.");

        collection.get(VARIABLE_TEXT_VALUE).setDescription("Text value assigned to this variable.");

        parameters.get(ADD_VARIABLE).setDescription(
                "Pre-define variables, which will be immediately accessible within the macro.  These can be used to provide user-controllable values to file-based macros or to prevent the need for editing macro code via the \""
                        + getName() + "\" panel.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public ParameterCollection updateAndGet(ParameterCollection params) {
                ParameterCollection returnedParameters = new ParameterCollection();

                returnedParameters.add(params.getParameter(VARIABLE_NAME));
                returnedParameters.add(params.getParameter(VARIABLE_TYPE));
                switch ((String) params.getValue(VARIABLE_TYPE)) {
                    case VariableTypes.BOOLEAN:
                        returnedParameters.add(params.getParameter(VARIABLE_CHECKBOX));
                        break;
                    case VariableTypes.NUMBER:
                        returnedParameters.add(params.getParameter(VARIABLE_NUMERIC_VALUE));
                        break;
                    case VariableTypes.TEXT:
                        returnedParameters.add(params.getParameter(VARIABLE_TEXT_VALUE));
                        break;
                }

                return returnedParameters;

            }
        };
    }
}
