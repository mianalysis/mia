package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ObjectNamesType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class InputObjectsP extends ObjectNamesType {
    public InputObjectsP(String name, Module module) {
        super(name,module);
    }

    public InputObjectsP(String name, Module module, @Nonnull String choice) {
        super(name, module);
        this.choice = choice;

    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new InputObjectsP(name,module,getChoice());
    }
}
