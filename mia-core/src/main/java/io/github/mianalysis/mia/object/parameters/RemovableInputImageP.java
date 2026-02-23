package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class RemovableInputImageP extends InputImageP {
    private boolean removeInputImages = false;

    public RemovableInputImageP(String name, ModuleI module) {
        super(name, module);
    }

    public RemovableInputImageP(String name, ModuleI module, @NotNull String imageName) {
        super(name, module, imageName);
    }

    public RemovableInputImageP(String name, ModuleI module, @NotNull String imageName, String description) {
        super(name, module, imageName, description);
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        RemovableInputImageP newParameter = new RemovableInputImageP(name, module, getImageName(), getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());
        newParameter.setRemoveInputImages(isRemoveInputImages());

        return (T) newParameter;

    }

    public boolean isRemoveInputImages() {
        return removeInputImages;
    }

    public void setRemoveInputImages(boolean removeInputImages) {
        this.removeInputImages = removeInputImages;
    }
}
