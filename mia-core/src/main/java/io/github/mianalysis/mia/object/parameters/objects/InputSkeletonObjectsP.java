package io.github.mianalysis.mia.object.parameters.objects;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;

public class InputSkeletonObjectsP extends InputObjectsP {
    public InputSkeletonObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public InputSkeletonObjectsP(String name, ModuleI module, @NotNull String choice) {
        super(name, module, choice);
    }

    public InputSkeletonObjectsP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjectsMatchingClass(module, OutputSkeletonObjectsP.class, true);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
