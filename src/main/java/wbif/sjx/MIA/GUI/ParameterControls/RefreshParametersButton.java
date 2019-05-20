package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;
import wbif.sjx.MIA.Object.Parameters.RefreshButtonP;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class RefreshParametersButton extends ParameterControl implements ActionListener {
    private RefreshButtonP parameter;
    private JButton control;


    public RefreshParametersButton(RefreshButtonP parameter) {
        this.parameter = parameter;

        control = new JButton(parameter.getValueAsString());
        control.addActionListener(this);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModules();
        GUI.populateModuleParameters();
        GUI.updateModuleStates(true);

        updateControl();

    }
}
