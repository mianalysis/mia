package io.github.mianalysis.MIA.Module.ImageProcessing.Stack;

import java.util.LinkedHashMap;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.Category;
import io.github.mianalysis.MIA.Module.Categories;
import io.github.mianalysis.MIA.Object.Status;
import io.github.mianalysis.MIA.Object.Workspace;
import io.github.mianalysis.MIA.Object.Parameters.BooleanP;
import io.github.mianalysis.MIA.Object.Parameters.SeparatorP;
import io.github.mianalysis.MIA.Object.Parameters.ParameterCollection;
import io.github.mianalysis.MIA.Object.Parameters.ParameterGroup;
import io.github.mianalysis.MIA.Object.Parameters.RemovedImageP;
import io.github.mianalysis.MIA.Object.References.Collections.ImageMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.MetadataRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ObjMeasurementRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;
import io.github.mianalysis.MIA.Object.References.Collections.PartnerRefCollection;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends Module {
    public static final String REMOVAL_SEPARATOR = "Images to remove";
    public static final String INPUT_IMAGE = "Input image";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";
    public static final String REMOVE_ANOTHER_IMAGE = "Remove another image";

    public RemoveImage(ModuleCollection modules) {
        super("Remove image",modules);
    }



    @Override
    public Category getCategory() {
        return Categories.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Removes the specified image(s) from the workspace.  Doing this helps keep memory usage down.  Measurements associated with an image can be retained for further use.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        ParameterGroup parameterGroup = parameters.getParameter(REMOVE_ANOTHER_IMAGE);
        LinkedHashMap<Integer,ParameterCollection> collections = parameterGroup.getCollections(false);

        for (ParameterCollection collection:collections.values()) {
            String inputImageName = collection.getValue(INPUT_IMAGE);
            boolean retainMeasurements = collection.getValue(RETAIN_MEASUREMENTS);

            // Removing the relevant image from the workspace
            writeStatus("Removing image ("+inputImageName+") from workspace");
            workspace.removeImage(inputImageName,retainMeasurements);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(REMOVAL_SEPARATOR,this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new RemovedImageP(INPUT_IMAGE,this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_IMAGE, this, collection, 1));

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
        return null;
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
        ParameterCollection collection = ((ParameterGroup) parameters.get(REMOVE_ANOTHER_IMAGE))
                .getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription("Name of the image to be removed from the workspace.");

        collection.get(RETAIN_MEASUREMENTS).setDescription("Retain measurements for this image, or remove everything.  When selected, the image intensity information will be removed, as this is typically where most memory us used, however any measurements associated with it will be retained.");

        parameters.get(REMOVE_ANOTHER_IMAGE).setDescription("Mark another image from the workspace for removal.");

    }
}
