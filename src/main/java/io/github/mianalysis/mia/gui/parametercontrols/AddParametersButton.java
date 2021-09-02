package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class AddParametersButton extends ParameterControl implements ActionListener {
    private JButton control;


    public AddParametersButton(ParameterGroup parameter) {
        super(parameter);

        control = new JButton("Add");
        control.addActionListener(this);
        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

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
        GUI.addUndo();

        ((ParameterGroup) parameter).addParameters();

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        GUI.updateModuleStates(true);
        GUI.updateModules();
        GUI.updateParameters();

        updateControl();

    }
}
