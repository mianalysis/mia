package io.github.mianalysis.mia.macro.objectprocessing;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetObjectParentID extends MacroOperation {
    public MIA_GetObjectParentID(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String parentObjectsName = (String) objects[2];

        // Getting the children of the input object
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        if (inputObjects == null) return "";
        Obj inputObject = inputObjects.get(objectID);
        Obj parentObject = inputObject.getParent(parentObjectsName);

        return String.valueOf(parentObject.getID());

    }

    @Override
    public String getArgumentsDescription() {
        return "String inputObjectsName, int objectID, String parentObjectsName";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns the object ID for the parent of the specified input object.";
    }
}
