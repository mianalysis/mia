package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class BooleanP extends BooleanType {
    public BooleanP(String name, ModuleI module, boolean enabled) {
        super(name, module, enabled);
    }

    public BooleanP(String name, ModuleI module, boolean enabled, String description) {
        super(name, module, enabled, description);
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        BooleanP newParameter = new BooleanP(getName(),newModule,getValue(null),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
        
    }
}
