package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.*;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

import java.util.LinkedHashSet;

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
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getDescription() {
        return "Removes the specified image(s) from the workspace.  This helps keep memory usage down.";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        ParameterGroup parameterGroup = parameters.getParameter(REMOVE_ANOTHER_IMAGE);
        LinkedHashSet<ParameterCollection> collections = parameterGroup.getCollections();

        for (ParameterCollection collection:collections) {
            String inputImageName = collection.getValue(INPUT_IMAGE);
            boolean retainMeasurements = collection.getValue(RETAIN_MEASUREMENTS);

            // Removing the relevant image from the workspace
            writeMessage("Removing image ("+inputImageName+") from workspace");
            workspace.removeImage(inputImageName,retainMeasurements);

        }

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(REMOVAL_SEPARATOR,this));

        ParameterCollection collection = new ParameterCollection();
        collection.add(new RemovedImageP(INPUT_IMAGE,this));
        collection.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));
        parameters.add(new ParameterGroup(REMOVE_ANOTHER_IMAGE,this,collection,1));

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
    public RelationshipRefCollection updateAndGetRelationships() {
        return null;
    }

    @Override
    public boolean verify() {
        return true;
    }
}
