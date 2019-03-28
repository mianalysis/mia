package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeSet;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleListMenu extends JMenu implements Comparable, MouseListener {
    private final JPopupMenu topLevelMenu;
    private TreeSet<ModuleListMenu> children = new TreeSet<>();

    public ModuleListMenu(String name, ArrayList<Module> modules, JPopupMenu topLevelMenu) {
        this.topLevelMenu = topLevelMenu;

        setText(name);
        for (Module module : modules) {
            add(new PopupMenuItem(module,topLevelMenu));
        }
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addMouseListener(this);

    }

    public void addMenuItem(Module module) {
        add(new PopupMenuItem(module,topLevelMenu));
    }

    public TreeSet<ModuleListMenu> getChildren() {
        return children;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        setVisible(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Adding the mouse listener to show the relevant sub-menu
//        GUI.getModuleListMenu().show(GUI.getFrame(), e.getX(), e.getY());
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public int compareTo(Object o) {
        String n1 = getText();
        String n2 = ((ModuleListMenu) o).getText();

        return n1.compareTo(n2);

    }
}
