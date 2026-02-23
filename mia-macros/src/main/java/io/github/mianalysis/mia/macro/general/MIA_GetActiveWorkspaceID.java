package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.WorkspaceI;

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
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
        return String.valueOf(workspace.getID());

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns ID of active Workspace.";
    }
}
