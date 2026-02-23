package io.github.mianalysis.mia.gui.regions.processingpanel;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.ModuleI;

public class ModuleTitle extends JLabel implements MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = 5328769382022849737L;
    private final ModuleI module;

    public ModuleTitle(ModuleI module) {
        this.module = module;

        setText(module.getNickname());
        addMouseListener(this);
        // setEditable(false);
        setBorder(null);
        Font font = GUI.getDefaultFont().deriveFont(Font.BOLD, 14f);
        setFont(font);
        setOpaque(false);
        // setForeground(Color.BLACK);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GUI.setSelectedModules(new ModuleI[] { module });

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
