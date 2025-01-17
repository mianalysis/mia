package io.github.mianalysis.mia.gui.regions.extrapanels.notes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;

/**
 * Created by Stephen on 28/07/2017.
 */
public class NotesArea extends JTextArea implements FocusListener {
    /**
     *
     */
    private static final long serialVersionUID = -2361836535673525320L;
    Module module;

    public NotesArea(Module module) {
        this.module = module;

        setFont(GUI.getDefaultFont().deriveFont(14f));

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
