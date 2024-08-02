package io.github.mianalysis.mia.gui.regions.extrapanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import io.github.mianalysis.mia.MIA;
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

    public enum Tab {
        FILES, HELP, NOTES, SEARCH;
    }

    public ExtraPanel() {
        setLayout(new GridBagLayout());

        tabbedPane.setOpaque(false);
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

    public FileListPanel getFileListPanel() {
        return fileListPanel;
    }

    public HelpPanel getHelpPanel() {
        return helpPanel;
    }

    public NotesPanel getNotesPanel() {
        return notesPanel;
    }
}
