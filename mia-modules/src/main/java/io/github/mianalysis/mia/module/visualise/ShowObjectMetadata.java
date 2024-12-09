package io.github.mianalysis.mia.module.visualise;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;
import io.github.mianalysis.mia.object.parameters.Parameters;

/**
 * Displays all metadata items associated with all objects of the specified
 * object collection.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class ShowObjectMetadata extends Module {

    /**
    * 
    */
    public static final String INPUT_SEPARATOR = "Input";

    /**
     * Object collection to display all metadata for.
     */
    public static final String INPUT_OBJECTS = "Input objects";

    public ShowObjectMetadata(Modules modules) {
        super("Show object metadata", modules);

        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;

    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Displays all metadata items associated with all objects of the specified object collection.";
    }

    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    public Status process(Workspace workspace) {
        String inputObjects = parameters.getValue(INPUT_OBJECTS, workspace);

        if (showOutput)
            workspace.getObjects(inputObjects).showAllMetadata();

        return Status.PASS;

    }

    @Override
    public void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR, this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS, this, "", "Object collection to display all metadata for."));
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
