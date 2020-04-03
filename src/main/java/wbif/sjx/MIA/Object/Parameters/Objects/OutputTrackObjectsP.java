package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputTrackObjectsP extends OutputObjectsP {
    public OutputTrackObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputTrackObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputTrackObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
