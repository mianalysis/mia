package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectShape;
import wbif.sjx.MIA.Object.Workspace;

public class MeasureObjectShapeMacro extends MacroOperation {
    public MeasureObjectShapeMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureObjectShape";
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
