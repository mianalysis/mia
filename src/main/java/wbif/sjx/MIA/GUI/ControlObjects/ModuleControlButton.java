package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;

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

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public void removeModule() {
        Module[] activeModules = GUI.getSelectedModules();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModules == null) return;

        // Getting lowest index
        ModuleCollection modules = GUI.getAnalysis().getModules();
        int lowestIdx = modules.indexOf(activeModules[0]);
        if (lowestIdx <= lastModuleEval) GUI.setLastModuleEval(lowestIdx - 1);

        // Removing modules
        for (Module activeModule:activeModules) {
            modules.remove(activeModule);
        }

        GUI.setSelectedModules(null);
        GUI.updateModules();
        GUI.updateModuleStates(true);
        GUI.populateModuleParameters();
        GUI.populateHelpNotes();

    }

    public void moveModuleUp() {
        MIA.log.writeDebug("To update");
        Module activeModule = GUI.getFirstSelectedModule();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModule != null) {
            ModuleCollection modules = GUI.getAnalysis().getModules();
            int idx = modules.indexOf(activeModule);

            if (idx != 0) {
                if (idx - 2 <= lastModuleEval) GUI.setLastModuleEval(idx - 2);

                modules.remove(activeModule);
                modules.add(idx - 1, activeModule);
                GUI.updateModules();
                GUI.updateModuleStates(true);

            }
        }
    }

    public void moveModuleDown() {
        MIA.log.writeDebug("To update");
        Module activeModule = GUI.getFirstSelectedModule();
        int lastModuleEval = GUI.getLastModuleEval();

        if (activeModule != null) {
            ModuleCollection modules = GUI.getAnalysis().getModules();
            int idx = modules.indexOf(activeModule);

            if (idx < modules.size()-1) {
                if (idx <= lastModuleEval) GUI.setLastModuleEval(idx - 1);

                modules.remove(activeModule);
                modules.add(idx + 1, activeModule);
                GUI.updateModules();
                GUI.updateModuleStates(true);

            }
        }
    }
}
