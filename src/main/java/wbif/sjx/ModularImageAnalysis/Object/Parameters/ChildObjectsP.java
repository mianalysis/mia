package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import com.drew.lang.annotations.NotNull;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;

public class ChildObjectsP extends ChoiceType {
    private String parentObjectsName;
    private WiderDropDownCombo control;

    public ChildObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String parentObjectsName) {
        super(name, module);
        this.choice = choice;
        this.parentObjectsName = parentObjectsName;

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
