package wbif.sjx.ModularImageAnalysis.Module;

import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.InputImageParam;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageParam;

public class TestModule extends Module {
    private String INPUT_IMAGE = "Input image";
    private String OUTPUT_IMAGE = "Output image";
    private String FILTER_RADIUS = "Filter radius";

    InputImageParam inputImageParam = new InputImageParam(INPUT_IMAGE,null);


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
        parameters.add(new InputImageParam(INPUT_IMAGE,null));
        parameters.add(new OutputImageParam(OUTPUT_IMAGE,null));
        parameters.add(new Num<Double>(FILTER_RADIUS,1d));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetImageMeasurementReferences() {
        return null;
    }

    @Override
    public MeasurementReferenceCollection updateAndGetObjectMeasurementReferences() {
        return null;
    }

    @Override
    public MetadataReferenceCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
