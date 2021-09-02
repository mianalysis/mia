package io.github.mianalysis.MIA.GUI.Regions.BasicPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import io.github.mianalysis.MIA.GUI.GUI;
import io.github.mianalysis.MIA.Module.Module;

public class ModuleTitle extends JTextField implements MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = 5328769382022849737L;
    private final Module module;

    public ModuleTitle(Module module) {
        this.module = module;

        setText(module.getNickname());
        addMouseListener(this);
        setEditable(false);
        setBorder(null);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        setFont(font);
        setOpaque(false);
        setForeground(Color.BLACK);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GUI.setSelectedModules(new Module[] { module });
        GUI.updateHelpNotes();

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