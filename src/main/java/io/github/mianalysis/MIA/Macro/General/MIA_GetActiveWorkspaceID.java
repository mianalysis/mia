package io.github.mianalysis.MIA.Macro.General;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Workspace;

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
