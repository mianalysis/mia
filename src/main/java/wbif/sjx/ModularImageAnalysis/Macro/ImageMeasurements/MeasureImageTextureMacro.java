package wbif.sjx.ModularImageAnalysis.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class MeasureImageTextureMacro extends MacroOperation {
    public MeasureImageTextureMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_MeasureImageTexture";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        MeasureImageTexture measureImageTexture = new MeasureImageTexture();

        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,objects[0]);
        measureImageTexture.updateParameterValue(MeasureImageTexture.X_OFFSET,(int) objects[1]);
        measureImageTexture.updateParameterValue(MeasureImageTexture.Y_OFFSET,(int) objects[2]);
        measureImageTexture.updateParameterValue(MeasureImageTexture.Z_OFFSET,(int) objects[3]);
        measureImageTexture.setShowOutput((double) objects[4] == 1);

        measureImageTexture.run(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, int xOffset, int yOffset, int zOffset, boolean showResults";
    }

    @Override
    public String getDescription() {
        return "Measure texture across the image.  Calculates the Haralick features.  Offset provided in pixel untis";
    }
}
