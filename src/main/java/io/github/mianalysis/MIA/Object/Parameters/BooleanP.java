package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.BooleanType;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

public class BooleanP extends BooleanType {
    public BooleanP(String name, Module module, boolean enabled) {
        super(name, module, enabled);
    }

    public BooleanP(String name, Module module, boolean enabled, String description) {
        super(name, module, enabled, description);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        BooleanP newParameter = new BooleanP(name,newModule,getValue(),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }
}
