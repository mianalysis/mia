package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.ComponentFactory;
import wbif.sjx.MIA.GUI.ControlObjects.EvalButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleButton;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledButton;
import wbif.sjx.MIA.GUI.ControlObjects.ShowOutputButton;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.ModuleList.MyTableModel;
import wbif.sjx.MIA.GUI.ModuleList.MyTransferHandler;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;
import wbif.sjx.MIA.Object.Parameters.BooleanP;
import wbif.sjx.MIA.Process.AnalysisHandling.Analysis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
        ComponentFactory componentFactory = GUI.getComponentFactory();
        Module activeModule = GUI.getActiveModule();
        int moduleButtonWidth = GUI.getModuleButtonWidth();

        panel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
//        c.anchor = GridBagConstraints.FIRST_LINE_START;

        ModuleCollection modules = GUI.getModules();
        JTable moduleNameTable = getModuleNameTable(modules);
        HashMap<Module,Boolean> expandedStatus = getExpandedModules(modules);

        // Creating control buttons for modules
        for (Module module:modules) {
//            if (!expandedStatus.get(module))
            c.gridx = 0;

            ModuleEnabledButton enabledButton = new ModuleEnabledButton(module);
            enabledButton.setPreferredSize(new Dimension(26,26));
            panel.add(enabledButton,c);
            c.gridx++;

            ShowOutputButton showOutputButton = new ShowOutputButton(module);
            showOutputButton.setPreferredSize(new Dimension(26,26));
            panel.add(showOutputButton,c);
            c.gridx++;
            c.gridx++;

            EvalButton evalButton = new EvalButton(module);
            evalButton.setPreferredSize(new Dimension(26,26));
            panel.add(evalButton,c);

            c.gridy++;

        }

        // Adding the draggable module list to the third column
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = modules.size();
        panel.add(moduleNameTable,c);




//        boolean expanded = true;
//        // Adding module buttons
//        ModuleCollection modules = analysis.getModules();
//        c.insets = new Insets(2,0,0,0);
//        for (int i=0;i<modules.size();i++) {
//            Module module = modules.get(i);
//            int idx = modules.indexOf(module);
//            if (idx == modules.size() - 1) c.weighty = 1;
//
//            JPanel modulePanel = null;
//            if (module instanceof GUISeparator) {
//                expanded = ((BooleanP) module.getParameter(GUISeparator.EXPANDED_EDITING)).isSelected();
//                modulePanel = componentFactory.createEditingSeparator(module, buttonGroup, activeModule, moduleButtonWidth - 25);
//            } else {
//                if (!expanded) continue;
//                modulePanel = componentFactory.createAdvancedModuleControl(module, buttonGroup, activeModule, moduleButtonWidth - 25);
//            }
//
//            // If this is the final module, addRef a gap at the bottom
//            if (i==modules.size()-1) modulePanel.setBorder(new EmptyBorder(0,0,5,0));
//
//            panel.add(modulePanel, c);
//            c.insets = new Insets(0,0,0,0);
//            c.gridy++;
//
//        }

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
            if (module instanceof GUISeparator) expanded = module.getParameterValue(GUISeparator.EXPANDED_EDITING);

            expandedStatus.put(module,expanded);

        }

        return expandedStatus;

    }

    private JTable getModuleNameTable(ModuleCollection modules) {
        String[] columnNames = {"Title"};
        Object[][] data = new Object[modules.size()][1];
        for (int i=0;i<modules.size();i++) data[i][0] = modules.get(i);

        MyTableModel tableModel = new MyTableModel(data, columnNames,modules);
        JTable table = new JTable(tableModel);
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.getSelectedRow();
                GUI.setActiveModule(modules.get(row));
                GUI.updateParameters();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        table.setTableHeader(null);
        table.setOpaque(false);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new MyTransferHandler(table));
        table.getColumn("Title").setPreferredWidth(200);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);

        return table;

    }

    public void updateButtonStates() {
        updatePanel();
//        for (Component panel : panel.getComponents()) {
//            if (panel.getClass() == JPanel.class) {
//                for (Component component: ((JPanel) panel).getComponents()) {
//                    if (component.getClass() == ModuleEnabledButton.class) {
//                        ((ModuleEnabledButton) component).updateState();
//                    } else if (component.getClass() == ShowOutputButton.class) {
//                        ((ShowOutputButton) component).updateState();
//                    } else if (component.getClass() == ModuleButton.class) {
//                        ((ModuleButton) component).updateState();
//                    } else if (component.getClass() == EvalButton.class) {
//                        ((EvalButton) component).updateState();
//                    }
//                }
//            }
//        }
    }
}
