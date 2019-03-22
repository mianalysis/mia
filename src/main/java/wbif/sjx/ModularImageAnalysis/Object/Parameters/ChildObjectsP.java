package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;

import javax.annotation.Nonnull;

public class ChildObjectsP extends ChoiceType {
    private String parentObjectsName = "";
    private WiderDropDownCombo control;

    public ChildObjectsP(String name, Module module) {
        super(name, module);
    }

    public ChildObjectsP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ChildObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String parentObjectsName) {
        super(name, module);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    public ChildObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String parentObjectsName, String description) {
        super(name, module, description);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new ChildObjectsP(name,module,getChoice(),parentObjectsName,getDescription());
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

        ModuleCollection modules = GUI.getModules();
        RelationshipCollection relationships = modules.getRelationships(module);
        return relationships.getChildNames(parentObjectsName, true);

    }
}
