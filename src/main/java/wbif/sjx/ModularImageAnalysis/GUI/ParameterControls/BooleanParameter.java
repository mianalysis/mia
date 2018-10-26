package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends JCheckBox implements ActionListener {
    private Module module;
    private Parameter parameter;

    public BooleanParameter(Module module, Parameter parameter) {
        this.module = module;
        this.parameter = parameter;

        setSelected(parameter.getValue());
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setValue(isSelected());

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModules(true);
    }
}
