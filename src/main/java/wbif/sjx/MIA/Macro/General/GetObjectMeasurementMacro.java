package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.*;

public class GetObjectMeasurementMacro extends MacroOperation {
    public GetObjectMeasurementMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_GetObjectMeasurement";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String objectName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String measurementName = (String) objects[2];

        // Getting the object set
        ObjCollection objCollection = workspace.getObjectSet(objectName);
        if (objCollection == null) return "";

        // Getting the object
        if (!objCollection.keySet().contains(objectID)) return "";
        Obj obj = objCollection.get(objectID);

        // Getting the measurement
        Measurement measurement = obj.getMeasurement(measurementName);
        if (measurement == null) return "";

        // Returning measurement value
        return String.valueOf(measurement.getValue());

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID, String measurementName";
    }

    @Override
    public String getDescription() {
        return "Returns the specified measurement value for the specified object.";
    }
}
