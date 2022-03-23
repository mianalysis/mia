package io.github.mianalysis.mia.gui.regions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.helpandnotes.HelpPanel;
import io.github.mianalysis.mia.gui.regions.helpandnotes.NotesPanel;
import io.github.mianalysis.mia.gui.regions.search.SearchPanel;

public class ClosePanelButton extends JButton implements ActionListener {
    private final JPanel panel;

    public ClosePanelButton(JPanel panel) {
        this.panel = panel;

        final ImageIcon blackIcon = new ImageIcon(
                ClosePanelButton.class.getResource("/icons/close_window_darkgrey_12px.png"), "");
        setIcon(blackIcon);
        setPreferredSize(new Dimension(26, 26));
        setMinimumSize(new Dimension(26, 26));
        setMaximumSize(new Dimension(26, 26));
        setToolTipText("Close panel");
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (panel instanceof HelpPanel)
            GUI.setShowHelp(false);
        else if (panel instanceof NotesPanel)
            GUI.setShowNotes(false);
        else if (panel instanceof FileListPanel)
            GUI.setShowFileList(false);
        else if (panel instanceof SearchPanel)
            GUI.setShowSearch(false);
    }
}
