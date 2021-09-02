package io.github.mianalysis.mia.macro.objectprocessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Obj;
import io.github.mianalysis.mia.Object.Objs;
import io.github.mianalysis.mia.Object.Workspace;

public class MIA_GetObjectChildIDs extends MacroOperation {
    public MIA_GetObjectChildIDs(MacroExtension theHandler) {
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
        String childObjectsName = (String) objects[2];

        // Getting the children of the input object
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
        if (inputObjects == null) return "";
        Obj inputObject = inputObjects.get(objectID);
        Objs childObjects = inputObject.getChildren(childObjectsName);

        StringBuilder sb = new StringBuilder();
        for (Obj childObject:childObjects.values()){
            if (sb.length() == 0) {
                sb.append(childObject.getID());
            } else {
                sb.append(",").append(childObject.getID());
            }
        }

        return sb.toString();

    }

    @Override
    public String getArgumentsDescription() {
        return "String inputObjectsName, int objectID, String childObjectsName";
    }

    @Override
    public String getDescription() {
        return "Returns a comma-delimited list of child object ID numbers for the specified input object.";
    }
}
