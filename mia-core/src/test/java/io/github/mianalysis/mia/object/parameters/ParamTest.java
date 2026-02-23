package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

public class ParamTest extends Module {
    public ParamTest(ModulesI modules) {
        super("Parameter test module", modules);
    }

    @Override
    public Category getCategory() {
        return new Category("Test","",null);
    }

    @Override
    public Status process(WorkspaceI workspace) {
        return Status.FAIL;
    }

    @Override
    public void initialiseParameters() {

    }

    @Override
    public Parameters updateAndGetParameters() {
WorkspaceI workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
WorkspaceI workspace = null;
        return imageMeasurementRefs;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
WorkspaceI workspace = null;
        return objectMeasurementRefs;
    }

    @Override
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
WorkspaceI workspace = null;
        return metadataRefs;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
WorkspaceI workspace = null;
        return parentChildRefs;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
WorkspaceI workspace = null;
        return partnerRefs;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Just used as a blank module for testing Parameters.";
    }
}
