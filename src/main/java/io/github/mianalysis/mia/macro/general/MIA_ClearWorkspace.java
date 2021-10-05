package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
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
