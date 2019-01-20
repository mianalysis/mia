package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.swing.*;

public class ParentObjectsParam extends ChoiceType {
    private String objectsName;
    private String childObjectsName;

    public ParentObjectsParam(String name, Module module, String objectsName, String childObjectsName) {
        super(name,module);
        this.objectsName = objectsName;
        this.childObjectsName = childObjectsName;
    }

    public String getObjectsName() {
        return objectsName;
    }

    public void setObjectsName(String objectsName) {
        this.objectsName = objectsName;
    }

    public String getChildObjectsName() {
        return childObjectsName;
    }

    public void setChildObjectsName(String childObjectsName) {
        this.childObjectsName = childObjectsName;
    }

    @Override
    public String getValueAsString() {
        return objectsName;
    }

    @Override
    public String[] getChoices() {
        return new String[0];
    }
}
