package io.github.mianalysis.MIA.GUI.Regions.MenuBar;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import io.github.mianalysis.MIA.GUI.GUI;

public class MenuCheckbox extends JCheckBoxMenuItem implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 7792919077297685894L;
    public static final String TOGGLE_HELP_NOTES = "Show help and notes panel";
    public static final String TOGGLE_HELP = "Show help panel";
    public static final String TOGGLE_NOTES = "Show notes panel";
    public static final String TOGGLE_FILE_LIST = "Show file list";
    public static final String TOGGLE_SEARCH = "Show module search";

    public MenuCheckbox(String command) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case TOGGLE_HELP:
                    GUI.setShowHelp(isSelected());
                    GUI.updatePanel();
                    GUI.updateHelpNotes();
                    break;

                case TOGGLE_NOTES:
                    GUI.setShowNotes(isSelected());
                    GUI.updatePanel();
                    GUI.updateHelpNotes();
                    break;

                case TOGGLE_FILE_LIST:
                    GUI.setShowFileList(isSelected());
                    GUI.updatePanel();
                    GUI.updateFileList();
                    break;

                case TOGGLE_SEARCH:
                    GUI.setShowSearch(isSelected());
                    GUI.updatePanel();
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
