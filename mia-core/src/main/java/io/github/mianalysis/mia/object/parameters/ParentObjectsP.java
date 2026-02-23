package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.refs.collections.ParentChildRefs;

public class ParentObjectsP extends ChoiceType {
    private String childObjectsName = "";

    public ParentObjectsP(String name, ModuleI module) {
        super(name,module);
    }

    public ParentObjectsP(String name, ModuleI module, String description) {
        super(name,module,description);
    }

    public ParentObjectsP(String name, ModuleI module, @NotNull String choice, @NotNull String childObjectsName) {
        super(name,module);
        this.choice = choice;
        this.childObjectsName = childObjectsName;
    }

    public ParentObjectsP(String name, ModuleI module, @NotNull String choice, @NotNull String childObjectsName, String description) {
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

        ModulesI modules = module.getModules();
        ParentChildRefs relationships = modules.getParentChildRefs(module);
        return relationships.getParentNames(childObjectsName,true);

    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        ParentObjectsP newParameter = new ParentObjectsP(name,newModule,choice,childObjectsName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
