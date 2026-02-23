package io.github.mianalysis.mia.object.parameters.objects;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public class OutputObjectsP extends TextType {
    private String objectsName = "";

    public OutputObjectsP(String name, ModuleI module) {
        super(name, module);
    }

    public OutputObjectsP(String name, ModuleI module, @NotNull String objectsName) {
        super(name, module);
        this.objectsName = objectsName;
    }

    public OutputObjectsP(String name, ModuleI module, @NotNull String objectsName, String description) {
        super(name, module, description);
        this.objectsName = objectsName;
    }

    public String getObjectsName() {
        return objectsName;
    }

    public void setObjectsName(String objectsName) {
        this.objectsName = objectsName;
    }

    @Override
    public <T> T getValue(WorkspaceI workspace) {
        return (T) objectsName;
    }

    @Override
    public <T> void setValue(T value) {
        objectsName = (String) value;
    }

    @Override
    public String getRawStringValue() {
        return objectsName;
    }

    @Override
    public void setValueFromString(String value) {
        objectsName = value;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        OutputObjectsP newParameter = new OutputObjectsP(getName(), newModule, objectsName, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public boolean verify() {
        if (!super.verify())
            return false;

        if (module.isEnabled()) {
            LinkedHashSet<OutputObjectsP> availableObjects = module.getModules().getAvailableObjects(module, true);
            for (OutputObjectsP availableObject : availableObjects)
                if (availableObject.getObjectsName().equals(objectsName))
                    return false;
        }
        
        return true;

    }
}
