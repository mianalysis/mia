package wbif.sjx.ModularImageAnalysis.GUI;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by steph on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    private MainGUI gui;

    NotesArea(MainGUI gui, String text) {
        this.gui = gui;
        setText(text);
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        gui.getActiveModule().setNotes(getText());

    }
}
