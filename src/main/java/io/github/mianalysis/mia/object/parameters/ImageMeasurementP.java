package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

import com.drew.lang.annotations.NotNull;

public class ImageMeasurementP extends ChoiceType {
    private String imageName = "";

    public ImageMeasurementP(String name, Module module) {
        super(name, module);
    }

    public ImageMeasurementP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ImageMeasurementP(String name, Module module, @NotNull String choice, @NotNull String imageName) {
        super(name, module);
        this.imageName = imageName;
        this.choice = choice;
    }

    public ImageMeasurementP(String name, Module module, @NotNull String choice, @NotNull String imageName, String description) {
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
    public <T extends Parameter> T duplicate(Module newModule) {
        ImageMeasurementP newParameter = new ImageMeasurementP(name,newModule,getChoice(),imageName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
