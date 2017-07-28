package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    private MainGUI gui;
    private HCModule module;


    // CONSTRUCTOR

    ModuleButton(MainGUI gui, HCModule module) {
        this.gui = gui;
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        if (module != null) setText(module.getTitle());

    }


    // GETTERS

    public HCModule getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.setActiveModule(module);

        if (gui.isBasicGUI()) {
            gui.populateBasicModules();

        } else {
            gui.populateModuleParameters();

        }
    }
}
