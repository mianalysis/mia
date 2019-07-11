package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;

import javax.annotation.Nonnull;

public class OutputTrackObjectP extends OutputObjectsP {
    public OutputTrackObjectP(String name, Module module) {
        super(name, module);
    }

    public OutputTrackObjectP(String name, Module module, @Nonnull String objectsName) {
        super(name, module, objectsName);
    }

    public OutputTrackObjectP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name, module, objectsName, description);
    }
}
