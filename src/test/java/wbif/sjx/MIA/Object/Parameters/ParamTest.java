package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ParamTest extends Module {
    public ParamTest(ModuleCollection modules) {
        super("Parameter test module", modules);
    }

    @Override
    public String getPackageName() {
        return "Test";
    }

    @Override
    protected boolean process(Workspace workspace) {
        return false;
    }

    @Override
    protected void initialiseParameters() {

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return imageMeasurementRefs;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return metadataRefs;
    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return parentChildRefs;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Just used as a blank module for testing Parameters.";
    }
}
