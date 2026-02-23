package io.github.mianalysis.mia.object.parameters;

import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.object.WorkspaceI;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.process.ParameterControlFactory;

public class SeparatorP extends Parameter {
    public SeparatorP(String name, ModuleI module) {
        super(name, module);
        setExported(false);

    }

    @Override
    protected ParameterControl initialiseControl() {
        return ParameterControlFactory.getSeparatorControl(this);
    }

    @Override
    public <T> T getValue(WorkspaceI workspace) {
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
    public <T extends Parameter> T duplicate(ModuleI newModule) {
        SeparatorP newParameter = new SeparatorP(name, newModule);

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
