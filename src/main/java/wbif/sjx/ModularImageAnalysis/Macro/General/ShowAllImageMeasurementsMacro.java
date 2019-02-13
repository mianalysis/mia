package wbif.sjx.ModularImageAnalysis.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

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
    public String action(Object[] objects, Workspace workspace) {
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
