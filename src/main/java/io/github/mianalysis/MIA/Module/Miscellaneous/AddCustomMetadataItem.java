package io.github.mianalysis.MIA.Module.Miscellaneous;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.Text.StringP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;

public class AddCustomMetadataItem extends Module {
    private static final String METADATA_SEPARATOR = "Specify metadata properties";
    private static final String METADATA_NAME = "Metadata name";
    private static final String METADATA_VALUE = "Metadata value";


    public AddCustomMetadataItem(ModuleCollection modules) {
        super("Add custom metadata item", modules);
        deprecated = true;
    }


    @Override
    public Category getCategory() {
        return Categories.MISCELLANEOUS;
    }

    @Override
    public String getDescription() {
        return "DEPRECATED: Please use \"" + new GlobalVariables(null).getName()
                + "\" module instead, which allows custom values to be accessed using the V{[NAME]} notation.  Global variables can also be stored as metadata items for the purpose of exporting."
        
        + "<br><br>This module allows for a specific metadata item to be used.  An example of this would be to add a label " +
                "for generic (metadata-based) filename generation in the image loader (i.e. all images to be loaded must have the word \"phase\" in them).";
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
        parameters.add(new SeparatorP(METADATA_SEPARATOR, this));
        parameters.add(new StringP(METADATA_NAME, this));
        parameters.add(new StringP(METADATA_VALUE, this));

        addParameterDescriptions();

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

    void addParameterDescriptions() {
        parameters.get(METADATA_NAME).setDescription("Name for metadata item to be assigned.");

        parameters.get(METADATA_VALUE).setDescription("Value of metadata item.  This will be the same for all analysis runs performed at the same time since this value doesn't update during an analysis.");

    }
}
