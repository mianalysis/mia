package io.github.mianalysis.mia.macro.visualisation;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.visualisation.ShowImage;
import io.github.mianalysis.mia.object.Workspace;

public class MIA_ShowImage extends MacroOperation {
    public MIA_ShowImage(MacroExtension theHandler) {
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
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        // Create Module
        ShowImage showImage = new ShowImage(modules);

        // Updating parameters
        showImage.updateParameterValue(ShowImage.DISPLAY_IMAGE,(String) objects[0]);

        showImage.process(workspace);

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
