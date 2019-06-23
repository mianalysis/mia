package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledCheck extends JCheckBox implements ActionListener {
    private Module module;

    public ModuleEnabledCheck(Module module) {
        this.module = module;

        setSelected(module.isEnabled());
        setName("ModuleEnabledCheck");
        setEnabled(module.canBeDisabled());

        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        module.setEnabled(isSelected());

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        GUI.updateModules();
        GUI.updateModuleStates(true);
    }
}
