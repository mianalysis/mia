package wbif.sjx.ModularImageAnalysis.Macro.Visualisation;

import ij.macro.MacroExtension;
import wbif.sjx.ModularImageAnalysis.Macro.MacroOperation;
import wbif.sjx.ModularImageAnalysis.Module.Visualisation.ShowImage;
import wbif.sjx.ModularImageAnalysis.Object.Workspace;

public class ShowImageMacro extends MacroOperation {
    public ShowImageMacro(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public String getName() {
        return "MIA_ShowImage";
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING};
    }

    @Override
    public String action(Object[] objects, Workspace workspace) {
        // Create Module
        ShowImage showImage = new ShowImage();

        // Updating parameters
        showImage.updateParameterValue(ShowImage.DISPLAY_IMAGE,(String) objects[0]);

        showImage.run(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName";
    }

    @Override
    public String getDescription() {
        return "Duplicate an image from the MIA workspace and display it.  Note: As this is a duplicate image, "
        +"changes made to it won't be reflected in the MIA workspace copy";
    }
}
