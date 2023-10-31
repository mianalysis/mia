package io.github.mianalysis.mia.gui.regions.search;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.Modules;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;

public class AddModuleFromSearchButton extends JButton implements ActionListener {
    private Module module;

    public AddModuleFromSearchButton(Module module) {
        this.module = module;

        setText("+");
        setMargin(new Insets(0, 0, 1, 1));
        addActionListener(this);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        setPreferredSize(new Dimension(26, 26));
        setMinimumSize(new Dimension(26, 26));
        setMaximumSize(new Dimension(26, 26));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Adding it after the currently-selected module
        Module newModule = null;
        try {
            Modules modules = GUI.getModules();
            newModule = module.getClass().getConstructor(Modules.class).newInstance(modules);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e1) {
            e1.printStackTrace();
        }

        Module activeModule = GUI.getFirstSelectedModule();
        Modules modules = GUI.getModules();
        if (activeModule == null || activeModule.getClass().isInstance(new InputControl(modules))
                || activeModule.getClass().isInstance(new OutputControl(modules))) {
            GUI.getModules().add(newModule);
        } else {
            Module[] activeModules = GUI.getSelectedModules();
            int idx = GUI.getModules().indexOf(activeModules[activeModules.length - 1]);
            GUI.getModules().add(++idx, newModule);
        }

        // Adding to the list of modules
        GUI.setSelectedModules(new Module[] { newModule });
        GUI.updateModules();
        GUI.updateParameters();
        GUI.updateHelpNotes();

    }
}
