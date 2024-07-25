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
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends ParameterControl implements CaretReporter, FocusListener {
    protected JTextField control;
    protected int caretPosition = 0;

    public TextParameter(TextType parameter) {
        super(parameter);

        control = new JTextField();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setText(parameter.getRawStringValue());
        control.addFocusListener(this);

    }

    public int getCaretPosition() {
        return caretPosition;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        caretPosition = control.getCaretPosition();
        GUI.addUndo();

        parameter.setValueFromString(control.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval() & !(parameter.getModule() instanceof OutputControl))
            GUI.setLastModuleEval(idx - 1);

        // GUI.updateParameters();
        GUI.updateModuleStates(true, parameter.getModule());
        updateControl();

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setText(parameter.getRawStringValue());
    }
}
