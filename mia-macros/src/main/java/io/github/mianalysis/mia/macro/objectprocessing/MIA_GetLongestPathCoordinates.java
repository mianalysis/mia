package io.github.mianalysis.mia.macro.objectprocessing;

import java.util.ArrayList;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.process.CreateSkeleton;
import io.github.mianalysis.mia.object.ObjsI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.ObjI;
import io.github.mianalysis.mia.object.coordinates.Point;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetLongestPathCoordinates extends MacroOperation {
    public MIA_GetLongestPathCoordinates(MacroExtension theHandler) {
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
        ObjsI inputObjects = workspace.getObjects(inputObjectsName);
        if (inputObjects == null)
            return "";
        
        ObjI inputObject = inputObjects.get(inputObjectsID);
        ArrayList<Point<Integer>> longestPath = CreateSkeleton.getLargestShortestPath(inputObject);

        StringBuilder sb = new StringBuilder();        
        for (Point<Integer> point : longestPath) {
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
        return "Extracts the longest path of the specified skeleton object and returns its xyz object coordinates as a string in the format \"x1,y1,z1;x2,y2,z2;...;xN,yN,zN\" where N is the number of coordinates.  Coordinates are returned in order along the longest path.  X and Y values are specified in pixel units and Z in slice units.";
    }
}
