package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.GUI.ControlObjects.FileListColumnSelectorMenu;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.common.Object.Metadata;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.HashSet;

public class FileListPanel extends JPanel implements MouseListener, TableCellRenderer {
    private final WorkspaceCollection workspaces;
    private final JTable table;
    private final HashMap<Workspace,Integer> rows = new HashMap<>();
    private final DefaultTableModel model = new DefaultTableModel();
    private final FileListColumnSelectorMenu columnSelectorMenu = new FileListColumnSelectorMenu(this);
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

    private final static int minimumWidth = 200;
    private final static int preferredWidth = 300;

    private final int maxWidth;
    private final int minWidth;
    private final int prefWidth;

    public static final int COL_WORKSPACE = 0;
    public static final int COL_SERIESNAME = 1;
    public static final int COL_SERIESNUMBER = 2;
    public static final int COL_PROGRESS = 3;

    public FileListPanel(WorkspaceCollection workspaces) {
        this.workspaces = workspaces;

        // Initialising the scroll panel
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(minimumWidth,1));
        setPreferredSize(new Dimension(preferredWidth,1));

        model.setColumnCount(4);
        model.setColumnIdentifiers(new String[]{"Filename","Ser. name","Ser. #","Progress"});

        table = new JTable(model);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().addMouseListener(this);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDragEnabled(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setAutoCreateRowSorter(true);
        table.setBackground(null);
        table.setDefaultEditor(Object.class,null);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(COL_WORKSPACE).setCellRenderer(this);
        columnModel.getColumn(COL_SERIESNAME).setCellRenderer(this);
        columnModel.getColumn(COL_SERIESNUMBER).setCellRenderer(this);
        columnModel.getColumn(COL_PROGRESS).setCellRenderer(this);

        showColumn(COL_SERIESNAME,false);
        showColumn(COL_SERIESNUMBER,false);

        maxWidth = columnModel.getColumn(0).getMaxWidth();
        minWidth = columnModel.getColumn(0).getMinWidth();
        prefWidth = columnModel.getColumn(0).getPreferredWidth();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        add(scrollPane,c);

        validate();
        repaint();

    }

    public synchronized void updatePanel() {
        // Iterate over all current rows, removing any that don't exist
        HashSet<Workspace> currentWorkspaces = new HashSet<>();
        if (model.getRowCount() > 0) {
            for (int row = model.getRowCount() - 1; row >= 0; row--) {
                Workspace workspace = (Workspace) model.getValueAt(row, COL_WORKSPACE);
                if (!workspaces.contains(workspace)) {
                    model.removeRow(row);
                } else {
                    currentWorkspaces.add(workspace);
                    model.setValueAt(workspace.getProgress(),row,COL_PROGRESS);
                }
            }
        }

        // Iterating over all current Workspaces, adding any that are missing
        for (Workspace workspace:workspaces) {
            Metadata metadata = workspace.getMetadata();
            String seriesName = metadata.getSeriesName();
            String seriesNumber = String.valueOf(metadata.getSeriesNumber());
            double progress = workspace.getProgress();

            if (!currentWorkspaces.contains(workspace)) model.addRow(new Object[]{workspace,seriesName,seriesNumber,progress});

        }

        table.repaint();
        table.validate();

    }

    public void showColumn(int columnIndex, boolean show) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);

        if (show) {
            column.setPreferredWidth(prefWidth);
            column.setMinWidth(minWidth);
            column.setMaxWidth(maxWidth);
        } else {
            column.setPreferredWidth(0);
            column.setMinWidth(0);
            column.setMaxWidth(0);
        }

        table.repaint();
        table.validate();
        table.doLayout();

    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public static int getPreferredWidth() {
        return preferredWidth;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        switch (column) {
            case COL_WORKSPACE:
                Metadata metadata = ((Workspace) value).getMetadata();
                JLabel label = new JLabel();
                label.setText(" "+metadata.getFilename());
                label.setToolTipText(metadata.getFile().getAbsolutePath());
                return label;

            case COL_SERIESNAME:
                label = new JLabel();
                label.setText((String) value);
                label.setToolTipText((String) value);
                return label;

            case COL_SERIESNUMBER:
                label = new JLabel();
                label.setText((String) value);
                label.setToolTipText((String) value);
                return label;

            case COL_PROGRESS:
                int progress = (int) Math.round(((double) value)*100);
                JProgressBar progressBar = new JProgressBar(0,100);
                progressBar.setValue(progress);
                progressBar.setBorderPainted(false);
                progressBar.setStringPainted(true);
                progressBar.setString("");
                progressBar.setToolTipText(String.valueOf((double) value));
                if (progress ==0) progressBar.setForeground(Colours.ORANGE);
                else if (progress == 100) progressBar.setForeground(Colours.GREEN);
                else progressBar.setForeground(Colours.BLUE);

                // Set a special colour if the analysis is marked as having failed
                if (((Workspace) model.getValueAt(row,COL_WORKSPACE)).isAnalysisFailed()) progressBar.setForeground(Colours.RED);

                return progressBar;
        }

        return null;

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Only display menu if the right mouse button is clicked
        if (e.getButton() != MouseEvent.BUTTON3) return;

        // Populating the list containing all available modules
        columnSelectorMenu.show(GUI.getFrame(), 0, 0);
        columnSelectorMenu.setLocation(MouseInfo.getPointerInfo().getLocation());
        columnSelectorMenu.setVisible(true);

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