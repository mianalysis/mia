package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by sc13967 on 06/09/2017.
 */
public class ModuleName extends JTextField implements FocusListener {
    private Module module;

    public ModuleName(Module module) {
        this.module = module;

        setText(module.getNickname());
        setPreferredSize(new Dimension(-1, 25));
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        module.setNickname(getText());
        GUI.updateModuleStates();

    }
}
