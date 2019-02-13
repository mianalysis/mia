package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class MeasureObjectShapeMacro extends MacroOperation {
    public MeasureObjectShapeMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
