package io.github.mianalysis.mia.object.parameters.objects;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;

public class OutputClusterObjectsP extends OutputObjectsP {
    public OutputClusterObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public OutputClusterObjectsP(String name, ModuleI module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputClusterObjectsP(String name, ModuleI module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
