package wbif.sjx.ModularImageAnalysis.Module;

import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.DoubleP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageP;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageP;

public class TestModule extends Module {
    private String INPUT_IMAGE = "Input image";
    private String OUTPUT_IMAGE = "Output image";
    private String FILTER_RADIUS = "Filter radius";


    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
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
    protected void initialiseParameters() {
        parameters.add(new InputImageP(INPUT_IMAGE,this,""));
        parameters.add(new OutputImageP(OUTPUT_IMAGE,this,""));
        parameters.add(new DoubleP(FILTER_RADIUS,this,1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementReferences() {
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
