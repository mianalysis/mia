package io.github.mianalysis.mia.gui.regions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.gui.regions.extrapanels.ExtraPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.filelist.FileListPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.help.HelpPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.notes.NotesPanel;
import io.github.mianalysis.mia.gui.regions.extrapanels.search.SearchPanel;
import io.github.mianalysis.mia.object.system.SwingPreferences;

public class ClosePanelButton extends JButton implements ActionListener {
    private final JPanel panel;

    public ClosePanelButton(JPanel panel) {
        this.panel = panel;

        boolean isDark = ((SwingPreferences) MIA.getPreferences()).darkThemeEnabled();

        if (isDark)
            setIcon(new ImageIcon(
                    ClosePanelButton.class.getResource("/icons/close_window_darkgreyDM_12px.png"), ""));
        else
            setIcon(new ImageIcon(
                    ClosePanelButton.class.getResource("/icons/close_window_darkgrey_12px.png"), ""));

        setPreferredSize(new Dimension(26, 26));
        setMinimumSize(new Dimension(26, 26));
        setMaximumSize(new Dimension(26, 26));
        setToolTipText("Close panel");
        addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (panel instanceof ExtraPanel)
            GUI.setShowSidebar(false);
    }
}
