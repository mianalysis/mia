package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.RefreshParametersButton;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class RefreshButtonP extends Parameter {
    protected String buttonLabel = "Refresh parameters";

    public RefreshButtonP(String name, Module module) {
        super(name, module);
    }

    public RefreshButtonP(String name, Module module, String description) {
        super(name, module, description);
    }

    public RefreshButtonP(String name, Module module, String buttonLabel, String description) {
        super(name, module, description);
        this.buttonLabel = buttonLabel;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new RefreshParametersButton(this);
    }

    @Override
    public <T> T getValue() {
        return (T) buttonLabel;
    }

    @Override
    public <T> void setValue(T value) {
        this.buttonLabel = (String) value;
    }

    @Override
    public String getRawStringValue() {
        return buttonLabel;
    }

    @Override
    public void setValueFromString(String string) {
        this.buttonLabel = string;
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new RefreshButtonP(name,module,buttonLabel,getDescription());
    }
}
