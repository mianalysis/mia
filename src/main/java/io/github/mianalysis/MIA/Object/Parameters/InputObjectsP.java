package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.ObjectNamesType;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

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
        InputObjectsP newParameter = new InputObjectsP(name,newModule,getChoice(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
