package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ImageNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class InputImageP extends ImageNamesType {
    public InputImageP(String name, ModuleI module) {
        super(name, module);
    }

    public InputImageP(String name, ModuleI module, @NotNull String imageName) {
        super(name, module);
        this.choice = imageName;
    }

    public InputImageP(String name, ModuleI module, @NotNull String imageName, String description) {
        super(name, module, description);
        this.choice = imageName;
    }

    public String getImageName() {
        return choice;
    }

    public void setImageName(String imageName) {
        this.choice = imageName;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        InputImageP newParameter = new InputImageP(getName(), newModule, getImageName(), getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
