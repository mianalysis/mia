package io.github.mianalysis.mia.object.parameters.abstrakt;

import javax.swing.JComponent;

public abstract class ParameterControl {
    protected ParameterI parameter;
    public abstract JComponent getComponent();

    public abstract void updateControl();
    
    public ParameterControl(ParameterI parameter) {
        this.parameter = parameter;
    }

    public ParameterI getParameter() {
        return parameter;
    }

    public void setParameter(ParameterI parameter) {
        this.parameter = parameter;
    }
}
