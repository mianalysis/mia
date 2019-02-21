package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.TextDisplayArea;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;

public class TextDisplayP extends TextType {
    private String value = "";

    public TextDisplayP(String name, Module module) {
        super(name, module);
    }

    public TextDisplayP(String name, Module module, @Nonnull String value) {
        super(name, module);
        this.value = value;
    }

    public TextDisplayP(String name, Module module, @Nonnull String value, String description) {
        super(name, module, description);
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
    public String getValueAsString() {
        return value;
    }

    @Override
    public ParameterControl getControl() {
        return new TextDisplayArea(this);
    }

    @Override
    public boolean verify() {
        return !value.equals("");
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new TextDisplayP(name,module,value,getDescription());
    }
}
