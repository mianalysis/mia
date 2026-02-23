package io.github.mianalysis.mia.object.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class ChoiceP extends ChoiceType {
    private String[] choices;

    public ChoiceP(String name, ModuleI module, @NotNull String choice, @NotNull String[] choices) {
        super(name, module);
        this.choice = choice;
        this.choices = choices;

    }

    public ChoiceP(String name, ModuleI module, @NotNull String choice, @NotNull String[] choices, String description) {
        super(name, module, description);
        this.choice = choice;
        this.choices = choices;

    }

    public void setChoices(String[] choices) {
        this.choices = choices;

    }

    @Override
    public String[] getChoices() {
        return choices;
    }

    @Override
    public String getRawStringValue() {
        return choice;
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        ChoiceP newParameter = new ChoiceP(getName(), newModule, choice, getChoices(), getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
    }
}
