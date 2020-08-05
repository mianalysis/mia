package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.References.ParentChildRefCollection;

import com.drew.lang.annotations.NotNull;

public class ParentObjectsP extends ChoiceType {
    private String childObjectsName = "";

    public ParentObjectsP(String name, Module module) {
        super(name,module);
    }

    public ParentObjectsP(String name, Module module, String description) {
        super(name,module);
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

        ModuleCollection modules = module.getModules();
        ParentChildRefCollection relationships = modules.getParentChildRefs(module);
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
