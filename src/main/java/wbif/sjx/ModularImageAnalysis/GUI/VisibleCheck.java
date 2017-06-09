package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Object.HCParameter;

import javax.swing.*;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class VisibleCheck extends JCheckBox {
    private HCParameter parameter;

    public VisibleCheck(HCParameter parameter) {
        this.setSelected(parameter.isVisible());
        this.setName("VisibleCheck");
        this.parameter = parameter;

    }

    public HCParameter getParameter() {
        return parameter;
    }
}
