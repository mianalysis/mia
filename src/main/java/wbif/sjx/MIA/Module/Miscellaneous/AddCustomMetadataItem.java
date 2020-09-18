package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;
import wbif.sjx.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.Collections.ParentChildRefCollection;
import wbif.sjx.MIA.Object.References.Collections.PartnerRefCollection;

public class AddCustomMetadataItem extends Module {
    private static final String METADATA_SEPARATOR = "Specify metadata properties";
    private static final String METADATA_NAME = "Metadata name";
    private static final String METADATA_VALUE = "Metadata value";


    public AddCustomMetadataItem(ModuleCollection modules) {
        super("Add custom metadata item",modules);
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "This module allows for a specific metadata item to be used.  An example of this would be to add a label " +
                "for generic (metadata-based) filename generation in the image loader.";
    }

    @Override
    protected Status process(Workspace workspace) {
        String metadataName = parameters.getValue(METADATA_NAME);
        String metadataValue = parameters.getValue(METADATA_VALUE);

        workspace.getMetadata().put(metadataName,metadataValue);

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(METADATA_SEPARATOR, this));
        parameters.add(new StringP(METADATA_NAME, this));
        parameters.add(new StringP(METADATA_VALUE, this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }

    @Override
    public ImageMeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public ObjMeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        MetadataRefCollection returnedRefs = new MetadataRefCollection();

        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue(METADATA_NAME)));

        return returnedRefs;

    }

    @Override
    public ParentChildRefCollection updateAndGetParentChildRefs() {
        return null;
    }

    @Override
    public PartnerRefCollection updateAndGetPartnerRefs() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
