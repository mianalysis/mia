package wbif.sjx.ModularImageAnalysis.Macro.General;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Object.Obj;
import wbif.sjx.ModularImageAnalysis.Object.ObjCollection;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

import java.util.HashMap;

public class ListObjectsInWorkspaceMacro extends MacroOperation {
    public ListObjectsInWorkspaceMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ListObjectsInWorkspace";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        HashMap<String,ObjCollection> objCollection = workspace.getObjects();
        for (String objName:objCollection.keySet()) {
            boolean measTest = false;
            Obj firstObj = objCollection.get(objName).getFirst();
            if (firstObj != null) measTest = firstObj.getNVoxels() == 0;
            String measurementsOnly = Boolean.toString(measTest);

            rt.setValue("Objects name",row,objName);
            rt.setValue("Number of objects",row,objCollection.size());
            rt.setValue("Measurements only",row,measurementsOnly);

            rt.incrementCounter();
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
