package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class MetadataItemP extends ChoiceType {
    public MetadataItemP(String name, Module module) {
        super(name,module);
    }

    public MetadataItemP(String name, Module module, @Nonnull String choice) {
        super(name,module);
        this.choice = choice;
    }

    public MetadataItemP(String name, Module module, @Nonnull String choice, String description) {
        super(name,module,description);
        this.choice = choice;
    }

    @Override
    public String[] getChoices() {
        return module.getModules().getMetadataRefs(module).getMetadataNames();
    }

    @Override
    public <T extends Parameter> T duplicate() {
        MetadataItemP newParameter = new MetadataItemP(name,module,choice,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
