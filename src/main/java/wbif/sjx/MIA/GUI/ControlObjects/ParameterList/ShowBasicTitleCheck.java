package wbif.sjx.MIA.GUI.ControlObjects.ParameterList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowBasicTitleCheck extends JCheckBox implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 4959776467163361296L;
    private Module module;

    public ShowBasicTitleCheck(Module module) {
        this.module = module;

        this.setSelected(module.canShowBasicTitle());
        setText("Show basic title  ");
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.addUndo();

        module.setShowBasicTitle(isSelected());

    }
}
