package wbif.sjx.ModularImageAnalysis.Module.Miscellaneous;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.MetadataRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by sc13967 on 14/03/2018.
 */
public class GUISeparator extends Module{
    private static boolean verbose = false;
    
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
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
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
