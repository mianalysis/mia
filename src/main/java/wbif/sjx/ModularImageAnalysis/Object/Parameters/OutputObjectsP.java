package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

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
    public String getValueAsString() {
        return objectsName;
    }

    @Override
    public void setValueFromString(String value) {
        objectsName = value;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new OutputObjectsP(name,module,objectsName,getDescription());
    }
}
