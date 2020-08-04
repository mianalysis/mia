package wbif.sjx.MIA.Module.WorkflowHandling;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Status;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Object.Parameters.ChoiceP;
import wbif.sjx.MIA.Object.Parameters.ModuleP;
import wbif.sjx.MIA.Object.Parameters.ParameterCollection;
import wbif.sjx.MIA.Object.Parameters.Text.StringP;

public abstract class CoreWorkspaceHandler extends Module {
    public static final String CONTINUATION_MODE = "Continuation mode";
    public static final String REDIRECT_MODULE = "Redirect module";
    public static final String SHOW_REDIRECT_MESSAGE = "Show redirect message";
    public static final String REDIRECT_MESSAGE = "Redirect message";
    public static final String EXPORT_WORKSPACE = "Export terminated workspaces";
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";
    public static final String REMOVE_IMAGES = "Remove images from workspace";

    public interface ContinuationModes {
        String REDIRECT_TO_MODULE = "Redirect to module";
        String TERMINATE = "Terminate";

        String[] ALL = new String[] { REDIRECT_TO_MODULE, TERMINATE };

    }

    public CoreWorkspaceHandler(String name, ModuleCollection modules) {
        super(name, modules);
    }

    Status processTermination(ParameterCollection params) {
        return processTermination(params, null, false);

    }

    Status processTermination(ParameterCollection parameters, Workspace workspace, boolean showRedirectMessage) {
        String continuationMode = parameters.getValue(CONTINUATION_MODE);
        String redirectMessage = parameters.getValue(REDIRECT_MESSAGE);
        boolean exportWorkspace = parameters.getValue(EXPORT_WORKSPACE);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS);

        // If terminate, remove necessary images and objects
        switch (continuationMode) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                if (showRedirectMessage)
                    MIA.log.writeMessage(workspace.getMetadata().insertMetadataValues(redirectMessage));
                return Status.REDIRECT;
            case ContinuationModes.TERMINATE:
                workspace.setExportWorkspace(exportWorkspace);
                if (removeImages)
                    workspace.clearAllImages(false);
                if (removeObjects)
                    workspace.clearAllObjects(false);
                return Status.TERMINATE;
        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(CONTINUATION_MODE, this, ContinuationModes.TERMINATE, ContinuationModes.ALL));
        parameters.add(new ModuleP(REDIRECT_MODULE, this,true));
        parameters.add(new BooleanP(SHOW_REDIRECT_MESSAGE, this, false));
        parameters.add(new StringP(REDIRECT_MESSAGE, this, ""));
        parameters.add(new BooleanP(EXPORT_WORKSPACE, this, true));
        parameters.add(new BooleanP(REMOVE_IMAGES, this, false));
        parameters.add(new BooleanP(REMOVE_OBJECTS, this, false));
    }

    @Override
    public ParameterCollection updateAndGetParameters() {
        return parameters;
    }
}