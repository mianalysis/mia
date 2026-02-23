package io.github.mianalysis.mia.object.parameters.objects;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;

public class OutputJunctionObjectsP extends OutputObjectsP {
    public OutputJunctionObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public OutputJunctionObjectsP(String name, ModuleI module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputJunctionObjectsP(String name, ModuleI module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
