package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ControlObjects.ModuleButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.ModuleEnabledButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.DraggableTableModel;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.EvalButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.ModuleTable;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.ShowOutputButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleList.SeparatorButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.HashMap;

public class DraggableModulesPanel extends JScrollPane {
    private JPanel panel;
    private ButtonGroup buttonGroup;

    public DraggableModulesPanel(ButtonGroup buttonGroup) {
        this.buttonGroup = buttonGroup;

        panel = new JPanel();

        setViewportView(panel);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, -1));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, -1));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getVerticalScrollBar().setUnitIncrement(10);

        // Initialising the panel for module buttons
        panel.setLayout(new GridBagLayout());
        panel.validate();
        panel.repaint();

        validate();
        repaint();

    }

    public void updatePanel() {
        Analysis analysis = GUI.getAnalysis();
        int moduleButtonWidth = GUI.getModuleButtonWidth();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        ModuleCollection modules = GUI.getModules();
        HashMap<Module,Boolean> expandedStatus = getExpandedModules(modules);
        // Get number of visible modules
        int expandedCount = (int) expandedStatus.values().stream().filter(p -> p).count();

        // Adding content
        String[] columnNames = {"Title"};
        Object[][] data = new Object[expandedCount][1];

        int count = 0;
        for (int i=0;i<modules.size();i++) {
            if (expandedStatus.get(modules.get(i))) data[count++][0] = modules.get(i);
        }
        DraggableTableModel tableModel = new DraggableTableModel(data, columnNames,modules);
        JTable moduleNameTable = new ModuleTable(tableModel, modules,expandedStatus);

        JScrollPane scrollPane = new JScrollPane(moduleNameTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(new Color(0, 0, 0, 0));

        // Creating control buttons for modules
        for (Module module:modules) {
            if (!expandedStatus.get(module)) continue;

            int top = c.gridy == 0 ? 5 : 0;
            c.gridx = 0;
            c.insets = new Insets(top, 5, 0, 0);

            ModuleEnabledButton enabledButton = new ModuleEnabledButton(module);
            enabledButton.setPreferredSize(new Dimension(26,26));
            panel.add(enabledButton,c);
            c.gridx++;
            c.insets = new Insets(top, 0, 0, 0);

            // If GUISeparator, add controls
            if (module instanceof GUISeparator) {
                SeparatorButton separatorButton = new SeparatorButton(module,true);
                separatorButton.setPreferredSize(new Dimension(26, 26));
                panel.add(separatorButton, c);
                c.gridx++;
                c.gridx++;

                separatorButton = new SeparatorButton(module,false);
                separatorButton.setPreferredSize(new Dimension(26, 26));
                panel.add(separatorButton, c);

            } else {
                ShowOutputButton showOutputButton = new ShowOutputButton(module);
                showOutputButton.setPreferredSize(new Dimension(26, 26));
                panel.add(showOutputButton, c);
                c.gridx++;
                c.gridx++;

                EvalButton evalButton = new EvalButton(module);
                evalButton.setPreferredSize(new Dimension(26, 26));
                panel.add(evalButton, c);

            }

            c.gridy++;

        }

        // Adding the draggable module list to the third column
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = modules.size();
        c.insets = new Insets(5,0,0,0);
        panel.add(moduleNameTable,c);

        c.gridwidth = 4;
        c.gridy = modules.size();
        c.weighty = Integer.MAX_VALUE;
        c.weightx = 1;
        c.fill = GridBagConstraints.VERTICAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(-1,1));
        panel.add(separator, c);

        panel.revalidate();
        panel.repaint();

        revalidate();
        repaint();

    }

    /**
     * Provides a map detailing which modules are expanded (true) and those that are collapsed (false)
     * @return
     */
    private HashMap<Module,Boolean> getExpandedModules(ModuleCollection modules) {
        HashMap<Module,Boolean> expandedStatus = new HashMap<>();
        boolean expanded = true;

        for (Module module:modules) {
            // If module is a GUI separator, update expanded status
            if (module instanceof GUISeparator) {
                expanded = module.getParameterValue(GUISeparator.EXPANDED_EDITING);
                expandedStatus.put(module,true); // GUISeparator is always expanded
            } else {
                expandedStatus.put(module,expanded);
            }
        }

        return expandedStatus;

    }

    public void updateButtonStates() {
        for (Component component : panel.getComponents()) {
            if (component.getClass() == ModuleEnabledButton.class) {
                ((ModuleEnabledButton) component).updateState();
            } else if (component.getClass() == ShowOutputButton.class) {
                ((ShowOutputButton) component).updateState();
            } else if (component.getClass() == ModuleButton.class) {
                ((ModuleButton) component).updateState();
            } else if (component.getClass() == EvalButton.class) {
                ((EvalButton) component).updateState();
            }
        }
    }
}
