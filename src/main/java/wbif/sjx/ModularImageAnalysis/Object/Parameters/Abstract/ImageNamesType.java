package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.OutputImageP;

import java.util.LinkedHashSet;

public abstract class ImageNamesType extends ChoiceType {
    public ImageNamesType(String name, Module module) {
        super(name, module);
    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputImageP> images = GUI.getModules().getAvailableImages(module);
        return images.stream().map(OutputImageP::getImageName).toArray(String[]::new);
    }
}
