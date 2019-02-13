package wbif.sjx.ModularImageAnalysis.Macro.ObjectMeasurements.Intensity;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ObjectMeasurements.Intensity.MeasureObjectIntensity;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class MeasureObjectIntensityMacro extends MacroOperation {
    public MeasureObjectIntensityMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureObjectIntensity";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_STRING,ARG_NUMBER,ARG_NUMBER};

    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        MeasureObjectIntensity measureObjectIntensity = new MeasureObjectIntensity();

        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_OBJECTS,objects[0]);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.INPUT_IMAGE,objects[1]);
        measureObjectIntensity.updateParameterValue(MeasureObjectIntensity.MEASURE_WEIGHTED_CENTRE,(double) objects[2] == 1);
        measureObjectIntensity.setShowOutput((double) objects[3] ==1);

        measureObjectIntensity.run(workspace);

        return null;
    }

    @Override
    public String getArgumentsDescription() {
        return "String objectsName, String imageName, boolean measureWeightedCentre, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure intensity for specified objects.";
    }
}
