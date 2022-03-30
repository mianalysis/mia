package io.github.mianalysis.mia.macro.imagemeasurements;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.image.ImageFactory;
import io.github.mianalysis.mia.object.measurements.Measurement;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_GetImageMeasurement extends MacroOperation {
    public MIA_GetImageMeasurement(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
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
