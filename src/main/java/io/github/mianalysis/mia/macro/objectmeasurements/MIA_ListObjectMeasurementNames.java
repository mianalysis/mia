package io.github.mianalysis.mia.macro.objectmeasurements;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.refs.ObjMeasurementRef;
import io.github.mianalysis.mia.object.refs.collections.ObjMeasurementRefs;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_ListObjectMeasurementNames extends MacroOperation {
    public MIA_ListObjectMeasurementNames(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        String objectName = (String) objects[0];

        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        ObjMeasurementRefs measurements = modules.getObjectMeasurementRefs(objectName);
        for (ObjMeasurementRef measurement:measurements.values()) {
            if (row != 0) rt.incrementCounter();

            rt.setValue("Measurement name",row,measurement.getName());
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
