package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class ObjectMetadataP extends ChoiceType {
    protected String objectName = "";

    public ObjectMetadataP(String name, ModuleI module) {
        super(name, module);
    }

    public ObjectMetadataP(String name, ModuleI module, String description) {
        super(name, module, description);
    }

    public ObjectMetadataP(String name, ModuleI module, @NotNull String choice, @NotNull String objectName) {
        super(name, module);
        this.objectName = objectName;
        this.choice = choice;

    }

    public ObjectMetadataP(String name, ModuleI module, @NotNull String choice, @NotNull String objectName,
            String description) {
        super(name, module, description);
        this.objectName = objectName;
        this.choice = choice;

    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String[] getChoices() {
        return module.getModules().getObjectMetadataRefs(objectName, module).getMetadataNames();
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        ObjectMetadataP newParameter = new ObjectMetadataP(getName(), newModule, choice, objectName, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
