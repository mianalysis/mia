package io.github.mianalysis.mia.macro.imageprocessing;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import ij.macro.MacroExtension;
import io.github.mianalysis.mia.macro.MacroOperation;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.RemoveImage;
import io.github.mianalysis.mia.object.Workspace;

@Plugin(type = MacroOperation.class, priority=Priority.LOW, visible=true)
public class MIA_RemoveImageFromWorkspace extends MacroOperation {
    public MIA_RemoveImageFromWorkspace(MacroExtension theHandler) {
        super(theHandler);
    }

    @Override
    public int[] getArgumentTypes() {
        return new int[]{ARG_STRING,ARG_NUMBER};
    }

    @Override
    public String action(Object[] objects, Workspace workspace, Modules modules) {
        RemoveImage removeImage = new RemoveImage(modules);

        removeImage.updateParameterValue(RemoveImage.INPUT_IMAGE,objects[0]);
        removeImage.updateParameterValue(RemoveImage.RETAIN_MEASUREMENTS,(double) objects[1] == 1);

        removeImage.process(workspace);

        return null;

    }

    @Override
    public String getArgumentsDescription() {
        return "String imageName, boolean retainMeasurements";
    }

    @Override
    public String getDescription() {
        return "Removes the specified image from the workspace.  If \"Retain measurements\" is true, any measurements"+
                " will be left available for export.";
    }
}
