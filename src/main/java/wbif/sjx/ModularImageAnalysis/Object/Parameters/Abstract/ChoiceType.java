package wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.ChoiceArrayParameter;
import wbif.sjx.ModularImageAnalysis.Module.Module;

public abstract class ChoiceType extends Parameter {
    protected String choice;

    public ChoiceType(String name, Module module) {
        super(name, module);
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public abstract String[] getChoices();

    @Override
    protected ParameterControl initialiseControl() {
        return new ChoiceArrayParameter(this);
    }
}
