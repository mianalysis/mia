package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.GUI.ParameterControl;
import wbif.sjx.ModularImageAnalysis.Object.Parameters.Abstract.TextType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends ParameterControl implements FocusListener {
    protected TextType parameter;
    protected JTextField control;

    public TextParameter(TextType parameter) {
        this.parameter = parameter;

        control = new JTextField();

        control.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        control.setText(parameter.getValueAsString());
        control.addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        parameter.setValueFromString(control.getText());

        int idx = GUI.getModules().indexOf(parameter.getModule());
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        updateControl();

        GUI.updateModuleParameters(parameter.getModule());
//        GUI.updateModules(true);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        control.setText(parameter.getValueAsString());
    }
}
