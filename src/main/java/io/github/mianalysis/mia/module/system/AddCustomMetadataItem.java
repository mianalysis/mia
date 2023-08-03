package io.github.mianalysis.mia.module.system;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class AddCustomMetadataItem extends Module {

	/**
	* 
	*/
    private static final String METADATA_SEPARATOR = "Specify metadata properties";

	/**
	* Name for metadata item to be assigned.
	*/
    private static final String METADATA_NAME = "Metadata name";

	/**
	* Value of metadata item.  This will be the same for all analysis runs performed at the same time since this value doesn't update during an analysis.
	*/
    private static final String METADATA_VALUE = "Metadata value";


    public AddCustomMetadataItem(Modules modules) {
        super("Add custom metadata item", modules);
    }


    @Override
    public Category getCategory() {
        return Categories.SYSTEM;
    }

    @Override
    public String getDescription() {
        return "This module allows for a specific metadata item to be used.  An example of this would be to add a label for generic (metadata-based) filename generation in the image loader (i.e. all images to be loaded must have the word \"phase\" in them).  Output metadata values can themselves be constructed from existing metadata values, accessed using the M{[NAME]} form (e.g. M{Filename}.";
    }

    @Override
    protected Status process(Workspace workspace) {
        String metadataName = parameters.getValue(METADATA_NAME,workspace);
        String metadataValue = parameters.getValue(METADATA_VALUE,workspace);

        // Applying existing metadata values and calculations
        metadataValue = workspace.getMetadata().insertMetadataValues(metadataValue);
        metadataValue = TextType.applyCalculation(metadataValue);

        workspace.getMetadata().put(metadataName,metadataValue);

        if (showOutput)
            workspace.showMetadata();

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
    public Parameters updateAndGetParameters() {
Workspace workspace = null;
        return parameters;
    }

    @Override
    public ImageMeasurementRefs updateAndGetImageMeasurementRefs() {
return null;
    }

    @Override
public ObjMeasurementRefs updateAndGetObjectMeasurementRefs() {
return null;
    }

    @Override
public MetadataRefs updateAndGetMetadataReferences() {
Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue(METADATA_NAME,workspace)));

        return returnedRefs;

    }

    @Override
    public ParentChildRefs updateAndGetParentChildRefs() {
return null;
    }

    @Override
    public PartnerRefs updateAndGetPartnerRefs() {
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
