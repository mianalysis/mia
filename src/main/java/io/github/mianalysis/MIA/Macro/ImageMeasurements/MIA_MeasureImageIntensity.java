package io.github.mianalysis.MIA.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ImageMeasurements.MeasureImageIntensity;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_MeasureImageIntensity extends MacroOperation {
    public MIA_MeasureImageIntensity(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity(modules);

        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,objects[0]);
        measureImageIntensity.setShowOutput((double) objects[1] == 1);

        measureImageIntensity.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure the intensity of the specified image.";
    }
}
