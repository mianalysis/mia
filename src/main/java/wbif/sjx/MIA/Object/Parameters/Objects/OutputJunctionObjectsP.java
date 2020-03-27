package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputJunctionObjectsP extends OutputObjectsP {
    public OutputJunctionObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputJunctionObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputJunctionObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
