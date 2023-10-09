package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.gui.parametercontrols.SeparatorParameter;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;

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
    public <T> T getValue(Workspace workspace) {
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
