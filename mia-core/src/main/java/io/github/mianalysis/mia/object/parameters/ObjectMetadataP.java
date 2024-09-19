package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

import com.drew.lang.annotations.NotNull;

public class ObjectMetadataP extends ChoiceType {
    protected String objectName = "";

    public ObjectMetadataP(String name, Module module) {
        super(name, module);
    }

    public ObjectMetadataP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ObjectMetadataP(String name, Module module, @NotNull String choice, @NotNull String objectName) {
        super(name, module);
        this.objectName = objectName;
        this.choice = choice;

    }

    public ObjectMetadataP(String name, Module module, @NotNull String choice, @NotNull String objectName, String description) {
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
        return module.getModules().getObjectMetadataRefs(objectName,module).getMetadataNames();
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ObjectMetadataP newParameter =  new ObjectMetadataP(name,newModule,choice,objectName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
