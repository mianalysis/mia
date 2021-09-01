package io.github.mianalysis.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectShape;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_MeasureObjectShape extends MacroOperation {
    public MIA_MeasureObjectShape(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        MeasureObjectShape measureObjectShape = new MeasureObjectShape(modules);

        measureObjectShape.updateParameterValue(MeasureObjectShape.INPUT_OBJECTS,objects[0]);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_VOLUME,(double) objects[1] == 1);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_AREA,(double) objects[2] == 1);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_DIA,(double) objects[3] == 1);
        measureObjectShape.updateParameterValue(MeasureObjectShape.MEASURE_PROJECTED_PERIM,(double) objects[4] == 1);
        measureObjectShape.setShowOutput((double) objects[5] == 1);

        measureObjectShape.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, boolean measureVolume, boolean measureProjectedArea, boolean " +
                "measureProjectedDiameter, boolean measureProjectedPerimeter, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure shape properties for the specified objects.";
    }
}
