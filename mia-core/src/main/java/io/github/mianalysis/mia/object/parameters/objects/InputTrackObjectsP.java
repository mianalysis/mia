package io.github.mianalysis.mia.object.parameters.objects;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.InputObjectsP;

public class InputTrackObjectsP extends InputObjectsP {
    public InputTrackObjectsP(String name, Module module) {
        super(name, module);
    }

    public InputTrackObjectsP(String name, Module module, @NotNull String choice) {
        super(name, module, choice);
    }

    public InputTrackObjectsP(String name, Module module, @NotNull String choice, String description) {
        super(name, module, choice, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputObjectsP> objects = module.getModules().getAvailableObjectsMatchingClass(module,
                OutputTrackObjectsP.class, true);
        return objects.stream().map(OutputObjectsP::getObjectsName).distinct().toArray(String[]::new);
    }
}
