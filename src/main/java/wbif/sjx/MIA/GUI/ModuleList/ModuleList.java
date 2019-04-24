package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.Module.ImageProcessing.Pixel.FilterImage;
import wbif.sjx.MIA.Module.InputOutput.ImageLoader;
import wbif.sjx.MIA.Module.InputOutput.ImageSaver;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ModuleList {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new ModuleList().test();

    }

    public void test() {
        JFrame frame = new JFrame();

        Module mod1 = new ImageLoader<>();
        Module mod2 = new ImageSaver();
        Module mod3 = new FilterImage();

        String[] columnNames = {"Enable", "ShowOutput", "Title", "Evaluate"};
        Object[][] data = {{mod1,mod1,mod1,mod1},
                {mod2,mod2,mod2,mod2},
                {mod3,mod3,mod3,mod3}};

        MyTableModel tableModel = new MyTableModel(data,columnNames);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setTableHeader(null);
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new MyTransferHandler(table));
        table.getColumn("Enable").setPreferredWidth(20);
        table.getColumn("ShowOutput").setPreferredWidth(20);
        table.getColumn("Title").setPreferredWidth(200);
        table.getColumn("Evaluate").setPreferredWidth(20);
        table.setRowHeight(30);
        table.setShowGrid(false);

        Action enable = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);
                module.setEnabled(!module.isEnabled());

            }
        };
        EnableButtonColumn enableButtonColumn = new EnableButtonColumn(table, enable, 0);

        Action showOutput = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);
                module.setShowOutput(!module.canShowOutput());

            }
        };
        ShowOutputButtonColumn showOutputButtonColumn = new ShowOutputButtonColumn(table, showOutput, 1);

        Action evaluate = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable) e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                Module module = (Module) table.getModel().getValueAt(modelRow,0);
                module.setShowOutput(!module.canShowOutput());

            }
        };
        EvaluateButtonColumn evaluateButtonColumn = new EvaluateButtonColumn(table, evaluate, 3);

        table.setFillsViewportHeight(true);
        frame.add(scrollPane);
        frame.setPreferredSize(new Dimension(300,200));
        frame.pack();
        frame.setVisible(true);

        table.repaint();

    }
}
