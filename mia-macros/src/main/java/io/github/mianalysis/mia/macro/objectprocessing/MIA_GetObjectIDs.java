package io.github.mianalysis.mia.macro.objectprocessing;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetObjectIDs extends MacroOperation {
    public MIA_GetObjectIDs(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
        String inputObjectsName = (String) objects[0];

        // Getting the input objects
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        if (inputObjects == null) return "";
                
        StringBuilder sb = new StringBuilder();
        for (ObjI inputObject:inputObjects.values()){
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns a comma-delimited list of object ID numbers for the specified input objects.";
    }
}
