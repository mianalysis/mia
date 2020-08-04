package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

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
