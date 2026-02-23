package io.github.mianalysis.mia.macro.objectmeasurements.spatial;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectCentroid;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureObjectCentroid extends MacroOperation {
    public MIA_MeasureObjectCentroid(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
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
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the centroid of each object.";
    }
}
