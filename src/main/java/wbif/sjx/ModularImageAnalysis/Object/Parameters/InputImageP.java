package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ImageNamesType;

import javax.annotation.Nonnull;
import javax.swing.*;

public class InputImageP extends ImageNamesType {
    public InputImageP(String name, Module module) {
        super(name, module);
    }

    public InputImageP(String name, Module module, @Nonnull String imageName) {
        super(name, module);
        this.choice = imageName;
    }

    public String getImageName() {
        return choice;
    }

    public void setImageName(String imageName) {
        this.choice = imageName;
    }

}
