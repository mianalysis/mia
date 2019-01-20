package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.RelationshipCollection;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.LinkedHashSet;

public class ParentObjectsP extends ChoiceType {
    private String childObjectsName;

    public ParentObjectsP(String name, Module module, @Nonnull String choice, @Nonnull String childObjectsName) {
        super(name,module);
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

        ModuleCollection modules = GUI.getModules();
        RelationshipCollection relationships = modules.getRelationships(module);
        return relationships.getParentNames(childObjectsName,true);

    }
}
