package io.github.mianalysis.MIA.Module.Visualisation;

import java.awt.Color;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.Parameters;
import io.github.mianalysis.MIA.Object.Parameters.Text.MessageP;
import io.github.mianalysis.MIA.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.MIA.Object.Refs.Collections.PartnerRefs;

/**
 * Created by Stephen Cross on 14/10/2019.
 */
public class ShowMetadata extends Module {
    public static final String MESSAGE = "Message";

    public ShowMetadata(Modules modules) {
        super("Show metadata", modules);

        // This module likely wants to have this enabled (otherwise it does nothing)
        showOutput = true;

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
    protected Status process(Workspace workspace) {
        if (showOutput) workspace.showMetadata();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new MessageP(MESSAGE,this,"There are no parameters for this module", Color.BLACK));

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