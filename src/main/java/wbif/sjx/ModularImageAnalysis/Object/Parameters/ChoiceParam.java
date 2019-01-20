package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;

public class ChoiceParam extends ChoiceType {
    private WiderDropDownCombo control;
    private String[] choices;

    public ChoiceParam(String name, Module module, String choice, String[] choices) {
        super(name,module);
        this.choice = choice;
        this.choices = choices;
    }

    @Override
    public String[] getChoices() {
        return choices;
    }

    @Override
    public String getValueAsString() {
        return getChoice();
    }

}
