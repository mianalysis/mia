package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.InputObjectsP;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;

public class InputLoopObjectsP extends InputObjectsP {
    public InputLoopObjectsP(String name, Module module) {
        super(name, module);
    }

    public InputLoopObjectsP(String name, Module module, @Nonnull String choice) {
        super(name, module, choice);
    }

    public InputLoopObjectsP(String name, Module module, @Nonnull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module,OutputLoopObjectsP.class);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
