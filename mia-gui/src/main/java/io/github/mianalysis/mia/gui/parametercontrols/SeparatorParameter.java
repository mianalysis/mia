package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.object.parameters.SeparatorP;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class SeparatorParameter extends ParameterControl {
    protected JPanel control;

    public SeparatorParameter(SeparatorP parameter) {
        super(parameter);

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        control = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        JSeparator separatorLeft = new JSeparator();
        separatorLeft.setForeground(Colours.getDarkBlue(isDark));
        c.weightx = 1;
        c.gridx++;
        c.insets = new Insets(0, 0, 0, 5);
        control.add(separatorLeft, c);

        JLabel label = new JLabel();
        label.setText(parameter.getNickname());
        label.setForeground(Colours.getDarkBlue(isDark));
        c.weightx = 0;
        c.gridx++;
        c.insets = new Insets(0, 0, 0, 0);
        control.add(label, c);

        JSeparator separatorRight = new JSeparator();
        separatorRight.setForeground(Colours.getDarkBlue(isDark));
        c.weightx = 1;
        c.gridx++;
        c.insets = new Insets(0, 5, 0, 0);
        control.add(separatorRight, c);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {

    }
}
