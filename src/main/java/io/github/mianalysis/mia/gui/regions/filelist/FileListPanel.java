package io.github.mianalysis.mia.gui.regions.filelist;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.ClosePanelButton;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.Workspaces;
import io.github.mianalysis.mia.object.system.Colours;
import io.github.mianalysis.mia.object.system.Preferences;
import io.github.mianalysis.mia.object.system.Status;
import io.github.sjcross.sjcommon.metadataextractors.Metadata;

public class FileListPanel extends JPanel implements MouseListener, TableCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = -2538934848503043479L;
    private final Workspaces workspaces;
    private final JTable table;
    private final DefaultTableModel model = new DefaultTableModel();
    private final FileListColumnSelectorMenu columnSelectorMenu = new FileListColumnSelectorMenu(this);
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

    private final static int minimumWidth = 200;
    private final static int preferredWidth = 300;

    public static final int COL_JOB_ID = 0;
    public static final int COL_WORKSPACE = 1;
    public static final int COL_SERIESNAME = 2;
    public static final int COL_SERIESNUMBER = 3;
    public static final int COL_PROGRESS = 4;

    HashMap<Integer, TableColumn> columns = new HashMap<>();

    private int maxJob = 0;

    public FileListPanel(Workspaces workspaces) {
        this.workspaces = workspaces;

        // Initialising the scroll panel
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setMinimumSize(new Dimension(minimumWidth, 1));
        setPreferredSize(new Dimension(preferredWidth, 1));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Adding title to help window
        JLabel fileListLabel = new JLabel();
        fileListLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        fileListLabel.setText("File list");
        add(fileListLabel, c);

        // Adding close button
        ClosePanelButton closeButton = new ClosePanelButton(this);
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        c.gridx++;
        add(closeButton, c);

        // Adding separator
        JSeparator separator = new JSeparator();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        add(separator, c);

        model.setColumnCount(5);
        model.setColumnIdentifiers(new String[] { "#", "Filename", "Ser. name", "Ser. #", "Progress" });

        table = new JTable(model);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().addMouseListener(this);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDragEnabled(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setAutoCreateRowSorter(true);
        table.setBackground(null);
        table.setDefaultEditor(Object.class, null);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(COL_JOB_ID).setCellRenderer(this);
        columnModel.getColumn(COL_WORKSPACE).setCellRenderer(this);
        columnModel.getColumn(COL_SERIESNAME).setCellRenderer(this);
        columnModel.getColumn(COL_SERIESNUMBER).setCellRenderer(this);
        columnModel.getColumn(COL_PROGRESS).setCellRenderer(this);

        columns.put(COL_JOB_ID, columnModel.getColumn(COL_JOB_ID));
        columns.put(COL_WORKSPACE, columnModel.getColumn(COL_WORKSPACE));
        columns.put(COL_SERIESNAME, columnModel.getColumn(COL_SERIESNAME));
        columns.put(COL_SERIESNUMBER, columnModel.getColumn(COL_SERIESNUMBER));
        columns.put(COL_PROGRESS, columnModel.getColumn(COL_PROGRESS));

        columnModel.getColumn(COL_JOB_ID).setPreferredWidth(10);

        showColumn(COL_SERIESNAME, false);
        showColumn(COL_SERIESNUMBER, false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;

        add(scrollPane, c);

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
                    model.setValueAt(workspace.getProgress(), row, COL_PROGRESS);
                }
            }
        }

        // Iterating over all current Workspaces, adding any that are missing
        for (Workspace workspace : workspaces) {
            if (!currentWorkspaces.contains(workspace)) {
                JobNumber jobNumber = new JobNumber(++maxJob);
                Metadata metadata = workspace.getMetadata();
                String seriesName = metadata.getSeriesName();
                String seriesNumber = String.valueOf(metadata.getSeriesNumber());
                double progress = workspace.getProgress();

                model.addRow(new Object[] { jobNumber, workspace, seriesName, seriesNumber, progress });

            }
        }

        table.repaint();
        table.validate();

    }

    public boolean showColumn(int columnIndex, boolean show) {
        TableColumn column = columns.get(columnIndex);

        if (show) {
            table.addColumn(column);
        } else {
            if (table.getColumnCount() > 1) {
                table.removeColumn(column);
            } else {
                MIA.log.writeWarning("File list must have at least one column");
                return false;
            }
        }

        table.repaint();
        table.validate();
        table.doLayout();

        return true;

    }

    public void resetJobNumbers() {
        maxJob = 0;
    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public static int getPreferredWidth() {
        return preferredWidth;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        switch (table.getColumnName(column)) {
            case "#":
                JLabel label = new JLabel();
                JobNumber jobNumber = (JobNumber) value;
                label.setText(String.valueOf(jobNumber.getJobNumber()));
                label.setToolTipText(String.valueOf(jobNumber.getJobNumber()));
                return label;

            case "Filename":
                Metadata metadata = ((Workspace) value).getMetadata();
                label = new JLabel();
                label.setText(" " + metadata.getFilename());
                label.setToolTipText(metadata.getFile().getAbsolutePath());
                return label;

            case "Ser. name":
            case "Ser. #":
                label = new JLabel();
                label.setText((String) value);
                label.setToolTipText((String) value);
                return label;

            case "Progress":
                int progress = (int) Math.round(((double) value) * 100);
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setValue(progress);
                progressBar.setBorderPainted(false);
                progressBar.setStringPainted(true);
                progressBar.setString("");
                progressBar.setToolTipText(String.valueOf((double) value));

                Preferences preferences = MIA.getPreferences();
                boolean darkMode = preferences == null ? false : preferences.darkThemeEnabled();

                // Set a special colour if the analysis is marked as having failed
                Status status = ((Workspace) model.getValueAt(row, COL_WORKSPACE)).getStatus();
                switch (status) {
                    case PASS:
                    case REDIRECT:
                        if (progress == 100)
                            progressBar.setForeground(Colours.getGreen(darkMode));
                        else
                            progressBar.setForeground(Colours.getBlue(darkMode));
                        break;
                    case FAIL:
                        progressBar.setForeground(Colours.getRed(darkMode));
                        break;
                    case TERMINATE:
                    case TERMINATE_SILENT:
                        progressBar.setForeground(Colours.getOrange(darkMode));
                        break;
                }

                return progressBar;
        }

        return null;

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Only display menu if the right mouse button is clicked
        if (e.getButton() != MouseEvent.BUTTON3)
            return;

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

    class JobNumber {
        private final DecimalFormat df = new DecimalFormat("00000000000");
        private final int jobNumber;

        public JobNumber(int jobNumber) {
            this.jobNumber = jobNumber;
        }

        public int getJobNumber() {
            return jobNumber;
        }

        @Override
        public String toString() {
            return df.format(jobNumber);
        }
    }
}