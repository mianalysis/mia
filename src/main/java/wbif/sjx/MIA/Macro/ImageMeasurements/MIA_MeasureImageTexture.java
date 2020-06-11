package wbif.sjx.MIA.Macro.ImageMeasurements;

import ij.macro.MacroExtension;
import wbif.sjx.MIA.Macro.MacroOperation;
import wbif.sjx.MIA.Module.ImageMeasurements.MeasureImageTexture;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Workspace;

public class MIA_MeasureImageTexture extends MacroOperation {
    public MIA_MeasureImageTexture(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, ModuleCollection modules) {
        MeasureImageTexture measureImageTexture = new MeasureImageTexture(modules);

        measureImageTexture.updateParameterValue(MeasureImageTexture.INPUT_IMAGE,objects[0]);
        measureImageTexture.updateParameterValue(MeasureImageTexture.X_OFFSET,(int) Math.round((double) objects[1]));
        measureImageTexture.updateParameterValue(MeasureImageTexture.Y_OFFSET,(int) Math.round((double) objects[2]));
        measureImageTexture.updateParameterValue(MeasureImageTexture.Z_OFFSET,(int) Math.round((double) objects[3]));
        measureImageTexture.setShowOutput((double) objects[4] == 1);

        measureImageTexture.process(workspace);

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
