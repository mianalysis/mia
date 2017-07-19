package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class VisibleCheck extends JCheckBox {
    private Parameter parameter;

    public VisibleCheck(Parameter parameter) {
        this.setSelected(parameter.isVisible());
        this.setName("VisibleCheck");
        this.parameter = parameter;

    }

    public Parameter getParameter() {
        return parameter;
    }
}
