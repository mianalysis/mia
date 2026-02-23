package io.github.mianalysis.mia.macro.objectmeasurements.intensity;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.objects.measure.intensity.MeasureObjectIntensity;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureObjectIntensity extends MacroOperation {
    public MIA_MeasureObjectIntensity(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER};

    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity(modules);

        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,objects[0]);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,objects[1]);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,(double) objects[2] == 1);
        measureObjectIntensity.setShowOutput((double) objects[3] ==1);

        measureObjectIntensity.process(workspace);

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, String imageName, boolean measureWeightedCentre, boolean showResults";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Measure intensity for specified objects.";
    }

}
