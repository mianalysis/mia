package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends ParameterControl implements FocusListener {
    private Module module;
    private TextType parameter;
    private JTextField control;

    public TextParameter(TextType parameter) {
        this.parameter = parameter;

        control = new JTextField();

        String name = parameter.getValueAsString();
        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setText(name);
        control.addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        String text = control.getText();

        parameter.setValueFromString(text);

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx - 1);

        GUI.updateModules(true);

    }

    @Override
    public JComponent getControl() {
        return control;
    }

    @Override
    public void updateControl() {

    }
}
