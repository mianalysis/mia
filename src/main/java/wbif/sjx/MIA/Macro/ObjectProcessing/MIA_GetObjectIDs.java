package wbif.sjx.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_GetObjectIDs extends MacroOperation {
    public MIA_GetObjectIDs(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String inputObjectsName = (String) objects[0];

        // Getting the input objects
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        
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
