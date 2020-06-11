package wbif.sjx.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectProcessing.Refinement.RemoveObjects;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_RemoveObjectsFromWorkspace extends MacroOperation {
    public MIA_RemoveObjectsFromWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        RemoveObjects removeObjects = new RemoveObjects(modules);

        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,objects[0]);
        removeObjects.updateParameterValue(RemoveObjects.RETAIN_MEASUREMENTS,(double) objects[1] == 1);

        removeObjects.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, boolean retainMeasurements";
    }

    @Override
    public String getDescription() {
        return "Removes the specified objects from the workspace.  If \"Retain measurements\" is true, any "+
                "measurements will be left available for export.";
    }

}
