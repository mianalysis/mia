package io.github.mianalysis.mia.module.objects.detect.extensions;

import javax.swing.JPanel;

import org.scijava.plugin.SciJavaPlugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.system.Status;

public abstract class ManualExtension implements SciJavaPlugin {
    protected Module module;
    protected Parameters parameters = new Parameters();

    public ManualExtension(Module module) {
        this.module = module;
        initialiseParameters();
    }

    public abstract Status initialiseBeforeImageShown(WorkspaceI workspace);

    public abstract Status initialiseAfterImageShown(@Nullable ImagePlus displayIpl);

    protected abstract Parameters initialiseParameters();

    public abstract Parameters updateAndGetParameters();

    public abstract boolean skipAutoAccept();

    public abstract JPanel getControlPanel();

    public Parameters getAllParameters() {
        return parameters;
    }

}
