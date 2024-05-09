package io.github.mianalysis.mia.module.system;

import java.util.LinkedHashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.RemovedImageP;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.refs.collections.ImageMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.MetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;
import io.github.mianalysis.mia.object.refs.collections.ObjMetadataRefs;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;
import io.github.mianalysis.mia.object.refs.collections.PartnerRefs;
import io.github.mianalysis.mia.object.system.Status;

/**
 * Created by sc13967 on 30/06/2017.
 */

/**
 * Removes the specified image(s) from the workspace. Doing this helps keep
 * memory usage down. Measurements associated with an image can be retained for
 * further use.
 */
@Plugin(type = Module.class, priority = Priority.LOW, visible = true)
public class RemoveImages extends Module {

    /**
    * 
    */
    public static final String REMOVAL_SEPARATOR = "Images to remove";
    public static final String INPUT_IMAGE = "Input image";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";

    /**
     * Mark another image from the workspace for removal.
     */
    public static final String REMOVE_ANOTHER_IMAGE = "Remove another image";

    public RemoveImages(Modules modules) {
        super("Remove images", modules);
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
        return "Removes the specified image(s) from the workspace.  Doing this helps keep memory usage down.  Measurements associated with an image can be retained for further use.";
    }

    @Override
    public Status process(Workspace workspace) {
        // Getting input image
        ParameterGroup parameterGroup = parameters.getParameter(REMOVE_ANOTHER_IMAGE);
        LinkedHashMap<Integer, Parameters> collections = parameterGroup.getCollections(false);

        for (Parameters collection : collections.values()) {
            String inputImageName = collection.getValue(INPUT_IMAGE, null);
            boolean retainMeasurements = collection.getValue(RETAIN_MEASUREMENTS, null);

            // Removing the relevant image from the workspace
            writeStatus("Removing image (" + inputImageName + ") from workspace");
            workspace.removeImage(inputImageName, retainMeasurements);

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new SeparatorP(REMOVAL_SEPARATOR, this));

        Parameters collection = new Parameters();
        collection.add(new RemovedImageP(INPUT_IMAGE, this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS, this, false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_IMAGE, this, collection, 1));

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

    void addParameterDescriptions() {
        Parameters collection = ((ParameterGroup) parameters.get(REMOVE_ANOTHER_IMAGE))
                .getTemplateParameters();

        collection.get(INPUT_IMAGE).setDescription("Name of the image to be removed from the workspace.");

        collection.get(RETAIN_MEASUREMENTS).setDescription(
                "Retain measurements for this image, or remove everything.  When selected, the image intensity information will be removed, as this is typically where most memory us used, however any measurements associated with it will be retained.");

        parameters.get(REMOVE_ANOTHER_IMAGE).setDescription("Mark another image from the workspace for removal.");

    }
}
