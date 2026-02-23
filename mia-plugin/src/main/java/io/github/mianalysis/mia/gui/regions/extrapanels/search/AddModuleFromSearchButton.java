package io.github.mianalysis.mia.gui.regions.extrapanels.search;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;

import io.github.mianalysis.mia.gui.GUI;
import io.github.mianalysis.mia.module.ModuleI;
import io.github.mianalysis.mia.module.ModulesI;
import io.github.mianalysis.mia.module.core.InputControl;
import io.github.mianalysis.mia.module.core.OutputControl;

public class AddModuleFromSearchButton extends JButton implements ActionListener {
    private ModuleI module;

    public AddModuleFromSearchButton(ModuleI module) {
        this.module = module;

        setText("+");
        setMargin(new Insets(0, 0, 1, 1));
        addActionListener(this);
        setFont(GUI.getDefaultFont().deriveFont(16f));
        setPreferredSize(new Dimension(26, 26));
        setMinimumSize(new Dimension(26, 26));
        setMaximumSize(new Dimension(26, 26));

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Adding it after the currently-selected module
        ModuleI newModule = null;
        try {
            ModulesI modules = GUI.getModules();
            newModule = module.getClass().getConstructor(ModulesI.class).newInstance(modules);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e1) {
            e1.printStackTrace();
        }

        ModuleI activeModule = GUI.getFirstSelectedModule();
        ModulesI modules = GUI.getModules();
        if (activeModule == null || activeModule.getClass().isInstance(new InputControl(modules))
                || activeModule.getClass().isInstance(new OutputControl(modules))) {
            GUI.getModules().add(newModule);
        } else {
            ModuleI[] activeModules = GUI.getSelectedModules();
            int idx = GUI.getModules().indexOf(activeModules[activeModules.length - 1]);
            GUI.getModules().addAtIndex(++idx, newModule);
        }

        // Adding to the list of modules
        GUI.setSelectedModules(new ModuleI[] { newModule });
        GUI.updateModules(true, activeModule);
        GUI.updateParameters(false, null);

    }
}
