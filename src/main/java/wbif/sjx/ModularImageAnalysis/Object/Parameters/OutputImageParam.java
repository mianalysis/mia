package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

public class OutputImageParam extends TextType {
    private String imageName;

    public OutputImageParam(String name, Module module, String imageName) {
        super(name,module);
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

    @Override
    public void setValueFromString(String text) {
        imageName = text;
    }
}
