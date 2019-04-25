package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.Object.Parameters.Abstract.TextType;
import wbif.sjx.MIA.Object.Parameters.GUISeparatorP;

import javax.swing.*;
import java.awt.*;

public class SeparatorParameter extends ParameterControl {
    protected GUISeparatorP parameter;
    protected JPanel control;

    public SeparatorParameter(GUISeparatorP parameter) {
        this.parameter = parameter;

        control = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        JSeparator separatorLeft = new JSeparator();
        separatorLeft.setForeground(Color.BLUE);
        c.weightx = 1;
        c.gridx++;
        c.insets = new Insets(0,0,0,5);
        control.add(separatorLeft,c);

        JLabel label = new JLabel();
        label.setText(parameter.getValueAsString());
        label.setForeground(Color.BLUE);
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(0,0,0,0);
        control.add(label,c);

        JSeparator separatorRight = new JSeparator();
        separatorRight.setForeground(Color.BLUE);
        c.weightx = 1;
        c.gridx++;
        c.insets = new Insets(0,5,0,0);
        control.add(separatorRight,c);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }
}
