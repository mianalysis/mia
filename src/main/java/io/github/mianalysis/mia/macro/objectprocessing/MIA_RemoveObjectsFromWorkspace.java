package io.github.mianalysis.mia.macro.objectprocessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objectprocessing.refinement.RemoveObjects;
import io.github.mianalysis.mia.object.Workspace;

public class MIA_RemoveObjectsFromWorkspace extends MacroOperation {
    public MIA_RemoveObjectsFromWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
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