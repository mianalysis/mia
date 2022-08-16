package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.text.StringP;

public class OutputImageP extends StringP {
    public OutputImageP(String name, Module module) {
        super(name,module);
    }

    public OutputImageP(String name, Module module, @NotNull String value) {
        super(name,module,value);
    }

    public OutputImageP(String name, Module module, @NotNull String value, String description) {
        super(name, module, value, description);
    }

    public String getImageName() {
        return getValue(null);
    }

    public void setImageName(String imageName) {
        setValue(imageName);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        OutputImageP newParameter = new OutputImageP(name,newModule,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
