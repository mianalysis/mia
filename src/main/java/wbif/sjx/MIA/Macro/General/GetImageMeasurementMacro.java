package wbif.sjx.MIA.Macro.General;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Image;
import wbif.sjx.MIA.Object.Measurement;
import wbif.sjx.MIA.Object.Workspace;

public class GetImageMeasurementMacro extends MacroOperation {
    public GetImageMeasurementMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_GetImageMeasurement";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        String imageName = (String) objects[0];
        String measurementName = (String) objects[1];

        // Getting the object set
        Image image = workspace.getImage(imageName);
        if (image == null) return "";

        // Getting the measurement
        Measurement measurement = image.getMeasurement(measurementName);
        if (measurement == null) return "";

        // Returning measurement value
        return String.valueOf(measurement.getValue());

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, String measurementName";
    }

    @Override
    public String getDescription() {
        return "Returns the specified measurement value for the specified image.";
    }
}
