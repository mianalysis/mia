package wbif.sjx.MIA.Macro.ObjectProcessing;

import java.util.ArrayList;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.FitSpline;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.common.Object.Point;
import wbif.sjx.common.Object.Vertex;

public class MIA_GetLongestPathCoordinates extends MacroOperation {
    public MIA_GetLongestPathCoordinates(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING, ARG_NUMBER };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);

        // Getting the input objects
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        if (inputObjects == null)
            return "";
        
        Obj inputObject = inputObjects.get(inputObjectsID);
        ArrayList<Vertex> longestPath = FitSpline.getSkeletonBackbone(inputObject);

        StringBuilder sb = new StringBuilder();        
        for (Vertex point : longestPath) {
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
    public String getDescription() {
        return "Extracts the longest path of the specified skeleton object and returns its xyz object coordinates as a string in the format \"x1,y1,z1;x2,y2,z2;...;xN,yN,zN\" where N is the number of coordinates.  Coordinates are returned in order along the longest path.  X and Y values are specified in pixel units and Z in slice units.";
    }
}
