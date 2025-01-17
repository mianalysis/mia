package io.github.mianalysis.mia.gui.regions.extrapanels.filelist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.gui.GUI;

public class FileListColumnSelectorMenu extends JPopupMenu implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -3296812915065763133L;
    private final FileListPanel panel;
    private final JCheckBoxMenuItem showJobID;
    private final JCheckBoxMenuItem showFilename;
    private final JCheckBoxMenuItem showSeriesname;
    private final JCheckBoxMenuItem showSeriesnumber;
    private final JCheckBoxMenuItem showProgress;


    public FileListColumnSelectorMenu(FileListPanel panel) {
        this.panel = panel;

        showJobID = new JCheckBoxMenuItem();
        showJobID.setText("Show job ID");
        showJobID.setSelected(true);
        showJobID.setFont(GUI.getDefaultFont().deriveFont(14f));
        showJobID.addActionListener(this);
        add(showJobID);

        showFilename = new JCheckBoxMenuItem();
        showFilename.setText("Show filename");
        showFilename.setSelected(true);
        showFilename.setFont(GUI.getDefaultFont().deriveFont(14f));
        showFilename.addActionListener(this);
        add(showFilename);

        showSeriesname = new JCheckBoxMenuItem();
        showSeriesname.setText("Show series name");
        showSeriesname.setSelected(false);
        showSeriesname.setFont(GUI.getDefaultFont().deriveFont(14f));
        showSeriesname.addActionListener(this);
        add(showSeriesname);

        showSeriesnumber = new JCheckBoxMenuItem();
        showSeriesnumber.setText("Show series number");
        showSeriesnumber.setSelected(false);
        showSeriesnumber.setFont(GUI.getDefaultFont().deriveFont(14f));
        showSeriesnumber.addActionListener(this);
        add(showSeriesnumber);

        showProgress = new JCheckBoxMenuItem();
        showProgress.setText("Show progress");
        showProgress.setSelected(true);
        showProgress.setFont(GUI.getDefaultFont().deriveFont(14f));
        showProgress.addActionListener(this);
        add(showProgress);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Show job ID":
                boolean state = panel.showColumn(FileListPanel.COL_JOB_ID, showJobID.isSelected());
                if (!state)
                    showJobID.setSelected(!showJobID.isSelected());
                break;
            case "Show filename":
                state = panel.showColumn(FileListPanel.COL_FILENAME, showFilename.isSelected());
                if (!state)
                    showFilename.setSelected(!showFilename.isSelected());
                break;
            case "Show series name":
                state = panel.showColumn(FileListPanel.COL_SERIESNAME, showSeriesname.isSelected());
                if (!state)
                    showSeriesname.setSelected(!showSeriesname.isSelected());
                break;
            case "Show series number":
                state = panel.showColumn(FileListPanel.COL_SERIESNUMBER, showSeriesnumber.isSelected());
                if (!state)
                    showSeriesnumber.setSelected(!showSeriesnumber.isSelected());
                break;
            case "Show progress":
                state = panel.showColumn(FileListPanel.COL_PROGRESS, showProgress.isSelected());
                if (!state)
                    showProgress.setSelected(!showProgress.isSelected());
                break;
        }
    }
}
