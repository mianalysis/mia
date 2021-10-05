package io.github.mianalysis.mia.macro.objectmeasurements.intensity;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objectmeasurements.intensity.MeasureObjectTexture;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureObjectTexture extends MacroOperation {
    public MIA_MeasureObjectTexture(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};

    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        MeasureObjectTexture measureObjectTexture = new MeasureObjectTexture(modules);

        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_OBJECTS,objects[0]);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.INPUT_IMAGE,objects[1]);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.X_OFFSET,objects[2]);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Y_OFFSET,objects[3]);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.Z_OFFSET,objects[4]);
        measureObjectTexture.updateParameterValue(MeasureObjectTexture.CALIBRATED_OFFSET,(double) objects[5] == 1);
        measureObjectTexture.setShowOutput((double) objects[6] == 1);

        measureObjectTexture.process(workspace);

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, String imageName, int xOffset, int yOffset, int zOffset, boolean calibratedOffset, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure object texture across the image.  Calculates the Haralick features.  If \"calibratedOffset\" " +
                "is true offsets are in calibrated units, otherwise units are in pixels.";
    }
}
