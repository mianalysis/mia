package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;

public class StringP extends TextType {
    protected String value = "";

    public StringP(String name, Module module) {
        super(name,module);
    }

    public StringP(String name, Module module, @Nonnull String value) {
        super(name,module);
        this.value = value;
    }

    public StringP(String name, Module module, @Nonnull String value, String description) {
        super(name,module,description);
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getRawStringValue() {
        return String.valueOf(value);
    }

    @Override
    public void setValueFromString(String value) {
        this.value = value;
    }

    @Override
    public <T> T getValue() {
        return (T) MIA.getGlobalVariables().convertString(value);
    }

    @Override
    public <T> void setValue(T value) {
        this.value = (String) value;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        StringP newParameter = new StringP(name,module,value,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }

    @Override
    public boolean verify() {
        // The only thing to check is that any global variables and metadata values have been defined
        return MIA.getGlobalVariables().variablesPresent(value);

    }
}
