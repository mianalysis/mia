package io.github.mianalysis.MIA.Macro.Visualisation;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_ShowAllObjectMeasurements extends MacroOperation {
    public MIA_ShowAllObjectMeasurements(MacroExtension theHandler) {
        super(theHandler);
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
