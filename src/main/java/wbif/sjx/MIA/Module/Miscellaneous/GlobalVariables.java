package wbif.sjx.MIA.Module.Miscellaneous;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup.ParameterUpdaterAndGetter;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class GlobalVariables extends Module {
    public static final String ADD_NEW_VARIABLE = "Add new variable";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String CONTROL_TYPE = "Control type";
    public static final String VARIABLE_VALUE = "Variable value";
    public static final String VARIABLE_CHOICES = "Variable choices";
    public static final String VARIABLE_CHOICE = "Variable choice";
    public static final String STORE_AS_METADATA_ITEM = "Store as metadata item";

    private static final HashMap<StringP, String> globalVariables = new HashMap<>();

    public interface ControlTypes {
        String CHOICE = "Choice";
        String TEXT = "Text";

        String[] ALL = new String[] { CHOICE, TEXT };

    }

    public GlobalVariables(ModuleCollection modules) {
        super("Global variables", modules);
    }

    public static String convertString(String string, ModuleCollection modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            for (StringP nameP : globalVariables.keySet()) {
                if (nameP.getValue().equals(metadataName)) {
                    String value = globalVariables.get(nameP);
                    string = string.replace(fullName, value);
                    break;
                }
            }
        }

        return string;

    }

    public static boolean variablesPresent(String string, ModuleCollection modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        // Re-compiling the matcher
        matcher = pattern.matcher(string);
        if (matcher.find()) {
            String metadataName = matcher.group(1);

            boolean found = false;
            for (StringP name : globalVariables.keySet()) {
                if (name.getValue().equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current parameter wasn't found, return false
            return found;

        }

        return true;

    }

    public static boolean containsValue(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static void updateVariables(ModuleCollection modules) {
        // Reset global variables
        globalVariables.clear();
        for (Module module : modules.values()) {
            if (module instanceof GlobalVariables && module.isEnabled()) {
                module.updateAndGetParameters();
            }
        }
    }

    public static int count() {
        return globalVariables.size();
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected Status process(Workspace workspace) {
        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_NEW_VARIABLE);

        for (ParameterCollection collection : collections.values()) {
            if ((boolean) collection.getValue(STORE_AS_METADATA_ITEM)) {
                String variableName = collection.getValue(VARIABLE_NAME);
                String controlType = collection.getValue(CONTROL_TYPE);

                switch (controlType) {
                    case ControlTypes.CHOICE:
                        workspace.getMetadata().put(variableName, collection.getValue(VARIABLE_CHOICE));
                        break;
                    case ControlTypes.TEXT:
                        workspace.getMetadata().put(variableName, collection.getValue(VARIABLE_VALUE));
                        break;
                }
            }
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        ParameterCollection parameterCollection = new ParameterCollection();
        parameterCollection.add(new StringP(VARIABLE_NAME, this));
        parameterCollection.add(new ChoiceP(CONTROL_TYPE, this, ControlTypes.TEXT, ControlTypes.ALL));
        parameterCollection.add(new StringP(VARIABLE_VALUE, this));
        parameterCollection.add(new StringP(VARIABLE_CHOICES, this));
        parameterCollection.add(new ChoiceP(VARIABLE_CHOICE, this, "", new String[0]));
        parameterCollection.add(new BooleanP(STORE_AS_METADATA_ITEM, this, false));

        parameters.add(new ParameterGroup(ADD_NEW_VARIABLE, this, parameterCollection, 1, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        ParameterGroup group = parameters.getParameter(ADD_NEW_VARIABLE);
        if (group == null)
            return parameters;

        LinkedHashMap<Integer, ParameterCollection> collections = group.getCollections(false);
        for (ParameterCollection collection : collections.values()) {
            StringP variableName = (StringP) collection.get(VARIABLE_NAME);
            if (isEnabled()) {
                switch ((String) collection.getValue(CONTROL_TYPE)) {
                    case ControlTypes.CHOICE:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_CHOICE));
                        break;
                    case ControlTypes.TEXT:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_VALUE));
                        break;
                }

            } else if (globalVariables.containsKey(variableName)) {
                globalVariables.remove(variableName);
            }
        }

        return parameters;

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
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        LinkedHashMap<Integer, ParameterCollection> collections = parameters.getValue(ADD_NEW_VARIABLE);

        for (ParameterCollection collection : collections.values()) {
            if ((boolean) collection.getValue(STORE_AS_METADATA_ITEM))
                returnedRefs.add(metadataRefs.getOrPut(collection.getValue(VARIABLE_NAME)));
        }

        return returnedRefs;

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
        parameters.get(ADD_NEW_VARIABLE).setDescription(
                "Add a new global variable.  Added variables can be removed using the \"Remove\" button.");

        ParameterCollection collection = ((ParameterGroup) parameters.get(ADD_NEW_VARIABLE)).getTemplateParameters();

        collection.get(VARIABLE_NAME).setDescription(
                "Name of this variable.  This is the name that will be used when referring to the variable in place of fixed values.  To refer to variables, use the form \"V{[VARIABLE_NAME]}\", where \"[VARIABLE_NAME]\" is replaced by the variable name.  For example, a variable called \"my_var\" would be referred to using the text \"V{my_var}\".");

        collection.get(VARIABLE_CHOICES)
                .setDescription("When \"" + CONTROL_TYPE + "\" is set to \"" + ControlTypes.CHOICE
                        + "\" mode, these are the options that will be presented by the \"" + VARIABLE_CHOICE
                        + "\" drop-down parameter.  Choices are specified as a comma-separated list.");

        collection.get(CONTROL_TYPE).setDescription("Controls how the variable is specified:<br><ul>"

                + "<li>\"" + ControlTypes.CHOICE
                + "\" Select the output variable from a pre-determined list of options (specified with the \""
                + VARIABLE_CHOICES + "\" parameter).</li>"

                + "<li>\"" + ControlTypes.TEXT + "\" Specify a fixed text value for this variable using the \""
                + VARIABLE_VALUE + "\" parameter.</li></ul>");

        collection.get(VARIABLE_VALUE).setDescription("Fixed value for the corresponding global variable when \""
                + CONTROL_TYPE + "\" is in \"" + ControlTypes.TEXT + "\" mode.");

        collection.get(VARIABLE_CHOICE)
                .setDescription("Pre-defined list of choices to select global variable value from when \""
                        + CONTROL_TYPE + "\" is in \"" + ControlTypes.CHOICE + "\" mode.");

        collection.get(STORE_AS_METADATA_ITEM).setDescription(
                "When selected, the variable will be stored as a metadata item.  This allows it to be exported to the final spreadsheet.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public ParameterCollection updateAndGet(ParameterCollection params) {
                ParameterCollection returnedParameters = new ParameterCollection();

                returnedParameters.add(params.getParameter(VARIABLE_NAME));
                returnedParameters.add(params.getParameter(CONTROL_TYPE));
                switch ((String) params.getValue(CONTROL_TYPE)) {
                    case ControlTypes.CHOICE:
                        returnedParameters.add(params.getParameter(VARIABLE_CHOICES));
                        returnedParameters.add(params.getParameter(VARIABLE_CHOICE));
                        String variableChoices = params.getValue(VARIABLE_CHOICES);
                        String[] choices = variableChoices.split(",");
                        ((ChoiceP) params.getParameter(VARIABLE_CHOICE)).setChoices(choices);
                        break;
                    case ControlTypes.TEXT:
                        returnedParameters.add(params.getParameter(VARIABLE_VALUE));
                        break;
                }
                returnedParameters.add(params.getParameter(STORE_AS_METADATA_ITEM));

                return returnedParameters;

            }
        };
    }
}
