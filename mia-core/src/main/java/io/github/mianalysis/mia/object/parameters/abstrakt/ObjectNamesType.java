package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.util.LinkedHashSet;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;

public abstract class ObjectNamesType extends ChoiceType {
    public ObjectNamesType(String name, ModuleI module) {
        super(name,module);
    }

    public ObjectNamesType(String name, ModuleI module, String description) {
        super(name,module,description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjects(module, true);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
