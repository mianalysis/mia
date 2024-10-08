package io.github.mianalysis.mia.module.objects.detect.extensions;

import org.scijava.plugin.SciJavaPlugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.image.Image;
import io.github.mianalysis.mia.object.parameters.Parameters;

public abstract class ManualExtension implements SciJavaPlugin {
    protected Module module;
    protected Parameters parameters = new Parameters();

    public ManualExtension(Module module) {
        this.module = module;
        initialiseParameters();
    }

    public abstract void initialiseBeforeImageShown(Workspace workspace, Image image);

    public abstract void initialiseAfterImageShown(@Nullable ImagePlus displayIpl);

    protected abstract Parameters initialiseParameters();

    public abstract Parameters updateAndGetParameters();

    public Parameters getAllParameters() {
        return parameters;
    }

}
