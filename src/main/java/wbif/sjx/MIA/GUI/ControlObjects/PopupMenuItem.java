package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Object.ModuleCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Stephen on 20/05/2017.
 */
public class PopupMenuItem extends JMenuItem implements ActionListener {
    private JPopupMenu moduleListMenu;
    private Module module;

    public PopupMenuItem(Module module, JPopupMenu moduleListMenu) {
        this.module = module;
        this.moduleListMenu = moduleListMenu;

        setText(module.getName());
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addActionListener(this);

    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moduleListMenu.setVisible(false);

        if (module == null) return;

        // Adding it after the currently-selected module
        Module newModule = null;
        try {
            ModuleCollection modules = GUI.getModules();
            newModule = module.getClass().getConstructor(ModuleCollection.class).newInstance(modules);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e1) {
            e1.printStackTrace();
        }

        Module activeModule = GUI.getFirstSelectedModule();
        ModuleCollection modules = GUI.getModules();
        if (activeModule == null
                || activeModule.getClass().isInstance(new InputControl(modules))
                || activeModule.getClass().isInstance(new OutputControl(modules))) {
            GUI.getModules().add(newModule);
        } else {
            Module[] activeModules = GUI.getSelectedModules();
            int idx = GUI.getModules().indexOf(activeModules[activeModules.length-1]);
            GUI.getModules().add(++idx,newModule);
        }

        // Adding to the list of modules
        GUI.setSelectedModules(new Module[]{newModule});
        GUI.updateModules();
        GUI.updateModuleStates(true);
        GUI.updateModuleList();
        GUI.updateModuleParameters();
        GUI.updateHelpNotes();

        moduleListMenu.setVisible(false);

    }
}
