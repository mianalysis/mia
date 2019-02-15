package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Spatial.MeasureObjectOverlap;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class MeasureObjectOverlapMacro extends MacroOperation {
    public MeasureObjectOverlapMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureObjectOverlap";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        MeasureObjectOverlap measureObjectOverlap = new MeasureObjectOverlap();

        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_1,objects[0]);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.OBJECT_SET_2,objects[1]);
        measureObjectOverlap.updateParameterValue(MeasureObjectOverlap.LINK_IN_SAME_FRAME,(double) objects[2] == 1);
        measureObjectOverlap.setShowOutput((double) objects[3] == 1);

        measureObjectOverlap.run(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName1, String objectsName2, boolean linkInSameFrame, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Calculates the total voxel and percentage overlap between objects.  Can be restricted to only "+
                "comparing objects present in the same timepoint.";
    }
}
