package wbif.sjx.ModularImageAnalysis.GUI.ControlObjects;

import wbif.sjx.ModularImageAnalysis.GUI.Layouts.GUI;
import wbif.sjx.ModularImageAnalysis.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledButton extends JButton implements ActionListener {
    private GUI gui;
    private Module module;
    private boolean state = true;
    private static final ImageIcon redIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_red_12px.png"), "");
    private static final ImageIcon greenIcon = new ImageIcon(ModuleEnabledCheck.class.getResource("/Icons/power_green_12px.png"), "");

    public ModuleEnabledButton(GUI gui, Module module) {
        this.gui = gui;
        this.module = module;

        state = module.isEnabled();

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("ModuleEnabled");
        setIcon(state);

        addActionListener(this);

    }

    public void setIcon(boolean state) {
        if (state) setIcon(greenIcon);
        else setIcon(redIcon);
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        state = !state;

        setIcon(state);
        module.setEnabled(state);

        int idx = gui.getModules().indexOf(module);
        if (idx <= gui.getLastModuleEval()) {
            gui.setLastModuleEval(idx-1);
        }

        gui.updateModules();

    }
}
