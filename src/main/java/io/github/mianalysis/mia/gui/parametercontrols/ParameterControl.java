package io.github.mianalysis.mia.gui.parametercontrols;

import javax.swing.JComponent;

import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public abstract class ParameterControl {
    protected Parameter parameter;
    public abstract JComponent getComponent();

    public abstract void updateControl();
    
    public ParameterControl(Parameter parameter) {
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }
}
