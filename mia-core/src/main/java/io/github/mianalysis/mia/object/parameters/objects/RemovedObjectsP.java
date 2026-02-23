package io.github.mianalysis.mia.object.parameters.objects;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ObjectNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class RemovedObjectsP extends ObjectNamesType {
    public RemovedObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public RemovedObjectsP(String name, ModuleI module, @NotNull String choice) {
        super(name, module);
        this.choice = choice;
    }

    public RemovedObjectsP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, description);
        this.choice = choice;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        RemovedObjectsP newParameter = new RemovedObjectsP(getName(), newModule, choice, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
