package io.github.mianalysis.MIA.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Image;
import io.github.mianalysis.MIA.Object.Measurement;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_GetImageMeasurement extends MacroOperation {
    public MIA_GetImageMeasurement(MacroExtension theHandler) {
        super(theHandler);
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
