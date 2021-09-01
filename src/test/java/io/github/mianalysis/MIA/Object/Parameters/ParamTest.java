package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;

public class ParamTest extends Module {
    public ParamTest(ModuleCollection modules) {
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
    public PartnerRefCollection updateAndGetPartnerRefs() {
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
