package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalVariables extends Module {
    public static final String ADD_NEW_VARIABLE = "Add new variable";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_VALUE = "Variable value";

    private static ParameterCollection globalParameters = new ParameterCollection();

    public GlobalVariables(ModuleCollection modules) {
        super("Global variables",modules);
    }

    public static String convertString(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);


        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            // Iterating over all parameters, finding the one with the matching name
            ParameterGroup group = globalParameters.getParameter(ADD_NEW_VARIABLE);
            if (group == null) return string;
            LinkedHashSet<ParameterCollection> collections = group.getCollections();

            for (ParameterCollection collection:collections) {
                String name = collection.getValue(VARIABLE_NAME);
                if (name.equals(metadataName)) {
                    String value = collection.getValue(VARIABLE_VALUE);
                    string = string.replace(fullName,value);
                    break;
                }
            }
        }

        return string;

    }

    public static boolean variablesPresent(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String metadataName = matcher.group(1);

            // Iterating over all parameters, finding the one with the matching name
            ParameterGroup group = globalParameters.getParameter(ADD_NEW_VARIABLE);
            if (group == null) return false;
            LinkedHashSet<ParameterCollection> collections = group.getCollections();

            boolean found = false;
            for (ParameterCollection collection:collections) {
                String name = collection.getValue(VARIABLE_NAME);
                if (name.equals(metadataName)) {
                    found = true;
                    break;
                }
            }

            // If the current parameter wasn't found, return false
            if (!found) return false;

        }

        return true;

    }

    public static boolean containsMetadata(String string) {
        Pattern pattern = Pattern.compile("V\\{([\\w]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }

    public static void resetCollection() {
        globalParameters = new ParameterCollection();
    }

    public static int count() {
        return globalParameters.size();
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
    protected boolean process(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        ParameterCollection parameterCollection = new ParameterCollection();
        parameterCollection.add(new StringP(VARIABLE_NAME,this));
        parameterCollection.add(new StringP(VARIABLE_VALUE,this));

        parameters.add(new ParameterGroup(ADD_NEW_VARIABLE,this,parameterCollection));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        globalParameters.addAll(parameters);
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
