package io.github.mianalysis.mia.module.imageprocessing.Stack;

import java.util.LinkedHashMap;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.Object.Status;
import io.github.mianalysis.mia.Object.Workspace;
import io.github.mianalysis.mia.Object.Parameters.BooleanP;
import io.github.mianalysis.mia.Object.Parameters.SeparatorP;
import io.github.mianalysis.mia.Object.Refs.Collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.MetadataRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.ParentChildRefs;
import io.github.mianalysis.mia.Object.Refs.Collections.PartnerRefs;
import io.github.mianalysis.mia.Object.Parameters.Parameters;
import io.github.mianalysis.mia.Object.Parameters.ParameterGroup;
import io.github.mianalysis.mia.Object.Parameters.RemovedImageP;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends Module {
    public static final String REMOVAL_SEPARATOR = "Images to remove";
    public static final String INPUT_IMAGE = "Input image";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";
    public static final String REMOVE_ANOTHER_IMAGE = "Remove another image";

    public RemoveImage(Modules modules) {
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
        LinkedHashMap<Integer,Parameters> collections = parameterGroup.getCollections(false);

        for (Parameters collection:collections.values()) {
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

        Parameters collection = new Parameters();
        collection.add(new RemovedImageP(INPUT_IMAGE,this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_IMAGE, this, collection, 1));

        addParameterDescriptions();

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

    void addParameterDescriptions() {
        Parameters collection = ((ParameterGroup) parameters.get(REMOVE_ANOTHER_IMAGE))
                .getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription("Name of the image to be removed from the workspace.");

        collection.get(RETAIN_MEASUREMENTS).setDescription("Retain measurements for this image, or remove everything.  When selected, the image intensity information will be removed, as this is typically where most memory us used, however any measurements associated with it will be retained.");

        parameters.get(REMOVE_ANOTHER_IMAGE).setDescription("Mark another image from the workspace for removal.");

    }
}
