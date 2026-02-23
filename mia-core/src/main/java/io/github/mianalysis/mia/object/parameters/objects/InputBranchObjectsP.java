package io.github.mianalysis.mia.object.parameters.objects;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;

public class InputBranchObjectsP extends InputObjectsP {
    public InputBranchObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public InputBranchObjectsP(String name, ModuleI module, @NotNull String choice) {
        super(name, module, choice);
    }

    public InputBranchObjectsP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjectsMatchingClass(module, OutputBranchObjectsP.class, true);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
