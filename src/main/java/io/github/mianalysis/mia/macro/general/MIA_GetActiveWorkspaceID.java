package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetActiveWorkspaceID extends MacroOperation {
    public MIA_GetActiveWorkspaceID(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        return String.valueOf(workspace.getID());

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns ID of active Workspace.";
    }
}
