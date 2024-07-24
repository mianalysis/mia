package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends TextSwitchableParameterControl implements ActionListener {
    private JCheckBox control;

    public BooleanParameter(BooleanType parameter) {
        super(parameter);

        control = new JCheckBox();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setSelected(parameter.isSelected());
        control.addActionListener(this);
        control.setOpaque(false);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        ((BooleanType) parameter).setSelected(control.isSelected());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl)) GUI.setLastModuleEval(idx-1);

        GUI.updateModules(true, parameter.getModule());
        GUI.updateParameters(true, parameter.getModule());

        updateControl();

    }
    @Override
    public JComponent getDefaultComponent() {
        return control;
    }

    @Override
    public void updateDefaultControl() {
        control.setSelected(((BooleanType) parameter).isSelected());
    }
}
