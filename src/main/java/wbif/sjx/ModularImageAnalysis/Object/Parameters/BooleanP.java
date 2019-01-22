package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.BooleanType;

public class BooleanP extends BooleanType {
    public BooleanP(String name, Module module, boolean enabled) {
        super(name, module, enabled);
    }
}
