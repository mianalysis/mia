package io.github.mianalysis.mia.macro.imagemeasurements;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.images.measure.MeasureImageTexture;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureImageTexture extends MacroOperation {
    public MIA_MeasureImageTexture(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
        MeasureImageTexture measureImageTexture = new MeasureImageTexture(modules);

        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,objects[0]);
        measureImageTexture.updateParameterValue(MeasureImageTexture.X_OFFSET,(int) Math.round((double) objects[1]));
        measureImageTexture.updateParameterValue(MeasureImageTexture.Y_OFFSET,(int) Math.round((double) objects[2]));
        measureImageTexture.updateParameterValue(MeasureImageTexture.Z_OFFSET,(int) Math.round((double) objects[3]));
        measureImageTexture.setShowOutput((double) objects[4] == 1);

        measureImageTexture.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, int xOffset, int yOffset, int zOffset, boolean showResults";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure texture across the image.  Calculates the Haralick features.  Offset provided in pixel untis";
    }
}
