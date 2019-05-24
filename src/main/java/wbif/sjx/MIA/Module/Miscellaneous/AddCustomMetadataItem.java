package wbif.sjx.MIA.Module.Miscellaneous;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.StringP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;
import wbif.sjx.MIA.Object.Workspace;

public class AddCustomMetadataItem extends Module {
    private static final String METADATA_SEPARATOR = "Specify metadata properties";
    private static final String METADATA_NAME = "Metadata name";
    private static final String METADATA_VALUE = "Metadata value";


    public AddCustomMetadataItem(ModuleCollection modules) {
        super(modules);
    }

    @Override
    public String getTitle() {
        return "Add custom metadata item";
    }

    @Override
    public String getPackageName() {
        return PackageNames.MISCELLANEOUS;
    }

    @Override
    public String getHelp() {
        return "This module allows for a specific metadata item to be used.  An example of this would be to add a label " +
                "for generic (metadata-based) filename generation in the image loader.";
    }

    @Override
    protected boolean process(Workspace workspace) {
        String metadataName = parameters.getValue(METADATA_NAME);
        String metadataValue = parameters.getValue(METADATA_VALUE);

        workspace.getMetadata().put(metadataName,metadataValue);

        return true;

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }
}
