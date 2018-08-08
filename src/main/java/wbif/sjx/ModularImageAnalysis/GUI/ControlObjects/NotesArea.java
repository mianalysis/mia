package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by steph on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    public NotesArea(String text) {
        setText(text);
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
