package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;
import wbif.sjx.ModularImageAnalysis.Object.ModuleCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleControlButton extends JButton implements ActionListener {
    public static final String ADD_MODULE = "+";
    public static final String REMOVE_MODULE = "-";
    public static final String MOVE_MODULE_UP = "▲";
    public static final String MOVE_MODULE_DOWN = "▼";

    private JPopupMenu moduleListMenu;

    public ModuleControlButton(String command, int buttonSize, JPopupMenu moduleListMenu) {
        this.moduleListMenu = moduleListMenu;

        setText(command);
        addActionListener(this);
        setMargin(new Insets(0,0,0,0));
        setFocusPainted(false);
        setMargin(new Insets(0,0,0,0));
        setPreferredSize(new Dimension(buttonSize, buttonSize));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (getText()) {
            case ADD_MODULE:
                addModule();
                break;

            case REMOVE_MODULE:
                removeModule();
                break;

            case MOVE_MODULE_UP:
                moveModuleUp();
                break;

            case MOVE_MODULE_DOWN:
                moveModuleDown();
                break;

        }
    }

    public void addModule() {
        // Populating the list containing all available modules
        moduleListMenu.show(GUI.getFrame(), 0, 0);
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);
        GUI.updateModules(true);

    }

    public void removeModule() {
        Module activeModule = GUI.getActiveModule();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModule != null) {
            ModuleCollection modules = GUI.getAnalysis().getModules();
            // Removing a module resets all the current evaluation
            int idx = modules.indexOf(activeModule);

            if (idx <= lastModuleEval) lastModuleEval = idx - 1;

            modules.remove(activeModule);
            activeModule = null;

            GUI.updateModules(true);
            GUI.populateModuleParameters();
            GUI.populateHelpNotes();

        }
    }

    public void moveModuleUp() {
        Module activeModule = GUI.getActiveModule();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModule != null) {
            ModuleCollection modules = GUI.getAnalysis().getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != 0) {
                if (idx - 2 <= lastModuleEval) lastModuleEval = idx - 2;

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                GUI.updateModules(true);

            }
        }
    }

    public void moveModuleDown() {
        Module activeModule = GUI.getActiveModule();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModule != null) {
            ModuleCollection modules = GUI.getAnalysis().getModules();
            int idx = modules.indexOf(activeModule);

            if (idx < modules.size()-1) {
                if (idx <= lastModuleEval) lastModuleEval = idx - 1;

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                GUI.updateModules(true);

            }
        }
    }
}
