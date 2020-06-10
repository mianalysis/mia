package wbif.sjx.MIA.Macro.ObjectMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.*;

public class SetObjectMeasurementMacro extends MacroOperation {
    public SetObjectMeasurementMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_SetObjectMeasurement";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String objectName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String measurementName = (String) objects[2];
        double measurementValue = (double) objects[3];

        // Getting the object set
        ObjCollection objCollection = workspace.getObjectSet(objectName);
        if (objCollection == null) return "";

        // Getting the object
        if (!objCollection.keySet().contains(objectID)) return "";
        Obj obj = objCollection.get(objectID);

        // Getting the measurement
        Measurement measurement = obj.getMeasurement(measurementName);
        if (measurement == null) {
            measurement = new Measurement(measurementName, measurementValue);
            obj.addMeasurement(measurement);
        } else {
            measurement.setValue(measurementValue);
        }

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectName, Integer objectID, String measurementName, Double measurementValue";
    }

    @Override
    public String getDescription() {
        return "Sets the value of the specified measurement for the specified object.";
    }
}
