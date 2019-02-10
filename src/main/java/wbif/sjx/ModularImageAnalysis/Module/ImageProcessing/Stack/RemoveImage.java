package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing.Stack;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Module.PackageNames;
import wbif.sjx.ModularImageAnalysis.Object.*;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.ParameterCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.RemovedImageP;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends Module {
    public static final String INPUT_IMAGE = "Input image";

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
    public boolean run(Workspace workspace) {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        // Removing the relevant image from the workspace
        writeMessage("Removing image ("+inputImageName+") from workspace");
        workspace.removeImage(inputImageName);

        return true;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new RemovedImageP(INPUT_IMAGE,this));

    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;

    }

    @Override
    public MeasurementRefCollection updateAndGetImageMeasurementRefs() {
        return null;
    }

    @Override
    public MeasurementRefCollection updateAndGetObjectMeasurementRefs() {
        return null;
    }

    @Override
    public MetadataRefCollection updateAndGetMetadataReferences() {
        return null;
    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }

}
