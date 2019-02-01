package wbif.sjx.ModularImageAnalysis.Object.Parameters;

import wbif.sjx.ModularImageAnalysis.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class ChoiceP extends ChoiceType {
    private WiderDropDownCombo control;
    private String[] choices;

    public ChoiceP(String name, Module module, @Nonnull String choice, @Nonnull String[] choices) {
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

    @Override
    public <T extends Parameter> T duplicate() {
        return (T) new ChoiceP(name,module,getChoice(),getChoices());
    }
}
