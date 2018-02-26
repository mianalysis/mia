package wbif.sjx.ModularImageAnalysis.GUI.ParameterControls;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.MainGUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by sc13967 on 06/09/2017.
 */
public class ModuleName extends JTextField implements FocusListener {
    private MainGUI gui;
    private Module module;

    public ModuleName(MainGUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        setText(module.getNickname());
        setPreferredSize(new Dimension(535, 25));
        addFocusListener(this);

    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        module.setNickname(getText());
        gui.populateModuleList();

    }
}
