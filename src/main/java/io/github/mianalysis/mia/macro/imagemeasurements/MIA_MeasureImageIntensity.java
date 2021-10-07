package io.github.mianalysis.mia.macro.imagemeasurements;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.imagemeasurements.MeasureImageIntensity;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureImageIntensity extends MacroOperation {
    public MIA_MeasureImageIntensity(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
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
