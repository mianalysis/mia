package io.github.mianalysis.mia.module.Visualisation;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.Object.Status;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Parameters.InputObjectsP;
import io.github.mianalysis.mia.Object.Parameters.SeparatorP;
import io.github.mianalysis.mia.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.mia.Object.Parameters.Parameters;

/**
 * Created by Stephen Cross on 14/10/2019.
 */
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
