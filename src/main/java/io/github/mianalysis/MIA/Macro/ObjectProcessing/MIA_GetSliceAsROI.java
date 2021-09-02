package io.github.mianalysis.mia.macro.objectprocessing;

import ij.gui.Roi;
import ij.macro.MacroExtension;
import ij.plugin.frame.RoiManager;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Obj;
import io.github.mianalysis.mia.Object.Objs;
import io.github.mianalysis.mia.Object.Workspace;

public class MIA_GetSliceAsROI extends MacroOperation {
    public MIA_GetSliceAsROI(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING, ARG_NUMBER, ARG_NUMBER };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        String inputObjectsName = (String) objects[0];
        int inputObjectsID = (int) Math.round((Double) objects[1]);
        int slice = (int) Math.round((Double) objects[2]);        

        // Getting the input objects
        Objs inputObjects = workspace.getObjectSet(inputObjectsName);
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
