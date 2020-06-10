package wbif.sjx.MIA.Macro.Visualisation;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

public class ShowAllObjectMeasurementsMacro extends MacroOperation {
    public ShowAllObjectMeasurementsMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ShowAllObjectMeasurements";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        ObjCollection objCollection = workspace.getObjectSet((String) objects[0]);

        objCollection.showAllMeasurements();

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName";
    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with an object";
    }
}
