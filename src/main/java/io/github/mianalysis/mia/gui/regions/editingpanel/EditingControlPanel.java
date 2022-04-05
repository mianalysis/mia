package io.github.mianalysis.mia.gui.regions.editingpanel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EtchedBorder;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.abstrakt.AnalysisControlButton;
import io.github.mianalysis.mia.gui.regions.abstrakt.ModuleControlButton;
import io.github.mianalysis.mia.gui.regions.availablemodulelist.ModuleListMenu;
import io.github.mianalysis.mia.gui.regions.availablemodulelist.SearchForModuleItem;
import io.github.mianalysis.mia.module.Categories;
import io.github.mianalysis.mia.module.Category;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Preferences;

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

    private void addCategoryModules(JPopupMenu rootMenu, ModuleListMenu parentMenu, Category category, boolean ignoreEmptyCategories) {
        // Adding child categories
        for (Category childCategory : category.getChildren()) {
            ModuleListMenu childCategoryMenu = new ModuleListMenu(childCategory.getName(), new ArrayList<>(), rootMenu);

            // If this category isn't to be shown, skip to next (note: child modules also won't be shown)
            if (!childCategory.showInMenu())
                continue;

            if (childCategory.getModuleCount() == 0)
                continue;
                
            if (parentMenu == null)
                rootMenu.add(childCategoryMenu);
            else
                parentMenu.add(childCategoryMenu);

            addCategoryModules(rootMenu, childCategoryMenu, childCategory, ignoreEmptyCategories);

        }

        // Adding modules
        if (parentMenu != null)
            for (Module module : GUI.getAvailableModules())
                if (module.getCategory() == category)
                    parentMenu.addMenuItem(module);

    }

    public void listAvailableModules() {
        Category root = Categories.getRootCategory();
        moduleListMenu.removeAll();

        // Iterating over all categories, resetting counts
        root.resetModuleCount(true);
        
        // Iterating over all available modules, adding their count to the relevant categories
        for (Module availableModule : GUI.getAvailableModules())
            if (!availableModule.isDeprecated() || MIA.preferences.showDeprecated())
                if (MIA.preferences.getDataStorageMode().equals(Preferences.DataStorageModes.KEEP_IN_RAM) || !availableModule.getIL2Support().equals(IL2Support.NONE))
                availableModule.getCategory().incrementModuleCount(true);

        addCategoryModules(moduleListMenu, null, root,true);

        moduleListMenu.add(new SearchForModuleItem());

    }
}
