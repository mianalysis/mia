package wbif.sjx.MIA.Module.Hidden;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkflowParameters extends Module {
    public static final String CONFIGURATION_SEPARATOR = "Configuration";
    public static final String OBJECT_TYPE = "Default object type";
    public static final String GLOBAL_VARIABLES_SEPARATOR = "Global variables";
    public static final String ADD_NEW_VARIABLE = "Add new variable";
    public static final String VARIABLE_NAME = "Variable name";
    public static final String VARIABLE_VALUE = "Variable value";

    public interface ObjectTypes {
        String OPTIMISED = "Optimised";
        String POINTLIST = "Point list";
        String QUADTREE = "Quadtree";

        String[] ALL = new String[]{OPTIMISED,POINTLIST,QUADTREE};

    }

    public WorkflowParameters(ModuleCollection modules) {
        super("Global variables",modules);
    }

    public String convertString(String string) {
        Pattern pattern = Pattern.compile("\\£\\{([^£{}]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String fullName = matcher.group(0);
            String metadataName = matcher.group(1);

            // Iterating over all parameters, finding the one with the matching name
            ParameterGroup group = getParameter(ADD_NEW_VARIABLE);
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

    public boolean variablesPresent(String string) {
        Pattern pattern = Pattern.compile("\\£\\{([^£{}]+)}");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            String metadataName = matcher.group(1);

            // Iterating over all parameters, finding the one with the matching name
            ParameterGroup group = getParameter(ADD_NEW_VARIABLE);
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
        Pattern pattern = Pattern.compile("\\£\\{([^£{}]+)}");
        Matcher matcher = pattern.matcher(string);

        return matcher.find();

    }


    @Override
    public String getPackageName() {
        return "Hidden";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    protected boolean process(Workspace workspace) {
        return false;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(CONFIGURATION_SEPARATOR,this));
        parameters.add(new ChoiceP(OBJECT_TYPE,this,ObjectTypes.OPTIMISED,ObjectTypes.ALL));

        ParameterCollection parameterCollection = new ParameterCollection();
        parameterCollection.add(new StringP(VARIABLE_NAME,this));
        parameterCollection.add(new StringP(VARIABLE_VALUE,this));
        parameters.add(new ParamSeparatorP(GLOBAL_VARIABLES_SEPARATOR,this));
        parameters.add(new ParameterGroup(ADD_NEW_VARIABLE,this,parameterCollection));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
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
