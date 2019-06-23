package wbif.sjx.MIA.GUI.ModuleList;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseButton extends JButton implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        Point point = e.getPoint();

        System.out.println(point);
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
