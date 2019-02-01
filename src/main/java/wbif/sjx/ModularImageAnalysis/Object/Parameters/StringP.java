package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;

public class StringP extends TextType {
    private String value = "";

    public StringP(String name, Module module) {
        super(name,module);
    }

    public StringP(String name, Module module, @Nonnull String value) {
        super(name,module);
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }

    @Override
    public void setValueFromString(String value) {
        this.value = value;
    }

    @Override
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value) {
        this.value = (String) value;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new StringP(name,module,value);
    }
}
