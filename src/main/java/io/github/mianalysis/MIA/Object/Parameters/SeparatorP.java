package io.github.mianalysis.MIA.Object.Parameters;

import io.github.mianalysis.MIA.GUI.ParameterControls.ParameterControl;
import io.github.mianalysis.MIA.GUI.ParameterControls.SeparatorParameter;
import io.github.mianalysis.MIA.Module.Module;
import io.github.mianalysis.MIA.Object.Parameters.Abstract.Parameter;

public class SeparatorP extends Parameter {
    public SeparatorP(String name, Module module) {
        super(name, module);
        setExported(false);

    }

    @Override
    protected ParameterControl initialiseControl() {
        return new SeparatorParameter(this);

    }

    @Override
    public <T> T getValue() {
        return null;
    }

    @Override
    public <T> void setValue(T value) {

    }

    @Override
    public String getRawStringValue() {
        return "";
    }

    @Override
    public void setValueFromString(String string) {

    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        SeparatorP newParameter = new SeparatorP(name,newModule);

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
        
    }
}
