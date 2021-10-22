package io.github.mianalysis.mia.gui.regions.parameterlist;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowProcessingTitleCheck extends JCheckBox implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 4959776467163361296L;
    private Module module;

    public ShowProcessingTitleCheck(Module module) {
        this.module = module;

        this.setSelected(module.canShowProcessingTitle());
        setText("Show basic title  ");
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        module.setShowProcessingViewTitle(isSelected());

    }
}
