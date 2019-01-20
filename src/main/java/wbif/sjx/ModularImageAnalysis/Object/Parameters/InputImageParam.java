package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ImageNamesType;

import javax.swing.*;

public class InputImageParam extends ImageNamesType {
    private String imageName;

    public InputImageParam(String name, Module module, String imageName) {
        super(name, module);
        this.imageName = imageName;

    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String getValueAsString() {
        return imageName;
    }

}
