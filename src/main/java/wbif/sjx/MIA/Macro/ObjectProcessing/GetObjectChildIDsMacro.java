package wbif.sjx.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

public class GetObjectChildIDsMacro extends MacroOperation {
    public GetObjectChildIDsMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_GetObjectChildIDs";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        String inputObjectsName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String childObjectsName = (String) objects[2];

        // Getting the children of the input object
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        if (inputObjects == null) return "";
        Obj inputObject = inputObjects.get(objectID);
        ObjCollection childObjects = inputObject.getChildren(childObjectsName);

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
