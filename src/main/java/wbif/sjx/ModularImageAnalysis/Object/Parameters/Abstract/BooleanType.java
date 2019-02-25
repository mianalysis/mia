package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.BooleanParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class BooleanType extends Parameter {
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
    public String getValueAsString() {
        return Boolean.toString(isSelected());
    }

    public void setValueFromString(String value) {
        selected = Boolean.parseBoolean(value);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new BooleanParameter(this);
    }

    @Override
    public <T> T getValue() {
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
