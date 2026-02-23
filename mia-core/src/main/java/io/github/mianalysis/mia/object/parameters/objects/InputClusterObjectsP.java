package io.github.mianalysis.mia.object.parameters.objects;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;

public class InputClusterObjectsP extends InputObjectsP {
    public InputClusterObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public InputClusterObjectsP(String name, ModuleI module, @NotNull String choice) {
        super(name, module, choice);
    }

    public InputClusterObjectsP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjectsMatchingClass(module, OutputClusterObjectsP.class, true);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
