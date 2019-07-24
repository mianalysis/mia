package wbif.sjx.MIA.Macro.ObjectMeasurements.Spatial;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectCentroid;
import wbif.sjx.MIA.Module.ObjectMeasurements.Spatial.MeasureObjectOverlap;
import wbif.sjx.MIA.Object.Workspace;

public class MeasureObjectCentroidMacro extends MacroOperation {
    public MeasureObjectCentroidMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureObjectCentroid";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        MeasureObjectCentroid measureObjectCentroid = new MeasureObjectCentroid(workspace.getAnalysis().getModules());

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
