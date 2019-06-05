package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.TextAreaParameter;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;

public class TextAreaP extends TextType {
    private String value = "";
    private boolean editable = false;

    public TextAreaP(String name, Module module, boolean editable) {
        super(name, module);
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, @Nonnull String value, boolean editable) {
        super(name, module);
        this.value = value;
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, @Nonnull String value, boolean editable, String description) {
        super(name, module, description);
        this.value = value;
        this.editable = editable;
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
    public String getRawStringValue() {
        return value;
    }

    @Override
    public ParameterControl getControl() {
        return new TextAreaParameter(this);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean verify() {
        return !value.equals("");
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new TextAreaP(name,module,value,editable,getDescription());
    }
}
