package io.github.mianalysis.mia.macro.objectprocessing;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.coordinates.Point;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetObjectCoordinates extends MacroOperation {
    public MIA_GetObjectCoordinates(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING, ARG_NUMBER };
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);

        // Getting the input objects
        Objs inputObjects = workspace.getObjects(inputObjectsName);
        if (inputObjects == null)
            return "";
        
        Obj inputObject = inputObjects.get(inputObjectsID);

        StringBuilder sb = new StringBuilder();        
        for (Point<Integer> point : inputObject.getCoordinateSet()) {
            sb.append(point.getX()).append(",");
            sb.append(point.getY()).append(",");
            sb.append(point.getZ()).append(";");
        }
                
        // Removing the final semicolon
        sb.deleteCharAt(sb.length() - 1);
        
        return sb.toString();
        
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns the xyz object coordinates of the specified object as a string in the format \"x1,y1,z1;x2,y2,z2;...;xN,yN,zN\" where N is the number of coordinates.  X and Y values are specified in pixel units and Z in slice units.";
    }
}
