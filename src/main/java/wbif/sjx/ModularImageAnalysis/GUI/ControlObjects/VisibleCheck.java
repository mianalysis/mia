package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 06/06/2017.
 */
public class VisibleCheck extends JCheckBox implements ActionListener {
    private Parameter parameter;

    public VisibleCheck(Parameter parameter) {
        this.parameter = parameter;

        setSelected(parameter.isVisible());
        addActionListener(this);

    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setVisible(isSelected());

    }
}
