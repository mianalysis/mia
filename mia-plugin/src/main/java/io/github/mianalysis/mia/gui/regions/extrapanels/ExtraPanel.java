package io.github.mianalysis.mia.gui.regions.extrapanels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.formdev.flatlaf.FlatClientProperties;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.extrapanels.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.help.HelpPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.notes.NotesPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.search.SearchPanel;

public class ExtraPanel extends JPanel {
    private JTabbedPane tabbedPane = new JTabbedPane();
    private FileListPanel fileListPanel = new FileListPanel(GUI.getAnalysisRunner().getWorkspaces());
    private SearchPanel searchPanel = new SearchPanel();
    private HelpPanel helpPanel = new HelpPanel();
    private NotesPanel notesPanel = new NotesPanel();
    private static final int minimumWidth = 200;
    private static final int preferredWidth = 300;

    public enum Tab {
        FILES, HELP, NOTES, SEARCH;
    }

    public ExtraPanel() {
        setLayout(new GridBagLayout());

        tabbedPane.putClientProperty(FlatClientProperties.STYLE,
                "background: #00000000; tabArc: 16; hoverColor: #86d0e6");

        // tabbedPane.setOpaque(false);
        tabbedPane.setFont(GUI.getDefaultFont().deriveFont(14f));
        tabbedPane.add("Files", fileListPanel);
        tabbedPane.add("Help", helpPanel);
        tabbedPane.add("Notes", notesPanel);
        tabbedPane.add("Search", searchPanel);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        add(tabbedPane, c);

    }

    public void setTab(Tab tab) {
        switch (tab) {
            case FILES:
                tabbedPane.setSelectedIndex(0);
                break;
            case HELP:
                tabbedPane.setSelectedIndex(1);
                break;
            case NOTES:
                tabbedPane.setSelectedIndex(2);
                break;
            case SEARCH:
                tabbedPane.setSelectedIndex(3);
                break;

        }
    }

    public void setShowSearch(boolean showSearch) {
        if (showSearch)
            tabbedPane.add("Search", searchPanel);
        else if (tabbedPane.indexOfTab("Search") != -1)
            tabbedPane.removeTabAt(tabbedPane.indexOfTab("Search"));
    }

    public FileListPanel getFileListPanel() {
        return fileListPanel;
    }

    public HelpPanel getHelpPanel() {
        return helpPanel;
    }

    public NotesPanel getNotesPanel() {
        return notesPanel;
    }

    public static int getMinimumWidth() {
        return minimumWidth;
    }

    public static int getPreferredWidth() {
        return preferredWidth;
    }
}
