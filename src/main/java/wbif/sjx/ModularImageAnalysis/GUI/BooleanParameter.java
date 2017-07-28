package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;
import wbif.sjx.ModularImageAnalysis.Object.Parameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class BooleanParameter extends JCheckBox implements ActionListener {
    private MainGUI gui;
    private HCModule module;
    private Parameter parameter;

    public BooleanParameter(MainGUI gui, HCModule module, Parameter parameter) {
        this.gui = gui;
        this.module = module;
        this.parameter = parameter;

        setSelected(parameter.getValue());
        addActionListener(this);

    }

    public HCModule getModule() {
        return module;
    }

    public Parameter getParameter() {
        return parameter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        parameter.setValue(isSelected());

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) gui.setLastModuleEval(idx-1);

        if (gui.isBasicGUI()) {
            gui.populateBasicModules();
        } else {
            gui.populateModuleList();
            gui.populateModuleParameters();
        }
    }
}
