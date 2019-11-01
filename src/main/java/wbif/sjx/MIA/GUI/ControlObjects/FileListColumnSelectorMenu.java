package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.FileListPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileListColumnSelectorMenu extends JPopupMenu implements ActionListener {
    private final FileListPanel panel;
    private final JCheckBoxMenuItem showFilename;
    private final JCheckBoxMenuItem showSeriesname;
    private final JCheckBoxMenuItem showSeriesnumber;
    private final JCheckBoxMenuItem showProgress;


    public FileListColumnSelectorMenu(FileListPanel panel) {
        this.panel = panel;

        showFilename = new JCheckBoxMenuItem();
        showFilename.setText("Show filename");
        showFilename.setSelected(true);
        showFilename.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        showFilename.addActionListener(this);
        add(showFilename);

        showSeriesname = new JCheckBoxMenuItem();
        showSeriesname.setText("Show series name");
        showSeriesname.setSelected(false);
        showSeriesname.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        showSeriesname.addActionListener(this);
        add(showSeriesname);

        showSeriesnumber = new JCheckBoxMenuItem();
        showSeriesnumber.setText("Show series number");
        showSeriesnumber.setSelected(false);
        showSeriesnumber.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        showSeriesnumber.addActionListener(this);
        add(showSeriesnumber);

        showProgress = new JCheckBoxMenuItem();
        showProgress.setText("Show progress");
        showProgress.setSelected(true);
        showProgress.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        showProgress.addActionListener(this);
        add(showProgress);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Show filename":
                panel.showColumn(FileListPanel.COL_WORKSPACE,showFilename.isSelected());
                break;
            case "Show series name":
                panel.showColumn(FileListPanel.COL_SERIESNAME,showSeriesname.isSelected());
                break;
            case "Show series number":
                panel.showColumn(FileListPanel.COL_SERIESNUMBER,showSeriesnumber.isSelected());
                break;
            case "Show progress":
                panel.showColumn(FileListPanel.COL_PROGRESS,showProgress.isSelected());
                break;
        }
    }
}
