package io.github.mianalysis.mia.gui.regions.parameterlist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.ModuleI;

public class DisableableCheck extends JCheckBox implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 4959776467163361296L;
    private ModuleI module;

    public DisableableCheck(ModuleI module) {
        this.module = module;

        this.setSelected(module.canBeDisabled());
        setText("Can be disabled  ");
        addActionListener(this);

    }

    public ModuleI getModule() {
        return module;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        module.setCanBeDisabled(isSelected());

    }
}
