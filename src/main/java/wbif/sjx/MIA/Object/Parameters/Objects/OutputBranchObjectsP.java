package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputBranchObjectsP extends OutputObjectsP {
    public OutputBranchObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputBranchObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputBranchObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
