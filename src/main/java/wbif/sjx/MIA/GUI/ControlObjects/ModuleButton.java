package wbif.sjx.MIA.GUI.ControlObjects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;

import wbif.sjx.MIA.GUI.GUI;
import wbif.sjx.MIA.Module.Module;
import wbif.sjx.MIA.Module.Miscellaneous.GUISeparator;
import wbif.sjx.MIA.Object.Colours;

/**
 * Created by Stephen on 20/05/2017.
 */
public class ModuleButton extends JToggleButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -7386345086449867288L;
    private Module module;


    // CONSTRUCTOR

    public ModuleButton(Module module) {
        this.module = module;
        setFocusPainted(false);
        setSelected(false);
        addActionListener(this);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setText(module.getNickname());
        setMinimumSize(new Dimension(1,30));
        setPreferredSize(new Dimension(1,30));
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
