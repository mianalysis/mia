package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class ImageMeasurementP extends ChoiceType {
    private String imageName = "";

    public ImageMeasurementP(String name, ModuleI module) {
        super(name, module);
    }

    public ImageMeasurementP(String name, ModuleI module, String description) {
        super(name, module, description);
    }

    public ImageMeasurementP(String name, ModuleI module, @NotNull String choice, @NotNull String imageName) {
        super(name, module);
        this.imageName = imageName;
        this.choice = choice;
    }

    public ImageMeasurementP(String name, ModuleI module, @NotNull String choice, @NotNull String imageName, String description) {
        super(name, module, description);
        this.imageName = imageName;
        this.choice = choice;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String[] getChoices() {
        return module.getModules().getImageMeasurementRefs(imageName,module).getMeasurementNames();
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        ImageMeasurementP newParameter = new ImageMeasurementP(getName(),newModule,getRawStringValue(),imageName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
