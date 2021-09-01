package io.github.mianalysis.MIA.GUI.ParameterControls;

import javax.swing.JButton;
import javax.swing.JComponent;

import io.github.mianalysis.MIA.Object.Parameters.GenericButtonP;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class GenericButton extends ParameterControl {
    private JButton control;

    public GenericButton(GenericButtonP parameter) {
        super(parameter);
        
        control = new JButton(parameter.getRawStringValue());
        control.addActionListener(parameter.getActionListener());

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }
}
