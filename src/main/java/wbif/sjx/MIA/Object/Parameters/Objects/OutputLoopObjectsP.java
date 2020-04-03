package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputLoopObjectsP extends OutputObjectsP {
    public OutputLoopObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputLoopObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputLoopObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
