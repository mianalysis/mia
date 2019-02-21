package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.BooleanType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

public class BooleanP extends BooleanType {
    public BooleanP(String name, Module module, boolean enabled) {
        super(name, module, enabled);
    }

    public BooleanP(String name, Module module, boolean enabled, String description) {
        super(name, module, enabled, description);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new BooleanP(name,module,getValue(),getDescription());
    }
}
