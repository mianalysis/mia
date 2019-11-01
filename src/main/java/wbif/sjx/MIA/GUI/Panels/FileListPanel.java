package wbif.sjx.MIA.GUI.Panels;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Object.Workspace;
import wbif.sjx.MIA.Object.WorkspaceCollection;
import wbif.sjx.common.Object.Metadata;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class FileListPanel extends JPanel implements TableCellRenderer {
    private final WorkspaceCollection workspaces;
    private final JTable table;
    private final HashMap<Workspace,Integer> rows = new HashMap<>();
    private DefaultTableModel model = new DefaultTableModel();

    private static final int COL_WORKSPACE = 0;
    private static final int COL_SERIESNAME = 1;
    private static final int COL_SERIESNUMBER = 2;
    private static final int COL_PROGRESS = 3;

    public FileListPanel(WorkspaceCollection workspaces) {
        this.workspaces = workspaces;

        int frameWidth = GUI.getMinimumFrameWidth();
        int bigButtonSize = GUI.getBigButtonSize();

        // Initialising the scroll panel
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setPreferredSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));
        setMinimumSize(new Dimension(frameWidth-45-bigButtonSize, bigButtonSize+15));

        model.setColumnCount(4);
        model.setColumnIdentifiers(new String[]{"Filename","Series name","Series #","Progress"});

        table = new JTable(model);
        table.setRowSelectionAllowed(false);
        table.getColumnModel().getColumn(COL_WORKSPACE).setCellRenderer(this);
        table.getColumnModel().getColumn(COL_PROGRESS).setCellRenderer(this);

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

    }

    public void updatePanel() {
        // Iterate over all current rows, removing any that don't exist
        HashSet<Workspace> currentWorkspaces = new HashSet<>();
        if (model.getRowCount() > 0) {
            for (int row = model.getRowCount() - 1; row >= 0; row--) {
                Workspace workspace = (Workspace) model.getValueAt(row, COL_WORKSPACE);
                if (!workspaces.contains(workspace)) {
                    model.removeRow(row);
                } else {
                    currentWorkspaces.add(workspace);
                    model.setValueAt(String.valueOf(workspace.getProgress()),row,COL_PROGRESS);
                }
            }
        }

        // Iterating over all current Workspaces, adding any that are missing
        for (Workspace workspace:workspaces) {
            Metadata metadata = workspace.getMetadata();
            String seriesName = metadata.getSeriesName();
            String seriesNumber = String.valueOf(metadata.getSeriesNumber());
            String progress = String.valueOf(workspace.getProgress());

            if (!currentWorkspaces.contains(workspace)) model.addRow(new Object[]{workspace,seriesName,seriesNumber,progress});

        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        switch (column) {
            case COL_WORKSPACE:
                JLabel label = new JLabel();
                label.setText(((Workspace) value).getMetadata().getFilename());
                return label;
            case COL_PROGRESS:
                return null;
        }

        return null;

    }
}