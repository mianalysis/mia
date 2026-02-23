package io.github.mianalysis.mia.macro.imageprocessing;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.image.ImageI;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_ListImagesInWorkspace extends MacroOperation {
    public MIA_ListImagesInWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[0];
    }

    @Override
    public String action(Object[] objects, WorkspaceI workspace, ModulesI modules) {
        // Creating a new ResultsTable to hold the Image names
        ResultsTable rt = new ResultsTable();
        int row = 0;

        // Getting a list of Images in the Workspace
        HashMap<String,ImageI> images = workspace.getImages();
        for (String imageName:images.keySet()) {
            if (row != 0) rt.incrementCounter();

            String measurementsOnly = Boolean.toString(images.get(imageName).getImagePlus() == null);

            rt.setValue("Image name",row,imageName);
            rt.setValue("Measurements only",row,measurementsOnly);

            row++;

        }

        rt.show("Images in workspace");

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "";
    }

    @Override
    public String getVersionNumber() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Returns a list of images currently in the workspace.  \"Measurements only\" is true if the image data"+
                " has been removed, leaving measurements only";
    }
}
