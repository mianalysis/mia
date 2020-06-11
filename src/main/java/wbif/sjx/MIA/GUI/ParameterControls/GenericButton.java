package wbif.sjx.MIA.GUI.ParameterControls;

import javax.swing.JButton;
import javax.swing.JComponent;

import wbif.sjx.MIA.Object.Parameters.GenericButtonP;

/**
 * Created by Stephen Cross on 01/02/2019.
 */
public class GenericButton extends ParameterControl {
    private JButton control;

    public GenericButton(GenericButtonP parameter) {
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
