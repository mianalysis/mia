package io.github.mianalysis.mia.macro.objectmeasurements.spatial;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.objects.measure.spatial.MeasureObjectOverlap;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_MeasureObjectOverlap extends MacroOperation {
    public MIA_MeasureObjectOverlap(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap(modules);

        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objects[0]);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objects[1]);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.LINK_IN_SAME_FRAME,(double) objects[2] == 1);
        measureObjectOverlap.setShowOutput((double) objects[3] == 1);

        measureObjectOverlap.process(workspace);
        
        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName1, String objectsName2, boolean linkInSameFrame, boolean showResults";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Calculates the total voxel and percentage overlap between objects.  Can be restricted to only "+
                "comparing objects present in the same timepoint.";
    }
}
