package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.TextParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class TextType extends Parameter {
    public TextType(String name, Module module) {
        super(name, module);
    }

    public abstract void setValueFromString(String text);

    @Override
    protected ParameterControl initialiseControl() {
        return new TextParameter(this);
    }
}
