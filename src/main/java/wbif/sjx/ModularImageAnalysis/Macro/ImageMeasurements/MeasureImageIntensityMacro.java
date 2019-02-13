package wbif.sjx.ModularImageAnalysis.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageIntensity;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class MeasureImageIntensityMacro extends MacroOperation {
    public MeasureImageIntensityMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureImageIntensity";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        MeasureImageIntensity measureImageIntensity = new MeasureImageIntensity();

        measureImageIntensity.updateParameterValue(MeasureImageIntensity.INPUT_IMAGE,objects[0]);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MEAN,(double) objects[1] == 1);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MIN,(double) objects[2] == 1);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_MAX,(double) objects[3] == 1);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_STDEV,(double) objects[4] == 1);
        measureImageIntensity.updateParameterValue(MeasureImageIntensity.MEASURE_SUM,(double) objects[5] == 1);
        measureImageIntensity.setShowOutput((double) objects[6] == 1);

        measureImageIntensity.run(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, boolean measureMean, boolean measureMin, boolean measureMax, boolean measureStd, "+
                "boolean measureSum, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure the intensity of the specified image.";
    }
}
