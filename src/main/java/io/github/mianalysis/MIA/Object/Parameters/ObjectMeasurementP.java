package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.ChoiceType;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

import com.drew.lang.annotations.NotNull;

public class ObjectMeasurementP extends ChoiceType {
    private String objectName = "";

    public ObjectMeasurementP(String name, Module module) {
        super(name, module);
    }

    public ObjectMeasurementP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ObjectMeasurementP(String name, Module module, @NotNull String choice, @NotNull String objectName) {
        super(name, module);
        this.objectName = objectName;
        this.choice = choice;

    }

    public ObjectMeasurementP(String name, Module module, @NotNull String choice, @NotNull String objectName, String description) {
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
    public <T extends Parameter> T duplicate(Module newModule) {
        ObjectMeasurementP newParameter =  new ObjectMeasurementP(name,newModule,choice,objectName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
