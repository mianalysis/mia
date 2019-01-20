package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.BooleanParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class BooleanType extends Parameter {
    private boolean selected = false;

    public BooleanType(String name, Module module, boolean selected) {
        super(name,module);
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String getValueAsString() {
        return Boolean.toString(isSelected());
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new BooleanParameter(this);
    }
}
