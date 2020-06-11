package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

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
