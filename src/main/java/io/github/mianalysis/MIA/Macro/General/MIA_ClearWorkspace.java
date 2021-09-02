package io.github.mianalysis.MIA.Macro.General;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_ClearWorkspace extends MacroOperation {
    public MIA_ClearWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
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
