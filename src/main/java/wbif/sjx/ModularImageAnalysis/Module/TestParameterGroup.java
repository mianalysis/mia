package wbif.sjx.ModularImageAnalysis.Module;

import wbif.sjx.ModularImageAnalysis.Object.MeasurementRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.MetadataRefCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.BooleanP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterGroup;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class TestParameterGroup extends Module {
    public static final String GROUP_NAME = "Filter group";
    public static final String IMAGE_NAME = "Input image";
    public static final String BOO = "Random boo";


    @Override
    public String getTitle() {
        return "Test parameter group";
    }

    @Override
    public String getPackageName() {
        return "Test\\";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    protected boolean run(Workspace workspace) {
        return false;
    }

    @Override
    protected void initialiseParameters() {;
        ParameterCollection group = new ParameterCollection();
        group.add(new InputImageP(IMAGE_NAME,this));
        group.add(new BooleanP(BOO,this,true));

        parameters.add(new ParameterGroup(GROUP_NAME,this,group,1));

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
    public void addRelationships(RelationshipCollection relationships) {

    }
}
