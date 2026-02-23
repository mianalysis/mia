package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ImageNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class RemovedImageP extends ImageNamesType {
    public RemovedImageP(String name, ModuleI module) {
        super(name,module);
    }

    public RemovedImageP(String name, ModuleI module, @NotNull String choice) {
        super(name,module);
        this.choice = choice;
    }

    public RemovedImageP(String name, ModuleI module, @NotNull String choice, String description) {
        super(name,module,description);
        this.choice = choice;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        RemovedImageP newParameter = new RemovedImageP(name,newModule,choice,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
