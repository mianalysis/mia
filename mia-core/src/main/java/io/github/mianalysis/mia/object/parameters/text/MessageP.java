package io.github.mianalysis.mia.object.parameters.text;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.parameters.ParameterState;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;



public class MessageP extends TextAreaP {
    private ParameterState state = ParameterState.NORMAL;
    private int controlHeight = 50;

    public MessageP(String name, ModuleI module, ParameterState state) {
        super(name, module, false);
        this.state = state;
    }

    public MessageP(String name, ModuleI module, ParameterState state, int controlHeight) {
        super(name, module, false);
        this.state = state;
        this.controlHeight = controlHeight;
    }

    public MessageP(String name, ModuleI module, @NotNull String value, ParameterState state) {
        super(name, module, value, false);
        this.state = state;
    }

    public MessageP(String name, ModuleI module, @NotNull String value, ParameterState state, int controlHeight) {
        super(name, module, value, false);
        this.state = state;
        this.controlHeight = controlHeight;
    }

    public MessageP(String name, ModuleI module, @NotNull String value, ParameterState state, String description) {
        super(name, module, value, false, description);
        this.state = state;
    }

    public ParameterState getState() {
        return state;
    }

    public void setState(ParameterState state) {
        this.state = state;
    }

    public void setControlHeight(int controlHeight) {
        this.controlHeight = controlHeight;
    }

    @Override
    public ParameterControl getControl() {
        return ParameterControlFactory.getMessageTypeControl(this, controlHeight);
    }

    @Override
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        MessageP newParameter = new MessageP(name, newModule, getRawStringValue(), state, getDescription());
        
        newParameter.setControlHeight(controlHeight);
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
