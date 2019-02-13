package wbif.sjx.ModularImageAnalysis.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ObjectProcessing.Refinement.RemoveObjects;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class RemoveObjectsFromWorkspaceMacro extends MacroOperation {
    public RemoveObjectsFromWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_RemoveObjectsFromWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        RemoveObjects removeObjects = new RemoveObjects();

        removeObjects.updateParameterValue(RemoveObjects.INPUT_OBJECTS,objects[0]);
        removeObjects.updateParameterValue(RemoveObjects.RETAIN_MEASUREMENTS,(double) objects[1] == 1);

        removeObjects.run(workspace);

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
