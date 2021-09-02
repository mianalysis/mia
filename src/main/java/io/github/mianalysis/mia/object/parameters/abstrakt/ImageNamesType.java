package io.github.mianalysis.mia.object.parameters.abstrakt;

import java.util.LinkedHashSet;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.OutputImageP;

public abstract class ImageNamesType extends ChoiceType {
    public ImageNamesType(String name, Module module) {
        super(name, module);
    }

    public ImageNamesType(String name, Module module, String description) {
        super(name, module, description);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputImageP> images = module.getModules().getAvailableImages(module);

        return images.stream().map(OutputImageP::getImageName).distinct().toArray(String[]::new);

    }
}
