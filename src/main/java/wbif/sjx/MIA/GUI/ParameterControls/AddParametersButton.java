package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.ParameterGroup;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class AddParametersButton extends ParameterControl implements ActionListener {
    private ParameterGroup parameter;
    private JButton control;


    public AddParametersButton(ParameterGroup parameter) {
        this.parameter = parameter;

        control = new JButton("Add");
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
        parameter.addParameters();

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModules();
        GUI.populateModuleParameters();
        GUI.updateModuleStates(true);

        updateControl();

    }
}
