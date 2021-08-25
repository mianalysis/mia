package wbif.sjx.MIA.GUI.ControlObjects;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Panels.FileListPanel;
import wbif.sjx.MIA.GUI.Panels.HelpPanel;
import wbif.sjx.MIA.GUI.Panels.NotesPanel;

public class ClosePanelButton extends JButton implements ActionListener {
    private final JPanel panel;

    public ClosePanelButton(JPanel panel) {
        this.panel = panel;

        final ImageIcon blackIcon = new ImageIcon(
                ClosePanelButton.class.getResource("/Icons/close_window_black_12px.png"), "");
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
    }
}
