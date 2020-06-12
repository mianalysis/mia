package wbif.sjx.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_GetObjectParentID extends MacroOperation {
    public MIA_GetObjectParentID(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String inputObjectsName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String parentObjectsName = (String) objects[2];

        // Getting the children of the input object
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
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
