package io.github.mianalysis.mia.macro.general;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroHandler;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;

public class MIA_SetActiveWorkspace extends MacroOperation {
    public MIA_SetActiveWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_NUMBER };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        int workspaceID = (int) Math.round((Double) objects[0]);

        // Getting this workspace
        Workspace newActiveWorkspace = workspace.getWorkspaces().getWorkspace(workspaceID);

        // If this Workspace is present, change the active Workspace
        if (newActiveWorkspace != null) MacroHandler.setWorkspace(newActiveWorkspace);

        // Returning the active Workspace
        return String.valueOf(MacroHandler.getWorkspace().getID());

    }

    @Override
    public String getArgumentsDescription() {
        return "Integer newActiveWorkspaceID";
    }

    @Override
    public String getDescription() {
        return "Sets the active Workspace to that with the specified ID.  If there is no Workspace matching this ID, the active Workspace remains unchanged.  The active Workspace ID is returned.";
    }
}
