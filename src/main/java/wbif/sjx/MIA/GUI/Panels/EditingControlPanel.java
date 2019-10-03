package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleListMenu;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class EditingControlPanel extends JPanel {
    private ModuleControlButton addModuleButton = null;
    private static final JPopupMenu moduleListMenu = new JPopupMenu();

    public EditingControlPanel() {
        addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE,GUI.getBigButtonSize(),moduleListMenu);
        listAvailableModules();

        int bigButtonSize = GUI.getBigButtonSize();
        int frameHeight = GUI.getFrameHeight();
        int statusHeight = GUI.getStatusHeight();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.PAGE_START;
        
        setMaximumSize(new Dimension(bigButtonSize + 20, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(bigButtonSize + 20, frameHeight - statusHeight-350));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

        // Add module button
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(ModuleControlButton.REMOVE_MODULE,bigButtonSize,moduleListMenu);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_UP,bigButtonSize,moduleListMenu);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_DOWN,bigButtonSize,moduleListMenu);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.weighty = 0;
        add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,bigButtonSize);
        c.gridy++;
        add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,bigButtonSize);
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 5);
        add(stopAnalysisButton, c);

        validate();
        repaint();

    }

    private void listAvailableModules() {
        addModuleButton.setEnabled(false);
        addModuleButton.setToolTipText("Loading modules");

        TreeMap<String, Module> availableModules = GUI.getAvailableModules();
        TreeSet<ModuleListMenu> topList = new TreeSet<>();
        TreeSet<String> moduleNames = new TreeSet<>();
        moduleNames.addAll(availableModules.keySet());

        for (String name : moduleNames) {
            // ActiveList starts at the top list
            TreeSet<ModuleListMenu> activeList = topList;
            ModuleListMenu activeItem = null;

            String[] names = name.split("\\\\");
            for (int i = 0; i < names.length-1; i++) {
                boolean found = false;
                for (ModuleListMenu listItemm : activeList) {
                    if (listItemm.getName().equals(names[i])) {
                        activeItem = listItemm;
                        found = true;
                    }
                }

                if (!found) {
                    ModuleListMenu newItem = new ModuleListMenu(names[i], new ArrayList<>(),moduleListMenu);
                    newItem.setName(names[i]);
                    activeList.add(newItem);
                    if (activeItem != null) activeItem.add(newItem);
                    activeItem = newItem;
                }

                activeList = activeItem.getChildren();

            }

            if (activeItem != null) activeItem.addMenuItem(availableModules.get(name));

        }

        for (ModuleListMenu listMenu : topList) moduleListMenu.add(listMenu);

        addModuleButton.setToolTipText("Add module");
        addModuleButton.setEnabled(true);

    }
}
