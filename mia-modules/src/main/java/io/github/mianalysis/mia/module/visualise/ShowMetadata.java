package io.github.mianalysis.mia.module.visualise;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by Stephen Cross on 14/10/2019.
 */

/**
 * Displays all measurements associated with an image.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ShowMetadata extends Module {

    /**
    * 
    */
    public static final String MESSAGE = "Message";

    public ShowMetadata(Modules modules) {
        super("Show metadata", modules);

        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;

    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with an image.";
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public Status process(Workspace workspace) {
        if (showOutput)
            workspace.showMetadata();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new MessageP(MESSAGE, this, "There are no parameters for this module", ParameterState.NORMAL));

    }

    @Override
    public Parameters updateAndGetParameters() {
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
    public ObjMetadataRefs updateAndGetObjectMetadataRefs() {  
	return null; 
    }

    @Override
    public MetadataRefs updateAndGetMetadataReferences() {
        return null;
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
}
