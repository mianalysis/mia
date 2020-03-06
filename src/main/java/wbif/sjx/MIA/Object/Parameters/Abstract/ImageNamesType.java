package wbif.sjx.MIA.Object.Parameters.Abstract;

import java.util.LinkedHashSet;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.OutputImageP;

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
