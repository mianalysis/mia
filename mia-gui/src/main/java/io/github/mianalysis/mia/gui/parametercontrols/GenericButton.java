package io.github.mianalysis.mia.gui.parametercontrols;

import javax.swing.JButton;
import javax.swing.JComponent;

import io.github.mianalysis.mia.object.parameters.GenericButtonP;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;

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
