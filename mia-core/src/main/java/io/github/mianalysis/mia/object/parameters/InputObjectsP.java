package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ObjectNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class InputObjectsP extends ObjectNamesType {
    public InputObjectsP(String name, ModuleI module) {
        super(name,module);
    }

    public InputObjectsP(String name, ModuleI module, @NotNull String choice) {
        super(name, module);
        this.choice = choice;

    }

    public InputObjectsP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name, module, description);
        this.choice = choice;

    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        InputObjectsP newParameter = new InputObjectsP(getName(),newModule,getRawStringValue(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
