package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Stephen on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    Module module;

    public NotesArea(Module module) {
        this.module = module;

        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        if (module == null) {
            setText("");
        } else {
            setText(module.getNotes());
        }

        setLineWrap(true);
        setWrapStyleWord(true);
        addFocusListener(this);
        setCaretPosition(0);
        setBorder(new EmptyBorder(2,5,5,5));

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        GUI.addUndo();

        module.setNotes(getText());

    }
}
