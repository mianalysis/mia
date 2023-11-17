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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.GUIAnalysisHandler;
import io.github.mianalysis.mia.gui.regions.RenameListMenu;
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

    public ModuleTable(TableModel tableModel, Modules modules, HashMap<Module, Boolean> expandedStatus) {
        super(tableModel);

        this.modules = modules;

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
                GUI.updateParameters();
                GUI.updateHelpNotes();

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
        setRowHeight(26);
        setFillsViewportHeight(true);
        setShowGrid(false);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

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

        GUI.updateModules();
        GUI.updateParameters();

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        if (value instanceof Module) {
            Module module = (Module) value;
            moduleNames.putIfAbsent(module, new ModuleName(module, table, isSelected));
            ModuleName moduleName = moduleNames.get(module);
            moduleName.setSelected(isSelected);

            return moduleName;

        } else if (value instanceof String) {
            JLabel label = new JLabel();

            label.setBorder(new EmptyBorder(2, 5, 0, 0));
            label.setOpaque(true);

            boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

            if (isSelected)
                label.setBackground(Colours.getLightBlue(isDark));
            else
                label.setBackground(table.getBackground());

            GUI.getModules().get(row).setNickname(((String) value).trim());
            GUI.updateModules();
            GUI.updateParameters();

            return label;

        }

        else
            return null;

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                Module module = (Module) getValueAt(rowAtPoint(e.getPoint()), 0);
                RenameListMenu renameListMenu = new RenameListMenu(module);
                renameListMenu.show(GUI.getFrame(), 0, 0);
                renameListMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
                renameListMenu.setVisible(true);

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
