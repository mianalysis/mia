package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import javax.swing.*;

public class MetadataItemP extends ChoiceType {
    public MetadataItemP(String name, Module module) {
        super(name,module);
    }

    public MetadataItemP(String name, Module module, @Nonnull String choice) {
        super(name,module);
        this.choice = choice;
    }

    @Override
    public String[] getChoices() {
        return GUI.getModules().getMetadataReferences(module).getMetadataNames();
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new MetadataItemP(name,module,choice);
    }
}
