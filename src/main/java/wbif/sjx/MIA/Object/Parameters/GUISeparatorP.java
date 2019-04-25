package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

public class GUISeparatorP extends Parameter {
    public GUISeparatorP(String name, Module module) {
        super(name, module);
    }

    @Override
    protected ParameterControl initialiseControl() {
        return null;
    }

    @Override
    public <T> T getValue() {
        return null;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getValueAsString() {
        return null;
    }

    @Override
    public boolean verify() {
        return false;
    }

    @Override
    public <T extends Parameter> T duplicate() {
        return null;
    }
}
