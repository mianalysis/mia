package io.github.mianalysis.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import io.github.mianalysis.MIA.Macro.MacroOperation;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import io.github.mianalysis.MIA.Object.Workspace;

public class MIA_MeasureObjectCentroid extends MacroOperation {
    public MIA_MeasureObjectCentroid(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid(modules);

        measureObjectCentroid.updateParameterValue(MeasureObjectCentroid.INPUT_OBJECTS,objects[0]);
        measureObjectCentroid.setShowOutput((double) objects[2] == 1);

        measureObjectCentroid.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Calculates the centroid of each object.";
    }
}
