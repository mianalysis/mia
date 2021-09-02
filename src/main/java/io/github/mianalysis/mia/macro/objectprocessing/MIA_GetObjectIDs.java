package io.github.mianalysis.mia.macro.objectprocessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Obj;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;

public class MIA_GetObjectIDs extends MacroOperation {
    public MIA_GetObjectIDs(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];

        // Getting the input objects
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
        if (inputObjects == null) return "";
                
        StringBuilder sb = new StringBuilder();
        for (Obj inputObject:inputObjects.values()){
            if (sb.length() == 0) {
                sb.append(inputObject.getID());
            } else {
                sb.append(",").append(inputObject.getID());
            }
        }

        return sb.toString();

    }

    @Override
    public String getArgumentsDescription() {
        return "String inputObjectsName";
    }

    @Override
    public String getDescription() {
        return "Returns a comma-delimited list of object ID numbers for the specified input objects.";
    }
}
