package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.TextAreaParameter;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;
import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;

import javax.annotation.Nonnull;
import javax.swing.*;

public class TextAreaP extends TextType {
    private String value = "";
    private boolean editable = false;
    private ParameterControl control = null;

    public TextAreaP(String name, Module module, boolean editable) {
        super(name, module);
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, boolean editable, String description) {
        super(name, module, description);
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
        return (T) MIA.getGlobalVariables().convertString(value);
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
        if (control == null) control = new TextAreaParameter(this);
        return control;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public boolean verify() {
        if (value.equals("")) return false;

        return MIA.getGlobalVariables().variablesPresent(value);

    }

    @Override
    public <T extends Parameter> T duplicate() {
        TextAreaP newParameter = new TextAreaP(name,module,value,editable,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
