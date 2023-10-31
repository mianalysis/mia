package io.github.mianalysis.mia.object.parameters.objects;

import io.github.mianalysis.mia.module.Module;

import com.drew.lang.annotations.NotNull;

public class OutputJunctionObjectsP extends OutputObjectsP {
    public OutputJunctionObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputJunctionObjectsP(String name, Module module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputJunctionObjectsP(String name, Module module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
