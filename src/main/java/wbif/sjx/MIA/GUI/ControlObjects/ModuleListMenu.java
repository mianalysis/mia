package wbif.sjx.MIA.GUI.ControlObjects;

import java.awt.Font;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import wbif.sjx.MIA.Module.Module;

/**
 * Created by Stephen on 28/07/2017.
 */
public class ModuleListMenu extends JMenu implements Comparable {
    /**
     *
     */
    private static final long serialVersionUID = -4618976421020237449L;
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
