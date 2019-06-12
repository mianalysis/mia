package wbif.sjx.MIA.Object.Parameters.Abstract;

import wbif.sjx.MIA.GUI.ParameterControls.ParameterControl;
import wbif.sjx.MIA.GUI.ParameterControls.ChoiceArrayParameter;
import wbif.sjx.MIA.Module.Module;

public abstract class ChoiceType extends Parameter {
    protected String choice = "";

    public ChoiceType(String name, Module module) {
        super(name, module);
    }

    public ChoiceType(String name, Module module, String description) {
        super(name, module, description);
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public abstract String[] getChoices();

    @Override
    public String getRawStringValue() {
        return choice;
    }

    @Override
    public void setValueFromString(String string) {
        this.choice = string;
    }

    @Override
    protected ParameterControl initialiseControl() {
        return new ChoiceArrayParameter(this);
    }

    @Override
    public <T> T getValue() {
        return (T) choice;
    }

    @Override
    public <T> void setValue(T value) {
        choice = (String) value;
    }

    @Override
    public boolean verify() {
        // Verifying the choice is present in the choices.  When we generateModuleList getChoices, we should be getting the valid
        // options only.
        String[] choices = getChoices();

        for (String currChoice:choices) {
            if (choice.equals(currChoice)) return true;
        }

        return false;

    }
}
