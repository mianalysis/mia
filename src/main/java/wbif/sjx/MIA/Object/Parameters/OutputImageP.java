package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputImageP extends TextType {
    private String imageName = "";

    public OutputImageP(String name, Module module) {
        super(name,module);
    }

    public OutputImageP(String name, Module module, @Nonnull String imageName, String description) {
        super(name,module,description);
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public <T> T getValue() {
        return (T) imageName;

    }

    @Override
    public <T> void setValue(T value) {
        imageName = (String) value;
    }

    @Override
    public String getValueAsString() {
        return imageName;
    }

    @Override
    public void setValueFromString(String value) {
        imageName = value;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new OutputImageP(name,module,imageName,getDescription());
    }
}
