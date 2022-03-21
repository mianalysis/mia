package io.github.mianalysis.mia.object.parameters.text;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.gui.parametercontrols.ParameterControl;
import io.github.mianalysis.mia.gui.parametercontrols.TextAreaParameter;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public class TextAreaP extends TextType {
    private String value = "";
    private boolean editable = false;
    private ParameterControl control = null;
    private int controlHeight = 250;

    public TextAreaP(String name, Module module, boolean editable) {
        super(name, module);
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, boolean editable, String description) {
        super(name, module, description);
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, @NotNull String value, boolean editable) {
        super(name, module);
        this.value = value;
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, @NotNull String value, boolean editable, String description) {
        super(name, module, description);
        this.value = value;
        this.editable = editable;
    }

    public TextAreaP(String name, Module module, boolean editable, int controlHeight) {
        super(name, module);
        this.editable = editable;
        this.controlHeight = controlHeight;
    }

    public TextAreaP(String name, Module module, boolean editable, String description, int controlHeight) {
        super(name, module, description);
        this.editable = editable;
        this.controlHeight = controlHeight;
    }

    public TextAreaP(String name, Module module, @NotNull String value, boolean editable, int controlHeight) {
        super(name, module);
        this.value = value;
        this.editable = editable;
        this.controlHeight = controlHeight;
    }

    public TextAreaP(String name, Module module, @NotNull String value, boolean editable, String description, int controlHeight) {
        super(name, module, description);
        this.value = value;
        this.editable = editable;
        this.controlHeight = controlHeight;
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
        String converted1 = GlobalVariables.convertString(value, module.getModules());
        return (T) applyCalculation(converted1);

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
        if (control == null)
            control = new TextAreaParameter(this,controlHeight);
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

        return GlobalVariables.variablesPresent(value,module.getModules());

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        TextAreaP newParameter = new TextAreaP(name,newModule,value,editable,getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
