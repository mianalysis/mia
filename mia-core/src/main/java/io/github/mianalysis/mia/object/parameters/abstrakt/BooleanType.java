package io.github.mianalysis.mia.object.parameters.abstrakt;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public abstract class BooleanType extends TextSwitchableParameter {
    protected boolean selected = false;

    public BooleanType(String name, Module module, boolean selected) {
        super(name,module);
        this.selected = selected;
    }

    public BooleanType(String name, Module module, boolean selected, String description) {
        super(name,module,description);
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void flipBoolean() {
        selected = !selected;
    }

    @Override
    public String getRawStringValue() {
        return Boolean.toString(isSelected());
    }

    @Override
    public void setValueFromString(String value) {
        selected = Boolean.parseBoolean(value);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getBooleanControl(this);
    }

    @Override
    public <T> T getValue(Workspace workspace) {
        return (T) (Boolean) selected;
    }

    @Override
    public <T> void setValue(T value) {
        selected = (Boolean) value;
    }

    @Override
    public boolean verify() {
        // It doesn't matter what state a boolean is in, so this always returns true
        return true;
    }
}
