package io.github.mianalysis.mia.macro.objectmeasurements.spatial;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectCentroid;
import io.github.mianalysis.mia.object.Workspace;

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
    public String action(Object[] objects, Workspace workspace, Modules modules) {
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
