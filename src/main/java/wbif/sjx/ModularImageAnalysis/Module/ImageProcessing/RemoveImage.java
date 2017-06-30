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
    public void execute(HCWorkspace workspace, boolean verbose) throws GenericMIAException {
        String moduleName = this.getClass().getSimpleName();
        if (verbose) System.out.println("["+moduleName+"] Initialising");

        // Getting input image
        String inputImageName = parameters.getValue(INPUT_IMAGE);

        // Removing the relevant image from the workspace
        if (verbose) System.out.println("["+moduleName+"] Removing image ("+inputImageName+") from workspace");
        workspace.removeImage(inputImageName);

        if (verbose) System.out.println("["+moduleName+"] Complete");

    }

    @Override
    public void initialiseParameters() {
        parameters.addParameter(new HCParameter(INPUT_IMAGE,HCParameter.REMOVED_IMAGE,null));

    }

    @Override
    public HCParameterCollection getActiveParameters() {
        return parameters;

    }

    @Override
    public void addMeasurements(HCMeasurementCollection measurements) {

    }

    @Override
    public void addRelationships(HCRelationshipCollection relationships) {

    }
}
