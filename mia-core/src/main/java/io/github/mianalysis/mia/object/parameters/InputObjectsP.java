package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ObjectNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

import com.drew.lang.annotations.NotNull;

public class InputObjectsP extends ObjectNamesType {
    public InputObjectsP(String name, Module module) {
        super(name,module);
    }

    public InputObjectsP(String name, Module module, @NotNull String choice) {
        super(name, module);
        this.choice = choice;

    }

    public InputObjectsP(String name, Module module, @NotNull String choice, String description) {
        super(name, module, description);
        this.choice = choice;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        InputObjectsP newParameter = new InputObjectsP(name,newModule,getRawStringValue(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
