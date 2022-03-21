package io.github.mianalysis.mia.object.parameters.objects;

import javax.swing.JComponent;
import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public class OutputObjectsP extends TextType {
    private String objectsName = "";

    public OutputObjectsP(String name, Module module) {
        super(name, module);
    }

    public OutputObjectsP(String name, Module module, @NotNull String objectsName) {
        super(name, module);
        this.objectsName = objectsName;
    }

    public OutputObjectsP(String name, Module module, @NotNull String objectsName, String description) {
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
    public <T> T getValue() {
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
    public <T extends Parameter> T duplicate(Module newModule) {
        OutputObjectsP newParameter = new OutputObjectsP(name, newModule, objectsName, getDescription());

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
            LinkedHashSet<OutputObjectsP> availableObjects = module.getModules().getAvailableObjects(module);
            for (OutputObjectsP availableObject : availableObjects)
                if (availableObject.getObjectsName().equals(objectsName))
                    return false;
        }
        
        return true;

    }
}
