package io.github.mianalysis.mia.object.parameters.objects;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;

public class OutputTrackObjectsP extends OutputObjectsP {
    public OutputTrackObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public OutputTrackObjectsP(String name, ModuleI module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputTrackObjectsP(String name, ModuleI module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
