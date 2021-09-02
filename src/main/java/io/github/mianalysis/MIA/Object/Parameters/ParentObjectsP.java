package io.github.mianalysis.mia.Object.Parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.Object.Parameters.Abstract.ChoiceType;
import io.github.mianalysis.mia.Object.Parameters.Abstract.Parameter;
import io.github.mianalysis.mia.Object.Refs.Collections.ParentChildRefs;

import com.drew.lang.annotations.NotNull;

public class ParentObjectsP extends ChoiceType {
    private String childObjectsName = "";

    public ParentObjectsP(String name, Module module) {
        super(name,module);
    }

    public ParentObjectsP(String name, Module module, String description) {
        super(name,module,description);
    }

    public ParentObjectsP(String name, Module module, @NotNull String choice, @NotNull String childObjectsName) {
        super(name,module);
        this.choice = choice;
        this.childObjectsName = childObjectsName;
    }

    public ParentObjectsP(String name, Module module, @NotNull String choice, @NotNull String childObjectsName, String description) {
        super(name,module,description);
        this.choice = choice;
        this.childObjectsName = childObjectsName;
    }

    public String getChildObjectsName() {
        return childObjectsName;
    }

    public void setChildObjectsName(String childObjectsName) {
        this.childObjectsName = childObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (childObjectsName == null) return null;

        Modules modules = module.getModules();
        ParentChildRefs relationships = modules.getParentChildRefs(module);
        return relationships.getParentNames(childObjectsName,true);

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ParentObjectsP newParameter = new ParentObjectsP(name,newModule,choice,childObjectsName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
