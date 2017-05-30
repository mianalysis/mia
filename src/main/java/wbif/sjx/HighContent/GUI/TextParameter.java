package wbif.sjx.HighContent.GUI;

import wbif.sjx.HighContent.Object.HCParameter;

import javax.swing.*;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends JTextField {
    private HCParameter parameter;

    public TextParameter(HCParameter parameter) {
        this.parameter = parameter;

    }

    public HCParameter getParameter() {
        return parameter;
    }
}
