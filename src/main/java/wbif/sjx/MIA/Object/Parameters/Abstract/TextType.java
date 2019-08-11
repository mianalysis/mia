package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.TextParameter;
import wbif.sjx.MIA.Module.Miscellaneous.GlobalVariables;
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
        // The only thing to check is that any global variables and metadata values have been defined
        return GlobalVariables.variablesPresent(getRawStringValue());
    }
}
