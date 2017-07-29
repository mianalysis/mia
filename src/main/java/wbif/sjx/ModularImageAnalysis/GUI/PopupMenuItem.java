package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class PopupMenuItem extends JMenuItem implements ActionListener {
    private MainGUI gui;
    private HCModule module;

    PopupMenuItem(MainGUI gui, HCModule module) {
        this.gui = gui;
        this.module = module;
        if (module != null) setText(module.getTitle());
        addActionListener(this);

    }

    public HCModule getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gui.getModuleListMenu().setVisible(false);

        if (module == null) return;

        // Adding it after the currently-selected module
        HCModule newModule = null;
        try {
            newModule = module.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        if (gui.getActiveModule() != null) {
            int idx = gui.getModules().indexOf(gui.getActiveModule());
            gui.setActiveModule(newModule);
            gui.getModules().add(++idx,newModule);

        } else {
            gui.setActiveModule(newModule);
            gui.getModules().add(newModule);

        }

        // Adding to the list of modules
        gui.populateModuleList();

        gui.setActiveModule(newModule);

        if (gui.isBasicGUI()) {
            gui.populateBasicModules();
        } else {
            gui.populateModuleParameters();
        }

    }
}
