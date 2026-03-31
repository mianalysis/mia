package io.github.mianalysis.mia.module.system;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.metadata.Metadata;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.text.MessageP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * This module allows for a specific metadata item to be used. An example of
 * this would be to add a label for generic (metadata-based) filename generation
 * in the image loader (i.e. all images to be loaded must have the word "phase"
 * in them). Output metadata values can themselves be constructed from existing
 * metadata values, accessed using the M{[NAME]} form (e.g. M{Filename}.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class GetCurrentDateAndTime extends Module {
    public static String INFO_SEPARATOR = "Date and time metadata";

    public static String INFO = "Info";

    public GetCurrentDateAndTime(Modules modules) {
        super("Get current date and time", modules);
    }

    @Override
    public Category getCategory() {
        return Categories.SYSTEM;
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Adds the current date and time to the workspace metadata.";
    }

    @Override
    public Status process(Workspace workspace) {
        Metadata metadata = workspace.getMetadata();
        Date date = new Date(System.currentTimeMillis());

        metadata.put("DATE", new SimpleDateFormat("YYYY-MM-dd").format(date));
        metadata.put("DATE_TIME", new SimpleDateFormat("YYYY-MM-dd:HH:mm:ss").format(date));
        metadata.put("TIME", new SimpleDateFormat("HH:mm:ss").format(date));
        metadata.put("YEAR", new SimpleDateFormat("YYYY").format(date));
        metadata.put("MONTH", new SimpleDateFormat("MM").format(date));
        metadata.put("DAY", new SimpleDateFormat("dd").format(date));
        metadata.put("HOUR", new SimpleDateFormat("HH").format(date));
        metadata.put("MINUTE", new SimpleDateFormat("mm").format(date));
        metadata.put("SECONDS", new SimpleDateFormat("ss").format(date));

        if (showOutput)
            workspace.showMetadata();

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(INFO_SEPARATOR, this));
        parameters.add(new MessageP(INFO, this, "Will add the following metadata items: DATE, DATE_TIME, TIME, YEAR, MONTH, DAY, HOUR, MINUTE, SECONDS", ParameterState.MESSAGE));
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
        Workspace workspace = null;
        MetadataRefs returnedRefs = new MetadataRefs();

        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("DATE", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("DATE_TIME", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("TIME", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("YEAR", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("MONTH", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("DAY", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("HOUR", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("MINUTE", workspace)));
        returnedRefs.add(metadataRefs.getOrPut(parameters.getValue("SECONDS", workspace)));

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
}
