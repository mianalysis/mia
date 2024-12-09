package io.github.mianalysis.mia.macro.general;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetMetadataValue extends MacroOperation {
    public MIA_GetMetadataValue(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        String metadataName = (String) objects[0];

        // Returning metadata value
        if (!workspace.getMetadata().containsKey(metadataName)) return "";
        return workspace.getMetadata().getAsString(metadataName);

    }

    @Override
    public String getArgumentsDescription() {
        return "String metadataName";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns the metadata value matching the specified name.";
    }
}
