package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ObjectNamesType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class InputObjectsP extends ObjectNamesType {
    public InputObjectsP(String name, Module module) {
        super(name,module);
    }

    public InputObjectsP(String name, Module module, @Nonnull String choice) {
        super(name, module);
        this.choice = choice;

    }

    public InputObjectsP(String name, Module module, @Nonnull String choice, String description) {
        super(name, module, description);
        this.choice = choice;

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        InputObjectsP newParameter = new InputObjectsP(name,newModule,getChoice(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
