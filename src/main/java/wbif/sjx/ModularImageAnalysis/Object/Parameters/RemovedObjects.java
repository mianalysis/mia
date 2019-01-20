package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ObjectNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.swing.*;

public class RemovedObjects extends ObjectNamesType {
    private String objectsName;

    public RemovedObjects(String name, Module module, String objectsName) {
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
}
