package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleListMenu extends JMenu implements Comparable {
    private final JPopupMenu topLevelMenu;
    private TreeSet<ModuleListMenu> children = new TreeSet<>();

    public ModuleListMenu(String name, ArrayList<Module> modules, JPopupMenu topLevelMenu) {
        this.topLevelMenu = topLevelMenu;

        setText(name);

        for (Module module : modules) {
            add(new PopupMenuItem(module,topLevelMenu));
        }
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    }

    public void addMenuItem(Module module) {
        add(new PopupMenuItem(module,topLevelMenu));
    }

    public TreeSet<ModuleListMenu> getChildren() {
        return children;
    }

    @Override
    public int compareTo(Object o) {
        String n1 = getText();
        String n2 = ((ModuleListMenu) o).getText();

        return n1.compareTo(n2);

    }
}
