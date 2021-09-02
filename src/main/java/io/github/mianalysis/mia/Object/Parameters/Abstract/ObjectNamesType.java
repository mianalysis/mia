package io.github.mianalysis.mia.Object.Parameters.Abstract;

import java.util.LinkedHashSet;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.Object.Parameters.Objects.OutputObjectsP;

public abstract class ObjectNamesType extends ChoiceType {
    public ObjectNamesType(String name, Module module) {
        super(name,module);
    }

    public ObjectNamesType(String name, Module module, String description) {
        super(name,module,description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
