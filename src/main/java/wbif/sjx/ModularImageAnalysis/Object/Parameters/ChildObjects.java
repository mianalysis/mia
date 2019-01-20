package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;

public class ChildObjects extends ChoiceType {
    private String childObjectsName;
    private String parentObjectsName;
    private WiderDropDownCombo control;

    public ChildObjects(String name, Module module, String childObjectsName, String parentObjectsName) {
        super(name,module);
        this.childObjectsName = childObjectsName;
        this.parentObjectsName = parentObjectsName;

    }

    public String getChildObjectsName() {
        return childObjectsName;
    }

    public void setChildObjectsName(String childObjectsName) {
        this.childObjectsName = childObjectsName;
    }

    public String getParentObjectsName() {
        return parentObjectsName;
    }

    public void setParentObjectsName(String parentObjectsName) {
        this.parentObjectsName = parentObjectsName;
    }

    @Override
    public String getValueAsString() {
        return childObjectsName;
    }

    @Override
    public String[] getChoices() {
        if (parentObjectsName == null) return null;

        ModuleCollection modules = GUI.getModules();
        RelationshipCollection relationships = modules.getRelationships(module);
        return relationships.getChildNames(parentObjectsName,true);

    }
}
