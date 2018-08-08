package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by steph on 28/07/2017.
 */
public class ModuleListMenu extends JMenu implements MouseListener {
    private LinkedHashSet<ModuleListMenu> children = new LinkedHashSet<>();

    public ModuleListMenu(String name, ArrayList<Module> modules) {
        setText(name);
        for (Module module : modules) {
            add(new PopupMenuItem(module));
        }
        addMouseListener(this);

    }

    public void addMenuItem(Module module) {
        add(new PopupMenuItem(module));
    }

    public LinkedHashSet<ModuleListMenu> getChildren() {
        return children;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GUI.getModuleListMenu().setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Adding the mouse listener to show the relevant sub-menu
        GUI.getModuleListMenu().show(GUI.getFrame(), e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
