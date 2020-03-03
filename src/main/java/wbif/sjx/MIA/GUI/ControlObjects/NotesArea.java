package wbif.sjx.MIA.GUI.ControlObjects;

import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

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
