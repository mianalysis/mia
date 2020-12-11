package wbif.sjx.MIA.GUI.Panels;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EtchedBorder;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.ControlObjects.AnalysisControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleControlButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleListMenu;
import wbif.sjx.MIA.Module.Categories;
import wbif.sjx.MIA.Module.Category;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

public class EditingControlPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 2461857783317770626L;
    private ModuleControlButton addModuleButton = null;
    private static final JPopupMenu moduleListMenu = new JPopupMenu();

    private static final int minimumWidth = GUI.getBigButtonSize() + 20;

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public EditingControlPanel() {
        addModuleButton = new ModuleControlButton(ModuleControlButton.ADD_MODULE, GUI.getBigButtonSize(),
                moduleListMenu);
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

        setMaximumSize(new Dimension(minimumWidth, Integer.MAX_VALUE));
        setMinimumSize(new Dimension(minimumWidth, frameHeight - statusHeight - 350));

        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setLayout(new GridBagLayout());

        // Add module button
        addModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        add(addModuleButton, c);

        // Remove module button
        ModuleControlButton removeModuleButton = new ModuleControlButton(ModuleControlButton.REMOVE_MODULE,
                bigButtonSize, moduleListMenu);
        removeModuleButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        c.gridy++;
        add(removeModuleButton, c);

        // Move module up button
        ModuleControlButton moveModuleUpButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_UP,
                bigButtonSize, moduleListMenu);
        moveModuleUpButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        add(moveModuleUpButton, c);

        // Move module down button
        ModuleControlButton moveModuleDownButton = new ModuleControlButton(ModuleControlButton.MOVE_MODULE_DOWN,
                bigButtonSize, moduleListMenu);
        moveModuleDownButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        c.gridy++;
        add(moveModuleDownButton, c);

        // Load analysis protocol button
        AnalysisControlButton loadAnalysisButton = new AnalysisControlButton(AnalysisControlButton.LOAD_ANALYSIS,
                bigButtonSize);
        c.gridy++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.PAGE_END;
        add(loadAnalysisButton, c);

        // Save analysis protocol button
        AnalysisControlButton saveAnalysisButton = new AnalysisControlButton(AnalysisControlButton.SAVE_ANALYSIS,
                bigButtonSize);
        c.gridy++;
        c.weighty = 0;
        add(saveAnalysisButton, c);

        // Start analysis button
        AnalysisControlButton startAnalysisButton = new AnalysisControlButton(AnalysisControlButton.START_ANALYSIS,
                bigButtonSize);
        c.gridy++;
        add(startAnalysisButton, c);

        // Stop analysis button
        AnalysisControlButton stopAnalysisButton = new AnalysisControlButton(AnalysisControlButton.STOP_ANALYSIS,
                bigButtonSize);
        c.gridy++;
        c.insets = new Insets(5, 5, 5, 5);
        add(stopAnalysisButton, c);

        validate();
        repaint();

    }

    private void addCategoryModules(JPopupMenu rootMenu, ModuleListMenu parentMenu, Category category) {
        // Adding child categories
        for (Category childCategory : category.getChildren()) {
            ModuleListMenu childCategoryMenu = new ModuleListMenu(childCategory.getName(), new ArrayList<>(), rootMenu);

            if (parentMenu == null)
                rootMenu.add(childCategoryMenu);
            else
                parentMenu.add(childCategoryMenu);
            addCategoryModules(rootMenu, childCategoryMenu, childCategory);
        }

        // Adding modules
        if (parentMenu != null)
            for (Module module : GUI.getAvailableModules())
                if (module.getCategory() == category)
                    parentMenu.addMenuItem(module);

    }

    private void listAvailableModules() {
        addModuleButton.setEnabled(false);
        addModuleButton.setToolTipText("Loading modules");

        Category root = Categories.getRootCategory();
        addCategoryModules(moduleListMenu, null, root);

        // TreeMap<String, Module> availableModules = GUI.getAvailableModules();
        // TreeSet<ModuleListMenu> topList = new TreeSet<>();
        // TreeSet<String> moduleNames = new TreeSet<>();
        // moduleNames.addAll(availableModules.keySet());

        // for (String name : moduleNames) {
        // // ActiveList starts at the top list
        // TreeSet<ModuleListMenu> activeList = topList;
        // ModuleListMenu activeItem = null;

        // String[] names = name.split("\\\\");
        // for (int i = 0; i < names.length-1; i++) {
        // String curr_name = names[i];
        // if (name.substring(0,1).equals(".")) curr_name = curr_name.substring(1);

        // boolean found = false;
        // for (ModuleListMenu listItem : activeList) {
        // if (listItem.getName().equals(curr_name)) {
        // activeItem = listItem;
        // found = true;
        // }
        // }

        // if (!found) {
        // // It's a new package
        // ModuleListMenu newItem = new ModuleListMenu(curr_name, new
        // ArrayList<>(),moduleListMenu);
        // newItem.setName(curr_name);
        // activeList.add(newItem);
        // if (activeItem != null) activeItem.add(newItem);
        // activeItem = newItem;
        // }

        // activeList = activeItem.getChildren();

        // }

        // if (activeItem != null) activeItem.addMenuItem(availableModules.get(name));

        // }

        // for (ModuleListMenu listMenu : topList) {
        // moduleListMenu.add(listMenu);
        // }

        addModuleButton.setToolTipText("Add module");
        addModuleButton.setEnabled(true);

    }
}
