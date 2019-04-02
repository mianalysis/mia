package wbif.sjx.MIA.Object.Parameters;

import wbif.sjx.MIA.GUI.ParameterControls.WiderDropDownCombo;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.Parameters.Abstract.ChoiceType;
import wbif.sjx.MIA.Object.Parameters.Abstract.Parameter;

import javax.annotation.Nonnull;

public class ChoiceP extends ChoiceType {
    private WiderDropDownCombo control;
    private String[] choices;

    public ChoiceP(String name, Module module, @Nonnull String choice, @Nonnull String[] choices) {
        super(name,module);
        this.choice = choice;
        this.choices = choices;

    }

    public ChoiceP(String name, Module module, @Nonnull String choice, @Nonnull String[] choices, String description) {
        super(name,module,description);
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
        return (T) new ChoiceP(name,module,getChoice(),getChoices(),getDescription());
    }
}
