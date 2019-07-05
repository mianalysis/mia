package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class ObjectMeasurementP extends ChoiceType {
    private String objectName = "";

    public ObjectMeasurementP(String name, Module module) {
        super(name, module);
    }

    public ObjectMeasurementP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ObjectMeasurementP(String name, Module module, @Nonnull String choice, @Nonnull String objectName) {
        super(name, module);
        this.objectName = objectName;
        this.choice = choice;

    }

    public ObjectMeasurementP(String name, Module module, @Nonnull String choice, @Nonnull String objectName, String description) {
        super(name, module, description);
        this.objectName = objectName;
        this.choice = choice;

    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String[] getChoices() {
        return module.getModules().getObjectMeasurementRefs(objectName,module).getMeasurementNames();
    }

    @Override
    public <T extends Parameter> T duplicate() {
        ObjectMeasurementP newParameter =  new ObjectMeasurementP(name,module,choice,objectName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
