package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ImageNamesType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

import com.drew.lang.annotations.NotNull;

public class InputImageP extends ImageNamesType {
    public InputImageP(String name, Module module) {
        super(name, module);
    }

    public InputImageP(String name, Module module, @NotNull String imageName) {
        super(name, module);
        this.choice = imageName;
    }

    public InputImageP(String name, Module module, @NotNull String imageName, String description) {
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
    public <T extends Parameter> T duplicate(Module newModule) {
        InputImageP newParameter = new InputImageP(name,newModule,getImageName(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
