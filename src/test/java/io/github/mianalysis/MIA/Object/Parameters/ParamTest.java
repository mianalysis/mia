package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

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
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
        return imageMeasurementRefs;
    }

    @Override
    public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
        return objectMeasurementRefs;
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return metadataRefs;
    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
        return parentChildRefs;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
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
