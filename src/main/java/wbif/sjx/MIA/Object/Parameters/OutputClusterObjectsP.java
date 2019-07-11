package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputClusterObjectsP extends OutputObjectsP {
    public OutputClusterObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputClusterObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputClusterObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
