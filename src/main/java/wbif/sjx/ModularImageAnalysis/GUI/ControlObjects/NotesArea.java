package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    public NotesArea(String text) {
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        setText(text);
        setLineWrap(true);
        setWrapStyleWord(true);
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.getActiveModule().setNotes(getText());
    }
}
