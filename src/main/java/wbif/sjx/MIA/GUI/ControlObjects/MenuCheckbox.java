package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuCheckbox extends JCheckBoxMenuItem implements ActionListener {
    public static final String TOGGLE_HELP_NOTES = "Show help and notes panel";
    public static final String TOGGLE_HELP = "Show help panel";
    public static final String TOGGLE_NOTES = "Show notes panel";
    public static final String TOGGLE_FILE_LIST = "Show file list";

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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
