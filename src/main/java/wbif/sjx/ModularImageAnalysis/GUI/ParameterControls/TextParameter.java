package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class TextParameter extends JTextField implements FocusListener {
    private Module module;
    private Parameter parameter;

    public TextParameter(Module module, Parameter parameter) {
        this.module = module;
        this.parameter = parameter;

        String name = parameter.getValue() == null ? "" : parameter.getValue().toString();
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(name);
        addFocusListener(this);

    }

    public Module getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        new Thread(() -> {
            String text = getText();

            if (parameter.getType() == Parameter.OUTPUT_IMAGE | parameter.getType() == Parameter.OUTPUT_OBJECTS) {
                parameter.setValue(text);

            } else if (parameter.getType() == Parameter.INTEGER) {
                parameter.setValue(Integer.valueOf(text));

            } else if (parameter.getType() == Parameter.DOUBLE) {
                parameter.setValue(Double.valueOf(text));

            } else if (parameter.getType() == Parameter.STRING) {
                parameter.setValue(text);

            }

            int idx = GUI.getModules().indexOf(module);
            if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx - 1);

            GUI.updateTestFile();
            GUI.updateModules(true);

        }).start();
    }
}
