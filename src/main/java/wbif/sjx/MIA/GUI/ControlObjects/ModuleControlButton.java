package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

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
        GUI.addUndo();

        // Populating the list containing all available modules
        moduleListMenu.show(GUI.getFrame(), 0, 0);
        moduleListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        moduleListMenu.setVisible(true);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public void removeModule() {
        GUI.addUndo();

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
        GUI.updateParameters();
        GUI.updateHelpNotes();

    }

    public void moveModuleUp() {
        GUI.addUndo();

        ModuleCollection modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules== null) return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[0]-1;
        if (toIndex < 0) return;

        modules.reorder(fromIndices,toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (toIndex <= lastModuleEval) GUI.setLastModuleEval(toIndex - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }

    public void moveModuleDown() {
        GUI.addUndo();

        ModuleCollection modules = GUI.getAnalysis().getModules();
        Module[] selectedModules = GUI.getSelectedModules();
        if (selectedModules== null) return;

        int[] fromIndices = GUI.getSelectedModuleIndices();
        int toIndex = fromIndices[fromIndices.length-1]+2;
        if (toIndex > modules.size()) return;

        modules.reorder(fromIndices,toIndex);

        int lastModuleEval = GUI.getLastModuleEval();
        if (fromIndices[0] <= lastModuleEval) GUI.setLastModuleEval(fromIndices[0] - 1);

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }
}
