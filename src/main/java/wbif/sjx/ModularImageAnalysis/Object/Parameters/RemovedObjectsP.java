package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ObjectNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import javax.swing.*;

public class RemovedObjectsP extends ObjectNamesType {
    public RemovedObjectsP(String name, Module module, @Nonnull String objectsName) {
        super(name,module);
        this.choice = objectsName;

    }

    public String getObjectsName() {
        return choice;
    }

    public void setObjectsName(String objectsName) {
        this.choice = objectsName;
    }
}
