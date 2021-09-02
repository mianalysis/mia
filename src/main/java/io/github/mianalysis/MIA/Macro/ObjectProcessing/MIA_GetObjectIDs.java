package io.github.mianalysis.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.Modules;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.Objs;
import io.github.mianalysis.MIA.Object.Workspace;

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