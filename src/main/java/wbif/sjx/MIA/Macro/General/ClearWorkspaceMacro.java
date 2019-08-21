package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ClearWorkspaceMacro extends MacroOperation {
    public ClearWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ClearWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        workspace.clearAllImages(false);
        workspace.clearAllObjects(false);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Removes all images and objects from the workspace.  This should be generateModuleList at the beginning of a macro.";
    }
}
