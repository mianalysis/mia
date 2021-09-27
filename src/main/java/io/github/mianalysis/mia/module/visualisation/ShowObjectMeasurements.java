package io.github.mianalysis.mia.module.visualisation;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Module;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.object.Status;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.parameters.Parameters;

/**
 * Created by Stephen Cross on 14/10/2019.
 */
@Plugin(type = Module.class, priority=Priority.LOW, visible=true)
public class ShowObjectMeasurements extends Module {
    public static final String INPUT_SEPARATOR = "Input";
    public static final String INPUT_OBJECTS = "Input objects";

    public ShowObjectMeasurements(Modules modules) {
        super("Show object measurements", modules);

        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;

    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with all objects of the specified object collection.";
    }


    @Override
    public Category getCategory() {
        return Categories.VISUALISATION;
    }

    @Override
    protected Status process(Workspace workspace) {
        String inputObjects = parameters.getValue(INPUT_OBJECTS);

        if (showOutput) workspace.getObjectSet(inputObjects).showAllMeasurements();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new InputObjectsP(INPUT_OBJECTS,this,"","Object collection to display all measurements for."));
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
