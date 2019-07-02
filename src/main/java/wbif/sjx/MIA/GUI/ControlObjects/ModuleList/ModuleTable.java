package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ModuleTable extends JTable implements ActionListener, TableCellRenderer {
    private ModuleCollection modules;

    public ModuleTable(TableModel tableModel, ModuleCollection modules, HashMap<Module,Boolean> expandedStatus) {
        super(tableModel);

        this.modules = modules;

        ListSelectionModel listSelectionModel = getSelectionModel();
        listSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] rows = getSelectedRows();
                Module[] selectedModules = new Module[rows.length];
                for (int i=0;i<rows.length;i++) {
                    selectedModules[i] = (Module) getValueAt(rows[i],0);
                }

                GUI.setSelectedModules(selectedModules);
                GUI.updateParameters();
                GUI.updateHelpNotes();

            }
        });

        setDefaultEditor(Object.class,null);
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

        KeyStroke backspace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0);
        registerKeyboardAction(this,"Backspace",backspace,JComponent.WHEN_FOCUSED);

        KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0);
        registerKeyboardAction(this,"Delete",delete,JComponent.WHEN_FOCUSED);

        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK,false);
        registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);

        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_MASK,false);
        registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);

        // Adding selection(s)
        Module[] selectedModules = GUI.getSelectedModules();
        clearSelection();
        if (selectedModules != null) {
            for (Module selectedModule:selectedModules) {
                // Getting index in table
                for (int row=0;row<getRowCount();row++) {
                    if (getValueAt(row,0) == selectedModule) {
                        addRowSelectionInterval(row,row);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Backspace":
            case "Delete":
                GUI.addUndo();
                for (int row:getSelectedRows()) {
                    Module module = (Module) getValueAt(row, 0);
                    modules.remove(module);
                }
                break;
            case "Copy":
                try {
                    int[] selectedRows = getSelectedRows();
                    if (selectedRows.length == 0) return;

                    ModuleCollection copyModules = new ModuleCollection();
                    for (int i=0;i<selectedRows.length;i++) {
                        copyModules.add(((Module) getValueAt(selectedRows[i], 0)).duplicate());
                    }

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    ModuleCollectionTransfer transfer = new ModuleCollectionTransfer(copyModules);
                    clipboard.setContents(transfer,null);

                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }

                break;
            case "Paste":
                try {
                    GUI.addUndo();
                    int[] selectedRows = getSelectedRows();
                    if (selectedRows.length == 0) return;
                    int toIdx = selectedRows[selectedRows.length-1];

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    DataFlavor dataFlavor = new ModuleCollectionDataFlavor();
                    ModuleCollection copyModules = (ModuleCollection) clipboard.getData(dataFlavor);

                    modules.insert(copyModules.duplicate(),toIdx);

                } catch (ClassNotFoundException | IOException | UnsupportedFlavorException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }

                break;
        }

        GUI.updateModules();

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = new JLabel();

        Border margin = new EmptyBorder(2,5,0,0);
        label.setBorder(margin);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setOpaque(true);

        if (isSelected) label.setBackground(new Color(145,201,247));
        else label.setBackground(table.getBackground());

        if (value instanceof Module) {
            Module module = (Module) value;
            if (module instanceof GUISeparator) label.setForeground(Color.BLUE);
            else setForeground(Color.BLACK);
            label.setText(module.getNickname());

        } else if (value instanceof String) {
            GUI.getModules().get(row).setNickname(((String) value).trim());
            GUI.updateModules();
            GUI.updateParameters();
        }

        return label;
    }
}
