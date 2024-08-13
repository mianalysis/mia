package io.github.mianalysis.mia.gui.regions.workflowmodules;

import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;
import io.github.mianalysis.mia.gui.regions.ReferenceEditingMenu;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ModuleTable extends JTable implements ActionListener, MouseListener, TableCellRenderer {
    private static final String BACKSPACE = "backspace";
    private static final String COPY = "copy";
    private static final String PASTE = "paste";
    private static final String DELETE = "delete";
    private static final String OUTPUT = "output";
    private static final String ENABLE = "enable";

    private static final long serialVersionUID = 3722736203899254351L;
    private Modules modules;
    private HashMap<Module, ModuleName> moduleNames = new HashMap<>();
    private HashMap<Module, RowItems> rowItems;

    public ModuleTable(TableModel tableModel, Modules modules, HashMap<Module, Boolean> expandedStatus, HashMap<Module, RowItems> rowItems) {
        super(tableModel);

        this.modules = modules;
        this.rowItems = rowItems;

        ListSelectionModel listSelectionModel = getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] rows = getSelectedRows();
                Module[] selectedModules = new Module[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    selectedModules[i] = (Module) getValueAt(rows[i], 0);
                }

                GUI.setSelectedModules(selectedModules);
                GUI.updateParameters(true, selectedModules[0]);

            }
        });

        addMouseListener(this);
        setDefaultEditor(Object.class, null);
        getColumnModel().getColumn(0).setCellRenderer(this);
        setTableHeader(null);
        setDragEnabled(true);
        setDropMode(DropMode.INSERT_ROWS);
        setTransferHandler(new DraggableTransferHandler(this));
        getColumn("Title").setPreferredWidth(200);
        setRowHeight(28);
        setFillsViewportHeight(true);
        setShowGrid(false);
        setOpaque(false);
        // setBackground(new Color(0, 0, 0, 0));

        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        registerKeyboardAction(this, BACKSPACE, backspace, JComponent.WHEN_FOCUSED);

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        registerKeyboardAction(this, DELETE, delete, JComponent.WHEN_FOCUSED);

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false);
        registerKeyboardAction(this, COPY, copy, JComponent.WHEN_FOCUSED);

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, false);
        registerKeyboardAction(this, PASTE, paste, JComponent.WHEN_FOCUSED);

        KeyStroke output = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0);
        registerKeyboardAction(this, OUTPUT, output, JComponent.WHEN_FOCUSED);

        KeyStroke enable = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
        registerKeyboardAction(this, ENABLE, enable, JComponent.WHEN_FOCUSED);

        // Adding selection(s)
        Module[] selectedModules = GUI.getSelectedModules();
        clearSelection();
        if (selectedModules != null) {
            for (Module selectedModule : selectedModules) {
                // Getting index in table
                for (int row = 0; row < getRowCount(); row++) {
                    if (getValueAt(row, 0) == selectedModule) {
                        addRowSelectionInterval(row, row);
                        break;
                    }
                }
            }
        }
    }

    public void updateStates() {
        for (ModuleName moduleName : moduleNames.values())
            moduleName.updateState();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case BACKSPACE:
            case DELETE:
                GUIAnalysisHandler.removeModules();
                break;
            case COPY:
                GUIAnalysisHandler.copyModules();
                break;
            case PASTE:
                GUIAnalysisHandler.pasteModules();
                break;
            case OUTPUT:
                GUIAnalysisHandler.toggleOutput();
                break;
            case ENABLE:
                GUIAnalysisHandler.toggleEnableDisable();
                break;
        }

        GUI.updateModules(false, null);
        GUI.updateParameters(false, null);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        if (value instanceof Module) {
            Module module = (Module) value;
            moduleNames.putIfAbsent(module, new ModuleName(module, table, isSelected));
            ModuleName moduleName = moduleNames.get(module);
            moduleName.setSelected(isSelected);

            if (isSelected) {
                boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();
                moduleName.setOpaque(true);
                moduleName.setBackground(Colours.getLightBlue(isDark));
                
                // RowItems rowItem = rowItems.get(module);
                // if (rowItem.getEvalButton() != null) {
                //     rowItem.getEvalButton().setOpaque(true);
                //     rowItem.getEvalButton().setBackground(Colours.getLightGrey(isDark));
                // }
                // if (rowItem.getSeparatorButton() != null) {
                //     rowItem.getSeparatorButton().setOpaque(true);
                //     rowItem.getSeparatorButton().setBackground(Colours.getLightGrey(isDark));
                // }
                // if (rowItem.getModuleEnabledButton() != null) {
                //     rowItem.getModuleEnabledButton().setOpaque(true);
                //     rowItem.getModuleEnabledButton().setBackground(Colours.getLightGrey(isDark));
                // }
                // if (rowItem.getShowOutputButton() != null) {
                //     rowItem.getShowOutputButton().setOpaque(true);
                //     rowItem.getShowOutputButton().setBackground(Colours.getLightGrey(isDark));
                // }
            } else {
                moduleName.setOpaque(false);
                moduleName.setBackground(new Color(0,0,0,0));

                // RowItems rowItem = rowItems.get(module);
                // if (rowItem.getEvalButton() != null) {
                //     rowItem.getEvalButton().setOpaque(false);
                //     rowItem.getEvalButton().setBackground(table.getBackground());
                // }
                // if (rowItem.getSeparatorButton() != null) {
                //     rowItem.getSeparatorButton().setOpaque(false);
                //     rowItem.getSeparatorButton().setBackground(table.getBackground());
                // }
                // if (rowItem.getModuleEnabledButton() != null) {
                //     rowItem.getModuleEnabledButton().setOpaque(false);
                //     rowItem.getModuleEnabledButton().setBackground(table.getBackground());
                // }
                // if (rowItem.getShowOutputButton() != null) {
                //     rowItem.getShowOutputButton().setOpaque(false);
                //     rowItem.getShowOutputButton().setBackground(table.getBackground());
                // }
            }

            return moduleName;

        }

        else
            return null;

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                Module module = (Module) getValueAt(rowAtPoint(e.getPoint()), 0);
                ReferenceEditingMenu refEditingMenu = new ReferenceEditingMenu(module);
                refEditingMenu.show(GUI.getFrame(), 0, 0);
                refEditingMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
                refEditingMenu.setVisible(true);

                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

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
}
