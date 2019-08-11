package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ImageNamesType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class RemovedImageP extends ImageNamesType {
    public RemovedImageP(String name, Module module) {
        super(name,module);
    }

    public RemovedImageP(String name, Module module, @Nonnull String choice) {
        super(name,module);
        this.choice = choice;
    }

    public RemovedImageP(String name, Module module, @Nonnull String choice, String description) {
        super(name,module,description);
        this.choice = choice;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        RemovedImageP newParameter = new RemovedImageP(name,newModule,choice,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
