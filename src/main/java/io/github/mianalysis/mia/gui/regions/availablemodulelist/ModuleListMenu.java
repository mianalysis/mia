package io.github.mianalysis.mia.gui.regions.availablemodulelist;

import java.awt.Font;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import io.github.mianalysis.mia.MIA;
import io.github.mianalysis.mia.module.IL2Support;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.system.Preferences;

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
        
        for (Module module : modules)
            add(new PopupMenuItem(module, topLevelMenu));

        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    }

    public void addMenuItem(Module module) {
        if (!module.isDeprecated() || MIA.preferences.showDeprecated())
            if (MIA.preferences.getDataStorageMode().equals(Preferences.DataStorageModes.KEEP_IN_RAM) || !module.getIL2Support().equals(IL2Support.NONE))
                add(new PopupMenuItem(module, topLevelMenu));
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
