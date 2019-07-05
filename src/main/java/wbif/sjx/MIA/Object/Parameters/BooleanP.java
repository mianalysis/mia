package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.BooleanType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class BooleanP extends BooleanType {
    public BooleanP(String name, Module module, boolean enabled) {
        super(name, module, enabled);
    }

    public BooleanP(String name, Module module, boolean enabled, String description) {
        super(name, module, enabled, description);
    }

    @Override
    public <T extends Parameter> T duplicate() {
        BooleanP newParameter = new BooleanP(name,module,getValue(),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }
}
