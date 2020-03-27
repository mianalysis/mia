package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputSkeletonObjectsP extends OutputObjectsP {
    public OutputSkeletonObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputSkeletonObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputSkeletonObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
