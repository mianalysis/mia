package io.github.mianalysis.mia.module.system;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.FilePathP;
import io.github.mianalysis.mia.object.parameters.FolderPathP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.ParameterGroup.ParameterUpdaterAndGetter;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.parameters.text.TextAreaP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Create fixed values which can be accessed by text entry parameters from all
 * modules. Use of global variables allows the same value to be used across
 * multiple different modules without the need to explicitly type it. Global
 * variables are accessed with the notation "V{[NAME]}", where "[NAME]" is
 * replaced with the name of the relevant variable. Global variables can be used
 * by any text or numeric parameter.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GlobalVariables extends Module {
    public static final String VARIABLE_SEPARATOR = "Variable settings";

    /**
     * Add a new global variable. Added variables can be removed using the "Remove"
     * button.
     */
    public static final String ADD_NEW_VARIABLE = "Add new variable";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_TYPE = "Variable type";
    public static final String VARIABLE_BOOLEAN = "Variable Boolean";
    public static final String VARIABLE_VALUE = "Variable value";
    public static final String VARIABLE_FILE = "Variable file";
    public static final String VARIABLE_FOLDER = "Variable folder";
    public static final String VARIABLE_CHOICES = "Variable choices";
    public static final String VARIABLE_CHOICE = "Variable choice";
    public static final String VARIABLE_TEXT_AREA = "Variable text";
    public static final String STORE_AS_METADATA_ITEM = "Store as metadata item";

    protected static final HashMap<StringP, String> globalVariables = new HashMap<>();

    public interface VariableTypes {
        String BOOLEAN = "Boolean";
        String CHOICE = "Choice";
        String FILE = "File";
        String FOLDER = "Folder";
        String TEXT = "Text";
        String TEXT_AREA = "Text area";

        String[] ALL = new String[] { BOOLEAN, CHOICE, FILE, FOLDER, TEXT, TEXT_AREA };

    }

    protected GlobalVariables(String name, ModulesI modules) {
        super(name, modules);
    }

    public GlobalVariables(ModulesI modules) {
        super("Global variables", modules);
    }

    public static String convertString(String string, ModulesI modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String variableName = matcher.group(1);

            for (StringP nameP : globalVariables.keySet()) {
                if (nameP.getValue(null).equals(variableName)) {
                    String value = globalVariables.get(nameP);
                    string = string.replace(fullName, value);
                    break;
                }
            }
        }

        return string;

    }

    public static boolean variablesPresent(String string, ModulesI modules) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        // Re-compiling the matcher
        matcher = pattern.matcher(string);
        if (matcher.find()) {
            String metadataName = matcher.group(1);

            boolean found = false;
            for (StringP name : globalVariables.keySet()) {
                if (name.getValue(null).equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current parameter wasn't found, return false
            return found;

        }

        return true;

    }

    public static HashMap<StringP, String> getGlobalVariables() {
        return globalVariables;
    }

    public static boolean containsValue(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static void updateVariables(ModulesI modules) {
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
    public Category getCategory() {
        return Categories.SYSTEM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Create fixed values which can be accessed by text entry parameters from all modules.  Use of global variables allows the same value to be used across multiple different modules without the need to explicitly type it.  Global variables are accessed with the notation \"V{[NAME]}\", where \"[NAME]\" is replaced with the name of the relevant variable.  Global variables can be used by any text or numeric parameter.";
    }

    @Override
    public Status process(WorkspaceI workspace) {
        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_NEW_VARIABLE, workspace);

        for (Parameters collection : collections.values()) {
            if ((boolean) collection.getValue(STORE_AS_METADATA_ITEM, workspace)) {
                String variableName = collection.getValue(VARIABLE_NAME, workspace);
                String variableType = collection.getValue(VARIABLE_TYPE, workspace);

                switch (variableType) {
                    case VariableTypes.BOOLEAN:
                        workspace.getMetadata().put(variableName,
                                collection.getValue(VARIABLE_BOOLEAN, workspace).toString());
                        break;
                    case VariableTypes.CHOICE:
                        workspace.getMetadata().put(variableName, collection.getValue(VARIABLE_CHOICE, workspace));
                        break;
                    case VariableTypes.FILE:
                        String path = collection.getValue(VARIABLE_FILE, workspace);
                        path = path.replace("\\", "\\\\");
                        workspace.getMetadata().put(variableName, path);
                        break;
                    case VariableTypes.FOLDER:
                        path = collection.getValue(VARIABLE_FOLDER, workspace);
                        path = path.replace("\\", "\\\\");
                        workspace.getMetadata().put(variableName, path);
                        break;
                    case VariableTypes.TEXT:
                        workspace.getMetadata().put(variableName, collection.getValue(VARIABLE_VALUE, workspace));
                        break;
                    case VariableTypes.TEXT_AREA:
                        workspace.getMetadata().put(variableName, collection.getValue(VARIABLE_TEXT_AREA, workspace));
                        break;
                }
            }
        }

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        Parameters parameterCollection = new Parameters();
        parameterCollection.add(new SeparatorP(VARIABLE_SEPARATOR, this));
        parameterCollection.add(new StringP(VARIABLE_NAME, this));
        parameterCollection.add(new ChoiceP(VARIABLE_TYPE, this, VariableTypes.TEXT, VariableTypes.ALL));
        parameterCollection.add(new BooleanP(VARIABLE_BOOLEAN, this, false));
        parameterCollection.add(new StringP(VARIABLE_VALUE, this));
        parameterCollection.add(new FilePathP(VARIABLE_FILE, this));
        parameterCollection.add(new FolderPathP(VARIABLE_FOLDER, this));
        parameterCollection.add(new StringP(VARIABLE_CHOICES, this));
        parameterCollection.add(new ChoiceP(VARIABLE_CHOICE, this, "", new String[0]));
        parameterCollection.add(new BooleanP(STORE_AS_METADATA_ITEM, this, false));
        parameterCollection.add(new TextAreaP(VARIABLE_TEXT_AREA, this, true));

        parameters.add(new ParameterGroup(ADD_NEW_VARIABLE, this, parameterCollection, 1, getUpdaterAndGetter()));

        addParameterDescriptions();

    }

    @Override
    public Parameters updateAndGetParameters() {
        WorkspaceI workspace = null;
        ParameterGroup group = parameters.getParameter(ADD_NEW_VARIABLE);
        if (group == null)
            return parameters;

        LinkedHashMap<Integer, Parameters> collections = group.getCollections(false);
        for (Parameters collection : collections.values()) {
            StringP variableName = (StringP) collection.get(VARIABLE_NAME);
            if (isEnabled()) {
                switch ((String) collection.getValue(VARIABLE_TYPE, workspace)) {
                    case VariableTypes.BOOLEAN:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_BOOLEAN, workspace).toString());
                        break;
                    case VariableTypes.CHOICE:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_CHOICE, workspace));
                        break;
                    case VariableTypes.FILE:
                        String path = collection.getValue(VARIABLE_FILE, workspace);
                        path = path.replace("\\", "\\\\");
                        globalVariables.put(variableName, path);
                        break;
                    case VariableTypes.FOLDER:
                        path = collection.getValue(VARIABLE_FOLDER, workspace);
                        path = path.replace("\\", "\\\\");
                        globalVariables.put(variableName, path);
                        break;
                    case VariableTypes.TEXT:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_VALUE, workspace));
                        break;
                    case VariableTypes.TEXT_AREA:
                        globalVariables.put(variableName, collection.getValue(VARIABLE_TEXT_AREA, workspace));
                        break;
                }

            } else if (globalVariables.containsKey(variableName)) {
                globalVariables.remove(variableName);
            }
        }

        return parameters;

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
        WorkspaceI workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        LinkedHashMap<Integer, Parameters> collections = parameters.getValue(ADD_NEW_VARIABLE, workspace);

        for (Parameters collection : collections.values()) {
            if ((boolean) collection.getValue(STORE_AS_METADATA_ITEM, workspace))
                returnedRefs.add(metadataRefs.getOrPut(collection.getValue(VARIABLE_NAME, workspace)));
        }

        return returnedRefs;

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
        parameters.get(ADD_NEW_VARIABLE).setDescription(
                "Add a new global variable.  Added variables can be removed using the \"Remove\" button.");

        Parameters collection = ((ParameterGroup) parameters.get(ADD_NEW_VARIABLE)).getTemplateParameters();

        collection.get(VARIABLE_NAME).setDescription(
                "Name of this variable.  This is the name that will be used when referring to the variable in place of fixed values.  To refer to variables, use the form \"V{[VARIABLE_NAME]}\", where \"[VARIABLE_NAME]\" is replaced by the variable name.  For example, a variable called \"my_var\" would be referred to using the text \"V{my_var}\".");

        collection.get(VARIABLE_CHOICES)
                .setDescription("When \"" + VARIABLE_TYPE + "\" is set to \"" + VariableTypes.CHOICE
                        + "\" mode, these are the options that will be presented by the \"" + VARIABLE_CHOICE
                        + "\" drop-down parameter.  Choices are specified as a comma-separated list.");

        collection.get(VARIABLE_TYPE).setDescription("Controls how the variable is specified:<br><ul>"

                + "<li>\"" + VariableTypes.BOOLEAN
                + "\" The output variable is assigned either \"True\" or \"False\" depending on whether the switch (specified with the \""
                + VARIABLE_BOOLEAN + "\" parameter) is enabled or disabled, respectively.</li>"

                + "<li>\"" + VariableTypes.CHOICE
                + "\" Select the output variable from a pre-determined list of options (specified with the \""
                + VARIABLE_CHOICES + "\" parameter).</li>"

                + "<li>\"" + VariableTypes.FILE
                + "\" Select a specific file on the computer for this variable using the \"" + VARIABLE_FILE
                + "\" parameter.  The variable will be set to the full path to this file.  Note: backslash characters will be escaped (i.e. \"\\\" will appear as \"\\\\\").</li>"

                + "<li>\"" + VariableTypes.FOLDER
                + "\" Select a specific folder on the computer for this variable using the \"" + VARIABLE_FOLDER
                + "\" parameter.  The variable will be set to the full path to this folder.  Note: backslash characters will be escaped (i.e. \"\\\" will appear as \"\\\\\").</li>"

                + "<li>\"" + VariableTypes.TEXT + "\" Specify a fixed text value for this variable using the \""
                + VARIABLE_VALUE + "\" parameter.</li></ul>"

                + "<li>\"" + VariableTypes.TEXT_AREA + "\" Specify a fixed text value for this variable using the \""
                + VARIABLE_TEXT_AREA
                + "\" parameter.  This simply provides a larger, multiline area in which text can be entered.</li></ul>");

        collection.get(VARIABLE_BOOLEAN).setDescription("Boolean value for the corresponding global variable when \""
                + VARIABLE_TYPE + "\" is in \"" + VariableTypes.BOOLEAN + "\" mode.");

        collection.get(VARIABLE_VALUE).setDescription("Fixed value for the corresponding global variable when \""
                + VARIABLE_TYPE + "\" is in \"" + VariableTypes.TEXT + "\" mode.");

        collection.get(VARIABLE_FILE)
                .setDescription("Fixed value file location for the corresponding global variable when \""
                        + VARIABLE_TYPE + "\" is in \"" + VariableTypes.FILE + "\" mode.");

        collection.get(VARIABLE_FOLDER)
                .setDescription("Fixed value folder location for the corresponding global variable when \""
                        + VARIABLE_TYPE + "\" is in \"" + VariableTypes.FOLDER + "\" mode.");

        collection.get(VARIABLE_CHOICE)
                .setDescription("Pre-defined list of choices to select global variable value from when \""
                        + VARIABLE_TYPE + "\" is in \"" + VariableTypes.CHOICE + "\" mode.");

        collection.get(STORE_AS_METADATA_ITEM).setDescription(
                "When selected, the variable will be stored as a metadata item.  This allows it to be exported to the final spreadsheet.");

    }

    private ParameterUpdaterAndGetter getUpdaterAndGetter() {
        return new ParameterUpdaterAndGetter() {

            @Override
            public Parameters updateAndGet(Parameters params) {
                Parameters returnedParameters = new Parameters();

                returnedParameters.add(params.getParameter(VARIABLE_SEPARATOR));
                returnedParameters.add(params.getParameter(VARIABLE_NAME));
                returnedParameters.add(params.getParameter(VARIABLE_TYPE));
                switch ((String) params.getValue(VARIABLE_TYPE, null)) {
                    case VariableTypes.BOOLEAN:
                        returnedParameters.add(params.getParameter(VARIABLE_BOOLEAN));
                        break;
                    case VariableTypes.CHOICE:
                        returnedParameters.add(params.getParameter(VARIABLE_CHOICES));
                        returnedParameters.add(params.getParameter(VARIABLE_CHOICE));
                        String variableChoices = params.getValue(VARIABLE_CHOICES, null);
                        String[] choices = variableChoices.split(",");
                        ((ChoiceP) params.getParameter(VARIABLE_CHOICE)).setChoices(choices);
                        break;
                    case VariableTypes.FILE:
                        returnedParameters.add(params.getParameter(VARIABLE_FILE));
                        break;
                    case VariableTypes.FOLDER:
                        returnedParameters.add(params.getParameter(VARIABLE_FOLDER));
                        break;
                    case VariableTypes.TEXT:
                        returnedParameters.add(params.getParameter(VARIABLE_VALUE));
                        break;
                    case VariableTypes.TEXT_AREA:
                        returnedParameters.add(params.getParameter(VARIABLE_TEXT_AREA));
                        break;
                }
                returnedParameters.add(params.getParameter(STORE_AS_METADATA_ITEM));

                return returnedParameters;

            }
        };
    }
}
