package wbif.sjx.MIA.GUI.ControlObjects.ModuleList;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.GUI.ControlObjects.ModuleEnabledCheck;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.GUI.Icons.IconFactory;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.ModuleCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sc13967 on 07/06/2017.
 */
public class ModuleEnabledButton extends JButton implements ActionListener {
    private Module module;
    private static final ImageIcon blackIcon = new IconFactory(13,13).getPowerOff(Colours.BLACK_HEX);
    private static final ImageIcon redIcon = new IconFactory(13,13).getPowerOn(Colours.RED_HEX);
    private static final ImageIcon greenIcon = new IconFactory(13,13).getPowerOn(Colours.GREEN_HEX);

    public ModuleEnabledButton(Module module) {
        this.module = module;

        setFocusPainted(false);
        setSelected(false);
        setMargin(new Insets(0,0,0,0));
        setName("ModuleEnabled");
        setToolTipText("Enable/disable module");
        updateState();

        addActionListener(this);

    }

    public void updateState() {
        if (module.isEnabled() && module.isRunnable()) setIcon(greenIcon);
        else if (module.isEnabled() &! module.isRunnable()) setIcon(redIcon);
        else setIcon(blackIcon);
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Invert state
        module.setEnabled(!module.isEnabled());

        updateState();

        int idx = GUI.getModules().indexOf(module);
        if (idx <= GUI.getLastModuleEval()) GUI.setLastModuleEval(idx-1);

        // If this is a GUISeparator module, disable all modules after it, until the next separator
        ModuleCollection modules = GUI.getModules();
        if (module.getClass().isInstance(new GUISeparator(modules))) {
            for (int i=idx+1;i<modules.size();i++) {
                Module currentModule = modules.get(i);
                if (currentModule.getClass().isInstance(new GUISeparator(modules))) {
                    break;
                } else {
                    currentModule.setEnabled(module.isEnabled());
                }
            }
        }

        GUI.updateModules();
        GUI.updateModuleStates(true);

    }
}
