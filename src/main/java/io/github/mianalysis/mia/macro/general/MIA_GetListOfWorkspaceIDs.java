package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetListOfWorkspaceIDs extends MacroOperation {
    public MIA_GetListOfWorkspaceIDs(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        // Getting Workspaces
        Workspaces workspaces = workspace.getWorkspaces();

        StringBuilder sb = new StringBuilder();
        for (Workspace currWorkspace : workspaces) {
            if (sb.length() == 0) {
                sb.append(currWorkspace.getID());
            } else {
                sb.append(",").append(currWorkspace.getID());
            }
        }
        
        return sb.toString();

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns a list of all Workspace IDs.  These can be used to enable a specific Workspace using the \"MIA_EnableWorkspace\" macro";
    }
}
