package io.github.mianalysis.MIA.Macro.General;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_GetMetadataValue extends MacroOperation {
    public MIA_GetMetadataValue(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
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
    public String getDescription() {
        return "Returns the metadata value matching the specified name.";
    }
}
