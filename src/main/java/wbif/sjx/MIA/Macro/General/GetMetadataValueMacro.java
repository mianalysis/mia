package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.HCMetadata;

import java.util.HashMap;

public class GetMetadataValueMacro extends MacroOperation {
    public GetMetadataValueMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_GetMetadataValue";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
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
