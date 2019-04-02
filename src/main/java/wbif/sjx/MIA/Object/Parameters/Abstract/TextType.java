package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.GUI.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.TextParameter;
import wbif.sjx.MIA.Module.Module;

public abstract class TextType extends Parameter {
    public TextType(String name, Module module) {
        super(name, module);
    }

    public TextType(String name, Module module, String description) {
        super(name, module, description);
    }

    public abstract void setValueFromString(String value);

    @Override
    protected ParameterControl initialiseControl() {
        return new TextParameter(this);
    }

    @Override
    public boolean verify() {
        // It doesn't matter what this output is, so the test always passes.
        return true;
    }
}
