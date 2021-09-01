package io.github.mianalysis.MIA.Object.Parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Module.ModuleCollection;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.ChoiceType;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.MIA.Object.References.Collections.ParentChildRefCollection;

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
        ChildObjectsP newParameter = new ChildObjectsP(name,newModule,getChoice(),parentObjectsName,getDescription());
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

        ModuleCollection modules = module.getModules();
        ParentChildRefCollection relationships = modules.getParentChildRefs(module);

        return relationships.getChildNames(parentObjectsName, true);

    }
}
