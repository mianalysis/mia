package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;
import javax.swing.*;

public class MetadataItemP extends ChoiceType {
    private String metadataName;

    public MetadataItemP(String name, Module module, @Nonnull String metadataName) {
        super(name,module);
        this.metadataName = metadataName;

    }

    public String getMetadataName() {
        return metadataName;
    }

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    @Override
    public String[] getChoices() {
        return GUI.getModules().getMetadataReferences(module).getMetadataNames();
    }

    @Override
    public String getValueAsString() {
        return metadataName;
    }
}
