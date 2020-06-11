package wbif.sjx.MIA.Macro.ObjectMeasurements;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

public class MIA_ListObjectMeasurementNames extends MacroOperation {
    public MIA_ListObjectMeasurementNames(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String objectName = (String) objects[0];

        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        ObjMeasurementRefCollection measurements = modules.getObjectMeasurementRefs(objectName);
        for (ObjMeasurementRef measurement:measurements.values()) {
            if (row != 0) rt.incrementCounter();

            rt.setValue("Measurement name",row,measurement.getFinalName());
            rt.setValue("Measurement nickname",row,measurement.getNickname());

            row++;

        }

        rt.show("Measurements for \""+objectName+"\" objects");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName";
    }

    @Override
    public String getDescription() {
        return "Returns a list of available measurements for the specified object." +
                "\n\nUse the macro Ext.MIA_ShowAllObjectMeasurements(objectsName) to get a table containing all measurement values for all objects." +
                "\n\nUse the macro Ext.MIA_GetObjectMeasurement(objectsName, objectID, measurementID) to get a specific measurement value for a specific object.";
    }
}
