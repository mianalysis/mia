package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    private GUI gui;
    private Module module;


    // CONSTRUCTOR

    public ModuleButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setText(module.getNickname());

    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.setActiveModule(module);

        gui.updateModules();
    }
}
