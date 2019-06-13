package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuCheckbox extends JCheckBoxMenuItem implements ActionListener {
    public static final String TOGGLE_HELP_NOTES = "Show help and notes panel";
    public static final String TOGGLE_MEMORY_LOGGING = "Log memory usage";

    public MenuCheckbox(String command) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(command);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (getText()) {
                case TOGGLE_HELP_NOTES:
                    GUI.setShowHelpNotes(isSelected());
                    GUI.updatePanel();
                    GUI.populateHelpNotes();
                    break;

                case TOGGLE_MEMORY_LOGGING:
                    MIA.setLogMemory(isSelected());
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
