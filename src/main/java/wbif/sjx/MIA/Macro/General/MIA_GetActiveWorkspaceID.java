package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_GetActiveWorkspaceID extends MacroOperation {
    public MIA_GetActiveWorkspaceID(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
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
