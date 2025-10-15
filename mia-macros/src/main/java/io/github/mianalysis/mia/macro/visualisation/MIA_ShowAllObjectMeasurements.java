package io.github.mianalysis.mia.macro.visualisation;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_ShowAllObjectMeasurements extends MacroOperation {
    public MIA_ShowAllObjectMeasurements(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        ObjsI objCollection = workspace.getObjects((String) objects[0]);

        objCollection.showAllMeasurements();

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Displays all measurements associated with an object";
    }
}
