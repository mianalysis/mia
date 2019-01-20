package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

public class OutputObjectsParam extends TextType {
    private String objectsName;

    public OutputObjectsParam(String name, Module module, String objectsName) {
        super(name,module);
        this.objectsName = objectsName;
    }

    public String getObjectsName() {
        return objectsName;
    }

    public void setObjectsName(String objectsName) {
        this.objectsName = objectsName;
    }

    @Override
    public String getValueAsString() {
        return objectsName;
    }

    @Override
    public void setValueFromString(String text) {
        objectsName = text;
    }
}
