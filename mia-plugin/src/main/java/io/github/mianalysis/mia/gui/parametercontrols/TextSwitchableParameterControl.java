package io.github.mianalysis.mia.gui.parametercontrols;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.core.OutputControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.CaretReporter;
import io.github.mianalysis.mia.object.parameters.abstrakt.ParameterControl;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextSwitchableParameter;

public abstract class TextSwitchableParameterControl extends ParameterControl implements CaretReporter, FocusListener {
    private JTextField textControl;
    protected int caretPosition = 0;

    public abstract JComponent getDefaultComponent();
    public abstract void updateDefaultControl();

    public TextSwitchableParameterControl(TextSwitchableParameter parameter) {
        super(parameter);

        textControl = new JTextField();
        textControl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textControl.setText(parameter.getRawStringValue());
        textControl.addFocusListener(this);

    }

    public int getCaretPosition() {
        return caretPosition;
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
        textControl.setText(parameter.getRawStringValue());
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
        caretPosition = textControl.getCaretPosition();
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
