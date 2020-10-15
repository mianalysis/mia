package wbif.sjx.MIA.Macro.ObjectProcessing;

import ij.gui.Roi;
import ij.macro.MacroExtension;
import ij.plugin.frame.RoiManager;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Obj;
import wbif.sjx.MIA.Object.ObjCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_GetSliceAsROI extends MacroOperation {
    public MIA_GetSliceAsROI(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING, ARG_NUMBER, ARG_NUMBER };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);
        int slice = (int) Math.round((Double) objects[2]);        

        // Getting the input objects
        ObjCollection inputObjects = workspace.getObjectSet(inputObjectsName);
        if (inputObjects == null)
            return "";
        
        Obj inputObject = inputObjects.get(inputObjectsID);

        Roi roi = inputObject.getRoi(slice);

        RoiManager roiManager = RoiManager.getInstance2();
        roiManager.addRoi(roi);
                
        return "";
        
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID, Integer slice (zero-based)";
    }

    @Override
    public String getDescription() {
        return "Adds the specific object Z-slice as a ROI in the ImageJ ROI Manager.";
    }
}
