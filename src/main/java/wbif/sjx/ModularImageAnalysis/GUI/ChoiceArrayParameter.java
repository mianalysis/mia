package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Object.HCParameter;

import javax.swing.*;

/**
 * Created by sc13967 on 22/05/2017.
 */
public class ChoiceArrayParameter extends JComboBox<String> {
    private HCParameter parameter;

    public ChoiceArrayParameter(HCParameter parameter, String[] choices) {
        super(choices);
        this.parameter = parameter;

    }

    public HCParameter getParameter() {
        return parameter;
    }
}
