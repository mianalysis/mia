package wbif.sjx.MIA.Object.Parameters.Objects;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;

public class OutputObjectsP extends TextType {
    private String objectsName = "";

    public OutputObjectsP(String name, Module module) {
        super(name,module);
    }

    public OutputObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name,module);
        this.objectsName = objectsName;
    }

    public OutputObjectsP(String name, Module module, @Nonnull String objectsName, String description) {
        super(name,module,description);
        this.objectsName = objectsName;
    }

    public String getObjectsName() {
        return objectsName;
    }

    public void setObjectsName(String objectsName) {
        this.objectsName = objectsName;
    }

    @Override
    public <T> T getValue() {
        return (T) objectsName;
    }

    @Override
    public <T> void setValue(T value) {
        objectsName = (String) value;
    }

    @Override
    public String getRawStringValue() {
        return objectsName;
    }

    @Override
    public void setValueFromString(String value) {
        objectsName = value;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        OutputObjectsP newParameter = new OutputObjectsP(name,newModule,objectsName,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
