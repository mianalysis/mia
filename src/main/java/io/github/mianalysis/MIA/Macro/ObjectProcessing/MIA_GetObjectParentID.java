package io.github.mianalysis.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Workspace;

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
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
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
    public String getDescription() {
        return "Returns the object ID for the parent of the specified input object.";
    }
}
