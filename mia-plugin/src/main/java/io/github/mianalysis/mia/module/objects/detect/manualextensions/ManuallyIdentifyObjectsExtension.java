package io.github.mianalysis.mia.module.objects.detect.manualextensions;

import org.scijava.plugin.SciJavaPlugin;

import com.drew.lang.annotations.Nullable;

import ij.ImagePlus;
import io.github.mianalysis.mia.module.objects.detect.ManuallyIdentifyObjects;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.Parameters;

public abstract class ManuallyIdentifyObjectsExtension implements SciJavaPlugin {
    public abstract void initialiseBeforeImageShown(ManuallyIdentifyObjects module, Workspace workspace);

    public abstract void initialiseAfterImageShown(@Nullable ImagePlus displayIpl);
    
    public abstract Parameters getParameters();
    
    public abstract Parameters updateAndGetParameters();

}
