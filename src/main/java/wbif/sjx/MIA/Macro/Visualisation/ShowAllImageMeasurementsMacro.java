package wbif.sjx.MIA.Macro.Visualisation;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ShowAllImageMeasurementsMacro extends MacroOperation {
    public ShowAllImageMeasurementsMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ShowAllImageMeasurements";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        workspace.getImage((String) objects[0]).showAllMeasurements();
        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName";
    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with an image";
    }
}
