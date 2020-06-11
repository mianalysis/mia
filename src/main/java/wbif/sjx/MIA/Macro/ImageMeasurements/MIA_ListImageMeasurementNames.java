package wbif.sjx.MIA.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.References.ImageMeasurementRef;
import wbif.sjx.MIA.Object.References.ImageMeasurementRefCollection;
import wbif.sjx.MIA.Object.References.ObjMeasurementRef;
import wbif.sjx.MIA.Object.References.ObjMeasurementRefCollection;

public class MIA_ListImageMeasurementNames extends MacroOperation {
    public MIA_ListImageMeasurementNames(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[] { ARG_STRING };
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String imageName = (String) objects[0];

        // Creating a new ResultsTable to hold the measurement names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        ImageMeasurementRefCollection measurements = modules.getImageMeasurementRefs(imageName);
        for (ImageMeasurementRef measurement:measurements.values()) {
            if (row != 0) rt.incrementCounter();

            rt.setValue("Measurement name",row,measurement.getFinalName());
            rt.setValue("Measurement nickname",row,measurement.getNickname());

            row++;

        }

        rt.show("Measurements for \""+imageName+"\" image");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName";
    }

    @Override
    public String getDescription() {
        return "Returns a list of available measurements for the specified image." +
                "\n\nUse the macro Ext.MIA_ShowAllImageMeasurements(imageName) to get a table containing all measurement values for the image." +
                "\n\nUse the macro Ext.MIA_GetImageMeasurement(imageName, measurementID) to get a specific measurement value for a specific image.";
    }
}
