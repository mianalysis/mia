package io.github.mianalysis.MIA.Macro.ObjectProcessing;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Obj;
import io.github.mianalysis.MIA.Object.ObjCollection;
import io.github.mianalysis.MIA.Object.Workspace;

import java.util.HashMap;

public class MIA_ListObjectsInWorkspace extends MacroOperation {
    public MIA_ListObjectsInWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        HashMap<String,ObjCollection> allObj = workspace.getObjects();
        for (String objName:allObj.keySet()) {
            if (row != 0) rt.incrementCounter();

            boolean measTest = false;
            Obj firstObj = allObj.get(objName).getFirst();
            if (firstObj != null) measTest = firstObj.size() == 0;
            String measurementsOnly = Boolean.toString(measTest);

            rt.setValue("Objects name",row,objName);
            rt.setValue("Number of objects",row,allObj.get(objName).size());
            rt.setValue("Measurements only",row,measurementsOnly);

            row++;

        }

        rt.show("Objects in workspace");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Returns a list of objects currently in the workspace.  \"Measurements only\" is true if the coordinate"+
                " data has been removed, leaving measurements only";
    }
}
