package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.common.Object.Metadata;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class FileListPanel extends JPanel {
    private final WorkspaceCollection workspaces;
    private final JTable table;

    public FileListPanel(WorkspaceCollection workspaces) {
        this.workspaces = workspaces;

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));

        String[] columnNames = new String[]{"Filename","Series name","Series #","Progress"};
        String[][] testData = new String[][]{{"dsfsf","gdfg","dgdgdfg","0.1"}};
        table = new JTable(testData,columnNames);
        table.setRowSelectionAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 20, 0);
        c.anchor = GridBagConstraints.NORTHWEST;

        add(scrollPane,c);

        validate();
        repaint();

        validate();
        repaint();

    }

    public void updatePanel() {
//        removeAll();
//
//        // Get list of available images
//        String[][] tableContents = getTableContents(workspaces);
//
//        // Creating table containing all file names
//        JTable table = new JTable(tableContents,columnNames);
//        scrollPane.add(table,c);
//
//        scrollPane.revalidate();
//        scrollPane.repaint();
//
//        revalidate();
//        repaint();

        //        DefaultTableModel model = (DefaultTableModel) table.getModel();

    }

    static String[][] getTableContents(WorkspaceCollection workspaces) {
        String[][] contents = new String[workspaces.size()][4];

        int i = 0;
        for (Workspace workspace:workspaces) {
            Metadata metadata = workspace.getMetadata();
            String filename = metadata.getFilename();
            String seriesName = metadata.getSeriesName();
            String seriesNumber = String.valueOf(metadata.getSeriesNumber());
            String progress = String.valueOf(workspace.getProgress());

            contents[i++] = new String[]{filename,seriesName,seriesNumber,progress};

        }

        return contents;

    }
}





//package wbif.sjx.MIA.GUI.Panels;
//
//        import wbif.sjx.MIA.GUI.GUI;
//        import wbif.sjx.MIA.Object.Workspace;
//        import wbif.sjx.MIA.Object.WorkspaceCollection;
//        import wbif.sjx.common.Object.Metadata;
//
//        import javax.swing.*;
//        import javax.swing.border.EtchedBorder;
//        import java.awt.*;
//        import java.io.File;
//
//public class FileListPanel extends JScrollPane {
//    private JPanel panel;
//    private final WorkspaceCollection workspaces;
//    private final JTable table;
//
//    public FileListPanel(WorkspaceCollection workspaces) {
//        this.workspaces = workspaces;
//
//        panel = new JPanel();
//        setViewportView(panel);
//
//        String[] columnNames = new String[]{"Filename", "Series name", "Series #", "Progress"};
//        String[][] testData = new String[][]{{"dsfsf", "gdfg", "dgdgdfg", "0.1"}};
//        table = new JTable(testData, columnNames);
//        table.setRowSelectionAllowed(false);
//
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
//
//        panel.add(table);
//
//        int frameWidth = GUI.getMinimumFrameWidth();
//        int bigButtonSize = GUI.getBigButtonSize();
//
//        // Initialising the scroll panel
//        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
//        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        getVerticalScrollBar().setUnitIncrement(10);
//        setPreferredSize(new Dimension(frameWidth - 45 - bigButtonSize, bigButtonSize + 15));
//        setMinimumSize(new Dimension(frameWidth - 45 - bigButtonSize, bigButtonSize + 15));
//
//        panel.setLayout(new GridBagLayout());
//        panel.validate();
//        panel.repaint();
//
//        validate();
//        repaint();
//
//    }
//
//    public void updatePanel() {
////        removeAll();
////
////        // Get list of available images
////        String[][] tableContents = getTableContents(workspaces);
////
////        // Creating table containing all file names
////        JTable table = new JTable(tableContents,columnNames);
////        scrollPane.add(table,c);
////
////        scrollPane.revalidate();
////        scrollPane.repaint();
////
////        revalidate();
////        repaint();
//
//        //        DefaultTableModel model = (DefaultTableModel) table.getModel();
//
//    }
//
//    static String[][] getTableContents(WorkspaceCollection workspaces) {
//        String[][] contents = new String[workspaces.size()][4];
//
//        int i = 0;
//        for (Workspace workspace : workspaces) {
//            Metadata metadata = workspace.getMetadata();
//            String filename = metadata.getFilename();
//            String seriesName = metadata.getSeriesName();
//            String seriesNumber = String.valueOf(metadata.getSeriesNumber());
//            String progress = String.valueOf(workspace.getProgress());
//
//            contents[i++] = new String[]{filename, seriesName, seriesNumber, progress};
//
//        }
//
//        return contents;
//
//    }
//}
