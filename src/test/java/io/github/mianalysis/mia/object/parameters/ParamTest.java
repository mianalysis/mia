package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;

public class ParamTest extends Module {
    public ParamTest(Modules modules) {
        super("Parameter test module", modules);
    }

    @Override
    public Category getCategory() {
        return new Category("Test","",null);
    }

    @Override
    protected Status process(Workspace workspace) {
        return Status.FAIL;
    }

    @Override
    protected void initialiseParameters() {

    }

    @Override
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
Workspace workspace = null;
        return imageMeasurementRefs;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
Workspace workspace = null;
        return objectMeasurementRefs;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        return metadataRefs;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
Workspace workspace = null;
        return parentChildRefs;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
Workspace workspace = null;
        return partnerRefs;
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
