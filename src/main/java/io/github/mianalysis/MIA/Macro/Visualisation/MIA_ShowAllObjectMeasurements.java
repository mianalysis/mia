package io.github.mianalysis.MIA.Macro.Visualisation;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Objs;
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
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        Objs objCollection = workspace.getObjectSet((String) objects[0]);

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
