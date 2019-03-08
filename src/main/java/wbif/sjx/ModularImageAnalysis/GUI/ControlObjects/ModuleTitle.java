package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ModuleTitle extends JTextField implements MouseListener {
    private final Module module;

    public ModuleTitle(Module module) {
        this.module = module;

        setText(module.getNickname());
        addMouseListener(this);
        setEditable(false);
        setBorder(null);
        setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
        setOpaque(false);
        setForeground(Color.BLACK);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GUI.setActiveModule(module);
        GUI.populateBasicHelpNotes();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
