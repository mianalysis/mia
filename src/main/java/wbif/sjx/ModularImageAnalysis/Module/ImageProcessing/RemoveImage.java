package wbif.sjx.ModularImageAnalysis.Module.ImageProcessing;

import wbif.sjx.ModularImageAnalysis.Exceptions.GenericMIAException;
import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.*;

/**
 * Created by sc13967 on 30/06/2017.
 */
public class RemoveImage extends HCModule {
    public static final String INPUT_IMAGE = "Input image";

    @Override
    public String getTitle() {
        return "Remove image";
    }

    @Override
    public String getHelp() {
        return "Removes the specified image from the workspace.  This helps keep memory usage down";
    }

    @Override
    public void run(Workspace workspace, boolean verbose) throws GenericMIAException {
        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        // Removing the relevant image from the workspace
        if (verbose) System.out.println("["+moduleName+"] Removing image ("+inputImageName+") from workspace");
        workspace.removeImage(inputImageName);

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new Parameter(INPUT_IMAGE, Parameter.REMOVED_IMAGE,null));

    }

    @Override
    public ParameterCollection getActiveParameters() {
        return parameters;

    }

    @Override
    public void addMeasurements(MeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(RelationshipCollection relationships) {

    }
}
