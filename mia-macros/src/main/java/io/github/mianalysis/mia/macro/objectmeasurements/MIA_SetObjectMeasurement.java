package io.github.mianalysis.mia.macro.objectmeasurements;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Objs;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.coordinates.Obj;
import io.github.mianalysis.mia.object.measurements.Measurement;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_SetObjectMeasurement extends MacroOperation {
    public MIA_SetObjectMeasurement(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, Modules modules) {
        String objectName = (String) objects[0];
        int objectID = (int) Math.round((Double) objects[1]);
        String measurementName = (String) objects[2];
        double measurementValue = (double) objects[3];

        // Getting the object set
        Objs objCollection = workspace.getObjects(objectName);
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Sets the value of the specified measurement for the specified object.";
    }
}
