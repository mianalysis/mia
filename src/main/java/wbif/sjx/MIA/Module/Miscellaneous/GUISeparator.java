package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module{
    private static boolean verbose = false;

    public static final String VISIBILITY_SEPARATOR = "Separator visibility";
    public static final String SHOW_BASIC = "Show basic";
    public static final String EXPANDED_BASIC = "Expanded basic GUI";
    public static final String EXPANDED_EDITING = "Expanded editing GUI";


    @Override
    public String getTitle() {
        return "GUI separator";
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public boolean process(Workspace workspace) {
        return true;
    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(VISIBILITY_SEPARATOR,this));
        parameters.add(new BooleanP(SHOW_BASIC,this,true));
        parameters.add(new BooleanP(EXPANDED_BASIC,this,true));
        parameters.add(new BooleanP(EXPANDED_EDITING,this,true));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs(ModuleCollection modules) {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public RelationshipCollection updateAndGetRelationships() {
        return null;
    }

}
