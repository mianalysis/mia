package wbif.sjx.ModularImageAnalysis.GUI;

import wbif.sjx.ModularImageAnalysis.Module.HCModule;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Created by steph on 28/07/2017.
 */
public class ModuleListMenu extends JMenu implements MouseListener {
    private MainGUI gui;

    ModuleListMenu(MainGUI gui, String name, ArrayList<HCModule> modules) {
        this.gui = gui;

        setText(name);
        for (HCModule module : modules) add(new PopupMenuItem(gui,module));
        addMouseListener(this);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gui.getModuleListMenu().setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Adding the mouse listener to show the relevant sub-menu
        gui.getModuleListMenu().show(gui.getFrame(), e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
