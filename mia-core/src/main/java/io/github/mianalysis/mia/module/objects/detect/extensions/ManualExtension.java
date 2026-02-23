package io.github.mianalysis.mia.module.objects.detect.extensions;

import javax.swing.JPanel;

import org.scijava.plugin.SciJavaPlugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.system.Status;

public interface ManualExtension extends SciJavaPlugin {
    public void setModule(ModuleI module);
    
    public Status initialiseBeforeImageShown(WorkspaceI workspace);

    public Status initialiseAfterImageShown(@Nullable ImagePlus displayIpl);

    public Status onObjectAdded();

    public Status onFinishAddingObjects();

    public Parameters initialiseParameters();

    public Parameters updateAndGetParameters();

    public boolean skipAutoAccept();

    public JPanel getControlPanel();

    public Parameters getAllParameters();

}
