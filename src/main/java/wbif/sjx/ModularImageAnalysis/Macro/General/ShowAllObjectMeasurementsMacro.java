package wbif.sjx.ModularImageAnalysis.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

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
    public String action(Object[] objects, Workspace workspace) {
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
