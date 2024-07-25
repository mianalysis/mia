package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;

public class ChildObjectsP extends ChoiceType {
    private String parentObjectsName = "";

    public ChildObjectsP(String name, Module module) {
        super(name, module);
    }

    public ChildObjectsP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ChildObjectsP(String name, Module module, @NotNull String choice, @NotNull String parentObjectsName) {
        super(name, module);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    public ChildObjectsP(String name, Module module, @NotNull String choice, @NotNull String parentObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ChildObjectsP newParameter = new ChildObjectsP(name,newModule,getRawStringValue(),parentObjectsName,getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }

    public String getParentObjectsName() {
        return parentObjectsName;
    }

    public void setParentObjectsName(String parentObjectsName) {
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (parentObjectsName == null) return null;

        Modules modules = module.getModules();
        ParentChildRefs relationships = modules.getParentChildRefs(module);

        return relationships.getChildNames(parentObjectsName, true);

    }
}
