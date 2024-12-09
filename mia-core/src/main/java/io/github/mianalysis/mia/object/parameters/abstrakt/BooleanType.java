package io.github.mianalysis.mia.object.parameters.abstrakt;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public abstract class BooleanType extends TextSwitchableParameter {
    protected String value = "false";

    public BooleanType(String name, Module module, boolean selected) {
        super(name,module);
        this.setSelected(selected);
    }

    public BooleanType(String name, Module module, boolean selected, String description) {
        super(name,module,description);
        this.setSelected(selected);
    }

    public boolean isSelected() {
        String converted = GlobalVariables.convertString(value, module.getModules());
        return Boolean.parseBoolean(converted);
    }

    public void setSelected(boolean selected) {
        this.value = Boolean.toString(selected);
    }

    public void flipBoolean() {
        setSelected(!isSelected());
    }

    @Override
    public String getRawStringValue() {
        return value;
    }

    @Override
    public void setValueFromString(String value) {
        this.value = value;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getBooleanControl(this);
    }

    @Override
    public <T> T getValue(WorkspaceI workspace) {
        String converted = GlobalVariables.convertString(value, module.getModules());
        return (T) (Boolean) Boolean.parseBoolean(converted);
    }

    @Override
    public <T> void setValue(T value) {
        this.value = ((Boolean) value).toString();
    }

    @Override
    public boolean verify() {
        // It doesn't matter what state a boolean is in, so this always returns true
        return true;
    }
}
