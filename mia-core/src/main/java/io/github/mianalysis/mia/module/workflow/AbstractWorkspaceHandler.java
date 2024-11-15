package io.github.mianalysis.mia.module.workflow;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.BooleanP;
import io.github.mianalysis.mia.object.parameters.ChoiceP;
import io.github.mianalysis.mia.object.parameters.ModuleP;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.text.StringP;
import io.github.mianalysis.mia.object.system.Status;

public abstract class AbstractWorkspaceHandler extends Module {
    public static final String CONTINUATION_MODE = "Continuation mode";
    public static final String REDIRECT_MODULE = "Redirect module";
    public static final String SHOW_REDIRECT_MESSAGE = "Show redirect message";
    public static final String REDIRECT_MESSAGE = "Redirect message";
    public static final String EXPORT_WORKSPACE = "Export terminated workspaces";
    public static final String REMOVE_OBJECTS = "Remove objects from workspace";
    public static final String REMOVE_IMAGES = "Remove images from workspace";
    public static final String SHOW_TERMINATION_WARNING = "Show termination warning";

    public interface ContinuationModes {
        String REDIRECT_TO_MODULE = "Redirect to module";
        String TERMINATE = "Terminate";

        String[] ALL = new String[] { REDIRECT_TO_MODULE, TERMINATE };

    }

    public AbstractWorkspaceHandler(String name, Modules modules) {
        super(name, modules);
    }

    Status processTermination(Parameters params) {
        return processTermination(params, null, false);

    }

    public Status processTermination(Parameters parameters, Workspace workspace, boolean showRedirectMessage) {
        String continuationMode = parameters.getValue(CONTINUATION_MODE,workspace);
        String redirectMessage = parameters.getValue(REDIRECT_MESSAGE,workspace);
        boolean showTerminationWarning = parameters.getValue(SHOW_TERMINATION_WARNING,workspace);
        boolean exportWorkspace = parameters.getValue(EXPORT_WORKSPACE,workspace);
        boolean removeImages = parameters.getValue(REMOVE_IMAGES,workspace);
        boolean removeObjects = parameters.getValue(REMOVE_OBJECTS,workspace);

        // If terminate, remove necessary images and objects
        switch (continuationMode) {
            case ContinuationModes.REDIRECT_TO_MODULE:
                if (showRedirectMessage)
                    MIA.log.writeMessage(workspace.getMetadata().insertMetadataValues(redirectMessage));
                    
                return Status.REDIRECT;

            case ContinuationModes.TERMINATE:
                if (showTerminationWarning)
                    MIA.log.writeWarning("Analysis terminated early");

                workspace.setExportWorkspace(exportWorkspace);
                if (removeImages)
                    workspace.clearAllImages(false);
                if (removeObjects)
                    workspace.clearAllObjects(false);

                return Status.TERMINATE_SILENT;

        }

        return Status.PASS;

    }

    @Override
    protected void initialiseParameters() {
        parameters.add(new ChoiceP(CONTINUATION_MODE, this, ContinuationModes.TERMINATE, ContinuationModes.ALL));
        parameters.add(new ModuleP(REDIRECT_MODULE, this, true));
        parameters.add(new BooleanP(SHOW_REDIRECT_MESSAGE, this, false));
        parameters.add(new StringP(REDIRECT_MESSAGE, this, ""));
        parameters.add(new BooleanP(SHOW_TERMINATION_WARNING, this, true));
        parameters.add(new BooleanP(EXPORT_WORKSPACE, this, true));
        parameters.add(new BooleanP(REMOVE_IMAGES, this, false));
        parameters.add(new BooleanP(REMOVE_OBJECTS, this, false));

    }

    @Override
    public Parameters updateAndGetParameters() {
        return parameters;

    }

    protected void addParameterDescriptions() {
        parameters.get(CONTINUATION_MODE)
                .setDescription("Controls what happens if the termination/redirection condition is met:<br><ul>"

                        + "<li>\"" + ContinuationModes.REDIRECT_TO_MODULE
                        + "\" The analysis workflow will skip to the module specified by the \"" + REDIRECT_MODULE
                        + "\" parameter.  Any modules between the present module and the target module will not be evaluated.</li>"

                        + "<li>\"" + ContinuationModes.TERMINATE
                        + "\"The analysis will stop evaluating any further modules.</li></ul>");

        parameters.get(REDIRECT_MODULE).setDescription(
                "If the condition is met, the workflow will redirect to this module.  In doing so, it will skip evaluation of any modules between the present module and this module.");

        parameters.get(SHOW_REDIRECT_MESSAGE)
                .setDescription("Controls if a message should be displayed in the log if redirection occurs.");

        parameters.get(REDIRECT_MESSAGE).setDescription("Message to display if redirection occurs.");

        parameters.get(SHOW_TERMINATION_WARNING).setDescription(
                "When selected, a warning will be displayed in the terminal if terminating a workflow early.");

        parameters.get(EXPORT_WORKSPACE).setDescription(
                "Controls if the workspace should still be exported to the output Excel spreadsheet if termination occurs.");

        parameters.get(REMOVE_IMAGES).setDescription(
                "Controls if images should be completely removed from the workspace along with any associated measurements if termination occurs.");

        parameters.get(REMOVE_OBJECTS).setDescription(
                "Controls if objects should be completely removed from the workspace along with any associated measurements if termination occurs.");

    }
}