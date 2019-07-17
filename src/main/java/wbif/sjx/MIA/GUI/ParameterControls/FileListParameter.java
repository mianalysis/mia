package wbif.sjx.MIA.GUI.ParameterControls;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Parameters.FileListP;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class FileListParameter extends ParameterControl {
    protected FileListP parameter;
    private JPanel control;
    private JTable table = new JTable();

    public FileListParameter(FileListP parameter) {
        this.parameter = parameter;

        control = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.addColumn("Filename");
        tableModel.addColumn("Series number");
        tableModel.addColumn("Series name");

        JScrollPane scrollPane = new JScrollPane(table);

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, 200));
        scrollPane.setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, 200));

        control.add(scrollPane,c);

    }

    @Override
    public JComponent getComponent() {
        return control;
    }

    @Override
    public void updateControl() {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        // Removing all rows
        while (tableModel.getRowCount() > 0) tableModel.removeRow(0);

        // Adding current files
        tableModel.addRow(new String[]{"Testname","Sername1","4"});
        tableModel.addRow(new String[]{"T2","Series 2","6"});
        tableModel.addRow(new String[]{"TN 3","Final series","9"});

    }
}
