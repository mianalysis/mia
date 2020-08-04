package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;

import com.drew.lang.annotations.NotNull;
import java.util.LinkedHashSet;

public class InputJunctionObjectsP extends InputObjectsP {
    public InputJunctionObjectsP(String name, Module module) {
        super(name, module);
    }

    public InputJunctionObjectsP(String name, Module module, @NotNull String choice) {
        super(name, module, choice);
    }

    public InputJunctionObjectsP(String name, Module module, @NotNull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module,OutputJunctionObjectsP.class);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
