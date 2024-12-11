package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.system.GUISeparator;

public class ModuleListPanel extends JScrollPane {
    private static final long serialVersionUID = -8916783536735299254L;

    private JPanel moduleListPanel;
    private HashMap<Module,RowItems> rowItems = new HashMap<>();

    public ModuleListPanel() {
        moduleListPanel = new JPanel();

        setViewportView(moduleListPanel);

        // Initialising the scroll panel
        setViewportBorder(BorderFactory.createEmptyBorder());
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);
        setOpaque(false);
        getViewport().setOpaque(false);

        // Initialising the panel for module buttons
        moduleListPanel.setBorder(BorderFactory.createEmptyBorder());
        moduleListPanel.setLayout(new GridBagLayout());
        moduleListPanel.setOpaque(false);
        moduleListPanel.validate();
        moduleListPanel.repaint();

        validate();
        repaint();

    }

    public void updatePanel() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        Modules modules = GUI.getModules();
        HashMap<Module, Boolean> expandedStatus = getExpandedModules(modules);
        // Get number of visible modules
        int expandedCount = (int) expandedStatus.values().stream().filter(p -> p).count();

        // Adding content
        String[] columnNames = { "Title" };
        Object[][] data = new Object[expandedCount][1];

        int count = 0;
        for (int i = 0; i < modules.size(); i++) {
            if (expandedStatus.get(modules.get(i)))
                data[count++][0] = modules.get(i);
        }
        DraggableTableModel tableModel = new DraggableTableModel(data, columnNames, modules);
        ModuleTable moduleNameTable = new ModuleTable(tableModel, modules, expandedStatus, rowItems);
        moduleNameTable.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(moduleNameTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(new Color(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        moduleListPanel.removeAll();

        // Creating control buttons for modules
        for (Module module : modules) {
            if (!expandedStatus.get(module))
                continue;

            int top = c.gridy == 0 ? 5 : 0;

            ModuleEnabledButton enabledButton = new ModuleEnabledButton(module);
            enabledButton.setPreferredSize(new Dimension(26, 26));
            c.gridx = 0;
            c.insets = new Insets(top, 10, 0, 0);                        
            moduleListPanel.add(enabledButton, c);

            SeparatorButton separatorButton = null;
            ShowOutputButton showOutputButton = null;
            EvalButton evalButton = null;

            // If GUISeparator, add controls
            if (module instanceof GUISeparator) {
                separatorButton = new SeparatorButton(module, true);
                separatorButton.setPreferredSize(new Dimension(26, 26));
                c.gridx++;
                c.insets = new Insets(top, 0, 0, 0);
                moduleListPanel.add(separatorButton, c);
                
                separatorButton = new SeparatorButton(module, false);
                separatorButton.setPreferredSize(new Dimension(26, 26));
                c.gridx++;
                c.gridx++;
                c.insets = new Insets(top, 0, 0, 10);
                moduleListPanel.add(separatorButton, c);

            } else {
                showOutputButton = new ShowOutputButton(module);
                showOutputButton.setPreferredSize(new Dimension(26, 26));
                c.gridx++;
                c.insets = new Insets(top, 0, 0, 0);                
                moduleListPanel.add(showOutputButton, c);

                evalButton = new EvalButton(module);
                evalButton.setPreferredSize(new Dimension(26, 26));
                c.gridx++;
                c.gridx++;
                c.insets = new Insets(top, 0, 0, 10);
                moduleListPanel.add(evalButton, c);

            }

            rowItems.put(module, new RowItems(enabledButton, showOutputButton, evalButton, separatorButton));

            c.gridy++;

        }

        // Adding the draggable module list to the third column
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = modules.size();
        c.insets = new Insets(6, 1, 0, 0);
        moduleListPanel.add(moduleNameTable, c);

        c.gridwidth = 4;
        c.gridy = modules.size();
        c.weighty = Integer.MAX_VALUE;
        c.weightx = 1;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1, 1));
        moduleListPanel.add(separator, c);

        moduleListPanel.revalidate();
        moduleListPanel.repaint();

        revalidate();
        repaint();

    }

    /**
     * Provides a map detailing which modules are expanded (true) and those that are
     * collapsed (false)
     * 
     * @return
     */
    private HashMap<Module, Boolean> getExpandedModules(Modules modules) {
        HashMap<Module, Boolean> expandedStatus = new HashMap<>();
        boolean expanded = true;

        for (Module module : modules) {
            // If module is a GUI separator, update expanded status
            if (module instanceof GUISeparator) {
                expanded = module.getParameterValue(GUISeparator.EXPANDED_EDITING, null);
                expandedStatus.put(module, true); // GUISeparator is always expanded
            } else {
                expandedStatus.put(module, expanded);
            }
        }

        return expandedStatus;

    }

    public void updateStates() {
        for (Component component : moduleListPanel.getComponents()) {
            if (component.getClass() == ModuleEnabledButton.class) {
                ((ModuleEnabledButton) component).updateState();
            } else if (component.getClass() == ShowOutputButton.class) {
                ((ShowOutputButton) component).updateState();
            } else if (component.getClass() == ModuleButton.class) {
                ((ModuleButton) component).updateState();
            } else if (component.getClass() == EvalButton.class) {
                ((EvalButton) component).updateState();
            } else if (component.getClass() == ModuleTable.class) {
                ((ModuleTable) component).updateStates();
            }
        }
    }
}
