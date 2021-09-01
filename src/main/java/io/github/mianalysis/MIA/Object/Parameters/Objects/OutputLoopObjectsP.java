package io.github.mianalysis.MIA.Object.Parameters.Objects;

import io.github.mianalysis.MIA.Module.Module;

import com.drew.lang.annotations.NotNull;

public class OutputLoopObjectsP extends OutputObjectsP {
    public OutputLoopObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputLoopObjectsP(String name, Module module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputLoopObjectsP(String name, Module module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
