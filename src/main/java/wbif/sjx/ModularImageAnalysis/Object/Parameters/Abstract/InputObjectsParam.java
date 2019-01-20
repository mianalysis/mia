package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.Module.Module;

public class InputObjectsParam extends ObjectNamesType {
    private String objectName;

    public InputObjectsParam(String name, Module module, String objectName) {
        super(name, module);
        this.objectName = objectName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String getValueAsString() {
        return objectName;
    }
}
