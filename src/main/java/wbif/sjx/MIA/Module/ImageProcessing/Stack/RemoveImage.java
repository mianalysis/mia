package wbif.sjx.MIA.Module.ImageProcessing.Stack;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.PackageNames;
import wbif.sjx.MIA.Object.*;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ParamSeparatorP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.RemovedImageP;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.MetadataRefCollection;
import wbif.sjx.MIA.Object.References.RelationshipRefCollection;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends Module {
    public static final String INPUT_SEPARATOR = "Image input";
    public static final String INPUT_IMAGE = "Input image";
    public static final String REMOVAL_CONTROLS = "Image removal controls";
    public static final String RETAIN_MEASUREMENTS = "Retain measurements";

    public RemoveImage(ModuleCollection modules) {
        super(modules);
    }

    @Override
    public String getTitle() {
        return "Remove image";
    }

    @Override
    public String getPackageName() {
        return PackageNames.IMAGE_PROCESSING_STACK;
    }

    @Override
    public String getHelp() {
        return "Removes the specified image from the workspace.  This helps keep memory usage down";
    }

    @Override
    public boolean process(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);
        boolean retainMeasurements = parameters.getValue(RETAIN_MEASUREMENTS);

        // Removing the relevant image from the workspace
        writeMessage("Removing image ("+inputImageName+") from workspace");
        workspace.removeImage(inputImageName,retainMeasurements);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ParamSeparatorP(INPUT_SEPARATOR,this));
        parameters.add(new RemovedImageP(INPUT_IMAGE,this));
        parameters.add(new ParamSeparatorP(REMOVAL_CONTROLS,this));
        parameters.add(new BooleanP(RETAIN_MEASUREMENTS,this,false));

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

}
