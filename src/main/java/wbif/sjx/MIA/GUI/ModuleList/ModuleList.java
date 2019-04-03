package wbif.sjx.MIA.GUI.ModuleList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ModuleList {
    public static void main(String[] args) {
        new ModuleList().test();

    }

    public void test() {
        JFrame frame = new JFrame();

        String[] columnNames = {"First Name", "Last Name", ""};
        Object[][] data = {{"Homer", "Simpson", "delete Homer"},
                {"Madge", "Simpson", "delete Madge"},
                {"Bart",  "Simpson", "delete Bart"},
                {"Lisa",  "Simpson", "delete Lisa"}};

        MyTableModel tableModel = new MyTableModel(data,columnNames);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new MyTransferHandler(table));


        tableModel.insertRow(2,new String[]{"Maggie", "Simpson", "delete Maggie"});

        Action delete = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                ((DefaultTableModel)table.getModel()).removeRow(modelRow);
            }
        };
        ButtonColumn buttonColumn = new ButtonColumn(table, delete, 2);

        table.setFillsViewportHeight(true);
        frame.add(scrollPane);
        frame.setPreferredSize(new Dimension(300,200));
        frame.pack();
        frame.setVisible(true);

        table.repaint();

    }

    class Blob {
        String name;
        public Blob(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
