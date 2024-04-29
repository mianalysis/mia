package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;

public abstract class TextSwitchableParameterControl extends ParameterControl implements FocusListener {
    private JTextField textControl;

    public abstract JComponent getDefaultComponent();
    public abstract void updateDefaultControl();

    public TextSwitchableParameterControl(TextSwitchableParameter parameter) {
        super(parameter);

        textControl = new JTextField();
        textControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textControl.setText(parameter.getRawStringValue());
        textControl.addFocusListener(this);

    }

    @Override
    public JComponent getComponent() {
        if (((TextSwitchableParameter) parameter).isShowText())
            return getTextComponent();
        else
            return getDefaultComponent();
    }

    public JComponent getTextComponent() {
        return textControl;
    }

    public void updateTextControl() {
        
    }

    @Override
    public void updateControl() {
        if (((TextSwitchableParameter) parameter).isShowText())
            updateTextControl();
        else
            updateDefaultControl();
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        parameter.setValueFromString(textControl.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        GUI.updateParameters();
        GUI.updateModuleStates();

        updateControl();

    }
}
