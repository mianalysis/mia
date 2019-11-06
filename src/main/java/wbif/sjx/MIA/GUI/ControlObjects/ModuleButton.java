package wbif.sjx.MIA.GUI.ControlObjects;

import wbif.sjx.MIA.GUI.Colours;
import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.MIA;
import wbif.sjx.MIA.Module.Hidden.InputControl;
import wbif.sjx.MIA.Module.Hidden.OutputControl;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Module.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    private Module module;


    // CONSTRUCTOR

    public ModuleButton(Module module) {
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(module.getNickname());
        updateState();
    }


    // PUBLIC METHODS

    public void updateState() {
        setText(module.getNickname());
        if (module.getClass() == GUISeparator.class) {
            setForeground(Colours.DARK_BLUE);
        } else if (module.isEnabled() && module.isRunnable()) {
            setForeground(Color.BLACK);
        } else if (module.isEnabled() &! module.isRunnable()) {
            setForeground(Colours.RED);
        } else {
            setForeground(Color.GRAY);
        }
    }


    // GETTERS

    public Module getModule() {
        return module;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUI.setSelectedModules(new Module[]{module});
        GUI.updateParameters();
        GUI.updateModules();
    }
}
