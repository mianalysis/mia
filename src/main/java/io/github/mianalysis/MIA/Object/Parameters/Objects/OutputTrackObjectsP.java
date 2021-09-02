package io.github.mianalysis.mia.Object.Parameters.Objects;

import io.github.mianalysis.mia.module.Module;

import com.drew.lang.annotations.NotNull;

public class OutputTrackObjectsP extends OutputObjectsP {
    public OutputTrackObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputTrackObjectsP(String name, Module module, @NotNull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputTrackObjectsP(String name, Module module, @NotNull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
