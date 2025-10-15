package io.github.mianalysis.mia.macro.objectprocessing;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetObjectChildIDs extends MacroOperation {
    public MIA_GetObjectChildIDs(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String childObjectsName = (String) objects[2];

        // Getting the children of the input object
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        if (inputObjects == null) return "";
        ObjI inputObject = inputObjects.get(objectID);
        ObjsI childObjects = inputObject.getChildren(childObjectsName);

        StringBuilder sb = new StringBuilder();
        for (ObjI childObject:childObjects.values()){
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns a comma-delimited list of child object ID numbers for the specified input object.";
    }
}
