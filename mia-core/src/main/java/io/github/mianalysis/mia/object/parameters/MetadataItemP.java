package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class MetadataItemP extends ChoiceType {
    public MetadataItemP(String name, ModuleI module) {
        super(name, module);
    }

    public MetadataItemP(String name, ModuleI module, @NotNull String choice) {
        super(name, module);
        this.choice = choice;
    }

    public MetadataItemP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, description);
        this.choice = choice;
    }

    @Override
    public String[] getChoices() {
        return module.getModules().getMetadataRefs(module).getMetadataNames();
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        MetadataItemP newParameter = new MetadataItemP(getName(), newModule, choice, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
