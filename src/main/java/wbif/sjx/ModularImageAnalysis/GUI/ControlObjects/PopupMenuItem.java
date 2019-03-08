package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.InputControl;
import wbif.sjx.ModularImageAnalysis.GUI.InputOutput.OutputControl;
import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class PopupMenuItem extends JMenuItem implements ActionListener {
    private Module module;

    public PopupMenuItem(Module module) {
        this.module = module;

        setText(module.getTitle());
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.getModuleListMenu().setVisible(false);

        if (module == null) return;

        // Adding it after the currently-selected module
        Module newModule = null;
        try {
            newModule = module.getClass().newInstance();
        } catch (IllegalAccessException | InstantiationException e1) {
            e1.printStackTrace();
        }

        Module activeModule = GUI.getActiveModule();
        if (activeModule == null || activeModule.getClass().isInstance(new InputControl()) || activeModule.getClass().isInstance(new OutputControl())) {
            GUI.getModules().add(newModule);
        } else {
            int idx = GUI.getModules().indexOf(GUI.getActiveModule());
            GUI.getModules().add(++idx,newModule);
        }
        GUI.setActiveModule(newModule);

        // Adding to the list of modules
        GUI.updateModules(true);
        GUI.populateModuleList();

        GUI.setActiveModule(newModule);

        if (GUI.isBasicGUI()) {
            GUI.populateBasicModules();
        } else {
            GUI.populateModuleParameters();
            GUI.populateHelpNotes();
            GUI.populateBasicHelpNotes();
        }

        GUI.getModuleListMenu().setVisible(false);

    }
}
